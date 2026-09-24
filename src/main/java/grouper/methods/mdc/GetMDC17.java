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
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class GetMDC17 {

    public GetMDC17() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC17.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC17(
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
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            AX getAx = new AX();
            Endovasc getEndo = new Endovasc();
            ORProcedure orProcedure = new ORProcedure();
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter17PBX = 0;
            int ORProcedureCounter = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter17PA = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                Counter17PA += getEndo.Endovasc(datasource, SchemaName, procS, "17PA", mdcWithoutZeros).isSuccess() ? 1 : 0;
                CartProc += getAx.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += getAx.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += getAx.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                Counter17PBX += getAx.AX(datasource, SchemaName, "17PBX", procS).isSuccess() ? 1 : 0;
                PDXCounter99 += getAx.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += getAx.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                if (orProcedure.ORProcedure(datasource, SchemaName, procS).isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(orProcedure.ORProcedure(datasource, SchemaName, procS).getResult()));
                }
                MDCProcedureMethod mdcProcedureRes = new MDCProcedureMethod();
                if (mdcProcedureRes.MDCProcedure(datasource, SchemaName,
                        procS,
                        mdcWithoutZeros,
                        grouperparameter.getGender()).isSuccess()) {
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(mdcProcedureRes.MDCProcedure(datasource, SchemaName,
                            procS,
                            mdcWithoutZeros,
                            grouperparameter.getGender()).getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }

            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                CartSDx += getAx.AX(datasource, SchemaName, "99BX", sdxCode).isSuccess() ? 1 : 0;
                CaCRxSDx += getAx.AX(datasource, SchemaName, "99CX", sdxCode).isSuccess() ? 1 : 0;
            }

            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 16
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    String dc = this.principalDaignosis(
                            drgResult.getPDC(),
                            ORProcedureCounter,
                            Counter17PA,
                            CartSDx,
                            CaCRxSDx,
                            CartProc,
                            CaCRxProc,
                            Counter17PBX,
                            PBX99Proc);
                    drgResult.setDC(dc);
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "1705" : "1706");
                }
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        ORProcedureCounter,
                        Counter17PA,
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter17PBX,
                        PBX99Proc);
                drgResult.setDC(dc);
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 17 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC17 Method");
            logger.error("Error in MDC17 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    private String principalDaignosis(
            final String pdc,
            final Integer ORProcedureCounter,
            final Integer Counter17PA,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer Counter17PBX,
            final Integer PBX99Proc) {
        String dc = "";
        switch (pdc.toUpperCase()) {
            case "17A"://Acute Leukemia
                if (ORProcedureCounter > 0) {
                    dc = Counter17PA > 0 ? "1701" : "1703";
                } else {
                    //Radio+Chemotherapy
                    if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                        dc = "1756";
                        //Chemotherapy
                    } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                        dc = "1757";
                        //Radiotherapy
                    } else if (CartSDx > 0 && CartProc > 0) {
                        dc = "1758";
                    } else if (Counter17PBX > 0) { //##Dx Procedure
                        dc = "1759";
                    } else if (PBX99Proc > 0) {//Blood Transfusion
                        dc = "1760";
                    } else {//Malignancy 
                        dc = "1750";
                    }
                }
                break;
            case "17B"://Lymphoma & Non-acute Leukemia
                if (ORProcedureCounter > 0) {
                    dc = Counter17PA > 0 ? "1701" : "1703";
                } else {
                    //Radio+Chemotherapy
                    if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                        dc = "1761";
                        //Chemotherapy
                    } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                        dc = "1762";
                        //Radiotherapy
                    } else if (CartSDx > 0 && CartProc > 0) {
                        dc = "1763";
                    } else if (Counter17PBX > 0) { //##Dx Procedure
                        dc = "1764";
                    } else if (PBX99Proc > 0) {//Blood Transfusion
                        dc = "1765";
                    } else {//Malignancy 
                        dc = "1751";
                    }
                }
                break;
            case "17C"://Other Neoplastic Disorders PDC 17C
                if (ORProcedureCounter > 0) {
                    dc = Counter17PA > 0 ? "1702" : "1704";
                } else {
                    //Radio+Chemotherapy
                    if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                        dc = "1766";
                        //Chemotherapy
                    } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                        dc = "1767";
                        //Radiotherapy
                    } else if (CartSDx > 0 && CartProc > 0) {
                        dc = "1768";
                    } else if (Counter17PBX > 0) { //##Dx Procedure
                        dc = "1769";
                    } else if (PBX99Proc > 0) {//Blood Transfusion
                        dc = "1770";
                    } else {//Malignancy 
                        dc = "1752";
                    }
                }
                break;
        }
        return dc;

    }

}
