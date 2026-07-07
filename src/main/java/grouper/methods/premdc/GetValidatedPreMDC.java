/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.AgeConfictValidation;
import grouper.methods.validation.COUNTBMDCICD10CODE;
import grouper.methods.validation.Endovasc;
import grouper.methods.validation.GetBMDC;
import grouper.methods.validation.GetICD10PreMDC;
import grouper.methods.validation.TRAUMAICD10;
import grouper.methods.validation.TRAUMAICD9CM;
import grouper.structures.BMDCPreMDCResult;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetValidatedPreMDC {

    private final Logger logger = LogManager.getLogger(GetValidatedPreMDC.class);
    private final Utility utility = new Utility();

    public GetValidatedPreMDC() {
    }

    public DRGWSResult getValidatedPreMDC(
            final DataSource datasource,
            final String schemaName,
            final GrouperParameter grouperParameter) {

        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");

        String pdx = "";
        TRAUMAICD10 checkTraumaICD10 = new TRAUMAICD10();
        TRAUMAICD9CM checkTraumaICD9 = new TRAUMAICD9CM();
        Endovasc endoVasc = new Endovasc();
        AX checkAx = new AX();

        try {
            // 1. Parse Parameters cleanly and eliminate trailing whitespaces safely
            List<String> procedureList = grouperParameter.getProc() != null && !grouperParameter.getProc().trim().isEmpty()
                    ? Arrays.stream(grouperParameter.getProc().split(",")).map(String::trim).collect(Collectors.toList())
                    : Collections.emptyList();

            List<String> sDxList = grouperParameter.getSdx() != null && !grouperParameter.getSdx().trim().isEmpty()
                    ? Arrays.stream(grouperParameter.getSdx().split(",")).map(String::trim).collect(Collectors.toList())
                    : Collections.emptyList();

            DRGOutput drgResult = new DRGOutput();
            drgResult.setClaimseries(grouperParameter.getClaimseries());
            drgResult.setWarningerror(grouperParameter.getWarningerror());

            // Cache repeatedly calculated values
            int ageInYears = utility.ComputeYear(grouperParameter.getBirthDate(), grouperParameter.getAdmissionDate());
            int finalDays = (ageInYears > 0) ? (ageInYears * 365) : utility.ComputeDay(grouperParameter.getBirthDate(), grouperParameter.getAdmissionDate());

            // 2. Run Age Conflict Verification
            DRGWSResult getAgeConflictResult = new AgeConfictValidation().AgeConfictValidation(
                    datasource, schemaName, grouperParameter.getPdx(), String.valueOf(finalDays), String.valueOf(ageInYears));

            if (!getAgeConflictResult.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("PDx : " + grouperParameter.getPdx() + " Having conflict with age");
            } else {
                // 3. Process ICD10 Base Calculations
                DRGWSResult icd10SortResult = new GetICD10PreMDC().GetICD10(
                        datasource, schemaName, grouperParameter.getPdx(), String.valueOf(finalDays), String.valueOf(ageInYears), grouperParameter.getGender());
                ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(icd10SortResult.getResult(), ICD10PreMDCResult.class);

                int traumaCounterPDX0 = 0;
                int traumaCounterPDX1 = 0;
                DRGWSResult validatePdx = checkTraumaICD10.TRAUMAICD10(datasource, schemaName, grouperParameter.getPdx());

                if (validatePdx.isSuccess()) {
                    if (!"0".equals(validatePdx.getResult())) {
                        traumaCounterPDX1++;
                        pdx = validatePdx.getResult();
                    } else {
                        traumaCounterPDX0++;
                    }
                }

                // 4. Clean $O(N)$ Deduplication Strategy for SDX and Procedure Lists using Sets
                Set<String> uniqueSdxElements = new HashSet<>();
                Set<String> duplicateSdxIndices = new HashSet<>();
                List<String> sdxNewList = sDxList.stream()
                        .map(sdx -> checkTraumaICD10.TRAUMAICD10(datasource, schemaName, sdx))
                        .filter(res -> res.isSuccess() && !"0".equals(res.getResult()))
                        .map(DRGWSResult::getResult)
                        .collect(Collectors.toList());

                for (int i = 0; i < sdxNewList.size(); i++) {
                    if (!uniqueSdxElements.add(sdxNewList.get(i))) {
                        duplicateSdxIndices.add(String.valueOf(i));
                    }
                }

                final List<String> finalSdxNewList = new java.util.ArrayList<>();
                final List<String> sdxPdxNewList = new java.util.ArrayList<>();
                for (int i = 0; i < sdxNewList.size(); i++) {
                    if (!duplicateSdxIndices.contains(String.valueOf(i))) {
                        finalSdxNewList.add(sdxNewList.get(i));
                        sdxPdxNewList.add(sdxNewList.get(i));
                    }
                }
                if (!pdx.isEmpty()) {
                    sdxPdxNewList.remove(pdx);
                }

                // Process Procedures & Counter Accumulations
                int pdc0PB = 0, pdc0PD = 0, pdc0PA = 0;
                List<String> procNewList = new java.util.ArrayList<>();

                String timeAdm = utility.Convert24to12(grouperParameter.getTimeAdmission());
                String timeDis = utility.Convert24to12(grouperParameter.getTimeDischarge());

                for (String proc : procedureList) {
                    DRGWSResult checkProc = checkTraumaICD9.TRAUMAICD9CM(datasource, schemaName, proc);
                    if (checkProc.isSuccess()) {
                        procNewList.add(checkProc.getResult());
                    }
                    if (endoVasc.Endovasc(datasource, schemaName, proc, "0PB", "00").isSuccess() || endoVasc.Endovasc(datasource, schemaName, proc, "0PB", "0").isSuccess()) {
                        pdc0PB++;
                    }
                    if (endoVasc.Endovasc(datasource, schemaName, proc, "0PD", "00").isSuccess() || endoVasc.Endovasc(datasource, schemaName, proc, "0PD", "0").isSuccess()) {
                        pdc0PD++;
                    }
                    if (endoVasc.Endovasc(datasource, schemaName, proc, "0PA", "00").isSuccess() || endoVasc.Endovasc(datasource, schemaName, proc, "0PA", "0").isSuccess()) {
                        pdc0PA++;
                    }
                }

                Set<String> uniqueProcElements = new HashSet<>();
                Set<String> duplicateProcIndices = new HashSet<>();
                for (int i = 0; i < procNewList.size(); i++) {
                    if (!uniqueProcElements.add(procNewList.get(i))) {
                        duplicateProcIndices.add(String.valueOf(i));
                    }
                }
                List<String> finalProcNewList = new java.util.ArrayList<>();
                for (int i = 0; i < procNewList.size(); i++) {
                    if (!duplicateProcIndices.contains(String.valueOf(i))) {
                        finalProcNewList.add(procNewList.get(i));
                    }
                }

                int counter0CX = checkAx.AX(datasource, schemaName, "0CX", grouperParameter.getPdx()).isSuccess() ? 1 : 0;
                int counter0DX = checkAx.AX(datasource, schemaName, "0DX", grouperParameter.getPdx()).isSuccess() ? 1 : 0;
                int counter0EX = checkAx.AX(datasource, schemaName, "0EX", grouperParameter.getPdx()).isSuccess() ? 1 : 0;

                // 5. Consolidated Business Rules Branch (Eliminated duplication between getBmdcResult Success/Fail)
                GetBMDC checkBmdc = new GetBMDC();
                DRGWSResult getBmdcResult = checkBmdc.GetBMDC(datasource, schemaName, grouperParameter.getPdx());
                boolean isBmdcSuccess = getBmdcResult.isSuccess();
                BMDCPreMDCResult bmdcResult = isBmdcSuccess ? utility.objectMapper().readValue(getBmdcResult.getResult(), BMDCPreMDCResult.class) : null;

                int computedLos = utility.ComputeLOS(grouperParameter.getAdmissionDate(), timeAdm, grouperParameter.getDischargeDate(), timeDis);
                int computedTime = utility.ComputeTime(grouperParameter.getAdmissionDate(), timeAdm, grouperParameter.getDischargeDate(), timeDis);
                int minutesCompute = utility.MinutesCompute(grouperParameter.getAdmissionDate(), timeAdm, grouperParameter.getDischargeDate(), timeDis);
//                System.out.println("LOS : " + computedLos + " TIME : " + computedTime);
                if (ageInYears > 124) {
                    drgResult.setDRG("26509");
                    drgResult.setDC("2650");
                    drgResult.setDRGName("Invalid Age");
                } else if (computedLos <= 0 && computedTime < 24) {
                    int hoursLimit = isBmdcSuccess ? 2 : 6;
                    if (computedTime < hoursLimit) {
                        drgResult.setDRG("26549");
                        drgResult.setDRGName("LOS("+computedTime+"), The required Length of Stay (LOS) is at least 24 hours");
                    } else {
                        drgResult.setMDC("28");
                    }
                } else if (counter0CX > 0 && pdc0PA > 0) {
                    drgResult.setDC("0001");
                    drgResult.setDRG("00019");
                    drgResult.setMDC("00");
                    drgResult.setDRGName("Liver Transplant");
                } else if (counter0DX > 0 && pdc0PB > 0) {
                    drgResult.setDC("0002");
                    drgResult.setDRG("00029");
                    drgResult.setMDC("00");
                    drgResult.setDRGName("Heart-Lung Transplant");
                } else if (counter0EX > 0 && pdc0PD > 0) {
                    drgResult.setDC("0004");
                    drgResult.setDRG("00049");
                    drgResult.setMDC("00");
                    drgResult.setDRGName("Bone Marrow Transplant");
                } else if (traumaCounterPDX0 > 0 && finalSdxNewList.size() >= 2) {
                    drgResult.setMDC("24");
                } else if (traumaCounterPDX0 > 0 && finalProcNewList.size() >= 2) {
                    drgResult.setMDC("24");
                } else if (traumaCounterPDX1 > 0 && sdxPdxNewList.size() >= 1) {
                    drgResult.setMDC("24");
                } else if (traumaCounterPDX1 > 0 && finalProcNewList.size() >= 2) {
                    drgResult.setMDC("24");
                } else if (icd10Result.getPDC() != null && "25A".equalsIgnoreCase(icd10Result.getPDC())) {
                    drgResult.setMDC("25");
                } else if (ageInYears <= 0 && utility.ComputeDay(grouperParameter.getBirthDate(), grouperParameter.getAdmissionDate()) < 28) {
                    drgResult.setMDC("15");
                } else if (isBmdcSuccess && new COUNTBMDCICD10CODE().COUNTBMDCICD10CODE(datasource, schemaName, grouperParameter.getPdx()).isSuccess()) {
                    String gender = grouperParameter.getGender().toUpperCase();
                    if (bmdcResult.getICD10().equals(grouperParameter.getPdx()) && "M".equals(gender)) {
                        drgResult.setMDC(bmdcResult.getMDC_M());
                        drgResult.setPDC(bmdcResult.getPDC_M());
                    } else if (bmdcResult.getICD10().equals(grouperParameter.getPdx()) && "F".equals(gender)) {
                        drgResult.setMDC(bmdcResult.getMDC_F());
                        drgResult.setPDC(bmdcResult.getPDC_F());
                    }
                } else {
                    drgResult.setMDC(icd10Result.getMDC());
                    drgResult.setPDC(icd10Result.getPDC());
                }
            }

            // 6. Final Assignment Process
            ProcessMDC getMDC = new ProcessMDC();
            if (drgResult.getDRG() == null) {
                if ("30".equals(drgResult.getMDC())) {
                    if (drgResult.getPDC() == null || drgResult.getPDC().isEmpty()) {
                        drgResult.setDRG("26519");
                        drgResult.setDC("2651");
                        drgResult.setDRGName("UNACCEPTABLE PRINCIPAL DIAGNOSIS");
                        result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                        result.setSuccess(true);
                        result.setMessage("Grouper Done in Pre-MDC level only");
                    } else {
                        DRGOutput drgResultsUnwrapped = new DRGOutput();
                        drgResultsUnwrapped.setCC(drgResult.getCC());
                        drgResultsUnwrapped.setDC(drgResult.getDC());
                        drgResultsUnwrapped.setDRG(drgResult.getDRG());
                        drgResultsUnwrapped.setWarningerror(grouperParameter.getWarningerror());
                        drgResultsUnwrapped.setDRGName(drgResult.getDRGName());

                        int substringLen = drgResult.getPDC().length() > 2 ? 2 : 1;
                        drgResultsUnwrapped.setMDC(drgResult.getPDC().substring(0, substringLen));

                        drgResultsUnwrapped.setMDF(drgResult.getMDF());
                        drgResultsUnwrapped.setOT(drgResult.getOT());
                        drgResultsUnwrapped.setPDC(drgResult.getPDC());
                        drgResultsUnwrapped.setRW(drgResult.getRW());
                        drgResultsUnwrapped.setClaimseries(grouperParameter.getClaimseries());

                        result = getMDC.ProcessMDC(datasource, schemaName, drgResultsUnwrapped, grouperParameter);
                    }
                } else {
                    result = getMDC.ProcessMDC(datasource, schemaName, drgResult, grouperParameter);
                }
            } else {
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
                result.setMessage("Grouper Done in Pre-MDC level only");
            }

        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.error("Error in Pre-MDC Validation Method: {}", ex.getMessage(), ex);
        }

        return result;
    }
}
//public class GetValidatedPreMDC {
//
//    public GetValidatedPreMDC() {
//    }
//
//    private final Logger logger = (Logger) LogManager.getLogger(GetValidatedPreMDC.class);
//    private final Utility utility = new Utility();
//
//    public DRGWSResult GetValidatedPreMDC(
//            final DataSource datasource,
//            final String SchemaName,
//            final GrouperParameter grouperparameter) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setSuccess(false);
//        result.setMessage("");
//        result.setResult("");
//        String pdx = "";
//        TRAUMAICD10 checkTraumaICD10 = new TRAUMAICD10();
//        TRAUMAICD9CM checkTraumaICD9 = new TRAUMAICD9CM();
//        Endovasc endoVasc = new Endovasc();
//        AX checkAx = new AX();
//        try {
//            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().trim().split(","));
//            List<String> SDxList = Arrays.asList(grouperparameter.getSdx().trim().split(","));
//            DRGOutput drgResult = new DRGOutput();
//            drgResult.setClaimseries(grouperparameter.getClaimseries());
//            drgResult.setWarningerror(grouperparameter.getWarningerror());
//            int finaldays = 0;
//            if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
//                finaldays = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) * 365;
//            } else {
//                finaldays = utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
//            }
//            DRGWSResult getAgeConfictResult = new AgeConfictValidation().AgeConfictValidation(datasource,
//                    SchemaName,
//                    grouperparameter.getPdx(),
//                    String.valueOf(finaldays),
//                    String.valueOf(utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate())));
//            if (!getAgeConfictResult.isSuccess()) {
//                drgResult.setDRG("26509");
//                drgResult.setDC("2650");
//                drgResult.setDRGName("PDx : " + grouperparameter.getPdx() + " Having conflict with age");
//            } else {
//                DRGWSResult icd10SortResult = new GetICD10PreMDC().GetICD10(datasource,
//                        SchemaName,
//                        grouperparameter.getPdx(), String.valueOf(finaldays), String.valueOf(utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate())), grouperparameter.getGender());
//                ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(icd10SortResult.getResult(), ICD10PreMDCResult.class);
//                int TraumaCounterPDXO = 0;
//                int TraumaCounterPDX1 = 0;
//                DRGWSResult validatePdx = checkTraumaICD10.TRAUMAICD10(datasource, SchemaName, grouperparameter.getPdx());
//                if (validatePdx.isSuccess()) {
//                    if (!validatePdx.getResult().equals("0")) {
//                        TraumaCounterPDX1++;
//                        pdx = validatePdx.getResult();
//                    } else {
//                        TraumaCounterPDXO++;
//                    }
//                }
//                LinkedList<String> procSite = new LinkedList<>();
//                LinkedList<String> sdxSite = new LinkedList<>();
//                LinkedList<String> sdxnewlist = new LinkedList<>();
//                LinkedList<String> finalsdxnewlist = new LinkedList<>();
//                LinkedList<String> dupsdxnewlist = new LinkedList<>();
//                LinkedList<String> procnewlist = new LinkedList<>();
//                LinkedList<String> finalprocnewlist = new LinkedList<>();
//                LinkedList<String> dupprocnewlist = new LinkedList<>();
//                LinkedList<String> sdxpdxnewlist = new LinkedList<>();
//                if (SDxList.size() > 0) {
//                    for (int x = 0; x < SDxList.size(); x++) {
//                        DRGWSResult SDxVal = checkTraumaICD10.TRAUMAICD10(datasource, SchemaName, SDxList.get(x).trim());
//                        if (SDxVal.isSuccess()) {
//                            if (!SDxVal.getResult().equals("0")) {
//                                sdxSite.add(SDxVal.getResult());
//                                sdxnewlist.add(SDxVal.getResult());
//                            }
//                        }
//                    }
//                }
//                int Counter0CX = 0;
//                int Counter0DX = 0;
//                int Counter0EX = 0;
//                int PDC0PB = 0;
//                int PDC0PD = 0;
//                int PDC0PA = 0;
//                if (checkAx.AX(datasource, SchemaName, "0CX", grouperparameter.getPdx()).isSuccess()) {
//                    Counter0CX++;
//                }
//                if (checkAx.AX(datasource, SchemaName, "0DX", grouperparameter.getPdx()).isSuccess()) {
//                    Counter0DX++;
//                }
//                if (checkAx.AX(datasource, SchemaName, "0EX", grouperparameter.getPdx()).isSuccess()) {
//                    Counter0EX++;
//                }
//                if (ProcedureList.size() > 0) {
//                    for (int x = 0; x < ProcedureList.size(); x++) {
//                        DRGWSResult checkProc = checkTraumaICD9.TRAUMAICD9CM(datasource, SchemaName, ProcedureList.get(x).trim());
//                        if (checkProc.isSuccess()) {
//                            procSite.add(checkProc.getResult());
//                            procnewlist.add(checkProc.getResult());
//                        }
//                        if (endoVasc.Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "0PB", "00").isSuccess()
//                                || endoVasc.Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "0PB", "0").isSuccess()) {
//                            PDC0PB++;
//                        }
//                        if (endoVasc.Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "0PD", "00").isSuccess()
//                                || endoVasc.Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "0PD", "0").isSuccess()) {
//                            PDC0PD++;
//                        }
//                        if (endoVasc.Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "0PA", "00").isSuccess()
//                                || endoVasc.Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "0PA", "0").isSuccess()) {
//                            PDC0PA++;
//                        }
//                    }
//                }
//                //Proc Validation for MDC 24
//                for (int i = 0; i < procSite.size() - 1; i++) {
//                    for (int j = i + 1; j < procSite.size(); j++) {
//                        if (procSite.get(i).equals(procSite.get(j)) && (i != j)) {
//                            dupprocnewlist.add(String.valueOf(j));
//                        }
//                    }
//                }
//                //SDX Validation for MDC 24
//                for (int i = 0; i < sdxSite.size() - 1; i++) {
//                    for (int j = i + 1; j < sdxSite.size(); j++) {
//                        if (sdxSite.get(i).equals(sdxSite.get(j)) && (i != j)) {
//                            dupsdxnewlist.add(String.valueOf(j));
//                        }
//                    }
//                }
//                //-------------------------------------------------------------
//                for (int i = 0; i < sdxnewlist.size(); i++) {
//                    int indexvalue = dupsdxnewlist.indexOf(String.valueOf(i));
//                    if (indexvalue >= 0) {
//                    } else {
//                        finalsdxnewlist.add(sdxnewlist.get(i));
//                        sdxpdxnewlist.add(sdxnewlist.get(i));
//                    }
//                }
//                for (int i = 0; i < procnewlist.size(); i++) {
//                    int indexvalue = dupprocnewlist.indexOf(String.valueOf(i));
//                    if (indexvalue >= 0) {
//                    } else {
//                        finalprocnewlist.add(procnewlist.get(i));
//                    }
//                }
//                //-------------------------------------------------
//                if (!pdx.isEmpty()) {
//                    sdxpdxnewlist.remove(pdx);
//                }
//                // START OF PARSING PART
//                GetBMDC checkBmdc = new GetBMDC();
//                DRGWSResult getBmdcResult = checkBmdc.GetBMDC(datasource, SchemaName, grouperparameter.getPdx());
//                if (getBmdcResult.isSuccess()) {
////                    DRGWSResult restA = new GrouperMethod().COUNTBMDCICD10CODE(datasource, grouperparameter.getPdx());
//                    BMDCPreMDCResult bmdcResult = utility.objectMapper().readValue(getBmdcResult.getResult(), BMDCPreMDCResult.class);
//                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 124) {
//                        drgResult.setDRG("26509");
//                        drgResult.setDC("2650");
//                        drgResult.setDRGName("Invalid Age");
//                    } else if (utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()), grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0
//                            && utility.ComputeTime(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()), grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 6
//                            && utility.MinutesCompute(grouperparameter.getAdmissionDate(),
//                                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                                    grouperparameter.getDischargeDate(),
//                                    utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0) {
//                        if (utility.ComputeTime(grouperparameter.getAdmissionDate(),
//                                utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                                grouperparameter.getDischargeDate(),
//                                utility.Convert24to12(grouperparameter.getTimeDischarge())) < 2) {
//                            drgResult.setDRG("26549");
//                            drgResult.setDRGName("LOS < 24 Hours");
//                        } else {
//                            drgResult.setMDC("28");
//                        }
//                    } else if (Counter0CX > 0 && PDC0PA > 0) { //Liver Transplant
//                        drgResult.setDC("0001");
//                        drgResult.setDRG("00019");
//                        drgResult.setMDC("00");
//                        drgResult.setDRGName("Liver Transplant");
//                    } else if (Counter0DX > 0 && PDC0PB > 0) {//Heart-Lung Transplant
//                        drgResult.setDC("0002");
//                        drgResult.setDRG("00029");
//                        drgResult.setMDC("00");
//                        drgResult.setDRGName("Heart-Lung Transplant");
//                    } else if (Counter0EX > 0 && PDC0PD > 0) {//Bone Marrow Transplant
//                        drgResult.setDC("0004");
//                        drgResult.setDRG("00049");
//                        drgResult.setMDC("00");
//                        drgResult.setDRGName("Bone Marrow Transplant");
//                        //TRAUMA CHECKING AREA 
//                    } else if (TraumaCounterPDXO > 0 && finalsdxnewlist.size() >= 2) {
//                        drgResult.setMDC("24");
//                    } else if (TraumaCounterPDXO > 0 && finalprocnewlist.size() >= 2) {
//                        drgResult.setMDC("24");
//                    } else if (TraumaCounterPDX1 > 0 && sdxpdxnewlist.size() >= 1) {
//                        drgResult.setMDC("24");
//                    } else if (TraumaCounterPDX1 > 0 && finalprocnewlist.size() >= 2) {
//                        drgResult.setMDC("24");
//                        //TRAUMA CHECKING AREA  
//                    } else if (icd10Result.getPDC() != null && icd10Result.getPDC().toUpperCase().equals("25A")) {
//                        drgResult.setMDC("25");
//                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) == 0
//                            && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) < 28) {
//                        drgResult.setMDC("15");
//                    } else if (new COUNTBMDCICD10CODE().COUNTBMDCICD10CODE(datasource, SchemaName, grouperparameter.getPdx()).isSuccess()) {
//                        if (bmdcResult.getICD10().equals(grouperparameter.getPdx()) && grouperparameter.getGender().toUpperCase().equals("M")) {
//                            drgResult.setMDC(bmdcResult.getMDC_M());
//                            drgResult.setPDC(bmdcResult.getPDC_M());
//                        } else if (bmdcResult.getICD10().equals(grouperparameter.getPdx()) && grouperparameter.getGender().toUpperCase().equals("F")) {
//                            drgResult.setMDC(bmdcResult.getMDC_F());
//                            drgResult.setPDC(bmdcResult.getPDC_F());
//                        }
//                    } else {
//                        drgResult.setMDC(icd10Result.getMDC());
//                        drgResult.setPDC(icd10Result.getPDC());
//                    }
//                } else {
//                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 124) {
//                        drgResult.setDRG("26509");
//                        drgResult.setDRGName("Invalid Age");
//                    } else if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
//                            utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                            grouperparameter.getDischargeDate(),
//                            utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0
//                            && utility.ComputeTime(grouperparameter.getAdmissionDate(),
//                                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                                    grouperparameter.getDischargeDate(),
//                                    utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 6
//                            && utility.MinutesCompute(grouperparameter.getAdmissionDate(),
//                                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                                    grouperparameter.getDischargeDate(),
//                                    utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0) {
//
//                        if (utility.ComputeTime(grouperparameter.getAdmissionDate(),
//                                utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                                grouperparameter.getDischargeDate(),
//                                utility.Convert24to12(grouperparameter.getTimeDischarge())) < 6) {
//                            drgResult.setDRG("26549");
//                            drgResult.setDRGName("LOS < 6 Hours");
//                        } else {
//                            drgResult.setMDC("28");
//                        }
//                    } else if (Counter0CX > 0 && PDC0PA > 0) { //Liver Transplant
//                        drgResult.setDC("0001");
//                        drgResult.setDRG("00019");
//                        drgResult.setMDC("00");
//                        drgResult.setDRGName("Liver Transplant");
//                    } else if (Counter0DX > 0 && PDC0PB > 0) {//Heart-Lung Transplant
//                        drgResult.setDC("0002");
//                        drgResult.setDRG("00029");
//                        drgResult.setMDC("00");
//                        drgResult.setDRGName("Heart-Lung Transplant");
//                    } else if (Counter0EX > 0 && PDC0PD > 0) {//Bone Marrow Transplant
//                        drgResult.setDC("0004");
//                        drgResult.setDRG("00049");
//                        drgResult.setMDC("00");
//                        drgResult.setDRGName("Bone Marrow Transplant");
//                        //TRAUMA CHECKING AREA 
//                    } else if (TraumaCounterPDXO > 0 && finalsdxnewlist.size() >= 2) {
//                        drgResult.setMDC("24");
//                    } else if (TraumaCounterPDXO > 0 && finalprocnewlist.size() >= 2) {
//                        drgResult.setMDC("24");
//                    } else if (TraumaCounterPDX1 > 0 && sdxpdxnewlist.size() >= 1) {
//                        drgResult.setMDC("24");
//                    } else if (TraumaCounterPDX1 > 0 && finalprocnewlist.size() >= 2) {
//                        drgResult.setMDC("24");
//                        //TRAUMA CHECKING AREA    
//                    } else if (icd10Result.getPDC() != null && icd10Result.getPDC().toUpperCase().equals("25A")) {
//                        drgResult.setMDC("25");
//                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(),
//                            grouperparameter.getAdmissionDate()) <= 0 && utility.ComputeDay(grouperparameter.getBirthDate(),
//                            grouperparameter.getAdmissionDate()) < 28) {
//                        drgResult.setMDC("15");
//                    } else {
//                        drgResult.setMDC(icd10Result.getMDC());
//                        drgResult.setPDC(icd10Result.getPDC());
//
//                    }
//                }
//            }
//            //END OF PARSING PART
//            ProcessMDC getMDC = new ProcessMDC();
//            if (drgResult.getDRG() == null) {
//
//                if (drgResult.getMDC().equals("30")) {
//
//                    if (drgResult.getPDC().isEmpty()) {
//                        drgResult.setDRG("26519");
//                        drgResult.setDC("2651");
//                        drgResult.setDRGName("UNACCEPTABLE PRINCIPAL DIAGNOSIS");
//                        result.setResult(utility.objectMapper().writeValueAsString(drgResult));
//                        result.setSuccess(true);
//                        result.setMessage("Grouper Done in Pre-MDC level only");
//                    } else {
//                        DRGOutput drgResults = new DRGOutput();
//                        drgResults.setCC(drgResult.getCC());
//                        drgResults.setDC(drgResult.getDC());
//                        drgResults.setDRG(drgResult.getDRG());
//                        drgResults.setWarningerror(grouperparameter.getWarningerror());
//                        drgResults.setDRGName(drgResult.getDRGName());
//                        if (drgResult.getPDC().length() > 2) {
//                            drgResults.setMDC(drgResult.getPDC().substring(0, 2));
//                        } else {
//                            drgResults.setMDC(drgResult.getPDC().substring(0, 1));
//                        }
//                        drgResults.setMDF(drgResult.getMDF());
//                        drgResults.setOT(drgResult.getOT());
//                        drgResults.setPDC(drgResult.getPDC());
//                        drgResults.setRW(drgResult.getRW());
//                        drgResults.setClaimseries(grouperparameter.getClaimseries());
////                        //-----------------------------------------------
//                        result = getMDC.ProcessMDC(datasource, SchemaName, drgResults, grouperparameter);
//                    }
//                } else {
//                    result = getMDC.ProcessMDC(datasource, SchemaName, drgResult, grouperparameter);
//                }
//
//            } else {
//                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
//                result.setSuccess(true);
//                result.setMessage("Grouper Done in Pre-MDC level only");
//            }
//        } catch (IOException ex) {
//            result.setMessage("Something went wrong");
//            logger.info("Executing Pre-MDC Validation Method");
//            logger.error("Error in Pre-MDC Validation Method : {}", ex.getMessage(), ex);
//        }
//
//        return result;
//    }
//}
