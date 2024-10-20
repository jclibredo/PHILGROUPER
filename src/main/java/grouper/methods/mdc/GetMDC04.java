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
 * @author MinoSun
 */
@RequestScoped
public class GetMDC04 {

    public GetMDC04() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC04(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            GrouperMethod gm = new GrouperMethod();
            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            int mdcprocedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PCX4Proc = 0;
            int PBX99Proc = 0;
            int Counter4BX = 0;
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
                //AX 99PDX Checking
                DRGWSResult Result99PDX = gm.AX(datasource, "99PDX", proc.trim());
                if (Result99PDX.isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                DRGWSResult Result99PCX = gm.AX(datasource, "99PCX", proc.trim());
                if (Result99PCX.isSuccess()) {
                    PCXCounter99++;
                }
                DRGWSResult Result99PEX = gm.AX(datasource, "99PEX", proc.trim());
                if (Result99PEX.isSuccess()) {
                    CartProc++;
                }
                DRGWSResult Result99PFX = gm.AX(datasource, "99PFX", proc.trim());
                if (Result99PFX.isSuccess()) {
                    CaCRxProc++;
                }

                //AX 4PCX
                DRGWSResult Result4PCX = gm.AX(datasource, "4PCX", proc.trim());
                if (Result4PCX.isSuccess()) {
                    PCX4Proc++;
                }
                DRGWSResult Result99PBX = gm.AX(datasource, "99PBX", proc.trim());
                if (Result99PBX.isSuccess()) {
                    PBX99Proc++;
                }

                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc.trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }

            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                String Secon = SecondaryList.get(a);
                DRGWSResult Result99BX = gm.AX(datasource, "99BX", Secon.trim());
                if (Result99BX.isSuccess()) {
                    CartSDx++;
                }
                DRGWSResult Result99CX = gm.AX(datasource, "99CX", Secon.trim());
                if (Result99CX.isSuccess()) {
                    CaCRxSDx++;
                }
            }
            DRGWSResult Result4BX = gm.AX(datasource, "4PBX", grouperparameter.getPdx().trim());
            if (Result4BX.isSuccess()) {
                Counter4BX++;
            }

            //THIS AREA START FOR CONDITIONAL STATEMENT TO FIND DC
            if (PDXCounter99 > 0 || Counter4BX > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
                    if (PCXCounter99 > 0) { //Procedures for upper airway obstruction
                        drgResult.setDC("0405");
                    } else {
                        drgResult.setDC("0406");
                    }

                } else if (mdcprocedureCounter > 0) { //THIS AREA MDC PROCEDURE
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
                        case "4PA"://Major Chest
                            drgResult.setDC("0401");
                            break;
                        case "4PB"://Other Respiratory System Procedures
                            drgResult.setDC("0402");
                            break;
                        case "4PD"://Ventilator Support
                            drgResult.setDC("0403");
                            break;
                        case "4PE"://Noninvasive Ventilation 
                            drgResult.setDC("0407");
                            break;
                        case "4PC"://Other Minor Respiratory System Procedures PDC 4PC
                            drgResult.setDC("0408");
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
                        case "4A"://Cystic Fibrosis
                            drgResult.setDC("0450");
                            break;
                        case "4B"://Pulmonary Embolism
                            drgResult.setDC("0451");
                            break;
                        case "4C"://Respiratory Infection/Inflammation
                            drgResult.setDC("0452");
                            break;
                        case "4D"://Sleep Apnea
                            drgResult.setDC("0453");
                            break;

                        case "4E"://Noninvasive Ventilation 
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("0471");//Transfer
                            } else {
                                drgResult.setDC("0454");//Others
                            }
                            break;
                        case "4F"://COPD
                            drgResult.setDC("0455");
                            break;
                        case "4G"://Major Chest Trauma
                            drgResult.setDC("0456");
                            break;
                        case "4H"://Respiratory Signs and Symptoms 
                            drgResult.setDC("0457");
                            break;
                        case "4J"://Pneumothorax
                            drgResult.setDC("0458");
                            break;
                        case "4K"://Bronchitis and Asthma
                            drgResult.setDC("0459");
                            break;
                        case "4L"://Whooping Cough and Acute Bronchiolitis
                            drgResult.setDC("0460");
                            break;
                        case "4M"://Respiratory Neoplasms
                            //Radio+Chemotherapy
                            if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                drgResult.setDC("0465");
                                //Chemotherapy
                            } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                drgResult.setDC("0466");
                                //Radiotherapy
                            } else if (CartSDx > 0 && CartProc > 0) {
                                drgResult.setDC("0467");
                            } else if (PCX4Proc > 0) { //##Dx Procedure
                                drgResult.setDC("0468");
                                //Radiotherapy
                            } else if (PBX99Proc > 0) {//Blood Transfusion
                                drgResult.setDC("0469");
                            } else {
                                drgResult.setDC("0461");
                            }
                            break;
                        case "4R"://Pyothorax 
                            drgResult.setDC("0470");
                            break;
                        case "4N"://Pleural Effusion
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("0472");//Transfer
                            } else {
                                drgResult.setDC("0462");//Others
                            }
                            break;
                        case "4P"://Interstitial Lung Diseases
                            drgResult.setDC("0463");
                            break;
                        case "4Q"://Other Minor Respiratory System Diagnosis PDC 4Q
                            drgResult.setDC("0464");
                            break;
                    }
                }

            } else if (mdcprocedureCounter > 0) { //THIS AREA MDC PROCEDURE
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
                    case "4PA"://Major Chest
                        drgResult.setDC("0401");
                        break;
                    case "4PB"://Other Respiratory System Procedures
                        drgResult.setDC("0402");
                        break;
                    case "4PD"://Ventilator Support
                        drgResult.setDC("0403");
                        break;
                    case "4PE"://Noninvasive Ventilation 
                        drgResult.setDC("0407");
                        break;
                    case "4PC"://Other Minor Respiratory System Procedures
                        drgResult.setDC("0408");
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
                    case "4A"://Cystic Fibrosis
                        drgResult.setDC("0450");
                        break;
                    case "4B"://Pulmonary Embolism
                        drgResult.setDC("0451");
                        break;
                    case "4C"://Respiratory Infection/Inflammation
                        drgResult.setDC("0452");
                        break;
                    case "4D"://Sleep Apnea
                        drgResult.setDC("0453");
                        break;

                    case "4E"://Noninvasive Ventilation 
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("0471");//Transfer
                        } else {
                            drgResult.setDC("0454");//Others
                        }
                        break;
                    case "4F"://COPD
                        drgResult.setDC("0455");
                        break;
                    case "4G"://Major Chest Trauma
                        drgResult.setDC("0456");
                        break;
                    case "4H"://Respiratory Signs and Symptoms 
                        drgResult.setDC("0457");
                        break;
                    case "4J"://Pneumothorax
                        drgResult.setDC("0458");
                        break;
                    case "4K"://Bronchitis and Asthma
                        drgResult.setDC("0459");
                        break;
                    case "4L"://Whooping Cough and Acute Bronchiolitis
                        drgResult.setDC("0460");
                        break;
                    case "4M"://Respiratory Neoplasms
                        //Radio+Chemotherapy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0465");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0466");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("0467");
                        } else if (PCX4Proc > 0) { //##Dx Procedure
                            drgResult.setDC("0468");
                            //Radiotherapy
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("0469");
                        } else {
                            drgResult.setDC("0461");
                        }
                        break;
                    case "4R"://Pyothorax 
                        drgResult.setDC("0470");
                        break;
                    case "4N"://Pleural Effusion
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("0472");//Transfer
                        } else {
                            drgResult.setDC("0462");//Others
                        }
                        break;
                    case "4P"://Interstitial Lung Diseases
                        drgResult.setDC("0463");
                        break;
                    case "4Q"://Other Minor Respiratory System Diagnosis PDC 4Q
                        drgResult.setDC("0464");
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
                    drgResult.setDRGName("No DRG Name Found");
                }
            }
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 04 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC04.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
}
