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
public class TRAUMAICD9CM {

    public TRAUMAICD9CM() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(TRAUMAICD9CM.class);
    private final Utility utility = new Utility();

    public DRGWSResult TRAUMAICD9CM(final DataSource datasource, final String icdproc) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement conn = connection.prepareCall("begin :trauma_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.TRAUMAICD9CM(:icdproc); end;");
            conn.registerOutParameter("trauma_output", OracleTypes.CURSOR);
            conn.setString("icdproc", icdproc);
            conn.execute();
            ResultSet connResult = (ResultSet) conn.getObject("trauma_output");
            if (connResult.next()) {
                result.setResult(connResult.getString("PROC_SITE"));
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing TRAUMAICD9CM Method");
            logger.error("Error in TRAUMAICD9CM Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

}
