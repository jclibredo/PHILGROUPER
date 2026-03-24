/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
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
public class MDCProcedureMethod {

    public MDCProcedureMethod() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult MDCProcedure(final DataSource datasource, String icd9code, final String mdcs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetMDCProcedure = connection.prepareCall("begin :join_icd9_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ICD9_JOIN_TABLE(:icd9code,:mdcs); end;");
            GetMDCProcedure.registerOutParameter("join_icd9_output", OracleTypes.CURSOR);
            GetMDCProcedure.setString("icd9code", icd9code.trim());
            GetMDCProcedure.setString("mdcs", mdcs.trim());
            GetMDCProcedure.execute();
            ResultSet MDCProcResultset = (ResultSet) GetMDCProcedure.getObject("join_icd9_output");
            if (MDCProcResultset.next()) {
                grouper.structures.MDCProcedure mdcProcedure = new grouper.structures.MDCProcedure();
                mdcProcedure.setA_CODE(MDCProcResultset.getString("CODES"));
                mdcProcedure.setA_MDC(MDCProcResultset.getString(String.valueOf("MDC")));
                mdcProcedure.setA_PDC(MDCProcResultset.getString("PDC"));
                mdcProcedure.setB_CODE(MDCProcResultset.getString("CODE"));
                mdcProcedure.setB_ORP(MDCProcResultset.getString("ORP"));
                mdcProcedure.setB_SEX(MDCProcResultset.getString("SEX"));
                mdcProcedure.setB_ORPTYPE(MDCProcResultset.getString("ORPTYPE"));
                mdcProcedure.setB_PROCGR(MDCProcResultset.getString(String.valueOf("PROCGR")));//Convert to String
                mdcProcedure.setB_PCPART(MDCProcResultset.getString(String.valueOf("PCPART")));//Convert to String
                mdcProcedure.setB_EXTLEV(MDCProcResultset.getString(String.valueOf("EXTLEV")));//Convert to String
                mdcProcedure.setB_DRGUSE(MDCProcResultset.getString("DRGUSE"));
                mdcProcedure.setB_MAYUN(MDCProcResultset.getString("MAYUN"));
                mdcProcedure.setB_PROC_SITE(MDCProcResultset.getString("PROC_SITE"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(mdcProcedure));
                result.setMessage(MDCProcResultset.getString("PROC_SITE"));
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(MDCProcedureMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
