/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.library;

import grouper.structures.DRGWSResult;
import grouper.structures.ICD10;
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
public class ServicesI10VX {

    public ServicesI10VX() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(ServicesI10VX.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetIcd10(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        ArrayList<ICD10> icd10List = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGLIBRARY.GET_ICD10(); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_results");
            while (resultset.next()) {
                ICD10 icd10 = new ICD10();
                icd10.setValidcode(resultset.getString("VALIDCODE"));
                icd10.setDescription(resultset.getString("DESCRIPTION"));
                icd10.setCode(resultset.getString("CODE"));
                icd10List.add(icd10);
            }
            if (icd10List.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(icd10List));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetIcd10 Method");
            logger.error("Error in GetIcd10 Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult CreateIcd10(final DataSource datasource,
            final String validcode,
            final String description,
            final String code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.INSERT_ICD10(:Message,:Code,"
                    + ":u_val,:u_desc,:u_code)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            auditrail.setString("u_val", validcode);
            auditrail.setString("u_desc", description);
            auditrail.setString("u_code", code);
            auditrail.execute();
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(auditrail.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing CreateIcd10 Method");
            logger.error("Error in CreateIcd10 Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult DeleteIcd10(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.DELETE_ALL_ICD10(:Message,:Code)");
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
            logger.info("Executing DeleteIcd10 Method");
            logger.error("Error in DeleteIcd10 Method : {}", ex.getMessage(), ex);
        }
        return result;
    }
}
