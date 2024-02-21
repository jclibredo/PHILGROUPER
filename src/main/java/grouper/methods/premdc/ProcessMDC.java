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
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class ProcessMDC {
    public ProcessMDC() {
    }
    private final Utility utility = new Utility();
    private final GetMDC01 get01 = new GetMDC01();
    private final GetMDC02 get02 = new GetMDC02();
    private final GetMDC03 get03 = new GetMDC03();
    private final GetMDC04 get04 = new GetMDC04();
    private final GetMDC05 get05 = new GetMDC05();
    private final GetMDC06 get06 = new GetMDC06();
    private final GetMDC07 get07 = new GetMDC07();
    private final GetMDC11 get11 = new GetMDC11();
    private final GetMDC08 get08 = new GetMDC08();
    private final GetMDC09 get09 = new GetMDC09();
    private final GetMDC10 get10 = new GetMDC10();
    private final GetMDC12 get12 = new GetMDC12();
    private final GetMDC13 get13 = new GetMDC13();
    private final GetMDC14 get14 = new GetMDC14();
    private final GetMDC15 get15 = new GetMDC15();
    private final GetMDC16 get16 = new GetMDC16();
    private final GetMDC17 get17 = new GetMDC17();
    private final GetMDC18 get18 = new GetMDC18();
    private final GetMDC19 get19 = new GetMDC19();
    private final GetMDC20 get20 = new GetMDC20();
    private final GetMDC21 get21 = new GetMDC21();
    private final GetMDC22 get22 = new GetMDC22();
    private final GetMDC23 get23 = new GetMDC23();
    private final GetMDC24 get24 = new GetMDC24();
    private final GetMDC25 get25 = new GetMDC25();
    private final GetMDC28 get28 = new GetMDC28();

    public DRGWSResult ProcessMDC(final DataSource datasource, final DRGOutput mdcfinderoutput, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        try {
            String MDC = mdcfinderoutput.getMDC();
            result.setMessage("");
            result.setSuccess(false);
            result.setResult("");
            switch (MDC) {
                case "1":
                    DRGWSResult get01Result = get01.GetMDC01(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get01Result.getMessage());
                    result.setResult(get01Result.getResult());
                    result.setSuccess(get01Result.isSuccess());
                    break;
                case "2":
                    DRGWSResult get02Result = get02.GetMDC02(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get02Result.getMessage());
                    result.setResult(get02Result.getResult());
                    result.setSuccess(get02Result.isSuccess());
                    break;
                case "3":
                    DRGWSResult get03Result = get03.GetMDC03(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get03Result.getMessage());
                    result.setResult(get03Result.getResult());
                    result.setSuccess(get03Result.isSuccess());
                    break;
                case "4":
                    DRGWSResult get04Result = get04.GetMDC04(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get04Result.getMessage());
                    result.setResult(get04Result.getResult());
                    result.setSuccess(get04Result.isSuccess());
                    break;
                case "5":
                    DRGWSResult get05Result = get05.GetMDC05(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get05Result.getMessage());
                    result.setResult(get05Result.getResult());
                    result.setSuccess(get05Result.isSuccess());
                    break;
                case "6":
                    DRGWSResult get06Result = get06.GetMDC06(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get06Result.getMessage());
                    result.setResult(get06Result.getResult());
                    result.setSuccess(get06Result.isSuccess());
                    break;
                case "7":
                    DRGWSResult get07Result = get07.GetMDC07(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get07Result.getMessage());
                    result.setResult(get07Result.getResult());
                    result.setSuccess(get07Result.isSuccess());
                    break;
                case "8":
                    DRGWSResult get08Result = get08.GetMDC08(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get08Result.getMessage());
                    result.setResult(get08Result.getResult());
                    result.setSuccess(get08Result.isSuccess());
                    break;
                case "9":
                    DRGWSResult get09Result = get09.GetMDC09(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get09Result.getMessage());
                    result.setResult(get09Result.getResult());
                    result.setSuccess(get09Result.isSuccess());
                    break;
                case "10":
                    DRGWSResult get10Result = get10.GetMDC10(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get10Result.getMessage());
                    result.setResult(get10Result.getResult());
                    result.setSuccess(get10Result.isSuccess());
                    break;
                case "11":
                    DRGWSResult get11Result = get11.GetMDC11(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get11Result.getMessage());
                    result.setResult(get11Result.getResult());
                    result.setSuccess(get11Result.isSuccess());

                    break;
                case "12":
                    DRGWSResult get12Result = get12.GetMDC12(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get12Result.getMessage());
                    result.setResult(get12Result.getResult());
                    result.setSuccess(get12Result.isSuccess());
                    break;
                case "13":
                    DRGWSResult get13Result = get13.GetMDC13(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get13Result.getMessage());
                    result.setResult(get13Result.getResult());
                    result.setSuccess(get13Result.isSuccess());
                    break;
                case "14":
                    DRGWSResult get14Result = get14.GetMDC14(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get14Result.getMessage());
                    result.setResult(get14Result.getResult());
                    result.setSuccess(get14Result.isSuccess());
                    break;
                case "15":
                    DRGWSResult get15Result = get15.GetMDC15(datasource, mdcfinderoutput, grouperparameter);
                    //result.setMessage("MDC 15 Located");
                    result.setMessage(get15Result.getMessage());
                    result.setResult(get15Result.getResult());
                    result.setSuccess(get15Result.isSuccess());
                    break;
                case "16":
                    DRGWSResult get16Result = get16.GetMDC16(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get16Result.getMessage());
                    result.setResult(get16Result.getResult());
                    result.setSuccess(get16Result.isSuccess());
                    break;
                case "17":
                    DRGWSResult get17Result = get17.GetMDC17(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get17Result.getMessage());
                    result.setResult(get17Result.getResult());
                    result.setSuccess(get17Result.isSuccess());
                    break;
                case "18":
                    DRGWSResult get18Result = get18.GetMDC18(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get18Result.getMessage());
                    result.setResult(get18Result.getResult());
                    result.setSuccess(get18Result.isSuccess());
                    break;
                case "19":
                    DRGWSResult get19Result = get19.GetMDC19(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get19Result.getMessage());
                    result.setResult(get19Result.getResult());
                    result.setSuccess(get19Result.isSuccess());
                    break;
                case "20":
                    DRGWSResult get20Result = get20.GetMDC20(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get20Result.getMessage());
                    result.setResult(get20Result.getResult());
                    result.setSuccess(get20Result.isSuccess());
                    break;
                case "21":
                    DRGWSResult get21Result = get21.GetMDC21(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get21Result.getMessage());
                    result.setResult(get21Result.getResult());
                    result.setSuccess(get21Result.isSuccess());
                    break;
                case "22":
                    DRGWSResult get22Result = get22.GetMDC22(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get22Result.getMessage());
                    result.setResult(get22Result.getResult());
                    result.setSuccess(get22Result.isSuccess());
                    break;
                case "23":
                    DRGWSResult get23Result = get23.GetMDC23(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get23Result.getMessage());
                    result.setResult(get23Result.getResult());
                    result.setSuccess(get23Result.isSuccess());
                    break;
                case "24":
                    DRGWSResult get24Result = get24.GetMDC24(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get24Result.getMessage());
                    result.setResult(get24Result.getResult());
                    result.setSuccess(get24Result.isSuccess());
                    break;
                case "25":
                    DRGWSResult get25Result = get25.GetMDC25(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get25Result.getMessage());
                    result.setResult(get25Result.getResult());
                    result.setSuccess(get25Result.isSuccess());
                    break;
                case "28":
                    DRGWSResult get28Result = get28.GetMDC28(datasource, mdcfinderoutput, grouperparameter);
                    result.setMessage(get28Result.getMessage());
                    result.setResult(get28Result.getResult());
                    result.setSuccess(get28Result.isSuccess());
                    break;
                default:
                    result.setMessage("MDC NOT FOUND");
                    break;
            }

        } catch (IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(ProcessMDC.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
