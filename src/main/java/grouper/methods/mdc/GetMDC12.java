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
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class GetMDC12 {

    private final Logger logger = (Logger) LogManager.getLogger(GetMDC12.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC12(
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
            AX axRest = new AX();
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX12Proc = 0;
            int PBX99Proc = 0;
            String pdc12A = "12A";
            int MalignantCount = 0;
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procS = ProcedureList.get(y).trim();
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, SchemaName,
                        procS.trim(),
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
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, procS.trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                if (axRest.AX(datasource, SchemaName, "99PDX", procS).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (axRest.AX(datasource, SchemaName, "99PCX", procS).isSuccess()) {
                    PCXCounter99++;
                }
                if (axRest.AX(datasource, SchemaName, "99PEX", procS).isSuccess()) {
                    CartProc++;
                }
                if (axRest.AX(datasource, SchemaName, "99PFX", procS).isSuccess()) {
                    CaCRxProc++;
                }
                if (axRest.AX(datasource, SchemaName, "12PBX", procS).isSuccess()) {
                    PBX12Proc++;
                }
                if (axRest.AX(datasource, SchemaName, "99PBX", procS).isSuccess()) {
                    PBX99Proc++;
                }
            }

            //Malignant Counter for Primay Code (PDx)
            DRGWSResult getMalignantResult = new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "12A");
            if (getMalignantResult.isSuccess()) {
                MalignantCount++;
            }
            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                String Secon = SecondaryList.get(a).trim();
                if (axRest.AX(datasource, SchemaName, "99BX", Secon).isSuccess()) {
                    CartSDx++;
                }
                if (axRest.AX(datasource, SchemaName, "99CX", Secon).isSuccess()) {
                    CaCRxSDx++;
                }
            }

            if (PDXCounter99 > 0) { //Check Procedure if Tracheostomy
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los > 21) {
                    drgResult.setDC(PCXCounter99 > 0 ? "1209" : "1210");
                } else if (mdcprocedureCounter > 0) { //MDC Procedure
                    int min = hierarvalue.get(0);
                    for (int i = 0; i < hierarvalue.size(); i++) {
                        if (hierarvalue.get(i) < min) {
                            min = hierarvalue.get(i);
                        }
                    }
                    drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                    drgResult.setDC(this.mdcProcedure(pdc12A, MalignantCount));
                } else if (ORProcedureCounter > 0) {
                    drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                } else {
                    String dc = this.principalDaignosis(
                            drgResult.getPDC(),
                            CartSDx,
                            CaCRxSDx,
                            CartProc,
                            CaCRxProc,
                            PBX12Proc,
                            PBX99Proc);
                    drgResult.setDC(dc);
                }
            } else if (mdcprocedureCounter > 0) { //MDC Procedure
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setDC(this.mdcProcedure(pdc12A, MalignantCount));
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        PBX12Proc,
                        PBX99Proc);
                drgResult.setDC(dc);
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 12 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            logger.info("Executing MDC12 Method");
            logger.error("Error in MDC12 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    private String mdcProcedure(
            final String pdc,
            final Integer MalignantCount) {
        String result = "";
        switch (pdc.toUpperCase()) {
            case "12PA": {  //1Major Male Pelvic Procedures
                result = "1201";
                break;
            }
            case "12PB": {//Transurethral Prostatectomy
                result = "1202";
                break;
            }
            case "12PD": {//Penis Procedures
                result = "1204";
                break;
            }
            case "12PF": {//Other Male Reproductive System OR Procedures
                result = MalignantCount > 0 ? "1206" : "1207";
                break;
            }
            case "12PC": {  //Testis Procedures
                result = "1203";
                break;
            }
            case "12PG": { //1Cystourethroscopy
                result = "1208";
                break;
            }
            case "12PE": {//Circumcision 12PE
                result = "1205";
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
            final Integer PBX12Proc,
            final Integer PBX99Proc) {
        String dc = "";
        switch (pdc.toUpperCase()) {
            case "12A": {
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "1255";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "1256";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "1257";
                } else if (PBX12Proc > 0) { //##Dx Procedure
                    dc = "1258";
                    //Radiotherapy
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "1259";
                } else {
                    dc = "1250";
                }
                break;
            }
            case "12B": { //#Benign prostatic hypertrophy
                dc = "1251";
                break;
            }
            case "12C": { //#Inflammation of male reproductive system
                dc = "1252";
                break;
            }
            case "12D": { //#Inflammation of male reproductive system
                dc = "1253";
                break;
            }
            case "12E": {//##Other male reproductive system diagnoses 12E
                dc = "1254";
                break;
            }
        }
        return dc;

    }

}
