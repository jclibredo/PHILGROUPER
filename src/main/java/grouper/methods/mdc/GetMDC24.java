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
//public class GetMDC24 {
//
//    public GetMDC24() {
//    }
//
//    private final Utility utility = new Utility();
//
//    public DRGWSResult GetMDC24(
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
//            //CHECKING FOR TRAUMA CODES
//            int A = 0;
//            int B = 0;
//            int C = 0;
//            int D = 0;
//            int E = 0;
//            int F = 0;
//            int G = 0;
//            int H = 0;
//            int J = 0;
//            int PDXCounter99 = 0;
//            int PCXCounter99 = 0;
//            int Counter24PBX = 0;
////            int ORProcedureCounter = 0;
//            int mdcprocedureCounter = 0;
//            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
//            ArrayList<Integer> hierarvalue = new ArrayList<>();
//            ArrayList<String> pdclist = new ArrayList<>();
//            AX checkAX = new AX();
//            for (int x = 0; x < ProcedureList.size(); x++) {
//                //AX 99PDX Checking
//                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
//                    PDXCounter99++;
//                }
//                //AX 99PCX
//                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
//                    PCXCounter99++;
//                }
//                //AX 24PBX
//                if (checkAX.AX(datasource, SchemaName, "24PBX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
//                    Counter24PBX++;
////                    ORProcedureCounter++;
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
//                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource,
//                        SchemaName, ProcedureList.get(x).trim());
//                if (ORProcedureResult.isSuccess()) {
////                    ORProcedureCounter++;
//                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
//                    switch (ORProcedureResult.getMessage()) {
//                        case "A":
//                            A++;
//                            break;
//                        case "B":
//                            B++;
//                            break;
//                        case "C":
//                            C++;
//                            break;
//                        case "D":
//                            D++;
//                            break;
//                        case "E":
//                            E++;
//                            break;
//                        case "F":
//                            F++;
//                            break;
//                        case "G":
//                            G++;
//                            break;
//                        case "H":
//                            H++;
//                            break;
//
//                        case "J":
//                            J++;
//                            break;
//                    }
//                }
//            }
//            if (PDXCounter99 > 0) {
//                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
//                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
//                        grouperparameter.getDischargeDate(),
//                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
////                    if (ORProcedureCounter > 0) {
//                    if (mdcprocedureCounter > 0) {
//                        String getDcResult = this.MDCProcedure(A, D, H, G, E, Counter24PBX, B, C, F, J);
//                        drgResult.setDC(getDcResult);
//                    } else {
//                        drgResult.setDC("2450");
//                    }
//                } else {
//                    if (PCXCounter99 > 0) {//Cont. Mech Vent 96+ hr
//                        drgResult.setDC("2415");
//                    } else {
//                        drgResult.setDC("2416");
//                    }
//                }
//            } else {
////                if (ORProcedureCounter > 0) {
//                if (mdcprocedureCounter > 0) {
//                    String getDcResult = this.MDCProcedure(A, D, H, G, E, Counter24PBX, B, C, F, J);
//                    drgResult.setDC(getDcResult);
//                } else {
//                    drgResult.setDC("2450");
//                }
//            }
//
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
////            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
//            if (getPCCLResult.isSuccess()) {
//                result.setSuccess(getPCCLResult.isSuccess());
//                result.setResult(getPCCLResult.getResult());
//                result.setMessage("MDC 24 Done Checking");
//            } else {
//                result = getPCCLResult;
//            }
//        } catch (IOException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(GetMDC24.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
//
//    private String MDCProcedure(
//            final Integer A,
//            final Integer D,
//            final Integer H,
//            final Integer G,
//            final Integer E,
//            final Integer Counter24PBX,
//            final Integer B,
//            final Integer C,
//            final Integer F,
//            final Integer J) {
//        String dc = "";
//        if (A > 0 && D > 0) {//Intracranial w Others Proc site A+D/E/G/H
//            dc = "2401";
//        } else if (A > 0 && H > 0) {//Intracranial w Others Proc site A+D/E/G/H
//            dc = "2401";
//        } else if (A > 0 && G > 0) {//Intracranial w Others Proc site A+D/E/G/H
//            dc = "2401";
//        } else if (A > 0 && E > 0) {//Intracranial w Others Proc site A+D/E/G/H
//            dc = "2401";
//        } else if (E > 0 && D > 0) {//Spinal w Others
//            dc = "2405";
//        } else if (E > 0 && H > 0) {//Spinal w Others
//            dc = "2405";
//        } else if (E > 0 && G > 0) {//Spinal w Others
//            dc = "2405";
//        } else if (Counter24PBX > 0) {//Multiple Wound Debridement
//            dc = "2417";
//        } else if (D > 0 && G > 0) { //Abdominal w Lower Ext.
//            dc = "2410";
//        } else if (E > 0) {//Spinal
//            dc = "2412";
//        } else if (A > 0) {//Intracranial
//            dc = "2411";
//        } else if (H > 0 && D > 0) {//Wound Debridement w Abdominal or Lower Ext.
//            dc = "2408";
//        } else if (H > 0 && G > 0) {//Wound Debridement w Abdominal or Lower Ext.
//            dc = "2408";
//        } else if (B > 0 || C > 0 || D > 0 || F > 0 || G > 0 || H > 0 || J > 0) {//Wound Debridement w Abdominal or Lower Ext.
//            dc = "2413";
//        } else {//Other OR Procedure
//            dc = "2414";
//        }
//        return dc;
//
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
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC24 {

    private final Utility utility = new Utility();

    public GetMDC24() {
    }

    public DRGWSResult GetMDC24(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        // Kept as in the original (NumberFormatException propagates for a bad MDC value)
        final String mdcWithoutZeros = String.valueOf(Integer.parseInt(drgResult.getMDC()));
        final String[] procedures = grouperparameter.getProc().split(",");

        // Created once instead of once per procedure
        final AX checkAX = new AX();
        final MDCProcedureMethod mdcProcedureMethod = new MDCProcedureMethod();

        boolean hasPDX99 = false;         // AX 99PDX
        boolean hasPCX99 = false;         // AX 99PCX
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
        // The original repeated the MDC-procedure / 2450 chain in two places; it is now written once.
        // The OR-procedure site lookups only run when they are actually needed.
        if (hasPDX99 && utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                utility.Convert24to12(grouperparameter.getTimeAdmission()),
                grouperparameter.getDischargeDate(),
                utility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
            drgResult.setDC(hasPCX99 ? "2415" : "2416"); // 2415 = Cont. Mech Vent 96+ hr
        } else if (hasMDCProcedure) {
            drgResult.setDC(this.mdcProcedureDc(datasource, SchemaName, checkAX, procedures));
        } else {
            drgResult.setDC("2450");
        }

        // FINDING PCCL IS HERE
//        final DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
        final DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
        if (!getPCCLResult.isSuccess()) {
            return getPCCLResult;
        }
        final DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(true);
        result.setResult(getPCCLResult.getResult());
        result.setMessage("MDC 24 Done Checking");
        return result;
    }

    /**
     * Picks the DC from the OR procedure sites (A-H, J) found across all
     * procedures. The order of the checks matters and matches the original.
     */
    private String mdcProcedureDc(
            final DataSource datasource,
            final String SchemaName,
            final AX checkAX,
            final String[] procedures) {
        final ORProcedure orProcedureMethod = new ORProcedure();
        final Set<String> sites = new HashSet<>();
        for (String rawProc : procedures) {
            final DRGWSResult orResult = orProcedureMethod.ORProcedure(datasource, SchemaName, rawProc.trim());
            if (orResult.isSuccess()) {
                sites.add(orResult.getMessage());
            }
        }
        final boolean a = sites.contains("A");
        final boolean b = sites.contains("B");
        final boolean c = sites.contains("C");
        final boolean d = sites.contains("D");
        final boolean e = sites.contains("E");
        final boolean f = sites.contains("F");
        final boolean g = sites.contains("G");
        final boolean h = sites.contains("H");
        final boolean j = sites.contains("J");

        if (a && (d || h || g || e)) {   // Intracranial w Others (A + D/E/G/H)
            return "2401";
        }
        if (e && (d || h || g)) {        // Spinal w Others
            return "2405";
        }
        if (anyMatch(checkAX, datasource, SchemaName, "24PBX", procedures)) { // Multiple Wound Debridement
            return "2417";
        }
        if (d && g) {                    // Abdominal w Lower Ext.
            return "2410";
        }
        if (e) {                         // Spinal
            return "2412";
        }
        if (a) {                         // Intracranial
            return "2411";
        }
        if (h && (d || g)) {             // Wound Debridement w Abdominal or Lower Ext.
            return "2408";
        }
        if (b || c || d || f || g || h || j) {
            return "2413";
        }
        return "2414";                   // Other OR Procedure
    }

    /** True if any code matches the given AX table. Stops at the first match. */
    private static boolean anyMatch(
            final AX checkAX,
            final DataSource datasource,
            final String SchemaName,
            final String table,
            final String[] codes) {
        for (String code : codes) {
            if (checkAX.AX(datasource, SchemaName, table, code.trim()).isSuccess()) {
                return true;
            }
        }
        return false;
    }
}
