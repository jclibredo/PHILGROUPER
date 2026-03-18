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
public class ORProcedure {

    public ORProcedure() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult ORProcedure(final DataSource datasource, final String orpCode) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetORproce = connection.prepareCall("begin :get_orp := MINOSUN.DRGPKGFUNCTION.GET_PROC_ORP(:orpCode); end;");
            GetORproce.registerOutParameter("get_orp", OracleTypes.CURSOR);
            GetORproce.setString("orpCode", orpCode);
            GetORproce.execute();
            ResultSet ORProceResultset = (ResultSet) GetORproce.getObject("get_orp");
            if (ORProceResultset.next()) {
                result.setSuccess(true);
                result.setMessage(ORProceResultset.getString("PROC_SITE"));
                result.setResult(ORProceResultset.getString("PROCGR"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ORProcedure.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
