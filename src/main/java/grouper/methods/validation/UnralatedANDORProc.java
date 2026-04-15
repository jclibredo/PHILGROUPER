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
public class UnralatedANDORProc {

    public UnralatedANDORProc() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(UnralatedANDORProc.class);
    private final Utility utility = new Utility();

    public DRGWSResult UnralatedANDORProc(final DataSource datasource, final String icd9codes, final String mdccode) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :unralated_or_proc := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_UNRALATED_PROC_ORPROC(:icd9code,:mdcs); end;");
            statement.registerOutParameter("unralated_or_proc", OracleTypes.CURSOR);
            statement.setString("icd9code", icd9codes);
            statement.setString("mdcs", mdccode);
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("unralated_or_proc");
            if (resultSet.next()) {
                result.setSuccess(true);
                result.setResult(resultSet.getString("PROCGR"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing UnralatedANDORProc Method");
            logger.error("Error in UnralatedANDORProc Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
