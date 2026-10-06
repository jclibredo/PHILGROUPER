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
public class GetMDC07 {

    public GetMDC07() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC07.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC07(
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
            PDxMalignancy pdxMalig = new PDxMalignancy();
            ORProcedure orProc = new ORProcedure();
            GetPDC getPdc = new GetPDC();
            MDCProcedureMethod mdcProc = new MDCProcedureMethod();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int B7Count = 0;
            int Counter7PDX = 0;
            int Counter7PBX = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                CartSDx += checkAX.AX(datasource, SchemaName, "99BX", sdxCode).isSuccess() ? 1 : 0;
                CaCRxSDx += checkAX.AX(datasource, SchemaName, "99CX", sdxCode).isSuccess() ? 1 : 0;
            }
            B7Count += pdxMalig.PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "7B").isSuccess() ? 1 : 0;
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
                    DRGWSResult pdcresult = getPdc.GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                CartProc += checkAX.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += checkAX.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += checkAX.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = orProc.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                Counter7PDX += checkAX.AX(datasource, SchemaName, "7PDX", procS).isSuccess() ? 1 : 0;
                Counter7PBX += checkAX.AX(datasource, SchemaName, "7PBX", procS).isSuccess() ? 1 : 0;
            }
            int max = Optional.ofNullable(ORProcedureCounterList)
                    .flatMap(list -> list.stream().filter(Objects::nonNull).max(Integer::compareTo))
                    .orElse(0);
//             pdclist.sort(Collections.reverseOrder());
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        //Loop through the array  
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            //Compare elements of array with min  
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        drgResult.setDC(this.mdcProcedure(drgResult.getPDC(), B7Count, Counter7PBX));
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(max));
                    } else {
                        MDCCodeOptimize dc = this.principalDaignosis(drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                Counter7PDX,
                                PBX99Proc,
                                grouperparameter.getDischargeType(),
                                SecondaryList,
                                SchemaName,
                                datasource);
                        drgResult.setDC(dc.getDC());
                        if (!dc.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(dc.getSdxfinder());
                        }
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "0712" : "0713");
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setDC(this.mdcProcedure(drgResult.getPDC(), B7Count, Counter7PBX));
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(max));
            } else {
                MDCCodeOptimize dc = this.principalDaignosis(drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter7PDX,
                        PBX99Proc,
                        grouperparameter.getDischargeType(),
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
                result.setMessage("MDC 07 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC7 Method");
            logger.error("Error in MDC7 Method : {}", ex.getMessage(), ex);
        }

        return result;

    }

    private String mdcProcedure(
            final String pdc,
            final Integer B7Count,
            final Integer Counter7PBX) {
        String result = "";
        switch (pdc) {
            case "7PA": {//Pancreas, Liver Resection and Shunt Procedures
                result = "0701";
                break;
            }
            case "7PB": {//Biliary Tract Procedure
                result = B7Count > 0 ? "0702" : "0703";
                break;
            }
            case "7PG": {//Pancreas and Liver Procedure Except Resection
                result = "0711";
                break;
            }
            case "7PF": {//Laparoscopic Cholecystectomy
                result = Counter7PBX > 0 ? "0709" : "0710";
                break;
            }
            case "7PC": {//Cholecystectomy
                result = Counter7PBX > 0 ? "0704" : "0705";
                break;
            }
            case "7PD": {//Hepatobiliary Diagnostic Procedures
                result = "0706";
                break;
            }
            case "7PE": {//Other Hepatobiliary and Pancreas Procedures
                result = "0707";
                break;
            }
            case "7PH": {//ERCP with Therapeutic Procedures 7PH
                result = "0708";
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
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer Counter7PDX,
            final Integer PBX99Proc,
            final String disChargeType,
            final List<String> SecondaryList,
            final String SchemaName,
            final DataSource dataSource) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        switch (pdc) {
            case "7B": {//Malignancy of Hepatobiliary or Pancreas
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    result.setDC("0756");
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
                    result.setDC("0757");
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
                    result.setDC("0758");
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
                } else if (Counter7PDX > 0) { //##Dx Procedure
                    result.setDC("0759");
                    //Radiotherapy
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    result.setDC("0760");
                } else {//Malignancy 
                    result.setDC("4".equals(disChargeType) ? "0761" : "0751");
                }
                break;
            }
            case "7A": {//Cirrhosis and Alcoholic Hepatitis
                result.setDC("0750");
                break;
            }
            case "7C": {//Disorder of Pancreas, Except Malignancy
                result.setDC("0753");
                break;
            }
            case "7D": {//Disorder of Liver, Except Malignancy, Cirrhosis, Alcoholic Hepatitis
                result.setDC("0754");
                break;
            }
            case "7E": {//Disorder of Biliary Tract 7E
                result.setDC("0755");
                break;
            }
        }
        return result;

    }

}
