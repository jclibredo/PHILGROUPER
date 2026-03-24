/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
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
public class GetPCCL {

    public GetPCCL() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetPCCL(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter, final String sdxfinalList) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement ps = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.GET_PCCL(:p_pccl,:p_pdx,:p_sdx,:p_dc)");
            ps.registerOutParameter("p_pccl", OracleTypes.NUMBER);
            ps.setString("p_pdx", grouperparameter.getPdx());
            ps.setString("p_sdx", sdxfinalList);
            ps.setString("p_dc", drgResult.getDC());
            ps.execute();
            drgResult.setDRG(drgResult.getDC() + "" + ps.getString("p_pccl"));
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetPCCL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
