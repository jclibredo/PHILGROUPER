/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.CleanSDxDCDeterminationPLSQL;
import grouper.methods.validation.DRG;
import grouper.methods.validation.GetPCCL;
import grouper.methods.validation.GetPDCUsePDx;
import grouper.methods.validation.ORProcedure;
import grouper.methods.validation.PDXandMDC;
import grouper.methods.validation.PDxMalignancy;
import grouper.methods.validation.UnralatedANDORProc;
import grouper.methods.validation.ValidatePCCL;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCCodeOptimize;
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
public class GetMDC14 {

    public GetMDC14() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC14.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC14(
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
            AX checkAX = new AX();
            PDXandMDC pdxMdc = new PDXandMDC();
            PDxMalignancy pdxMalignant = new PDxMalignancy();
            String PrimayDiag = grouperparameter.getPdx() != null ? grouperparameter.getPdx().trim() : "";
            //Procedure AX
            int Counter14PDX = 0;
            int Counter14PFX = 0;
            int Counter14PEX = 0;
            int Counter14PBX = 0;
            int Counter14PCX = 0;
            int Counter14PGX = 0;
            int Counter14PJX = 0;
            int Counter14PHX = 0;
            int ORProcedureCounter = 0;
            int UnralatedORProcedure = 0;
            // SDx Related AX Code
            int Counter14BX = 0;
            int Counter14EX = 0;
            int Counter14KX = 0;
            int Counter14DX = 0;
            int Counter14CX = 0;
            int Counter14FX = 0;
            int Counter14HX = 0;
            int Counter14GX = 0;
            int Counter14JX = 0;
            int ICD10mdcCounter = 0;
            //PRIMARY
            int pdxax14bx = 0;
            int pdxax14cx = 0;
            int pdxax14dx = 0;
            int dxax14Hx = 0;
            int dxax14Gx = 0;
            int dxax14Jx = 0;
            int dxas14Fx = 0;
            int D14Counter = 0;
            int E14Counter = 0;
            int G14Counter = 0;
            int H14Counter = 0;
            int K14Counter = 0;
            int L14Counter = 0;
            int J14Counter = 0;
            ORProcedure orProc = new ORProcedure();
            UnralatedANDORProc unrelated = new UnralatedANDORProc();
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procS = ProcedureList.get(y).trim();
                DRGWSResult ORProcedureResult = orProc.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                    DRGWSResult ResultUnralated = unrelated.UnralatedANDORProc(datasource, SchemaName,
                            procS, drgResult.getMDC());
                    if (!ResultUnralated.isSuccess()) {
                        UnralatedORProcedure++;
                    }
                }
                Counter14PFX += checkAX.AX(datasource, SchemaName, "14PFX", procS).isSuccess() ? 1 : 0;
                Counter14PGX += checkAX.AX(datasource, SchemaName, "14PGX", procS).isSuccess() ? 1 : 0;
                Counter14PJX += checkAX.AX(datasource, SchemaName, "14PJX", procS).isSuccess() ? 1 : 0;
                Counter14PEX += checkAX.AX(datasource, SchemaName, "14PEX", procS).isSuccess() ? 1 : 0;
                Counter14PBX += checkAX.AX(datasource, SchemaName, "14PBX", procS).isSuccess() ? 1 : 0;
                Counter14PCX += checkAX.AX(datasource, SchemaName, "14PCX", procS).isSuccess() ? 1 : 0;
                Counter14PHX += checkAX.AX(datasource, SchemaName, "14PHX", procS).isSuccess() ? 1 : 0;
                Counter14PDX += checkAX.AX(datasource, SchemaName, "14PDX", procS).isSuccess() ? 1 : 0;
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                Counter14KX += checkAX.AX(datasource, SchemaName, "14KX", sdxCode).isSuccess() ? 1 : 0;
                Counter14EX += checkAX.AX(datasource, SchemaName, "14EX", sdxCode).isSuccess() ? 1 : 0;
                Counter14DX += checkAX.AX(datasource, SchemaName, "14DX", sdxCode).isSuccess() ? 1 : 0;
                Counter14CX += checkAX.AX(datasource, SchemaName, "14CX", sdxCode).isSuccess() ? 1 : 0;
                Counter14BX += checkAX.AX(datasource, SchemaName, "14BX", sdxCode).isSuccess() ? 1 : 0;
                Counter14HX += checkAX.AX(datasource, SchemaName, "14HX", sdxCode).isSuccess() ? 1 : 0;
                Counter14FX += checkAX.AX(datasource, SchemaName, "14FX", sdxCode).isSuccess() ? 1 : 0;
                ICD10mdcCounter += pdxMdc.PDXandMDC(datasource, SchemaName, sdxCode, drgResult.getMDC()).isSuccess() ? 1 : 0;
                Counter14GX += checkAX.AX(datasource, SchemaName, "14GX", sdxCode).isSuccess() ? 1 : 0;
                Counter14JX += checkAX.AX(datasource, SchemaName, "14JX", sdxCode).isSuccess() ? 1 : 0;
            }
            pdxax14cx += checkAX.AX(datasource, SchemaName, "14CX", PrimayDiag).isSuccess() ? 1 : 0;
            pdxax14dx += checkAX.AX(datasource, SchemaName, "14DX", PrimayDiag).isSuccess() ? 1 : 0;
            dxas14Fx += checkAX.AX(datasource, SchemaName, "14FX", PrimayDiag).isSuccess() ? 1 : 0;
            pdxax14bx += checkAX.AX(datasource, SchemaName, "14BX", PrimayDiag).isSuccess() ? 1 : 0;
            dxax14Hx += checkAX.AX(datasource, SchemaName, "14HX", PrimayDiag).isSuccess() ? 1 : 0;
            dxax14Gx += checkAX.AX(datasource, SchemaName, "14GX", PrimayDiag).isSuccess() ? 1 : 0;
            dxax14Jx += checkAX.AX(datasource, SchemaName, "14JX", PrimayDiag).isSuccess() ? 1 : 0;
            D14Counter += pdxMalignant.PDxMalignancy(datasource, SchemaName, PrimayDiag, "14D").isSuccess() ? 1 : 0;
            E14Counter += pdxMalignant.PDxMalignancy(datasource, SchemaName, PrimayDiag, "14E").isSuccess() ? 1 : 0;
            G14Counter += pdxMalignant.PDxMalignancy(datasource, SchemaName, PrimayDiag, "14G").isSuccess() ? 1 : 0;
            H14Counter += pdxMalignant.PDxMalignancy(datasource, SchemaName, PrimayDiag, "14H").isSuccess() ? 1 : 0;
            K14Counter += pdxMalignant.PDxMalignancy(datasource, SchemaName, PrimayDiag, "14K").isSuccess() ? 1 : 0;
            L14Counter += pdxMalignant.PDxMalignancy(datasource, SchemaName, PrimayDiag, "14L").isSuccess() ? 1 : 0;
            J14Counter += pdxMalignant.PDxMalignancy(datasource, SchemaName, PrimayDiag, "14J").isSuccess() ? 1 : 0;
            //AREA 3
            int area1 = dxax14Gx + Counter14GX;
            int area2 = dxax14Hx + Counter14HX;
            int area3 = Counter14JX + dxax14Jx;
            //AREA 3
            int area4 = dxax14Hx + Counter14HX;
            boolean isMatch = (pdxax14bx > 0 && ICD10mdcCounter > 0)
                    || (Counter14PBX > 0 && dxax14Hx == 0 && Counter14HX == 0)
                    || (area4 > 0 && Counter14PBX == 0)
                    || (area1 > 0 && area2 > 0 && area3 == 0)
                    || (Counter14BX > 0 && (D14Counter > 0 || E14Counter > 0 || G14Counter > 0
                    || H14Counter > 0 || L14Counter > 0 || K14Counter > 0 || J14Counter > 0));
            if (isMatch) {
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
            } else {
                switch (drgResult.getPDC()) {
                    case "14A": {//Labour and Delivery
                        //COTNINUE TO 1
                        String dc = this.processOne(
                                Counter14PBX,
                                Counter14PCX,
                                Counter14PGX,
                                Counter14PJX,
                                UnralatedORProcedure,
                                Counter14PHX,
                                Collections.max(ORProcedureCounterList));
                        drgResult.setDC(dc);
                        break;
                    }
                    case "14B": {//Pregnancy
                        if (Counter14BX > 0) {
                            //COTNINUE TO 1
                            String dc = this.processOne(
                                    Counter14PBX,
                                    Counter14PCX,
                                    Counter14PGX,
                                    Counter14PJX,
                                    UnralatedORProcedure,
                                    Counter14PHX,
                                    Collections.max(ORProcedureCounterList));
                            drgResult.setDC(dc);
                        } else {
                            if (Counter14EX > 0) {
                                //COTNINUE TO 2
                                MDCCodeOptimize getResult = this.processTwo(ORProcedureCounter, Counter14PDX);
                                drgResult.setDRG(getResult.getDRG());
                                drgResult.setDC(getResult.getDC());
                            } else {
                                //COTNINUE TO 3
                                MDCCodeOptimize getResult = this.processThree(
                                        Counter14PDX,
                                        dxas14Fx,
                                        Counter14FX);
                                drgResult.setDRG(getResult.getDRG());
                                drgResult.setDC(getResult.getDC());
                            }
                        }

                        //SDX FINDER
                        if (Counter14BX > 0) {
                            drgResult.setSDXFINDER(this.sdxFinder(datasource, SchemaName, "14BX", SecondaryList));
                        }
                        break;
                    }
                    case "14C": {//PP/Post Abort/Deli
                        if (Counter14BX > 0) {
                            //COTNINUE TO 1
                            String dc = this.processOne(
                                    Counter14PBX,
                                    Counter14PCX,
                                    Counter14PGX,
                                    Counter14PJX,
                                    UnralatedORProcedure,
                                    Counter14PHX,
                                    Collections.max(ORProcedureCounterList));
                            drgResult.setDC(dc);
                        } else {
                            //COTNINUE TO 2
                            MDCCodeOptimize getResult = this.processTwo(ORProcedureCounter, Counter14PDX);
                            drgResult.setDRG(getResult.getDRG());
                            drgResult.setDC(getResult.getDC());
                        }
                        break;
                    }
                    case "14D": {//PP/Post Abortion
                        //COTNINUE TO 2
                        MDCCodeOptimize getResult = this.processTwo(ORProcedureCounter, Counter14PDX);
                        drgResult.setDRG(getResult.getDRG());
                        drgResult.setDC(getResult.getDC());
                        break;
                    }
                    case "14E": {//Antenatal
                        //COTNINUE TO 3
                        MDCCodeOptimize getResult = this.processThree(
                                Counter14PDX,
                                dxas14Fx,
                                Counter14FX);
                        drgResult.setDRG(getResult.getDRG());
                        drgResult.setDC(getResult.getDC());
                        //SDX FINDER
                        break;
                    }
                    case "14F": {//Ectopic Pregnancy
                        if (Counter14PDX > 0) {
                            if (Counter14KX > 0) {
                                drgResult.setDRG("14121");
                                drgResult.setDC("1412");
                            } else {
                                drgResult.setDRG("14120");
                                drgResult.setDC("1412");
                            }
                        } else {
                            drgResult.setDRG("14539");
                            drgResult.setDC("1453");
                        }
                        break;
                    }
                    case "14G": {//Threatened Abortion
                        drgResult.setDRG("14549");
                        drgResult.setDC("1454");
                        break;
                    }
                    case "14H"://Abortion
                    case "14K":
                    case "14L": {
                        if (Counter14PFX > 0) {
                            drgResult.setDRG("14069");
                            drgResult.setDC("1406");
                        } else {
                            String PDxPDC = new GetPDCUsePDx().GetPDCUsePDx(datasource, SchemaName, grouperparameter.getPdx());
                            switch (PDxPDC) {
                                case "14H"://Uncomplicated abortion
                                    if (Counter14PEX > 0) {
                                        drgResult.setDRG("14059");
                                        drgResult.setDC("1405");
                                    } else {
                                        drgResult.setDRG("14559");
                                        drgResult.setDC("1455");
                                    }
                                    break;
                                case "14K"://Complicated abortion
                                    if (Counter14PEX > 0) {
                                        drgResult.setDRG("14109");
                                        drgResult.setDC("1410");
                                    } else {
                                        drgResult.setDRG("14579");
                                        drgResult.setDC("1457");
                                    }

                                    break;
                                case "14L"://Molar Pregnancy PDC 14L
                                    if (Counter14PEX > 0) {
                                        drgResult.setDRG("14119");
                                        drgResult.setDC("1411");
                                    } else {
                                        drgResult.setDRG("14589");
                                        drgResult.setDC("1458");
                                    }
                                    break;
                            }

                        }
                        break;
                    }
                    case "14J": {//False Labor 14J
                        drgResult.setDRG("14569");
                        drgResult.setDC("1456");
                        break;
                    }
                }
            }
            //PROCESS LAST DRG DIGIT
            DRG checkDRG = new DRG();
            // 2. Call the service ONCE and store the result
            if (drgResult.getDRG() == null) {
                drgResult.setPrepccl("X");
                drgResult.setFinalpccl("X");
                drgResult.setDRGName("Grouper Error");
                drgResult.setDRG(drgResult.getDC() + "X");
                String rest = drgResult.getDC().substring(0, 2);
                if (Integer.parseInt(rest) == 26) {
                    if (utility.isValidDCList(drgResult.getDC())) {
                        drgResult.setDRG(drgResult.getDC() + "9");
                        drgResult.setPrepccl("9");
                        drgResult.setFinalpccl("9");
                    } else {
                        //String sdxfinalList = new CleanSDxDCDetermination().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                        String sdxfinalList = new CleanSDxDCDeterminationPLSQL().CleanSDxDCDeterminationPLSQL(datasource, SchemaName, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                        DRGWSResult getpcclvalue = new GetPCCL().GetPCCL(datasource, SchemaName, drgResult, grouperparameter, sdxfinalList);
                        if (getpcclvalue.isSuccess()) {
                            DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                            drgResult.setPrepccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
                            drgResult.setFinalpccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
                            drgResult.setDRG(finaldrgresult.getDRG());
                            if (checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
                                drgResult.setDRGName(checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), finaldrgresult.getDRG()).getMessage());
                            } else {
                                DRGWSResult drgvalues = new ValidatePCCL().ValidatePCCL(datasource, SchemaName, drgResult.getDC(), finaldrgresult.getDRG());
                                if (drgvalues.isSuccess()) {
                                    String drgcode = drgResult.getDC() + drgvalues.getResult();
                                    drgResult.setDRG(drgcode);
                                    DRGWSResult drgnames = checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), drgcode);
                                    if (drgnames.isSuccess()) {
                                        drgResult.setDRGName(drgnames.getMessage());
                                    }
                                    drgResult.setFinalpccl(drgcode.substring(drgcode.length() - 1));
                                } else {
                                    drgResult.setDRGName("DRG code grouper provide not exist in the library");
                                }
                            }
                        }
                    }
                } else {
                    // PROCESS LAST DRG DIGIT
                    if (pdxax14cx > 0 || Counter14CX > 0) {
                        drgResult.setPrepccl("3");
                        drgResult.setFinalpccl("3");
                        if ((Counter14CX + pdxax14cx) > 1) {
                            drgResult.setDRG(drgResult.getDC() + "3");
                        } else {
                            if (pdxax14dx > 0 || Counter14DX > 0) {
                                drgResult.setDRG(drgResult.getDC() + "3");
                            } else {
                                drgResult.setDRG(drgResult.getDC() + "2");
                                drgResult.setPrepccl("2");
                                drgResult.setFinalpccl("2");
                            }
                        }
                    } else {
                        if (pdxax14dx > 0 || Counter14DX > 0) {
                            drgResult.setDRG(drgResult.getDC() + "1");
                            drgResult.setPrepccl("1");
                            drgResult.setFinalpccl("1");
                        } else {
                            drgResult.setDRG(drgResult.getDC() + "0");
                            drgResult.setPrepccl("0");
                            drgResult.setFinalpccl("0");
                        }
                    }
                    // 1. Handle the specific edge case for "26041" first
                    if ("26041".equals(drgResult.getDRG())) {
                        drgResult.setDRG("26040");
                        drgResult.setDC("2604");
                    }
                    // 3. Use a ternary operator or a simple if/else for the result
                    DRGWSResult checkResult = checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), drgResult.getDRG());
                    if (checkResult.isSuccess()) {
                        drgResult.setDRGName(checkResult.getMessage());
                    } else {
                        drgResult.setDRGName("DRG code grouper provide not exist in the library");
                    }
                }
            } else {
                DRGWSResult checkResult = checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), drgResult.getDRG());
                if (checkResult.isSuccess()) {
                    drgResult.setDRGName(checkResult.getMessage());
                } else {
                    drgResult.setDRGName("DRG code grouper provide not exist in the library");
                }
            }
            result.setSuccess(true);
            result.setMessage("MDC 14 Done Checking");
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC14 Method");
            logger.error("Error in MDC14 Method : {}", ex.getMessage(), ex);
        }

        return result;
    }

    private String sdxFinder(
            final DataSource datasource,
            final String SchemaName,
            final String axCode,
            final List<String> SecondaryList) {
        String result = "";
        AX checkAX = new AX();
        for (int x = 0; x < SecondaryList.size(); x++) {
            String sdxCode = SecondaryList.get(x).trim();
            if (checkAX.AX(datasource, SchemaName, axCode, sdxCode).isSuccess()) {
                result = sdxCode;
                break;
            }
        }
        return result;
    }

    private MDCCodeOptimize processThree(
            final Integer Counter14PDX,
            final Integer dxas14Fx,
            final Integer Counter14FX) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setDRG("");
        if (Counter14PDX > 0) {
            result.setDRG("14049");
            result.setDC("1404");
        } else {
            if (dxas14Fx > 0 || Counter14FX > 0) {
                result.setDRG("14521");
                result.setDC("1452");
            } else {
                result.setDRG("14520");
                result.setDC("1452");
            }
        }
        return result;

    }

    private MDCCodeOptimize processTwo(
            final Integer ORProcedureCounter,
            final Integer Counter14PDX) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setDRG("");
        if (ORProcedureCounter > 0) {
            if (Counter14PDX > 0) {
                result.setDRG("14049");
                result.setDC("1404");
            } else {
                result.setDRG("14039");
                result.setDC("1403");
            }
        } else {
            result.setDRG("14519");
            result.setDC("1451");
        }
        return result;

    }

    private String processOne(
            final Integer Counter14PBX,
            final Integer Counter14PCX,
            final Integer Counter14PGX,
            final Integer Counter14PJX,
            final Integer UnralatedORProcedure,
            final Integer Counter14PHX,
            final Integer ORProcedureCounterList) {
        String dc = "";
        if (Counter14PBX > 0) {
            dc = "1401";
        } else if (Counter14PCX > 0) {
            dc = "1402";
        } else if (Counter14PGX > 0) {
            dc = "1407";
        } else if (Counter14PJX > 0) {
            dc = "1409";
        } else if (UnralatedORProcedure > 0) {
            dc = this.orProcedure(ORProcedureCounterList);
        } else {
            dc = Counter14PHX > 0 ? "1408" : "1450";
        }
        return dc;
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

}
