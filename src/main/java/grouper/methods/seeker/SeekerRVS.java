/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.seeker;

import grouper.structures.DRGWSResult;
import grouper.structures.RVS;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class SeekerRVS {

    public SeekerRVS() {
    }

    private final Logger logger = (Logger) LogManager.getLogger(SeekerRVS.class);
    private final Utility utility = new Utility();

    public DRGWSResult SeekerRVS(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGFUNCTION.SeekerRVS(); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.execute();
            ArrayList<RVS> rvsList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_results");
            while (resultset.next()) {
                RVS rvs = new RVS();
                rvs.setRvscode(resultset.getString("RVSCODE"));
                rvs.setDescription(resultset.getString("DESCRIPTION"));
                rvsList.add(rvs);
            }
            if (rvsList.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(rvsList));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing Seeker RVS Method");
            logger.error("Error in Seeker RVS Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
