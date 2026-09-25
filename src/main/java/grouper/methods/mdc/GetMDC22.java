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
import grouper.structures.MDCCodeOptimize;
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
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            AX checkAX = new AX();
            Endovasc endO = new Endovasc();
            PDxMalignancy pdxMalignant = new PDxMalignancy();
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
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
                String procS = ProcedureList.get(x).trim();
                PDXCounter99 += checkAX.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += checkAX.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                Counter22PA += endO.Endovasc(datasource, SchemaName, procS, "22PA", mdcWithoutZeros).isSuccess() ? 1 : 0;
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                Counter22BXSDx += checkAX.AX(datasource, SchemaName, "22BX", sdxCode).isSuccess() ? 1 : 0;
                Counter22ASDx += pdxMalignant.PDxMalignancy(datasource, SchemaName, sdxCode, "22A").isSuccess() ? 1 : 0;
                Counter22BSDx += pdxMalignant.PDxMalignancy(datasource, SchemaName, sdxCode, "22B").isSuccess() ? 1 : 0;
            }
            Counter22BXPDx += checkAX.AX(datasource, SchemaName, "22BX", grouperparameter.getPdx()).isSuccess() ? 1 : 0;
            Counter22APDx += pdxMalignant.PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "22A").isSuccess() ? 1 : 0;
            Counter22BPDx += pdxMalignant.PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "22B").isSuccess() ? 1 : 0;
            if (PDXCounter99 > 0) {
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    if (Counter22ASDx > 0 || Counter22APDx > 0) {
                        drgResult.setDC(Counter22PA > 0 ? "2201" : "2250");
                        if (Counter22APDx == 0 && Counter22ASDx > 0) {
                            SecondaryList.stream()
                                    .map(String::trim)
                                    .filter(sdx -> pdxMalignant.PDxMalignancy(datasource, SchemaName, sdx, "22A").isSuccess())
                                    .findFirst()
                                    .ifPresent(drgResult::setSDXFINDER);
                        }
                    } else {
                        MDCCodeOptimize getDC = this.processDc(
                                Counter22BSDx,
                                Counter22BPDx,
                                Counter22PA,
                                Counter22BXPDx,
                                Counter22BXSDx,
                                SecondaryList,
                                datasource,
                                SchemaName);
                        if (!getDC.getSdxfinder().isEmpty()) {
                            drgResult.setSDXFINDER(getDC.getSdxfinder());
                        }
                        drgResult.setDC(getDC.getDC());
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "2203" : "2204");
                }
            } else {
                if (Counter22ASDx > 0 || Counter22APDx > 0) {
                    drgResult.setDC(Counter22PA > 0 ? "2201" : "2250");
                    if (Counter22APDx == 0 && Counter22ASDx > 0) {
                        SecondaryList.stream()
                                .map(String::trim)
                                .filter(sdx -> pdxMalignant.PDxMalignancy(datasource, SchemaName, sdx, "22A").isSuccess())
                                .findFirst()
                                .ifPresent(drgResult::setSDXFINDER);
                    }
                } else {
                    MDCCodeOptimize getDC = this.processDc(
                            Counter22BSDx,
                            Counter22BPDx,
                            Counter22PA,
                            Counter22BXPDx,
                            Counter22BXSDx,
                            SecondaryList,
                            datasource,
                            SchemaName);
                    if (!getDC.getSdxfinder().isEmpty()) {
                        drgResult.setSDXFINDER(getDC.getSdxfinder());
                    }
                    drgResult.setDC(getDC.getDC());
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
        } catch (NumberFormatException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC22.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    private MDCCodeOptimize processDc(
            final Integer Counter22BSDx,
            final Integer Counter22BPDx,
            final Integer Counter22PA,
            final Integer Counter22BXPDx,
            final Integer Counter22BXSDx,
            final List<String> SecondaryList,
            final DataSource datasource,
            final String SchemaName) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setSdxfinder("");
        AX checkAX = new AX();
        if (Counter22BSDx > 0 || Counter22BPDx > 0) {
            boolean has22Condition = Counter22PA > 0 || Counter22BXPDx > 0 || Counter22BXSDx > 0;
            if (has22Condition) {
                if (Counter22BXPDx == 0 && Counter22BXSDx > 0) {
                    SecondaryList.stream()
                            .map(String::trim)
                            .filter(sdxCode -> checkAX.AX(datasource, SchemaName, "22BX", sdxCode).isSuccess())
                            .findFirst()
                            .ifPresent(result::setSdxfinder);
                }
            }
            result.setDC(has22Condition ? "2202" : "2251");
        } else {
            result.setDC("2252");
        }
        return result;
    }

}
