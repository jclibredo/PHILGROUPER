/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import java.io.IOException;
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
public class DRG {

    public DRG() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(DRG.class);
    private final Utility utility = new Utility();

    public DRGWSResult DRG(final DataSource datasource, final String dcs, final String drgs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement Getdrg = connection.prepareCall("begin :drg_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_DRG(:dcs,:drgs); end;");
            Getdrg.registerOutParameter("drg_output", OracleTypes.CURSOR);
            Getdrg.setString("dcs", dcs);
            Getdrg.setString("drgs", drgs);
            Getdrg.execute();
            ResultSet drgresultset = (ResultSet) Getdrg.getObject("drg_output");
            if (drgresultset.next()) {
                DRGOutput drgOutput = new DRGOutput();
                drgOutput.setRW(drgresultset.getString("RW"));
                drgOutput.setWTLOS(drgresultset.getString("WTLOS"));
                drgOutput.setOT(drgresultset.getString("OT"));
                drgOutput.setMDF(drgresultset.getString("MDF"));
                drgOutput.setDRGName(drgresultset.getString("DRGNAME"));
                drgOutput.setDRG(drgresultset.getString("DRG"));
                drgOutput.setMDC(drgresultset.getString("MDC"));
                drgOutput.setDC(drgresultset.getString("DC"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(drgOutput));
                result.setMessage(drgOutput.getDRGName());
            }
        } catch (IOException | SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing DRG Method");
            logger.error("Error in DRG Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
