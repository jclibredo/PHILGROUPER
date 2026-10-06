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
import grouper.structures.MDCCodeOptimize;
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
import grouper.structures.SdxDcHelper;
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
public class GetMDC04 {

    public GetMDC04() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC04.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC04(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ArrayList<SdxDcHelper> helperList = new ArrayList<>();
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            AX checkAX = new AX();
            ORProcedure orProc = new ORProcedure();
            GetPDC getPdc = new GetPDC();
            MDCProcedureMethod mdcProc = new MDCProcedureMethod();
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            int ORProcedureCounter = 0;
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
            int CounterPdx4BX = 0;
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procS = ProcedureList.get(y).trim();
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
                if (checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("99PDX").tags("PROC").codes(procS).build());
                    PDXCounter99++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("99PCX").tags("PROC").codes(procS).build());
                    PCXCounter99++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PEX", procS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("99PEX").tags("PROC").codes(procS).build());
                    CartProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PFX", procS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("99PFX").tags("PROC").codes(procS).build());
                    CaCRxProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "4PCX", procS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("4PCX").tags("PROC").codes(procS).build());
                    PCX4Proc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PBX", procS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("99PBX").tags("PROC").codes(procS).build());
                    PBX99Proc++;
                }
                DRGWSResult ORProcedureResult = orProc.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }

            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxS = SecondaryList.get(a).trim();
                if (checkAX.AX(datasource, SchemaName, "99BX", sdxS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("99PBX").tags("SDX").codes(sdxS).build());
                    CartSDx++;
                }
                if (checkAX.AX(datasource, SchemaName, "4BX", sdxS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("4BX").tags("SDX").codes(sdxS).build());
                    Counter4BX++;
                }
                if (checkAX.AX(datasource, SchemaName, "99CX", sdxS).isSuccess()) {
                    helperList.add(SdxDcHelper.builder().type("99CX").tags("SDX").codes(sdxS).build());
                    CaCRxSDx++;
                }
            }
            if (checkAX.AX(datasource, SchemaName, "4BX", grouperparameter.getPdx().trim()).isSuccess()) {
                helperList.add(SdxDcHelper.builder().type("4BX").tags("SDX").codes(grouperparameter.getPdx()).build());
                CounterPdx4BX++;
            }
            boolean hasQualifyingCondition = PDXCounter99 > 0 || Counter4BX > 0 || CounterPdx4BX > 0;
            int max = Optional.ofNullable(ORProcedureCounterList)
                    .flatMap(list -> list.stream().filter(Objects::nonNull).max(Integer::compareTo))
                    .orElse(0);
//            pdclist.sort(Collections.reverseOrder());
            if (hasQualifyingCondition) {
                long los = utility.ComputeLOS(
                        grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())
                );
                if (los < 21) {
                    if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        drgResult.setDC(this.mdcProcedure(drgResult.getPDC()));
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(max));
                    } else {
                        MDCCodeOptimize dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                grouperparameter.getDischargeType(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                PCX4Proc,
                                PBX99Proc,
                                SecondaryList,
                                SchemaName,
                                datasource);
                        drgResult.setDC(dc.getDC());
                        if (!dc.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(dc.getSdxfinder());
                        }
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "0405" : "0406");
                    if (PDXCounter99 == 0 && CounterPdx4BX == 0 && Counter4BX > 0) {
                        for (int x = 0; x < helperList.size(); x++) {
                            if (helperList.get(x).getTags().equals("SDX")) {
                                String sdxCode = helperList.get(x).getCodes();
                                drgResult.setSDXFINDER(sdxCode);
                                break;
                            }
                        }
                    }
                }
            } else if (mdcprocedureCounter > 0) { //THIS AREA MDC PROCEDURE
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setDC(this.mdcProcedure(drgResult.getPDC()));
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(max));
            } else {
                MDCCodeOptimize dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        grouperparameter.getDischargeType(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        PCX4Proc,
                        PBX99Proc,
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

    private String mdcProcedure(
            final String pdc) {
        String result = "";
        switch (pdc.toUpperCase()) {
            case "4PA": {//Major Chest
                result = "0401";
                break;
            }
            case "4PB": {//Other Respiratory System Procedures
                result = "0402";
                break;
            }
            case "4PD": {//Ventilator Support
                result = "0403";
                break;
            }
            case "4PE": {//Noninvasive Ventilation 
                result = "0407";
                break;
            }
            case "4PC": {//Other Minor Respiratory System Procedures PDC 4PC
                result = "0408";
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

    private MDCCodeOptimize principalDaignosis(
            final String pdc,
            final String discharge,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer PCX4Proc,
            final Integer PBX99Proc,
            final List<String> SecondaryList,
            final String SchemaName,
            final DataSource dataSource) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        switch (pdc) {
            case "4A": {//Cystic Fibrosis
                result.setDC("0450");
                break;
            }
            case "4B": {//Pulmonary Embolism
                result.setDC("0451");
                break;
            }
            case "4C": {//Respiratory Infection/Inflammation
                result.setDC("0452");
                break;
            }
            case "4D": {//Sleep Apnea
                result.setDC("0453");
                break;
            }
            case "4E": {//Noninvasive Ventilation 
                result.setDC("4".equals(discharge) ? "0471" : "0454");
                break;
            }
            case "4F": {//COPD
                result.setDC("0455");
                break;
            }
            case "4G": {//Major Chest Trauma
                result.setDC("0456");
                break;
            }
            case "4H": {//Respiratory Signs and Symptoms 
                result.setDC("0457");
                break;
            }
            case "4J": {//Pneumothorax
                result.setDC("0458");
                break;
            }
            case "4K": {//Bronchitis and Asthma
                result.setDC("0459");
                break;
            }
            case "4L": {//Whooping Cough and Acute Bronchiolitis
                result.setDC("0460");
                break;
            }
            case "4M": {//Respiratory Neoplasms
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    result.setDC("0465");
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
                    result.setDC("0466");
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
                    result.setDC("0467");
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
                } else if (PCX4Proc > 0) { //##Dx Procedure
                    result.setDC("0468");
                    //Radiotherapy
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    result.setDC("0469");
                } else {
                    result.setDC("0461");
                }
                break;
            }
            case "4R": {//Pyothorax 
                result.setDC("0470");
                break;
            }
            case "4N": {//Pleural Effusion
                result.setDC("4".equals(discharge) ? "0472" : "0462");
                break;
            }
            case "4P": {//Interstitial Lung Diseases
                result.setDC("0463");
                break;
            }
            case "4Q": {//Other Minor Respiratory System Diagnosis PDC 4Q
                result.setDC("0464");
                break;
            }
        }
        return result;

    }

}
