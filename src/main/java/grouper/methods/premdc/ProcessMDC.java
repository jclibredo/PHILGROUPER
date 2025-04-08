/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.premdc;

import grouper.methods.mdc.GetMDC01;
import grouper.methods.mdc.GetMDC02;
import grouper.methods.mdc.GetMDC03;
import grouper.methods.mdc.GetMDC04;
import grouper.methods.mdc.GetMDC05;
import grouper.methods.mdc.GetMDC06;
import grouper.methods.mdc.GetMDC07;
import grouper.methods.mdc.GetMDC08;
import grouper.methods.mdc.GetMDC09;
import grouper.methods.mdc.GetMDC10;
import grouper.methods.mdc.GetMDC11;
import grouper.methods.mdc.GetMDC12;
import grouper.methods.mdc.GetMDC13;
import grouper.methods.mdc.GetMDC14;
import grouper.methods.mdc.GetMDC15;
import grouper.methods.mdc.GetMDC16;
import grouper.methods.mdc.GetMDC17;
import grouper.methods.mdc.GetMDC18;
import grouper.methods.mdc.GetMDC19;
import grouper.methods.mdc.GetMDC20;
import grouper.methods.mdc.GetMDC21;
import grouper.methods.mdc.GetMDC22;
import grouper.methods.mdc.GetMDC23;
import grouper.methods.mdc.GetMDC24;
import grouper.methods.mdc.GetMDC25;
import grouper.methods.mdc.GetMDC28;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class ProcessMDC {

    public ProcessMDC() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ProcessMDC(final DataSource datasource, final DRGOutput mdcfinderoutput, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        int MDC = Integer.parseInt(mdcfinderoutput.getMDC());
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        switch (MDC) {
            case 1: {
                result = new GetMDC01().GetMDC01(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 2: {
                result = new GetMDC02().GetMDC02(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 3: {
                result = new GetMDC03().GetMDC03(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 4: {
                result = new GetMDC04().GetMDC04(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 5: {
                result = new GetMDC05().GetMDC05(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 6: {
                result = new GetMDC06().GetMDC06(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 7: {
                result = new GetMDC07().GetMDC07(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 8: {
                result = new GetMDC08().GetMDC08(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 9: {
                result = new GetMDC09().GetMDC09(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 10: {
                result = new GetMDC10().GetMDC10(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 11: {
                result = new GetMDC11().GetMDC11(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 12: {
                result = new GetMDC12().GetMDC12(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 13: {
                result = new GetMDC13().GetMDC13(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 14: {
                result = new GetMDC14().GetMDC14(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 15: {
                result = new GetMDC15().GetMDC15(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 16: {
                result = new GetMDC16().GetMDC16(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 17: {
                result = new GetMDC17().GetMDC17(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 18: {
                result = new GetMDC18().GetMDC18(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 19: {
                result = new GetMDC19().GetMDC19(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 20: {
                result = new GetMDC20().GetMDC20(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 21: {
                result = new GetMDC21().GetMDC21(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 22: {
                result = new GetMDC22().GetMDC22(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 23: {
                result = new GetMDC23().GetMDC23(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 24: {
                result = new GetMDC24().GetMDC24(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 25: {
                result = new GetMDC25().GetMDC25(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            case 28: {
                result = new GetMDC28().GetMDC28(datasource, mdcfinderoutput, grouperparameter);
                break;
            }
            default: {
                result.setMessage("MDC NOT FOUND");
                break;
            }
        }

        return result;
    }

}
