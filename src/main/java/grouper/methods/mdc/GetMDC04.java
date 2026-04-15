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
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC04 {

    public GetMDC04() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC04.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC04(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            AX checkAX = new AX();
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
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, ProcedureList.get(y).trim(), drgResult.getMDC());
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
                if (checkAX.AX(datasource, "99PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, "99PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                if (checkAX.AX(datasource, "99PEX", ProcedureList.get(y).trim()).isSuccess()) {
                    CartProc++;
                }
                if (checkAX.AX(datasource, "99PFX", ProcedureList.get(y).trim()).isSuccess()) {
                    CaCRxProc++;
                }

                //AX 4PCX
                if (checkAX.AX(datasource, "4PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    PCX4Proc++;
                }
                if (checkAX.AX(datasource, "99PBX", ProcedureList.get(y).trim()).isSuccess()) {
                    PBX99Proc++;
                }

                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }

            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (checkAX.AX(datasource, "99BX", SecondaryList.get(a).trim()).isSuccess()) {
                    CartSDx++;
                }
                if (checkAX.AX(datasource, "99CX", SecondaryList.get(a).trim()).isSuccess()) {
                    CaCRxSDx++;
                }
            }
            if (checkAX.AX(datasource, "4PBX", grouperparameter.getPdx().trim()).isSuccess()) {
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
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 04 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC4 Method");
            logger.error("Error in MDC4 Method : {}", ex.getMessage(), ex);
        }
        return result;
    }
}
