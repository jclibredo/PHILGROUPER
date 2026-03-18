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
public class CheckExclusionList {

    public CheckExclusionList() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult CheckExclusionList(final DataSource datasource, final String sdx, final String pdx) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement exclusionlist = connection.prepareCall("begin :getexclusion := MINOSUN.DRGPKGFUNCTION.GET_EXCLUSION(:sdx,:pdx); end;");
            exclusionlist.registerOutParameter("getexclusion", OracleTypes.CURSOR);
            exclusionlist.setString("sdx", sdx);
            exclusionlist.setString("pdx", pdx);
            exclusionlist.execute();
            ResultSet EndovascResult = (ResultSet) exclusionlist.getObject("getexclusion");
            if (EndovascResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(CheckExclusionList.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
