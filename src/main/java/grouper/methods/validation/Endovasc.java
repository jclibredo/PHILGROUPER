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
public class Endovasc {
    
    public Endovasc() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult Endovasc(final DataSource datasource, final String proce, final String pdcs, final String mdcs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement Endovasc = connection.prepareCall("begin :get_icd9_cm := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ICD9_MDC(:icd9code,:p_pdc,:p_mdc); end;");
            Endovasc.registerOutParameter("get_icd9_cm", OracleTypes.CURSOR);
            Endovasc.setString("icd9code", proce);
            Endovasc.setString("p_pdc", pdcs);
            Endovasc.setString("p_mdc", mdcs);
            Endovasc.execute();
            ResultSet EndovascResult = (ResultSet) Endovasc.getObject("get_icd9_cm");
            if (EndovascResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(Endovasc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
