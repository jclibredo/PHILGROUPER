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

    //Get Validate PCCL Value
    public DRGWSResult ValidatePCCL(final DataSource datasource, final String dcs, final String drgs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            DRG getDrg = new DRG();
            String cclval = drgs.substring(5 - 1, 5);
            switch (Integer.parseInt(cclval)) {
                case 4: {
                    DRGWSResult drgname3 = getDrg.DRG(datasource, dcs, dcs + "3");
                    DRGWSResult drgname222 = getDrg.DRG(datasource, dcs, dcs + "2");
                    DRGWSResult drgname111 = getDrg.DRG(datasource, dcs, dcs + "1");
                    if (drgname3.isSuccess()) {
                        result.setResult("3");
                    } else if (drgname222.isSuccess()) {
                        result.setResult("2");
                    } else if (drgname111.isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                }
                case 3: {
                    DRGWSResult drgname4 = getDrg.DRG(datasource, dcs, dcs + "4");
                    DRGWSResult drgname2 = getDrg.DRG(datasource, dcs, dcs + "2");
                    DRGWSResult drgname11 = getDrg.DRG(datasource, dcs, dcs + "1");
                    if (drgname4.isSuccess()) {
                        result.setResult("4");
                    } else if (drgname2.isSuccess()) {
                        result.setResult("2");
                    } else if (drgname11.isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                }
                case 2: {
                    DRGWSResult drgname333 = getDrg.DRG(datasource, dcs, dcs + "3");
                    DRGWSResult drgname444 = getDrg.DRG(datasource, dcs, dcs + "4");
                    DRGWSResult drgname1 = getDrg.DRG(datasource, dcs, dcs + "1");
                    if (drgname333.isSuccess()) {
                        result.setResult("3");
                    } else if (drgname444.isSuccess()) {
                        result.setResult("4");
                    } else if (drgname1.isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                }
                case 1: {
                    DRGWSResult drgname22 = getDrg.DRG(datasource, dcs, dcs + "2");
                    DRGWSResult drgname33 = getDrg.DRG(datasource, dcs, dcs + "3");
                    DRGWSResult drgname44 = getDrg.DRG(datasource, dcs, dcs + "4");
                    if (drgname22.isSuccess()) {
                        result.setResult("2");
                    } else if (drgname33.isSuccess()) {
                        result.setResult("3");
                    } else if (drgname44.isSuccess()) {
                        result.setResult("4");
                    }
                    result.setSuccess(true);
                    break;
                }
                case 0: {
                    DRGWSResult drgname0 = getDrg.DRG(datasource, dcs, dcs + "0");
                    if (drgname0.isSuccess()) {
                        result.setResult("0");
                    }
                    result.setSuccess(true);
                    break;
                }
            }

        } catch (NumberFormatException ex) {
            result.setMessage(ex.toString());
            logger.info("Executing ValidatePCCL Method");
            logger.error("Error in ValidatePCCL Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
