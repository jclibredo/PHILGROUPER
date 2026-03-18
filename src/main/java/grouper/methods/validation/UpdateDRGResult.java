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
public class UpdateDRGResult {

    public UpdateDRGResult() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult UpdateDRGResult(final DataSource datasource,
            final String mdcs,
            final String pdcs,
            final String dcs,
            final String result_id,
            final String series,
            final String drg,
            final String drgdetails) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement updatedrgresult = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.UPDATE_DRG_RESULT(:Message,"
                    + ":Code,:umdc,:updc,:udc,:uresultid,:useries,:utags,:udrg,:udrgdetails)");
            updatedrgresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            updatedrgresult.registerOutParameter("Code", OracleTypes.INTEGER);
            updatedrgresult.setString("umdc", mdcs);
            updatedrgresult.setString("updc", pdcs);
            updatedrgresult.setString("udc", dcs);
            updatedrgresult.setString("uresultid", result_id);
            updatedrgresult.setString("useries", series);
            updatedrgresult.setString("utags", "DG".trim());
            updatedrgresult.setString("udrg", drg);
            updatedrgresult.setString("udrgdetails", drgdetails);
            updatedrgresult.executeUpdate();
            if (updatedrgresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            }
            result.setMessage(updatedrgresult.getString("Code"));
            result.setResult(updatedrgresult.getString("Message"));
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(UpdateDRGResult.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
