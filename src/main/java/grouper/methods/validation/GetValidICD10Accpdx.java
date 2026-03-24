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
public class GetValidICD10Accpdx {

    public GetValidICD10Accpdx() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetValidICD10Accpdx(final DataSource datasource, final String p_pdx_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getAccpdx = connection.prepareCall("begin :accpdxs := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ACCPDX_VALUE(:p_pdx_code); end;");
            getAccpdx.registerOutParameter("accpdxs", OracleTypes.CURSOR);
            getAccpdx.setString("p_pdx_code", p_pdx_code);
            getAccpdx.execute();
            ResultSet accpdxResult = (ResultSet) getAccpdx.getObject("accpdxs");
            if (accpdxResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetValidICD10Accpdx.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
