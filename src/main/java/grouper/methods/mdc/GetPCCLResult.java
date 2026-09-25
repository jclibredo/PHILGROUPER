/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.CleanSDxDCDeterminationPLSQL;
import grouper.methods.validation.DRG;
import grouper.methods.validation.DRGDetermination;
import grouper.methods.validation.GetPCCL;
import grouper.methods.validation.ValidatePCCL;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.IOException;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class GetPCCLResult {

    public GetPCCLResult() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetPCCLResult.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetPCCLResult(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            DRG checkDRG = new DRG();
            if (drgResult.getDRG() == null) {
                drgResult.setPrepccl("X");
                drgResult.setFinalpccl("X");
                drgResult.setDRGName("Grouper Error");
                drgResult.setDRG(drgResult.getDC() + "X");
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                    drgResult.setPrepccl("9");
                    drgResult.setFinalpccl("9");
                    drgResult.setDRGName(checkDRG.DRG(datasource, SchemaName, drgResult.getDC(),
                            drgResult.getDC() + "9").getMessage());
                } else {
                    String sdxfinalList = new CleanSDxDCDeterminationPLSQL().CleanSDxDCDeterminationPLSQL(datasource, SchemaName, grouperparameter.getSdx(),
                            drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = new GetPCCL().GetPCCL(datasource, SchemaName, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        drgResult.setPrepccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
                        drgResult.setFinalpccl(finaldrgresult.getDRG().substring(finaldrgresult.getDRG().length() - 1));
                        drgResult.setDRG(finaldrgresult.getDRG());
                        if (checkDRG.DRG(datasource, SchemaName, drgResult.getDC(),
                                finaldrgresult.getDRG()).isSuccess()) {
                            drgResult.setDRGName(checkDRG.DRG(datasource, SchemaName, drgResult.getDC(),
                                    finaldrgresult.getDRG()).getMessage());
                        } else {
                            DRGWSResult drgvalues = new ValidatePCCL().ValidatePCCL(datasource, SchemaName, drgResult.getDC(), finaldrgresult.getDRG());
                            if (drgvalues.isSuccess()) {
                                String drgcode = drgResult.getDC() + drgvalues.getResult();
                                drgResult.setDRG(drgcode);
                                DRGWSResult drgnames = checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), drgcode);
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
                if (checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("DRG code grouper provide not exist in the library");
                }
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
        } catch (IOException | NumberFormatException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetPCCLResult Method");
            logger.error("Error in GetPCCLResult Method : {}", ex.getMessage(), ex);
        }

        return result;
    }

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

            DRG checkDRG = new DRG();
            String currentDrg = drgResult.getDRG();
            String dc = drgResult.getDC();
            String sdxdcfinder = drgResult.getSDXFINDER() != null ? drgResult.getSDXFINDER() : "";
            ValidatePCCL validatePCCL = new ValidatePCCL();
            if (currentDrg == null) {
                drgResult.setDRGName("Grouper Error");
                if (utility.isValidDCList(dc)) {
                    String fallbackDrg = dc + "9";
                    drgResult.setDRG(fallbackDrg);
                    drgResult.setPrepccl("9");
                    drgResult.setFinalpccl("9");
                    DRGWSResult drgCheckResult = checkDRG.DRG(datasource, schemaName, dc, fallbackDrg);
                    drgResult.setDRGName(drgCheckResult.getMessage());
                } else {
                    DRGDetermination drgDetermination = new DRGDetermination();
                    String sdxFinalList = drgDetermination.CleanSDxDCDetermination(
                            datasource,
                            schemaName,
                            grouperparameter.getSdx(),
                            sdxdcfinder,
                            grouperparameter.getPdx(),
                            dc);
                    String constructedDrg = dc + sdxFinalList;
                    drgResult.setDRG(constructedDrg);
                    drgResult.setPrepccl(getLastChar(constructedDrg));
                    // Execute query ONCE and store result
                    DRGWSResult drgCheckResult = checkDRG.DRG(datasource, schemaName, dc, constructedDrg);
                    if (drgCheckResult.isSuccess()) {
                        drgResult.setDRGName(drgCheckResult.getMessage());
                        drgResult.setFinalpccl(getLastChar(constructedDrg));
                    } else {
                        DRGWSResult drgValues = validatePCCL.ValidatePCCL(datasource, schemaName, dc, constructedDrg);
                        if (drgValues.isSuccess()) {
                            String validDrgCode = dc + drgValues.getResult();
                            drgResult.setDRG(validDrgCode);
                            DRGWSResult drgNames = checkDRG.DRG(datasource, schemaName, dc, validDrgCode);
                            if (drgNames.isSuccess()) {
                                drgResult.setDRGName(drgNames.getMessage());
                            }
                            drgResult.setFinalpccl(getLastChar(validDrgCode));
                        } else {
                            drgResult.setPrepccl("X");
                            drgResult.setFinalpccl("X");
                            drgResult.setDRGName("DRG code grouper provide not exist in the library");
                        }
                    }
                }
            } else {
                // Execute query ONCE and store result
                DRGWSResult drgCheckResult = checkDRG.DRG(datasource, schemaName, dc, currentDrg);
                if (drgCheckResult.isSuccess()) {
                    drgResult.setDRGName(drgCheckResult.getMessage());
                } else {
                    drgResult.setDRGName("DRG code grouper provide not exist in the library");
                }
            }

            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));

        } catch (IOException ex) {
            logger.error("JSON serialization error in GetPCCLJava: {}", ex.getMessage(), ex);
            result.setMessage("Serialization error occurred");
        } catch (Exception ex) {
            logger.error("Unexpected error in GetPCCLJava: {}", ex.getMessage(), ex);
            result.setMessage("Something went wrong");
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
