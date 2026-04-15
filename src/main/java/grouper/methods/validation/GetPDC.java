/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.structures.PDC;
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
public class GetPDC {

    public GetPDC() {
    }

    private final Logger logger = (Logger) LogManager.getLogger(GetPDC.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetPDC(final DataSource datasource, final String pdcs, final String mdc) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            //Get BMDC Validation Result
            CallableStatement getPDC = connection.prepareCall("begin :pdc_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PDC(:pdcs,:mdcs); end;");
            getPDC.registerOutParameter("pdc_output", OracleTypes.CURSOR);
            getPDC.setString("pdcs", pdcs);
            getPDC.setString("mdcs", mdc);
            getPDC.execute();
            ResultSet Pdcresultset = (ResultSet) getPDC.getObject("pdc_output");
            if (Pdcresultset.next()) {
                PDC pdcReult = new PDC();
                pdcReult.setPDC(Pdcresultset.getString("PDC"));
                pdcReult.setCTYPE(Pdcresultset.getString("CTYPE"));
                pdcReult.setHIERAR(Pdcresultset.getInt("HIERAR"));
                pdcReult.setCNAME(Pdcresultset.getString("CNAME"));
                pdcReult.setMDC(Pdcresultset.getString("MDC"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(pdcReult));
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetPDC Method");
            logger.error("Error in GetPDC Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
