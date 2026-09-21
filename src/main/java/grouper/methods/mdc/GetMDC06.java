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
        int mdcAsInt = Integer.parseInt(drgResult.getMDC());
        String mdcWithoutZeros = String.valueOf(mdcAsInt);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            AX checkAX = new AX();
            //THIS AREA IS FOR CHECKING OF RADIO AND CHECMO
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX6Proc = 0;
            int PBX99Proc = 0;
            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (checkAX.AX(datasource, SchemaName, "99BX", SecondaryList.get(a).trim()).isSuccess()) {//Dx Procedure
                    CartSDx++;
                }
                if (checkAX.AX(datasource, SchemaName, "99CX", SecondaryList.get(a).trim()).isSuccess()) {//Dx Procedure
                    CaCRxSDx++;
                }
            }
            int MalignantCount = 0;
            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "6A").isSuccess()) {
                MalignantCount++;
            }
            //Maj Dig Dis AX 6BX
            int Ax6BXCount = 0;
            if (checkAX.AX(datasource, SchemaName, "6BX", grouperparameter.getPdx()).isSuccess()) {
                Ax6BXCount++;
            }
            //Inguinal or Femoral PDC 6PH
            int Counter6PH = 0;
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int y = 0; y < ProcedureList.size(); y++) {
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        ProcedureList.get(y).trim(),
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
                if (new Endovasc().Endovasc(datasource, SchemaName, ProcedureList.get(y).trim(), "6PH", mdcWithoutZeros).isSuccess()) {
                    Counter6PH++;
                }
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
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
                if (checkAX.AX(datasource, SchemaName, "6PBX", ProcedureList.get(y).trim()).isSuccess()) {//Dx Procedure
                    PBX6Proc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PBX", ProcedureList.get(y).trim()).isSuccess()) {//Dx Procedure
                    PBX99Proc++;
                }
            }
            //CONDITIONAL STATEMENT STARTS HERE FOR MDC 06
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
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
                        String dc = this.orProcedure(ORProcedureCounterList);
                        drgResult.setDC(dc);
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
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0633");
                    } else {
                        drgResult.setDC("0634");
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
                String dc = this.orProcedure(ORProcedureCounterList);
                drgResult.setDC(dc);
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
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
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
        switch (pdc.toUpperCase()) {
            case "6PS"://Lap Stomach, Eso & Duodenum
                result.setDC("0627");
                break;
            case "6PV"://Stomach ,Eso & Duodenum Resection
                if (MalignantCount > 0) {
                    result.setDC("0630");
                } else {
                    result.setDC("0631");
                }
                break;
            case "6PE"://Other Stomach ,Eso & Duodenum
                if (MalignantCount > 0) {
                    result.setDC("0601");
                } else {
                    result.setDC("0602");
                }
                break;
            case "6PA"://Rectal Resection
                result.setDC("0604");
                break;
            case "6PB"://Major Small and Large Bowel
                result.setDC("0603");
                break;
            case "6PT"://Lap Peritoneal Adhesiolysis
                result.setDC("0628");
                break;
            case "6PK"://Other Digestive System OR Procedures
                if (MalignantCount > 0) {
                    result.setDC("0613");
                } else {
                    result.setDC("0614");
                }
                break;
            case "6PM"://Complex Therapeutic Gastroscopy
                if (Ax6BXCount > 0) {
                    result.setDC("0616");
                } else {
                    result.setDC("0617");
                }
                break;
            case "6PD"://Minor Small and Large Bowel
                result.setDC("0608");
                break;
            case "6PC"://Peritoneal Adhesiolysis
                result.setDC("0605");
                break;
            case "6PG":
            case "6PH":
                if (utility.ComputeYear(bdate,
                        admDate) > 14) {
                    if (Counter6PH > 0) { //IF TRUE
                        result.setDC("0610");
                    } else {
                        result.setDC("0611");//IF FALSE
                    }
                } else {
                    result.setDC("0612");
                }
                break;

            case "6PL"://Pyloromyotomy procedure
                result.setDC("0615");
                break;
            case "6PU"://Lap Appendectomy
                result.setDC("0629");
                break;
            case "6PJ"://Appendectomy
                if (checkAX.AX(datasource, SchemaName, "6CX", pdx.trim()).isSuccess()) {
                    result.setDC("0632");
                } else {
                    result.setDC("0607");
                }
                break;
            case "6PN"://Other Gastroscopy
                if (Ax6BXCount > 0) {
                    if (utility.ComputeLOS(admDate,
                            utility.Convert24to12(admTime),
                            disDate,
                            utility.Convert24to12(disTime)) > 0) {
                        result.setDC("0619");
                    } else {
                        result.setDRG("06209");
                        result.setDC("0620");
                    }
                } else {
                    if (utility.ComputeLOS(admDate,
                            utility.Convert24to12(admTime),
                            disDate,
                            utility.Convert24to12(disTime)) > 0) {
                        result.setDC("0621");
                    } else {
                        result.setDRG("06229");
                        result.setDC("0622");
                    }
                }
                break;
            case "6PP"://Complex Therpeutic Colonoscopy
                result.setDC("0623");
                break;
            case "6PQ"://Other Colonoscopy
                if (utility.ComputeLOS(admDate,
                        utility.Convert24to12(admTime),
                        disDate,
                        utility.Convert24to12(disTime)) > 0) {
                    result.setDC("0624");
                } else {
                    result.setDRG("06259");
                    result.setDC("0625");
                }
                break;
            case "6PF"://Anal and Stomal
                result.setDC("0609");
                break;
            case "6PR"://Dilatation of Intestine 6PR
                result.setDRG("06269");
                result.setDC("0626");
                break;
        }
        return result;
    }

    private String orProcedure(ArrayList<Integer> ORProcedureCounterList) {
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
        switch (pdc) {
            case "6A":
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
                    if (discharge.equals("4")) {
                        dc = "0673";
                    } else {
                        dc = "0650";
                    }
                }
                break;

            case "6B"://G.I. Hemorrhage
                if (utility.ComputeYear(bdate, admDate) > 64) {
                    dc = "0651";
                } else {
                    dc = "0652";
                }
                break;
            case "6C"://Complicated Peptic Ulcer
                dc = "0653";
                break;
            case "6D"://Uncomplicated Peptic Ulcer
                dc = "0654";
                break;
            case "6E"://Inflammatory Bowel Diseases
                dc = "0655";
                break;
            case "6F"://G.I. Obstruction
                if (discharge.equals("4")) {//Transfer
                    dc = "0674";
                } else {
                    dc = "0656";
                    //Others
                }

                break;
            case "6G"://Gastroenteritis
                if (utility.ComputeYear(bdate, admDate) > 9) {
                    dc = "0657";
                } else {
                    dc = "0658";
                }

                break;
            case "6H"://Misc Digestive Disorder
                if (utility.ComputeYear(bdate, admDate) > 9) {
                    dc = "0666";
                } else {
                    dc = "0667";
                }
                break;
            case "6J"://Other Digestive System Diagnoses
                if (discharge.equals("4")) {
                    dc = "0675";
                } else {
                    dc = "0660";
                }
                break;
            case "6K"://Abdominal Pain or Mesenteric Adenitis
                dc = "0661";
                break;
            case "6L"://Intestinal Helminthiases
                if (utility.ComputeYear(bdate, admDate) > 9) {
                    dc = "0662";
                } else {
                    dc = "0663";
                }
                break;
            case "6M"://Esophagitis, Gastritis & Dyspepsia PDC 6M
                if (utility.ComputeYear(bdate, admDate) > 9) {
                    dc = "0664";
                } else {
                    dc = "0665";
                }
                break;
        }
        return dc;

    }

}
