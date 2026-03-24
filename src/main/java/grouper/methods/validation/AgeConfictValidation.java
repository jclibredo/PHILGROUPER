/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class AgeConfictValidation {

    public AgeConfictValidation() {
    }

    private final Utility utility = new Utility();

    // GET AGE VALIDATION THIS AREA
//    public DRGWSResult AgeConfictValidation(final DataSource datasource,
//            final String p_pdx_code,
//            final String age_day,
//            final String age_min_year) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        if (new GetICD10().GetICD10(datasource, p_pdx_code).isSuccess()) {
//            if (this.ProcessAgeConflict(datasource, p_pdx_code, age_day, age_min_year).isSuccess()) {
//                result.setSuccess(true);
//            } else {
//                if (this.ProcessAgeConflict(datasource,
//                        p_pdx_code.substring(0, p_pdx_code.length() - 1).replaceAll("\\.", "").toUpperCase().trim(), age_day, age_min_year).isSuccess()) {
//                    result.setSuccess(true);
//                }
//            }
//        }
//        return result;
//    }

    public DRGWSResult AgeConfictValidation(
            final DataSource datasource,
            final String p_pdx_code,
            final String age_day,
            final String age_min_year) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement stateA = connection.prepareCall("begin :age_validation := DRG_SHADOWBILLING.DRGPKGFUNCTION.VALIDATE_AGE(:p_pdx_code,:age_day,:age_min_year); end;");
            stateA.registerOutParameter("age_validation", OracleTypes.CURSOR);
            stateA.setString("p_pdx_code", utility.CleanCode(p_pdx_code).trim());
            stateA.setString("age_day", age_day);
            stateA.setString("age_min_year", age_min_year);
            stateA.execute();
            ResultSet resultSetA = (ResultSet) stateA.getObject("age_validation");
            if (resultSetA.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(AgeConfictValidation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
