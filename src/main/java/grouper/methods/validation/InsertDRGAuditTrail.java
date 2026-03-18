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
import java.util.Date;
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
public class InsertDRGAuditTrail {

    public InsertDRGAuditTrail() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult InsertDRGAuditTrail(final DataSource datasource,
            final String type,
            final String request,
            final String details) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_AUDITRAIL(:Message,:Code,:datetime,:details,:type,:request)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            auditrail.setString("datetime", utility.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
            auditrail.setString("details", details);
            auditrail.setString("type", type);
            auditrail.setString("request", request);
            auditrail.executeUpdate();
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            }
            result.setMessage(auditrail.getString("Code"));
            result.setResult(auditrail.getString("Message"));
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(InsertDRGAuditTrail.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
