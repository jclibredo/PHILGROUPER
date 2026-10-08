/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

//import grouper.methods.validation.CleanSDxDCDeterminationPLSQL;
import grouper.methods.validation.DRG;
import grouper.methods.validation.DRGDetermination;
//import grouper.methods.validation.GetDC;
import grouper.methods.validation.GetFDRG;
//import grouper.methods.validation.GetDC;
//import grouper.methods.validation.GetPCCL;
//import grouper.methods.validation.ValidatePCCL;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.FDRG;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class GetPCCLResult {

    public GetPCCLResult() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetPCCLJava(
            final DataSource datasource,
            final String schemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            DRGDetermination drgDetermination = new DRGDetermination();
            DRG checkDRG = new DRG();
            GetFDRG getFdrg = new GetFDRG();
            String sdxdcfinder = drgResult.getSDXFINDER() != null ? drgResult.getSDXFINDER() : "";
            if (drgResult.getDRG() == null) {
//                drgResult.setDRGName("Grouper Error");
//                System.out.println();
//                if (getDc.GetDC(datasource, schemaName, dc).isSuccess()) {
////                if (utility.isValidDCList(drgResult.getDC())) {
//                    String fallbackDrg = dc + "9";
//                    drgResult.setDRG(fallbackDrg);
//                    drgResult.setPrepccl("9");
//                    drgResult.setFinalpccl("9");
//                    DRGWSResult drgCheckResult = checkDRG.DRG(datasource, schemaName, dc, fallbackDrg);
//                    drgResult.setDRGName(drgCheckResult.getMessage());
//                } else {
                String getPrePccl = drgDetermination.PCCLDetermination(
                        datasource,
                        schemaName,
                        grouperparameter.getSdx(),
                        sdxdcfinder,
                        grouperparameter.getPdx(),
                        drgResult.getDC());
                String preDrg = drgResult.getDC() + getPrePccl;
                drgResult.setPrepccl(getPrePccl);
                drgResult.setPredrg(preDrg);
                DRGWSResult getFdrgResult = getFdrg.GetFDRG(datasource, schemaName, drgResult.getDC(), preDrg);
                if (getFdrgResult.isSuccess()) {
                    FDRG fdrg = utility.objectMapper().readValue(getFdrgResult.getResult(), FDRG.class);
                    String finalDrg = fdrg.getFdrg() == null ? fdrg.getPdrg() : fdrg.getFdrg();
                    String finalPccl = fdrg.getFpccl() == null ? fdrg.getPpccl() : fdrg.getFpccl();
                    drgResult.setDRG(finalDrg);
                    drgResult.setFinalpccl(finalPccl);
                    drgResult.setDC(fdrg.getDc());
                    drgResult.setDRGName(checkDRG.DRG(datasource, schemaName, fdrg.getDc(), finalDrg).getMessage());
                } else {
                    drgResult.setDRG(drgResult.getDC() + getPrePccl);
                    drgResult.setFinalpccl("X");
                    drgResult.setDRGName(getFdrgResult.getMessage());
                }
                // Execute query ONCE and store result
//                DRGWSResult drgCheckResult = checkDRG.DRG(datasource, schemaName, dc, constructedDrg);
//                if (drgCheckResult.isSuccess()) {
//                    drgResult.setDRGName(drgCheckResult.getMessage());
//                    drgResult.setFinalpccl(getLastChar(constructedDrg));
//                } else {
//                    DRGWSResult drgValues = validatePCCL.ValidatePCCL(datasource, schemaName, dc, constructedDrg);
//                    if (drgValues.isSuccess()) {
//                        String validDrgCode = dc + drgValues.getResult();
//                        drgResult.setDRG(validDrgCode);
//                        DRGWSResult drgNames = checkDRG.DRG(datasource, schemaName, dc, validDrgCode);
//                        if (drgNames.isSuccess()) {
//                            drgResult.setDRGName(drgNames.getMessage());
//                        }
//                        drgResult.setFinalpccl(getLastChar(validDrgCode));
//                    } else {
//                        drgResult.setPrepccl("X");
//                        drgResult.setFinalpccl("X");
//                        drgResult.setDRGName("DRG code grouper provide not exist in the library");
//                    }
//
//                }
//                }
            } else {
                drgResult.setDRGName(checkDRG.DRG(datasource, schemaName, drgResult.getDC(), drgResult.getDRG()).getMessage());
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
        } catch (IOException ex) {
            result.setMessage("Something went wrong " + ex.getMessage());
        } catch (Exception ex) {
            result.setMessage("Something went wrong " + ex.getMessage());
        }

        return result;
    }

    /**
     * Safe helper to extract the last character of a string
     */
    private String getLastChar(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        return str.substring(str.length() - 1);
    }
}
