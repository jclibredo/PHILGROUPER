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
public class GetMDC03 {

    public GetMDC03() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC03.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC03(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            AX checkAX = new AX();
            int Counter3PDX = 0;
            int Counter3PBX = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PCX3Proc = 0;
            int PBX99Proc = 0;
            int Counter3PEX = 0;
            int Counter3BX = 0;
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                CartSDx += checkAX.AX(datasource, SchemaName, "99BX", sdxCode).isSuccess() ? 1 : 0;
                CaCRxSDx += checkAX.AX(datasource, SchemaName, "99CX", sdxCode).isSuccess() ? 1 : 0;
            }
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procS = ProcedureList.get(y).trim();
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        procS,
                        mdcWithoutZeros, grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource,
                            SchemaName,
                            mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                Counter3PBX += checkAX.AX(datasource, SchemaName, "3PBX", procS).isSuccess() ? 1 : 0;
                Counter3PEX += checkAX.AX(datasource, SchemaName, "3PEX", procS).isSuccess() ? 1 : 0;
                CartProc += checkAX.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += checkAX.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PCX3Proc += checkAX.AX(datasource, SchemaName, "3PCX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += checkAX.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                Counter3PDX += checkAX.AX(datasource, SchemaName, "3PDX", procS).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
            }
            Counter3BX += checkAX.AX(datasource, SchemaName, "3BX", grouperparameter.getPdx().toUpperCase().trim()).isSuccess() ? 1 : 0;
            if (PDXCounter99 > 0) {
                if (Counter3BX > 0) {
                    drgResult.setDC(Counter3PDX > 0 ? "0322" : "0323");
                } else {
                    long los = utility.ComputeLOS(
                            grouperparameter.getAdmissionDate(),
                            utility.Convert24to12(grouperparameter.getTimeAdmission()),
                            grouperparameter.getDischargeDate(),
                            utility.Convert24to12(grouperparameter.getTimeDischarge())
                    );
                    if (los > 21) {
                        drgResult.setDC(PCXCounter99 > 0 ? "0318" : "0319");
                    } else if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        drgResult.setDC(this.mdcProcedure(drgResult.getPDC(), Counter3PEX));
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                    } else {
                        String dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                PCX3Proc,
                                PBX99Proc,
                                Counter3PBX);
                        drgResult.setDC(dc);
                    }
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setDC(this.mdcProcedure(drgResult.getPDC(), Counter3PEX));
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        PCX3Proc,
                        PBX99Proc,
                        Counter3PBX);
                drgResult.setDC(dc);
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 03 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC3 Method");
            logger.error("Error in MDC3 Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    private String mdcProcedure(
            final String pdc,
            final Integer Counter3PEX) {
        String result = "";
        switch (pdc) {
            case "3PT": {   //Laryngectomy
                result = "0320";
                break;
            }
            case "3PA": {
                result = (Counter3PEX > 0) ? "0321" : "0301";
                break;
            }
            case "3PR": {//Major Sinus Procedures
                result = "0316";
                break;
            }
            case "3PC": {//Maxillo Surgery
                result = "0303";
                break;
            }
            case "3PQ": {//Mastoidectomy & Inner Ear Procedures
                result = "0315";
                break;
            }
            case "3PP": {//Minor Head and Neck Procedures
                result = "0314";
                break;
            }
            case "3PD": {//Salivary Procedures
                result = "0304";
                break;
            }
            case "3PL": {//Other Head and Neck Procedures
                result = "0310";
                break;
            }
            case "3PE": {//Minor Nose & Sinus Procedures
                result = "0305";
                break;
            }
            case "3PJ": {//Rhinoplasty
                result = "0308";
                break;
            }
            case "3PN": {//Pharyngeal & Laryngeal Procedures
                result = "0313";
                break;
            }
            case "3PB": {//Cleft Lip and Palate Repair
                result = "0302";
                break;
            }
            case "3PH": {//Miscellaneous Ear, Nose, Mouth and Throat Procedures
                result = "0307";
                break;
            }
            case "3PG": {//Mouth Procedures
                result = "0306";
                break;
            }
            case "3PK": {//Tonsil and/or Adenoidectomy
                result = "0309";
                break;
            }
            case "3PS": {//Tympanoplasty & Other Ear Procedures
                result = "0317";
                break;
            }
            case "3PM": {//Myringotomy with Tube Insertion 3PM
                result = "0311";
                break;
            }
        }

        return result;
    }

    private String orProcedure(final Integer ORProcedureCounterList) {
        String dc = "";
        switch (ORProcedureCounterList) {
            case 1: {
                dc = "2601";
                break;
            }
            case 2: {
                dc = "2602";
                break;
            }
            case 3: {
                dc = "2603";
                break;
            }
            case 4: {
                dc = "2604";
                break;
            }
            case 5: {
                dc = "2605";
                break;
            }
            case 6: {
                dc = "2606";
                break;
            }
        }
        return dc;
    }

    private String principalDaignosis(
            final String pdc,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer PCX3Proc,
            final Integer PBX99Proc,
            final Integer Counter3PBX) {
        String dc = "";
        switch (pdc) {
            case "3A": {
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "0358";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "0359";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "0360";
                } else if (PCX3Proc > 0) { //##Dx Procedure
                    dc = "0361";
                    //Radiotherapy
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "0362";
                } else {
                    dc = "0350";
                }
                break;
            }
            case "3B": {//Dysequilibrium
                dc = "0351";
                break;
            }
            case "3C": {//Epistaxis
                dc = "0352";
                break;
            }
            case "3D": {//Otitis Media and Upper Respiratory Infection
                dc = "0353";
                break;
            }
            case "3E": {//Epiglottitis and Cellulitis of Face-Neck
                dc = "0354";
                break;
            }
            case "3F": {//Nasal Trauma and Deformity
                dc = "0355";
                break;
            }
            case "3G": {//Other Ear, Noes, Mouth and Throat Diagnoses
                dc = "0356";
                break;
            }
            case "3H": {//Dental and Oral 3H
                dc = (Counter3PBX > 0) ? "0312" : "0357";//Extraction and Restoration
                break;
            }
        }
        return dc;

    }

}
