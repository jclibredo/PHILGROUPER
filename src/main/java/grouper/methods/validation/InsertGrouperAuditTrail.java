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
public class InsertGrouperAuditTrail {

    public InsertGrouperAuditTrail() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult InsertGrouperAuditTrail(final DataSource datasource,
            final String p_series,
            final String p_claimnumber,
            final String p_details,
            final String p_status) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call MINOSUN.DRGPKGPROCEDURE.INSERT_GROUPER_AUDITRAIL(:Message,:Code,"
                    + ":udatein,:useries,:uclaimnumber,:udesc,:ustats)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            auditrail.setString("udatein", utility.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
            auditrail.setString("useries", p_series);
            auditrail.setString("uclaimnumber", p_claimnumber);
            auditrail.setString("udesc", p_details);
            auditrail.setString("ustats", p_status);
            auditrail.execute();
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(auditrail.getString("Message"));
            } else {
                result.setMessage(auditrail.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
