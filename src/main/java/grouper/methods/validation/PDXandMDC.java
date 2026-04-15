/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.structures.ICD10PreMDCResult;
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
public class PDXandMDC {

    public PDXandMDC() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(PDXandMDC.class);
    private final Utility utility = new Utility();

    //PDX used to find MDC
    public DRGWSResult PDXandMDC(final DataSource datasource, final String pdx, final String mdc) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :pdxmdc := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PDX_MDC(:p_pdx_code,:mdcs); end;");
            statement.registerOutParameter("pdxmdc", OracleTypes.CURSOR);
            statement.setString("p_pdx_code", pdx);
            statement.setString("mdcs", mdc);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("pdxmdc");
            if (resultset.next()) {
                ICD10PreMDCResult premdc = new ICD10PreMDCResult();
                premdc.setAccPDX(resultset.getString("ACCPDX"));
                premdc.setAgeDMin(resultset.getString("AGEDMIN"));
                premdc.setAgeDUse(resultset.getString("AGEDUSE"));
                premdc.setAgeMax(resultset.getString("AGEMAX"));
                premdc.setAgeMin(resultset.getString("AGEMIN"));
                premdc.setCC(resultset.getString("CC"));
                premdc.setCCRow(resultset.getString("CCROW"));
                premdc.setCode(resultset.getString("CODE"));
                premdc.setHIV_AX(resultset.getString("HIV_AX"));
                premdc.setMDC(resultset.getString("MDC"));
                premdc.setMainCC(resultset.getString("MAINCC"));
                premdc.setPDC(resultset.getString("PDC"));
                premdc.setSex(resultset.getString("SEX"));
                premdc.setTrauma(resultset.getString("TRAUMA"));
                result.setSuccess(true);
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(premdc));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing PDXandMDC Method");
            logger.error("Error in PDXandMDC Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
