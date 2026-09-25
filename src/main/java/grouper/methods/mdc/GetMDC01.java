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
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            AX checkAX = new AX();
            Endovasc enDov = new Endovasc();
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int EndoCounter = 0;
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            int Counter1PBX = 0;
            MDCProcedureMethod mdcProc = new MDCProcedureMethod();
            GetPDC getPdc = new GetPDC();
            ORProcedure orProc = new ORProcedure();
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                CartSDx += checkAX.AX(datasource, SchemaName, "99BX", sdxCode).isSuccess() ? 1 : 0;
                CaCRxSDx += checkAX.AX(datasource, SchemaName, "99CX", sdxCode).isSuccess() ? 1 : 0;
            }
            for (int a = 0; a < ProcedureList.size(); a++) {
                String procS = ProcedureList.get(a).trim();
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                EndoCounter += enDov.Endovasc(datasource, SchemaName, procS, "1PJ", mdcWithoutZeros).isSuccess() ? 1 : 0;
                CartProc += checkAX.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += checkAX.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += checkAX.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                DRGWSResult JoinResult = mdcProc.MDCProcedure(datasource, SchemaName, procS, mdcWithoutZeros, grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = getPdc.GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                DRGWSResult getOrProc = orProc.ORProcedure(datasource, SchemaName, procS);
                if (getOrProc.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(getOrProc.getResult()));
                }
                Counter1PBX += checkAX.AX(datasource, SchemaName, "1PBX", procS).isSuccess() ? 1 : 0;
            }
            // THIS AREA WILL START STATEMENT TO FIND DC FOR MDC 1
            if (PDXCounter99 > 0) { //Check Procedure if Tracheostomy
                long los = utility.ComputeLOS(
                        grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())
                );
                if (los > 21) {
                    drgResult.setDC(PCXCounter99 > 0 ? "0115" : "0116");
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
                    drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
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
                System.out.println(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                String getResult = this.mdcProcedure(
                        drgResult.getPDC(),
                        datasource,
                        SchemaName,
                        grouperparameter.getPdx(),
                        EndoCounter);
                drgResult.setDC(getResult);
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
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
                dc = "4".equals(discharge) ? "0175" : "0155";
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
                dc = "4".equals(discharge) ? "0176" : "0159";
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
            case "1PK": {
                result = checkAX.AX(dataSource, SchemaName, "1BX", pdx).isSuccess() ? "0112" : "0113";
                break;
            }
            case "1PL": {//Plasmapheresis 
                result = "0117";
                break;
            }
            case "1PH": {//Intacranial Vasc
                result = (EndoCounter > 0) ? "0110" : (checkAX.AX(dataSource, SchemaName, "1CX", pdx).isSuccess() ? "0108" : "0109");
                break;
            }
            case "1PC": {//SPINAL PROCEDURES
                result = "0103";
                break;
            }
            case "1PB": {  //Craniotomy
                result = checkAX.AX(dataSource, SchemaName, "1BX", pdx).isSuccess() ? "0101" : "0102";
                break;
            }
            case "1PJ": { //Endovasc Procedures
                result = "0114";
                break;
            }
            case "1PD": { //Extracranial Vascular Procedures
                result = "0105";
                break;
            }
            case "1PA": { //Ventricular Shunt Revision
                result = "0104";
                break;
            }
            case "1PF":
            case "1PG": {  //Peripheral & Cranial Nerve & Other Nervous System Procedures
                result = "0106";
                break;
            }
            case "1PE": {//Carpal Tunnel Release
                result = "0107";
                break;
            }
        }
        return result;
    }
}
