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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class GET_CONVERTER {

    public GET_CONVERTER() {
    }

    private final Logger logger = (Logger) LogManager.getLogger(GET_CONVERTER.class);
    private final Utility utility = new Utility();

    public DRGWSResult GET_CONVERTER(final DataSource datasource, final String rvs_code) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            String ProcListNew = "";
            List<String> FinalNewProcList = new ArrayList<>();
            CallableStatement statement = connection.prepareCall("begin :converter := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
            statement.registerOutParameter("converter", OracleTypes.CURSOR);
            statement.setString("rvs_code", rvs_code);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("converter");
            if (resultset.next()) {
                ProcListNew = resultset.getString("ICD9CODE");
                List<String> ConverterResult = Arrays.asList(ProcListNew.split(","));
                for (int g = 0; g < ConverterResult.size(); g++) {
                    String ICD9Codes = ConverterResult.get(g);
                    FinalNewProcList.add(ICD9Codes);
                }
                result.setResult(ProcListNew);
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing Get converter Method");
            logger.error("Error in Get converter Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult ValidateRVS(final DataSource datasource, final String rvs_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :converter := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
            statement.registerOutParameter("converter", OracleTypes.CURSOR);
            statement.setString("rvs_code", rvs_code.trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("converter");
            if (resultset.next()) {
                if (resultset.getString("ICD9CODE").trim() != null
                        || resultset.getString("ICD9CODE").trim().equals("")
                        || !resultset.getString("ICD9CODE").trim().isEmpty()) {
                    result.setResult(resultset.getString("ICD9CODE").trim());
                    result.setSuccess(true);
                } else {
                    result.setMessage("RVS " + rvs_code + " icd9cm not found");
                }
            } else {
                result.setMessage("RVS " + rvs_code + " invalid");
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing RVS Validate");
            logger.error("Error in RVS Validate Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
