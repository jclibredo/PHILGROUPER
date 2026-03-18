/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.seeker;

import grouper.structures.DRGWSResult;
import grouper.structures.ICD9PreMDCResult;
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
public class SeekerICD9cm {

    public SeekerICD9cm() {
    }
    private final Utility utility = new Utility();
    
      public DRGWSResult SeekerICD9cm(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := MINOSUN.DRGPKGFUNCTION.SeekerICD9cm(); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.execute();
            ArrayList<ICD9PreMDCResult> icd9List = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_results");
            while (resultset.next()) {
                ICD9PreMDCResult icd9 = new ICD9PreMDCResult();
                icd9.setCode(resultset.getString("CODE"));
                icd9.setDescription(resultset.getString("DESCS"));
                icd9List.add(icd9);
            }
            if (icd9List.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(icd9List));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(SeekerICD9cm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
