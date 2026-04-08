/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.library;

import grouper.structures.DRGWSResult;
import grouper.structures.ICD10PreMDCResult;
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
 * @author LAPTOP
 */
@RequestScoped
public class ServicesI10 {

    public ServicesI10() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetIcd10PreMDC(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        ArrayList<ICD10PreMDCResult> icd10List = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.DRGPKGLIBRARY.GET_ICD10_PREMDC(); end;");
            statement.registerOutParameter("v_result", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_result");
            while (resultset.next()) {
                ICD10PreMDCResult i10 = new ICD10PreMDCResult();
                i10.setAccPDX(resultset.getString("ACCPDX"));
                i10.setAgeDMin(resultset.getString("AGEDMIN"));
                i10.setAgeDUse(resultset.getString("AGEDUSE"));
                i10.setAgeMax(resultset.getString("AGEMAX"));
                i10.setAgeMin(resultset.getString("AGEMIN"));
                i10.setCC(resultset.getString("CC"));
                i10.setCCRow(resultset.getString("CCROW"));
                i10.setCode(resultset.getString("CODE"));
                i10.setHIV_AX(resultset.getString("HIV_AX"));
                i10.setMDC(resultset.getString("MDC"));
                i10.setMainCC(resultset.getString("MAINCC"));
                i10.setPDC(resultset.getString("PDC"));
                i10.setSex(resultset.getString("SEX"));
                i10.setTrauma(resultset.getString("TRAUMA"));
                icd10List.add(i10);
            }
            if (icd10List.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(icd10List));
                result.setSuccess(true);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ServicesI10.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult CreateIcd10PreMdc(final DataSource datasource,
            final String code,
            final String mdc,
            final String pdc,
            final String cc,
            final String maincc,
            final String ccrow,
            final String hiv_ax,
            final String trauma,
            final String sex,
            final String accpdx,
            final String ageduse,
            final String agemin,
            final String agemax,
            final String agedmin) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.INSERT_ICD10_PREMDC(:Message,:Code,"
                    + ":u_code,:u_mdc,:u_pdc,:u_cc,:u_maincc,:u_ccrow,:u_hiv_ax,:u_trauma,:u_sex,:u_accpdx,:u_ageduse,:u_agemin,:u_agemax,:u_agedmin)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.setString("u_code", code);
            statement.setString("u_mdc", mdc);
            statement.setString("u_pdc", pdc);
            statement.setString("u_cc", cc);
            statement.setString("u_maincc", maincc);
            statement.setString("u_ccrow", ccrow);
            statement.setString("u_hiv_ax", hiv_ax);
            statement.setString("u_trauma", trauma);
            statement.setString("u_sex", sex);
            statement.setString("u_accpdx", accpdx);
            statement.setString("u_ageduse", ageduse);
            statement.setString("u_agemin", agemin);
            statement.setString("u_agemax", agemax);
            statement.setString("u_agedmin", agedmin);
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ServicesI10.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult DeleteIcd10PreMdc(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGLIBRARY.DELETE_ALL_ICD10_PREMDC(:Message,:Code)");
            statement.registerOutParameter("Message", OracleTypes.VARCHAR);
            statement.registerOutParameter("Code", OracleTypes.INTEGER);
            statement.execute();
            if (statement.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setMessage(statement.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(ServicesI10.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
