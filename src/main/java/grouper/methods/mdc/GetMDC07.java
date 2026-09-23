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
                if (checkAX.AX(datasource, SchemaName, "99BX", SecondaryList.get(a).trim()).isSuccess()) {
                    CartSDx++;
                }
                if (checkAX.AX(datasource, SchemaName, "99CX", SecondaryList.get(a).trim()).isSuccess()) {
                    CaCRxSDx++;
                }
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "7B").isSuccess()) {
                B7Count++;
            }
            for (int y = 0; y < ProcedureList.size(); y++) {
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        ProcedureList.get(y),
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
                //AX 99PDX Checking
                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PEX", ProcedureList.get(y).trim()).isSuccess()) {
                    CartProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PFX", ProcedureList.get(y).trim()).isSuccess()) {
                    CaCRxProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PBX", ProcedureList.get(y).trim()).isSuccess()) {
                    PBX99Proc++;
                }
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                if (checkAX.AX(datasource, SchemaName, "7PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter7PDX++;
                }
                //Maj Dig Dis AX 7PBX
                if (checkAX.AX(datasource, SchemaName, "7PBX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter7PBX++;
                }
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
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
                        MDCCodeOptimize getMdcProc = this.mdcProcedure(drgResult.getPDC(), B7Count, Counter7PBX);
                        drgResult.setDC(getMdcProc.getDC());
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                    } else {
                        drgResult.setDC(this.principalDaignosis(drgResult.getPDC(), CartSDx, CaCRxSDx, CartProc, CaCRxProc, Counter7PDX, PBX99Proc, grouperparameter.getDischargeType()));
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
                MDCCodeOptimize getMdcProc = this.mdcProcedure(drgResult.getPDC(), B7Count, Counter7PBX);
                drgResult.setDC(getMdcProc.getDC());
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else {
                drgResult.setDC(this.principalDaignosis(drgResult.getPDC(), CartSDx, CaCRxSDx, CartProc, CaCRxProc, Counter7PDX, PBX99Proc, grouperparameter.getDischargeType()));
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

    private MDCCodeOptimize mdcProcedure(
            final String pdc,
            final Integer B7Count,
            final Integer Counter7PBX) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setPDC("");
        result.setDC("");
        result.setSdxfinder("");
        //CHECKING FOR TRAUMA CODES
        switch (pdc) {
            case "7PA": {//Pancreas, Liver Resection and Shunt Procedures
                result.setDC("0701");
                break;
            }
            case "7PB": {//Biliary Tract Procedure
                result.setDC(B7Count > 0 ? "0702" : "0703");
                break;
            }
            case "7PG": {//Pancreas and Liver Procedure Except Resection
                result.setDC("0711");
                break;
            }
            case "7PF": {//Laparoscopic Cholecystectomy
                result.setDC(Counter7PBX > 0 ? "0709" : "0710");
                break;
            }
            case "7PC": {//Cholecystectomy
                result.setDC(Counter7PBX > 0 ? "0704" : "0705");
                break;
            }
            case "7PD": {//Hepatobiliary Diagnostic Procedures
                result.setDC("0706");
                break;
            }
            case "7PE": {//Other Hepatobiliary and Pancreas Procedures
                result.setDC("0707");
                break;
            }
            case "7PH": {//ERCP with Therapeutic Procedures 7PH
                result.setDC("0708");
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
            final Integer Counter7PDX,
            final Integer PBX99Proc,
            final String disChargeType) {
        String dc = "";
        switch (pdc) {
            case "7B": {//Malignancy of Hepatobiliary or Pancreas
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "0756";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "0757";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "0758";
                } else if (Counter7PDX > 0) { //##Dx Procedure
                    dc = "0759";
                    //Radiotherapy
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "0760";
                } else {//Malignancy 
                    if (disChargeType.equals("4")) {
                        dc = "0761";
                    } else {
                        dc = "0751";
                    }
                }
                break;
            }
            case "7A": {//Cirrhosis and Alcoholic Hepatitis
                dc = "0750";
                break;
            }
            case "7C": {//Disorder of Pancreas, Except Malignancy
                dc = "0753";
                break;
            }
            case "7D": {//Disorder of Liver, Except Malignancy, Cirrhosis, Alcoholic Hepatitis
                dc = "0754";
                break;
            }
            case "7E": {//Disorder of Biliary Tract 7E
                dc = "0755";
                break;
            }
        }
        return dc;

    }

}
