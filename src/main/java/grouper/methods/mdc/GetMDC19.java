///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package grouper.methods.mdc;
//
//import grouper.methods.validation.AX;
//import grouper.structures.DRGOutput;
//import grouper.structures.DRGWSResult;
//import grouper.structures.GrouperParameter;
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
//public class GetMDC19 {
//
//    public GetMDC19() {
//    }
//
//    private final Utility utility = new Utility();
//
//    public DRGWSResult GetMDC19(
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
//            AX checkAX = new AX();
//            int Counter19PBX = 0;
//            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
//            for (int x = 0; x < ProcedureList.size(); x++) {
//                if (checkAX.AX(datasource, SchemaName, "19PBX", ProcedureList.get(x).trim()).isSuccess()) {
//                    ORProcedureCounterList.add(Integer.valueOf(checkAX.AX(datasource, SchemaName, "19PBX", ProcedureList.get(x).trim()).getResult()));
//                    Counter19PBX++;
//                }
//            }
//            //FINDING DC IS HERE
//            String getDc = this.principalDiag(drgResult.getPDC(), Counter19PBX);
//            drgResult.setDC(getDc);
//            //FINDING PCCL IS HERE
////            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
//            if (getPCCLResult.isSuccess()) {
//                result.setSuccess(getPCCLResult.isSuccess());
//                result.setResult(getPCCLResult.getResult());
//                result.setMessage("MDC 19 Done Checking");
//            } else {
//                result = getPCCLResult;
//            }
//        } catch (NumberFormatException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(GetMDC19.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//
//    }
//
//    private String principalDiag(
//            final String pdc,
//            final Integer Counter19PBX) {
//        String dc = "";
//        switch (pdc.toUpperCase()) {
//            case "19A"://Acute Psychotic Disorders
//                if (Counter19PBX > 0) {
//                    dc = "1901";
//                } else {
//                    dc = "1950";
//                }
//                break;
//            case "19B"://Chronic Psychotic Disorders
//                if (Counter19PBX > 0) {
//                    dc = "1902";
//                } else {
//                    dc = "1951";
//                }
//                break;
//            case "19C"://Major Affective Disorders
//                if (Counter19PBX > 0) {
//                    dc = "1903";
//                } else {
//                    dc = "1952";
//                }
//                break;
//            case "19D"://Other Affect and Somatoform Disorders
//                dc = "1953";
//                break;
//            case "19E"://Acute Reaction and Psychosocial Dysfunction
//                dc = "1954";
//                break;
//            case "19F"://Anxiety Disorders
//                dc = "1955";
//                break;
//            case "19G"://Eating and Obsessive Compulsive Disorders
//                dc = "1956";
//                break;
//            case "19H"://Personality and Impulse Control Disorders
//                dc = "1957";
//                break;
//            case "19J"://Childhood Mental Disorders
//                dc = "1958";
//                break;
//            case "19K"://Organic Disturbance and Mental Retardation
//                dc = "1959";
//                break;
//            case "19L"://Other Mental Disorders
//                dc = "1960";
//                break;
//            case "19M"://Sexual Dysfunction PDC 19M
//                dc = "1961";
//                break;
//        }
//        return dc;
//    }
//
//}


package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC19 {

    private static final Logger LOGGER = Logger.getLogger(GetMDC19.class.getName());
    private static final String PROC_TABLE = "19PBX";

    private final Utility utility = new Utility();

    public GetMDC19() {
    }

    public DRGWSResult GetMDC19(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            // Count procedures that exist in the 19PBX table
            final AX checkAX = new AX();
            int counter19PBX = 0;
            for (String proc : grouperparameter.getProc().split(",")) {
                // One DB call per procedure (original called AX twice per procedure)
                final DRGWSResult ax = checkAX.AX(datasource, SchemaName, PROC_TABLE, proc.trim());
                if (ax.isSuccess()) {
                    // Kept only to preserve the original NumberFormatException behavior.
                    // Safe to remove if AX always returns a numeric result.
                    Integer.parseInt(ax.getResult());
                    counter19PBX++;
                }
            }

            // FINDING DC IS HERE
            drgResult.setDC(principalDiag(drgResult.getPDC(), counter19PBX > 0));

            // FINDING PCCL IS HERE
//            final DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
             DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 19 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (NumberFormatException ex) {
            result.setMessage("Something went wrong");
            LOGGER.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private String principalDiag(final String pdc, final boolean hasProcedure) {
        switch (pdc.toUpperCase(Locale.ROOT)) {
            case "19A": // Acute Psychotic Disorders
                return hasProcedure ? "1901" : "1950";
            case "19B": // Chronic Psychotic Disorders
                return hasProcedure ? "1902" : "1951";
            case "19C": // Major Affective Disorders
                return hasProcedure ? "1903" : "1952";
            case "19D": // Other Affect and Somatoform Disorders
                return "1953";
            case "19E": // Acute Reaction and Psychosocial Dysfunction
                return "1954";
            case "19F": // Anxiety Disorders
                return "1955";
            case "19G": // Eating and Obsessive Compulsive Disorders
                return "1956";
            case "19H": // Personality and Impulse Control Disorders
                return "1957";
            case "19J": // Childhood Mental Disorders
                return "1958";
            case "19K": // Organic Disturbance and Mental Retardation
                return "1959";
            case "19L": // Other Mental Disorders
                return "1960";
            case "19M": // Sexual Dysfunction
                return "1961";
            default:
                return "";
        }
    }
}