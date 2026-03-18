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
public class GETPATIENTBDAY {

    public GETPATIENTBDAY() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GETPATIENTBDAY(
            final DataSource datasource,
            final String seriesnum) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :nclaims := MINOSUN.UHCDRGPKG.GET_NCLAIMS(:seriesnum); end;");
            statement.registerOutParameter("nclaims", OracleTypes.CURSOR);
            statement.setString("seriesnum", seriesnum.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("nclaims");
            if (resultSet.next()) {
                if (resultSet.getString("DATEOFBIRTH") == null || resultSet.getString("DATEOFBIRTH").isEmpty() || resultSet.getString("DATEOFBIRTH").equals("")) {
                } else {
                    result.setSuccess(true);
                    result.setMessage("OK");
                    result.setResult(utility.SimpleDateFormat("MM-dd-yyyy").format(resultSet.getTimestamp("DATEOFBIRTH")));
                }
            }
        } catch (Exception ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GETPATIENTBDAY.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
