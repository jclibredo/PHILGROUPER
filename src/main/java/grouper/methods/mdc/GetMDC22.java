///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package grouper.methods.mdc;
//
//import grouper.methods.validation.AX;
//import grouper.methods.validation.Endovasc;
//import grouper.methods.validation.PDxMalignancy;
//import grouper.structures.DRGOutput;
//import grouper.structures.DRGWSResult;
//import grouper.structures.GrouperParameter;
//import grouper.structures.MDCCodeOptimize;
//import grouper.utility.Utility;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.enterprise.context.RequestScoped;
//import javax.sql.DataSource;
//
///**
// *
// * @author MINOSUN
// */
//@RequestScoped
//public class GetMDC22 {
//
//    public GetMDC22() {
//    }
//
//    private final Utility utility = new Utility();
//
//    public DRGWSResult GetMDC22(
//            final DataSource datasource,
//            final String SchemaName,
//            final DRGOutput drgResult,
//            final GrouperParameter grouperparameter) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        int mdcAsInt = Integer.parseInt(drgResult.getMDC());
//        String mdcWithoutZeros = String.valueOf(mdcAsInt);
//        try {
//            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
//            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
//            AX checkAX = new AX();
//            ArrayList<String> sdxfinder = new ArrayList<>();
//            int PDXCounter99 = 0;
//            int PCXCounter99 = 0;
//            int Counter22PA = 0;
//            int Counter22BSDx = 0;
//            int Counter22BPDx = 0;
//            int Counter22BXSDx = 0;
//            int Counter22BXPDx = 0;
//            int Counter22APDx = 0;
//            int Counter22ASDx = 0;
//            for (int x = 0; x < ProcedureList.size(); x++) {
//                //AX 99PDX Checking
//                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
//                    PDXCounter99++;
//                }
//                //AX 99PCX Checking
//                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
//                    PCXCounter99++;
//                }
//                if (new Endovasc().Endovasc(datasource, SchemaName, ProcedureList.get(x).trim(), "22PA", mdcWithoutZeros).isSuccess()) {
//                    Counter22PA++;
//                }
//            }
//            for (int a = 0; a < SecondaryList.size(); a++) {
//                if (checkAX.AX(datasource, SchemaName, "22BX", SecondaryList.get(a).trim()).isSuccess()) {
//                    Counter22BXSDx++;
//                }
//                if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, SecondaryList.get(a).trim(), "22A").isSuccess()) {
//                    Counter22ASDx++;
//                }
//                if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, SecondaryList.get(a).trim(), "22B").isSuccess()) {
//                    Counter22BSDx++;
//                }
//            }
//
//            if (checkAX.AX(datasource, SchemaName, "22BX", grouperparameter.getPdx()).isSuccess()) {
//                Counter22BXPDx++;
//            }
//            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "22A").isSuccess()) {
//                Counter22APDx++;
//            }
//            if (new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "22B").isSuccess()) {
//                Counter22BPDx++;
//            }
//            if (PDXCounter99 > 0) {
//                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
//                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
//                    if (Counter22ASDx > 0 || Counter22APDx > 0) {
//                        if (Counter22PA > 0) {
//                            drgResult.setDC("2201");
//                        } else {
//                            drgResult.setDC("2250");
//                        }
//                    } else {
//                        MDCCodeOptimize getDC = this.processDc(
//                                Counter22BSDx,
//                                Counter22BPDx,
//                                Counter22PA,
//                                Counter22BXPDx,
//                                Counter22BXSDx,
//                                SecondaryList,
//                                datasource,
//                                SchemaName);
//                        if (!getDC.getSdxfinder().isEmpty()) {
//                            drgResult.setSDXFINDER(getDC.getSdxfinder());
//                        }
//                        drgResult.setDC(getDC.getDC());
//                    }
//                } else {
//                    if (PCXCounter99 > 0) {
//                        drgResult.setDC("2203");
//                    } else {
//                        drgResult.setDC("2204");
//                    }
//                }
//            } else {
//                if (Counter22ASDx > 0 || Counter22APDx > 0) {
//                    if (Counter22PA > 0) {
//                        drgResult.setDC("2201");
//                    } else {
//                        drgResult.setDC("2250");
//                    }
//                } else {
//                    MDCCodeOptimize getDC = this.processDc(
//                            Counter22BSDx,
//                            Counter22BPDx,
//                            Counter22PA,
//                            Counter22BXPDx,
//                            Counter22BXSDx,
//                            SecondaryList,
//                            datasource,
//                            SchemaName);
//                    if (!getDC.getSdxfinder().isEmpty()) {
//                        drgResult.setSDXFINDER(getDC.getSdxfinder());
//                    }
//                    drgResult.setDC(getDC.getDC());
//                }
//            }
//
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
////            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
//            if (getPCCLResult.isSuccess()) {
//                result.setSuccess(getPCCLResult.isSuccess());
//                result.setResult(getPCCLResult.getResult());
//                result.setMessage("MDC 18 Done Checking");
//            } else {
//                result = getPCCLResult;
//            }
//        } catch (Exception ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(GetMDC22.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//
//    }
//
//    private MDCCodeOptimize processDc(
//            final Integer Counter22BSDx,
//            final Integer Counter22BPDx,
//            final Integer Counter22PA,
//            final Integer Counter22BXPDx,
//            final Integer Counter22BXSDx,
//            final List<String> SecondaryList,
//            final DataSource datasource,
//            final String SchemaName) {
//        MDCCodeOptimize result = utility.MDCCodeOptimize();
//        result.setDC("");
//        result.setSdxfinder("");
//        AX checkAX = new AX();
//        ArrayList<String> sdxfinder = new ArrayList<>();
//        if (Counter22BSDx > 0 || Counter22BPDx > 0) {
//            if (Counter22PA > 0 || Counter22BXPDx > 0 || Counter22BXSDx > 0) {
//                for (int x = 0; x < SecondaryList.size(); x++) {
//                    if (checkAX.AX(datasource, SchemaName, "22BX", SecondaryList.get(x).trim()).isSuccess()) {
//                        sdxfinder.add(SecondaryList.get(x));
//                    }
//                }
//                if (!sdxfinder.isEmpty()) {
//                    result.setSdxfinder(String.join(",", sdxfinder));
//                }
//                result.setDC("2202");
//            } else {
//                result.setDC("2251");
//            }
//        } else {
//            result.setDC("2252");
//        }
//        return result;
//    }
//
//}


package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.Endovasc;
import grouper.methods.validation.PDxMalignancy;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.util.ArrayList;
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

    private static final Logger LOGGER = Logger.getLogger(GetMDC22.class.getName());

    private final Utility utility = new Utility();

    public GetMDC22() {
    }

    public DRGWSResult GetMDC22(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        // Kept outside the try, as in the original (NumberFormatException propagates)
        final String mdcWithoutZeros = String.valueOf(Integer.parseInt(drgResult.getMDC()));
        try {
            final String[] procedures = grouperparameter.getProc().split(",");
            final String[] secondaries = grouperparameter.getSdx().split(",");
            final String pdx = grouperparameter.getPdx(); // used untrimmed, as in the original

            // Created once instead of once per item
            final AX checkAX = new AX();
            final Endovasc endovasc = new Endovasc();
            final PDxMalignancy malignancy = new PDxMalignancy();

            boolean hasPDX99 = false; // AX 99PDX
            boolean hasPCX99 = false; // AX 99PCX
            boolean has22PA = false;  // Endovasc 22PA
            for (String rawProc : procedures) {
                final String proc = rawProc.trim();
                // Once a flag is true, further lookups for it can't change the outcome
                if (!hasPDX99 && checkAX.AX(datasource, SchemaName, "99PDX", proc).isSuccess()) {
                    hasPDX99 = true;
                }
                if (!hasPCX99 && checkAX.AX(datasource, SchemaName, "99PCX", proc).isSuccess()) {
                    hasPCX99 = true;
                }
                if (!has22PA && endovasc.Endovasc(datasource, SchemaName, proc, "22PA", mdcWithoutZeros).isSuccess()) {
                    has22PA = true;
                }
            }

            // FINDING DC IS HERE
            // The original ran every secondary/PDx lookup up front, then repeated the same
            // decision chain in two places. Now the chain is written once and lookups only
            // run when a branch actually needs them.
            if (hasPDX99 && utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(),
                    utility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
                drgResult.setDC(hasPCX99 ? "2203" : "2204");
            } else if (hasMalignancy(datasource, SchemaName, malignancy, "22A", pdx, secondaries)) {
                drgResult.setDC(has22PA ? "2201" : "2250");
            } else if (hasMalignancy(datasource, SchemaName, malignancy, "22B", pdx, secondaries)) {
                // Secondary Dx matching AX 22BX (untrimmed values, as in the original SDXFINDER)
                final List<String> bxSecondaries = new ArrayList<>();
                for (String sdx : secondaries) {
                    if (checkAX.AX(datasource, SchemaName, "22BX", sdx.trim()).isSuccess()) {
                        bxSecondaries.add(sdx);
                    }
                }
                if (has22PA || !bxSecondaries.isEmpty()
                        || checkAX.AX(datasource, SchemaName, "22BX", pdx).isSuccess()) {
                    if (!bxSecondaries.isEmpty()) {
                        drgResult.setSDXFINDER(String.join(",", bxSecondaries));
                    }
                    drgResult.setDC("2202");
                } else {
                    drgResult.setDC("2251");
                }
            } else {
                drgResult.setDC("2252");
            }

            // FINDING PCCL IS HERE
//            final DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            final DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 18 Done Checking"); // kept as-is; see note about the "18"
            } else {
                result = getPCCLResult;
            }
        } catch (Exception ex) {
            result.setMessage("Something went wrong");
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * True if the principal Dx or any secondary Dx matches the given
     * malignancy code (22A / 22B). Stops at the first match.
     */
    private static boolean hasMalignancy(
            final DataSource datasource,
            final String SchemaName,
            final PDxMalignancy malignancy,
            final String code,
            final String pdx,
            final String[] secondaries) {
        if (malignancy.PDxMalignancy(datasource, SchemaName, pdx, code).isSuccess()) {
            return true;
        }
        for (String sdx : secondaries) {
            if (malignancy.PDxMalignancy(datasource, SchemaName, sdx.trim(), code).isSuccess()) {
                return true;
            }
        }
        return false;
    }
}