/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.structures.ICD10PreMDCResult;
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
public class GetICD10PreMDC {

    public GetICD10PreMDC() {
    }

    private final Utility utility = new Utility();

//    public DRGWSResult GetICD10PreMDC(final DataSource datasource, final String pdx) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setSuccess(false);
//        result.setMessage("");
//        result.setResult("");
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement statement = connection.prepareCall("begin :accpdxs := MINOSUN.DRGPKGFUNCTION.GET_ICD10PREMDC(:pdx); end;");
//            statement.registerOutParameter("accpdxs", OracleTypes.CURSOR);
//            statement.setString("pdx", pdx.toUpperCase().trim());
//            statement.execute();
//            ResultSet resultset = (ResultSet) statement.getObject("accpdxs");
//            if (resultset.next()) {
//                ICD10PreMDCResult premdc = new ICD10PreMDCResult();
//                premdc.setAccPDX(resultset.getString("ACCPDX"));
//                premdc.setAgeDMin(resultset.getString("AGEDMIN"));
//                premdc.setAgeDUse(resultset.getString("AGEDUSE"));
//                premdc.setAgeMax(resultset.getString("AGEMAX"));
//                premdc.setAgeMin(resultset.getString("AGEMIN"));
//                premdc.setCC(resultset.getString("CC"));
//                premdc.setCCRow(resultset.getString("CCROW"));
//                premdc.setCode(resultset.getString("CODE"));
//                premdc.setHIV_AX(resultset.getString("HIV_AX"));
//                premdc.setMDC(resultset.getString("MDC"));
//                premdc.setMainCC(resultset.getString("MAINCC"));
//                premdc.setPDC(resultset.getString("PDC"));
//                premdc.setSex(resultset.getString("SEX"));
//                premdc.setTrauma(resultset.getString("TRAUMA"));
//                result.setResult(utility.objectMapper().writeValueAsString(premdc));
//                result.setMessage(resultset.getString("CCROW"));
//                result.setSuccess(true);
//            } else {
//                result.setMessage("N/A");
//            }
//        } catch (SQLException | IOException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(GetICD10PreMDC.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    public DRGWSResult GetICD10PreMDC(final DataSource datasource, final String pdx) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        if (pdx.trim().isEmpty()) {
        } else {
            if (this.ProcessGetICD10PreMDC(datasource, pdx).isSuccess()) {
                result = this.ProcessGetICD10PreMDC(datasource, pdx);
            } else {
                if (this.ProcessGetICD10PreMDC(datasource, pdx.substring(0, pdx.length() - 1)).isSuccess()) {
                    result = this.ProcessGetICD10PreMDC(datasource, pdx.substring(0, pdx.length() - 1));
                }
            }
        }
        return result;
    }

    private DRGWSResult ProcessGetICD10PreMDC(
            final DataSource datasource,
            final String pdx) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :accpdxs := MINOSUN.DRGPKGFUNCTION.GET_ICD10PREMDC(:pdx); end;");
            statement.registerOutParameter("accpdxs", OracleTypes.CURSOR);
            statement.setString("pdx", utility.CleanCode(pdx).trim());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("accpdxs");
            if (resultset.next()) {
                ICD10PreMDCResult premdc = new ICD10PreMDCResult();
                premdc.setAccPDX(resultset.getString("ACCPDX"));
                premdc.setAgeDMin(resultset.getString("AGEDMIN"));
                premdc.setAgeDUse(resultset.getString("AGEDUSE"));
                premdc.setAgeMax(resultset.getString("AGEMAX"));
                premdc.setAgeMin(resultset.getString("AGEMIN"));
                premdc.setCC(resultset.getString("CC"));
                premdc.setCCRow(resultset.getString("CCROW"));
                premdc.setCode(resultset.getString("CODE"));
                premdc.setHIV_AX(resultset.getString("HIV_AX"));
                premdc.setMDC(resultset.getString("MDC"));
                premdc.setMainCC(resultset.getString("MAINCC"));
                premdc.setPDC(resultset.getString("PDC"));
                premdc.setSex(resultset.getString("SEX"));
                premdc.setTrauma(resultset.getString("TRAUMA"));
                result.setResult(utility.objectMapper().writeValueAsString(premdc));
                result.setMessage(resultset.getString("CCROW"));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetICD10PreMDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//      public DRGWSResult GetICD10(final DataSource datasource, final String p_pdx_code, final String Days, final String Years, final String p_patient_sex) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setResult("");
//        result.setMessage("");
//        result.setSuccess(false);
//        ICD10PreMDCResult icd10Result = new ICD10PreMDCResult();
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement GetPDx = connection.prepareCall("begin :pdx_validation := MINOSUN.DRGPKGFUNCTION.GET_ICD10_PREMDC(:p_pdx_code,:AgeDay,:AgeYear,:p_patient_sex); end;");
//            GetPDx.registerOutParameter("pdx_validation", OracleTypes.CURSOR);
//            GetPDx.setString("p_pdx_code", p_pdx_code.trim());
//            GetPDx.setString("AgeDay", Days);
//            GetPDx.setString("AgeYear", Years);
//            GetPDx.setString("p_patient_sex", p_patient_sex);
//            GetPDx.execute();
//            ResultSet resultset = (ResultSet) GetPDx.getObject("pdx_validation");
//            if (resultset != null) {
//                if (resultset.next()) {
//                    icd10Result.setCode(resultset.getString("CODE"));
//                    icd10Result.setMDC(resultset.getString("MDC"));
//                    icd10Result.setPDC(resultset.getString("PDC"));
//                    icd10Result.setCC(resultset.getString("CC"));
//                    icd10Result.setCCRow(resultset.getString("CCROW"));
//                    icd10Result.setHIV_AX(resultset.getString("HIV_AX"));
//                    icd10Result.setSex(resultset.getString("SEX"));
//                    icd10Result.setTrauma(resultset.getString("TRAUMA"));
//                    icd10Result.setAccPDX(resultset.getString("ACCPDX"));
//                    icd10Result.setMainCC(resultset.getString("MAINCC"));
//                    icd10Result.setAgeMax(resultset.getString("AGEMAX"));
//                    icd10Result.setAgeMin(resultset.getString("AGEMIN"));
//                    icd10Result.setAgeDMin(resultset.getString("AGEDMIN"));
//                    icd10Result.setAgeDUse(resultset.getString("AGEDUSE"));
//                    result.setResult(utility.objectMapper().writeValueAsString(icd10Result));
//                    result.setSuccess(true);
//                }
//            }
//        } catch (SQLException | IOException ex) {
//            result.setMessage("Something went wrong");
//            Logger.getLogger(GetICD10.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return result;
//    }
    public DRGWSResult GetICD10(final DataSource datasource, final String p_pdx_code, final String Days, final String Years, final String p_patient_sex) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        if (p_pdx_code.trim().isEmpty()) {
        } else {
            if (this.ProcessGetICD10(datasource, p_pdx_code, Days, Years, p_patient_sex).isSuccess()) {
                result = this.ProcessGetICD10(datasource, p_pdx_code, Days, Years, p_patient_sex);
            } else {
                if (this.ProcessGetICD10(datasource, p_pdx_code.substring(0, p_pdx_code.length() - 1), Days, Years, p_patient_sex).isSuccess()) {
                    result = this.ProcessGetICD10(datasource, p_pdx_code.substring(0, p_pdx_code.length() - 1), Days, Years, p_patient_sex);
                }
            }
        }
        return result;
    }

    private DRGWSResult ProcessGetICD10(
            final DataSource datasource,
            final String p_pdx_code,
            final String Days,
            final String Years,
            final String p_patient_sex) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :pdx_validation := MINOSUN.DRGPKGFUNCTION.GET_ICD10_PREMDC(:p_pdx_code,:AgeDay,:AgeYear,:p_patient_sex); end;");
            statement.registerOutParameter("pdx_validation", OracleTypes.CURSOR);
            statement.setString("p_pdx_code", utility.CleanCode(p_pdx_code).trim());
            statement.setString("AgeDay", Days);
            statement.setString("AgeYear", Years);
            statement.setString("p_patient_sex", p_patient_sex.toUpperCase());
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("pdx_validation");
            if (resultset.next()) {
                ICD10PreMDCResult icd10Result = new ICD10PreMDCResult();
                icd10Result.setCode(resultset.getString("CODE"));
                icd10Result.setMDC(resultset.getString("MDC"));
                icd10Result.setPDC(resultset.getString("PDC"));
                icd10Result.setCC(resultset.getString("CC"));
                icd10Result.setCCRow(resultset.getString("CCROW"));
                icd10Result.setHIV_AX(resultset.getString("HIV_AX"));
                icd10Result.setSex(resultset.getString("SEX"));
                icd10Result.setTrauma(resultset.getString("TRAUMA"));
                icd10Result.setAccPDX(resultset.getString("ACCPDX"));
                icd10Result.setMainCC(resultset.getString("MAINCC"));
                icd10Result.setAgeMax(resultset.getString("AGEMAX"));
                icd10Result.setAgeMin(resultset.getString("AGEMIN"));
                icd10Result.setAgeDMin(resultset.getString("AGEDMIN"));
                icd10Result.setAgeDUse(resultset.getString("AGEDUSE"));
                result.setResult(utility.objectMapper().writeValueAsString(icd10Result));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetICD10PreMDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
