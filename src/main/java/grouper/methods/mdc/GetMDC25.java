///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package grouper.methods.mdc;
//
//import grouper.methods.validation.AX;
//import grouper.methods.validation.GetPDC;
//import grouper.methods.validation.MDCProcedureMethod;
//import grouper.methods.validation.ORProcedure;
//import grouper.structures.DRGOutput;
//import grouper.structures.DRGWSResult;
//import grouper.structures.GrouperParameter;
//import grouper.structures.MDCProcedure;
//import grouper.structures.PDC;
//import grouper.utility.Utility;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
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
//public class GetMDC25 {
//
//    public GetMDC25() {
//    }
//
//    private final Utility utility = new Utility();
//
//    public DRGWSResult GetMDC25(
//            final DataSource datasource,
//            final String SchemaName,
//            final DRGOutput drgResult,
//            final GrouperParameter grouperparameter) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try {
//            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
//            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
//            AX checkAX = new AX();
//            //CHECKING FOR TRAUMA CODES
//            ArrayList<String> sdxfinder = new ArrayList<>();
//            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
//            String mdcWithoutZeros = String.valueOf(mdcAsInt);
//            int PDXCounter99 = 0;
//            int PCXCounter99 = 0;
////            int ORProcedureCounter = 0;
//            int mdcprocedureCounter = 0;
//            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
//            ArrayList<Integer> hierarvalue = new ArrayList<>();
//            ArrayList<String> pdclist = new ArrayList<>();
//            for (int x = 0; x < ProcedureList.size(); x++) {
//                //AX 99PDX Checking
//                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
//                    PDXCounter99++;
//                }
//                //AX 99PCX Checking
//                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
//                    PCXCounter99++;
//                }
//                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(x).trim());
//                if (ORProcedureResult.isSuccess()) {
////                    ORProcedureCounter++;
//                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
//                }
//                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, SchemaName,
//                        ProcedureList.get(x).trim(),
//                        mdcWithoutZeros,
//                        grouperparameter.getGender());
//                if (JoinResult.isSuccess()) {
//                    mdcprocedureCounter++;
//                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
//                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
//                    if (pdcresult.isSuccess()) {
//                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
//                        hierarvalue.add(hiarresult.getHIERAR());
//                        pdclist.add(hiarresult.getPDC());
//                    }
//                }
//
//            }
//
//            int Counter25BXSDx = 0;
//            int Counter25CXSDx = 0;
//            int Counter25DXSDx = 0;
//            int Counter25BXPDx = 0;
//            int Counter25DXPDx = 0;
//            int Counter25CXPDx = 0;
//            for (int a = 0; a < SecondaryList.size(); a++) {
//                String sdxCode = SecondaryList.get(a);
//                if (checkAX.AX(datasource, SchemaName, "25BX", sdxCode.trim()).isSuccess()) {
//                    Counter25BXSDx++;
//                }
//                if (checkAX.AX(datasource, SchemaName, "25CX", sdxCode.trim()).isSuccess()) {
//                    Counter25CXSDx++;
//                }
//                if (checkAX.AX(datasource, SchemaName, "25DX", sdxCode.trim()).isSuccess()) {
//                    Counter25DXSDx++;
//                }
//            }
//            if (checkAX.AX(datasource, SchemaName, "25BX", grouperparameter.getPdx().trim()).isSuccess()) {
//                Counter25BXPDx++;
//            }
//            if (checkAX.AX(datasource, SchemaName, "25CX", grouperparameter.getPdx().trim()).isSuccess()) {
//                Counter25CXPDx++;
//            }
//            if (checkAX.AX(datasource, SchemaName, "25DX", grouperparameter.getPdx()).isSuccess()) {
//                Counter25DXPDx++;
//            }
//
//            if (PDXCounter99 > 0) {//Trache-ostomy
//                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
//                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                        grouperparameter.getDischargeDate(),
//                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
//                    if (mdcprocedureCounter > 0) {
//                        String getDc = this.MDCProcedures(Collections.max(ORProcedureCounterList));
//                        drgResult.setDC(getDc);
//                    } else if (Counter25BXSDx > 0 || Counter25BXPDx > 0) {//HIV-related CNS Diseases
//                        for (int x = 0; x < SecondaryList.size(); x++) {
//                            if (checkAX.AX(datasource, SchemaName, "25BX", SecondaryList.get(x).trim()).isSuccess()) {
//                                sdxfinder.add(SecondaryList.get(x));
//                            }
//                        }
//                        if (!sdxfinder.isEmpty()) {
//                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
//                        }
//                        drgResult.setDC("2550");
//                    } else if (Counter25CXSDx > 0 || Counter25CXPDx > 0) {//HIV-related Malignancy
//                        for (int x = 0; x < SecondaryList.size(); x++) {
//                            if (checkAX.AX(datasource, SchemaName, "25CX", SecondaryList.get(x).trim()).isSuccess()) {
//                                sdxfinder.add(SecondaryList.get(x));
//                            }
//                        }
//                        if (!sdxfinder.isEmpty()) {
//                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
//                        }
//                        drgResult.setDC("2551");
//                    } else {
//                        if (Counter25DXSDx > 0 || Counter25DXPDx > 0) {//HIV-related Infection
//                            if (grouperparameter.getDischargeType().equals("4")) {
//                                drgResult.setDC("2554");
//                            } else {
//                                drgResult.setDC("2552");
//                            }
//                        } else {//Other HIV-related Condition
//                            drgResult.setDC("2553");
//                        }
//                        for (int x = 0; x < SecondaryList.size(); x++) {
//                            if (checkAX.AX(datasource, SchemaName, "25DX", SecondaryList.get(x).trim()).isSuccess()) {
//                                sdxfinder.add(SecondaryList.get(x).trim());
//                            }
//                        }
//                        if (!sdxfinder.isEmpty()) {
//                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
//                        }
//                    }
//                } else {
//                    if (PCXCounter99 > 0) {//Cont. Mech Vent 96+ hr
//                        drgResult.setDC("2508");
//                    } else {
//                        drgResult.setDC("2509");
//                    }
//                }
//            } else if (mdcprocedureCounter > 0) {
//                String getDc = this.MDCProcedures(Collections.max(ORProcedureCounterList));
//                drgResult.setDC(getDc);
//            } else if (Counter25BXSDx > 0 || Counter25BXPDx > 0) {//HIV-related CNS Diseases
//                for (int x = 0; x < SecondaryList.size(); x++) {
//                    if (checkAX.AX(datasource, SchemaName, "25BX", SecondaryList.get(x).trim()).isSuccess()) {
//                        sdxfinder.add(SecondaryList.get(x).trim());
//                    }
//                }
//                if (!sdxfinder.isEmpty()) {
//                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
//                }
//                drgResult.setDC("2550");
//            } else if (Counter25CXSDx > 0 || Counter25CXPDx > 0) {//HIV-related Malignancy
//                for (int x = 0; x < SecondaryList.size(); x++) {
//                    if (checkAX.AX(datasource, SchemaName, "25CX", SecondaryList.get(x).trim()).isSuccess()) {
//                        sdxfinder.add(SecondaryList.get(x).trim());
//                    }
//                }
//                if (!sdxfinder.isEmpty()) {
//                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
//                }
//                drgResult.setDC("2551");
//            } else {
//                if (Counter25DXSDx > 0 || Counter25DXPDx > 0) {//HIV-related Infection
//                    if (grouperparameter.getDischargeType().equals("4")) {//Transfer
//                        drgResult.setDC("2554");
//                    } else {//Other
//                        drgResult.setDC("2552");
//                    }
//                } else {//Other HIV-related Condition
//                    drgResult.setDC("2553");
//                }
//                for (int x = 0; x < SecondaryList.size(); x++) {
//                    if (checkAX.AX(datasource, SchemaName, "25DX", SecondaryList.get(x).trim()).isSuccess()) {
//                        sdxfinder.add(SecondaryList.get(x).trim());
//                    }
//                }
//                if (!sdxfinder.isEmpty()) {
//                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
//                }
//            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
////            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
//            if (getPCCLResult.isSuccess()) {
//                result.setSuccess(getPCCLResult.isSuccess());
//                result.setResult(getPCCLResult.getResult());
//                result.setMessage("MDC 25 Done Checking");
//            } else {
//                result = getPCCLResult;
//            }
//        } catch (NumberFormatException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(GetMDC25.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(GetMDC25.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//
//    }
//
//    private String MDCProcedures(final Integer ORProcedureCounterList) {
//        String dc = "";
//        switch (ORProcedureCounterList) {
//            case 6://OR Proc Level 6
//                dc = "2506";
//                break;
//            case 5://OR Proc Level 5
//                dc = "2505";
//                break;
//            case 4://OR Proc Level 4
//                dc = "2504";
//                break;
//            case 3://OR Proc Level 3
//                dc = "2503";
//                break;
//            case 2://OR Proc Level 2
//                dc = "2502";
//                break;
//            case 1://OR Proc Level 1
//                dc = "2501";
//                break;
//        }
//        return dc;
//    }
//
//}


package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.methods.validation.ORProcedure;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.util.ArrayList;
import java.util.Collections;
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
public class GetMDC25 {

    private static final Logger LOGGER = Logger.getLogger(GetMDC25.class.getName());

    private final Utility utility = new Utility();

    public GetMDC25() {
    }

    public DRGWSResult GetMDC25(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            final String[] procedures = grouperparameter.getProc().split(",");
            final String[] secondaries = grouperparameter.getSdx().split(",");
            final String mdcWithoutZeros = String.valueOf(Integer.parseInt(drgResult.getMDC()));

            // Created once instead of once per procedure
            final AX checkAX = new AX();
            final MDCProcedureMethod mdcProcedureMethod = new MDCProcedureMethod();

            boolean hasPDX99 = false;         // AX 99PDX (tracheostomy)
            boolean hasPCX99 = false;         // AX 99PCX (cont. mech vent 96+ hr)
            boolean hasMDCProcedure = false;  // MDC procedure match
            for (String rawProc : procedures) {
                final String proc = rawProc.trim();
                // Once a flag is true, further lookups for it can't change the outcome
                if (!hasPDX99 && checkAX.AX(datasource, SchemaName, "99PDX", proc).isSuccess()) {
                    hasPDX99 = true;
                }
                if (!hasPCX99 && checkAX.AX(datasource, SchemaName, "99PCX", proc).isSuccess()) {
                    hasPCX99 = true;
                }
                if (!hasMDCProcedure && mdcProcedureMethod.MDCProcedure(datasource, SchemaName,
                        proc,
                        mdcWithoutZeros,
                        grouperparameter.getGender()).isSuccess()) {
                    hasMDCProcedure = true;
                }
            }

            // FINDING DC IS HERE
            // The original wrote the MDC procedure / HIV chain twice (with and without 99PDX).
            // It is now written once, and lookups only run when a branch needs them.
            if (hasPDX99 && utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                    grouperparameter.getDischargeDate(),
                    utility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
                drgResult.setDC(hasPCX99 ? "2508" : "2509");
            } else if (hasMDCProcedure) {
                drgResult.setDC(this.orProcedureDc(this.highestORProcedureLevel(datasource, SchemaName, procedures)));
            } else {
                // Original quirk kept: when 99PDX was found (the LOS < 21 copy of the chain), the
                // 25BX/25CX SDXFINDER values were added untrimmed; otherwise they were trimmed.
                this.setHivDc(drgResult, grouperparameter, datasource, SchemaName, checkAX, secondaries, !hasPDX99);
            }

            // FINDING PCCL IS HERE
//            final DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            final DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 25 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (NumberFormatException ex) {
            result.setMessage("Something went wrong");
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /**
     * Highest OR procedure level across all procedures. Like the original,
     * throws NoSuchElementException (Collections.max) if no procedure has an
     * OR level, and NumberFormatException if a level is not numeric.
     */
    private Integer highestORProcedureLevel(
            final DataSource datasource,
            final String SchemaName,
            final String[] procedures) {
        final ORProcedure orProcedureMethod = new ORProcedure();
        final List<Integer> levels = new ArrayList<>();
        for (String rawProc : procedures) {
            final DRGWSResult orResult = orProcedureMethod.ORProcedure(datasource, SchemaName, rawProc.trim());
            if (orResult.isSuccess()) {
                levels.add(Integer.valueOf(orResult.getResult()));
            }
        }
        return Collections.max(levels);
    }

    private String orProcedureDc(final int orProcedureLevel) {
        // Levels 1..6 -> 2501..2506, anything else -> ""
        return (orProcedureLevel >= 1 && orProcedureLevel <= 6) ? "250" + orProcedureLevel : "";
    }

    /**
     * HIV-related DC (25BX -> 2550, 25CX -> 2551, else 25DX -> 2552/2553/2554)
     * and SDXFINDER. Each table is only scanned if the previous one didn't match.
     */
    private void setHivDc(
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter,
            final DataSource datasource,
            final String SchemaName,
            final AX checkAX,
            final String[] secondaries,
            final boolean trimBxCxSdx) {
        final String pdx = grouperparameter.getPdx();

        // HIV-related CNS Diseases
        final List<String> bx = matchingSecondaries(checkAX, datasource, SchemaName, "25BX", secondaries, trimBxCxSdx);
        if (!bx.isEmpty() || checkAX.AX(datasource, SchemaName, "25BX", pdx.trim()).isSuccess()) {
            setSdxFinder(drgResult, bx);
            drgResult.setDC("2550");
            return;
        }

        // HIV-related Malignancy
        final List<String> cx = matchingSecondaries(checkAX, datasource, SchemaName, "25CX", secondaries, trimBxCxSdx);
        if (!cx.isEmpty() || checkAX.AX(datasource, SchemaName, "25CX", pdx.trim()).isSuccess()) {
            setSdxFinder(drgResult, cx);
            drgResult.setDC("2551");
            return;
        }

        // HIV-related Infection (25DX), else Other HIV-related Condition.
        // The principal Dx is passed untrimmed here, as in the original.
        final List<String> dx = matchingSecondaries(checkAX, datasource, SchemaName, "25DX", secondaries, true);
        if (!dx.isEmpty() || checkAX.AX(datasource, SchemaName, "25DX", pdx).isSuccess()) {
            drgResult.setDC(grouperparameter.getDischargeType().equals("4") ? "2554" : "2552"); // 4 = Transfer
        } else {
            drgResult.setDC("2553");
        }
        setSdxFinder(drgResult, dx);
    }

    /** Secondary Dx that match the given AX table (trimmed or raw values for SDXFINDER). */
    private static List<String> matchingSecondaries(
            final AX checkAX,
            final DataSource datasource,
            final String SchemaName,
            final String table,
            final String[] secondaries,
            final boolean trimValues) {
        final List<String> matches = new ArrayList<>();
        for (String sdx : secondaries) {
            final String code = sdx.trim();
            if (checkAX.AX(datasource, SchemaName, table, code).isSuccess()) {
                matches.add(trimValues ? code : sdx);
            }
        }
        return matches;
    }

    private static void setSdxFinder(final DRGOutput drgResult, final List<String> matches) {
        if (!matches.isEmpty()) {
            drgResult.setSDXFINDER(String.join(",", matches));
        }
    }
}