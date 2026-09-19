/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.GetPDC;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.methods.validation.ORProcedure;
import grouper.methods.validation.PDxMalignancy;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class GetMDC07 {

    public GetMDC07() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC07(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            AX checkAX = new AX();
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            //Checking SDx RadioTherapy and Chemotherapy
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
<<<<<<< Updated upstream
                if (utility.isValid99BX(SecondaryList.get(a).trim())) {
                    CartSDx++;
                }
                if (utility.isValid99CX(SecondaryList.get(a).trim())) {
=======
                if (checkAX.AX(datasource, SchemaName, "99BX", SecondaryList.get(a).trim()).isSuccess()) {
                    CartSDx++;
                }
                if (checkAX.AX(datasource, SchemaName, "99CX", SecondaryList.get(a).trim()).isSuccess()) {
>>>>>>> Stashed changes
                    CaCRxSDx++;
                }
            }

            //Maj Dig Dis AX 6BX
            int B7Count = 0;
            if (new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "7B").isSuccess()) {
                B7Count++;
            }
            //Maj Dig Dis AX 7PDX
            int Counter7PDX = 0;
            int Counter7PBX = 0;
            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            for (int y = 0; y < ProcedureList.size(); y++) {
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, ProcedureList.get(y), drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }

                //AX 99PDX Checking
<<<<<<< Updated upstream
                if (utility.isValid99PDX(ProcedureList.get(y).trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(y).trim())) {
                    PCXCounter99++;
                }
                if (utility.isValid99PEX(ProcedureList.get(y).trim())) {
                    CartProc++;
                }
                if (utility.isValid99PFX(ProcedureList.get(y).trim())) {
                    CaCRxProc++;
                }
                if (utility.isValid99PBX(ProcedureList.get(y).trim())) { //Blood Transfusion AX 99PBX
=======
                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PEX", ProcedureList.get(y).trim()).isSuccess()) {
                    CartProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PFX", ProcedureList.get(y).trim()).isSuccess()) {
                    CaCRxProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PBX", ProcedureList.get(y).trim()).isSuccess()) {
>>>>>>> Stashed changes
                    PBX99Proc++;
                }

                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //-------------------------------------------------------------
                if (checkAX.AX(datasource, "7PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter7PDX++;
                }
                //Maj Dig Dis AX 7PBX
                if (checkAX.AX(datasource, "7PBX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter7PBX++;
                }
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (mdcprocedureCounter > 0) {
                        MDC5Proc getMdcProc = this.mdcProcedure(hierarvalue, pdclist, B7Count, Counter7PBX);
                        drgResult.setPDC(getMdcProc.getPdc());
                        drgResult.setDC(getMdcProc.getDc());
                    } else if (ORProcedureCounter > 0) {
                        String dc = this.orProcedure(ORProcedureCounterList);
                        drgResult.setDC(dc);
                    } else {
                        String dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                Counter7PDX,
                                PBX99Proc,
                                grouperparameter.getDischargeType());
                        drgResult.setDC(dc);
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0712");
                    } else {
                        drgResult.setDC("0713");
                    }
                }
            } else if (mdcprocedureCounter > 0) {
                MDC5Proc getMdcProc = this.mdcProcedure(hierarvalue, pdclist, B7Count, Counter7PBX);
                drgResult.setPDC(getMdcProc.getPdc());
                drgResult.setDC(getMdcProc.getDc());
            } else if (ORProcedureCounter > 0) {
                String dc = this.orProcedure(ORProcedureCounterList);
                drgResult.setDC(dc);
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter7PDX,
                        PBX99Proc,
                        grouperparameter.getDischargeType());
                drgResult.setDC(dc);
            }

//            drgResult.setPrepccl("X");
//            drgResult.setFinalpccl("X");
//            drgResult.setDRGName("Grouper Error");
//            drgResult.setDRG(drgResult.getDC() + "X");
//            DRG checkDRG = new DRG();
//            if (drgResult.getDRG() == null) {
//                if (utility.isValidDCList(drgResult.getDC())) {
//                    drgResult.setDRG(drgResult.getDC() + "9");
//                    drgResult.setPrepccl("9");
//                    drgResult.setFinalpccl("9");
//                } else {
//                    //  String sdxfinalList =  new GrouperMethod().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
//                    String sdxfinalList = new CleanSDxDCDeterminationPLSQL().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
//                    DRGWSResult getpcclvalue = new GetPCCL().GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
//                    if (getpcclvalue.isSuccess()) {
//                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
//                        drgResult.setPrepccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
//                        drgResult.setFinalpccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
//                        drgResult.setDRG(finaldrgresult.getDRG());
//                        if (checkDRG.DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
//                            drgResult.setDRGName(checkDRG.DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).getMessage());
//                        } else {
//                            DRGWSResult drgvalues = new ValidatePCCL().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG());
//                            if (drgvalues.isSuccess()) {
//                                String drgcode = drgResult.getDC() + drgvalues.getResult();
//                                drgResult.setDRG(drgcode);
//                                DRGWSResult drgnames = checkDRG.DRG(datasource, drgResult.getDC(), drgcode);
//                                if (drgnames.isSuccess()) {
//                                    drgResult.setDRGName(drgnames.getMessage());
//                                }
//                                drgResult.setFinalpccl(drgcode.substring(drgcode.length() - 1));
//                            } else {
//                                drgResult.setDRGName("DRG code grouper provide not exist in the library");
//                            }
//                        }
//                    }
//                }
//            } else {
//                if (checkDRG.DRG(datasource, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
//                    drgResult.setDRGName(checkDRG.DRG(datasource, drgResult.getDC(), drgResult.getDRG()).getMessage());
//                } else {
//                    drgResult.setDRGName("DRG code grouper provide not exist in the library");
//                }
//            }
//            result.setSuccess(true);
//            result.setMessage("MDC 07 Done Checking");
//            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 07 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC07.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;

    }

    public MDC5Proc mdcProcedure(
            final ArrayList<Integer> hierarvalue,
            final ArrayList<String> pdclist,
            final Integer B7Count,
            final Integer Counter7PBX) {
        MDC5Proc result = new MDC5Proc();
        result.setPdc("");
        result.setDc("");
        result.setSdxfinder("");
        //CHECKING FOR TRAUMA CODES
        int min = hierarvalue.get(0);
        //Loop through the array  
        for (int i = 0; i < hierarvalue.size(); i++) {
            //Compare elements of array with min  
            if (hierarvalue.get(i) < min) {
                min = hierarvalue.get(i);
            }
        }
        result.setPdc(pdclist.get(hierarvalue.indexOf(min)));
        switch (pdclist.get(hierarvalue.indexOf(min))) {
            case "7PA"://Pancreas, Liver Resection and Shunt Procedures
                result.setDc("0701");
                break;
            case "7PB"://Biliary Tract Procedure
                if (B7Count > 0) {
                    result.setDc("0702");
                } else {
                    result.setDc("0703");
                }
                break;
            case "7PG"://Pancreas and Liver Procedure Except Resection
                result.setDc("0711");
                break;
            case "7PF"://Laparoscopic Cholecystectomy
                if (Counter7PBX > 0) {
                    result.setDc("0709");
                } else {
                    result.setDc("0710");
                }
                break;
            case "7PC"://Cholecystectomy
                if (Counter7PBX > 0) {
                    result.setDc("0704");
                } else {
                    result.setDc("0705");
                }
                break;
            case "7PD"://Hepatobiliary Diagnostic Procedures
                result.setDc("0706");
                break;
            case "7PE"://Other Hepatobiliary and Pancreas Procedures
                result.setDc("0707");
                break;
            case "7PH"://ERCP with Therapeutic Procedures 7PH
                result.setDc("0708");
                break;
        }
        return result;
    }

    public String orProcedure(ArrayList<Integer> ORProcedureCounterList) {
        String dc = "";
        switch (Collections.max(ORProcedureCounterList)) {
            case 1:
                dc = "2601";
                break;
            case 2:
                dc = "2602";
                break;
            case 3:
                dc = "2603";
                break;
            case 4:
                dc = "2604";
                break;
            case 5:
                dc = "2605";
                break;
            case 6:
                dc = "2606";
                break;
        }
        return dc;
    }

    public String principalDaignosis(
            final String pdc,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer Counter7PDX,
            final Integer PBX99Proc,
            final String disChargeType) {
        String dc = "";
        switch (pdc) {
            case "7B"://Malignancy of Hepatobiliary or Pancreas
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "0756";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "0757";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "0758";
                } else if (Counter7PDX > 0) { //##Dx Procedure
                    dc = "0759";
                    //Radiotherapy
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "0760";
                } else {//Malignancy 
                    if (disChargeType.equals("4")) {
                        dc = "0761";
                    } else {
                        dc = "0751";
                    }
                }
                break;
            case "7A"://Cirrhosis and Alcoholic Hepatitis
                dc = "0750";
                break;
            case "7C"://Disorder of Pancreas, Except Malignancy
                dc = "0753";
                break;
            case "7D"://Disorder of Liver, Except Malignancy, Cirrhosis, Alcoholic Hepatitis
                dc = "0754";
                break;
            case "7E"://Disorder of Biliary Tract 7E
                dc = "0755";
                break;
        }
        return dc;

    }

    public class MDC5Proc {

        private String pdc;
        private String dc;
        private String sdxfinder;

        public String getPdc() {
            return pdc;
        }

        public void setPdc(String pdc) {
            this.pdc = pdc;
        }

        public String getDc() {
            return dc;
        }

        public void setDc(String dc) {
            this.dc = dc;
        }

        public String getSdxfinder() {
            return sdxfinder;
        }

        public void setSdxfinder(String sdxfinder) {
            this.sdxfinder = sdxfinder;
        }

    }

}
