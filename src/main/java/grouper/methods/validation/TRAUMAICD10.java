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
public class TRAUMAICD10 {

    public TRAUMAICD10() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult TRAUMAICD10(final DataSource datasource, final String sdx) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :trauma_output := MINOSUN.DRGPKGFUNCTION.TRAUMAICD10(:sdx); end;");
            statement.registerOutParameter("trauma_output", OracleTypes.CURSOR);
            statement.setString("sdx", sdx);
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("trauma_output");
            if (resultSet.next()) {
                result.setResult(resultSet.getString("TRAUMA"));
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(TRAUMAICD10.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
