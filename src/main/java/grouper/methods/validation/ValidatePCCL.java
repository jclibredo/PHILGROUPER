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

//    public DRGWSResult ValidatePCCL(final DataSource datasource, final String dcs, final String drgs) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try {
//            String cclval = drgs.substring(5 - 1, 5);
//            switch (Integer.parseInt(cclval)) {
//                case 4: {
//                    if (new DRG().DRG(datasource, dcs, dcs + "3").isSuccess()) {
//                        result.setResult("3");
//
//                    } else if (new DRG().DRG(datasource, dcs, dcs + "2").isSuccess()) {
//                        result.setResult("2");
//
//                    } else if (new DRG().DRG(datasource, dcs, dcs + "1").isSuccess()) {
//                        result.setResult("1");
//                    }
//                    result.setSuccess(true);
//                    break;
//                }
//                case 3: {
//                    if (new DRG().DRG(datasource, dcs, dcs + "4").isSuccess()) {
//                        result.setResult("4");
//                    } else if (new DRG().DRG(datasource, dcs, dcs + "2").isSuccess()) {
//                        result.setResult("2");
//                    } else if (new DRG().DRG(datasource, dcs, dcs + "1").isSuccess()) {
//                        result.setResult("1");
//                    }
//                    result.setSuccess(true);
//                    break;
//                }
//                case 2: {
//                    if (new DRG().DRG(datasource, dcs, dcs + "3").isSuccess()) {
//                        result.setResult("3");
//                    } else if (new DRG().DRG(datasource, dcs, dcs + "4").isSuccess()) {
//                        result.setResult("4");
//                    } else if (new DRG().DRG(datasource, dcs, dcs + "1").isSuccess()) {
//                        result.setResult("1");
//                    }
//                    result.setSuccess(true);
//                    break;
//                }
//                case 1: {
//                    if (new DRG().DRG(datasource, dcs, dcs + "2").isSuccess()) {
//                        result.setResult("2");
//                    } else if (new DRG().DRG(datasource, dcs, dcs + "3").isSuccess()) {
//                        result.setResult("3");
//                    } else if (new DRG().DRG(datasource, dcs, dcs + "4").isSuccess()) {
//                        result.setResult("4");
//                    }
//                    result.setSuccess(true);
//                    break;
//                }
//                case 0: {
//                    if (new DRG().DRG(datasource, dcs, dcs + "0").isSuccess()) {
//                        result.setResult("0");
//                    }
//                    result.setSuccess(true);
//                    break;
//                }
//            }
//        } catch (NumberFormatException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(ValidatePCCL.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
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
            Logger.getLogger(ValidatePCCL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
