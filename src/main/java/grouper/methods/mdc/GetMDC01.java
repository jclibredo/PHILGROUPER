/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.Endovasc;
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
public class GetMDC01 {

    public GetMDC01() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC01.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC01(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        int mdcAsInt = Integer.parseInt(drgResult.getMDC());
        String mdcWithoutZeros = String.valueOf(mdcAsInt);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            AX checkAX = new AX();
            // THIS AREA IS FOR CHECKING OF TRACHEOSTOMY AND CONT. MECH VENT
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (checkAX.AX(datasource, SchemaName, "99BX", SecondaryList.get(a).trim()).isSuccess()) {
                    CartSDx++;
                }
                if (checkAX.AX(datasource, SchemaName, "99CX", SecondaryList.get(a).trim()).isSuccess()) {
                    CaCRxSDx++;
                }
            }
            // CHECLING FOR ENDOVASC 
            int EndoCounter = 0;
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
//            String Pdcs = "1PJ";
            //Checking Procedure RadioTherapy and Chemotherapy
            int Counter1PBX = 0;
            for (int a = 0; a < ProcedureList.size(); a++) {
                //AX 99PDX Checking
                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(a).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(a).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                if (new Endovasc().Endovasc(datasource, SchemaName, ProcedureList.get(a).trim(), "1PJ", mdcWithoutZeros).isSuccess()) {
                    EndoCounter++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PEX", ProcedureList.get(a).trim()).isSuccess()) {
                    CartProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PFX", ProcedureList.get(a).trim()).isSuccess()) {
                    CaCRxProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PBX", ProcedureList.get(a).trim()).isSuccess()) {
                    PBX99Proc++;
                }
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, SchemaName, ProcedureList.get(a).trim(), mdcWithoutZeros, grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                DRGWSResult getOrProc = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(a).trim());
                if (getOrProc.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(getOrProc.getResult()));
                }
                if (checkAX.AX(datasource, SchemaName, "1PBX", ProcedureList.get(a).trim()).isSuccess()) {
                    Counter1PBX++;
                }
            }
            // THIS AREA WILL START STATEMENT TO FIND DC FOR MDC 1
            if (PDXCounter99 > 0) { //Check Procedure if Tracheostomy
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) > 21) {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0115");
                    } else {
                        drgResult.setDC("0116");
                    }
                } else if (mdcprocedureCounter > 0) { //MDC Procedure
                    int min = hierarvalue.get(0);
                    //Loop through the array  
                    for (int i = 0; i < hierarvalue.size(); i++) {
                        //Compare elements of array with min  
                        if (hierarvalue.get(i) < min) {
                            min = hierarvalue.get(i);
                        }
                    }
                    drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                    String getResult = this.mdcProcedure(
                            drgResult.getPDC(),
                            datasource,
                            SchemaName,
                            grouperparameter.getPdx(),
                            EndoCounter);
                    drgResult.setDC(getResult);
                } else if (ORProcedureCounter > 0) {
                    String orProc = this.orProcedure(Collections.max(ORProcedureCounterList));
                    drgResult.setDC(orProc);
                } else {
                    String dc = this.principalDaignosis(
                            drgResult.getPDC(),
                            CartSDx,
                            CaCRxSDx,
                            CartProc,
                            CaCRxProc,
                            Counter1PBX,
                            PBX99Proc,
                            grouperparameter.getDischargeType());
                    drgResult.setDC(dc);

                }
            } else if (mdcprocedureCounter > 0) { //MDC Procedure
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                String getResult = this.mdcProcedure(
                        drgResult.getPDC(),
                        datasource,
                        SchemaName,
                        grouperparameter.getPdx(),
                        EndoCounter);
                drgResult.setDC(getResult);
            } else if (ORProcedureCounter > 0) {
                String orProc = this.orProcedure(Collections.max(ORProcedureCounterList));
                drgResult.setDC(orProc);
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter1PBX,
                        PBX99Proc,
                        grouperparameter.getDischargeType());
                drgResult.setDC(dc);
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 1 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC1 Method");
            logger.error("Error in MDC1 Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    private String principalDaignosis(
            final String pdc,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer Counter1PBX,
            final Integer PBX99Proc,
            final String discharge) {
        String dc = "";
        switch (pdc) {
            case "1A":
                dc = "0150";
                break;
            case "1B":
                dc = "0151";
                break;
            case "1C":
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "0170";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "0171";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "0172";
                } else if (Counter1PBX > 0) { //##Dx Procedure
                    dc = "0173";
                    //Radiotherapy
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "0174";
                } else {
                    dc = "0152";
                }
                break;
            case "1D"://Degenerative Disorders
                dc = "0153";
                break;
            case "1E"://Multiple Sclerosis and Cerebellar Ataxia
                dc = "0154";
                break;
            case "1F"://Specific Cerebrovascular Disorders Except TIA
                if (discharge.equals("4")) {
                    dc = "0175";//Transfer
                } else {
                    dc = "0155";//Others
                }
                break;
            case "1G"://Transient Ischemic Attack and Precerebral Occlusions
                dc = "0156";
                break;
            case "1H"://Nonspecific Cerebrovascular Diseases
                dc = "0157";
                break;
            case "1J"://Cranial and Peripheral Nerve Disorders
                dc = "0158";
                break;
            case "1K"://Infections Except Viral Meningitis
                if (discharge.equals("4")) {
                    dc = "0176";
                } else {
                    dc = "0159";
                }
                break;
            case "1L"://Viral Meningitis
                dc = "0160";
                break;
            case "1M"://Nontraumatic Stupor and Coma
                dc = "0161";
                break;
            case "1N"://Febrile Convulsions
                dc = "0162";
                break;
            case "1P"://Seizure Disorders
                dc = "0163";
                break;
            case "1Q"://Headaches
                dc = "0164";
                break;
            case "1R"://Intracranial Injury
                dc = "0165";
                break;
            case "1S"://Skull Fractures
                dc = "0166";
                break;
            case "1T"://Other Head Injury
                dc = "0167";
                break;
            case "1U"://Other Disorders of Nervous System
                dc = "0168";
                break;
            case "1V"://Guillain-Barre Syndrome 1V
                dc = "0169";
                break;
        }
        return dc;
    }

    private String orProcedure(final Integer ORProcedureCounterList) {
        String dc = "";
        switch (ORProcedureCounterList) {
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

    private String mdcProcedure(
            final String pdc,
            final DataSource dataSource,
            final String SchemaName,
            final String pdx,
            final Integer EndoCounter) {
        String result = "";
        AX checkAX = new AX();
        switch (pdc) {
            case "1PK":   // drgResult.setDC("0116");
                if (checkAX.AX(dataSource, SchemaName, "1BX", pdx).isSuccess()) {
                    result = "0112";
                } else {
                    result = "0113";
                }
                break;
            case "1PL"://Plasmapheresis 
                result = "0117";
                break;
            case "1PH"://Intacranial Vasc
                if (EndoCounter > 0) {  // RECODE THIS AREA TO DOUBLE CHECK
                    result = "0110";
                } else {
                    if (checkAX.AX(dataSource, SchemaName, "1CX", pdx).isSuccess()) {
                        result = "0108";
                    } else {
                        result = "0109";
                    }
                }
                break;
            case "1PC"://SPINAL PROCEDURES
                result = "0103";
                break;
            case "1PB":  //Craniotomy
                if (checkAX.AX(dataSource, SchemaName, "1BX", pdx).isSuccess()) {
                    result = "0101";
                } else {
                    result = "0102";
                }
                break;
            case "1PJ": //Endovasc Procedures
                result = "0114";
                break;
            case "1PD": //Extracranial Vascular Procedures
                result = "0105";
                break;
            case "1PA": //Ventricular Shunt Revision
                result = "0104";
                break;
            case "1PF":
            case "1PG":  //Peripheral & Cranial Nerve & Other Nervous System Procedures
                result = "0106";
                break;
            case "1PE": //Carpal Tunnel Release
                result = "0107";
                break;
        }
        return result;
    }
}
