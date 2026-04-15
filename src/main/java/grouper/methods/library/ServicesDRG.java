/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.library;

import grouper.structures.DRGOutput;
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
public class ServicesDRG {

    public ServicesDRG() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(ServicesDRG.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetDrg(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        ArrayList<DRGOutput> drgList = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.DRGPKGLIBRARY.GET_DRG(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                DRGOutput drg = new DRGOutput();
                drg.setRW(resultset.getString("RW"));
                drg.setWTLOS(resultset.getString("WTLOS"));
                drg.setOT(resultset.getString("OT"));
                drg.setMDF(resultset.getString("MDF"));
                drg.setDRGName(resultset.getString("DRGNAME"));
                drg.setDRG(resultset.getString("DRG"));
                drg.setMDC(resultset.getString("MDC"));
                drg.setDC(resultset.getString("DC"));
                drgList.add(drg);
            }
            if (drgList.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(drgList));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetDrg Method");
            logger.error("Error in GetDrg Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult CreateDrg(final DataSource datasource,
            final String rw,
            final String wtlos,
            final String ot,
            final String mdf,
            final String drgname,
            final String drg,
            final String mdc,
            final String dc) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.INSERT_DRG(:Message,:Code,"
                    + ":u_rw,:u_wtlos,:u_ot,:u_mdf,:u_drgname,:u_drg,:u_mdc,:u_dc)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            auditrail.setString("u_rw", rw);
            auditrail.setString("u_wtlos", wtlos);
            auditrail.setString("u_ot", ot);
            auditrail.setString("u_mdf", mdf);
            auditrail.setString("u_drgname", drgname);
            auditrail.setString("u_drg", drg);
            auditrail.setString("u_mdc", mdc);
            auditrail.setString("u_dc", dc);
            auditrail.execute();
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(auditrail.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing CreateDrg Method");
            logger.error("Error in CreateDrg Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult DeleteDrg(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.DELETE_ALL_DRG(:Message,:Code)");
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
            logger.info("Executing DeleteDrg Method");
            logger.error("Error in DeleteDrg Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
