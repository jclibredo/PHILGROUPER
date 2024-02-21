/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.structures.BMDCPreMDCResult;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.DRGUtility;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.IOException;
import java.text.ParseException;
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
public class GetValidatedPreMDC {

    public GetValidatedPreMDC() {
    }
    private final Utility utility = new Utility();
    private final DRGUtility drgutility = new DRGUtility();
    private final ProcessMDC getdc = new ProcessMDC();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGWSResult GetValidatedPreMDC(final DataSource datasource, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        DRGOutput drgResult = new DRGOutput();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().trim().split(","));
        List<String> SDxList = Arrays.asList(grouperparameter.getSdx().trim().split(","));
        String pdx = "";
        try {
            String Days = String.valueOf(drgutility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()));
            String Years = String.valueOf(drgutility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()));
            DRGWSResult icd10SortResult = gm.GetICD10(datasource, grouperparameter.getPdx(), Days, Years, grouperparameter.getGender());
            ICD10PreMDCResult icd10Result = utility.objectMapper().readValue(icd10SortResult.getResult(), ICD10PreMDCResult.class);
            DRGWSResult GetBMDC = gm.GetBMDC(datasource, grouperparameter.getPdx());
            int TraumaCounterPDXO = 0;
            int TraumaCounterPDX1 = 0;
            DRGWSResult TraumaPrimary = gm.TRAUMAICD10(datasource, grouperparameter.getPdx());
            if (TraumaPrimary.isSuccess()) {
                if (!TraumaPrimary.getResult().equals("0")) {
                    TraumaCounterPDX1++;
                    pdx = TraumaPrimary.getResult();
                } else {
                    TraumaCounterPDXO++;
                }
            }
            // System.out.println("PDx Trauma Checking : "+TraumaPrimary.getResult());
            int ProccCount = 0;
            int SDXcountO = 0;
            int SDXcountI = 0;
            LinkedList<String> procSite = new LinkedList<>();
            LinkedList<String> sdxSite = new LinkedList<>();
            LinkedList<String> sdxnewlist = new LinkedList<>();
            LinkedList<String> finalsdxnewlist = new LinkedList<>();
            LinkedList<String> dupsdxnewlist = new LinkedList<>();
            LinkedList<String> procnewlist = new LinkedList<>();
            LinkedList<String> finalprocnewlist = new LinkedList<>();
            LinkedList<String> dupprocnewlist = new LinkedList<>();
            LinkedList<String> sdxpdxnewlist = new LinkedList<>();

//            System.out.println("Orig Sdx : " + oripdx);
//            System.out.println("Orig Pdx : " + orisdx);
//            System.out.println("----------------------------------------------");
//            System.out.println("Orig Sdx : " + grouperparameter.getSdx());
//            System.out.println("Orig Pdx : " + grouperparameter.getPdx());
            if (SDxList.size() > 0) {
                for (int x = 0; x < SDxList.size(); x++) {
                    String secon = SDxList.get(x);
                    DRGWSResult SDxVal = gm.TRAUMAICD10(datasource, secon);
                    if (String.valueOf(SDxVal.isSuccess()).equals("true")) {
                        if (!SDxVal.getResult().equals("0")) {
                            SDXcountI++;
                            sdxSite.add(SDxVal.getResult());
                            sdxnewlist.add(SDxVal.getResult());
                        }
                    }
                }
            }

            String OCX = "0CX";
            String ODX = "0DX";
            String OEX = "0EX";
            int Counter0PB = 0;
            int Counter0PA = 0;
            int Counter0PD = 0;
            int Counter0CX = 0;
            int Counter0DX = 0;
            int Counter0EX = 0;
            DRGWSResult ResultOCX = gm.AX(datasource, OCX, grouperparameter.getPdx());
            if (ResultOCX.isSuccess()) {
                Counter0CX++;
            }
            DRGWSResult ResultODX = gm.AX(datasource, ODX, grouperparameter.getPdx());
            if (ResultODX.isSuccess()) {
                Counter0DX++;
            }
            DRGWSResult ResultOEX = gm.AX(datasource, OEX, grouperparameter.getPdx());
            if (ResultOEX.isSuccess()) {
                Counter0EX++;
            }

            if (ProcedureList.size() > 0) {
                for (int x = 0; x < ProcedureList.size(); x++) {
                    String proc = ProcedureList.get(x);
                    DRGWSResult PROC = gm.TRAUMAICD9CM(datasource, proc);
                    if (PROC.isSuccess()) {
                        ProccCount++;
                        procSite.add(PROC.getResult());
                        procnewlist.add(PROC.getResult());
                    }
                    if (drgutility.isValidOPA(proc)) {
                        Counter0PA++;
                    }
                    if (drgutility.isValidOPD(proc)) {
                        Counter0PD++;
                    }
                    if (drgutility.isValidOPB(proc)) {
                        Counter0PB++;
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
            
            
            if (GetBMDC.isSuccess()) {
                BMDCPreMDCResult bmdcResult = utility.objectMapper().readValue(GetBMDC.getResult(), BMDCPreMDCResult.class);
                if (drgutility.ComputeLOS(grouperparameter.getAdmissionDate(), drgutility.Convert24to12(grouperparameter.getTimeAdmission()), grouperparameter.getDischargeDate(), drgutility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0
                        && drgutility.ComputeTime(grouperparameter.getAdmissionDate(), drgutility.Convert24to12(grouperparameter.getTimeAdmission()), grouperparameter.getDischargeDate(), drgutility.Convert24to12(grouperparameter.getTimeDischarge())) <= 6
                        && drgutility.MinutesCompute(grouperparameter.getAdmissionDate(), drgutility.Convert24to12(grouperparameter.getTimeAdmission()), grouperparameter.getDischargeDate(), drgutility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0) {

                    if (drgutility.ComputeTime(grouperparameter.getAdmissionDate(),
                            drgutility.Convert24to12(grouperparameter.getTimeAdmission()),
                            grouperparameter.getDischargeDate(),
                            drgutility.Convert24to12(grouperparameter.getTimeDischarge())) < 2) {
                        drgResult.setDRG("26549");
                        drgResult.setDRGName("LOS < 6 Hours");
                    } else {
                        drgResult.setMDC("28");
                    }
                } else if (Counter0CX > 0 && Counter0PA > 0) { //Liver Transplant
                    drgResult.setDC("0001");
                    drgResult.setDRG("00019");
                    drgResult.setDRGName("Liver Transplant");
                } else if (Counter0DX > 0 && Counter0PB > 0) {//Heart-Lung Transplant
                    drgResult.setDC("0002");
                    drgResult.setDRG("00029");
                    drgResult.setDRGName("Heart-Lung Transplant");
                } else if (Counter0EX > 0 && Counter0PD > 0) {//Bone Marrow Transplant
                    drgResult.setDC("0004");
                    drgResult.setDRG("00049");
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
                } else if (bmdcResult.getICD10().equals(grouperparameter.getPdx()) && grouperparameter.getGender().equals("M")) {
                    drgResult.setMDC(bmdcResult.getMDC_M());
                    drgResult.setPDC(bmdcResult.getPDC_M());
                } else if (bmdcResult.getICD10().equals(grouperparameter.getPdx()) && grouperparameter.getGender().equals("F")) {
                    drgResult.setMDC(bmdcResult.getMDC_F());
                    drgResult.setPDC(bmdcResult.getPDC_F());
                } else if (drgutility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) == 0 && drgutility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) < 28) {
                    drgResult.setMDC("15");
                } else {
                    drgResult.setMDC(icd10Result.getMDC());
                    drgResult.setPDC(icd10Result.getPDC());
                }

            } else {
                if (drgutility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 124) {
                    drgResult.setDRG("26539");
                } else if (drgutility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        drgutility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        drgutility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0
                        && drgutility.ComputeTime(grouperparameter.getAdmissionDate(),
                                drgutility.Convert24to12(grouperparameter.getTimeAdmission()),
                                grouperparameter.getDischargeDate(),
                                drgutility.Convert24to12(grouperparameter.getTimeDischarge())) <= 6
                        && drgutility.MinutesCompute(grouperparameter.getAdmissionDate(),
                                drgutility.Convert24to12(grouperparameter.getTimeAdmission()),
                                grouperparameter.getDischargeDate(),
                                drgutility.Convert24to12(grouperparameter.getTimeDischarge())) <= 0) {

                    if (drgutility.ComputeTime(grouperparameter.getAdmissionDate(),
                            drgutility.Convert24to12(grouperparameter.getTimeAdmission()),
                            grouperparameter.getDischargeDate(),
                            drgutility.Convert24to12(grouperparameter.getTimeDischarge())) < 2) {

                        drgResult.setDRG("26549");
                        drgResult.setDRGName("LOS < 6 Hours");
                    } else {
                        drgResult.setMDC("28");
                    }
                } else if (Counter0CX > 0 && Counter0PA > 0) { //Liver Transplant
                    drgResult.setDC("0001");
                    drgResult.setDRG("00019");
                    drgResult.setDRGName("Liver Transplant");
                } else if (Counter0DX > 0 && Counter0PB > 0) {//Heart-Lung Transplant
                    drgResult.setDC("0002");
                    drgResult.setDRG("00029");
                    drgResult.setDRGName("Heart-Lung Transplant");
                } else if (Counter0EX > 0 && Counter0PD > 0) {//Bone Marrow Transplant
                    drgResult.setDC("0004");
                    drgResult.setDRG("00049");
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
                } else if (drgutility.ComputeYear(grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate()) <= 0 && drgutility.ComputeDay(grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate()) < 28) {
                    drgResult.setMDC("15");
                } else {
                    drgResult.setMDC(icd10Result.getMDC());
                    drgResult.setPDC(icd10Result.getPDC());
                }
            }
            //END OF PARSING PART
            
            
            
            
            
            if (drgResult.getDRG() == null) {
                DRGWSResult getdcResult = getdc.ProcessMDC(datasource, drgResult, grouperparameter);
                result.setMessage(getdcResult.getMessage());
                result.setSuccess(getdcResult.isSuccess());
                result.setResult(getdcResult.getResult());
            } else {
                result.setResult(utility.objectMapper().writeValueAsString(drgResult));
                result.setSuccess(true);
                result.setMessage("Grouper Done in Pre-MDC level only");
            }

        } catch (ParseException | IOException | NullPointerException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetValidatedPreMDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
