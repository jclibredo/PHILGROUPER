/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DC;
import grouper.structures.DRGWSResult;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.IOException;
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
public class GetDC {

    public GetDC() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetDC(final DataSource datasource, final String dcs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetDC = connection.prepareCall("begin :dcs_output := MINOSUN.DRGPKGFUNCTION.GET_DC(:dcs); end;");
            GetDC.registerOutParameter("dcs_output", OracleTypes.CURSOR);
            GetDC.setString("dcs", dcs);
            GetDC.execute();
            ResultSet DCResultset = (ResultSet) GetDC.getObject("dcs_output");
            if (DCResultset.next()) {
                DC dcresults = new DC();
                dcresults.setDDCOL(DCResultset.getString("DCCOL"));
                dcresults.setDRGX(DCResultset.getString("DRGX"));
                dcresults.setCNAME(DCResultset.getString("CNAME"));
                dcresults.setDC(DCResultset.getString("DC"));
                dcresults.setMDC(DCResultset.getString("MDC"));
                result.setResult(utility.objectMapper().writeValueAsString(dcresults));
                result.setSuccess(true);
                result.setMessage(DCResultset.getString("DCCOL"));
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
