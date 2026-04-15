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
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class GenderConfictValidation {

    public GenderConfictValidation() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GenderConfictValidation.class);
    private final Utility utility = new Utility();

    // GET GENDER VALIDATION THIS AREA
    public DRGWSResult GenderConfictValidation(final DataSource datasource,
            final String p_pdx_code,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :gender_validation := DRG_SHADOWBILLING.DRGPKGFUNCTION.VALIDATE_GENDER(:p_pdx_code,:gender); end;");
            statement.registerOutParameter("gender_validation", OracleTypes.CURSOR);
            statement.setString("p_pdx_code", utility.CleanCode(p_pdx_code).trim());
            statement.setString("gender", gender.toUpperCase());
            statement.execute();
            ResultSet getSexValidationResult = (ResultSet) statement.getObject("gender_validation");
            if (getSexValidationResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GenderConfictValidation Method");
            logger.error("Error in GenderConfictValidation Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
