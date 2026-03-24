/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package drgseeker.utilities;

import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
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
public class DataWarehouse {

    private final Utility utility = new Utility();

//    public DRGWSResult getHospital(final DataSource dataSource, final Connection conn) {
//        DRGWSResult result = utility.DRGWSResult();
//        try (Connection connection = dataSource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :v_result := ACR_GB.ACRGBPKG.ACR_HCF(); end;");
//            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("v_result");
//            while (resultset.next()) {
//                resultset.getString("HCFNAME");
//                resultset.getString("HCFCODE");
//
//            }
//        } catch (SQLException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    public DRGWSResult getICD10cm(final DataSource dataSource) {
        DRGWSResult result = utility.DRGWSResult();
        String url = "jdbc:snowflake://TBRRSYQ-CR67548.snowflakecomputing.com/";
        Properties prop = new Properties();
        prop.put("user", "JCLIBREDO");
        prop.put("password", "1234Fraternitas^");
        prop.put("db", "DRG_DB");
        prop.put("schema", "DRG_SCHEMA");
        prop.put("warehouse", "COMPUTE_WH");
        try (Connection connection = dataSource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :a_result := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ALL_ICD10(); end;");
            statement.registerOutParameter("a_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("a_result");
            while (resultset.next()) {
                try (Connection conn = DriverManager.getConnection(url, prop)) {
                    PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO DRG_DB.DRG_SCHEMA.ICD10CM(CODE,DETAILS) VALUES (?, ?)");
                    preparedStatement.setString(1, resultset.getString("CODE"));
                    preparedStatement.setString(2, resultset.getString("DESCRIPTION"));
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(SeekerMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//    public DRGWSResult getPatient(final DataSource dataSource, final Connection conn) {
//        DRGWSResult result = utility.DRGWSResult();
//        return result;
//    }
//
//    public DRGWSResult postDataWarehouse(final DataSource dataSource, final Connection conn) {
//        DRGWSResult result = utility.DRGWSResult();
//        return result;
//    }
//
//    public DRGWSResult sentEmailLogs(final DataSource dataSource, final Connection conn) {
//        DRGWSResult result = utility.DRGWSResult();
//        return result;
//    }
}
