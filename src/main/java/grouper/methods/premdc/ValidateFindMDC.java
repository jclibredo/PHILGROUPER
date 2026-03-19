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
import grouper.structures.CombinationCode;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class ValidateFindMDC {

    public ValidateFindMDC() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ValidateFindMDC(final DataSource datasource, final GrouperParameter grouperparameter) throws ParseException {
        String checker = "true";
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        DRGOutput drgResult = new DRGOutput();
        try {
            //REMAP AREA
            GrouperParameter Newgrouperparam = new GrouperParameter();
            Newgrouperparam.setAdmissionDate(grouperparameter.getAdmissionDate());
            Newgrouperparam.setAdmissionWeight(grouperparameter.getAdmissionWeight());
            Newgrouperparam.setBirthDate(grouperparameter.getBirthDate());
            Newgrouperparam.setDischargeDate(grouperparameter.getDischargeDate());
            Newgrouperparam.setDischargeType(grouperparameter.getDischargeType());
            Newgrouperparam.setExpireTime(grouperparameter.getExpireTime());
            Newgrouperparam.setExpiredDate(grouperparameter.getExpiredDate());
            Newgrouperparam.setTimeAdmission(grouperparameter.getTimeAdmission());
            Newgrouperparam.setTimeDischarge(grouperparameter.getTimeDischarge());
            Newgrouperparam.setTimeOfBirth(grouperparameter.getTimeOfBirth());
            Newgrouperparam.setWarningerror(grouperparameter.getWarningerror());
            Newgrouperparam.setGender(grouperparameter.getGender());

            List<String> ProcList = Arrays.asList(grouperparameter.getProc().split(","));
            ArrayList<String> asterisk = new ArrayList<>();
            ArrayList<String> combiCode = new ArrayList<>();
            ArrayList<Integer> NegativeIndex = new ArrayList<>();
            for (int y = 0; y < ProcList.size(); y++) {  //TEST DATA HERE  Procwithgreathervalue
                String dataA = ProcList.get(y).replace(">1", "");  //TEST DATA HERE  Procwithgreathervalue
                for (int w = 0; w < ProcList.size(); w++) {  //TEST DATA HERE  Procwithgreathervalue
                    String dataB = ProcList.get(w).replace(">1", "");  //TEST DATA HERE  Procwithgreathervalue
                    DRGWSResult pcomResult = new GetPCOM().GetPCOM(datasource, dataA.trim(), dataB.trim());
                    if (String.valueOf(pcomResult.isSuccess()).equals("true")) {
                        combiCode.add(pcomResult.getResult());
                        for (int i = 0; i < ProcList.size(); i++) {  //TEST DATA HERE  Procwithgreathervalue
                            if (!ProcList.get(i).replace(">1", "").equals(dataA) && !ProcList.get(i).replace(">1", "").equals(dataB)) {  //TEST DATA HERE  Procwithgreathervalue
                            } else {
                                NegativeIndex.add(i);
                            }
                        }
                    }
                }
            }

            LinkedList<String> FinalListComcode = new LinkedList<>();
            for (int m = 0; m < NegativeIndex.size(); m++) {
                FinalListComcode.add(ProcList.get(NegativeIndex.get(m))); //TEST DATA HERE  Procwithgreathervalue
            }
            LinkedList<String> CleanCombiCode = new LinkedList<>();
            for (int i = 0; i < combiCode.size() - 1; i++) {
                for (int j = i + 1; j < combiCode.size(); j++) {
                    if (combiCode.get(i).equals(combiCode.get(j)) && (i != j)) {
                        CleanCombiCode.add(combiCode.get(j));
                    }
                }
            }
            //==================================
            List<String> SDxList = Arrays.asList(grouperparameter.getSdx().split(","));
            for (int b = 0; b < SDxList.size(); b++) {
                DRGWSResult gDAResult = new GetDA().GetDA(datasource, grouperparameter.getPdx(), SDxList.get(b).trim());
                if (gDAResult.isSuccess()) {
                    asterisk.add("true");
                } else {
                    asterisk.add("false");
                }
            }
            //GETTING THE INDEX AREA 
            //String FinalPrimary = "";
            String FinalSDxList = "";
            SDxPDx swapping = new SDxPDx();
            int indexNumber = asterisk.indexOf("true");
            if (asterisk.contains(checker)) {
                SDxList.set(indexNumber, grouperparameter.getPdx());
                StringBuilder newListSDx = new StringBuilder("");
                SDxList.forEach((eachstring) -> {
                    newListSDx.append(eachstring).append(",");
                });
                //Swapping area
                FinalSDxList = FinalSDxList.substring(0, FinalSDxList.length());
                swapping.setNewsdx(newListSDx.toString());
                List<String> NewPrimary = Arrays.asList(grouperparameter.getSdx().split(","));
                swapping.setNewpdx(NewPrimary.get(indexNumber));
            } else {
                if (!grouperparameter.getSdx().isEmpty()) {
                    swapping.setNewpdx(grouperparameter.getPdx());
                    swapping.setNewsdx(grouperparameter.getSdx());
                } else {
                    swapping.setNewpdx(grouperparameter.getPdx());
                }
            }
            //==========================================================================================================
            DRGWSResult geticd10Result = new GetICD10PreMDC().GetICD10PreMDC(datasource, swapping.getNewpdx());
            DRGWSResult getSexConfictResult = new GenderConfictValidation().GenderConfictValidation(datasource, swapping.getNewpdx(), grouperparameter.getGender());
            //==========================================================================================================
            if (!geticd10Result.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("Invalid PDx : " + grouperparameter.getPdx());
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 124) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("PDx : " + grouperparameter.getPdx() + " Having age conflict : ");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (!getSexConfictResult.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName(grouperparameter.getPdx() + " PDx having conflict with sex");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (!asterisk.contains(checker)) {

                ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(geticd10Result.getResult(), ICD10PreMDCResult.class);
                if (!icd10Result.getAccPDX().equals("Y")) {
                    drgResult.setDRG("26509");
                    drgResult.setDC("2650");
                    drgResult.setDRGName("Unacceptable PDx" + grouperparameter.getPdx());
                    result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                    result.setSuccess(true);
                } else {
                    Newgrouperparam.setPdx(grouperparameter.getPdx());
                    if (NegativeIndex.isEmpty()) {
                        Newgrouperparam.setProc(String.join(",", ProcList));
                    } else {
                        String Combinecode = String.join(",", CleanCombiCode);
                        String indextoremove = String.join(",", FinalListComcode);
                        String OrigList = String.join(",", ProcList);             //TEST DATA HERE  Procwithgreathervalue
                        CombinationCode combinationcode = new CombinationCode();
                        combinationcode.setComcode(Combinecode);
                        combinationcode.setIndexlist(indextoremove);
                        combinationcode.setProclist(OrigList);
                        String comResult = utility.ProcedureExecute(combinationcode);
                        Newgrouperparam.setProc(comResult);
                    }
                    Newgrouperparam.setSdx(grouperparameter.getSdx());
                    DRGWSResult getvalidatedpremdcResult = new GetValidatedPreMDC().GetValidatedPreMDC(datasource, Newgrouperparam);
                    result.setSuccess(getvalidatedpremdcResult.isSuccess());
                    result.setMessage(getvalidatedpremdcResult.getMessage());
                    result.setResult(getvalidatedpremdcResult.getResult());
                }
            } else {
                Newgrouperparam.setPdx(swapping.getNewpdx());
                if (NegativeIndex.isEmpty()) {
                    Newgrouperparam.setProc(String.join(",", ProcList));
                } else {
                    String Combinecode = String.join(",", CleanCombiCode);
                    String indextoremove = String.join(",", FinalListComcode);
                    String OrigList = String.join(",", ProcList);             //TEST DATA HERE  Procwithgreathervalue
                    CombinationCode combinationcode = new CombinationCode();
                    combinationcode.setComcode(Combinecode);
                    combinationcode.setIndexlist(indextoremove);
                    combinationcode.setProclist(OrigList);
                    String comResult = utility.ProcedureExecute(combinationcode);
                    Newgrouperparam.setProc(comResult);
                }
                Newgrouperparam.setSdx(swapping.getNewsdx());
                DRGWSResult getvalidatedpremdcResult = new GetValidatedPreMDC().GetValidatedPreMDC(datasource, Newgrouperparam);
                result = getvalidatedpremdcResult;
            }
            //====================================================================== 
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidateFindMDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
//

    public class SDxPDx {

        private String newpdx; // private = restricted access
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

//    public DRGWSResult ValidateFindMDC(final DataSource datasource, final GrouperParameter grouperparameter) throws ParseException {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setSuccess(false);
//        DRGOutput drgResult = new DRGOutput();
//        drgResult.setWarningerror(grouperparameter.getWarningerror());
//        
//        try {
//            // 1. Process Procedures efficiently
//            List<String> procList = Arrays.stream(grouperparameter.getProc().split(","))
//                    .map(String::trim)
//                    .collect(Collectors.toList());
//            
//            Set<String> combiCodes = new LinkedHashSet<>();
//            Set<Integer> negativeIndices = new HashSet<>();
//            GetPCOM pcomService = new GetPCOM();
//
//            // Optimized nested loop: only iterate what is necessary
//            for (int y = 0; y < procList.size(); y++) {
//                String dataA = procList.get(y).replace(">1", "").trim();
//                for (int w = 0; w < procList.size(); w++) {
//                    String dataB = procList.get(w).replace(">1", "").trim();
//                    
//                    DRGWSResult pcomResult = pcomService.GetPCOM(datasource, dataA, dataB);
//                    if (pcomResult.isSuccess()) {
//                        combiCodes.add(pcomResult.getResult());
//                        negativeIndices.add(y);
//                        negativeIndices.add(w);
//                    }
//                }
//            }
//
//            // 2. Secondary Diagnosis (SDx) Validation
//            List<String> sdxList = new ArrayList<>(Arrays.asList(grouperparameter.getSdx().split(",")));
//            List<Boolean> asterisk = new ArrayList<>();
//            GetDA daService = new GetDA();
//            sdxList.forEach((sdx) -> {
//                asterisk.add(daService.GetDA(datasource, grouperparameter.getPdx(), sdx.trim()).isSuccess());
//            });
//            // 3. Handle Swapping Logic
//            SDxPDx swapping = new SDxPDx();
//            int swapIndex = asterisk.indexOf(true);
//            boolean hasSwap = swapIndex != -1;
//            
//            if (hasSwap) {
//                swapping.setNewpdx(sdxList.get(swapIndex));
//                sdxList.set(swapIndex, grouperparameter.getPdx());
//                swapping.setNewsdx(String.join(",", sdxList));
//            } else {
//                swapping.setNewpdx(grouperparameter.getPdx());
//                swapping.setNewsdx(grouperparameter.getSdx());
//            }
//
//            // 4. Validations
//            DRGWSResult icd10Check = new GetICD10PreMDC().GetICD10PreMDC(datasource, swapping.getNewpdx());
//            DRGWSResult sexCheck = new GenderConfictValidation().GenderConfictValidation(datasource, swapping.getNewpdx(), grouperparameter.getGender());
//            long age = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
//            
//            if (!icd10Check.isSuccess() || age > 124 || !sexCheck.isSuccess()) {
//                return buildErrorResult(result, drgResult, swapping.getNewpdx(), age > 124 ? "age" : "pdx");
//            }
//
//            // 5. Finalize Parameters and Execute
//            GrouperParameter newParam = cloneParams(grouperparameter);
//            newParam.setPdx(swapping.getNewpdx());
//            newParam.setSdx(swapping.getNewsdx());
//            
//            if (!negativeIndices.isEmpty()) {
//                CombinationCode combo = new CombinationCode();
//                combo.setComcode(String.join(",", combiCodes));
//                combo.setIndexlist(negativeIndices.stream().map(procList::get).collect(Collectors.joining(",")));
//                combo.setProclist(String.join(",", procList));
//                newParam.setProc(utility.ProcedureExecute(combo));
//            } else {
//                newParam.setProc(String.join(",", procList));
//            }
//            return new GetValidatedPreMDC().GetValidatedPreMDC(datasource, newParam);
//        } catch (IOException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(ValidateFindMDC.class.getName()).log(Level.SEVERE, null, ex);
//            return result;
//        }
//    }
//
//    // Helper to avoid duplicate code for Error Building
//    private DRGWSResult buildErrorResult(DRGWSResult res, DRGOutput drg, String pdx, String type) throws IOException {
//        drg.setDRG("26509");
//        drg.setDC("2650");
//        drg.setDRGName("Error with " + type + " : " + pdx);
//        res.setResult(utility.objectMapper().writeValueAsString(drg));
//        res.setSuccess(true);
//        return res;
//    }
//
//    // Helper to clone GrouperParameter
//    private GrouperParameter cloneParams(GrouperParameter old) {
//        GrouperParameter n = new GrouperParameter();
//        n.setAdmissionDate(old.getAdmissionDate());
//        n.setAdmissionWeight(old.getAdmissionWeight());
//        n.setBirthDate(old.getBirthDate());
//        n.setDischargeDate(old.getDischargeDate());
//        n.setDischargeType(old.getDischargeType());
//        n.setExpireTime(old.getExpireTime());
//        n.setExpiredDate(old.getExpiredDate());
//        n.setGender(old.getGender());
//        n.setTimeAdmission(old.getTimeAdmission());
//        n.setTimeDischarge(old.getTimeDischarge());
//        n.setTimeOfBirth(old.getTimeOfBirth());
//        return n;
//    }
}
