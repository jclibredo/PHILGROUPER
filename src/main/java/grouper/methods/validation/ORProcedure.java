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
public class ORProcedure {

    public ORProcedure() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(ORProcedure.class);
    private final Utility utility = new Utility();

    public DRGWSResult ORProcedure(final DataSource datasource, final String orpCode) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetORproce = connection.prepareCall("begin :get_orp := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PROC_ORP(:orpCode); end;");
            GetORproce.registerOutParameter("get_orp", OracleTypes.CURSOR);
            GetORproce.setString("orpCode", orpCode);
            GetORproce.execute();
            ResultSet resultSet = (ResultSet) GetORproce.getObject("get_orp");
            if (resultSet.next()) {
                result.setSuccess(true);
                result.setMessage(resultSet.getString("PROC_SITE"));
                result.setResult(resultSet.getString("PROCGR"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing ORProcedure Method");
            logger.error("Error in ORProcedure Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
