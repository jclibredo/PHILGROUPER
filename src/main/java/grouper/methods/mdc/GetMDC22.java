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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class GetMDC22 {

    public GetMDC22() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC22(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            ArrayList<String> sdxfinder = new ArrayList<>();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter22PA = 0;
            int Counter22BSDx = 0;
            int Counter22BPDx = 0;
            int Counter22BXSDx = 0;
            int Counter22BXPDx = 0;
            int Counter22APDx = 0;
            int Counter22ASDx = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                //AX 99PDX Checking
                if (utility.isValid99PDX(ProcedureList.get(x).trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(x).trim())) {
                    PCXCounter99++;
                }
                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(x).trim(), "22PA", drgResult.getMDC()).isSuccess()) {
                    Counter22PA++;
                }
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (new GrouperMethod().AX(datasource, "22BX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter22BXSDx++;
                }
                if (new GrouperMethod().PDxMalignancy(datasource, SecondaryList.get(a).trim(), "22A").isSuccess()) {
                    Counter22ASDx++;
                }
                if (new GrouperMethod().PDxMalignancy(datasource, SecondaryList.get(a).trim(), "22B").isSuccess()) {
                    Counter22BSDx++;
                }
            }

            if (new GrouperMethod().AX(datasource, "22BX", grouperparameter.getPdx()).isSuccess()) {
                Counter22BXPDx++;
            }
            if (new GrouperMethod().PDxMalignancy(datasource, grouperparameter.getPdx(), "22A").isSuccess()) {
                Counter22APDx++;
            }
            if (new GrouperMethod().PDxMalignancy(datasource, grouperparameter.getPdx(), "22B").isSuccess()) {
                Counter22BPDx++;
            }
            if (PDXCounter99 > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
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
                                    if (new GrouperMethod().AX(datasource, "22BX", SecondaryList.get(x).trim()).isSuccess()) {
                                        sdxfinder.add(SecondaryList.get(x).trim());
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
                                if (new GrouperMethod().AX(datasource, "22BX", SecondaryList.get(x).trim()).isSuccess()) {
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
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                } else {
                    //----------------------------------------------------------------------
                    //  String sdxfinalList =  new GrouperMethod().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    String sdxfinalList = new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        //-----------------------------------------------------------------------
                        if (new GrouperMethod().DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
                            drgResult.setDRG(finaldrgresult.getDRG());
                            drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).getMessage());
                        } else {
                            DRGWSResult drgvalues = new GrouperMethod().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG());
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                DRGWSResult drgnames = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                drgResult.setDRGName(drgnames.getMessage());
                            } else {
                                drgResult.setDRG(finaldrgresult.getDRG());
                                drgResult.setDRGName("Grouper Error");
                            }
                        }
                    } else {
                        drgResult.setDRG(drgResult.getDC() + "X");
                        drgResult.setDRGName("Grouper Error");
                    }
                }
                //----------------------------------------------------------------------
            } else {
                if (new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 22 Done Checking");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC22.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
