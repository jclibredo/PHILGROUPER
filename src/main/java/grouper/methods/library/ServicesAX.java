/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.library;

import grouper.structures.AX;
import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author LAPTOP
 */
@RequestScoped
public class ServicesAX {

    public ServicesAX() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(ServicesAX.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetAx(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        ArrayList<AX> axList = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :validate_ax := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_AX(); end;");
            statement.registerOutParameter("validate_ax", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("validate_ax");
            while (resultset.next()) {
                AX ax = new AX();
                ax.setAx(resultset.getString("AX"));
                ax.setCodes(resultset.getString("CODES"));
                axList.add(ax);
            }
            if (axList.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(axList));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetAx Method");
            logger.error("Error in GetAx Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult CreateAx(final DataSource datasource,
            final String u_ax,
            final String u_codes) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.INSERT_AX(:Message,:Code,"
                    + ":u_ax,:u_codes)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("u_ax", u_ax);
            statement.setString("u_codes", u_codes);
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing CreateAx Method");
            logger.error("Error in CreateAx Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult DeleteAx(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.DELETE_ALL_AX(:Message,:Code)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            auditrail.execute();
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(auditrail.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing DeleteAx Method");
            logger.error("Error in DeleteAx Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
