/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.DRGUtility;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class GetMDC22 {

    public GetMDC22() {
    }

    private final Utility utility = new Utility();
    private final DRGUtility drgutility = new DRGUtility();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGWSResult GetMDC22(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            ArrayList<String> sdxfinder = new ArrayList<>();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);
                //AX 99PDX Checking
                if (drgutility.isValid99PDX(proc)) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (drgutility.isValid99PCX(proc)) {
                    PCXCounter99++;
                }
            }
            String A22 = "22A";
            String PA22 = "22PA";
            String B22 = "22B";
            String BX22 = "22BX";
            String C22 = "22C";
            int Counter22PA = 0;
            int Counter22BSDx = 0;
            int Counter22BPDx = 0;
            int Counter22BXSDx = 0;
            int Counter22BXPDx = 0;
            int Counter22APDx = 0;
            int Counter22ASDx = 0;
            int Counter22C = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                String SeconSDx = SecondaryList.get(a);

                DRGWSResult ResultBX18 = gm.AX(datasource, BX22, SeconSDx);
                if (String.valueOf(ResultBX18.isSuccess()).equals("true")) {
                    Counter22BXSDx++;
                }
                DRGWSResult Result22ASDx = gm.PDxMalignancy(datasource, SeconSDx, A22);
                if (String.valueOf(Result22ASDx.isSuccess()).equals("true")) {
                    Counter22ASDx++;
                }
                DRGWSResult Result22BSDx = gm.PDxMalignancy(datasource, SeconSDx, B22);
                if (String.valueOf(Result22BSDx.isSuccess()).equals("true")) {
                    Counter22BSDx++;
                }
            }
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);

                DRGWSResult Result22PASDx = gm.Endovasc(datasource, proc, PA22, drgResult.getMDC());
                if (String.valueOf(Result22PASDx.isSuccess()).equals("true")) {
                    Counter22PA++;
                }
            }
            DRGWSResult ResultBX22 = gm.AX(datasource, BX22, grouperparameter.getPdx());
            if (String.valueOf(ResultBX22.isSuccess()).equals("true")) {
                Counter22BXPDx++;
            }
            DRGWSResult Result22APDx = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), A22);
            if (String.valueOf(Result22APDx.isSuccess()).equals("true")) {
                Counter22APDx++;
            }
            DRGWSResult Result22BPDx = gm.PDxMalignancy(datasource, grouperparameter.getPdx(), B22);
            if (String.valueOf(Result22BPDx.isSuccess()).equals("true")) {
                Counter22BPDx++;
            }
            if (PDXCounter99 > 0) {
                if (drgutility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        drgutility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), drgutility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (Counter22ASDx > 0 || Counter22APDx > 0) {
                        if (Counter22PA > 0) {
                            drgResult.setDC("2201");
                        } else {
                            drgResult.setDC("2250");
                        }
                    } else {
                        if (Counter22BSDx > 0 || Counter22BPDx > 0) {
                            if (Counter22PA > 0 || Counter22BXPDx > 0 || Counter22BXSDx > 0) {
                                for (int x = 0; x < SecondaryList.size(); x++) {
                                    DRGWSResult sdxfinderResult = gm.AX(datasource, BX22, SecondaryList.get(x));
                                    if (sdxfinderResult.isSuccess()) {
                                        sdxfinder.add(SecondaryList.get(x));
                                    }
                                }
                                if (!sdxfinder.isEmpty()) {
                                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                }
                                drgResult.setDC("2202");
                            } else {
                                drgResult.setDC("2251");
                            }
                        } else {
                            drgResult.setDC("2252");
                        }
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("2203");
                    } else {
                        drgResult.setDC("2204");
                    }
                }
            } else {
                if (Counter22ASDx > 0 || Counter22APDx > 0) {
                    if (Counter22PA > 0) {
                        drgResult.setDC("2201");
                    } else {
                        drgResult.setDC("2250");
                    }
                } else {
                    if (Counter22BSDx > 0 || Counter22BPDx > 0) {
                        if (Counter22PA > 0 || Counter22BXPDx > 0 || Counter22BXSDx > 0) {
                            for (int x = 0; x < SecondaryList.size(); x++) {
                                DRGWSResult sdxfinderResult = gm.AX(datasource, BX22, SecondaryList.get(x));
                                if (sdxfinderResult.isSuccess()) {
                                    sdxfinder.add(SecondaryList.get(x));
                                }
                            }
                            if (!sdxfinder.isEmpty()) {
                                drgResult.setSDXFINDER(String.join(",", sdxfinder));
                            }
                            drgResult.setDC("2202");
                        } else {
                            drgResult.setDC("2251");
                        }
                    } else {
                        drgResult.setDC("2252");
                    }
                }
            }

            if (drgResult.getDRG() == null) {

                //-------------------------------------------------------------------------------------
                if (drgutility.isValidDCList(drgResult.getDC())) {
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
                result.setSuccess(true);
                //----------------------------------------------------------------------
            } else {
                result.setSuccess(true);
                DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                if (drgname.isSuccess()) {
                    drgResult.setDRGName(drgname.getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }

            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 22 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC22.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
