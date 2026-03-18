/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

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
public class MDC {

    public MDC() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult MDC(final DataSource datasource, final String mdc) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :mdc_output := MINOSUN.DRGPKGFUNCTION.GET_MDC(:mdcs); end;");
            statement.registerOutParameter("mdc_output", OracleTypes.CURSOR);
            statement.setString("mdcs", mdc);
            statement.execute();
            ResultSet mdcresultset = (ResultSet) statement.getObject("mdc_output");
            if (mdcresultset.next()) {
                grouper.structures.MDC mdcData = new grouper.structures.MDC();
                mdcData.setMDC(mdcresultset.getString("MDC"));
                mdcData.setDESCRIPTION(mdcresultset.getString("DESCRIPTION"));
                mdcData.setLABEL(mdcresultset.getString("LABEL"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(mdcData));
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(MDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
