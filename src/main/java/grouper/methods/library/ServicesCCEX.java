/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.library;

import grouper.structures.CCEX;
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
public class ServicesCCEX {

    public ServicesCCEX() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(ServicesCCEX.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetCCEX(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        ArrayList<CCEX> ccexList = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGLIBRARY.GET_CCEX(); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_results");
            while (resultset.next()) {
                CCEX ccex = new CCEX();
                ccex.setSdx(resultset.getString("SDX"));
                ccex.setPdx(resultset.getString("PDX"));
                ccexList.add(ccex);
            }
            if (ccexList.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(ccexList));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetCCEX Method");
            logger.error("Error in GetCCEX Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult CreateCCEX(final DataSource datasource,
            final String sdx,
            final String pdx) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.INSERT_CCEX(:Message,:Code,"
                    + ":u_sdx,:u_pdx)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("u_sdx", sdx);
            statement.setString("u_pdx", pdx);
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing CreateCCEX Method");
            logger.error("Error in CreateCCEX Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult DeleteCCEX(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.DELETE_ALL_CCEX(:Message,:Code)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing DeleteCCEX Method");
            logger.error("Error in DeleteCCEX Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
