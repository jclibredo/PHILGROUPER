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
public class UnralatedANDORProc {

    public UnralatedANDORProc() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult UnralatedANDORProc(final DataSource datasource, final String icd9codes, final String mdccode) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :unralated_or_proc := MINOSUN.DRGPKGFUNCTION.GET_UNRALATED_PROC_ORPROC(:icd9codes,:mdccode); end;");
            statement.registerOutParameter("unralated_or_proc", OracleTypes.CURSOR);
            statement.setString("icd9codes", icd9codes);
            statement.setString("mdccode", mdccode);
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("unralated_or_proc");
            if (resultSet.next()) {
                result.setSuccess(true);
                result.setResult(resultSet.getString("PROCGR"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(UnralatedANDORProc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
