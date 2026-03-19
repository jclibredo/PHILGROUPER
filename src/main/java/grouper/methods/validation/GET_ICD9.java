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
public class GET_ICD9 {

    public GET_ICD9() {
    }
    private final Utility utility = new Utility();

//    public DRGWSResult GetICD9cm(final DataSource datasource, final String procS) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :get_icd9_DC := MINOSUN.DRGPKGFUNCTION.GET_ICD9CM_FINDDC(:ICD9CODE); end;");
//            statement.registerOutParameter("get_icd9_DC", OracleTypes.CURSOR);
//            statement.setString("ICD9CODE", procS);
//            statement.execute();
//            ResultSet resultSet = (ResultSet) statement.getObject("get_icd9_DC");
//            if (resultSet.next()) {
//                result.setSuccess(true);
//            }
//        } catch (SQLException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(GetICD9cm.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    public DRGWSResult GetICD9cm(final DataSource datasource, final String procS) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :icd9code_output := MINOSUN.DRGPKGFUNCTION.GET_ICD9(:rvs); end;");
            statement.registerOutParameter("icd9code_output", OracleTypes.CURSOR);
            statement.setString("rvs", procS);
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("icd9code_output");
            if (resultSet.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GET_ICD9.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
