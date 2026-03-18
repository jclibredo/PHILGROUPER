/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.BMDCPreMDCResult;
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
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author MinoSun
 */
public class GetBMDC {

    public GetBMDC() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetBMDC(final DataSource datasource, final String p_pdx_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetBMDC = connection.prepareCall("begin :bmdc_validation := MINOSUN.DRGPKGFUNCTION.GET_BMDC_VALIDATION_PREMDC(:p_pdx_code); end;");
            GetBMDC.registerOutParameter("bmdc_validation", OracleTypes.CURSOR);
            GetBMDC.setString("p_pdx_code", p_pdx_code);
            GetBMDC.execute();
            ResultSet bmdcresultset = (ResultSet) GetBMDC.getObject("bmdc_validation");
            if (bmdcresultset.next()) {
                BMDCPreMDCResult bmdcResult = new BMDCPreMDCResult();
                bmdcResult.setICD10(bmdcresultset.getString("ICD10"));
                bmdcResult.setMDC_F(bmdcresultset.getString("MDC_F"));
                bmdcResult.setPDC_F(bmdcresultset.getString("PDC_F"));
                bmdcResult.setMDC_M(bmdcresultset.getString("MDC_M"));
                bmdcResult.setPDC_M(bmdcresultset.getString("PDC_M"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(bmdcResult));
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetBMDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
