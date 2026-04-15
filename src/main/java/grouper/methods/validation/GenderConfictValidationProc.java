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
public class GenderConfictValidationProc {

    public GenderConfictValidationProc() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GenderConfictValidationProc.class);
    private final Utility utility = new Utility();

    public DRGWSResult GenderConfictValidationProc(final DataSource datasource,
            final String procode,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getSexProcValidation = connection.prepareCall("begin :age_proc_validation := DRG_SHADOWBILLING.DRGPKGFUNCTION.PROC_AGE_VALIDATION(:procode,:gender); end;");
            getSexProcValidation.registerOutParameter("age_proc_validation", OracleTypes.CURSOR);
            getSexProcValidation.setString("procode", procode.trim());
            getSexProcValidation.setString("gender", gender);
            getSexProcValidation.execute();
            ResultSet getSexProcValidationResult = (ResultSet) getSexProcValidation.getObject("age_proc_validation");
            if (getSexProcValidationResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GenderConfictValidationProc Method");
            logger.error("Error in GenderConfictValidationProc Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
