/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.structures.CombinationCode;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
 * @author MinoSun
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
        GrouperMethod gm = new GrouperMethod();
        GetValidatedPreMDC getvalidatedpremdc = new GetValidatedPreMDC();
        SimpleDateFormat timeformat = utility.SimpleDateFormat("HH:mm");
        SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
        DRGOutput drgResult = new DRGOutput();
        try {
            List<String> ProcList = Arrays.asList(grouperparameter.getProc().split(","));
            ArrayList<String> asterisk = new ArrayList<>();
            ArrayList<String> combiCode = new ArrayList<>();
            ArrayList<Integer> NegativeIndex = new ArrayList<>();

            for (int y = 0; y < ProcList.size(); y++) {  //TEST DATA HERE  Procwithgreathervalue
                String dataA = ProcList.get(y).replace(">1", "");  //TEST DATA HERE  Procwithgreathervalue
                for (int w = 0; w < ProcList.size(); w++) {  //TEST DATA HERE  Procwithgreathervalue
                    String dataB = ProcList.get(w).replace(">1", "");  //TEST DATA HERE  Procwithgreathervalue
                    DRGWSResult pcomResult = gm.GetPCOM(datasource, dataA.trim(), dataB.trim());
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
                DRGWSResult gDAResult = gm.GetDA(datasource, grouperparameter.getPdx(), SDxList.get(b).trim());
                asterisk.add(String.valueOf(gDAResult.isSuccess()));
            }
            //GETTING THE INDEX AREA 
            String FinalPrimary = "";
            String FinalSDxList = "";
            SDxPDx swapping = new SDxPDx();
            int indexNumber = asterisk.indexOf("true");
            if (asterisk.contains(checker)) {
                SDxList.set(indexNumber, grouperparameter.getPdx());
                StringBuilder newListSDx = new StringBuilder("");
                for (String eachstring : SDxList) {
                    newListSDx.append(eachstring).append(",");
                }
                //Swapping area
                FinalSDxList = FinalSDxList.substring(0, FinalSDxList.length());
                swapping.setNewsdx(newListSDx.toString());
                List<String> NewPrimary = Arrays.asList(grouperparameter.getSdx().split(","));
                FinalPrimary = NewPrimary.get(indexNumber);
                swapping.setNewpdx(NewPrimary.get(indexNumber));

//                String icdCode = grouperparameter.getPdx() + "+ " + swapping.getNewpdx() + "*";
//                DRGWSResult daValidation = gm.GetValidCodeICD10(datasource, icdCode);
//                DagAstValidation = daValidation.isSuccess();
            } else {
                if (!grouperparameter.getSdx().isEmpty()) {
                    swapping.setNewpdx(grouperparameter.getPdx());
                    swapping.setNewsdx(grouperparameter.getSdx());
                } else {
                    swapping.setNewpdx(grouperparameter.getPdx());
                }
            }

            //==========================================================================================================
           
            DRGWSResult geticd10Result = gm.GetICD10PreMDC(datasource, grouperparameter.getPdx());
            DRGWSResult getSexConfictResult = gm.GenderConfictValidation(datasource, grouperparameter.getPdx(), grouperparameter.getGender());

            //==========================================================================================================
            if (!geticd10Result.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName("Invalid PDx : " + grouperparameter.getPdx());
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 124) {
                drgResult.setDRG("26539");
                drgResult.setDC("2653");
                drgResult.setDRGName("PDx : " + grouperparameter.getPdx() + " Having age conflict : ");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            }  else if (!getSexConfictResult.isSuccess()) {
                drgResult.setDRG("26509");
                drgResult.setDC("2650");
                drgResult.setDRGName(grouperparameter.getPdx() + " PDx having conflict with sex");
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
            } else if (!asterisk.contains(checker)) {
                ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(geticd10Result.getResult(), ICD10PreMDCResult.class);
                if (!icd10Result.getAccPDX().equals("Y")) {
                    drgResult.setDRG("26519");
                    drgResult.setDC("2651");
                    drgResult.setDRGName("Unacceptable PDx" + grouperparameter.getPdx());
                    result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                    result.setSuccess(true);
                } else {

                    GrouperParameter Newgrouperparam = new GrouperParameter();
                    Newgrouperparam.setAdmissionDate(grouperparameter.getAdmissionDate());
                    Newgrouperparam.setAdmissionWeight(grouperparameter.getAdmissionWeight());
                    Newgrouperparam.setBirthDate(grouperparameter.getBirthDate());
                    Newgrouperparam.setDischargeDate(grouperparameter.getDischargeDate());
                    Newgrouperparam.setDischargeType(grouperparameter.getDischargeType());
                    Newgrouperparam.setExpireTime(grouperparameter.getExpireTime());
                    Newgrouperparam.setExpiredDate(grouperparameter.getExpiredDate());
                    Newgrouperparam.setGender(grouperparameter.getGender());
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
                        String comResult = gm.ProcedureExecute(combinationcode);
                        Newgrouperparam.setProc(comResult);
                    }
                    Newgrouperparam.setSdx(grouperparameter.getSdx());
                    Newgrouperparam.setTimeAdmission(grouperparameter.getTimeAdmission());
                    Newgrouperparam.setTimeDischarge(grouperparameter.getTimeDischarge());
                    Newgrouperparam.setTimeOfBirth(grouperparameter.getTimeOfBirth());
                    DRGWSResult getvalidatedpremdcResult = getvalidatedpremdc.GetValidatedPreMDC(datasource, Newgrouperparam);
                    result.setSuccess(getvalidatedpremdcResult.isSuccess());
                    result.setMessage(getvalidatedpremdcResult.getMessage());
                    result.setResult(getvalidatedpremdcResult.getResult());

                }
            } else {
                GrouperParameter Newgrouperparam = new GrouperParameter();
                Newgrouperparam.setAdmissionDate(grouperparameter.getAdmissionDate());
                Newgrouperparam.setAdmissionWeight(grouperparameter.getAdmissionWeight());
                Newgrouperparam.setBirthDate(grouperparameter.getBirthDate());
                Newgrouperparam.setDischargeDate(grouperparameter.getDischargeDate());
                Newgrouperparam.setDischargeType(grouperparameter.getDischargeType());
                Newgrouperparam.setExpireTime(grouperparameter.getExpireTime());
                Newgrouperparam.setExpiredDate(grouperparameter.getExpiredDate());
                Newgrouperparam.setGender(grouperparameter.getGender());
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
                    String comResult = gm.ProcedureExecute(combinationcode);
                    Newgrouperparam.setProc(comResult);
                }
                Newgrouperparam.setSdx(swapping.getNewsdx());
                Newgrouperparam.setTimeAdmission(grouperparameter.getTimeAdmission());
                Newgrouperparam.setTimeDischarge(grouperparameter.getTimeDischarge());
                Newgrouperparam.setTimeOfBirth(grouperparameter.getTimeOfBirth());

                DRGWSResult getvalidatedpremdcResult = getvalidatedpremdc.GetValidatedPreMDC(datasource, Newgrouperparam);
                result.setSuccess(getvalidatedpremdcResult.isSuccess());
                result.setResult(getvalidatedpremdcResult.getResult());
                result.setMessage(getvalidatedpremdcResult.getMessage());
//                System.out.println(Newgrouperparam);

            }
            //====================================================================== 
        } catch (ParseException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ValidateFindMDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

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

}
