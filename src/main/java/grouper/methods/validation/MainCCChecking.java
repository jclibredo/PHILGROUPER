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
public class MainCCChecking {

    public MainCCChecking() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult MainCCChecking(final DataSource datasource, final String ccCode, final String mdcCode) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetMainCC = connection.prepareCall("begin :maincc := MINOSUN.DRGPKGFUNCTION.GET_MAINCC_USED_ICD10(:ccCode,:mdcCode); end;");
            GetMainCC.registerOutParameter("maincc", OracleTypes.CURSOR);
            GetMainCC.setString("ccCode", ccCode);
            GetMainCC.setString("mdcCode", mdcCode);
            GetMainCC.execute();
            ResultSet MainCCResultset = (ResultSet) GetMainCC.getObject("maincc");
            if (MainCCResultset.next()) {
                result.setResult(ccCode);
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(MainCCChecking.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
