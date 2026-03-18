/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.structures.WarningError;
import grouper.utility.Utility;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class GetWarningError {

    public GetWarningError() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetWarningError(final DataSource datasource, final String claimsid) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement geterror = connection.prepareCall("begin :warningerror := MINOSUN.DRGPKGFUNCTION.GET_WARNING_ERROR(:claimsid); end;");
            geterror.registerOutParameter("warningerror", OracleTypes.CURSOR);
            geterror.setString("claimsid", claimsid);
            geterror.execute();
            ResultSet geterrorResult = (ResultSet) geterror.getObject("warningerror");
            ArrayList<WarningError> warninglist = new ArrayList<>();
            while (geterrorResult.next()) {
                WarningError warningerror = new WarningError();
                warningerror.setData(geterrorResult.getString("DATA"));
                warningerror.setDescription(geterrorResult.getString("DESCRIPTION"));
                warningerror.setErrorcode(geterrorResult.getString("ERROR_CODE"));
                warningerror.setLhio(geterrorResult.getString("LHIO"));
                warningerror.setResultid(geterrorResult.getString("LHIO"));
                warningerror.setSeries(geterrorResult.getString("RESULT_ID"));
                warninglist.add(warningerror);
            }
            if (warninglist.size() > 0) {
                result.setResult(utility.objectMapper().writeValueAsString(warninglist));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetWarningError.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
