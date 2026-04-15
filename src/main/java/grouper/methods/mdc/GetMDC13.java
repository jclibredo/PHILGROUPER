/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.GetPDC;
import grouper.methods.validation.GetPDCUsePDx;
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
public class GetMDC13 {

    public GetMDC13() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC13.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC13(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
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
            int Counter13PBX = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                if (utility.isValid99PEX(ProcedureList.get(x).trim())) {
                    CartProc++;
                }
                if (utility.isValid99PFX(ProcedureList.get(x).trim())) {
                    CaCRxProc++;
                }
                //AX 99PDX Checking
                if (utility.isValid99PDX(ProcedureList.get(x).trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(x).trim())) {
                    PCXCounter99++;
                }
                //AX 13PBX Checking
                if (checkAX.AX(datasource, "13PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter13PBX++;
                }

                //THIS AREA IS FOR CHECKING OF OR PROCEDURE
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, ProcedureList.get(x).trim(), drgResult.getMDC());
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
            }

            for (int a = 0; a < SecondaryList.size(); a++) {
                if (utility.isValid99BX(SecondaryList.get(a).trim())) {
                    CartSDx++;
                }
                if (utility.isValid99CX(SecondaryList.get(a).trim())) {
                    CaCRxSDx++;
                }
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (mdcprocedureCounter > 0) {
                        String PDxPDC = new GetPDCUsePDx().GetPDCUsePDx(datasource, grouperparameter.getPdx());
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
                            case "13PJ"://Pelvic Evisceration
                                drgResult.setDC("1312");
                                break;
                            case "13PA"://Radical Hysterectomy and Radical Vulvectomy
                                drgResult.setDC("1301");
                                break;
                            case "13PK": //Lap Uterine and Adnexal
                                switch (PDxPDC) {
                                    case "13A"://Other Malignancy
                                        drgResult.setDC("1313");
                                        break;
                                    case "13B"://CA in situ
                                        drgResult.setDC("1314");
                                        break;
                                    case "13C"://Ovarian and Adnexal Malignancy
                                        drgResult.setDC("1315");
                                        break;
                                    default://Non- Malignancy
                                        drgResult.setDC("1316");
                                        break;
                                }
                                break;
                            case "13PB": //Uterine and Adnexal
                                switch (PDxPDC) {
                                    case "13A"://Other Malignancy
                                        drgResult.setDC("1302");
                                        break;
                                    case "13B": //CA in situ
                                        drgResult.setDC("1303");
                                        break;
                                    case "13C": //Ovarian and Adnexal Malignancy
                                        drgResult.setDC("1304");
                                        break;
                                    default://Non- Malignancy
                                        drgResult.setDC("1305");
                                        break;
                                }
                                break;
                            case "13PH"://Other Female Reproductive System OR Procedures
                                drgResult.setDC("1311");
                                break;
                            case "13PC"://Female Reproductive System Reconstructive Procedures
                                drgResult.setDC("1308");
                                break;
                            case "13PF"://Endoscopic Tubal Interruption
                                drgResult.setDC("1310");
                                break;
                            case "13PD"://Vagina, Cervix and Vulva Procedures
                                drgResult.setDC("1307");
                                break;
                            case "13PE"://Incisional Tubal Interruption
                                drgResult.setDC("1306");
                                break;
                            case "13PG"://D&C 13PG
                                drgResult.setDC("1309");
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
                            //Radio+Chemotherapy
                            case "13A"://Admit for Renal Dialysis
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {  //Chemotherapy
                                    drgResult.setDC("1356");
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {  //Radiotherapy
                                    drgResult.setDC("1357");
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("1358");
                                } else if (Counter13PBX > 0) { //##Dx Procedure
                                    drgResult.setDC("1359");
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("1360");
                                } else {//Malignancy 
                                    drgResult.setDC("1350");
                                }
                                break;
                            case "13B"://Non Ovarian/Adnexal CA in situ
                                drgResult.setDC("1351");
                                break;
                            case "13C"://Ovarian/Adnexal Malignancy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {  //Chemotherapy
                                    drgResult.setDC("1361");
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) { //Radiotherapy
                                    drgResult.setDC("1362");
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("1363");
                                } else if (Counter13PBX > 0) { //##Dx Procedure
                                    drgResult.setDC("1364");
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("1365");
                                } else {//Malignancy 
                                    drgResult.setDC("1352");
                                }
                                break;
                            case "13D"://Lower Genitourinary Tract Infection
                                drgResult.setDC("1353");
                                break;
                            case "13E"://Female Pelvic Infection
                                drgResult.setDC("1354");
                                break;
                            case "13F"://Menstrual and Other Female Reproductive System Disorders PDC 13F
                                drgResult.setDC("1355");
                                break;
                        }
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("1318");
                    } else {
                        drgResult.setDC("1319");
                    }
                }

            } else if (mdcprocedureCounter > 0) {
                String PDxPDC = new GetPDCUsePDx().GetPDCUsePDx(datasource, grouperparameter.getPdx());
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
                    case "13PJ"://Pelvic Evisceration
                        drgResult.setDC("1312");
                        break;
                    case "13PA"://Radical Hysterectomy and Radical Vulvectomy
                        drgResult.setDC("1301");
                        break;
                    case "13PK": //Lap Uterine and Adnexal
                        switch (PDxPDC) {
                            case "13A": //Other Malignancy
                                drgResult.setDC("1313");
                                break;
                            case "13B":  //CA in situ
                                drgResult.setDC("1314");
                                break;
                            case "13C": //Ovarian and Adnexal Malignancy
                                drgResult.setDC("1315");
                                break;
                            default://Non- Malignancy
                                drgResult.setDC("1316");
                                break;
                        }
                        break;
                    case "13PB": //Uterine and Adnexal
                        switch (PDxPDC) {
                            case "13A": //Other Malignancy
                                drgResult.setDC("1302");
                                break;
                            case "13B":  //CA in situ
                                drgResult.setDC("1303");
                                break;
                            case "13C": //Ovarian and Adnexal Malignancy
                                drgResult.setDC("1304");
                                break;
                            default: //Non- Malignancy
                                drgResult.setDC("1305");
                                break;

                        }
                        break;
                    case "13PH"://Other Female Reproductive System OR Procedures
                        drgResult.setDC("1311");
                        break;
                    case "13PC"://Female Reproductive System Reconstructive Procedures
                        drgResult.setDC("1308");
                        break;
                    case "13PF"://Endoscopic Tubal Interruption
                        drgResult.setDC("1310");
                        break;
                    case "13PD"://Vagina, Cervix and Vulva Procedures
                        drgResult.setDC("1307");
                        break;
                    case "13PE"://Incisional Tubal Interruption
                        drgResult.setDC("1306");
                        break;
                    case "13PG"://D&C 13PG
                        drgResult.setDC("1309");
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
                    //Radio+Chemotherapy
                    case "13A"://Admit for Renal Dialysis
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("1356");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("1357");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("1358");
                        } else if (Counter13PBX > 0) { //##Dx Procedure
                            drgResult.setDC("1359");
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("1360");
                        } else {//Malignancy 
                            drgResult.setDC("1350");
                        }
                        break;
                    case "13B"://Non Ovarian/Adnexal CA in situ
                        drgResult.setDC("1351");
                        break;
                    case "13C"://Ovarian/Adnexal Malignancy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {        //Chemotherapy
                            drgResult.setDC("1361");
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) { //Radiotherapy
                            drgResult.setDC("1362");
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("1363");
                        } else if (Counter13PBX > 0) { //##Dx Procedure
                            drgResult.setDC("1364");
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("1365");
                        } else {//Malignancy 
                            drgResult.setDC("1352");
                        }
                        break;
                    case "13D"://Lower Genitourinary Tract Infection
                        drgResult.setDC("1353");
                        break;
                    case "13E"://Female Pelvic Infection
                        drgResult.setDC("1354");
                        break;
                    case "13F"://Menstrual and Other Female Reproductive System Disorders PDC 13F
                        drgResult.setDC("1355");
                        break;
                }
            }
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 13 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC13 Method");
            logger.error("Error in MDC13 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

}
