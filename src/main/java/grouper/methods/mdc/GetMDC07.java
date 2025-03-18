/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
import grouper.utility.GrouperMethod;
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
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);
                //AX 99PDX Checking
                if (utility.isValid99PDX(proc)) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(proc)) {
                    PCXCounter99++;
                }
            }

            //Checking SDx RadioTherapy and Chemotherapy
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                String Secon = SecondaryList.get(a);
                if (utility.isValid99BX(Secon)) {
                    CartSDx++;
                }

                if (utility.isValid99CX(Secon)) {
                    CaCRxSDx++;
                }
            }

            //Checking Procedure RadioTherapy and Chemotherapy
            for (int a = 0; a < ProcedureList.size(); a++) {
                String Proce = ProcedureList.get(a);
                if (utility.isValid99PEX(Proce)) {
                    CartProc++;
                }
                if (utility.isValid99PFX(Proce)) {
                    CaCRxProc++;
                }
                if (utility.isValid99PBX(Proce)) { //Blood Transfusion AX 99PBX
                    PBX99Proc++;
                }
            }

            //Maj Dig Dis AX 6BX
            String PDC7B = "7B";
            int B7Count = 0;
            DRGWSResult getpdc7BCountResult = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), PDC7B);
            if (getpdc7BCountResult.isSuccess()) {
                B7Count++;
            }
            //Maj Dig Dis AX 7PDX
            int Counter7PDX = 0;
            int Counter7PBX = 0;
            String PDX7 = "7PDX";
            String PBX7 = "7PBX";
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procPDX7 = ProcedureList.get(y);
                DRGWSResult Result7PDX = gm.AX(datasource, PDX7, procPDX7);
                if (Result7PDX.isSuccess()) {
                    Counter7PDX++;
                }
                //Maj Dig Dis AX 7PBX
                DRGWSResult Result7PBX = gm.AX(datasource, PBX7, procPDX7);
                if (Result7PBX.isSuccess()) {
                    Counter7PBX++;
                }
            }

            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            //  String models = String.join(",", secondaryList);
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procs = ProcedureList.get(y);
                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, procs);
                if (String.valueOf(ORProcedureResult.isSuccess()).equals("true")) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            for (int y = 0; y < ProcedureList.size(); y++) {
                String proc = ProcedureList.get(y);
                DRGWSResult JoinResult = gm.MDCProcedure(datasource, proc, drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = gm.GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (String.valueOf(pdcresult.isSuccess()).equals("true")) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }

            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        //Loop through the array  
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            //Compare elements of array with min  
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        switch (pdclist.get(hierarvalue.indexOf(min))) {
                            case "7PA"://Pancreas, Liver Resection and Shunt Procedures
                                drgResult.setDC("0701");
                                break;
                            case "7PB"://Biliary Tract Procedure
                                if (B7Count > 0) {
                                    drgResult.setDC("0702");
                                } else {
                                    drgResult.setDC("0703");
                                }
                                break;
                            case "7PG"://Pancreas and Liver Procedure Except Resection
                                drgResult.setDC("0711");
                                break;
                            case "7PF"://Laparoscopic Cholecystectomy
                                if (Counter7PBX > 0) {
                                    drgResult.setDC("0709");
                                } else {
                                    drgResult.setDC("0710");
                                }
                                break;
                            case "7PC"://Cholecystectomy
                                if (Counter7PBX > 0) {
                                    drgResult.setDC("0704");
                                } else {
                                    drgResult.setDC("0705");
                                }
                                break;
                            case "7PD"://Hepatobiliary Diagnostic Procedures
                                drgResult.setDC("0706");
                                break;
                            case "7PE"://Other Hepatobiliary and Pancreas Procedures
                                drgResult.setDC("0707");
                                break;
                            case "7PH"://ERCP with Therapeutic Procedures 7PH
                                drgResult.setDC("0708");
                                break;
                        }

                    } else if (ORProcedureCounter > 0) {
                        switch (Collections.max(ORProcedureCounterList)) {
                            case 1:
                                drgResult.setDC("2601");
                                break;
                            case 2:
                                drgResult.setDC("2602");
                                break;
                            case 3:
                                drgResult.setDC("2603");
                                break;
                            case 4:
                                drgResult.setDC("2604");
                                break;
                            case 5:
                                drgResult.setDC("2605");
                                break;
                            case 6:
                                drgResult.setDC("2606");
                                break;
                        }
                    } else {
                        switch (drgResult.getPDC()) {
                            case "7B"://Malignancy of Hepatobiliary or Pancreas
                                //Radio+Chemotherapy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0756");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0757");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("0758");
                                } else if (Counter7PDX > 0) { //##Dx Procedure
                                    drgResult.setDC("0759");
                                    //Radiotherapy
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("0760");
                                } else {//Malignancy 
                                    if (grouperparameter.getDischargeType().equals("4")) {
                                        drgResult.setDC("0761");
                                    } else {
                                        drgResult.setDC("0751");
                                    }
                                }
                                break;

                            case "7A"://Cirrhosis and Alcoholic Hepatitis
                                drgResult.setDC("0750");
                                break;
                            case "7C"://Disorder of Pancreas, Except Malignancy
                                drgResult.setDC("0753");
                                break;
                            case "7D"://Disorder of Liver, Except Malignancy, Cirrhosis, Alcoholic Hepatitis
                                drgResult.setDC("0754");
                                break;
                            case "7E"://Disorder of Biliary Tract 7E
                                drgResult.setDC("0755");
                                break;
                        }
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0712");
                    } else {
                        drgResult.setDC("0713");
                    }
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                //Loop through the array  
                for (int i = 0; i < hierarvalue.size(); i++) {
                    //Compare elements of array with min  
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }

                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                switch (pdclist.get(hierarvalue.indexOf(min))) {
                    case "7PA"://Pancreas, Liver Resection and Shunt Procedures
                        drgResult.setDC("0701");
                        break;
                    case "7PB"://Biliary Tract Procedure
                        if (B7Count > 0) {
                            drgResult.setDC("0702");
                        } else {
                            drgResult.setDC("0703");
                        }
                        break;
                    case "7PG"://Pancreas and Liver Procedure Except Resection
                        drgResult.setDC("0711");
                        break;
                    case "7PF"://Laparoscopic Cholecystectomy
                        if (Counter7PBX > 0) {
                            drgResult.setDC("0709");
                        } else {
                            drgResult.setDC("0710");
                        }
                        break;
                    case "7PC"://Cholecystectomy
                        if (Counter7PBX > 0) {
                            drgResult.setDC("0704");
                        } else {
                            drgResult.setDC("0705");
                        }
                        break;
                    case "7PD"://Hepatobiliary Diagnostic Procedures
                        drgResult.setDC("0706");
                        break;
                    case "7PE"://Other Hepatobiliary and Pancreas Procedures
                        drgResult.setDC("0707");
                        break;
                    case "7PH"://ERCP with Therapeutic Procedures 7PH
                        drgResult.setDC("0708");
                        break;

                }

            } else if (ORProcedureCounter > 0) {
                switch (Collections.max(ORProcedureCounterList)) {
                    case 1:
                        drgResult.setDC("2601");
                        break;
                    case 2:
                        drgResult.setDC("2602");
                        break;
                    case 3:
                        drgResult.setDC("2603");
                        break;
                    case 4:
                        drgResult.setDC("2604");
                        break;
                    case 5:
                        drgResult.setDC("2605");
                        break;
                    case 6:
                        drgResult.setDC("2606");
                        break;
                }

            } else {
                switch (drgResult.getPDC()) {
                    case "7B"://Malignancy of Hepatobiliary or Pancreas
                        //Radio+Chemotherapy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0756");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0757");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("0758");
                        } else if (Counter7PDX > 0) { //##Dx Procedure
                            drgResult.setDC("0759");
                            //Radiotherapy
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("0760");
                        } else {//Malignancy 
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("0761");
                            } else {
                                drgResult.setDC("0751");
                            }
                        }
                        break;
                    case "7A"://Cirrhosis and Alcoholic Hepatitis
                        drgResult.setDC("0750");
                        break;
                    case "7C"://Disorder of Pancreas, Except Malignancy
                        drgResult.setDC("0753");
                        break;
                    case "7D"://Disorder of Liver, Except Malignancy, Cirrhosis, Alcoholic Hepatitis
                        drgResult.setDC("0754");
                        break;
                    case "7E"://Disorder of Biliary Tract 7E
                        drgResult.setDC("0755");
                        break;
                }
            }

            if (drgResult.getDRG() == null) {

                //-------------------------------------------------------------------------------------
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                } else {
                    //----------------------------------------------------------------------
                    //  String sdxfinalList = gm.CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    String sdxfinalList = gm.CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = gm.GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        String drgValue = finaldrgresult.getDRG();
                        DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgValue);
                        //-----------------------------------------------------------------------
                        if (drgname.isSuccess()) {
                            drgResult.setDRG(drgValue);
                            drgResult.setDRGName(drgname.getMessage());
                        } else {
                            DRGWSResult drgvalues = gm.ValidatePCCL(datasource, drgResult.getDC(), drgValue);
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                DRGWSResult drgnames = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                drgResult.setDRGName(drgnames.getMessage());
                            } else {
                                drgResult.setDRG(drgValue);
                                drgResult.setDRGName("Grouper Error");
                            }
                        }
                    } else {
                        drgResult.setDRG("00000");
                        drgResult.setDRGName("Grouper Error");
                    }
                }
                result.setSuccess(true);
                //----------------------------------------------------------------------
            } else {
                result.setSuccess(true);
                DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                if (drgname.isSuccess()) {
                    drgResult.setDRGName(drgname.getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }
            result.setMessage("MDC 07 Done Checking");
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC07.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;

    }

}
