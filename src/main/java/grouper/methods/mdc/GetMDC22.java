/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.Endovasc;
import grouper.methods.validation.PDxMalignancy;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC22 {

    public GetMDC22() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC22(
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
//                if (utility.isValid99PDX(ProcedureList.get(x).trim())) {
//                    PDXCounter99++;
//                }
                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
                    PDXCounter99++;
                }
                //AX 99PCX Checking
//                if (utility.isValid99PCX(ProcedureList.get(x).trim())) {
//                    PCXCounter99++;
//                }
                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
                    PCXCounter99++;
                }
                if (new Endovasc().Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "22PA", mdcWithoutZeros).isSuccess()) {
                    Counter22PA++;
                }
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (checkAX.AX(datasource, SchemaName, "22BX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter22BXSDx++;
                }
                if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, SecondaryList.get(a).trim(), "22A").isSuccess()) {
                    Counter22ASDx++;
                }
                if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, SecondaryList.get(a).trim(), "22B").isSuccess()) {
                    Counter22BSDx++;
                }
            }

            if (checkAX.AX(datasource, SchemaName, "22BX", grouperparameter.getPdx()).isSuccess()) {
                Counter22BXPDx++;
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "22A").isSuccess()) {
                Counter22APDx++;
            }
            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "22B").isSuccess()) {
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
                                    if (checkAX.AX(datasource, SchemaName, "22BX", SecondaryList.get(x).trim()).isSuccess()) {
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
                                if (checkAX.AX(datasource, SchemaName, "22BX", SecondaryList.get(x).trim()).isSuccess()) {
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

//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 18 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (Exception ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC22.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
