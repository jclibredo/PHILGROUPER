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
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC23 {

    public GetMDC23() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC23(
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
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            AX checkAX = new AX();
            ORProcedure orProc = new ORProcedure();
            GetPDC getPdc = new GetPDC();
            MDCProcedureMethod mdcProc = new MDCProcedureMethod();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int ORProcedureCounter = 0;
            int Counter23PBX = 0;
            int Counter23BX = 0;
            long age = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = orProc.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                Counter23PBX += checkAX.AX(datasource, SchemaName, "23PBX", procS).isSuccess() ? 1 : 0;
                DRGWSResult JoinResult = mdcProc.MDCProcedure(datasource, SchemaName,
                        procS,
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
//                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = getPdc.GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }

            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                Counter23BX += checkAX.AX(datasource, SchemaName, "23BX", sdxCode).isSuccess() ? 1 : 0;
            }
            if (PDXCounter99 > 0) {
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                    } else {
                        MDCCodeOptimize getResult = this.principalDaignosis(
                                drgResult.getPDC(),
                                Counter23BX,
                                SecondaryList,
                                datasource,
                                SchemaName,
                                Counter23PBX,
                                age);
                        if (!getResult.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(getResult.getSdxfinder());
                        }
                        drgResult.setDC(getResult.getDC());
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "2311" : "2312");
                }
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else {
                MDCCodeOptimize getResult = this.principalDaignosis(
                        drgResult.getPDC(),
                        Counter23BX,
                        SecondaryList,
                        datasource,
                        SchemaName,
                        Counter23PBX,
                        age);
                if (!getResult.getSdxfinder().isEmpty()) {
                    drgResult.setSDXFINDER(getResult.getSdxfinder());
                }
                drgResult.setDC(getResult.getDC());
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 23 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException | NumberFormatException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC23.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    private String orProcedure(final Integer ORProcedureCounterList) {
        String dc = "";
        switch (ORProcedureCounterList) {
            case 6://OR Proc Level 6 AND 5
            case 5:
                dc = "2308";
                break;
            case 4://OR Proc Level 4
                dc = "2307";
                break;
            case 3://OR Proc Level 3
                dc = "2306";
                break;
            case 2://OR Proc Level 2
                dc = "2305";
                break;
            case 1://OR Proc Level 1
                dc = "2304";
                break;
        }
        return dc;
    }

    private MDCCodeOptimize principalDaignosis(
            final String pdc,
            final Integer Counter23BX,
            final List<String> SecondaryList,
            final DataSource datasource,
            final String SchemaName,
            final Integer Counter23PBX,
            final Long age) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setPDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        switch (pdc.toUpperCase()) {
            case "23A": {//Rehabilitation
                result.setDC(Counter23BX > 0 ? "2355" : "2350");
                if (Counter23BX > 0) {
                    for (int x = 0; x < SecondaryList.size(); x++) {
                        String sdxCode = SecondaryList.get(x).trim();
                        if (checkAX.AX(datasource, SchemaName, "23BX", sdxCode).isSuccess()) {
                            result.setSdxfinder(sdxCode);
                            break;
                        }
                    }
                }
                break;
            }
            case "23B": {//Signs, Symptoms and Other Abnormal Findings
                result.setDC("2351");
                break;
            }
            case "23C": {//Drugs
                result.setDC(Counter23PBX > 0 ? "2303" : "2352");
                break;
            }
            case "23D": {//Other Factors Influencing Health Status PDC 23D
                result.setDC(age > 54 ? "2353" : "2354");
                break;
            }
        }
        return result;

    }

}
