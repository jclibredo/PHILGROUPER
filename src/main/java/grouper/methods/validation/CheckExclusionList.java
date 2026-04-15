/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
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
public class CheckExclusionList {

    public CheckExclusionList() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(CheckExclusionList.class);
    private final Utility utility = new Utility();

    public DRGWSResult CheckExclusionList(final DataSource datasource, final String sdx, final String pdx) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement exclusionlist = connection.prepareCall("begin :getexclusion := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_EXCLUSION(:secondary,:primarys); end;");
            exclusionlist.registerOutParameter("getexclusion", OracleTypes.CURSOR);
            exclusionlist.setString("secondary", utility.CleanCode(sdx));
            exclusionlist.setString("primarys", utility.CleanCode(pdx));
            exclusionlist.execute();
            ResultSet EndovascResult = (ResultSet) exclusionlist.getObject("getexclusion");
            if (EndovascResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing check exclusion list Method");
            logger.error("Error in check exclusion list Method : {}", ex.getMessage(), ex);
        }
        return result;
    }

}
