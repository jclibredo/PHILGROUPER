/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.drgrates;

import grouper.methods.library.ServiceDashboard;
import grouper.structures.DRGWSResult;
//import grouper.structures.DashboardView;
import grouper.structures.MaifipModel;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author LAPTOP
 */
@RequestScoped
public class Maifip {

    private final Logger logger = (Logger) LogManager.getLogger(ServiceDashboard.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMaifipMult(
            final DataSource datasource,
            final String SchemaName,
            final String pdx,
            final String pccl,
            final String hcftype) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := " + SchemaName + ".UHCDRGPKG.GET_MAIFIP_MUL(:u_pccl,:u_hcftype); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("u_pccl", pccl);
            statement.setString("u_hcftype", hcftype);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                MaifipModel maifip = new MaifipModel();
                maifip.setCategory(resultset.getString("CATEGORY"));
                maifip.setHcftype(resultset.getString("HCFTYPE"));
                maifip.setMultiplier(resultset.getString("MULTIPLIER"));
                maifip.setPrepccl(resultset.getString("PREPCCL"));
                maifip.setType(resultset.getString("TYPE"));
                //GET THE CODE AMOUNT
                maifip.setAmount(this.getAcrRates(pdx));
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(maifip));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong GetMaifipMult");
            logger.info("Executing Maifip Class");
            logger.error("Error in GetMaifipMult Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    public DRGWSResult GetHcfType(
            final DataSource datasource,
            final String SchemaName,
            final String hcfcode) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := " + SchemaName + ".UHCDRGPKG.GET_MAIFIP_MUL(:u_hcfcode); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.setString("u_hcfcode", hcfcode);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            if (resultset.next()) {
                result.setMessage(resultset.getString("TYPE"));
                result.setResult(resultset.getString("HCFLEVEL"));
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong GetMaifipMult");
            logger.info("Executing GetHcfType Class");
            logger.error("Error in GetMaifipMult Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

    // Define the map once as a static constant
    private static final Map<String, String> ACR_RATES_MAP;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("A000", "20000");
        map.put("A100", "40000");
        map.put("A200", "50000");
        ACR_RATES_MAP = Collections.unmodifiableMap(map);
    }

    private String getAcrRates(final String pdx) {
        // Returns the amount if found, or "0" if pdx is null or not found
        return ACR_RATES_MAP.getOrDefault(pdx, "0");
    }

};
