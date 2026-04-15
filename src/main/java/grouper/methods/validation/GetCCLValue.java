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
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MinoSun
 */
@RequestScoped
public class GetCCLValue {

    public GetCCLValue() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetCCLValue.class);

    public int GetCCLValue(final DataSource datasource, final String dccol, final String ccrows) {
        int result = 0;
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getccrowval = connection.prepareCall("begin :cclvalue := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CCLVALUE(:ccrows); end;");
            getccrowval.registerOutParameter("cclvalue", OracleTypes.CURSOR);
            getccrowval.setString("ccrows", ccrows);
            getccrowval.execute();
            ResultSet cclList = (ResultSet) getccrowval.getObject("cclvalue");
            if (cclList.next()) {
                //Get Value suing inderx off
                String longstring = cclList.getString("CCL");
                int x = Integer.parseInt(dccol);
                String cclval = longstring.substring(x - 1, Integer.parseInt(dccol));
                result = Integer.parseInt(cclval);
            }
        } catch (SQLException ex) {
            logger.info("Executing GetCCLValue Method");
            logger.error("Error in GetCCLValue Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
