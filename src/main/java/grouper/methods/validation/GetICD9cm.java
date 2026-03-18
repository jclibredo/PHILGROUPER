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
public class GetICD9cm {
    
     public GetICD9cm() {
    }
    private final Utility utility = new Utility();

    
    
     public DRGWSResult GetICD9cm(final DataSource datasource, final String procS) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement_icd9_findDC = connection.prepareCall("begin : get_icd9_DC := MINOSUN.DRGPKGFUNCTION.GET_ICD9CM_FINDDC(:procS); end;");
            statement_icd9_findDC.registerOutParameter("get_icd9_DC", OracleTypes.CURSOR);
            statement_icd9_findDC.setString("procS", procS);
            statement_icd9_findDC.execute();
            ResultSet resultset_icd9_findDC = (ResultSet) statement_icd9_findDC.getObject("get_icd9_DC");
            if (resultset_icd9_findDC.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetICD9cm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
}
