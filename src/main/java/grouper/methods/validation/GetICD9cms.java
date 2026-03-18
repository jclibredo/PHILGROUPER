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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MinoSun
 */
public class GetICD9cms {

    public GetICD9cms() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetICD9cms(final DataSource datasource, final String rvs_code) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            String ProcListNew = "";
            List<String> FinalNewProcList = new ArrayList<>();
            CallableStatement statement = connection.prepareCall("begin :converter := MINOSUN.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
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
            Logger.getLogger(GetICD9cms.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
