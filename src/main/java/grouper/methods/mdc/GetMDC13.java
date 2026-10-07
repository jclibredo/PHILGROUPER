/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.GetPDC;
import grouper.methods.validation.GetPDCUsePDx;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC13 {

    public GetMDC13() {
    }
//    private final Logger logger = (Logger) LogManager.getLogger(GetMDC13.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC13(
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
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            AX checkAX = new AX();
            GetPDC getPdc = new GetPDC();
            ORProcedure orProcs = new ORProcedure();
            MDCProcedureMethod mdcProcs = new MDCProcedureMethod();
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter13PBX = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                CartProc += checkAX.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += checkAX.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += checkAX.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                Counter13PBX += checkAX.AX(datasource, SchemaName, "13PBX", procS).isSuccess() ? 1 : 0;
                DRGWSResult ORProcedureResult = orProcs.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
                DRGWSResult JoinResult = mdcProcs.MDCProcedure(datasource,
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
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                CartSDx += checkAX.AX(datasource, SchemaName, "99BX", sdxCode).isSuccess() ? 1 : 0;
                CaCRxSDx += checkAX.AX(datasource, SchemaName, "99CX", sdxCode).isSuccess() ? 1 : 0;
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 13
            int max = Optional.ofNullable(ORProcedureCounterList)
                    .flatMap(list -> list.stream().filter(Objects::nonNull).max(Integer::compareTo))
                    .orElse(0);
//             pdclist.sort(Collections.reverseOrder());
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    if (mdcprocedureCounter > 0) {
                        String PDxPDC = new GetPDCUsePDx().GetPDCUsePDx(datasource, SchemaName, grouperparameter.getPdx());
                        int min = hierarvalue.get(0);
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        drgResult.setDC(this.mdcProcedure(drgResult.getPDC(), PDxPDC));
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(max));
                    } else {
                        MDCCodeOptimize dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                Counter13PBX,
                                PBX99Proc,
                                SecondaryList,
                                SchemaName,
                                datasource);
                        drgResult.setDC(dc.getDC());
                        if (!dc.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(dc.getSdxfinder());
                        }
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "1318" : "1319");
                }
            } else if (mdcprocedureCounter > 0) {
                String PDxPDC = new GetPDCUsePDx().GetPDCUsePDx(datasource, SchemaName, grouperparameter.getPdx());
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setDC(this.mdcProcedure(drgResult.getPDC(), PDxPDC));
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(max));
            } else {
                MDCCodeOptimize dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter13PBX,
                        PBX99Proc,
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
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 13 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong " + ex.getMessage());
//            logger.info("Executing MDC13 Method");
//            logger.error("Error in MDC13 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    private String mdcProcedure(
            final String pdc,
            final String PDxPDC) {
        String result = "";
        switch (pdc.toUpperCase()) {
            case "13PJ": {//Pelvic Evisceration
                result = "1312";
                break;
            }
            case "13PA": {//Radical Hysterectomy and Radical Vulvectomy
                result = "1301";
                break;
            }
            case "13PK": { //Lap Uterine and Adnexal
                switch (PDxPDC.toUpperCase()) {
                    case "13A": //Other Malignancy
                        result = "1313";
                        break;
                    case "13B":  //CA in situ
                        result = "1314";
                        break;
                    case "13C": //Ovarian and Adnexal Malignancy
                        result = "1315";
                        break;
                    default://Non- Malignancy
                        result = "1316";
                        break;
                }
                break;
            }
            case "13PB": { //Uterine and Adnexal
                switch (PDxPDC.toUpperCase()) {
                    case "13A": //Other Malignancy
                        result = "1302";
                        break;
                    case "13B":  //CA in situ
                        result = "1303";
                        break;
                    case "13C": //Ovarian and Adnexal Malignancy
                        result = "1304";
                        break;
                    default: //Non- Malignancy
                        result = "1305";
                        break;

                }
                break;
            }
            case "13PH": {//Other Female Reproductive System OR Procedures
                result = "1311";
                break;
            }
            case "13PC": {//Female Reproductive System Reconstructive Procedures
                result = "1308";
                break;
            }
            case "13PF": {//Endoscopic Tubal Interruption
                result = "1310";
                break;
            }
            case "13PD": {//Vagina, Cervix and Vulva Procedures
                result = "1307";
                break;
            }
            case "13PE": {//Incisional Tubal Interruption
                result = "1306";
                break;
            }
            case "13PG": {//D&C 13PG
                result = "1309";
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
            final Integer Counter13PBX,
            final Integer PBX99Proc,
            final List<String> SecondaryList,
            final String SchemaName,
            final DataSource dataSource) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        switch (pdc.toUpperCase()) {
            //Radio+Chemotherapy
            case "13A": {//Admit for Renal Dialysis
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {  //Chemotherapy
                    result.setDC("1356");
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
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {  //Radiotherapy
                    result.setDC("1357");
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
                } else if (CartSDx > 0 && CartProc > 0) {
                    result.setDC("1358");
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
                } else if (Counter13PBX > 0) { //##Dx Procedure
                    result.setDC("1359");
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    result.setDC("1360");
                } else {//Malignancy 
                    result.setDC("1350");
                }
                break;
            }
            case "13B": {//Non Ovarian/Adnexal CA in situ
                result.setDC("1351");
                break;
            }
            case "13C": {//Ovarian/Adnexal Malignancy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {  //Chemotherapy
                    result.setDC("1361");
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
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) { //Radiotherapy
                    result.setDC("1362");
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
                } else if (CartSDx > 0 && CartProc > 0) {
                    result.setDC("1363");
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
                } else if (Counter13PBX > 0) { //##Dx Procedure
                    result.setDC("1364");
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    result.setDC("1365");
                } else {//Malignancy 
                    result.setDC("1352");
                }
                break;
            }
            case "13D": {//Lower Genitourinary Tract Infection
                result.setDC("1353");
                break;
            }
            case "13E": {//Female Pelvic Infection
                result.setDC("1354");
                break;
            }
            case "13F": {//Menstrual and Other Female Reproductive System Disorders PDC 13F
                result.setDC("1355");
                break;
            }
        }
        return result;

    }

}
