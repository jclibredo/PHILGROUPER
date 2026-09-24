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
public class GetMDC16 {

    public GetMDC16() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC16.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC16(
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
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            AX getAx = new AX();
            MDCProcedureMethod mdcProce = new MDCProcedureMethod();
            ORProcedure orProc = new ORProcedure();
            GetPDC getPdc = new GetPDC();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int PBXCounter99 = 0;
            int Counter16PBX = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                Counter16PBX += getAx.AX(datasource, SchemaName, "16PBX", procS).isSuccess() ? 1 : 0;
                PDXCounter99 += getAx.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PBXCounter99 += getAx.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += getAx.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = orProc.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult JoinResult = mdcProce.MDCProcedure(datasource, SchemaName,
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
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 16
            if (PDXCounter99 > 0) { 
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge()));
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
                        drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                    } else {
                        drgResult.setDC(this.principalDaignosis(drgResult.getPDC(), PBXCounter99, Counter16PBX));
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "1604" : "1605");
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setDC(this.mdcProcedure(drgResult.getPDC()));
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else {
                drgResult.setDC(this.principalDaignosis(drgResult.getPDC(), PBXCounter99, Counter16PBX));
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 16 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC16 Method");
            logger.error("Error in MDC16 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    private String mdcProcedure(
            final String pdc) {
        String result = "";
        switch (pdc.toUpperCase()) {
            case "16PA"://Splenectomy
                result = "1601";
                break;
            case "16PB"://Major OR Procedures for Blood and Blood Forming Organs Except Splenectomy
                result = "1602";
                break;
            case "16PC"://Minor OR Procedures PDC 16PC
                result = "1603";
                break;
        }
        return result;
    }

    private String orProcedure(final Integer ORcounter) {
        String dc = "";
        switch (ORcounter) {
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

    private String principalDaignosis(
            final String pdc,
            final Integer PBXCounter99,
            final Integer Counter16PBX) {
        String dc = "";
        switch (pdc.toUpperCase()) {
            case "16A"://Red Blood Cell Disorders
                if (PBXCounter99 > 0) {
                    dc = "1653";
                } else {
                    dc = "1650";
                }
                break;
            case "16B"://Coagulation Disorders
                if (Counter16PBX > 0) {
                    dc = "1654";
                } else {
                    if (PBXCounter99 > 0) {
                        dc = "1655";
                    } else {
                        dc = "1651";;
                    }
                }
                break;
            case "16C"://Reticuloendothelial and Immunity Disorders
                if (PBXCounter99 > 0) {
                    dc = "1656";
                } else {
                    dc = "1652";
                }
                break;
        }
        return dc;

    }

}
