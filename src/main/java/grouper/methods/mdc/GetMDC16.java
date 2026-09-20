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
        int mdcAsInt = Integer.parseInt(drgResult.getMDC());
        String mdcWithoutZeros = String.valueOf(mdcAsInt);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
//            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int PBXCounter99 = 0;
            int Counter16PBX = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            AX getAx = new AX();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                //AX 16PBX Checking
                if (getAx.AX(datasource, SchemaName, "16PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter16PBX++;
                }
                //AX 99PDX Checking
                if (getAx.AX(datasource, SchemaName, "99PDX", ProcedureList.get(x).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PBX Checking
                if (getAx.AX(datasource, SchemaName, "99PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    PBXCounter99++;
                }
                //AX 99PCX Checking
                if (getAx.AX(datasource, SchemaName, "99PCX", ProcedureList.get(x).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, SchemaName,
                        ProcedureList.get(x).trim(),
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
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 16
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
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
                        String dc = this.mdcProcedure(drgResult.getPDC());
                        drgResult.setDC(dc);
                    } else if (ORProcedureCounter > 0) {
                        String dc = this.orProcedure(ORProcedureCounterList);
                        drgResult.setDC(dc);
                    } else {
                        String dc = this.principalDaignosis(drgResult.getPDC(), PBXCounter99, Counter16PBX);
                        drgResult.setDC(dc);
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("1604");
                    } else {
                        drgResult.setDC("1605");
                    }
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                //Loop through the array  
                for (int i = 0; i < hierarvalue.size(); i++) {
                    //Compare elements of array with min  
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                String dc = this.mdcProcedure(drgResult.getPDC());
                drgResult.setDC(dc);
            } else if (ORProcedureCounter > 0) {
                String dc = this.orProcedure(ORProcedureCounterList);
                drgResult.setDC(dc);
            } else {
                String dc = this.principalDaignosis(drgResult.getPDC(), PBXCounter99, Counter16PBX);
                drgResult.setDC(dc);
            }
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
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

    public String mdcProcedure(
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

    public String orProcedure(ArrayList<Integer> ORProcedureCounterList) {
        String dc = "";
        switch (Collections.max(ORProcedureCounterList)) {
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

    public String principalDaignosis(
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
