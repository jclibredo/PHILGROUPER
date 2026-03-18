/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.utility.GrouperMethod;
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
public class PDCUseProcedureChecking {

    public PDCUseProcedureChecking() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult PDCUseProcedureChecking(final DataSource datasource, String icd9code, final String pdcs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetPDCProcedure = connection.prepareCall("begin :join_icd9_output := MINOSUN.DRGPKGFUNCTION.GET_PROC_PDC(:icd9code,:pdcs); end;");
            GetPDCProcedure.registerOutParameter("join_icd9_output", OracleTypes.CURSOR);
            GetPDCProcedure.setString("icd9code", icd9code);
            GetPDCProcedure.setString("pdcs", pdcs);
            GetPDCProcedure.execute();
            ResultSet PDCProcResultset = (ResultSet) GetPDCProcedure.getObject("join_icd9_output");
            if (PDCProcResultset.next()) {
                grouper.structures.MDCProcedure mdcProcedure = new grouper.structures.MDCProcedure();
                mdcProcedure.setA_CODE(PDCProcResultset.getString("CODES"));
                mdcProcedure.setA_MDC(PDCProcResultset.getString("MDC"));
                mdcProcedure.setA_PDC(PDCProcResultset.getString("PDC"));
                mdcProcedure.setB_CODE(PDCProcResultset.getString("CODE"));
                mdcProcedure.setB_ORP(PDCProcResultset.getString("ORP"));
                mdcProcedure.setB_SEX(PDCProcResultset.getString("SEX"));
                mdcProcedure.setB_ORPTYPE(PDCProcResultset.getString("ORPTYPE"));
                mdcProcedure.setB_PROCGR(PDCProcResultset.getString("PROCGR"));
                mdcProcedure.setB_PCPART(PDCProcResultset.getString("PCPART"));
                mdcProcedure.setB_EXTLEV(PDCProcResultset.getString("EXTLEV"));
                mdcProcedure.setB_DRGUSE(PDCProcResultset.getString("DRGUSE"));
                mdcProcedure.setB_MAYUN(PDCProcResultset.getString("MAYUN"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(mdcProcedure));
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(PDCUseProcedureChecking.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
