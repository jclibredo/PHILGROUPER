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
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MinoSun
 */
public class GetClaimDuplication {

    public GetClaimDuplication() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetClaimDuplication(final DataSource datasource, final String accre, final String claimnum) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getduplication = connection.prepareCall("begin :dupnclaims := MINOSUN.DRGPKGFUNCTION.GET_CHECK_DUPLICATE(:claimnum,:accre); end;");
            getduplication.registerOutParameter("dupnclaims", OracleTypes.CURSOR);
            getduplication.setString("claimnum", claimnum);
            getduplication.setString("accre", accre);
            getduplication.execute();
            ResultSet getduplicationResult = (ResultSet) getduplication.getObject("dupnclaims");
            if (getduplicationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetClaimDuplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
