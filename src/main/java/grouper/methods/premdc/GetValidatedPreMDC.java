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
import java.util.LinkedList;
import java.util.List;
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
    
    public GetValidatedPreMDC() {
    }
    
    private final Logger logger = (Logger) LogManager.getLogger(GetValidatedPreMDC.class);
    private final Utility utility = new Utility();
    
    public DRGWSResult GetValidatedPreMDC(final DataSource datasource, final GrouperParameter grouperparameter) {
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
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().trim().split(","));
            List<String> SDxList = Arrays.asList(grouperparameter.getSdx().trim().split(","));
            DRGOutput drgResult = new DRGOutput();
            drgResult.setClaimseries(grouperparameter.getClaimseries());
            drgResult.setWarningerror(grouperparameter.getWarningerror());
            int finaldays = 0;
            if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                finaldays = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) * 365;
            } else {
                finaldays = utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
            }
            DRGWSResult getAgeConfictResult = new AgeConfictValidation().AgeConfictValidation(datasource,
                    grouperparameter.getPdx(),
                    String.valueOf(finaldays),
                    String.valueOf(utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate())));
            if (!getAgeConfictResult.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("PDx : " + grouperparameter.getPdx() + " Having conflict with age");
            } else {
                DRGWSResult icd10SortResult = new GetICD10PreMDC().GetICD10(datasource, grouperparameter.getPdx(), String.valueOf(finaldays), String.valueOf(utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate())), grouperparameter.getGender());
                ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(icd10SortResult.getResult(), ICD10PreMDCResult.class);
                int TraumaCounterPDXO = 0;
                int TraumaCounterPDX1 = 0;
                DRGWSResult validatePdx = checkTraumaICD10.TRAUMAICD10(datasource, grouperparameter.getPdx());
                if (validatePdx.isSuccess()) {
                    if (!validatePdx.getResult().equals("0")) {
                        TraumaCounterPDX1++;
                        pdx = validatePdx.getResult();
                    } else {
                        TraumaCounterPDXO++;
                    }
                }
                LinkedList<String> procSite = new LinkedList<>();
                LinkedList<String> sdxSite = new LinkedList<>();
                LinkedList<String> sdxnewlist = new LinkedList<>();
                LinkedList<String> finalsdxnewlist = new LinkedList<>();
                LinkedList<String> dupsdxnewlist = new LinkedList<>();
                LinkedList<String> procnewlist = new LinkedList<>();
                LinkedList<String> finalprocnewlist = new LinkedList<>();
                LinkedList<String> dupprocnewlist = new LinkedList<>();
                LinkedList<String> sdxpdxnewlist = new LinkedList<>();
                if (SDxList.size() > 0) {
                    for (int x = 0; x < SDxList.size(); x++) {
                        DRGWSResult SDxVal = checkTraumaICD10.TRAUMAICD10(datasource, SDxList.get(x).trim());
                        if (SDxVal.isSuccess()) {
                            if (!SDxVal.getResult().equals("0")) {
                                sdxSite.add(SDxVal.getResult());
                                sdxnewlist.add(SDxVal.getResult());
                            }
                        }
                    }
                }
                int Counter0CX = 0;
                int Counter0DX = 0;
                int Counter0EX = 0;
                int PDC0PB = 0;
                int PDC0PD = 0;
                int PDC0PA = 0;
                
                if (checkAx.AX(datasource, "0CX", grouperparameter.getPdx()).isSuccess()) {
                    Counter0CX++;
                }
                if (checkAx.AX(datasource, "0DX", grouperparameter.getPdx()).isSuccess()) {
                    Counter0DX++;
                }
                if (checkAx.AX(datasource, "0EX", grouperparameter.getPdx()).isSuccess()) {
                    Counter0EX++;
                }
                if (ProcedureList.size() > 0) {
                    for (int x = 0; x < ProcedureList.size(); x++) {
                        DRGWSResult checkProc = checkTraumaICD9.TRAUMAICD9CM(datasource, ProcedureList.get(x).trim());
                        if (checkProc.isSuccess()) {
                            procSite.add(checkProc.getResult());
                            procnewlist.add(checkProc.getResult());
                        }
                        if (endoVasc.Endovasc(datasource, ProcedureList.get(x).trim(), "0PB", "0").isSuccess()) {
                            PDC0PB++;
                        }
                        if (endoVasc.Endovasc(datasource, ProcedureList.get(x).trim(), "0PD", "0").isSuccess()) {
                            PDC0PD++;
                        }
                        if (endoVasc.Endovasc(datasource, ProcedureList.get(x).trim(), "0PA", "0").isSuccess()) {
                            PDC0PA++;
                        }
                        
                    }
                }
                //Proc Validation for MDC 24
                for (int i = 0; i < procSite.size() - 1; i++) {
                    for (int j = i + 1; j < procSite.size(); j++) {
                        if (procSite.get(i).equals(procSite.get(j)) && (i != j)) {
                            dupprocnewlist.add(String.valueOf(j));
                        }
                    }
                }
                //SDX Validation for MDC 24
                for (int i = 0; i < sdxSite.size() - 1; i++) {
                    for (int j = i + 1; j < sdxSite.size(); j++) {
                        if (sdxSite.get(i).equals(sdxSite.get(j)) && (i != j)) {
                            dupsdxnewlist.add(String.valueOf(j));
                        }
                    }
                }
                //-------------------------------------------------------------
                for (int i = 0; i < sdxnewlist.size(); i++) {
                    int indexvalue = dupsdxnewlist.indexOf(String.valueOf(i));
                    if (indexvalue >= 0) {
                    } else {
                        finalsdxnewlist.add(sdxnewlist.get(i));
                        sdxpdxnewlist.add(sdxnewlist.get(i));
                    }
                }
                for (int i = 0; i < procnewlist.size(); i++) {
                    int indexvalue = dupprocnewlist.indexOf(String.valueOf(i));
                    if (indexvalue >= 0) {
                    } else {
                        finalprocnewlist.add(procnewlist.get(i));
                    }
                }
                //-------------------------------------------------
                if (!pdx.isEmpty()) {
                    sdxpdxnewlist.remove(pdx);
                }
                // START OF PARSING PART
                GetBMDC checkBmdc = new GetBMDC();
                DRGWSResult getBmdcResult = checkBmdc.GetBMDC(datasource, grouperparameter.getPdx());
                if (getBmdcResult.isSuccess()) {
//                    DRGWSResult restA = new GrouperMethod().COUNTBMDCICD10CODE(datasource, grouperparameter.getPdx());
                    BMDCPreMDCResult bmdcResult = utility.objectMapper().readValue(getBmdcResult.getResult(), BMDCPreMDCResult.class);
                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 124) {
                        drgResult.setDRG("26509");
                        drgResult.setDC("2650");
                        drgResult.setDRGName("Invalid Age");
                    } else if (utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()), grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0
                            && utility.ComputeTime(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()), grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 6
                            && utility.MinutesCompute(grouperparameter.getAdmissionDate(), 
                                    utility.Convert24to12(grouperparameter.getTimeAdmission()), 
                                    grouperparameter.getDischargeDate(), 
                                    utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0) {
                        if (utility.ComputeTime(grouperparameter.getAdmissionDate(),
                                utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                grouperparameter.getDischargeDate(),
                                utility.Convert24to12(grouperparameter.getTimeDischarge())) < 2) {
                            drgResult.setDRG("26549");
                            drgResult.setDRGName("LOS < 24 Hours");
                        } else {
                            drgResult.setMDC("28");
                        }
                    } else if (Counter0CX > 0 && PDC0PA > 0) { //Liver Transplant
                        drgResult.setDC("0001");
                        drgResult.setDRG("00019");
                        drgResult.setMDC("00");
                        drgResult.setDRGName("Liver Transplant");
                    } else if (Counter0DX > 0 && PDC0PB > 0) {//Heart-Lung Transplant
                        drgResult.setDC("0002");
                        drgResult.setDRG("00029");
                        drgResult.setMDC("00");
                        drgResult.setDRGName("Heart-Lung Transplant");
                    } else if (Counter0EX > 0 && PDC0PD > 0) {//Bone Marrow Transplant
                        drgResult.setDC("0004");
                        drgResult.setDRG("00049");
                        drgResult.setMDC("00");
                        drgResult.setDRGName("Bone Marrow Transplant");
                        //TRAUMA CHECKING AREA 
                    } else if (TraumaCounterPDXO > 0 && finalsdxnewlist.size() >= 2) {
                        drgResult.setMDC("24");
                    } else if (TraumaCounterPDXO > 0 && finalprocnewlist.size() >= 2) {
                        drgResult.setMDC("24");
                    } else if (TraumaCounterPDX1 > 0 && sdxpdxnewlist.size() >= 1) {
                        drgResult.setMDC("24");
                    } else if (TraumaCounterPDX1 > 0 && finalprocnewlist.size() >= 2) {
                        drgResult.setMDC("24");
                        //TRAUMA CHECKING AREA  
                    } else if (icd10Result.getPDC() != null && icd10Result.getPDC().equals("25A")) {
                        drgResult.setMDC("25");
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) == 0
                            && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) < 28) {
                        drgResult.setMDC("15");
                    } else if (new COUNTBMDCICD10CODE().COUNTBMDCICD10CODE(datasource, grouperparameter.getPdx()).isSuccess()) {
                        if (bmdcResult.getICD10().equals(grouperparameter.getPdx()) && grouperparameter.getGender().equals("M")) {
                            drgResult.setMDC(bmdcResult.getMDC_M());
                            drgResult.setPDC(bmdcResult.getPDC_M());
                        } else if (bmdcResult.getICD10().equals(grouperparameter.getPdx()) && grouperparameter.getGender().equals("F")) {
                            drgResult.setMDC(bmdcResult.getMDC_F());
                            drgResult.setPDC(bmdcResult.getPDC_F());
                        }
                    } else {
                        drgResult.setMDC(icd10Result.getMDC());
                        drgResult.setPDC(icd10Result.getPDC());
                    }
                } else {
                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 124) {
                        drgResult.setDRG("26509");
                        drgResult.setDRGName("Invalid Age");
                    } else if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                            utility.Convert24to12(grouperparameter.getTimeAdmission()),
                            grouperparameter.getDischargeDate(),
                            utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0
                            && utility.ComputeTime(grouperparameter.getAdmissionDate(),
                                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                    grouperparameter.getDischargeDate(),
                                    utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 6
                            && utility.MinutesCompute(grouperparameter.getAdmissionDate(),
                                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                    grouperparameter.getDischargeDate(),
                                    utility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0) {
                        
                        if (utility.ComputeTime(grouperparameter.getAdmissionDate(),
                                utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                grouperparameter.getDischargeDate(),
                                utility.Convert24to12(grouperparameter.getTimeDischarge())) < 6) {
                            drgResult.setDRG("26549");
                            drgResult.setDRGName("LOS < 6 Hours");
                        } else {
                            drgResult.setMDC("28");
                        }
                    } else if (Counter0CX > 0 && PDC0PA > 0) { //Liver Transplant
                        drgResult.setDC("0001");
                        drgResult.setDRG("00019");
                        drgResult.setMDC("00");
                        drgResult.setDRGName("Liver Transplant");
                    } else if (Counter0DX > 0 && PDC0PB > 0) {//Heart-Lung Transplant
                        drgResult.setDC("0002");
                        drgResult.setDRG("00029");
                        drgResult.setMDC("00");
                        drgResult.setDRGName("Heart-Lung Transplant");
                    } else if (Counter0EX > 0 && PDC0PD > 0) {//Bone Marrow Transplant
                        drgResult.setDC("0004");
                        drgResult.setDRG("00049");
                        drgResult.setMDC("00");
                        drgResult.setDRGName("Bone Marrow Transplant");
                        //TRAUMA CHECKING AREA 
                    } else if (TraumaCounterPDXO > 0 && finalsdxnewlist.size() >= 2) {
                        drgResult.setMDC("24");
                    } else if (TraumaCounterPDXO > 0 && finalprocnewlist.size() >= 2) {
                        drgResult.setMDC("24");
                    } else if (TraumaCounterPDX1 > 0 && sdxpdxnewlist.size() >= 1) {
                        drgResult.setMDC("24");
                    } else if (TraumaCounterPDX1 > 0 && finalprocnewlist.size() >= 2) {
                        drgResult.setMDC("24");
                        //TRAUMA CHECKING AREA    
                    } else if (icd10Result.getPDC() != null && icd10Result.getPDC().equals("25A")) {
                        drgResult.setMDC("25");
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(),
                            grouperparameter.getAdmissionDate()) <= 0 && utility.ComputeDay(grouperparameter.getBirthDate(),
                            grouperparameter.getAdmissionDate()) < 28) {
                        drgResult.setMDC("15");
                    } else {
                        drgResult.setMDC(icd10Result.getMDC());
                        drgResult.setPDC(icd10Result.getPDC());
                        
                    }
                }
            }

            //END OF PARSING PART
            ProcessMDC getMDC = new ProcessMDC();
            if (drgResult.getDRG() == null) {
                
                if (drgResult.getMDC().equals("30")) {
                    
                    if (drgResult.getPDC().isEmpty()) {
                        drgResult.setDRG("26519");
                        drgResult.setDC("2651");
                        drgResult.setDRGName("UNACCEPTABLE PRINCIPAL DIAGNOSIS");
                        result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                        result.setSuccess(true);
                        result.setMessage("Grouper Done in Pre-MDC level only");
                    } else {
                        DRGOutput drgResults = new DRGOutput();
                        drgResults.setCC(drgResult.getCC());
                        drgResults.setDC(drgResult.getDC());
                        drgResults.setDRG(drgResult.getDRG());
                        drgResults.setWarningerror(grouperparameter.getWarningerror());
                        drgResults.setDRGName(drgResult.getDRGName());
                        if (drgResult.getPDC().length() > 2) {
                            drgResults.setMDC(drgResult.getPDC().substring(0, 2));
                        } else {
                            drgResults.setMDC(drgResult.getPDC().substring(0, 1));
                        }
                        drgResults.setMDF(drgResult.getMDF());
                        drgResults.setOT(drgResult.getOT());
                        drgResults.setPDC(drgResult.getPDC());
                        drgResults.setRW(drgResult.getRW());
                        drgResults.setClaimseries(grouperparameter.getClaimseries());
//                        //-----------------------------------------------
                        result = getMDC.ProcessMDC(datasource, drgResults, grouperparameter);
                    }
                } else {
                    result = getMDC.ProcessMDC(datasource, drgResult, grouperparameter);
                }
                
            } else {
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
                result.setMessage("Grouper Done in Pre-MDC level only");
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing Pre-MDC Validation Method");
            logger.error("Error in Pre-MDC Validation Method : {}", ex.getMessage(), ex);
        }
        
        return result;
    }
}
