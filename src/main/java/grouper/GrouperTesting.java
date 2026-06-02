/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper;

import grouper.methods.premdc.ValidateFindMDC;
import grouper.methods.validation.AgeConfictValidation;
import grouper.methods.validation.GenderConfictValidation;
import grouper.methods.validation.GenderConfictValidationProc;
import grouper.methods.validation.GetICD10PreMDC;
import grouper.methods.validation.GET_ICD9;
import grouper.methods.validation.InsertGrouperAuditTrail;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
@Path("GrouperTest")
@RequestScoped
public class GrouperTesting {

    public GrouperTesting() {
    }

    @Resource(lookup = "jdbc/grouperuser")
    private DataSource datasource;

    private final Logger logger = (Logger) LogManager.getLogger(GrouperTesting.class);
    private final Utility utility = new Utility();

    @POST
    @Path("JasonData")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DRGWSResult ProcessGrouperParameter(
            final List<GrouperParameter> grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> drgresultList = new ArrayList<>();
        ArrayList<String> errorList = new ArrayList<>();
        try {

            if (utility.GetString("FilePathReports").isSuccess()) {
                for (int g = 0; g < grouperparameter.size(); g++) {
                    DRGWSResult grouperResult = this.ProcessData(utility.GetString("FilePathReports").getResult(), datasource, grouperparameter.get(g));
//                DRGWSResult grouperResult = this.ProcessData(datasource, grouperparameter.get(g));
                    if (grouperResult.isSuccess()) {
                        DRGOutput drgout = utility.objectMapper().readValue(grouperResult.getResult(), DRGOutput.class);
                        System.out.println("SERIES: " + drgout.getClaimseries() + " PDC: " + drgout.getPDC() + " MDC: " + drgout.getMDC() + " DRG: " + drgout.getDRG());
                        drgresultList.add(grouperResult.getResult());
                    } else {
                        errorList.add(grouperResult.getMessage());
                    }
                }
                if (grouperparameter.size() > 0) {
                    result.setMessage("Data Process : " + grouperparameter.size());
                    result.setSuccess(true);
//                    result.setResult(drgresultList.toString());
                    //FILE HANDLER
                    File file = new File(utility.GetString("FilePathReports").getResult());
                    result.setResult("Open file: //" + file.getAbsolutePath().replace("\\", "/"));
                } else {
                    result.setMessage("NO DATA AVAILABLE TO PROCESS");
                }
            } else {
                result.setMessage("File or directory not found");
            }

        } catch (Exception ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing JasonData API end point");
            logger.error("Error in JasonData API end point : {}", ex.getMessage(), ex);
        }
        return result;
    }

    private DRGWSResult ProcessData(
            final String Path,
            final DataSource datasource,
            final GrouperParameter grouperparameter) {

        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);

        // Use fast HashSets for O(1) lookups instead of looping arrays
        final Set<String> SEX_SET = new HashSet<>(Arrays.asList("M", "F"));
        final Set<String> DISPOSITION_SET = new HashSet<>(Arrays.asList("1", "2", "3", "4", "5", "8", "9"));

        ArrayList<String> warningerror = new ArrayList<>();
        GetICD10PreMDC getPreMDC = new GetICD10PreMDC();
        AgeConfictValidation ageValidation = new AgeConfictValidation();
        GenderConfictValidation sexValidation = new GenderConfictValidation();

        try {
            DRGOutput drgresult = utility.DRGOutput();
            GrouperParameter grouper = utility.GrouperParameter();
            // Deep string sanitization copying
            grouper.setResult_id(grouperparameter.getResult_id());
            grouper.setExpireTime(grouperparameter.getExpireTime());
            grouper.setTimeAdmission(grouperparameter.getTimeAdmission());
            grouper.setAdmissionDate(grouperparameter.getAdmissionDate().replace("/", "-"));
            grouper.setTimeDischarge(grouperparameter.getTimeDischarge());
            grouper.setDischargeDate(grouperparameter.getDischargeDate().replace("/", "-"));
            grouper.setExpiredDate(grouperparameter.getExpiredDate() == null ? "" : grouperparameter.getExpiredDate().replace("/", "-"));
            grouper.setBirthDate(grouperparameter.getBirthDate().replace("/", "-"));
            grouper.setClaimseries(grouperparameter.getClaimseries());
            grouper.setGender(grouperparameter.getGender());
            grouper.setIdseries(grouperparameter.getIdseries());
            grouper.setAdmissionWeight(grouperparameter.getAdmissionWeight());
            String rawDischargeType = grouperparameter.getDischargeType().toUpperCase();
            switch (rawDischargeType) {
                case "E": {
                    grouper.setDischargeType("8");
                    break;
                }
                case "O": {
                    grouper.setDischargeType("5");
                    break;
                }
                case "I":
                case "R": {
                    grouper.setDischargeType("1");
                    break;
                }
                case "A": {
                    grouper.setDischargeType("3");
                    break;
                }
                case "T": {
                    grouper.setDischargeType("4");
                    break;
                }
                case "H": {
                    grouper.setDischargeType("2");
                    break;
                }
                default: {
                    grouper.setDischargeType(grouperparameter.getDischargeType());
                    break;
                }
            }
            drgresult.setPrepccl("");
            drgresult.setFinalpccl("");
            drgresult.setWarningerror("");
            // PDx Cleanup Check
            String rawPdx = grouperparameter.getPdx();
            if (getPreMDC.GetICD10PreMDC(datasource, rawPdx).isSuccess()) {
                grouper.setPdx(rawPdx);
            } else {
                String cleanedPdx = utility.icd10Cleaner(rawPdx);
                if (getPreMDC.GetICD10PreMDC(datasource, cleanedPdx).isSuccess()) {
                    grouper.setPdx(cleanedPdx);
                } else {
                    grouper.setPdx(rawPdx);
                }
            }

            // PROCEDURES LEVEL VALIDATION (Optimized)
            GET_ICD9 icd9cm = new GET_ICD9();
            GenderConfictValidationProc sexValidateProc = new GenderConfictValidationProc();
            String rawProc = grouperparameter.getProc();

            if (rawProc != null && !rawProc.trim().isEmpty()) {
                List<String> newprocList = new ArrayList<>();
//                for (String proc : rawProc.split(",")) {
//                    String trimmed = proc.trim();
//                    if (!trimmed.isEmpty()) {
//                        newprocList.add(trimmed);
//                    }
//                }
                for (String proc : rawProc.split(",")) {
                    String trimmed = proc.trim();
                    if (!trimmed.isEmpty()) {
                        String cleanProc = trimmed.replaceFirst("^0+", "");
                        if (cleanProc.isEmpty()) {
                            cleanProc = "0";
                        }
                        newprocList.add(cleanProc);
                    }
                }
                for (int m = 0; m < newprocList.size(); m++) {
                    String currentProc = newprocList.get(m);
                    if (icd9cm.GetICD9cm(datasource, currentProc).isSuccess()) {
                        DRGWSResult sexvalidationresult = sexValidateProc.GenderConfictValidationProc(datasource, currentProc, grouperparameter.getGender());
                        if (!sexvalidationresult.isSuccess()) {
                            warningerror.add("Proc " + currentProc + " sex conflict");
                            newprocList.remove(m);
                            m--;
                        }
                    } else {
                        warningerror.add("Proc " + currentProc + " invalid");
                    }
                }
                grouper.setProc(String.join(",", newprocList));
            } else {
                grouper.setProc(rawProc != null ? rawProc.trim() : "");
            }

            // SECONDARY DIAGNOSIS (SDx) CLEANING LEVEL (Optimized)
            String rawSdx = grouperparameter.getSdx();
            if (rawSdx != null && !rawSdx.isEmpty()) {
                List<String> newsdxList = new ArrayList<>();
                String targetPdx = grouper.getPdx().toUpperCase().trim();

                for (String sdx : rawSdx.split(",")) {
                    String trimmedSdx = sdx.trim();
                    if (trimmedSdx.toUpperCase().equals(targetPdx)) {
                        warningerror.add("SDx " + sdx + " duplicate with the PDx");
                    } else if (getPreMDC.GetICD10PreMDC(datasource, trimmedSdx).isSuccess()) {
                        newsdxList.add(trimmedSdx);
                    } else {
                        String cleanedSdx = utility.icd10Cleaner(trimmedSdx);
                        if (getPreMDC.GetICD10PreMDC(datasource, cleanedSdx).isSuccess()) {
                            newsdxList.add(cleanedSdx);
                        } else {
                            warningerror.add("SDx " + sdx + " invalid");
                        }
                    }
                }

                ArrayList<String> finalSdx = new ArrayList<>();
                String bDate = grouper.getBirthDate();
                String aDate = grouper.getAdmissionDate();

                if (!bDate.isEmpty() && !aDate.isEmpty()) {
                    // RUN AGE CALCULATIONS ONCE OUTSIDE LOOP
                    int calcYear = utility.ComputeYear(bDate, aDate);
                    int calcDay = utility.ComputeDay(bDate, aDate);
                    int daysfinal = (calcYear > 0) ? (calcYear * 365) : calcDay;
                    String yearStr = String.valueOf(calcYear);
                    String daysStr = String.valueOf(daysfinal);

                    if (calcYear >= 0 && calcDay >= 0) {
                        for (String currentSdx : newsdxList) {
                            if (currentSdx.isEmpty()) {
                                continue;
                            }
                            String upperSdx = currentSdx.toUpperCase().trim();
                            DRGWSResult ageConflictResult = ageValidation.AgeConfictValidation(datasource, upperSdx, daysStr, yearStr);
                            DRGWSResult sexConflictResult = sexValidation.GenderConfictValidation(datasource, upperSdx, grouper.getGender());

                            boolean hasAgeConflict = !ageConflictResult.isSuccess();
                            boolean hasSexConflict = !sexConflictResult.isSuccess();

                            if (hasAgeConflict || hasSexConflict) {
                                if (hasAgeConflict) {
                                    warningerror.add("SDx " + currentSdx + " age conflict");
                                }
                                if (hasSexConflict) {
                                    warningerror.add("SDx " + currentSdx + " sex conflict");
                                }
                            } else {
                                finalSdx.add(currentSdx);
                            }
                        }
                    }
                }
                grouper.setSdx(String.join(",", finalSdx));
            } else {
                grouper.setSdx(rawSdx);
            }

            if (!warningerror.isEmpty()) {
                String combinedWarnings = String.join(",", warningerror);
                grouper.setWarningerror(combinedWarnings);
                drgresult.setWarningerror(combinedWarnings);
            }

            // UNGROUPABLE ENGINE RULE CHECKERS
            String checkedPdx = grouper.getPdx();
            int finalAgeYears = utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate());
            int finalAgeDays = utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate());

            if (checkedPdx.isEmpty()) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid PDx");
            } else if (!getPreMDC.GetICD10PreMDC(datasource, checkedPdx).isSuccess()) {
                // Note: Cleaner duplicate step dropped here since it was validated at the top initialization step!
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Unacceptable PDx");
            } else if (grouper.getBirthDate().isEmpty()) {
                drgresult.setDRG("26539");
                drgresult.setDC("2653");
                drgresult.setDRGName("Ungroupable, invalid age due to missing birthdate");
            } else if (finalAgeYears > 124) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Ungroupable, invalid age more than 124 years old");
            } else if (grouper.getGender().trim().isEmpty()) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid sex , Patient sex is required");
            } else if (!SEX_SET.contains(grouper.getGender().toUpperCase())) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Patient sex is not valid");
            } else if (grouper.getAdmissionDate().isEmpty() || !utility.IsValidDate(grouper.getAdmissionDate())) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid LOS admission date constraints");
            } else if (grouper.getDischargeDate().isEmpty() || !utility.IsValidDate(grouper.getDischargeDate())) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid LOS discharge date constraints");
            } else if (grouper.getTimeAdmission().isEmpty() || !utility.IsValidTime(grouper.getTimeAdmission())) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid LOS time admission conditions");
            } else if (grouper.getTimeDischarge().isEmpty() || !utility.IsValidTime(grouper.getTimeDischarge())) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Invalid LOS discharge time constraints");
            } else if (grouper.getDischargeType().isEmpty() || !DISPOSITION_SET.contains(grouper.getDischargeType())) {
                drgresult.setDRG("26509");
                drgresult.setDC("2650");
                drgresult.setDRGName("Disposition is invalid or missing");
            } else if (finalAgeYears == 0 && finalAgeDays < 28) {
                String weight = grouper.getAdmissionWeight();
                if (weight == null || weight.isEmpty()) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Newborn <28 days requires AdmWt");
                } else if (!utility.isValidNumeric(weight) || Double.parseDouble(weight) < 0.3) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Admission Weight constraints failed");
                }
            } else {
                // Check Length of Stay logic bounds safely
                int losCalculated = utility.ComputeLOS(grouper.getAdmissionDate(), utility.Convert24to12(grouper.getTimeAdmission()), grouper.getDischargeDate(), utility.Convert24to12(grouper.getTimeDischarge()));
                if (losCalculated == 0) {
                    int oras = utility.ComputeTime(grouper.getAdmissionDate(), utility.Convert24to12(grouper.getTimeAdmission()), grouper.getDischargeDate(), utility.Convert24to12(grouper.getTimeDischarge()));
                    if (finalAgeDays <= 0 && oras < 0) {
                        drgresult.setDRG("26509");
                        drgresult.setDC("2650");
                        drgresult.setDRGName("Invalid LOS");
                    }
                } else if (utility.ComputeYear(grouper.getAdmissionDate(), grouper.getDischargeDate()) <= 0 && finalAgeDays < 0) {
                    drgresult.setDRG("26509");
                    drgresult.setDC("2650");
                    drgresult.setDRGName("Invalid LOS");
                }
            }

            // WRITE FINAL RESULT PACKETS
            if (drgresult.getDRG() != null) {
                drgresult.setClaimseries(grouperparameter.getClaimseries());
                result.setResult(utility.objectMapper().writeValueAsString(drgresult));
                result.setSuccess(true);
//                this.FileWriter(Path, grouperparameter.getClaimseries(), drgresult.getDRG(), "N/A", drgresult.getDRGName(), "N/A", "N/A", "N/A");
            } else {
                DRGWSResult validateresult = new ValidateFindMDC().ValidateFindMDC(datasource, grouper);
                if (validateresult.isSuccess()) {
//                    DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
//                    this.FileWriter(Path, grouperparameter.getClaimseries(), drgResults.getDRG(), drgResults.getPDC(), drgResults.getDRGName(), drgResults.getPrepccl(), drgResults.getFinalpccl(), drgResults.getWarningerror());
                    result.setResult(validateresult.getResult());
                    result.setSuccess(true);
//                    System.out.println(validateresult.getResult());
                } else {
//                    this.FileWriter(Path, grouperparameter.getClaimseries(), "N/A", "N/A", validateresult.getMessage(), "N/A", "N/A", "N/A");
                    result.setMessage(validateresult.getMessage());
                    System.out.println(validateresult.getMessage());
                }
            }
        } catch (IOException | NumberFormatException | ParseException ex) {
            result.setMessage("Something went wrong");
            logger.error("Error in ProcessData Method : {}", ex.getMessage(), ex);
            System.out.println(ex.toString());
//            this.FileWriter(Path, grouperparameter.getClaimseries(), "N/A", "N/A", ex.toString(), "N/A", "N/A", "N/A");
        }
        return result;
    }

//    private DRGWSResult ProcessData(
//            final String Path,
//            final DataSource datasource,
//            final GrouperParameter grouperparameter) {
//        DRGWSResult result = utility.DRGWSResult();
//        String[] sex = {"M", "F"};
//        String[] disposition = {"1", "2", "3", "4", "5", "8", "9"};
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        ArrayList<String> warningerror = new ArrayList<>();
//        GetICD10PreMDC getPreMDC = new GetICD10PreMDC();
//        AgeConfictValidation ageValidation = new AgeConfictValidation();
//        GenderConfictValidation sexValidation = new GenderConfictValidation();
//        try {
//            DRGOutput drgresult = utility.DRGOutput();
//            GrouperParameter grouper = utility.GrouperParameter();
//            grouper.setResult_id(grouperparameter.getResult_id());
//            grouper.setExpireTime(grouperparameter.getExpireTime());
//            grouper.setTimeAdmission(grouperparameter.getTimeAdmission());
//            grouper.setAdmissionDate(grouperparameter.getAdmissionDate().replaceAll("\\/", "-"));
//            grouper.setTimeDischarge(grouperparameter.getTimeDischarge());
//            grouper.setDischargeDate(grouperparameter.getDischargeDate().replaceAll("\\/", "-"));
//            grouper.setExpiredDate(grouperparameter.getExpiredDate() == null ? "" : grouperparameter.getExpiredDate().replaceAll("\\/", "-"));
//            grouper.setBirthDate(grouperparameter.getBirthDate().replaceAll("\\/", "-"));
//            grouper.setClaimseries(grouperparameter.getClaimseries());
//            grouper.setGender(grouperparameter.getGender());
//            grouper.setIdseries(grouperparameter.getIdseries());
//            switch (grouperparameter.getDischargeType().toUpperCase()) {
//                case "E": {
//                    grouper.setDischargeType("8");
//                    break;
//                }
//                case "O": {
//                    grouper.setDischargeType("5");
//                    break;
//                }
//                case "I":
//                case "R": {
//                    grouper.setDischargeType("1");
//                    break;
//                }
//                case "A": {
//                    grouper.setDischargeType("3");
//                    break;
//                }
//                case "T": {
//                    grouper.setDischargeType("4");
//                    break;
//                }
//                case "H": {
//                    grouper.setDischargeType("2");
//                    break;
//                }
//                default: {
//                    grouper.setDischargeType(grouperparameter.getDischargeType());
//                    break;
//                }
//            }
//
//            drgresult.setPrepccl("");
//            drgresult.setFinalpccl("");
//            drgresult.setWarningerror("");
//
//            if (getPreMDC.GetICD10PreMDC(datasource, grouperparameter.getPdx()).isSuccess()) {
//                grouper.setPdx(grouperparameter.getPdx());
//            } else if (getPreMDC.GetICD10PreMDC(datasource, utility.icd10Cleaner(grouperparameter.getPdx())).isSuccess()) {
//                grouper.setPdx(utility.icd10Cleaner(grouperparameter.getPdx()));
//            } else {
//                grouper.setPdx(grouperparameter.getPdx());
//            }
//            grouper.setAdmissionWeight(grouperparameter.getAdmissionWeight());
//
//            //CONVERT RVS CODE TO ICD9CM AND
////            GET_CONVERTER converter = new GET_CONVERTER();
//            GET_ICD9 icd9cm = new GET_ICD9();
//            GenderConfictValidationProc sexValidateProc = new GenderConfictValidationProc();
//            String rawProc = grouperparameter.getProc();
//            if (rawProc != null && !rawProc.trim().isEmpty()) {
//                List<String> newprocList = new ArrayList<>();
//                for (String proc : rawProc.split(",")) {
//                    String trimmed = proc.trim();
//                    if (!trimmed.isEmpty()) {
//                        newprocList.add(trimmed);
//                    }
//                }
//                for (int m = 0; m < newprocList.size(); m++) {
//                    String currentProc = newprocList.get(m);
//                    if (icd9cm.GetICD9cm(datasource, currentProc).isSuccess()) {
//                        DRGWSResult sexvalidationresult = sexValidateProc.GenderConfictValidationProc(
//                                datasource,
//                                currentProc,
//                                grouperparameter.getGender()
//                        );
//                        if (!sexvalidationresult.isSuccess()) {
//                            warningerror.add("Proc " + currentProc + " sex conflict");
//                            newprocList.remove(m);
//                            m--;
//                        }
//                    } else {
//                        warningerror.add("Proc " + currentProc + " invalid");
//                    }
//                }
//                grouper.setProc(String.join(",", newprocList));
//            } else {
//                grouper.setProc(rawProc != null ? rawProc.trim() : "");
//            }
////            if (!grouperparameter.getProc().trim().isEmpty()) {
////                LinkedList<String> newprocList = new LinkedList<>();
////                List<String> rvsList = Arrays.asList(grouperparameter.getProc().split(","));
////                for (int m = 0; m < rvsList.size(); m++) {
////                    DRGWSResult processRVS = converter.ValidateRVS(datasource, rvsList.get(m).trim());
////                    if (processRVS.isSuccess()) {
////                        List<String> procList = Arrays.asList(processRVS.getResult().split(","));
////                        for (int x = 0; x < procList.size(); x++) {
////                            newprocList.add(procList.get(x));
////                        }
////                        for (int pro = 0; pro < procList.size(); pro++) {
////                            if (icd9cm.GetICD9cm(datasource, procList.get(pro)).isSuccess()) {
////                                DRGWSResult sexvalidationresult = sexValidateProc.GenderConfictValidationProc(datasource, procList.get(pro).trim(), grouperparameter.getGender());
////                                if (!sexvalidationresult.isSuccess()) {
////                                    newprocList.remove(procList.get(pro).trim());
////                                }
////                            } else {
////                                warningerror.add("Proc " + procList.get(pro) + " invalid");
////                            }
////                        }
////                    } else {
////                        warningerror.add(processRVS.getMessage());
////                    }
////                }
////                grouper.setProc(String.join(",", newprocList));
////            } else {
////                grouper.setProc(grouperparameter.getProc().trim());
////            }
////            CLEANING SDX
//            if (!grouperparameter.getSdx().isEmpty()) {
//                LinkedList<String> newsdxList = new LinkedList<>();
//                List<String> sdxList = Arrays.asList(grouperparameter.getSdx().split(","));
//                for (int m = 0; m < sdxList.size(); m++) {
//                    if (sdxList.get(m).toUpperCase().trim().equals(grouper.getPdx().toUpperCase().trim())) {
//                        warningerror.add("SDx " + sdxList.get(m) + " duplicate with the PDx");
//                    } else {
//                        if (getPreMDC.GetICD10PreMDC(datasource, sdxList.get(m)).isSuccess()) {
//                            newsdxList.add(sdxList.get(m));
//                        } else if (getPreMDC.GetICD10PreMDC(datasource, utility.icd10Cleaner(sdxList.get(m))).isSuccess()) {
//                            newsdxList.add(utility.icd10Cleaner(sdxList.get(m)));
//                        } else {
//                            warningerror.add("SDx " + sdxList.get(m) + " invalid");
//                        }
//                    }
//                }
//                ArrayList<String> finalSdx = new ArrayList<>();
//
//                for (int u = 0; u < newsdxList.size(); u++) {
//                    if (!grouper.getBirthDate().isEmpty()
//                            && !grouper.getAdmissionDate().isEmpty()) {
//                        int daysfinal = 0;
//                        String year = String.valueOf(utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()));
//                        if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) > 0) {
//                            daysfinal = utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) * 365;
//                        } else {
//                            daysfinal = utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate());
//                        }
//                        if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0
//                                && utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0) {
//                            if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0
//                                    && utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0) {
//                                if (!grouper.getBirthDate().isEmpty() && !grouper.getAdmissionDate().isEmpty()) {
//                                    if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) >= 0
//                                            && utility.ComputeDay(grouper.getBirthDate(),
//                                                    grouper.getAdmissionDate()) >= 0 && !newsdxList.get(u).isEmpty()) {
//
//                                        // 1. Safely extract the current SDx code once and clean it up
//                                        String currentSdx = newsdxList.get(u).toUpperCase().trim();
//                                        String daysStr = String.valueOf(daysfinal);
//
//                                        // 2. Run validations EXACTLY ONCE
//                                        DRGWSResult ageConflictResult = ageValidation.AgeConfictValidation(datasource, currentSdx, daysStr, year);
//                                        DRGWSResult sexConflictResult = sexValidation.GenderConfictValidation(datasource, currentSdx, grouper.getGender());
//
//                                        boolean hasAgeConflict = !ageConflictResult.isSuccess();
//                                        boolean hasSexConflict = !sexConflictResult.isSuccess();
//
//                                        if (hasAgeConflict || hasSexConflict) {
//                                            // 3. Log conflicts accurately using the extracted string instance
//                                            if (hasAgeConflict) {
//                                                warningerror.add("SDx " + currentSdx + " age conflict");
//                                            }
//                                            if (hasSexConflict) {
//                                                warningerror.add("SDx " + currentSdx + " sex conflict");
//                                            }
//
//                                            // 4. Cleanly remove the item by index and decrement 'u' 
//                                            // This counteracts index-shifting so the loop doesn't skip the next item!
//                                            newsdxList.remove(u);
//                                            u--;
//                                        } else {
//                                            // 5. If both validations passed completely, append to your successful list
//                                            finalSdx.add(currentSdx);
//                                        }
//
////                                    
//                                    }
//                                }
//                            }
//
//                        }
//                    }
//                }
//                grouper.setSdx(String.join(",", finalSdx));
//            } else {
//                grouper.setSdx(grouperparameter.getSdx());
//            }
//            if (!warningerror.isEmpty()) {
//                grouper.setWarningerror(String.join(",", warningerror));
//                drgresult.setWarningerror(String.join(",", warningerror));
//            }
//
//            if (grouper.getPdx().isEmpty()) {
//                drgresult.setDRG("26509");
//                drgresult.setDC("2650");
//                drgresult.setDRGName("Invalid PDx");
//            } else if (!getPreMDC.GetICD10PreMDC(datasource, grouper.getPdx()).isSuccess()
//                    && !getPreMDC.GetICD10PreMDC(datasource, utility.icd10Cleaner(grouper.getPdx())).isSuccess()) {
//                drgresult.setDRG("26509");
//                drgresult.setDC("2650");
//                drgresult.setDRGName("Unacceptable PDx");
//            } else if (grouper.getBirthDate().equals("") || grouper.getBirthDate().isEmpty()) {
//                drgresult.setDRG("26539");
//                drgresult.setDC("2653");
//                drgresult.setDRGName("Ungroupable, invalid age due to missing birthdate");
//            } else if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) > 124) {
//                drgresult.setDRG("26509");
//                drgresult.setDC("2650");
//                drgresult.setDRGName("Ungroupable, invalid age more than 124 years old");
//            } else {
//                if (grouper.getGender().trim().isEmpty()) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid sex , Patient sex is required");
//                } else if (!Arrays.asList(sex).contains(grouper.getGender().toUpperCase())) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Patient sex is not valid");
//                } else if (grouper.getAdmissionDate().isEmpty()) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid LOS no admission date found");
//                } else if (!utility.IsValidDate(grouper.getAdmissionDate())) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid LOS admissiondate is not valid");
//                } else if (grouper.getDischargeDate().isEmpty()) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid LOS no discharge date found");
//                } else if (!utility.IsValidDate(grouper.getDischargeDate())) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid LOS discharge date is not valid");
//                } else if (grouper.getBirthDate().isEmpty()) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid Age Date of Birth is missing");
//                } else if (!utility.IsValidDate(grouper.getBirthDate())) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid Age date of birth is not valid");
//                } else if (grouper.getTimeAdmission().isEmpty()) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid LOS time admission not found");
//                } else if (!utility.IsValidTime(grouper.getTimeAdmission())) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid LOS time admission is not valid");
//                } else if (grouper.getTimeDischarge().isEmpty()) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid LOS discharge time not found");
//                } else if (!utility.IsValidTime(grouper.getTimeDischarge())) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Invalid LOS discharge time is not valid");
//                } else if (grouper.getDischargeType().isEmpty()) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Disposition is empty");
//                } else if (!Arrays.asList(disposition).contains(grouper.getDischargeType())) {
//                    drgresult.setDRG("26509");
//                    drgresult.setDC("2650");
//                    drgresult.setDRGName("Disposition is not valid");
//                } else if (utility.ComputeYear(grouper.getBirthDate(), grouper.getAdmissionDate()) == 0
//                        && utility.ComputeDay(grouper.getBirthDate(), grouper.getAdmissionDate()) < 28) {
//                    if (!grouper.getAdmissionWeight().isEmpty()) {
//                        if (!utility.isValidNumeric(grouper.getAdmissionWeight())) {
//                            drgresult.setDRG("26509");
//                            drgresult.setDC("2650");
//                            drgresult.setDRGName("Admission Weight is not valid");
//                        } else if (Double.parseDouble(grouper.getAdmissionWeight()) < 0.3) {
//                            drgresult.setDRG("26509");
//                            drgresult.setDC("2650");
//                            drgresult.setDRGName("Admission Weight less than 0.3 kg is not valid");
//                        }
//                    } else {
//                        drgresult.setDRG("26509");
//                        drgresult.setDC("2650");
//                        drgresult.setDRGName("Newborn <28 days requires AdmWt");
//                    }
//                } else {
//                    int oras = utility.ComputeTime(
//                            grouper.getAdmissionDate(),
//                            utility.Convert24to12(grouper.getTimeAdmission()),
//                            grouper.getDischargeDate(),
//                            utility.Convert24to12(grouper.getTimeDischarge()));
//                    int araw = utility.ComputeDay(grouper.getAdmissionDate(),
//                            grouper.getDischargeDate());
//                    int taon = utility.ComputeYear(grouper.getAdmissionDate(),
//                            grouper.getDischargeDate());
//                    if (utility.ComputeLOS(grouper.getAdmissionDate(),
//                            utility.Convert24to12(grouper.getTimeAdmission()),
//                            grouper.getDischargeDate(),
//                            utility.Convert24to12(grouper.getTimeDischarge())) == 0) {
//                        if (araw <= 0 && oras < 0) {
//                            drgresult.setDRG("26509");
//                            drgresult.setDC("2650");
//                            drgresult.setDRGName("Invalid LOS");
//                        }
//                    } else if (taon <= 0 && araw < 0) {
//                        drgresult.setDRG("26509");
//                        drgresult.setDC("2650");
//                        drgresult.setDRGName("Invalid LOS");
//                    }
//                }
//            }
//            if (drgresult.getDRG() != null) {
//                //result.setMessage(updatedrgresult.getMessage());
//                drgresult.setClaimseries(grouperparameter.getClaimseries());
//                result.setResult(utility.objectMapper().writeValueAsString(drgresult));
//                result.setSuccess(true);
//                // File writer
//                this.FileWriter(Path, grouperparameter.getClaimseries(), drgresult.getDRG(), "N/A", drgresult.getDRGName(), "N/A", "N/A", "N/A");
//            } else {
//                DRGWSResult validateresult = new ValidateFindMDC().ValidateFindMDC(datasource, grouper);
////                result = new ValidateFindMDC().ValidateFindMDC(datasource, grouper);
//                if (validateresult.isSuccess()) {
//                    DRGOutput drgResults = utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class);
//////                    result.setResult(utility.objectMapper().writeValueAsString(utility.objectMapper().readValue(validateresult.getResult(), DRGOutput.class)));
////                    result.setResult(validateresult.getResult());
//                    //File writer
//                    this.FileWriter(Path, grouperparameter.getClaimseries(), drgResults.getDRG(), drgResults.getPDC(), drgResults.getDRGName(), drgResults.getPrepccl(), drgResults.getFinalpccl(), drgResults.getWarningerror());
//                } else {
//////                    DRGAuditTrail(datasource, grouper.getClaimseries(), grouper.getIdseries(), validateresult.getMessage(), "FAILED");
//////                    result.setResult(utility.objectMapper().writeValueAsString(validateresult.getResult()));
////                    result.setResult(validateresult.getResult());
////                    result.setMessage(validateresult.getMessage());
////                    //File writer
//                    this.FileWriter(Path, grouperparameter.getClaimseries(), "N/A", "N/A", validateresult.getMessage(), "N/A", "N/A", "N/A");
//                }
//            }
//        } catch (ParseException | IOException ex) {
//            result.setMessage("Something went wrong");
//            logger.info("Executing ProcessData Method");
//            logger.error("Error in ProcessData Method : {}", ex.getMessage(), ex);
//            // File writer
//            this.FileWriter(Path, grouperparameter.getClaimseries(), "N/A", "N/A", ex.toString(), "N/A", "N/A", "N/A");
////        } catch (SQLException ex) {
////            java.util.logging.Logger.getLogger(GrouperTesting.class.getName()).log(Level.SEVERE, null, ex);
////        }
//
//        }
//        return result;
//    }
    public void FileWriter(String path, String series, String drgcode, String pdc, String drgname, String prepccl, String finalpccl, String warningerror) {
        try {
            FileReader fr = new FileReader(path);
            ArrayList<String> oldContent;
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                oldContent = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    oldContent.add(line);
                }
            }
            try (PrintWriter pw = new PrintWriter(path)) {
                for (int a = 0; a < oldContent.size(); a++) {
                    pw.write(oldContent.get(a) + "\n");
                }
                pw.write("SERIES: " + series + ", DRGCODE:" + drgcode + ", PDC:" + pdc + ", NAME:" + drgname + ", PREPCCL:" + prepccl + ", FINALPCCL:" + finalpccl + ", ERROR:" + warningerror + "\n");
                pw.flush();

            }
        } catch (IOException ex) {
            logger.info("Executing File writer Method");
            logger.error("Error in File writer Method : {}", ex.getMessage(), ex);
        }
    }

    public String DRGAuditTrail(final DataSource datasource, String claimsSeries, String idSeries, String deTails, String status) {
        DRGWSResult grouperauditrail = new InsertGrouperAuditTrail().InsertGrouperAuditTrail(datasource, claimsSeries, idSeries, deTails, status);
        return grouperauditrail.getMessage();
    }

}
