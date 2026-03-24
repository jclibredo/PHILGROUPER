/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.methods.premdc.ProcessGrouperParameter;
import grouper.structures.DRGOutput;
import grouper.structures.DRGRESULT;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
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
public class GetGrouper {

    public GetGrouper() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetGrouper(final DataSource datasource, final String tagss) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> errorList = new ArrayList<>();
        ArrayList<GrouperParameter> grouperparameterlsit = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement state = connection.prepareCall("begin :drgresult := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_DRG_RESULT(:tagss); end;");
            state.registerOutParameter("drgresult", OracleTypes.CURSOR);
            state.setString("tagss", tagss.trim());
            state.execute();
            int stopper = 0;
            ResultSet resultset = (ResultSet) state.getObject("drgresult");
            while (resultset.next()) {
                GrouperParameter ggrouperparameter = new GrouperParameter();
                DRGRESULT drgresultparam = new DRGRESULT();
                //GET GROUPER RESULT
                drgresultparam.setResult_id(resultset.getString("RESULT_ID"));
                ggrouperparameter.setResult_id(resultset.getString("RESULT_ID"));
                drgresultparam.setSeriesnum(resultset.getString("CLAIMS_SERIES"));
                if (resultset.getString("PDX") == null || resultset.getString("PDX").isEmpty() || resultset.getString("PDX").equals("")) {
                    drgresultparam.setPdx("");
                    ggrouperparameter.setPdx("");
                } else {
                    if (new GetICD10PreMDC().GetICD10PreMDC(datasource, resultset.getString("PDX").replaceAll("\\.", "").toUpperCase()).isSuccess()) {
                        drgresultparam.setPdx(resultset.getString("PDX").replaceAll("\\.", "").toUpperCase());
                        ggrouperparameter.setPdx(resultset.getString("PDX").replaceAll("\\.", "").toUpperCase());
                    } else if (new GetICD10PreMDC().GetICD10PreMDC(datasource, (resultset.getString("PDX").substring(0, resultset.getString("PDX").length() - 1)).replaceAll("\\.", "").toUpperCase()).isSuccess()) {
                        drgresultparam.setPdx((resultset.getString("PDX").substring(0, resultset.getString("PDX").length() - 1)).replaceAll("\\.", "").toUpperCase());
                        ggrouperparameter.setPdx((resultset.getString("PDX").substring(0, resultset.getString("PDX").length() - 1)).replaceAll("\\.", "").toUpperCase());
                    } else {
                        drgresultparam.setPdx("");
                        ggrouperparameter.setPdx("");
                    }
                }
                drgresultparam.setLhio(resultset.getString("LHIO"));
                ggrouperparameter.setIdseries(resultset.getString("CLAIM_ID"));
                if (resultset.getString("PROC") == null || resultset.getString("PROC").isEmpty() || resultset.getString("PROC").equals("")) {
                    drgresultparam.setProc("");
                    ggrouperparameter.setProc("");
                } else {
                    drgresultparam.setProc(resultset.getString("PROC"));
                    ggrouperparameter.setProc(resultset.getString("PROC"));
                }
                if (resultset.getString("SDX") == null || resultset.getString("SDX").isEmpty() || resultset.getString("SDX").equals("")) {
                    drgresultparam.setSdx("");
                    ggrouperparameter.setSdx("");
                } else {
                    drgresultparam.setSdx(resultset.getString("SDX"));
                    ggrouperparameter.setSdx(resultset.getString("SDX"));
                }
                drgresultparam.setTags(resultset.getString("TAGS"));
                //DRG XML GET PATIENT INFO
                CallableStatement getdrg_info = connection.prepareCall("begin :getdrginfo := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_DRG_INFO(:seriesnums); end;");
                getdrg_info.registerOutParameter("getdrginfo", OracleTypes.CURSOR);
                getdrg_info.setString("seriesnums", resultset.getString("CLAIMS_SERIES").trim());
                getdrg_info.execute();
                ResultSet infoResult = (ResultSet) getdrg_info.getObject("getdrginfo");
                if (infoResult.next()) {
                    ggrouperparameter.setAdmissionWeight(infoResult.getString("NB_ADMWEIGHT") == null
                            || infoResult.getString("NB_ADMWEIGHT").isEmpty()
                            || infoResult.getString("NB_ADMWEIGHT").equals("") ? "" : infoResult.getString("NB_ADMWEIGHT"));
                } else {
                    ggrouperparameter.setAdmissionWeight("");
                }
                CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.UHCDRGPKG.GETPATIENTDATA(:seriesnums); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("seriesnums", resultset.getString("CLAIMS_SERIES"));
                statement.execute();
                ResultSet resultSet = (ResultSet) statement.getObject("v_result");
                if (resultSet.next()) {
                    //EXPIREDDATE
                    ggrouperparameter.setExpiredDate(resultSet.getString("EXPIREDDATE") == null
                            || resultSet.getString("EXPIREDDATE").equals("")
                            || resultSet.getString("EXPIREDDATE").isEmpty() ? "" : utility.SimpleDateFormat("MM-dd-yyyy").format(resultSet.getTimestamp("EXPIREDDATE")));
                    //EXPIREDTIME
                    ggrouperparameter.setExpireTime(resultSet.getString("EXPIREDTIME") == null
                            || resultSet.getString("EXPIREDTIME").equals("")
                            || resultSet.getString("EXPIREDTIME").isEmpty() ? "" : utility.SimpleDateFormat("HH:mm").format(resultSet.getTimestamp("EXPIREDTIME")));
                    //ADMISSIONDATE
                    ggrouperparameter.setAdmissionDate(resultSet.getString("ADMISSIONDATE") == null
                            || resultSet.getString("ADMISSIONDATE").equals("")
                            || resultSet.getString("ADMISSIONDATE").isEmpty() ? "" : utility.SimpleDateFormat("MM-dd-yyyy").format(resultSet.getTimestamp("ADMISSIONDATE")));
                    //TIMEADMISSION
                    ggrouperparameter.setTimeAdmission(resultSet.getString("TIMEADMISSION") == null
                            || resultSet.getString("TIMEADMISSION").equals("")
                            || resultSet.getString("TIMEADMISSION").isEmpty() ? "" : utility.SimpleDateFormat("HH:mm").format(resultSet.getTimestamp("TIMEADMISSION")));
                    //DISCHARGETIME
                    ggrouperparameter.setTimeDischarge(resultSet.getString("TIMEDISCHARGE") == null
                            || resultSet.getString("TIMEDISCHARGE").equals("")
                            || resultSet.getString("TIMEDISCHARGE").isEmpty() ? "" : utility.SimpleDateFormat("HH:mm").format(resultSet.getTimestamp("TIMEDISCHARGE")));
                    //DISCHARGEDATE
                    ggrouperparameter.setDischargeDate(resultSet.getString("DISCHARGEDATE") == null
                            || resultSet.getString("DISCHARGEDATE").equals("")
                            || resultSet.getString("DISCHARGEDATE").isEmpty() ? "" : utility.SimpleDateFormat("MM-dd-yyyy").format(resultSet.getTimestamp("DISCHARGEDATE")));
                    //DATEOFBIRTH
                    ggrouperparameter.setBirthDate(!new GETPATIENTBDAY().GETPATIENTBDAY(datasource, resultset.getString("CLAIMS_SERIES").trim()).isSuccess()
                            ? "" : new GETPATIENTBDAY().GETPATIENTBDAY(datasource, resultset.getString("CLAIMS_SERIES").trim()).getResult());
                    //GENDER
                    ggrouperparameter.setGender(resultSet.getString("GENDER") == null
                            || resultSet.getString("GENDER").equals("")
                            || resultSet.getString("GENDER").isEmpty() ? "" : resultSet.getString("GENDER"));
                    //SERIES
                    ggrouperparameter.setClaimseries(resultset.getString("CLAIMS_SERIES"));
                    switch (resultSet.getString("DISCHARGETYPE")) {
                        case "E": {
                            ggrouperparameter.setDischargeType("8");
                            break;
                        }
                        case "O": {
                            ggrouperparameter.setDischargeType("5");
                            break;
                        }
                        case "I":
                        case "R": {
                            ggrouperparameter.setDischargeType("1");
                            break;
                        }
                        case "A": {
                            ggrouperparameter.setDischargeType("3");
                            break;
                        }
                        case "T": {
                            ggrouperparameter.setDischargeType("4");
                            break;
                        }
                        case "H": {
                            ggrouperparameter.setDischargeType("2");
                            break;
                        }
                        default: {
                            ggrouperparameter.setDischargeType("");
                            break;
                        }
                    }

                    grouperparameterlsit.add(ggrouperparameter);
                } else {
                    errorList.add(resultset.getString("CLAIMS_SERIES") + " NOT FOUND");
                }
                stopper++;
                if (stopper == 500) {
                    break;
                }
            }
            ArrayList<DRGOutput> drgresultList = new ArrayList<>();
            for (int y = 0; y < grouperparameterlsit.size(); y++) {
                DRGWSResult processResult = new ProcessGrouperParameter().ProcessGrouperParameter(datasource, grouperparameterlsit.get(y));
                if (processResult.isSuccess()) {
                    DRGOutput drgout = utility.objectMapper().readValue(processResult.getResult(), DRGOutput.class);
                    drgresultList.add(drgout);
                }
            }
            if (drgresultList.size() > 0) {
                result.setSuccess(true);
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(drgresultList));
            } else {
                result.setMessage("NO DATA FOUND here");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetGrouper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
}
