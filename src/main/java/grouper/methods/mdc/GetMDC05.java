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
import grouper.methods.validation.PDxMalignancy;
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
public class GetMDC05 {

    public GetMDC05() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC05.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC05(
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
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter5CX = 0;
            int Counter5DXSDx = 0;
            int Counter5DXPDx = 0;
            int Counter5BX = 0;
            int ORProcedureCounter = 0;
            int Counter5PEX = 0;
            int Counter5PCX = 0;
            int Counter5PFX = 0;
            int Counter5PDX = 0;
            int Counter5PGX = 0;
            int Counter5PHX = 0;
            int AMICount = 0;
            int Counter5PJX = 0;
            int CardiacCount = 0;
            int PPCount = 0;
            int Counter5PBX = 0;
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            AX checkAX = new AX();
            //5BX USES PRIMARY CODES
            if (checkAX.AX(datasource, SchemaName, "5BX", grouperparameter.getPdx()).isSuccess()) {
                Counter5BX++;
            }
            for (int x = 0; x < SecondaryList.size(); x++) {
                String sdxS = SecondaryList.get(x).trim();
                if (checkAX.AX(datasource, SchemaName, "5CX", sdxS).isSuccess()) {
                    Counter5CX++;
                }
                if (checkAX.AX(datasource, SchemaName, "5DX", sdxS).isSuccess()) {
                    Counter5DXSDx++;
                }
            }
            if (checkAX.AX(datasource, SchemaName, "5DX", grouperparameter.getPdx()).isSuccess()) {
                Counter5DXPDx++;
            }
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        procS,
                        mdcWithoutZeros,
                        grouperparameter.getGender());
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
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //AX 99PDX Checking
                if (checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess()) {
                    PCXCounter99++;
                }
                //AX 5PEX
                if (checkAX.AX(datasource, SchemaName, "5PEX", procS).isSuccess()) {
                    Counter5PEX++;
                }
                //AX 5PCX
                if (checkAX.AX(datasource, SchemaName, "5PCX", procS).isSuccess()) {
                    Counter5PCX++;
                }
                //AX 5PFX
                if (checkAX.AX(datasource, SchemaName, "5PFX", procS).isSuccess()) {
                    Counter5PFX++;
                }
                //AX 5PDX
                if (checkAX.AX(datasource, SchemaName, "5PDX", procS).isSuccess()) {
                    Counter5PDX++;
                }
                //AX 5PGX
                if (checkAX.AX(datasource, SchemaName, "5PGX", procS).isSuccess()) {
                    Counter5PGX++;
                }
                //AX 5PHX
                if (checkAX.AX(datasource, SchemaName, "5PHX", procS).isSuccess()) {
                    Counter5PHX++;
                }
                if (checkAX.AX(datasource, SchemaName, "5PJX", procS).isSuccess()) {
                    Counter5PJX++;
                }
                //Cardiac Cath PDC 5PT
                if (new Endovasc().Endovasc(datasource, SchemaName, procS, "5PT", mdcWithoutZeros).isSuccess()) {
                    CardiacCount++;
                }
                //AX 5PBX
                if (checkAX.AX(datasource, SchemaName, "5PBX", procS).isSuccess()) {
                    Counter5PBX++;
                }
                if (new Endovasc().Endovasc(datasource, SchemaName, procS, "5PK", mdcWithoutZeros).isSuccess()) {
                    PPCount++;
                }
            }

            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "5A").isSuccess()) {
                AMICount++;
            }
            // THIS AREA WILL START THE CONDITIONAL STATEMENT FOR THIS MDC
            if (PDXCounter99 > 0) {
                long los = utility.ComputeLOS(
                        grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())
                );
                if (los < 21) {
                    if (AMICount > 0) {
                        MDCCodeOptimize getAMICount = this.pdxAMI(
                                Counter5PEX,
                                Counter5PCX,
                                PPCount,
                                Counter5PFX,
                                Counter5PDX,
                                Counter5PGX,
                                Counter5PHX,
                                Counter5CX,
                                SecondaryList,
                                datasource,
                                SchemaName,
                                grouperparameter.getDischargeType());
                        drgResult.setDC(getAMICount.getDC());
                        if (!getAMICount.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(getAMICount.getSdxfinder());
                        }
                    } else if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        MDCCodeOptimize getMDCProcedure = this.mdcProcedure(
                                drgResult.getPDC(),
                                SecondaryList,
                                datasource,
                                SchemaName,
                                Counter5PCX,
                                CardiacCount,
                                Counter5PBX,
                                Counter5PDX,
                                Counter5PJX,
                                Counter5BX,
                                Counter5DXPDx,
                                Counter5DXSDx
                        );
                        drgResult.setDC(getMDCProcedure.getDC());
                        if (!getMDCProcedure.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(getMDCProcedure.getSdxfinder());
                        }
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                    } else {
                        drgResult.setDC(this.principalDaignosis(drgResult.getPDC(), grouperparameter.getDischargeType()));
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "0534" : "0535");
                }

            } else if (AMICount > 0) {
                MDCCodeOptimize getAMICount = this.pdxAMI(
                        Counter5PEX,
                        Counter5PCX,
                        PPCount,
                        Counter5PFX,
                        Counter5PDX,
                        Counter5PGX,
                        Counter5PHX,
                        Counter5CX,
                        SecondaryList,
                        datasource,
                        SchemaName,
                        grouperparameter.getDischargeType());
                drgResult.setDC(getAMICount.getDC());
                if (!getAMICount.getSdxfinder().isEmpty()) {
                    drgResult.setSDXFINDER(getAMICount.getSdxfinder());
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                MDCCodeOptimize getMDCProcedure = this.mdcProcedure(
                        drgResult.getPDC(),
                        SecondaryList,
                        datasource,
                        SchemaName,
                        Counter5PCX,
                        CardiacCount,
                        Counter5PBX,
                        Counter5PDX,
                        Counter5PJX,
                        Counter5BX,
                        Counter5DXPDx,
                        Counter5DXSDx
                );
                drgResult.setDC(getMDCProcedure.getDC());
                if (!getMDCProcedure.getSdxfinder().isEmpty()) {
                    drgResult.setSDXFINDER(getMDCProcedure.getSdxfinder());
                }
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else {
                drgResult.setDC(this.principalDaignosis(drgResult.getPDC(), grouperparameter.getDischargeType()));

            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 05 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }

        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC5 Method");
            logger.error("Error in MDC5 Method : {}", ex.getMessage(), ex);
        }

        return result;
    }

    private MDCCodeOptimize pdxAMI(
            final Integer Counter5PEX,
            final Integer Counter5PCX,
            final Integer PPCount,
            final Integer Counter5PFX,
            final Integer Counter5PDX,
            final Integer Counter5PGX,
            final Integer Counter5PHX,
            final Integer Counter5CX,
            final List<String> SecondaryList,
            final DataSource datasource,
            final String SchemaName,
            final String discharge) {
        AX checkAX = new AX();
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setPDC("");
        result.setDC("");
        result.setSdxfinder("");
        if (Counter5PEX > 0) {
            result.setDC(Counter5PCX > 0 ? "0525" : "0526");
        } else if (PPCount > 0) {
            result.setDC("0510");
        } else if (Counter5PFX > 0) {
            result.setDC(Counter5PDX > 0 ? "0527" : "0528");
        } else if (Counter5PGX > 0) {
            result.setDC(Counter5PDX > 0 ? "0529" : "0530");
        } else if (Counter5PHX > 0) {
            result.setDC((Counter5CX > 0) ? "0550" : "0551");
            if (Counter5CX > 0) {
                for (String sdx : SecondaryList) {
                    DRGWSResult sdxfinderResult = checkAX.AX(datasource, SchemaName, "5CX", sdx.trim());
                    if (sdxfinderResult.isSuccess()) {
                        result.setSdxfinder(sdx.trim());
                        break;
                    }
                }
            }
        } else if (discharge.equals("4")) {
            result.setDC("0569");
        } else {
            result.setDC((Counter5CX > 0) ? "0552" : "0553");
            if (Counter5CX > 0) {
                for (String sdx : SecondaryList) {
                    DRGWSResult sdxfinderResult = checkAX.AX(datasource, SchemaName, "5CX", sdx.trim());
                    if (sdxfinderResult.isSuccess()) {
                        result.setSdxfinder(sdx.trim());
                        break;
                    }
                }
            }
        }
        return result;
    }

    private MDCCodeOptimize mdcProcedure(
            final String pdc,
            final List<String> SecondaryList,
            final DataSource datasource,
            final String SchemaName,
            final Integer Counter5PCX,
            final Integer CardiacCount,
            final Integer Counter5PBX,
            final Integer Counter5PDX,
            final Integer Counter5PJX,
            final Integer Counter5BX,
            final Integer Counter5DXPDx,
            final Integer Counter5DXSDx) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setPDC("");
        result.setDC("");
        result.setSdxfinder("");
        //CHECKING FOR TRAUMA CODES
        AX checkAX = new AX();
        switch (pdc) {
            case "5PE": {//Thoracoabdominal Procedures Combination
                result.setDC("0507");
                break;
            }
            case "5PC": {//Coronary Bypass
                result.setDC(Counter5PCX > 0 ? "0503"
                        : CardiacCount > 0 ? "0504"
                                : "0505");
                break;
            }
            case "5PV": {//Multiple Valve Procedures
                result.setDC(Counter5PBX > 0 ? "0532" : "0533");
                break;
            }
            case "5PX": {//Complex Cardiothoracic Procedures
                result.setDC("0537");
                break;
            }
            case "5PA": {//Valve Replacement and Open Valvuloplasty
                result.setDC(Counter5PBX > 0 ? "0501" : "0502");
                break;
            }
            case "5PD": {//Other Cardiothoracic Procedures
                result.setDC("0506");
                break;
            }
            case "5PF": {//Major Cardiovascular Procedures
                result.setDC("0508");
                break;
            }
            case "5PU": {//Simple Cardiothoracic Procedures
                result.setDC("0513");
                break;
            }
            case "5PH": {//Cardiac Electrophysiologic Procedures
                result.setDC("0514");
                break;
            }
            case "5PG": {//Percutaneous Cardiovascular Procedures
                result.setDC(Counter5PDX > 0 ? "0523" : "0524");
                break;
            }
            case "5PS": {//Other Vascular Procedures
                result.setDC(Counter5PJX > 0 ? "0531" : "0515");
                break;
            }
            case "5PW": {//Multiple Wound Debridement
                result.setDC("0536");
                break;
            }
            case "5PK": {//Permanent Pacemaker
                result.setDC(Counter5BX > 0 ? "0510" : "0511");
                break;
            }
            case "5PJ": {//Major Amputation
                result.setDC("0509");
                break;
            }
            case "5PM": {//Automatic Cardioverter Procedures
                result.setDC("0512");
                break;
            }
            case "5PP": {//Pacemaker Device Replacement
                result.setDC("0518");
                break;
            }
            case "5PN": {//Pacemaker Revision
                result.setDC("0517");
                break;
            }
            case "5PR": {//Other Circulatory System OR Procedures
                result.setDC("0520");
                break;
            }
            case "5PT": {//Cardiac Cath
                result.setDC((Counter5DXPDx > 0 || Counter5DXSDx > 0) ? "0521" : "0522");
                if (Counter5DXPDx == 0 && Counter5DXSDx > 0) {
                    for (String sdx : SecondaryList) {
                        if (checkAX.AX(datasource, SchemaName, "5DX", sdx.trim()).isSuccess()) {
                            result.setSdxfinder(sdx.trim());
                            break;
                        }
                    }
                }
                break;
            }
            case "5PL": {//Minor Amputation
                result.setDC("0516");
                break;
            }
            default: {//Vein Ligation and Stripping
                result.setDC("0519");
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
            final String discharge) {
        String dc = "";
        switch (pdc) {
            case "5B": {//Infective Endocarditis
                dc = "0554";
                break;
            }
            case "5C": {//Heart Failure and Shock
                dc = "0555";
                break;
            }
            case "5D": {//Venous Thrombosis
                dc = "0556";
                break;
            }
            case "5E": {//Skin Ulcer for Circulatory Disorders
                dc = "0557";
                break;
            }
            case "5F": {//Peripheral Vascular
                dc = "4".equals(discharge) ? "0570" : "0558";
                break;
            }
            case "5G": {//Coronary Atherosclerosis
                dc = "0559";
                break;
            }
            case "5H": {//Hypertension
                dc = "0560";
                break;
            }
            case "5J": {//Congenital Heart Disease
                dc = "0561";
                break;
            }
            case "5K": {//Valvular Disorders
                dc = "0562";
                break;
            }
            case "5L": {//Major Arrhythmia and Cardiac Arrest
                dc = "0563";
                break;
            }
            case "5M": {//Non-major Arrhythmia and Conduction Disorders
                dc = "0564";
                break;
            }
            case "5N": {//Unstable Angina
                dc = "0565";
                break;
            }
            case "5P": {//Syncope and Collapse
                dc = "0566";
                break;
            }
            case "5Q": {//Chest Pain
                dc = "0567";
                break;
            }
            case "5R": {//Other Circulatory System Diagnoses PDC 5R
                dc = "0568";
                break;
            }
        }
        return dc;

    }

}
