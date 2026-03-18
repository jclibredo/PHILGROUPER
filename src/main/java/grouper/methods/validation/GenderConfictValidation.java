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
public class GenderConfictValidation {

    public GenderConfictValidation() {
    }
    private final Utility utility = new Utility();
    // GET GENDER VALIDATION THIS AREA
    public DRGWSResult GenderConfictValidation(final DataSource datasource,
            final String p_pdx_code,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (this.ProcessGenderConfictValidation(datasource, p_pdx_code, gender).isSuccess()) {
            result = this.ProcessGenderConfictValidation(datasource, p_pdx_code, gender);
        } else {
            if (this.ProcessGenderConfictValidation(datasource, p_pdx_code.substring(0, p_pdx_code.length() - 1), gender).isSuccess()) {
                result = this.ProcessGenderConfictValidation(datasource, p_pdx_code.substring(0, p_pdx_code.length() - 1), gender);
            }
        }
        return result;
    }

    private DRGWSResult ProcessGenderConfictValidation(final DataSource datasource,
            final String p_pdx_code,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :gENDer_validation := MINOSUN.DRGPKGFUNCTION.VALIDATE_GENDER(:p_pdx_code,:gENDer); end;");
            statement.registerOutParameter("gENDer_validation", OracleTypes.CURSOR);
            statement.setString("p_pdx_code", utility.CleanCode(p_pdx_code).trim());
            statement.setString("gENDer", gender.toUpperCase());
            statement.execute();
            ResultSet getSexValidationResult = (ResultSet) statement.getObject("gENDer_validation");
            if (getSexValidationResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GenderConfictValidation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
