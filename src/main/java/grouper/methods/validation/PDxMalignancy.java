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
public class PDxMalignancy {

    public PDxMalignancy() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult PDxMalignancy(final DataSource datasource, final String primaryPDx, final String pdcs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetMalignantPDx = connection.prepareCall("begin :pdx_malignant := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PDX_MALIGNANCY(:primaryPDx,:pdcs); end;");
            GetMalignantPDx.registerOutParameter("pdx_malignant", OracleTypes.CURSOR);
            GetMalignantPDx.setString("primaryPDx", primaryPDx);
            GetMalignantPDx.setString("pdcs", pdcs);
            GetMalignantPDx.execute();
            ResultSet MalignantResultset = (ResultSet) GetMalignantPDx.getObject("pdx_malignant");
            if (MalignantResultset.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(PDxMalignancy.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
