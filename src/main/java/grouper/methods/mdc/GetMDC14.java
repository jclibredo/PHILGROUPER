/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class GetMDC14 {

    public GetMDC14() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC14(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        //14PDX Procedure Codes
        String PDX14 = "14PDX";
        String PEX14 = "14PEX";
        String PBX14 = "14PBX";
        String PHX14 = "14PHX";
        String CX14 = "14CX";
        String DX14 = "14DX";

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
        ArrayList<String> sdxfinder = new ArrayList<>();
        ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
        for (int y = 0; y < ProcedureList.size(); y++) {
            String procData = ProcedureList.get(y);
            DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, procData.trim());
            if (ORProcedureResult.isSuccess()) {
                ORProcedureCounter++;
                ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                DRGWSResult ResultUnralated = gm.UnralatedANDORProc(datasource,
                        procData.trim(), drgResult.getMDC());
                if (!ResultUnralated.isSuccess()) {
                    UnralatedORProcedure++;
                }
            }
            DRGWSResult Result14PFX = gm.AX(datasource, "14PFX", procData.trim());
            if (Result14PFX.isSuccess()) {
                Counter14PFX++;
            }
            DRGWSResult Result14PGX = gm.AX(datasource, "14PGX", procData.trim());
            if (Result14PGX.isSuccess()) {
                Counter14PGX++;
            }
            DRGWSResult Result14PJX = gm.AX(datasource, "14PJX", procData.trim());
            if (Result14PJX.isSuccess()) {
                Counter14PJX++;
            }
            DRGWSResult Result14PEX = gm.AX(datasource, PEX14, procData.trim());
            if (Result14PEX.isSuccess()) {
                Counter14PEX++;
            }
            DRGWSResult Result14PBX = gm.AX(datasource, PBX14, procData.trim());
            if (Result14PBX.isSuccess()) {
                Counter14PBX++;
            }
            DRGWSResult Result14PCX = gm.AX(datasource, "14PCX", procData.trim());
            if (Result14PCX.isSuccess()) {
                Counter14PCX++;
            }
            DRGWSResult Result14PHX = gm.AX(datasource, PHX14, procData.trim());
            if (Result14PHX.isSuccess()) {
//                DRGWSResult NonORProcedure = gm.ORProcedure(datasource, Result14PHX.getResult());
//                if (NonORProcedure.isSuccess()) {
                Counter14PHX++;
                //}
            }
            DRGWSResult Result14PDX = gm.AX(datasource, PDX14, procData.trim());
            if (Result14PDX.isSuccess()) {
                Counter14PDX++;
            }

        }
        // SDx Related AX Code
        String BX14 = "14BX";
        String EX14 = "14EX";
        String KX14 = "14KX";
        String FX14 = "14FX";
        String HX14 = "14HX";
        String GX14 = "14GX";
        String JX14 = "14JX";
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
        DRGWSResult Result14KX = gm.AX(datasource, KX14, grouperparameter.getPdx().trim());
        if (Result14KX.isSuccess()) {
            Counter14KX++;
        }

        for (int a = 0; a < SecondaryList.size(); a++) {
            String SDxAxData = SecondaryList.get(a);

            DRGWSResult Result14EX = gm.AX(datasource, EX14, SDxAxData.trim());
            if (Result14EX.isSuccess()) {
                Counter14EX++;
            }
            DRGWSResult Result14DX = gm.AX(datasource, DX14, SDxAxData.trim());
            if (Result14DX.isSuccess()) {
                Counter14DX++;
            }
            DRGWSResult Result14CX = gm.AX(datasource, CX14, SDxAxData.trim());
            if (Result14CX.isSuccess()) {
                Counter14CX++;
            }
            DRGWSResult Result14BX = gm.AX(datasource, BX14, SDxAxData.trim());
            if (Result14BX.isSuccess()) {
                Counter14BX++;
            }
            DRGWSResult Result14HX = gm.AX(datasource, HX14, SDxAxData.trim());
            if (Result14HX.isSuccess()) {
                Counter14HX++;
            }
            DRGWSResult Result14FX = gm.AX(datasource, FX14, SDxAxData.trim());
            if (Result14FX.isSuccess()) {
                Counter14FX++;
            }
            DRGWSResult pdxResult = gm.PDXandMDC(datasource, SDxAxData.trim(), drgResult.getMDC());
            if (pdxResult.isSuccess()) {
                ICD10mdcCounter++;
            }

            DRGWSResult SDX14gxresult = gm.AX(datasource, GX14, SDxAxData.trim());
            if (SDX14gxresult.isSuccess()) {
                Counter14GX++;
            }

            DRGWSResult SDX14jxresult = gm.AX(datasource, JX14, SDxAxData.trim());
            if (SDX14jxresult.isSuccess()) {
                Counter14JX++;
            }

        }

        int pdxax14bx = 0;
        int pdxax14cx = 0;
        int pdxax14dx = 0;
        int dxax14Hx = 0;
        int dxax14Gx = 0;
        int dxax14Jx = 0;
        int dxas14Fx = 0;

        DRGWSResult pdxax14cxresult = gm.AX(datasource, CX14, grouperparameter.getPdx());
        if (pdxax14cxresult.isSuccess()) {
            pdxax14cx++;
        }
        DRGWSResult pdxax14dxresult = gm.AX(datasource, DX14, grouperparameter.getPdx());
        if (pdxax14dxresult.isSuccess()) {
            pdxax14dx++;
        }

        DRGWSResult pdxax14fxresult = gm.AX(datasource, FX14, grouperparameter.getPdx().trim());
        if (pdxax14fxresult.isSuccess()) {
            dxas14Fx++;
        }
        DRGWSResult pdxax14bxresult = gm.AX(datasource, BX14, grouperparameter.getPdx());
        if (pdxax14bxresult.isSuccess()) {
            pdxax14bx++;
        }
        DRGWSResult dxasax14hxresult = gm.AX(datasource, HX14, grouperparameter.getPdx());
        if (dxasax14hxresult.isSuccess()) {
            dxax14Hx++;
        }
        DRGWSResult dxasax14gxresult = gm.AX(datasource, GX14, grouperparameter.getPdx());
        if (dxasax14gxresult.isSuccess()) {
            dxax14Gx++;
        }
        DRGWSResult dxasax14jxresult = gm.AX(datasource, JX14, grouperparameter.getPdx());
        if (dxasax14jxresult.isSuccess()) {
            dxax14Jx++;
        }
        //PDC 14D
        String D14 = "14D";
        String E14 = "14E";
        String G14 = "14G";
        String H14 = "14H";
        String K14 = "14K";
        String L14 = "14L";
        String J14 = "14J";
        int D14Counter = 0;
        int E14Counter = 0;
        int G14Counter = 0;
        int H14Counter = 0;
        int K14Counter = 0;
        int L14Counter = 0;
        int J14Counter = 0;

        DRGWSResult D14Result = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), D14);
        if (D14Result.isSuccess()) {
            D14Counter++;
        }
        DRGWSResult E14Result = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), E14);
        if (E14Result.isSuccess()) {
            E14Counter++;
        }
        DRGWSResult G14Result = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), G14);
        if (G14Result.isSuccess()) {
            G14Counter++;
        }
        DRGWSResult H14Result = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), H14);
        if (H14Result.isSuccess()) {
            H14Counter++;
        }
        DRGWSResult K14Result = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), K14);
        if (K14Result.isSuccess()) {
            K14Counter++;
        }
        DRGWSResult L14Result = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), L14);
        if (L14Result.isSuccess()) {
            L14Counter++;
        }
        DRGWSResult J14Result = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), J14);
        if (J14Result.isSuccess()) {
            J14Counter++;
        }
        //----------------------------------------
        //AREA 3
        int area1 = dxax14Gx + Counter14GX;
        int area2 = dxax14Hx + Counter14HX;
        int area3 = Counter14JX + dxax14Jx;
        //----------------------------------------
        //AREA 3
        int area4 = dxax14Hx + Counter14HX;
        //----------------------------------------
        try {
            if (pdxax14bx > 0 && ICD10mdcCounter > 0) { //PDx as AX 14BX and SDx in this MDC
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //1
            } else if (Counter14PBX > 0 && dxax14Hx < 1 && Counter14HX < 1) { //Proc as AX 14PBX wo Dx as AX 14HX
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //2
            } else if (area4 > 0 && Counter14PBX < 1) { //Dx as AX 14HX wo Proc as AX 14PBX
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //3
            } else if (area1 > 0
                    && area2 > 0
                    && area3 < 1) { //Dx as AX 14GX and Dx as AX 14 HX wo Dx as AX 14JX
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //4
            } else if (D14Counter > 0 && Counter14BX > 0) { //PDx as PDC 14D and SDx as AX 14BX   HERE!!!!!
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //5
            } else if (E14Counter > 0 && Counter14BX > 0) { //PDx as PDC 14E and SDx as AX 14BX
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //6
            } else if (G14Counter > 0 && Counter14BX > 0) { //PDx as PDC 14G and SDx as AX 14BX
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //7
            } else if (H14Counter > 0 && Counter14BX > 0) { //PDx as PDC (14H or 14K or 14L) and SDx as AX 14BX
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //8
            } else if (L14Counter > 0 && Counter14BX > 0) { //PDx as PDC 14J and SDx as AX 14BX
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //9
            } else if (K14Counter > 0 && Counter14BX > 0) {
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //10
            } else if (J14Counter > 0 && Counter14BX > 0) {
                drgResult.setDRG("26529");
                drgResult.setDC("2652");
                //11
            } else {
                switch (drgResult.getPDC()) {
                    case "14A"://Labour and Delivery
                        //COTNINUE TO 1
                        if (Counter14PBX > 0) {
                            drgResult.setDC("1401");
                        } else if (Counter14PCX > 0) {
                            drgResult.setDC("1402");
                        } else if (Counter14PGX > 0) {
                            drgResult.setDC("1407");
                        } else if (Counter14PJX > 0) {
                            drgResult.setDC("1409");
                        } else if (UnralatedORProcedure > 0) {
                            switch (Collections.max(ORProcedureCounterList)) {// FIX HERE
                                case 1:
                                    drgResult.setDC("2601");
                                    break;
                                case 2:
                                    drgResult.setDC("2602");
                                    break;
                                case 3:
                                    drgResult.setDC("2603");
                                    break;
                                case 4:
                                    drgResult.setDC("2604");
                                    break;
                                case 5:
                                    drgResult.setDC("2605");
                                    break;
                                case 6:
                                    drgResult.setDC("2606");
                                    break;
                            }
                        } else {
                            if (Counter14PHX > 0) {
                                drgResult.setDC("1408");
                            } else {
                                drgResult.setDC("1450");
                            }
                        }
                        break;
                    case "14B"://Pregnancy
                        if (Counter14BX > 0) {
                            //COTNINUE TO 1
                            if (Counter14PBX > 0) {
                                drgResult.setDC("1401");
                            } else if (Counter14PCX > 0) {
                                drgResult.setDC("1402");
                            } else if (Counter14PGX > 0) {
                                drgResult.setDC("1407");
                            } else if (Counter14PJX > 0) {
                                drgResult.setDC("1409");
                            } else if (UnralatedORProcedure > 0) {
                                switch (Collections.max(ORProcedureCounterList)) {
                                    case 1:
                                        drgResult.setDC("2601");
                                        break;
                                    case 2:
                                        drgResult.setDC("2602");
                                        break;
                                    case 3:
                                        drgResult.setDC("2603");
                                        break;
                                    case 4:
                                        drgResult.setDC("2604");
                                        break;
                                    case 5:
                                        drgResult.setDC("2605");
                                        break;
                                    case 6:
                                        drgResult.setDC("2606");
                                        break;
                                }
                            } else {
                                if (Counter14PHX > 0) {
                                    drgResult.setDC("1408");
                                } else {
                                    drgResult.setDC("1450");
                                }
                            }
                            for (int x = 0; x < SecondaryList.size(); x++) {
                                DRGWSResult sdxfinderResult = gm.AX(datasource, BX14, SecondaryList.get(x));
                                if (sdxfinderResult.isSuccess()) {
                                    sdxfinder.add(SecondaryList.get(x));
                                }
                            }
                            if (!sdxfinder.isEmpty()) {
                                drgResult.setSDXFINDER(String.join(",", sdxfinder));
                            }
                        } else {
                            if (Counter14EX > 0) {
                                //COTNINUE TO 2
                                if (ORProcedureCounter > 0) {
                                    if (Counter14PDX > 0) {
                                        drgResult.setDRG("14049");
                                        drgResult.setDC("1404");
                                    } else {
                                        drgResult.setDRG("14039");
                                        drgResult.setDC("1403");
                                    }
                                } else {
                                    drgResult.setDRG("14519");
                                    drgResult.setDC("1451");
                                }
                            } else {
                                //COTNINUE TO 3
                                if (Counter14PDX > 0) {
                                    drgResult.setDRG("14049");
                                    drgResult.setDC("1404");
                                } else {
                                    if (dxas14Fx > 0 || Counter14FX > 0) {
                                        drgResult.setDRG("14521");
                                        drgResult.setDC("1452");
                                    } else {
                                        drgResult.setDRG("14520");
                                        drgResult.setDC("1452");
                                    }
                                }
                            }
                            for (int x = 0; x < SecondaryList.size(); x++) {
                                DRGWSResult sdxfinderResult = gm.AX(datasource, EX14, SecondaryList.get(x));
                                if (sdxfinderResult.isSuccess()) {
                                    sdxfinder.add(SecondaryList.get(x));
                                }
                            }
                            if (!sdxfinder.isEmpty()) {
                                drgResult.setSDXFINDER(String.join(",", sdxfinder));
                            }
                        }
                        break;
                    case "14C"://PP/Post Abort/Deli
                        if (Counter14BX > 0) {
                            //COTNINUE TO 1
                            if (Counter14PBX > 0) {
                                drgResult.setDC("1401");
                            } else if (Counter14PCX > 0) {
                                drgResult.setDC("1402");
                            } else if (Counter14PGX > 0) {
                                drgResult.setDC("1407");
                            } else if (Counter14PJX > 0) {
                                drgResult.setDC("1409");
                            } else if (UnralatedORProcedure > 0) {
                                switch (Collections.max(ORProcedureCounterList)) {
                                    case 1:
                                        drgResult.setDC("2601");
                                        break;
                                    case 2:
                                        drgResult.setDC("2602");
                                        break;
                                    case 3:
                                        drgResult.setDC("2603");
                                        break;
                                    case 4:
                                        drgResult.setDC("2604");
                                        break;
                                    case 5:
                                        drgResult.setDC("2605");
                                        break;
                                    case 6:
                                        drgResult.setDC("2606");
                                        break;
                                }
                            } else {
                                if (Counter14PHX > 0) {
                                    drgResult.setDC("1408");
                                } else {
                                    drgResult.setDC("1450");
                                }

                            }
                        } else {
                            //COTNINUE TO 2
                            if (ORProcedureCounter > 0) {
                                if (Counter14PDX > 0) {
                                    drgResult.setDRG("14049");
                                    drgResult.setDC("1404");
                                } else {
                                    drgResult.setDRG("14039");
                                    drgResult.setDC("1403");
                                }
                            } else {
                                drgResult.setDRG("14519");
                                drgResult.setDC("1451");
                            }
                        }

                        for (int x = 0; x < SecondaryList.size(); x++) {
                            DRGWSResult sdxfinderResult = gm.AX(datasource, BX14, SecondaryList.get(x));
                            if (sdxfinderResult.isSuccess()) {
                                sdxfinder.add(SecondaryList.get(x));
                            }
                        }
                        if (!sdxfinder.isEmpty()) {
                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
                        }
                        break;
                    case "14D"://PP/Post Abortion
                        //COTNINUE TO 2
                        if (ORProcedureCounter > 0) {
                            if (Counter14PDX > 0) {
                                drgResult.setDRG("14049");
                                drgResult.setDC("1404");
                            } else {
                                drgResult.setDRG("14039");
                                drgResult.setDC("1403");
                            }
                        } else {
                            drgResult.setDRG("14519");
                            drgResult.setDC("1451");
                        }
                        break;
                    case "14E"://Antenatal
                        //COTNINUE TO 3
                        if (Counter14PDX > 0) {
                            drgResult.setDRG("14049");
                            drgResult.setDC("1404");
                        } else {
                            if (dxas14Fx > 0 || Counter14FX > 0) {
                                drgResult.setDRG("14521");
                                drgResult.setDC("1452");
                            } else {
                                drgResult.setDRG("14520");
                                drgResult.setDC("1452");
                            }
                        }
                        break;
                    case "14F"://Ectopic Pregnancy
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
                    case "14G"://Threatened Abortion
                        drgResult.setDRG("14549");
                        drgResult.setDC("1454");
                        break;
                    case "14H"://Abortion
                    case "14K":
                    case "14L":

                        if (Counter14PFX > 0) {
                            drgResult.setDRG("14069");
                            drgResult.setDC("1406");
                        } else {
                            String PDxPDC = gm.GetPDCUsePDx(datasource, grouperparameter.getPdx());
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
                    case "14J"://False Labor 14J
                        drgResult.setDRG("14569");
                        drgResult.setDC("1456");
                        break;
                }
            }

            //PROCESS LAST DRG DIGIT
            // DC to DRG for DC 1401,1402,1407,1408,1409,1450
            if (drgResult.getDRG() == null) {
                String rest = drgResult.getDC().substring(0, 2);
                if (Integer.parseInt(rest) == 26) {
                    if (utility.isValidDCList(drgResult.getDC())) {
                        drgResult.setDRG(drgResult.getDC() + "9");
                    } else {
                        //----------------------------------------------------------------------
                        //  String sdxfinalList = gm.CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                        String sdxfinalList = gm.CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                        DRGWSResult getpcclvalue = gm.GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                        if (getpcclvalue.isSuccess()) {
                            DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                            String drgValue = finaldrgresult.getDRG();
                            DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgValue);
                            //-----------------------------------------------------------------------
                            if (drgname.isSuccess()) {
                                drgResult.setDRG(drgValue);
                                drgResult.setDRGName(drgname.getMessage());

                            } else {
                                DRGWSResult drgvalues = gm.ValidatePCCL(datasource, drgResult.getDC(), drgValue);
                                if (drgvalues.isSuccess()) {
                                    drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                    DRGWSResult drgnames = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                    drgResult.setDRGName(drgnames.getMessage());
                                } else {
                                    drgResult.setDRG(drgValue);
                                    drgResult.setDRGName("Grouper Error");
                                }
                            }
                        } else {
                            drgResult.setDRG("00000");
                            drgResult.setDRGName("Grouper Error");
                        }
                    }

                } else {
                    // PROCESS LAST DRG DIGIT
                    if (pdxax14cx > 0 || Counter14CX > 0) {
                        if (Counter14CX > 1) {
                            drgResult.setDRG(drgResult.getDC() + "3");
                        } else {
                            if (pdxax14dx > 0 || Counter14DX > 0) {
                                drgResult.setDRG(drgResult.getDC() + "3");
                            } else {
                                drgResult.setDRG(drgResult.getDC() + "2");
                            }
                        }
                    } else {
                        if (pdxax14dx > 0 || Counter14DX > 0) {
                            drgResult.setDRG(drgResult.getDC() + "1");
                        } else {
                            drgResult.setDRG(drgResult.getDC() + "0");
                        }
                    }

                    if (drgResult.getDRG().equals("26041")) {
                        drgResult.setDRG("26040");
                        drgResult.setDC("2604");
                        DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                        if (drgname.isSuccess()) {
                            drgResult.setDRGName(drgname.getMessage());
                        }
                    } else {
                        DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                        if (drgname.isSuccess()) {
                            drgResult.setDRGName(drgname.getMessage());
                        }
                    }
                    //=======================================
                }

                result.setSuccess(true);
            } else {
                result.setSuccess(true);
                DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                if (drgname.isSuccess()) {
                    drgResult.setDRGName(drgname.getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }
            result.setMessage("MDC 14 Done Checking");
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC04.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
}
