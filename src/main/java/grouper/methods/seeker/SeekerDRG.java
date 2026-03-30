/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.seeker;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class SeekerDRG {

    public SeekerDRG() {
    }
    private final Utility utility = new Utility();
    
     public DRGWSResult SeekerDRG(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        ArrayList<DRGOutput> drgList = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGFUNCTION.SeekerDRG(); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_results");
            while (resultset.next()) {
                DRGOutput drg = new DRGOutput();
                drg.setDC(resultset.getString("DC"));
                drg.setDRG(resultset.getString("DRG"));
                drg.setDRGName(resultset.getString("DRGNAME"));
                drg.setMDC(resultset.getString("MDC"));
                drg.setRW(resultset.getString("RW"));
                drgList.add(drg);
            }
            if (drgList.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(drgList));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(SeekerDRG.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
