/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.library;

import drgseeker.utilities.SeekerMethods;
import grouper.structures.DRGWSResult;
import grouper.structures.UserLogs;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author LAPTOP
 */
@RequestScoped
public class ServiceUserActivity {

    public ServiceUserActivity() {
    }
    private final Utility utility = new Utility();
//    private final SimpleDateFormat dateformat = utility.SimpleDateFormat("MM-dd-yyyy");
    private final SimpleDateFormat datetimeformat = utility.SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");

    public DRGWSResult CreateUserLogs(
            final DataSource dataSource,
            final String username,
            final String module,
            final String action,
            final String details) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        SeekerMethods seeker = new SeekerMethods();
        //GET USER FULLNAME
        DRGWSResult getName = seeker.GetUserByUsername(dataSource, username);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement getinsertresult = connection.prepareCall("call DRG_SHADOWBILLING.DRGSEEKER.INSERT_USER_LOGS(:Message,:Code,"
                    + ":u_username,:u_dateaction,:u_module,:u_action,:u_details)");
            getinsertresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            getinsertresult.registerOutParameter("Code", OracleTypes.INTEGER);
            getinsertresult.setString("u_username", getName.isSuccess() ? getName.getMessage() : "USER NOT FOUND");
            getinsertresult.setTimestamp("u_dateaction", new java.sql.Timestamp(utility.GetCurrentDate().getTime()));
            getinsertresult.setString("u_module", module);
            getinsertresult.setString("u_action", action.trim());
            getinsertresult.setString("u_details", details);
            getinsertresult.execute();
            if (getinsertresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(getinsertresult.getString("Message"));
            } else {
                result.setMessage("Something went wrong");
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ACTIVITY LOGS
    public DRGWSResult GetUserLogs(
            final DataSource dataSource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.DRGSEEKER.GETUSERLOGS(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            ArrayList<UserLogs> logsList = new ArrayList<>();
            while (resultset.next()) {
                UserLogs logs = new UserLogs();
                logs.setUsername(resultset.getString("USERNAME"));
                logs.setDataaction(datetimeformat.format(resultset.getTimestamp("DATEACTION")));
                logs.setModule(resultset.getString("MODULE"));
                logs.setAction(resultset.getString("ACTION"));
                logs.setDetails(resultset.getString("DETAILS"));
                logsList.add(logs);
            }
            if (logsList.size() > 0) {
                result.setMessage("OK");
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(logsList));
            } else {
                result.setMessage("No data found");
            }

        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
