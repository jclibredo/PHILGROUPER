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
public class GET_ICD9 {

    public GET_ICD9() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GET_ICD9.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetICD9cm(final DataSource datasource, final String procS) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :icd9code_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ICD9(:rvs); end;");
            statement.registerOutParameter("icd9code_output", OracleTypes.CURSOR);
            statement.setString("rvs", procS);
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("icd9code_output");
            if (resultSet.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing Get ICD9CM Method");
            logger.error("Error in Get ICD9CM Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
