/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.methods.validation.GenderConfictValidation;
import grouper.methods.validation.GetDA;
import grouper.methods.validation.GetICD10PreMDC;
import grouper.methods.validation.GetPCOM;
//import grouper.structures.CombinationCode;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class ValidateFindMDC {

    private final Logger logger = LogManager.getLogger(ValidateFindMDC.class);
    private final Utility utility = new Utility();

    public ValidateFindMDC() {
    }

    public DRGWSResult validateFindMDC(
            final DataSource datasource,
            final String schemaName,
            final GrouperParameter grouperParameter) {

        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGOutput drgResult = new DRGOutput();

        try {
            // 1. Shallow clone the parameter context
            GrouperParameter newGrouperParam = cloneGrouperParameter(grouperParameter);
            drgResult.setClaimseries(grouperParameter.getClaimseries());

            List<String> procList = Arrays.stream(grouperParameter.getProc().split(","))
                    .collect(Collectors.toList());
//            List<String> procList = Arrays.asList(grouperParameter.getProc().split(","));

            List<String> combiCode = new ArrayList<>();
            Set<Integer> negativeIndexSet = new LinkedHashSet<>();

            // 2. Optimized Cross-Join Combination Check
            GetPCOM pcomService = new GetPCOM();
            for (int y = 0; y < procList.size(); y++) {
                String dataA = procList.get(y).replace(">1", "").trim();
                for (int w = 0; w < procList.size(); w++) {
                    String dataB = procList.get(w).replace(">1", "").trim();

                    DRGWSResult pcomResult = pcomService.GetPCOM(datasource, schemaName, dataA, dataB);
                    if (pcomResult.isSuccess()) {
                        combiCode.add(pcomResult.getResult());

                        // Target exactly match entries efficiently
                        for (int i = 0; i < procList.size(); i++) {
                            String baseProc = procList.get(i).replace(">1", "").trim();
                            if (baseProc.equals(dataA) || baseProc.equals(dataB)) {
                                negativeIndexSet.add(i);
                            }
                        }
                    }
                }
            }
//            List<String> finalListComcode = negativeIndexSet.stream()
//                    .map(procList::get)
//                    .collect(Collectors.toList());
            // Deduplicate combination list in O(N) using Sets
//            Set<String> cleanCombiCodeSet = new LinkedHashSet<>(combiCode);
            // 3. Process Asterisk / Diagnostic Alignment (DA) Mapping
            List<String> sDxList = Arrays.stream(grouperParameter.getSdx().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            List<Boolean> asteriskMask = new ArrayList<>();
            GetDA daService = new GetDA();
//            sDxList.stream().map((sdx) -> daService.GetDA(datasource, schemaName, grouperParameter.getPdx(), sdx)).forEachOrdered((gDAResult) -> {
//                asteriskMask.add(gDAResult.isSuccess());
//            });
            for (String sdx : sDxList) {
                DRGWSResult gDAResult = daService.GetDA(datasource, schemaName, grouperParameter.getPdx(), sdx);
                asteriskMask.add(gDAResult.isSuccess());
            }
            SDxPDx swapping = new SDxPDx();
            int indexNumber = asteriskMask.indexOf(true);
            if (indexNumber != -1) {
                sDxList.set(indexNumber, grouperParameter.getPdx());
                String newListSDx = sDxList.stream().collect(Collectors.joining(","));
                swapping.setNewsdx(newListSDx);
                List<String> newPrimary = Arrays.asList(grouperParameter.getSdx().split(","));
                swapping.setNewpdx(newPrimary.get(indexNumber));
            } else {
                swapping.setNewpdx(grouperParameter.getPdx());
                if (!grouperParameter.getSdx().isEmpty()) {
                    swapping.setNewsdx(grouperParameter.getSdx());
                }
            }
            // 4. Run Standard Clinical Validations
            DRGWSResult getIcd10Result = new GetICD10PreMDC().GetICD10PreMDC(datasource, schemaName, swapping.getNewpdx());
            DRGWSResult getSexConflictResult = new GenderConfictValidation().GenderConfictValidation(datasource, schemaName, swapping.getNewpdx(), grouperParameter.getGender());
            int calculatedAge = utility.ComputeYear(grouperParameter.getBirthDate(), grouperParameter.getAdmissionDate());
//PROCESS PROC LIST
            String newProcList = Stream.concat(
                    combiCode.stream(),
                    Arrays.stream(grouperParameter.getProc().split(","))
            )
                    .filter(s -> s != null && !s.trim().isEmpty()) // Optional: removes empty strings
                    .distinct() // Keeps only unique values
                    .collect(Collectors.joining(","));

            GetValidatedPreMDC getPreMDC = new GetValidatedPreMDC();
            if (!getIcd10Result.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("Invalid PDx : " + grouperParameter.getPdx());
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (grouperParameter.getBirthDate() == null || grouperParameter.getBirthDate().isEmpty()) {
                drgResult.setDRG("26539");
                drgResult.setDC("2653");
                drgResult.setDRGName("Ungroupable, invalid age due to missing birthdate");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (calculatedAge > 124) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("Ungroupable, invalid age more than 124 years old");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (!getSexConflictResult.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName(grouperParameter.getPdx() + " PDx having conflict with sex");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (indexNumber == -1) {
                // If there's no diagnostic swap configuration found
                ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(getIcd10Result.getResult(), ICD10PreMDCResult.class);
                if (!"Y".equalsIgnoreCase(icd10Result.getAccPDX())) {
                    drgResult.setDRG("26519");
                    drgResult.setDC("2651");
                    drgResult.setDRGName("Inappropriate PDx " + grouperParameter.getPdx());
                    result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                    result.setSuccess(true);
                } else {
                    newGrouperParam.setPdx(grouperParameter.getPdx());
//                    newGrouperParam.setProc(resolveProcedureList(negativeIndexSet, procList, cleanCombiCodeSet, finalListComcode));
                    newGrouperParam.setProc(newProcList);
                    newGrouperParam.setSdx(grouperParameter.getSdx());

                    result = getPreMDC.getValidatedPreMDC(datasource, schemaName, newGrouperParam);
                }
            } else {
                // Execute standard pre-MDC mapping sequence with swapped codes  newProcList
                newGrouperParam.setPdx(swapping.getNewpdx());
//                newGrouperParam.setProc(resolveProcedureList(negativeIndexSet, procList, cleanCombiCodeSet, finalListComcode));
                newGrouperParam.setProc(newProcList);
                newGrouperParam.setSdx(swapping.getNewsdx());
                result = getPreMDC.getValidatedPreMDC(datasource, schemaName, newGrouperParam);
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.error("Error in Find MDC Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    // Unifies duplicate procedure string manipulation block
//    private String resolveProcedureList(
//            Set<Integer> negativeIndexSet,
//            List<String> procList,
//            Set<String> cleanCombiCode,
//            List<String> finalListComcode) {
//        if (negativeIndexSet.isEmpty()) {
//            return String.join(",", procList);
//        }
//        CombinationCode combinationCode = new CombinationCode();
//        combinationCode.setComcode(String.join(",", cleanCombiCode));
//        combinationCode.setIndexlist(String.join(",", finalListComcode));
//        combinationCode.setProclist(String.join(",", procList));
//        return utility.ProcedureExecute(combinationCode);
//    }

    private GrouperParameter cloneGrouperParameter(GrouperParameter src) {
        GrouperParameter target = new GrouperParameter();
        target.setAdmissionDate(src.getAdmissionDate());
        target.setAdmissionWeight(src.getAdmissionWeight());
        target.setBirthDate(src.getBirthDate());
        target.setDischargeDate(src.getDischargeDate());
        target.setDischargeType(src.getDischargeType());
        target.setExpireTime(src.getExpireTime());
        target.setExpiredDate(src.getExpiredDate());
        target.setTimeAdmission(src.getTimeAdmission());
        target.setTimeDischarge(src.getTimeDischarge());
        target.setTimeOfBirth(src.getTimeOfBirth());
        target.setWarningerror(src.getWarningerror());
        target.setGender(src.getGender());
        target.setClaimseries(src.getClaimseries());
        return target;
    }

    public static class SDxPDx {

        private String newpdx;
        private String newsdx;

        public String getNewpdx() {
            return newpdx;
        }

        public void setNewpdx(String newpdx) {
            this.newpdx = newpdx;
        }

        public String getNewsdx() {
            return newsdx;
        }

        public void setNewsdx(String newsdx) {
            this.newsdx = newsdx;
        }
    }
}
//public class ValidateFindMDC {
//
//    public ValidateFindMDC() {
//    }
//
//    private final Logger logger = (Logger) LogManager.getLogger(ValidateFindMDC.class);
//    private final Utility utility = new Utility();
//
//    public DRGWSResult ValidateFindMDC(
//            final DataSource datasource,
//            final String SchemaName,
//            final GrouperParameter grouperparameter) throws ParseException {
//        String checker = "true";
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        DRGOutput drgResult = new DRGOutput();
//        try {
//            //REMAP AREA
//            GrouperParameter Newgrouperparam = new GrouperParameter();
//            Newgrouperparam.setAdmissionDate(grouperparameter.getAdmissionDate());
//            Newgrouperparam.setAdmissionWeight(grouperparameter.getAdmissionWeight());
//            Newgrouperparam.setBirthDate(grouperparameter.getBirthDate());
//            Newgrouperparam.setDischargeDate(grouperparameter.getDischargeDate());
//            Newgrouperparam.setDischargeType(grouperparameter.getDischargeType());
//            Newgrouperparam.setExpireTime(grouperparameter.getExpireTime());
//            Newgrouperparam.setExpiredDate(grouperparameter.getExpiredDate());
//            Newgrouperparam.setTimeAdmission(grouperparameter.getTimeAdmission());
//            Newgrouperparam.setTimeDischarge(grouperparameter.getTimeDischarge());
//            Newgrouperparam.setTimeOfBirth(grouperparameter.getTimeOfBirth());
//            Newgrouperparam.setWarningerror(grouperparameter.getWarningerror());
//            Newgrouperparam.setGender(grouperparameter.getGender());
//            Newgrouperparam.setClaimseries(grouperparameter.getClaimseries());
//            drgResult.setClaimseries(grouperparameter.getClaimseries());
//
//            List<String> ProcList = Arrays.asList(grouperparameter.getProc().split(","));
//            ArrayList<String> asterisk = new ArrayList<>();
//            ArrayList<String> combiCode = new ArrayList<>();
//            ArrayList<Integer> NegativeIndex = new ArrayList<>();
//            for (int y = 0; y < ProcList.size(); y++) {  //TEST DATA HERE  Procwithgreathervalue
//                String dataA = ProcList.get(y).replace(">1", "");  //TEST DATA HERE  Procwithgreathervalue
//                for (int w = 0; w < ProcList.size(); w++) {  //TEST DATA HERE  Procwithgreathervalue
//                    String dataB = ProcList.get(w).replace(">1", "");  //TEST DATA HERE  Procwithgreathervalue
//                    DRGWSResult pcomResult = new GetPCOM().GetPCOM(datasource, SchemaName, dataA.trim(), dataB.trim());
//                    if (String.valueOf(pcomResult.isSuccess()).equals("true")) {
//                        combiCode.add(pcomResult.getResult());
//                        for (int i = 0; i < ProcList.size(); i++) {  //TEST DATA HERE  Procwithgreathervalue
//                            if (!ProcList.get(i).replace(">1", "").equals(dataA) && !ProcList.get(i).replace(">1", "").equals(dataB)) {  //TEST DATA HERE  Procwithgreathervalue
//                            } else {
//                                NegativeIndex.add(i);
//                            }
//                        }
//                    }
//                }
//            }
//
//            LinkedList<String> FinalListComcode = new LinkedList<>();
//            for (int m = 0; m < NegativeIndex.size(); m++) {
//                FinalListComcode.add(ProcList.get(NegativeIndex.get(m))); //TEST DATA HERE  Procwithgreathervalue
//            }
//            LinkedList<String> CleanCombiCode = new LinkedList<>();
//            for (int i = 0; i < combiCode.size() - 1; i++) {
//                for (int j = i + 1; j < combiCode.size(); j++) {
//                    if (combiCode.get(i).equals(combiCode.get(j)) && (i != j)) {
//                        CleanCombiCode.add(combiCode.get(j));
//                    }
//                }
//            }
//            List<String> SDxList = Arrays.asList(grouperparameter.getSdx().split(","));
//            for (int b = 0; b < SDxList.size(); b++) {
//                DRGWSResult gDAResult = new GetDA().GetDA(datasource, SchemaName, grouperparameter.getPdx(), SDxList.get(b).trim());
//                if (gDAResult.isSuccess()) {
//                    asterisk.add("true");
//                } else {
//                    asterisk.add("false");
//                }
//            }
//            //GETTING THE INDEX AREA 
//            //String FinalPrimary = "";
//            String FinalSDxList = "";
//            SDxPDx swapping = new SDxPDx();
//            int indexNumber = asterisk.indexOf("true");
//            if (asterisk.contains(checker)) {
//                SDxList.set(indexNumber, grouperparameter.getPdx());
//                StringBuilder newListSDx = new StringBuilder("");
//                SDxList.forEach((eachstring) -> {
//                    newListSDx.append(eachstring).append(",");
//                });
//                //Swapping area
//                FinalSDxList = FinalSDxList.substring(0, FinalSDxList.length());
//                swapping.setNewsdx(newListSDx.toString());
//                List<String> NewPrimary = Arrays.asList(grouperparameter.getSdx().split(","));
//                swapping.setNewpdx(NewPrimary.get(indexNumber));
//            } else {
//                if (!grouperparameter.getSdx().isEmpty()) {
//                    swapping.setNewpdx(grouperparameter.getPdx());
//                    swapping.setNewsdx(grouperparameter.getSdx());
//                } else {
//                    swapping.setNewpdx(grouperparameter.getPdx());
//                }
//            }
//            GetValidatedPreMDC validatePreMDC = new GetValidatedPreMDC();
//            DRGWSResult geticd10Result = new GetICD10PreMDC().GetICD10PreMDC(datasource, SchemaName, swapping.getNewpdx());
//            DRGWSResult getSexConfictResult = new GenderConfictValidation().GenderConfictValidation(datasource, SchemaName, swapping.getNewpdx(), grouperparameter.getGender());
//            if (!geticd10Result.isSuccess()) {
//                drgResult.setDRG("26509");
//                drgResult.setDC("2650");
//                drgResult.setDRGName("Invalid PDx : " + grouperparameter.getPdx());
//                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
//                result.setSuccess(true);
//            } else if (Newgrouperparam.getBirthDate().equals("") || Newgrouperparam.getBirthDate().isEmpty()) {
//                drgResult.setDRG("26539");
//                drgResult.setDC("2653");
//                drgResult.setDRGName("Ungroupable, invalid age due to missing birthdate");
//                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
//                result.setSuccess(true);
//            } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 124) {
//                drgResult.setDRG("26509");
//                drgResult.setDC("2650");
//                drgResult.setDRGName("Ungroupable, invalid age more than 124 years old");
//                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
//                result.setSuccess(true);
//            } else if (!getSexConfictResult.isSuccess()) {
//                drgResult.setDRG("26509");
//                drgResult.setDC("2650");
//                drgResult.setDRGName(grouperparameter.getPdx() + " PDx having conflict with sex");
//                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
//                result.setSuccess(true);
//            } else if (!asterisk.contains(checker)) {
//                ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(geticd10Result.getResult(), ICD10PreMDCResult.class);
//                if (!icd10Result.getAccPDX().equals("Y")) {
//                    drgResult.setDRG("26519");
//                    drgResult.setDC("2651");
//                    drgResult.setDRGName("Inappropriate PDx " + grouperparameter.getPdx());
//                    result.setResult(utility.objectMapper().writeValueAsString(drgResult));
//                    result.setSuccess(true);
//                } else {
//                    Newgrouperparam.setPdx(grouperparameter.getPdx());
//                    if (NegativeIndex.isEmpty()) {
//                        Newgrouperparam.setProc(String.join(",", ProcList));
//                    } else {
//                        String Combinecode = String.join(",", CleanCombiCode);
//                        String indextoremove = String.join(",", FinalListComcode);
//                        String OrigList = String.join(",", ProcList);             //TEST DATA HERE  Procwithgreathervalue
//                        CombinationCode combinationcode = new CombinationCode();
//                        combinationcode.setComcode(Combinecode);
//                        combinationcode.setIndexlist(indextoremove);
//                        combinationcode.setProclist(OrigList);
//                        String comResult = utility.ProcedureExecute(combinationcode);
//                        Newgrouperparam.setProc(comResult);
//                    }
//                    Newgrouperparam.setSdx(grouperparameter.getSdx());
//                    result = validatePreMDC.getValidatedPreMDC(datasource, SchemaName, Newgrouperparam);
//                }
//            } else {
//                Newgrouperparam.setPdx(swapping.getNewpdx());
//                if (NegativeIndex.isEmpty()) {
//                    Newgrouperparam.setProc(String.join(",", ProcList));
//                } else {
//                    String Combinecode = String.join(",", CleanCombiCode);
//                    String indextoremove = String.join(",", FinalListComcode);
//                    String OrigList = String.join(",", ProcList);             //TEST DATA HERE  Procwithgreathervalue
//                    CombinationCode combinationcode = new CombinationCode();
//                    combinationcode.setComcode(Combinecode);
//                    combinationcode.setIndexlist(indextoremove);
//                    combinationcode.setProclist(OrigList);
//                    String comResult = utility.ProcedureExecute(combinationcode);
//                    Newgrouperparam.setProc(comResult);
//                }
//                Newgrouperparam.setSdx(swapping.getNewsdx());
//                result = validatePreMDC.getValidatedPreMDC(datasource, SchemaName, Newgrouperparam);
//            }
//        } catch (IOException ex) {
//            result.setMessage("Something went wrong");
//            logger.info("Executing Find MDC Method");
//            logger.error("Error in Find MDC Method : {}", ex.getMessage(), ex);
//        }
//        return result;
//    }
//
//    public class SDxPDx {
//
//        private String newpdx; // private = restricted access
//        private String newsdx;
//
//        public String getNewpdx() {
//            return newpdx;
//        }
//
//        public void setNewpdx(String newpdx) {
//            this.newpdx = newpdx;
//        }
//
//        public String getNewsdx() {
//            return newsdx;
//        }
//
//        public void setNewsdx(String newsdx) {
//            this.newsdx = newsdx;
//        }
//
//    }
//}
