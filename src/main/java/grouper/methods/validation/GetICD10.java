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
public class GetICD10 {

    public GetICD10() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetICD10.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetICD10(final DataSource datasource, final String p_icd10_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :p_validcode := DRG_SHADOWBILLING.DRGPKGFUNCTION.get_valid_icd10(:p_icd10_code); end;");
            statement.registerOutParameter("p_validcode", OracleTypes.CURSOR);
            statement.setString("p_icd10_code", p_icd10_code);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("p_validcode");
            if (resultset.next()) {
                result.setSuccess(true);
                result.setResult(resultset.getString("validcode"));
                result.setMessage("Record Found");
            } else {
                result.setMessage("No ICD10 Record Found");
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetICD10 Method");
            logger.error("Error in GetICD10 Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
