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

    public DRGWSResult GetMDC14(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            AX checkAX = new AX();
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
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                    DRGWSResult ResultUnralated = new UnralatedANDORProc().UnralatedANDORProc(datasource,
                            ProcedureList.get(y).trim(), drgResult.getMDC());
                    if (!ResultUnralated.isSuccess()) {
                        UnralatedORProcedure++;
                    }
                }
                if (checkAX.AX(datasource, "14PFX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter14PFX++;
                }
                if (checkAX.AX(datasource, "14PGX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter14PGX++;
                }
                if (checkAX.AX(datasource, "14PJX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter14PJX++;
                }
                if (checkAX.AX(datasource, "14PEX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter14PEX++;
                }
                if (checkAX.AX(datasource, "14PBX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter14PBX++;
                }
                if (checkAX.AX(datasource, "14PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter14PCX++;
                }
                if (checkAX.AX(datasource, "14PHX", ProcedureList.get(y).trim()).isSuccess()) {
//                DRGWSResult NonORProcedure =  new ORProcedure().ORProcedure(datasource, Result14PHX.getResult());
//                if (NonORProcedure.isSuccess()) {
                    Counter14PHX++;
                    //}
                }
                DRGWSResult Result14PDX = checkAX.AX(datasource, "14PDX", ProcedureList.get(y).trim());
                if (Result14PDX.isSuccess()) {
                    Counter14PDX++;
                }

            }
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
            if (checkAX.AX(datasource, "14KX", grouperparameter.getPdx().trim()).isSuccess()) {
                Counter14KX++;
            }

            for (int a = 0; a < SecondaryList.size(); a++) {
                if (checkAX.AX(datasource, "14EX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter14EX++;
                }
                if (checkAX.AX(datasource, "14DX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter14DX++;
                }
                if (checkAX.AX(datasource, "14CX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter14CX++;
                }
                if (checkAX.AX(datasource, "14BX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter14BX++;
                }
                if (checkAX.AX(datasource, "14HX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter14HX++;
                }
                if (checkAX.AX(datasource, "14FX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter14FX++;
                }
                if (new PDXandMDC().PDXandMDC(datasource, SecondaryList.get(a).trim(), drgResult.getMDC()).isSuccess()) {
                    ICD10mdcCounter++;
                }
                if (checkAX.AX(datasource, "14GX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter14GX++;
                }
                if (checkAX.AX(datasource, "14JX", SecondaryList.get(a).trim()).isSuccess()) {
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

            if (checkAX.AX(datasource, "14CX", grouperparameter.getPdx()).isSuccess()) {
                pdxax14cx++;
            }
            if (checkAX.AX(datasource, "14DX", grouperparameter.getPdx()).isSuccess()) {
                pdxax14dx++;
            }

            if (checkAX.AX(datasource, "14FX", grouperparameter.getPdx().trim()).isSuccess()) {
                dxas14Fx++;
            }
            if (checkAX.AX(datasource, "14BX", grouperparameter.getPdx()).isSuccess()) {
                pdxax14bx++;
            }
            if (checkAX.AX(datasource, "14HX", grouperparameter.getPdx()).isSuccess()) {
                dxax14Hx++;
            }
            if (checkAX.AX(datasource, "14GX", grouperparameter.getPdx()).isSuccess()) {
                dxax14Gx++;
            }
            if (checkAX.AX(datasource, "14JX", grouperparameter.getPdx()).isSuccess()) {
                dxax14Jx++;
            }
            //PDC 14D
            int D14Counter = 0;
            int E14Counter = 0;
            int G14Counter = 0;
            int H14Counter = 0;
            int K14Counter = 0;
            int L14Counter = 0;
            int J14Counter = 0;

            if (new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "14D").isSuccess()) {
                D14Counter++;
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "14E").isSuccess()) {
                E14Counter++;
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "14G").isSuccess()) {
                G14Counter++;
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "14H").isSuccess()) {
                H14Counter++;
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "14K").isSuccess()) {
                K14Counter++;
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "14L").isSuccess()) {
                L14Counter++;
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, grouperparameter.getPdx(), "14J").isSuccess()) {
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
                                DRGWSResult sdxfinderResult = checkAX.AX(datasource, "14BX", SecondaryList.get(x).trim());
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
                                DRGWSResult sdxfinderResult = checkAX.AX(datasource, "14EX", SecondaryList.get(x));
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
                            DRGWSResult sdxfinderResult = checkAX.AX(datasource, "14BX", SecondaryList.get(x));
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
                            String PDxPDC = new GetPDCUsePDx().GetPDCUsePDx(datasource, grouperparameter.getPdx());
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
                        //  String sdxfinalList =  new GrouperMethod().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                        String sdxfinalList = new CleanSDxDCDeterminationPLSQL().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                        DRGWSResult getpcclvalue = new GetPCCL().GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                        if (getpcclvalue.isSuccess()) {
                            DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                            drgResult.setPrepccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
                            drgResult.setFinalpccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
                            drgResult.setDRG(finaldrgresult.getDRG());
                            if (checkDRG.DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
                                drgResult.setDRGName(checkDRG.DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).getMessage());
                            } else {
                                DRGWSResult drgvalues = new ValidatePCCL().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG());
                                if (drgvalues.isSuccess()) {
                                    String drgcode = drgResult.getDC() + drgvalues.getResult();
                                    drgResult.setDRG(drgcode);
                                    DRGWSResult drgnames = checkDRG.DRG(datasource, drgResult.getDC(), drgcode);
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
                        if (Counter14CX > 1) {
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
                    DRGWSResult checkResult = checkDRG.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                    if (checkResult.isSuccess()) {
                        drgResult.setDRGName(checkResult.getMessage());
                    } else {
                        drgResult.setDRGName("DRG code grouper provide not exist in the library");
                    }
                }
            } else {
                DRGWSResult checkResult = checkDRG.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
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
}
