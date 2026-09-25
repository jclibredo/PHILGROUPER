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
import grouper.structures.MDCCodeOptimize;
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
public class GetMDC08 {

    public GetMDC08() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC08.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC08(
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
            GetPDC getPdc = new GetPDC();
            Endovasc endO = new Endovasc();
            MDCProcedureMethod mdcProc = new MDCProcedureMethod();
            ORProcedure orProc = new ORProcedure();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter8PFX = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int Counter8PH = 0;
            int Counter8QA = 0;
            long age = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
            for (int a = 0; a < SecondaryList.size(); a++) {
                String Secon = SecondaryList.get(a);
                CartSDx += checkAX.AX(datasource, SchemaName, "99BX", Secon.trim()).isSuccess() ? 1 : 0;
                CaCRxSDx += checkAX.AX(datasource, SchemaName, "99CX", Secon.trim()).isSuccess() ? 1 : 0;
            }
            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procS = ProcedureList.get(y).trim();
                DRGWSResult ORProcedureResult = orProc.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult JoinResult = mdcProc.MDCProcedure(datasource,
                        SchemaName,
                        procS,
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = getPdc.GetPDC(datasource,
                            SchemaName,
                            mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                Counter8PH += endO.Endovasc(datasource, SchemaName, procS, "8PH", mdcWithoutZeros).isSuccess() ? 1 : 0;
                Counter8QA += endO.Endovasc(datasource, SchemaName, procS, "8QA", mdcWithoutZeros).isSuccess() ? 1 : 0;
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                CartProc += checkAX.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += checkAX.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += checkAX.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                Counter8PFX += checkAX.AX(datasource, SchemaName, "8PFX", procS).isSuccess() ? 1 : 0;
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        drgResult.setDC(this.mdcProcedure(
                                drgResult.getPDC(),
                                Counter8PH,
                                age,
                                Counter8QA));
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                    } else {
                        String dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                Counter8PFX,
                                PBX99Proc,
                                age);
                        drgResult.setDC(dc);
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "0833" : "0834");
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                String getMdcProc = this.mdcProcedure(
                        drgResult.getPDC(),
                        Counter8PH,
                        age,
                        Counter8QA);
                drgResult.setDC(getMdcProc);
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter8PFX,
                        PBX99Proc,
                        age);
                drgResult.setDC(dc);
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 08 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC8 Method");
            logger.error("Error in MDC8 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    private String mdcProcedure(
            final String pdc,
            final Integer Counter8PH,
            final Long age,
            final Integer Counter8QA) {
        String result = "";
        //CHECKING FOR TRAUMA CODES
        switch (pdc) {
            case "8QE": {//Plasmapheresis
                result = "0835";
                break;
            }
            case "8QF": {//Multiple (>4) Wound Debridement
                result = "0836";
                break;
            }
            case "8QD": {
                result = "0801";
                break;
            }
            case "8QB": {//Multiple (2-4) Wound Debridement
                result = Counter8PH > 0 ? "0828" : "0830";
                break;
            }
            case "8PD": {//Spinal Fusion
                result = "0805";
                break;
            }
            case "8PW": {//Total Hip Revision
                result = "0824";
                break;
            }
            case "8PF": {//Amputation for Musculoskeletal & Connective Tissue Disorders
                result = "0807";
                break;
            }
            case "8PZ": {//Partial Hip Revision
                result = "0827";
                break;
            }
            case "8PX": {//Total Knee Revision
                result = "0825";
                break;
            }
            case "8PY": {//Partial Knee Revision
                result = "0826";
                break;
            }
            case "8PA": {//Hip Replacement
                result = "0802";
                break;
            }
            case "8PV": {//Partial Hip Replacement
                result = "0823";
                break;
            }
            case "8PE": {//Back & Neck Procedure Except Spinal Fusion
                result = "0806";
                break;
            }
            case "8PB": {//Knee Replacement
                result = "0803";
                break;
            }
            case "8PC": {//Other Major Joint Replacement & Limb Reattach of Lower/Upper Extremities
                result = "0804";
                break;
            }
            case "8PG": {//Biopsies of Musculoskeletal & Connective Tissue Disorders
                result = "0808";
                break;
            }
            case "8PJ": {//Hip and Femur Procedures Except Replacement
                result = age > 17 ? "0810" : "0811";
                break;
            }
            case "8PH": {//Skin Graft Except Hand for MS&CT
                result = Counter8QA > 0 ? "0829" : "0809";
                break;
            }
            case "8PK": {//Knee Procedures Except Replacement
                result = "0812";
                break;
            }
            case "8PU": {//Other Musculoskeletal System and Connective Tissue OR Procedures
                result = "0822";
                break;
            }
            case "8QA": {//Wound Debridement for MS&CT
                result = "0831";
                break;
            }
            case "8PT": {//Arthroscopy
                result = "0821";
                break;
            }
            case "8PL": {//Shoulder, Elbow & Forearm Procedures Except Replacement
                result = "0813";
                break;
            }
            case "8PM": {//Humerus, Tibia, Fibula & Ankle Procedures Except Replacement
                result = age > 17 ? "0814" : "0815";
                break;
            }
            case "8QC": {//Reattachment of Finger
                result = "0832";
                break;
            }
            case "8PS": {//Soft Tissue Procedures
                result = "0820";
                break;
            }
            case "8PQ": {//Local Excision & Removal of Internal Fixation Devices of Hip & Femur
                result = "0818";
                break;
            }
            case "8PP": {//Foot Procedures
                result = "0817";
                break;
            }
            case "8PR": {//Local Excision & Removal of Internal Fixation Devices Exc Hip & Femur
                result = "0819";
                break;
            }
            case "8PN": {//Wrist & Hand Procedures Except Replacement PDC 8PN 
                result = "0816";
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
            final Integer Counter8PFX,
            final Integer PBX99Proc,
            final Long age) {
        String dc = "";
        switch (pdc) {
            case "8E": {//Pathological Fracture and Malignancy
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "0868";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "0869";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "0870";
                } else if (Counter8PFX > 0) { //##Dx Procedure
                    dc = "0871";
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "0872";
                } else {//Malignancy 
                    dc = "0854";
                }
                break;
            }
            case "8A": {//Fracture of Femur
                dc = "0850";
                break;
            }
            case "8B": {//Fracture of Hip and Pelvis
                dc = "0851";
                break;
            }
            case "8C": {//Sprain, Strain and Dislocation of Hip, Pelvis and Thigh
                dc = "0852";
                break;
            }
            case "8D": {//Osteomyelitis
                dc = "0853";
                break;
            }
            case "8F": {//Connective Tissue
                dc = "0855";
                break;
            }
            case "8G": {//Septic Arthritis
                dc = "0856";
                break;
            }
            case "8H": {//Medical Back Problems
                dc = "0857";
                break;
            }
            case "8J": {//Bone Disease and Specific Arthropathies
                dc = "0858";
                break;
            }
            case "8K": {//Nonspecific Arthropathies
                dc = "0859";
                break;
            }
            case "8L": {//Signs and Symptoms
                dc = "0860";
                break;
            }
            case "8M": {//Tendonitis, Myositis and Bursitis
                dc = "0861";
                break;
            }
            case "8N": {//Aftercare
                dc = "0862";
                break;
            }
            case "8P": {//Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                dc = (age > 17 ? "0863" : "0864");
                break;
            }
            case "8Q": {//Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                dc = (age > 17 ? "0865" : "0866");
                break;
            }
            case "8R": {//Other Musculoskeletal System and Connective Tissue Diagnoses
                dc = "0867";
                break;
            }
            case "8S": {//Major Connective Tissue Dx PDC 8S
                dc = "0873";
                break;
            }
        }
        return dc;

    }

}
