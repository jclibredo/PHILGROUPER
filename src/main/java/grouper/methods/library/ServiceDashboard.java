/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.library;

import grouper.structures.DRGWSResult;
import grouper.structures.DashboardView;
import grouper.utility.Utility;
import java.io.IOException;
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
 * @author LAPTOP
 */
@RequestScoped
public class ServiceDashboard {

    public ServiceDashboard() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetDashboard(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.DRGPKGLIBRARY.GET_DASHBOARD(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                DashboardView dashboard = new DashboardView();
                dashboard.setI10(resultset.getString("I10_TOTAL"));
                dashboard.setI10vx(resultset.getString("I10VX_TOTAL"));
                dashboard.setCcex(resultset.getString("CCEX_TOTAL"));
                dashboard.setDrg(resultset.getString("DRG_TOTAL"));
                dashboard.setPdc(resultset.getString("PDC_TOTAL"));
                dashboard.setPcom(resultset.getString("PCOM_TOTAL"));
                dashboard.setMdc(resultset.getString("MDC_TOTAL"));
                dashboard.setDcv(resultset.getString("DCV_TOTAL"));
                dashboard.setAx(resultset.getString("AX_TOTAL"));
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(dashboard));
                result.setSuccess(true);
            }

        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ServiceDashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
