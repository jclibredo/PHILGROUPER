/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

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
public class GetPDCUsePDx {

    public GetPDCUsePDx() {
    }

    public String GetPDCUsePDx(final DataSource datasource, final String pdx) {
        String result = "";
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getPDCUsePDx = connection.prepareCall("begin :pdc_pdx := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PDC_USE_PDX(:primaryPDx); end;");
            getPDCUsePDx.registerOutParameter("pdc_pdx", OracleTypes.CURSOR);
            getPDCUsePDx.setString("primaryPDx", pdx);
            getPDCUsePDx.execute();
            ResultSet PDCPDxResultset = (ResultSet) getPDCUsePDx.getObject("pdc_pdx");
            if (PDCPDxResultset.next()) {
                result = PDCPDxResultset.getString("PDC");
            }
        } catch (SQLException ex) {
            result = "Something went wrong";
            Logger.getLogger(GetPDCUsePDx.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
