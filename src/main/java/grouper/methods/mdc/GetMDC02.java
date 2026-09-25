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
public class GetMDC02 {

    public GetMDC02() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC02.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC02(
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
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            AX checkAX = new AX();
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            Endovasc enDova = new Endovasc();
            int ORProcedureCounter = 0;
            int MalignantCount = 0;
            int Counter2PCX = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int mdcprocedureCounter = 0;
            int pdcprocedureCounter2PA = 0;
            int pdcprocedureCounter2PJ = 0;
            int pdcprocedureCounter2PH = 0;
            int pdcprocedureCounter2PB = 0;
            int Counter2PDX = 0;
            int CounterPDx2BX = 0;
            GetPDC getPdc = new GetPDC();
            MDCProcedureMethod mdcProcess = new MDCProcedureMethod();
            PDxMalignancy pdxMalig = new PDxMalignancy();
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procS = ProcedureList.get(y).trim();
                if (mdcProcess.MDCProcedure(datasource,
                        SchemaName,
                        procS, mdcWithoutZeros, grouperparameter.getGender()).isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(mdcProcess.MDCProcedure(datasource,
                            SchemaName,
                            procS,
                            mdcWithoutZeros,
                            grouperparameter.getGender()).getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = getPdc.GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC().trim(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                pdcprocedureCounter2PA += enDova.Endovasc(datasource, SchemaName, procS, "2PA".trim(), mdcWithoutZeros).isSuccess() ? 1 : 0;
                pdcprocedureCounter2PJ += enDova.Endovasc(datasource, SchemaName, procS, "2PJ".trim(), mdcWithoutZeros).isSuccess() ? 1 : 0;
                pdcprocedureCounter2PH += enDova.Endovasc(datasource, SchemaName, procS, "2PH".trim(), mdcWithoutZeros).isSuccess() ? 1 : 0;
                pdcprocedureCounter2PB += enDova.Endovasc(datasource, SchemaName, procS, "2PB".trim(), mdcWithoutZeros).isSuccess() ? 1 : 0;
                Counter2PDX += checkAX.AX(datasource, SchemaName, "2PDX", procS).isSuccess() ? 1 : 0;
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                Counter2PCX += checkAX.AX(datasource, SchemaName, "2PCX", procS).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }
            CounterPDx2BX += checkAX.AX(datasource, SchemaName, "2BX", grouperparameter.getPdx()).isSuccess() ? 1 : 0;
            MalignantCount += pdxMalig.PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "2E".trim()).isSuccess() ? 1 : 0;
            //Condition Start this area   
            if (PDXCounter99 > 0) {
                long los = utility.ComputeLOS(
                        grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())
                );
                if (los > 21) {
                    drgResult.setDC(PCXCounter99 > 0 ? "0214" : "0215");
                } else if (pdcprocedureCounter2PA > 0) {  //Retina Procedure
                    drgResult.setDC("0201");
                } else if (pdcprocedureCounter2PJ > 0) {//Keratoplasty
                    drgResult.setDC("0209");
                } else if (pdcprocedureCounter2PH > 0) { //Other mech Vitrectomy
                    drgResult.setDC(Counter2PDX > 0 ? "0206" : "0201");
                } else if (pdcprocedureCounter2PB > 0) { //Enuc & Orbit Procedure
                    drgResult.setDC(MalignantCount > 0 ? "0210" : "0202");
                } else if (Counter2PCX > 0 && CounterPDx2BX > 0) { // Major Aye Injury with OR 
                    drgResult.setDC("0203");
                } else if (mdcprocedureCounter > 0) { // MDC Procedure
                    int min = hierarvalue.get(0);
                    for (int i = 0; i < hierarvalue.size(); i++) {
                        if (hierarvalue.get(i) < min) {
                            min = hierarvalue.get(i);
                        }
                    }
                    drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                    drgResult.setDC(this.mdcProcedure(drgResult.getPDC()));
                } else if (ORProcedureCounter > 0) { //Check if OR Procedure
                    drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                } else { //Principal Diagnosis
                    drgResult.setDC(this.principalDaignosis(drgResult.getPDC(), grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()));
                }
            } else if (pdcprocedureCounter2PA > 0) {  //Retina Procedure
                drgResult.setDC("0201");
            } else if (pdcprocedureCounter2PJ > 0) {//Keratoplasty
                drgResult.setDC("0209");
            } else if (pdcprocedureCounter2PH > 0) { //Other mech Vitrectomy
                drgResult.setDC(Counter2PDX > 0 ? "0206" : "0201");
            } else if (pdcprocedureCounter2PB > 0) { //Enuc & Orbit Procedure
                drgResult.setDC(MalignantCount > 0 ? "0210" : "0202");
            } else if (Counter2PCX > 0 && CounterPDx2BX > 0) { // Major Aye Injury with OR 
                drgResult.setDC("0203");
            } else if (mdcprocedureCounter > 0) { // MDC Procedure
                int min = hierarvalue.get(0); 
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setDC(this.mdcProcedure(drgResult.getPDC()));
            } else if (ORProcedureCounter > 0) { //Check if OR Procedure
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else { //Principal Diagnosis
                drgResult.setDC(this.principalDaignosis(drgResult.getPDC(), grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()));
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 02 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC2 Method");
            logger.error("Error in MDC2 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    private String mdcProcedure(
            final String pdc) {
        String result = "";
        switch (pdc) {
            case "2PK": { //Multiple Major Lens
                result = "0211";
                break;
            }
            case "2PL": {//Multiple Other Lens
                result = "0212";
                break;
            }
            case "2PE": { //Major Lens
                result = "0206";
                break;
            }
            case "2PM": { //Major Procedures for Lacrimal System
                result = "0213";
                break;
            }
            case "2PD": { //Intraoc Procedures Except lens & Retina
                result = "0205";
                break;
            }
            case "2PF": {//Other Lens
                result = "0207";
                break;
            }
            case "2PG": {//Other Eye Procedures 2PG
                result = "0208";
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
            final String dbate,
            final String admission) {
        String dc = "";
        switch (pdc) {
            case "2C": {//Hyphema and Trauma
                dc = "0250";
                break;
            }
            case "2A": {//Acute Major Infections
                dc = (utility.ComputeYear(dbate, admission) > 54) ? "0251" : "0252";
                break;
            }
            case "2E": {//Malignancy
                dc = "0255";
                break;
            }
            case "2B": {//Neurological & Vasc Disorders
                dc = "0253";
                break;
            }
            case "2D": {//Other Disorders of the Eye PDC 2D
                dc = "0254";
                break;
            }
        }
        return dc;

    }

}
