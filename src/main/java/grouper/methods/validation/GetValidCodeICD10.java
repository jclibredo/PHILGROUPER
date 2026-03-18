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
public class GetValidCodeICD10 {
    
    public GetValidCodeICD10() {
    }
    private final Utility utility = new Utility();

//    public DRGWSResult GetValidCodeICD10(final DataSource datasource, final String p_icd10_code) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setResult("");
//        result.setSuccess(false);
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement getValidCode = connection.prepareCall("begin :p_validcode := MINOSUN.DRGPKGFUNCTION.get_valid_icd10(:p_icd10_code); end;");
//            getValidCode.registerOutParameter("p_validcode", OracleTypes.CURSOR);
//            getValidCode.setString("p_icd10_code", p_icd10_code.trim());
//            getValidCode.execute();
//            ResultSet getValidCodeResult = (ResultSet) getValidCode.getObject("p_validcode");
//            if (getValidCodeResult.next()) {
//                result.setSuccess(true);
//            }
//        } catch (SQLException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(GetValidCodeICD10.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    
     public DRGWSResult GetValidCodeICD10(
            final DataSource datasource,
            final String p_icd10_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("No ICD10 Record Found");
        result.setResult("");
        if (!p_icd10_code.trim().isEmpty()) {
            if (this.ProcessGetICD10(datasource, p_icd10_code).isSuccess()) {
                result = this.ProcessGetICD10(datasource, p_icd10_code);
            } else {
                if (this.ProcessGetICD10(datasource, p_icd10_code.substring(0, p_icd10_code.length() - 1)).isSuccess()) {
                    result = this.ProcessGetICD10(datasource, p_icd10_code.substring(0, p_icd10_code.length() - 1));
                }
            }
        }
        return result;
    }
    
    
     private DRGWSResult ProcessGetICD10(
            final DataSource datasource,
            final String p_icd10_code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("No ICD10 Record Found");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :p_validcode := MINOSUN.DRGPKGFUNCTION.get_valid_icd10(:p_icd10_code); end;");
            statement.registerOutParameter("p_validcode", OracleTypes.CURSOR);
            statement.setString("p_icd10_code", utility.CleanCode(p_icd10_code));
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("p_validcode");
            if (resultset.next()) {
                result.setSuccess(true);
                result.setResult(resultset.getString("validcode"));
                result.setMessage("Record Found");
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetICD10.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }


}
