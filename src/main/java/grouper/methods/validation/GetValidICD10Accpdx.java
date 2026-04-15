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
public class GetValidICD10Accpdx {

    public GetValidICD10Accpdx() {
    }

    private final Logger logger = (Logger) LogManager.getLogger(GetValidICD10Accpdx.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetValidICD10Accpdx(final DataSource datasource, final String p_pdx_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getAccpdx = connection.prepareCall("begin :accpdxs := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ACCPDX_VALUE(:p_pdx_code); end;");
            getAccpdx.registerOutParameter("accpdxs", OracleTypes.CURSOR);
            getAccpdx.setString("p_pdx_code", p_pdx_code);
            getAccpdx.execute();
            ResultSet resultset = (ResultSet) getAccpdx.getObject("accpdxs");
            if (resultset.next()) {
                ICD10PreMDCResult i10 = new ICD10PreMDCResult();
                i10.setAccPDX(resultset.getString("ACCPDX"));
                i10.setAgeDMin(resultset.getString("AGEDMIN"));
                i10.setAgeDUse(resultset.getString("AGEDUSE"));
                i10.setAgeMax(resultset.getString("AGEMAX"));
                i10.setAgeMin(resultset.getString("AGEMIN"));
                i10.setCC(resultset.getString("CC"));
                i10.setCCRow(resultset.getString("CCROW"));
                i10.setCode(resultset.getString("CODE"));
                i10.setHIV_AX(resultset.getString("HIV_AX"));
                i10.setMDC(resultset.getString("MDC"));
                i10.setMainCC(resultset.getString("MAINCC"));
                i10.setPDC(resultset.getString("PDC"));
                i10.setSex(resultset.getString("SEX"));
                i10.setTrauma(resultset.getString("TRAUMA"));
                result.setResult(utility.objectMapper().writeValueAsString(i10));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing GetValidICD10Accpdx Method");
            logger.error("Error in GetValidICD10Accpdx Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
