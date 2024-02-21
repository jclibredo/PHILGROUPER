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
import grouper.utility.DRGUtility;
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
    private final DRGUtility drgutility = new DRGUtility();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGWSResult GetMDC04(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);

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
            } else {

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
        //CHECKING FOR TRAUMA CODES
        int PDXCounter99 = 0;
        int PCXCounter99 = 0;
        for (int x = 0; x < ProcedureList.size(); x++) {
            String proc = ProcedureList.get(x);
            //AX 99PDX Checking
            if (drgutility.isValid99PDX(proc)) {
                PDXCounter99++;
            }
            //AX 99PCX Checking
            if (drgutility.isValid99PCX(proc)) {
                PCXCounter99++;
            }
        }
        //THIS AREA IS FOR CHECKING OF RADIO AND CHECMO
        int CartSDx = 0;
        int CaCRxSDx = 0;
        int CartProc = 0;
        int CaCRxProc = 0;
        int PCX4Proc = 0;
        int PBX99Proc = 0;
        //Checking SDx RadioTherapy and Chemotherapy
        for (int a = 0; a < SecondaryList.size(); a++) {
            String Secon = SecondaryList.get(a);
            if (drgutility.isValid99BX(Secon)) {
                CartSDx++;
            }
            if (drgutility.isValid99CX(Secon)) {
                CaCRxSDx++;
            }
        }

        //Checking Procedure RadioTherapy and Chemotherapy
        for (int a = 0; a < ProcedureList.size(); a++) {
            String Proce = ProcedureList.get(a);
            if (drgutility.isValid99PEX(Proce)) {
                CartProc++;
            }
            if (drgutility.isValid99PFX(Proce)) {
                CaCRxProc++;
            }
            if (drgutility.isValid4PCX(Proce)) {
                PCX4Proc++;
            }
            if (drgutility.isValid99PBX(Proce)) { //Blood Transfusion AX 99PBX
                PBX99Proc++;
            }
        }

        //THIS AREA START FOR CONDITIONAL STATEMENT TO FIND DC
        try {
            if (PDXCounter99 > 0 || drgutility.isValid4BX(grouperparameter.getPdx())) {
                if (drgutility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        drgutility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        drgutility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
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
                if (drgutility.isValidDCList(drgResult.getDC())) {
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
