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
public class GetMDC06 {

    public GetMDC06() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC06.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC06(
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
            AX checkAX = new AX();
            PDxMalignancy pdxMalig = new PDxMalignancy();
            Endovasc enDov = new Endovasc();
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX6Proc = 0;
            int PBX99Proc = 0;
            int MalignantCount = 0;
            int Ax6BXCount = 0;
            int Counter6PH = 0;
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                CartSDx += checkAX.AX(datasource, SchemaName, "99BX", sdxCode).isSuccess() ? 1 : 0;
                CaCRxSDx += checkAX.AX(datasource, SchemaName, "99CX", sdxCode).isSuccess() ? 1 : 0;
            }
            MalignantCount += pdxMalig.PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "6A").isSuccess() ? 1 : 0;
            Ax6BXCount += checkAX.AX(datasource, SchemaName, "6BX", grouperparameter.getPdx()).isSuccess() ? 1 : 0;
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procS = ProcedureList.get(y).trim();
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
                //Inguinal or Femoral PDC 6PH
                Counter6PH += enDov.Endovasc(datasource, SchemaName, procS, "6PH", mdcWithoutZeros).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                CartProc += checkAX.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += checkAX.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PBX6Proc += checkAX.AX(datasource, SchemaName, "6PBX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += checkAX.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
            }
            //CONDITIONAL STATEMENT STARTS HERE FOR MDC 06
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                long los = utility.ComputeLOS(
                        grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())
                );
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
                        MDCCodeOptimize getResult = this.mdcProcedure(
                                drgResult.getPDC(),
                                MalignantCount,
                                Ax6BXCount,
                                grouperparameter.getBirthDate(),
                                grouperparameter.getAdmissionDate(),
                                Counter6PH,
                                datasource,
                                SchemaName,
                                grouperparameter.getPdx(),
                                grouperparameter.getTimeAdmission(),
                                grouperparameter.getDischargeDate(),
                                grouperparameter.getTimeDischarge());
                        drgResult.setDC(getResult.getDC());
                        if (!getResult.getDRG().isEmpty()) {
                            drgResult.setDRG(getResult.getDRG());
                        }
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(ORProcedureCounterList));
                    } else {
                        String dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                PBX6Proc,
                                PBX99Proc,
                                grouperparameter.getDischargeType(),
                                grouperparameter.getBirthDate(),
                                grouperparameter.getAdmissionDate());
                        drgResult.setDC(dc);
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "0633" : "0634");
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
                MDCCodeOptimize getResult = this.mdcProcedure(
                        drgResult.getPDC(),
                        MalignantCount,
                        Ax6BXCount,
                        grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate(),
                        Counter6PH,
                        datasource,
                        SchemaName,
                        grouperparameter.getPdx(),
                        grouperparameter.getTimeAdmission(),
                        grouperparameter.getDischargeDate(),
                        grouperparameter.getTimeDischarge());
                drgResult.setDC(getResult.getDC());
                if (!getResult.getDRG().isEmpty()) {
                    drgResult.setDRG(getResult.getDRG());
                }
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(ORProcedureCounterList));
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        PBX6Proc,
                        PBX99Proc,
                        grouperparameter.getDischargeType(),
                        grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate());
                drgResult.setDC(dc);
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 06 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }

        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC6 Method");
            logger.error("Error in MDC6 Method : {}", ex.getMessage(), ex);
        }

        return result;
    }

    private MDCCodeOptimize mdcProcedure(
            final String pdc,
            final Integer MalignantCount,
            final Integer Ax6BXCount,
            final String bdate,
            final String admDate,
            final Integer Counter6PH,
            final DataSource datasource,
            final String SchemaName,
            final String pdx,
            final String admTime,
            final String disDate,
            final String disTime) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setDRG("");
        result.setPDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        long los = utility.ComputeLOS(admDate,
                utility.Convert24to12(admTime),
                disDate,
                utility.Convert24to12(disTime));
        switch (pdc.toUpperCase()) {
            case "6PS": {//Lap Stomach, Eso & Duodenum
                result.setDC("0627");
                break;
            }
            case "6PV": {//Stomach ,Eso & Duodenum Resection
                result.setDC(MalignantCount > 0 ? "0630" : "0631");
                break;
            }
            case "6PE": {//Other Stomach ,Eso & Duodenum
                result.setDC(MalignantCount > 0 ? "0601" : "0602");
                break;
            }
            case "6PA": {//Rectal Resection
                result.setDC("0604");
                break;
            }
            case "6PB": {//Major Small and Large Bowel
                result.setDC("0603");
                break;
            }
            case "6PT": {//Lap Peritoneal Adhesiolysis
                result.setDC("0628");
                break;
            }
            case "6PK": {//Other Digestive System OR Procedures
                result.setDC(MalignantCount > 0 ? "0613" : "0614");
                break;
            }
            case "6PM": {//Complex Therapeutic Gastroscopy
                result.setDC(Ax6BXCount > 0 ? "0616" : "0617");
                break;
            }
            case "6PD": {//Minor Small and Large Bowel
                result.setDC("0608");
                break;
            }
            case "6PC": {//Peritoneal Adhesiolysis
                result.setDC("0605");
                break;
            }
            case "6PG":
            case "6PH": {
                result.setDC((utility.ComputeYear(bdate, admDate) > 14) ? ((Counter6PH > 0) ? "0610" : "0611") : "0612");
                break;
            }
            case "6PL": {//Pyloromyotomy procedure
                result.setDC("0615");
                break;
            }
            case "6PU": {//Lap Appendectomy
                result.setDC("0629");
                break;
            }
            case "6PJ": {//Appendectomy
                result.setDC(checkAX.AX(datasource, SchemaName, "6CX", pdx.trim()).isSuccess() ? "0632" : "0607");
                break;
            }
            case "6PN": {//Other Gastroscopy
                if (Ax6BXCount > 0) {
                    if (los <= 0) {
                        result.setDRG("06209");
                    }
                    result.setDC(los > 0 ? "0619" : "0620");
                } else {
                    if (los <= 0) {
                        result.setDRG("06229");
                    }
                    result.setDC(los > 0 ? "0621" : "0622");
                }
                break;
            }
            case "6PP": {//Complex Therpeutic Colonoscopy
                result.setDC("0623");
                break;
            }
            case "6PQ": {//Other Colonoscopy
                if (los <= 0) {
                    result.setDRG("06259");
                }
                result.setDC(los > 0 ? "0624" : "0625");
                break;
            }
            case "6PF": {//Anal and Stomal
                result.setDC("0609");
                break;
            }
            case "6PR": {//Dilatation of Intestine 6PR
                result.setDRG("06269");
                result.setDC("0626");
                break;
            }
        }
        return result;
    }

    private String orProcedure(ArrayList<Integer> ORProcedureCounterList) {
        String dc = "";
        switch (Collections.max(ORProcedureCounterList)) {
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
            final Integer PBX6Proc,
            final Integer PBX99Proc,
            final String discharge,
            final String bdate,
            final String admDate) {
        String dc = "";
        long age = utility.ComputeYear(bdate, admDate);
        switch (pdc) {
            case "6A": {
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "0668";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "0669";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "0670";
                } else if (PBX6Proc > 0) { //##Dx Procedure
                    dc = "0671";
                    //Radiotherapy
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "0672";
                } else {//Malignancy 
                    dc = "4".equals(discharge) ? "0673" : "0650";
                }
                break;
            }
            case "6B": {//G.I. Hemorrhage
                dc = (age > 64 ? "0651" : "0652");
                break;
            }
            case "6C": {//Complicated Peptic Ulcer
                dc = "0653";
                break;
            }
            case "6D": {//Uncomplicated Peptic Ulcer
                dc = "0654";
                break;
            }
            case "6E": {//Inflammatory Bowel Diseases
                dc = "0655";
                break;
            }
            case "6F": {//G.I. Obstruction
                dc = "4".equals(discharge) ? "0674" : "0656";
                break;
            }
            case "6G": {//Gastroenteritis
                dc = (age > 9 ? "0657" : "0658");
                break;
            }
            case "6H": {//Misc Digestive Disorder
                dc = (age > 9 ? "0666" : "0667");
                break;
            }
            case "6J": {//Other Digestive System Diagnoses
                dc = "4".equals(discharge) ? "0675" : "0660";
                break;
            }
            case "6K": {//Abdominal Pain or Mesenteric Adenitis
                dc = "0661";
                break;
            }
            case "6L": {//Intestinal Helminthiases
                dc = (age > 9 ? "0662" : "0663");
                break;
            }
            case "6M": {//Esophagitis, Gastritis & Dyspepsia PDC 6M
                dc = (age > 9 ? "0664" : "0665");
                break;
            }
        }
        return dc;

    }

}
