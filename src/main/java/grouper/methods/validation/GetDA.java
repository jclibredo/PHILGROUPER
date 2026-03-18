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
public class GetDA {

    public GetDA() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetDA(final DataSource datasource, final String dagger, final String asterisk) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement conn = connection.prepareCall("begin :DaggerAs := MINOSUN.DRGPKGFUNCTION.get_da(:dagger,:asterisk); end;");
            conn.registerOutParameter("DaggerAs", OracleTypes.CURSOR);
            conn.setString("dagger", dagger.toUpperCase().trim());
            conn.setString("asterisk", asterisk.toUpperCase().trim());
            conn.execute();
            ResultSet connResult = (ResultSet) conn.getObject("DaggerAs");
            if (connResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetDA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
