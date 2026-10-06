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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
            int ProcCounter8PCX = 0;
            int ProcCounter8PDX = 0;
            int ProcCounter8PEX = 0;
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
                ProcCounter8PDX += checkAX.AX(datasource, SchemaName, "8PDX", procS).isSuccess() ? 1 : 0;
                ProcCounter8PCX += checkAX.AX(datasource, SchemaName, "8PCX", procS).isSuccess() ? 1 : 0;
                ProcCounter8PEX += checkAX.AX(datasource, SchemaName, "8PEX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                CartProc += checkAX.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += checkAX.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += checkAX.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                Counter8PFX += checkAX.AX(datasource, SchemaName, "8PFX", procS).isSuccess() ? 1 : 0;
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07

            int max = Optional.ofNullable(ORProcedureCounterList)
                    .flatMap(list -> list.stream().filter(Objects::nonNull).max(Integer::compareTo))
                    .orElse(0);
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
                                Counter8QA,
                                ProcCounter8PCX,
                                ProcCounter8PDX,
                                ProcCounter8PEX));
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(max));
                    } else {
                        MDCCodeOptimize dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                Counter8PFX,
                                PBX99Proc,
                                age,
                                SecondaryList,
                                SchemaName,
                                datasource);
                        drgResult.setDC(dc.getDC());
                        if (!dc.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(dc.getSdxfinder());
                        }
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
                        Counter8QA,
                        ProcCounter8PCX,
                        ProcCounter8PDX,
                        ProcCounter8PEX);
                drgResult.setDC(getMdcProc);
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(max));
            } else {
                MDCCodeOptimize dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter8PFX,
                        PBX99Proc,
                        age,
                        SecondaryList,
                        SchemaName,
                        datasource);
                drgResult.setDC(dc.getDC());
                if (!dc.getSdxfinder().isEmpty()) {
                    drgResult.setSDXFINDER(dc.getSdxfinder());
                }
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
            final Integer Counter8QA,
            final Integer ProcCounter8PCX,
            final Integer ProcCounter8PDX,
            final Integer ProcCounter8PEX) {
        String result = "";
        // 1. Static 0801 procedure combinations check
        boolean isBilateralOrMultipleProc = "8QD".equals(pdc)
                || (ProcCounter8PCX > 0 && ProcCounter8PDX > 0)
                || (ProcCounter8PCX > 0 && ProcCounter8PEX > 0)
                || (ProcCounter8PDX > 0 && ProcCounter8PEX > 0)
                || ProcCounter8PDX > 1
                || ProcCounter8PCX > 1;
        if ("8QE".equals(pdc)) { // Plasmapheresis
            result = "0835";
        } else if ("8QF".equals(pdc)) { // Multiple (>4) Wound Debridement
            result = "0836";
        } else if (isBilateralOrMultipleProc) { // Bilateral / Multiple Joint / 8QD
            result = "0801";
        } else if ("8QB".equals(pdc)) { // Multiple (2-4) Wound Debridement
            result = Counter8PH > 0 ? "0828" : "0830";
        } else if ("8PD".equals(pdc)) { // Spinal Fusion
            result = "0805";
        } else if ("8PW".equals(pdc)) { // Total Hip Revision
            result = "0824";
        } else if ("8PF".equals(pdc)) { // Amputation for Musculoskeletal & Connective Tissue Disorders
            result = "0807";
        } else if ("8PZ".equals(pdc)) { // Partial Hip Revision
            result = "0827";
        } else if ("8PX".equals(pdc)) { // Total Knee Revision
            result = "0825";
        } else if ("8PY".equals(pdc)) { // Partial Knee Revision
            result = "0826";
        } else if ("8PA".equals(pdc)) { // Hip Replacement
            result = "0802";
        } else if ("8PV".equals(pdc)) { // Partial Hip Replacement
            result = "0823";
        } else if ("8PE".equals(pdc)) { // Back & Neck Procedure Except Spinal Fusion
            result = "0806";
        } else if ("8PB".equals(pdc)) { // Knee Replacement
            result = "0803";
        } else if ("8PC".equals(pdc)) { // Other Major Joint Replacement & Limb Reattach of Lower/Upper Extremities
            result = "0804";
        } else if ("8PG".equals(pdc)) { // Biopsies of Musculoskeletal & Connective Tissue Disorders
            result = "0808";
        } else if ("8PJ".equals(pdc)) { // Hip and Femur Procedures Except Replacement
            result = age > 17 ? "0810" : "0811";
        } else if ("8PH".equals(pdc)) { // Skin Graft Except Hand for MS&CT
            result = Counter8QA > 0 ? "0829" : "0809";
        } else if ("8PK".equals(pdc)) { // Knee Procedures Except Replacement
            result = "0812";
        } else if ("8PU".equals(pdc)) { // Other Musculoskeletal System and Connective Tissue OR Procedures
            result = "0822";
        } else if ("8QA".equals(pdc)) { // Wound Debridement for MS&CT
            result = "0831";
        } else if ("8PT".equals(pdc)) { // Arthroscopy
            result = "0821";
        } else if ("8PL".equals(pdc)) { // Shoulder, Elbow & Forearm Procedures Except Replacement
            result = "0813";
        } else if ("8PM".equals(pdc)) { // Humerus, Tibia, Fibula & Ankle Procedures Except Replacement
            result = age > 17 ? "0814" : "0815";
        } else if ("8QC".equals(pdc)) { // Reattachment of Finger
            result = "0832";
        } else if ("8PS".equals(pdc)) { // Soft Tissue Procedures
            result = "0820";
        } else if ("8PQ".equals(pdc)) { // Local Excision & Removal of Internal Fixation Devices of Hip & Femur
            result = "0818";
        } else if ("8PP".equals(pdc)) { // Foot Procedures
            result = "0817";
        } else if ("8PR".equals(pdc)) { // Local Excision & Removal of Internal Fixation Devices Exc Hip & Femur
            result = "0819";
        } else if ("8PN".equals(pdc)) { // Wrist & Hand Procedures Except Replacement PDC 8PN 
            result = "0816";
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

    private MDCCodeOptimize principalDaignosis(
            final String pdc,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer Counter8PFX,
            final Integer PBX99Proc,
            final Long age,
            final List<String> SecondaryList,
            final String SchemaName,
            final DataSource dataSource) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        switch (pdc) {
            case "8E": {//Pathological Fracture and Malignancy
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    result.setDC("0868");
                    for (String rawSdxCode : SecondaryList) {
                        if (rawSdxCode == null) {
                            continue;
                        }
                        String sdxCode = rawSdxCode.trim();
                        // Short-circuiting ensures 99CX is only evaluated if 99BX fails
                        if (checkAX.AX(dataSource, SchemaName, "99BX", sdxCode).isSuccess()
                                || checkAX.AX(dataSource, SchemaName, "99CX", sdxCode).isSuccess()) {
                            result.setSdxfinder(sdxCode);
                            break;
                        }
                    }
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    result.setDC("0869");
                    for (String rawSdxCode : SecondaryList) {
                        if (rawSdxCode == null) {
                            continue;
                        }
                        String sdxCode = rawSdxCode.trim();
                        // Short-circuiting ensures 99CX is only evaluated if 99BX fails
                        if (checkAX.AX(dataSource, SchemaName, "99CX", sdxCode).isSuccess()) {
                            result.setSdxfinder(sdxCode);
                            break;
                        }
                    }
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    result.setDC("0870");
                    for (String rawSdxCode : SecondaryList) {
                        if (rawSdxCode == null) {
                            continue;
                        }
                        String sdxCode = rawSdxCode.trim();
                        // Short-circuiting ensures 99CX is only evaluated if 99BX fails
                        if (checkAX.AX(dataSource, SchemaName, "99BX", sdxCode).isSuccess()) {
                            result.setSdxfinder(sdxCode);
                            break;
                        }
                    }
                } else if (Counter8PFX > 0) { //##Dx Procedure
                    result.setDC("0871");
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    result.setDC("0872");
                } else {//Malignancy 
                    result.setDC("0854");
                }
                break;
            }
            case "8A": {//Fracture of Femur
                result.setDC("0850");
                break;
            }
            case "8B": {//Fracture of Hip and Pelvis
                result.setDC("0851");
                break;
            }
            case "8C": {//Sprain, Strain and Dislocation of Hip, Pelvis and Thigh
                result.setDC("0852");
                break;
            }
            case "8D": {//Osteomyelitis
                result.setDC("0853");
                break;
            }
            case "8F": {//Connective Tissue
                result.setDC("0855");
                break;
            }
            case "8G": {//Septic Arthritis
                result.setDC("0856");
                break;
            }
            case "8H": {//Medical Back Problems
                result.setDC("0857");
                break;
            }
            case "8J": {//Bone Disease and Specific Arthropathies
                result.setDC("0858");
                break;
            }
            case "8K": {//Nonspecific Arthropathies
                result.setDC("0859");
                break;
            }
            case "8L": {//Signs and Symptoms
                result.setDC("0860");
                break;
            }
            case "8M": {//Tendonitis, Myositis and Bursitis
                result.setDC("0861");
                break;
            }
            case "8N": {//Aftercare
                result.setDC("0862");
                break;
            }
            case "8P": {//Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                result.setDC(age > 17 ? "0863" : "0864");
                break;
            }
            case "8Q": {//Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                result.setDC(age > 17 ? "0865" : "0866");
                break;
            }
            case "8R": {//Other Musculoskeletal System and Connective Tissue Diagnoses
                result.setDC("0867");
                break;
            }
            case "8S": {//Major Connective Tissue Dx PDC 8S
                result.setDC("0873");
                break;
            }
        }
        return result;

    }

}
