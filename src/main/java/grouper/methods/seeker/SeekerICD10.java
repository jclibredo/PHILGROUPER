/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.seeker;

import grouper.structures.DRGWSResult;
import grouper.structures.PreMDC;
import grouper.utility.GrouperMethod;
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
public class SeekerICD10 {

    public SeekerICD10() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult SeekerICD10(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        ArrayList<PreMDC> icd10List = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGFUNCTION.SeekerICD10(); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_results");
            while (resultset.next()) {
                PreMDC icd10 = new PreMDC();
                icd10.setCode(resultset.getString("CODE"));
                icd10.setDesc(resultset.getString("DESCRIPTION"));
                icd10List.add(icd10);
            }
            if (icd10List.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(icd10List));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(SeekerICD10.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult INSERT_ICD10(final DataSource datasource,
            final String validcode,
            final String description,
            final String code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement auditrail = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.INSERT_ICD10(:Message,:Code,"
                    + ":u_val,:u_desc,:u_code)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            auditrail.setString("u_val", validcode);
            auditrail.setString("u_desc", description);
            auditrail.setString("u_code", code);
            auditrail.execute();
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(auditrail.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(SeekerICD10.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
