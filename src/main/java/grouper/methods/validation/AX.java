/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
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
public class AX {

    public AX() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult AX(final DataSource datasource, final String axcodes, final String requestcode) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :get_ax := MINOSUN.DRGPKGFUNCTION.GET_AX_PARAM(:axcodes); end;");
            statement.registerOutParameter("get_ax", OracleTypes.CURSOR);
            statement.setString("axcodes", axcodes.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("get_ax");
            if (resultSet.next()) {
                List<String> codelist = Arrays.asList(resultSet.getString("CODES").split(","));
                for (int x = 0; x < codelist.size(); x++) {
                    if (requestcode.trim().equals(codelist.get(x).trim())) {
                        result.setResult(requestcode);
                        result.setSuccess(true);
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(AX.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
