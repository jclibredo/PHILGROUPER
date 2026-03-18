/*
 * To change new DRG() license header, choose License Headers in Project Properties.
 * To change new DRG() template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
public class ValidatePCCL {

    public ValidatePCCL() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult ValidatePCCL(final DataSource datasource, final String dcs, final String drgs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            String cclval = drgs.substring(5 - 1, 5);
            switch (Integer.parseInt(cclval)) {
                case 4: {
                    if (new DRG().DRG(datasource, dcs, dcs + "3").isSuccess()) {
                        result.setResult("3");

                    } else if (new DRG().DRG(datasource, dcs, dcs + "2").isSuccess()) {
                        result.setResult("2");

                    } else if (new DRG().DRG(datasource, dcs, dcs + "1").isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                }
                case 3: {
                    if (new DRG().DRG(datasource, dcs, dcs + "4").isSuccess()) {
                        result.setResult("4");
                    } else if (new DRG().DRG(datasource, dcs, dcs + "2").isSuccess()) {
                        result.setResult("2");
                    } else if (new DRG().DRG(datasource, dcs, dcs + "1").isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                }
                case 2: {
                    if (new DRG().DRG(datasource, dcs, dcs + "3").isSuccess()) {
                        result.setResult("3");
                    } else if (new DRG().DRG(datasource, dcs, dcs + "4").isSuccess()) {
                        result.setResult("4");
                    } else if (new DRG().DRG(datasource, dcs, dcs + "1").isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                }
                case 1: {
                    if (new DRG().DRG(datasource, dcs, dcs + "2").isSuccess()) {
                        result.setResult("2");
                    } else if (new DRG().DRG(datasource, dcs, dcs + "3").isSuccess()) {
                        result.setResult("3");
                    } else if (new DRG().DRG(datasource, dcs, dcs + "4").isSuccess()) {
                        result.setResult("4");
                    }
                    result.setSuccess(true);
                    break;
                }
            }
        } catch (NumberFormatException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ValidatePCCL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
