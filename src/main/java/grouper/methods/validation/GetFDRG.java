/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.structures.FDRG;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author LAPTOP
 */
@RequestScoped
public class GetFDRG {

    public GetFDRG() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetFDRG(
            final DataSource datasource,
            final String SchemaName,
            final String dcs,
            final String drgs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement Getdrg = connection.prepareCall("begin :v_result := " + SchemaName + ".DRGPKGFUNCTION.GET_FDRG(:dcs,:drgs); end;");
            Getdrg.registerOutParameter("v_result", OracleTypes.CURSOR);
            Getdrg.setString("dcs", dcs);
            Getdrg.setString("drgs", drgs);
            Getdrg.execute();
            ResultSet drgresultset = (ResultSet) Getdrg.getObject("v_result");
            if (drgresultset.next()) {
                FDRG fdrg = new FDRG();
                fdrg.setDc(drgresultset.getString("DC"));
                fdrg.setFpcclx(drgresultset.getString("FPCCLX"));
                fdrg.setPpccl(drgresultset.getString("PPCCL"));
                fdrg.setPdrg(drgresultset.getString("PDRG"));
                fdrg.setFpccl(drgresultset.getString("FPCCL"));
                fdrg.setFdrg(drgresultset.getString("FDRG"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(fdrg));
            }
        } catch (IOException | SQLException ex) {
            result.setMessage("Something went wrong " + ex.getMessage());
        }
        return result;
    }
}
