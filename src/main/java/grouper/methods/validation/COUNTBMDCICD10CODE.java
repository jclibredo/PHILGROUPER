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
public class COUNTBMDCICD10CODE {

    public COUNTBMDCICD10CODE() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(COUNTBMDCICD10CODE.class);
    private final Utility utility = new Utility();

    public DRGWSResult COUNTBMDCICD10CODE(final DataSource datasource, final String icd10code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGFUNCTION.COUNTBMDCICD10CODE(:icd10code); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.setString("icd10code", icd10code.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("v_results");
            if (resultSet.next()) {
                if (Integer.parseInt(resultSet.getString("CODECOUNT")) > 1) {
                    result.setSuccess(true);
                }
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing count bmdc Method");
            logger.error("Error in count bmdc Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
