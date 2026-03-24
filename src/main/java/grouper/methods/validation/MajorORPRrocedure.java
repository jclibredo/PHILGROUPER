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
public class MajorORPRrocedure {

    public MajorORPRrocedure() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult MajorORPRrocedure(final DataSource datasource, final String icd9codes, final String mdcs, final String pdcs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetMajorORProc = connection.prepareCall("begin :major_or_proc := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_MAINCC_USED_ICD10(:icd9codes,:mdcs,:pdcs); end;");
            GetMajorORProc.registerOutParameter("major_or_proc", OracleTypes.CURSOR);
            GetMajorORProc.setString("icd9codes", icd9codes);
            GetMajorORProc.setString("mdcs", mdcs);
            GetMajorORProc.setString("pdcs", pdcs);
            GetMajorORProc.execute();
            ResultSet GetMajorORProcResultset = (ResultSet) GetMajorORProc.getObject("major_or_proc");
            if (GetMajorORProcResultset.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(MajorORPRrocedure.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
