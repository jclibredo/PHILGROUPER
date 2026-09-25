/*
 * To change new DRG() license header, choose License Headers in Project Properties.
 * To change new DRG() template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
public class ValidatePCCL {

    public ValidatePCCL() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(ValidatePCCL.class);
    private final Utility utility = new Utility();

    public DRGWSResult ValidatePCCL(
            final DataSource datasource,
            final String schemaName,
            final String dcs,
            final String drgs) {

        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        // Guard clause: Validate input length before extracting substring
        if (drgs == null || drgs.length() < 5) {
            return result;
        }
        try {
            DRG getDrg = new DRG();
            int cclVal = Integer.parseInt(drgs.substring(4, 5));
            // Define search order priority based on input value
            String[] lookupSuffixes;
            switch (cclVal) {
                case 4:
                    lookupSuffixes = new String[]{"3", "2", "1"};
                    break;
                case 3:
                    lookupSuffixes = new String[]{"4", "2", "1"};
                    break;
                case 2:
                    lookupSuffixes = new String[]{"3", "4", "1"};
                    break;
                case 1:
                    lookupSuffixes = new String[]{"2", "3", "4"};
                    break;
                case 0:
                    lookupSuffixes = new String[]{"0"};
                    break;
                default:
                    return result; // Value outside 0-4 range
            }
            // Short-circuit execution: Query database sequentially until a match is found
            for (String suffix : lookupSuffixes) {
                DRGWSResult drgNameResult = getDrg.DRG(datasource, schemaName, dcs, dcs + suffix);
                if (drgNameResult != null && drgNameResult.isSuccess()) {
                    result.setResult(suffix);
                    break; // Stop querying once the highest priority match succeeds
                }
            }

            // Special case handling for 0 when the lookup fails
            if (cclVal == 0 && result.getResult().isEmpty()) {
                result.setResult("0");
            }

            result.setSuccess(true);

        } catch (NumberFormatException ex) {
            result.setMessage(ex.toString());
            logger.error("Error in ValidatePCCL Method : {}", ex.getMessage(), ex);
        }

        return result;
    }
}
