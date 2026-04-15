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
public class GetDA {

    public GetDA() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetDA.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetDA(final DataSource datasource, final String dagger, final String asterisk) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement conn = connection.prepareCall("begin :DaggerAs := DRG_SHADOWBILLING.DRGPKGFUNCTION.get_da(:daggers,:ASterisks); end;");
            conn.registerOutParameter("DaggerAs", OracleTypes.CURSOR);
            conn.setString("daggers", dagger.toUpperCase().trim());
            conn.setString("ASterisks", asterisk.toUpperCase().trim());
            conn.execute();
            ResultSet connResult = (ResultSet) conn.getObject("DaggerAs");
            if (connResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetDA Method");
            logger.error("Error in GetDA Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
