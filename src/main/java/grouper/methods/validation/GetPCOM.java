/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.structures.PCOM;
import grouper.utility.GrouperMethod;
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
public class GetPCOM {

    public GetPCOM() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetPCOM(final DataSource datasource, final String code1, final String code2) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetPCOM = connection.prepareCall("begin :pcom := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PCOM(:codea,:codeb); end;");
            GetPCOM.registerOutParameter("pcom", OracleTypes.CURSOR);
            GetPCOM.setString("codea", code1);
            GetPCOM.setString("codeb", code2);
            GetPCOM.execute();
            ResultSet PCOMResultset = (ResultSet) GetPCOM.getObject("pcom");
            if (PCOMResultset.next()) {
                PCOM pcom = new PCOM();
                pcom.setCode(PCOMResultset.getString("CODE"));
                pcom.setCode1(PCOMResultset.getString("CODE1"));
                pcom.setCode2(PCOMResultset.getString("CODE2"));
                pcom.setDesc1(PCOMResultset.getString("DESC1"));
                pcom.setDesc2(PCOMResultset.getString("DESC2"));
                pcom.setDescription(PCOMResultset.getString("DESCRIPTION"));
                pcom.setPdc(PCOMResultset.getString("PDC"));
                // result.setResult(utility.objectMapper().writeValueAsString(pcom));
                result.setResult(PCOMResultset.getString("CODE"));
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
