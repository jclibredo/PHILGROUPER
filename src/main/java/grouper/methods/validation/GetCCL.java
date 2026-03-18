/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.CCL;
import grouper.structures.DRGWSResult;
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
public class GetCCL {

    public GetCCL() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetCCL(final DataSource datasource, final String ccrows) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetCCL = connection.prepareCall("begin :cclm_output := MINOSUN.DRGPKGFUNCTION.GET_CCL(:ccrows); end;");
            GetCCL.registerOutParameter("cclm_output", OracleTypes.CURSOR);
            GetCCL.setString("ccrows", ccrows);
            GetCCL.execute();
            ResultSet CCLResultset = (ResultSet) GetCCL.getObject("cclm_output");
            if (CCLResultset.next()) {
                CCL cclresults = new CCL();
                result.setSuccess(true);
                cclresults.setCcrow(CCLResultset.getString("CCROW"));
                cclresults.setCcl(CCLResultset.getString("CCL"));
                result.setResult(utility.objectMapper().writeValueAsString(cclresults));
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetCCL.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
