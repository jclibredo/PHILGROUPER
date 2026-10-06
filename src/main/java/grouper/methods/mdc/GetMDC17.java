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
import grouper.structures.MDCCodeOptimize;
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
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            AX getAx = new AX();
            MDCProcedureMethod mdcProc = new MDCProcedureMethod();
            Endovasc getEndo = new Endovasc();
            ORProcedure orProcedure = new ORProcedure();
            GetPDC getPdc = new GetPDC();
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
                DRGWSResult getOrProcResult = orProcedure.ORProcedure(datasource, SchemaName, procS);
                if (getOrProcResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(getOrProcResult.getResult()));
                }
                if (mdcProc.MDCProcedure(datasource, SchemaName,
                        procS,
                        mdcWithoutZeros,
                        grouperparameter.getGender()).isSuccess()) {
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(mdcProc.MDCProcedure(datasource, SchemaName,
                            procS,
                            mdcWithoutZeros,
                            grouperparameter.getGender()).getResult(), MDCProcedure.class);
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
                CartSDx += getAx.AX(datasource, SchemaName, "99BX", sdxCode).isSuccess() ? 1 : 0;
                CaCRxSDx += getAx.AX(datasource, SchemaName, "99CX", sdxCode).isSuccess() ? 1 : 0;
            }
//             pdclist.sort(Collections.reverseOrder());
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    MDCCodeOptimize dc = this.principalDaignosis(
                            drgResult.getPDC(),
                            ORProcedureCounter,
                            Counter17PA,
                            CartSDx,
                            CaCRxSDx,
                            CartProc,
                            CaCRxProc,
                            Counter17PBX,
                            PBX99Proc,
                            SecondaryList,
                            SchemaName,
                            datasource);
                    drgResult.setDC(dc.getDC());
                    if (!dc.getSdxfinder().isEmpty()) {
                        drgResult.setSDXFINDER(dc.getSdxfinder());
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "1705" : "1706");
                }
            } else {
                MDCCodeOptimize dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        ORProcedureCounter,
                        Counter17PA,
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter17PBX,
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

    private MDCCodeOptimize principalDaignosis(
            final String pdc,
            final Integer ORProcedureCounter,
            final Integer Counter17PA,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer Counter17PBX,
            final Integer PBX99Proc,
            final List<String> SecondaryList,
            final String SchemaName,
            final DataSource dataSource) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        switch (pdc.toUpperCase()) {
            case "17A"://Acute Leukemia
                if (ORProcedureCounter > 0) {
                    result.setDC(Counter17PA > 0 ? "1701" : "1703");
                } else {
                    //Radio+Chemotherapy
                    if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                        result.setDC("1756");
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
                        //Chemotherapy
                    } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                        result.setDC("1757");
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
                        //Radiotherapy
                    } else if (CartSDx > 0 && CartProc > 0) {
                        result.setDC("1758");
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
                    } else if (Counter17PBX > 0) { //##Dx Procedure
                        result.setDC("1759");
                    } else if (PBX99Proc > 0) {//Blood Transfusion
                        result.setDC("1760");
                    } else {//Malignancy 
                        result.setDC("1750");
                    }
                }
                break;
            case "17B"://Lymphoma & Non-acute Leukemia
                if (ORProcedureCounter > 0) {
                    result.setDC(Counter17PA > 0 ? "1701" : "1703");
                } else {
                    //Radio+Chemotherapy
                    if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                        result.setDC("1761");
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
                        //Chemotherapy
                    } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                        result.setDC("1762");
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
                        //Radiotherapy
                    } else if (CartSDx > 0 && CartProc > 0) {
                        result.setDC("1763");
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
                    } else if (Counter17PBX > 0) { //##Dx Procedure
                        result.setDC("1764");
                    } else if (PBX99Proc > 0) {//Blood Transfusion
                        result.setDC("1765");
                    } else {//Malignancy 
                        result.setDC("1751");
                    }
                }
                break;
            case "17C"://Other Neoplastic Disorders PDC 17C
                if (ORProcedureCounter > 0) {
                    result.setDC(Counter17PA > 0 ? "1702" : "1704");
                } else {
                    //Radio+Chemotherapy
                    if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                        result.setDC("1766");
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
                        //Chemotherapy
                    } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                        result.setDC("1767");
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
                        //Radiotherapy
                    } else if (CartSDx > 0 && CartProc > 0) {
                        result.setDC("1768");
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
                    } else if (Counter17PBX > 0) { //##Dx Procedure
                        result.setDC("1769");
                    } else if (PBX99Proc > 0) {//Blood Transfusion
                        result.setDC("1770");
                    } else {//Malignancy 
                        result.setDC("1752");
                    }
                }
                break;
        }
        return result;

    }

}
