/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC20 {

    public GetMDC20() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC20(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            switch (drgResult.getPDC()) {
                case "20A"://Alcohol Intoxication and Withdrawal
                    drgResult.setDC("2050");
                    break;
                case "20B"://Drug Use Disorders and Withdrawal
                    drgResult.setDC("2051");
                    break;
                case "20C"://Alcohol Use Disorders and Dependence
                    drgResult.setDC("2052");
                    break;
                case "20D"://Opioid Use Disorders and Dependence
                    drgResult.setDC("2053");
                    break;
                case "20E"://Psychostimulant Use Disorders and Dependence
                    drgResult.setDC("2054");
                    break;
                case "20F"://Other Drug Use Disorders and Intoxication PDC 20F
                    drgResult.setDC("2055");
                    break;
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 20 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (Exception ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC20.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
