/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.utility;

import grouper.methods.premdc.ProcessGrouperParameter;
import grouper.structures.BMDCPreMDCResult;
import grouper.structures.CCL;
import grouper.structures.CombinationCode;
import grouper.structures.DC;
import grouper.structures.DRGOutput;
import grouper.structures.DRGRESULT;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.ICD10PreMDCResult;
import grouper.structures.ICD9PreMDCResult;
import grouper.structures.MDC;
import grouper.structures.MDCProcedure;
import grouper.structures.PCOM;
import grouper.structures.PDC;
import grouper.structures.PreMDC;
import grouper.structures.RVS;
import grouper.structures.WarningError;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class GrouperMethod {

    public GrouperMethod() {
    }

    private final Utility utility = new Utility();

    //Removed duplication for Proc Code with extension code
    public static <T> ArrayList<T> RemovedDuplicates(ArrayList<T> list) {
        ArrayList<T> newList = new ArrayList<>();
        for (T element : list) {
            if (!newList.contains(element)) {
                newList.add(element);
            }
        }
        return newList;
    }

    //GET ICD10 FOR KEY VALUE PAIR VALIDATION
    public DRGWSResult GetICD10(final DataSource datasource, final String p_icd10_code) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            CallableStatement statement = connection.prepareCall("begin :p_validcode := DRG_SHADOWBILLING.DRGPKGFUNCTION.get_valid_icd10(:p_icd10_code); end;");
            statement.registerOutParameter("p_validcode", OracleTypes.CURSOR);
            statement.setString("p_icd10_code", p_icd10_code);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("p_validcode");
            if (resultset.next()) {
                result.setSuccess(true);
                result.setResult(resultset.getString("validcode"));
                result.setMessage("Record Found");
            } else {
                result.setMessage("No ICD10 Record Found");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET ACCPDX VALUE FROM ICD10_PREMDC TABLE
    public DRGWSResult GetClaimDuplication(final DataSource datasource, final String accre, final String claimnum) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getduplication = connection.prepareCall("begin :dupnclaims := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CHECK_DUPLICATE(:claimnum,:accre); end;");
            getduplication.registerOutParameter("dupnclaims", OracleTypes.CURSOR);
            getduplication.setString("claimnum", claimnum);
            getduplication.setString("accre", accre);
            getduplication.execute();
            ResultSet getduplicationResult = (ResultSet) getduplication.getObject("dupnclaims");
            if (getduplicationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //PDX used to find MDC
    public DRGWSResult PDXandMDC(final DataSource datasource, final String pdx, final String mdc) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            CallableStatement statement = connection.prepareCall("begin :pdxmdc := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PDX_MDC(:pdx,:mdc); end;");
            statement.registerOutParameter("pdxmdc", OracleTypes.CURSOR);
            statement.setString("pdx", pdx);
            statement.setString("mdc", mdc);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("pdxmdc");
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
                result.setSuccess(true);
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(premdc));
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET ICD10 FOR PRE MDC VALIDATION PROCESS
    public DRGWSResult GetICD10PreMDC(final DataSource datasource, final String pdx) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            CallableStatement statement = connection.prepareCall("begin :accpdxs := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ICD10PREMDC(:pdx); end;");
            statement.registerOutParameter("accpdxs", OracleTypes.CURSOR);
            statement.setString("pdx", pdx.toUpperCase().trim());
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
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //GET ICD9CM 
    public DRGWSResult GetICD9cms(final DataSource datasource, final String rvs_code) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            result.setSuccess(false);
            result.setMessage("");
            result.setResult("");
            String ProcListNew = "";
            List<String> FinalNewProcList = new ArrayList<>();
            CallableStatement statement = connection.prepareCall("begin :converter := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;");
            statement.registerOutParameter("converter", OracleTypes.CURSOR);
            statement.setString("rvs_code", rvs_code);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("converter");
            if (resultset.next()) {
                ProcListNew = resultset.getString("ICD9CODE");
                List<String> ConverterResult = Arrays.asList(ProcListNew.split(","));
                for (int g = 0; g < ConverterResult.size(); g++) {
                    String ICD9Codes = ConverterResult.get(g);
                    FinalNewProcList.add(ICD9Codes);
                }
                result.setResult(ProcListNew);
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult GETPATIENTBDAY(
            final DataSource datasource,
            final String seriesnum) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :nclaims := DRG_SHADOWBILLING.UHCDRGPKG.GET_NCLAIMS(:seriesnum); end;");
            statement.registerOutParameter("nclaims", OracleTypes.CURSOR);
            statement.setString("seriesnum", seriesnum.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("nclaims");
            if (resultSet.next()) {
                if (resultSet.getString("DATEOFBIRTH") != null && !resultSet.getString("DATEOFBIRTH").isEmpty() && !resultSet.getString("DATEOFBIRTH").equals("")) {
                    result.setResult(resultSet.getString("DATEOFBIRTH"));
                    result.setSuccess(true);
                    result.setMessage("OK");
                }
            }
        } catch (Exception ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // Get Data from NCLaims and Process for grouper
    public DRGWSResult GetGrouper(final DataSource datasource, final String tagss) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        ArrayList<String> errorList = new ArrayList<>();
        ArrayList<GrouperParameter> grouperparameterlsit = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getDRGParam = connection.prepareCall("begin :drgresult := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_DRG_RESULT(:tagss); end;");
            getDRGParam.registerOutParameter("drgresult", OracleTypes.CURSOR);
            getDRGParam.setString("tagss", tagss.trim());
            getDRGParam.execute();
            int stopper = 0;
            ResultSet getDRGParamResult = (ResultSet) getDRGParam.getObject("drgresult");
            while (getDRGParamResult.next()) {
                GrouperParameter ggrouperparameter = new GrouperParameter();
                DRGRESULT drgresultparam = new DRGRESULT();
                //======================================================GET GROUPER RESULT
                drgresultparam.setResult_id(getDRGParamResult.getString("RESULT_ID"));
                ggrouperparameter.setResult_id(getDRGParamResult.getString("RESULT_ID"));
                drgresultparam.setSeriesnum(getDRGParamResult.getString("CLAIMS_SERIES"));
                if (getDRGParamResult.getString("PDX") == null || getDRGParamResult.getString("PDX").isEmpty() || getDRGParamResult.getString("PDX").equals("")) {
                    drgresultparam.setPdx("");
                    ggrouperparameter.setPdx("");
                } else {
                    drgresultparam.setPdx(getDRGParamResult.getString("PDX"));
                    ggrouperparameter.setPdx(getDRGParamResult.getString("PDX"));
                }
                drgresultparam.setLhio(getDRGParamResult.getString("LHIO"));
                ggrouperparameter.setIdseries(getDRGParamResult.getString("CLAIM_ID"));
                //=====================================================================
                if (getDRGParamResult.getString("PROC") == null || getDRGParamResult.getString("PROC").isEmpty() || getDRGParamResult.getString("PROC").equals("")) {
                    drgresultparam.setProc("");
                    ggrouperparameter.setProc("");
                } else {
                    drgresultparam.setProc(getDRGParamResult.getString("PROC"));
                    ggrouperparameter.setProc(getDRGParamResult.getString("PROC"));
                }
                //======================================================================
                if (getDRGParamResult.getString("SDX") == null || getDRGParamResult.getString("SDX").isEmpty() || getDRGParamResult.getString("SDX").equals("")) {
                    drgresultparam.setSdx("");
                    ggrouperparameter.setSdx("");
                } else {
                    drgresultparam.setSdx(getDRGParamResult.getString("SDX"));
                    ggrouperparameter.setSdx(getDRGParamResult.getString("SDX"));
                }
                //======================================================================
                drgresultparam.setTags(getDRGParamResult.getString("TAGS"));
                //========================================== DRG XML GET PATIENT INFO
                CallableStatement getdrg_info = connection.prepareCall("begin :getdrginfo := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_DRG_INFO(:seriesnums); end;");
                getdrg_info.registerOutParameter("getdrginfo", OracleTypes.CURSOR);
                getdrg_info.setString("seriesnums", getDRGParamResult.getString("CLAIMS_SERIES").trim());
                getdrg_info.execute();
                ResultSet infoResult = (ResultSet) getdrg_info.getObject("getdrginfo");
                if (infoResult.next()) {
                    ggrouperparameter.setAdmissionWeight(infoResult.getString("NB_ADMWEIGHT"));
                }
                //==============================================ECLAIMS XML GET NCLAIMS DATA============================================================
                CallableStatement statement = connection.prepareCall("begin :v_result := DRG_SHADOWBILLING.UHCDRGPKG.GETPATIENTDATA(:seriesnum); end;");
                statement.registerOutParameter("v_result", OracleTypes.CURSOR);
                statement.setString("seriesnum", getDRGParamResult.getString("CLAIMS_SERIES"));
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
                    if (this.GETPATIENTBDAY(datasource, getDRGParamResult.getString("CLAIMS_SERIES").trim()).isSuccess()) {
                        if (utility.isParsableDate(this.GETPATIENTBDAY(datasource, getDRGParamResult.getString("CLAIMS_SERIES").trim()).getResult(), "MM/dd/yyyy")) {
                            ggrouperparameter.setBirthDate(!this.GETPATIENTBDAY(datasource, getDRGParamResult.getString("CLAIMS_SERIES").trim()).isSuccess() ? ""
                                    : utility.SimpleDateFormat("MM-dd-yyyy").format(utility.SimpleDateFormat("MM/dd/yyyy").parse(this.GETPATIENTBDAY(datasource, getDRGParamResult.getString("CLAIMS_SERIES").trim()).getResult())));
                        } else {
                            ggrouperparameter.setBirthDate("");
                        }
                    } else {
                        ggrouperparameter.setBirthDate("");
                    }
                    //ggrouperparameter.setBirthDate(!this.GETPATIENTBDAY(datasource, getDRGParamResult.getString("CLAIMS_SERIES").trim()).isSuccess() ? ""
                    //       : utility.SimpleDateFormat("MM-dd-yyyy").format(utility.SimpleDateFormat("MM/dd/yyyy").parse(this.GETPATIENTBDAY(datasource, getDRGParamResult.getString("CLAIMS_SERIES").trim()).getResult())));
                    //GENDER
                    ggrouperparameter.setGender(resultSet.getString("GENDER") == null
                            || resultSet.getString("GENDER").equals("")
                            || resultSet.getString("GENDER").isEmpty() ? "" : resultSet.getString("GENDER"));
                    //SERIES
                    ggrouperparameter.setClaimseries(getDRGParamResult.getString("CLAIMS_SERIES"));
                    switch (resultSet.getString("DISCHARGETYPE")) {
                        case "E":
                            ggrouperparameter.setDischargeType("8");
                            break;
                        case "O":
                            ggrouperparameter.setDischargeType("5");
                            break;
                        case "I":
                        case "R":
                            ggrouperparameter.setDischargeType("1");
                            break;
                        case "A":
                            ggrouperparameter.setDischargeType("3");
                            break;
                        case "T":
                            ggrouperparameter.setDischargeType("4");
                            break;
                        case "H":
                            ggrouperparameter.setDischargeType("2");
                            break;
                        default:
                            ggrouperparameter.setDischargeType("");
                            break;
                    }

                    grouperparameterlsit.add(ggrouperparameter);
                } else {
                    errorList.add(getDRGParamResult.getString("CLAIMS_SERIES") + " NOT FOUND");
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
            if (!drgresultList.isEmpty()) {
                result.setSuccess(true);
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(drgresultList));
            } else {
                result.setMessage("NO DATA FOUND");
            }
        } catch (SQLException | IOException | ParseException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //Validation for Dagger Asterisk
    public DRGWSResult GetDA(final DataSource datasource, final String dagger, final String asterisk) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setSuccess(false);
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement conn = connection.prepareCall("begin :DaggerAs := DRG_SHADOWBILLING.DRGPKGFUNCTION.get_da(:dagger,:asterisk); end;");
            conn.registerOutParameter("DaggerAs", OracleTypes.CURSOR);
            conn.setString("dagger", dagger.toUpperCase().trim());
            conn.setString("asterisk", asterisk.toUpperCase().trim());
            conn.execute();
            ResultSet connResult = (ResultSet) conn.getObject("DaggerAs");
            if (connResult.next()) {
                result.setSuccess(true);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    //UPDATE DRG RESULT AND CHANGE TAGS VALUE
    public DRGWSResult UpdateDRGResult(final DataSource datasource,
            final String mdcs,
            final String pdcs,
            final String dcs,
            final String result_id,
            final String series,
            final String drg) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            String tagss = "DG";
            CallableStatement updatedrgresult = connection.prepareCall("call DRGPKGPROCEDURE.UPDATE_DRG_RESULT(:Message,:Code,:umdc,:updc,:udc,:uresultid,:useries,:utags,:udrg)");
            updatedrgresult.registerOutParameter("Message", OracleTypes.VARCHAR);
            updatedrgresult.registerOutParameter("Code", OracleTypes.INTEGER);
            updatedrgresult.setString("umdc", mdcs);
            updatedrgresult.setString("updc", pdcs);
            updatedrgresult.setString("udc", dcs);
            updatedrgresult.setString("uresultid", result_id);
            updatedrgresult.setString("useries", series);
            updatedrgresult.setString("utags", tagss.trim());
            updatedrgresult.setString("udrg", drg);
            updatedrgresult.executeUpdate();
            if (updatedrgresult.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            }
            result.setMessage(updatedrgresult.getString("Code"));
            result.setResult(updatedrgresult.getString("Message"));
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

//count rvs value
//    public DRGWSResult CountProc(final DataSource datasource, final String codes) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setMessage("");
//        result.setSuccess(false);
//        result.setResult("");
//        try (Connection connection = datasource.getConnection()) {
//            CallableStatement getResult = connection.prepareCall("begin :count_output := DRG_SHADOWBILLING..DRGPKGFUNCTION.GET_COUNT(:codes); end;");
//            getResult.registerOutParameter("count_output", OracleTypes.CURSOR);
//            getResult.setString("codes", codes.trim());
//            getResult.execute();
//            ResultSet rest = (ResultSet) getResult.getObject("count_output");
//            if (rest.next()) {
//                result.setSuccess(true);
//            } else {
//                result.setMessage("N/A");
//            }
//        } catch (SQLException ex) {
//            result.setMessage(ex.getLocalizedMessage());
//            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//
//    }
    //public MethodResult GET ACCPDX VALUE FROM ICD10_PREMDC TABLE
    public DRGWSResult GetWarningError(final DataSource datasource, final String claimsid) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement geterror = connection.prepareCall("begin :warningerror := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_WARNING_ERROR(:claimsid); end;");
            geterror.registerOutParameter("warningerror", OracleTypes.CURSOR);
            geterror.setString("claimsid", claimsid);
            geterror.execute();
            ResultSet geterrorResult = (ResultSet) geterror.getObject("warningerror");
            ArrayList<WarningError> warninglist = new ArrayList<>();
            while (geterrorResult.next()) {
                WarningError warningerror = new WarningError();
                warningerror.setData(geterrorResult.getString("DATA"));
                warningerror.setDescription(geterrorResult.getString("DESCRIPTION"));
                warningerror.setErrorcode(geterrorResult.getString("ERROR_CODE"));
                warningerror.setLhio(geterrorResult.getString("LHIO"));
                warningerror.setResultid(geterrorResult.getString("LHIO"));
                warningerror.setSeries(geterrorResult.getString("RESULT_ID"));
                warninglist.add(warningerror);
            }
            //=======================================================================
            result.setResult(utility.objectMapper().writeValueAsString(warninglist));
            result.setSuccess(true);
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET ACCPDX VALUE FROM ICD10_PREMDC TABLE
    public DRGWSResult GetValidICD10Accpdx(final DataSource datasource, final String p_pdx_code) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getAccpdx = connection.prepareCall("begin :accpdxs := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ACCPDX_VALUE(:p_pdx_code); end;");
            getAccpdx.registerOutParameter("accpdxs", OracleTypes.CURSOR);
            getAccpdx.setString("p_pdx_code", p_pdx_code);
            getAccpdx.execute();
            ResultSet accpdxResult = (ResultSet) getAccpdx.getObject("accpdxs");
            if (accpdxResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public Method for Insert DRG Auditrail
    public DRGWSResult InsertDRGAuditTrail(final DataSource datasource,
            final String type,
            final String request,
            final String details) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {

            SimpleDateFormat sdf = utility.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = new Date();
            CallableStatement auditrail = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.INSERT_AUDITRAIL(:Message,:Code,:datetime,:details,:type,:request)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            //=====================================================================End Process SDx duplication================================ 
            auditrail.setString("datetime", sdf.format(date));
            auditrail.setString("details", details);
            auditrail.setString("type", type);
            auditrail.setString("request", request);
            auditrail.executeUpdate();
            //======================================================================
            result.setMessage(auditrail.getString("Code"));
            result.setResult(auditrail.getString("Message"));
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public Method for Insert DRG Auditrail
    public DRGWSResult InsertGrouperAuditTrail(final DataSource datasource,
            final String p_series,
            final String p_claimnumber,
            final String p_details,
            final String p_status) {
        DRGWSResult result = utility.DRGWSResult();
        result.setResult("");
        result.setMessage("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            SimpleDateFormat sdf = utility.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date date = new Date();
            CallableStatement auditrail = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.INSERT_GROUPER_AUDITRAIL(:Message,:Code,"
                    + ":udatein,:useries,:uclaimnumber,:udesc,:ustats)");
            auditrail.registerOutParameter("Message", OracleTypes.VARCHAR);
            auditrail.registerOutParameter("Code", OracleTypes.INTEGER);
            //=====================================================================End Process SDx duplication================================ 
            auditrail.setString("udatein", sdf.format(date));
            auditrail.setString("useries", p_series);
            auditrail.setString("uclaimnumber", p_claimnumber);
            auditrail.setString("udesc", p_details);
            auditrail.setString("ustats", p_status);
            auditrail.execute();
            if (auditrail.getString("Message").equals("SUCC")) {
                result.setSuccess(true);
                result.setMessage(auditrail.getString("Message"));
            } else {
                result.setMessage(auditrail.getString("Message"));
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET PDC MAIN TABLE
//    public DRGWSResult InsertDRGResult(final DataSource datasource,
//            final String series,
//            final String lhio,
//            final String pdx,
//            final String sdx,
//            final String proc,
//            final String result_id,
//            final String claimid) {
//        DRGWSResult result = utility.DRGWSResult();
//        result.setResult("");
//        result.setMessage("");
//        result.setSuccess(false);
//        try (Connection connection = datasource.getConnection()) {
//            String tagss = "FG";
//            CallableStatement grouperdata = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.INSERT_DRG_RESULT(:Message,:Code,:uclaimid,:uresultid,:useries,:utags,:ulhio,:updxcode,:usdxcode,:uproc)");
//            grouperdata.registerOutParameter("Message", OracleTypes.VARCHAR);
//            grouperdata.registerOutParameter("Code", OracleTypes.INTEGER);
//            //=====================================================================Process SDx duplication================================
//            List<String> SDXList = Arrays.asList(sdx.split(","));
//            LinkedList<String> duplicate = new LinkedList<>();
//            LinkedList<String> newlist = new LinkedList<>();
//            for (int y = 0; y < SDXList.size(); y++) {
//                newlist.add(SDXList.get(y));
//            }
//            for (int i = 0; i < SDXList.size() - 1; i++) {
//                for (int j = i + 1; j < SDXList.size(); j++) {
//                    if (SDXList.get(i).equals(SDXList.get(j)) && (i != j)) {
//                        duplicate.add(SDXList.get(j));
//                        newlist.remove(SDXList.get(j));
//                    }
//                }
//            }
//            //==================================================================
//            //START HERE
//            List<String> ProcList = Arrays.asList(proc.split(","));
//            LinkedList<String> procduplicate = new LinkedList<>();
//            LinkedList<String> procnewlist = new LinkedList<>();
//
//            for (int y = 0; y < ProcList.size(); y++) {
//                procnewlist.add(ProcList.get(y));
//            }
//            for (int i = 0; i < ProcList.size() - 1; i++) {
//                for (int j = i + 1; j < ProcList.size(); j++) {
//                    if (ProcList.get(i).equals(ProcList.get(j)) && (i != j)) {
//                        procduplicate.add(ProcList.get(j));
//                        procnewlist.remove(ProcList.get(j));
//                    }
//                }
//            }
//
//            //=====================================================================End Process SDx duplication================================ 
//            grouperdata.setString("rest_id", result_id);
//            grouperdata.setString("series", series);
//            grouperdata.setString("tags", tagss.trim());
//            grouperdata.setString("lhio", lhio);
//            grouperdata.setString("pdx", pdx);
//            if (duplicate.isEmpty()) {
//                grouperdata.setString("sdx", sdx);
//            } else {
//                grouperdata.setString("sdx", String.join(",", newlist));
//            }
//            grouperdata.setString("proc", proc);
//            grouperdata.executeUpdate();
//            result.setMessage(grouperdata.getString("Code"));
//            result.setResult(grouperdata.getString("Message"));
//            if (grouperdata.getString("Message").equals("SUCC")) {
//                result.setSuccess(true);
//            } else {
//                result.setSuccess(false);
//            }
//
//        } catch (SQLException ex) {
//            result.setMessage(ex.toString());
//            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return result;
//    }
    //public MethodResult POST(final String token, final String stringurl, final String stringrequest) {
    public DRGWSResult GetICD9cm(final DataSource datasource, final String procS) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement_icd9_findDC = connection.prepareCall("begin : get_icd9_DC := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ICD9CM_FINDDC(:procS); end;");
            statement_icd9_findDC.registerOutParameter("get_icd9_DC", OracleTypes.CURSOR);
            statement_icd9_findDC.setString("procS", procS);
            statement_icd9_findDC.execute();
            ResultSet resultset_icd9_findDC = (ResultSet) statement_icd9_findDC.getObject("get_icd9_DC");
            if (resultset_icd9_findDC.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

// GET ICD10 VALIDATION WITH PARAMETER
    public DRGWSResult GetICD10(final DataSource datasource, final String p_pdx_code, final String Days, final String Years, final String p_patient_sex) {
        DRGWSResult result = utility.DRGWSResult();
        ICD10PreMDCResult icd10Result = new ICD10PreMDCResult();
        try (Connection connection = datasource.getConnection()) {
            //  String strrr = Long.toString(AgeYears);
            CallableStatement GetPDx = connection.prepareCall("begin :pdx_validation := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ICD10_PREMDC(:p_pdx_code,:AgeDay,:AgeYear,:p_patient_sex); end;");
            GetPDx.registerOutParameter("pdx_validation", OracleTypes.CURSOR);
            GetPDx.setString("p_pdx_code", p_pdx_code.trim());
            GetPDx.setString("AgeDay", Days);
            GetPDx.setString("AgeYear", Years);
//            GetPDx.setString("AgeMax", MaxAge);
            GetPDx.setString("p_patient_sex", p_patient_sex);
            GetPDx.execute();
            ResultSet resultset = (ResultSet) GetPDx.getObject("pdx_validation");
            if (resultset.next()) {
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
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

    //public DRGWSResult GetBMDC(final DataSource datasource, final String p_pdx_code) {
    public DRGWSResult GetBMDC(final DataSource datasource, final String p_pdx_code) {
        DRGWSResult result = utility.DRGWSResult();
        BMDCPreMDCResult bmdcResult = new BMDCPreMDCResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetBMDC = connection.prepareCall("begin :bmdc_validation := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_BMDC_VALIDATION_PREMDC(:p_pdx_code); end;");
            GetBMDC.registerOutParameter("bmdc_validation", OracleTypes.CURSOR);
            GetBMDC.setString("p_pdx_code", p_pdx_code);
            GetBMDC.execute();
            ResultSet bmdcresultset = (ResultSet) GetBMDC.getObject("bmdc_validation");
            if (bmdcresultset.next()) {
                bmdcResult.setICD10(bmdcresultset.getString("ICD10"));
                bmdcResult.setMDC_F(bmdcresultset.getString("MDC_F"));
                bmdcResult.setPDC_F(bmdcresultset.getString("PDC_F"));
                bmdcResult.setMDC_M(bmdcresultset.getString("MDC_M"));
                bmdcResult.setPDC_M(bmdcresultset.getString("PDC_M"));
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
            result.setResult(utility.objectMapper().writeValueAsString(bmdcResult));
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET PDC MAIN TABLE
    public DRGWSResult GetPDC(final DataSource datasource, final String pdcs, final String mdc) {
        DRGWSResult result = utility.DRGWSResult();

        try (Connection connection = datasource.getConnection()) {
            //Get BMDC Validation Result
            CallableStatement getPDC = connection.prepareCall("begin :pdc_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PDC(:pdcs,:mdc); end;");
            getPDC.registerOutParameter("pdc_output", OracleTypes.CURSOR);
            getPDC.setString("pdcs", pdcs);
            getPDC.setString("mdc", mdc);
            getPDC.execute();
            ResultSet Pdcresultset = (ResultSet) getPDC.getObject("pdc_output");
            if (Pdcresultset.next()) {
                PDC pdcReult = new PDC();
                pdcReult.setPDC(Pdcresultset.getString("PDC"));
                pdcReult.setCTYPE(Pdcresultset.getString("CTYPE"));
                pdcReult.setHIERAR(Pdcresultset.getInt("HIERAR"));
                pdcReult.setCNAME(Pdcresultset.getString("CNAME"));
                pdcReult.setMDC(Pdcresultset.getString("MDC"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(pdcReult));
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult PROCESS PROCEDURE
    public String ProcedureExecute(final CombinationCode combinationcode) {
        String result = "";
        List<String> indexlist = Arrays.asList(combinationcode.getIndexlist().split(","));
        List<String> comcode = Arrays.asList(combinationcode.getComcode().split(","));
        List<String> proclist = Arrays.asList(combinationcode.getProclist().split(","));
        Set<String> set1 = new HashSet<>(proclist);
        set1.removeAll(indexlist);
        for (int y = 0; y < comcode.size(); y++) {
            set1.add(comcode.get(y));
        }
        result = String.join(", ", set1);
        return result;
    }

    //public MethodResult PROCESS PROCEDURE BEFORE SAVING SA DATA TO DATABASE
    public String FrontProcedureExecute(final CombinationCode combinationcode) {
        String result = "";
        List<String> indexlist = Arrays.asList(combinationcode.getIndexlist().split(","));
        List<String> comcode = Arrays.asList(combinationcode.getComcode().split(","));
        List<String> proclist = Arrays.asList(combinationcode.getProclist().split(","));
        Set<String> set1 = new HashSet<>(proclist);
        set1.removeAll(indexlist);
        for (int y = 0; y < comcode.size(); y++) {
            if (comcode.get(y).equals("")) {
            } else {
                set1.add(comcode.get(y));
            }
        }
        result = set1.toString();
        return result;
    }

    //public MethodResult GET PDC MAIN TABLE
    public String GetPDCUsePDx(final DataSource datasource, final String pdx) {
        String result = "";
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getPDCUsePDx = connection.prepareCall("begin :pdc_pdx := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PDC_USE_PDX(:pdx); end;");
            getPDCUsePDx.registerOutParameter("pdc_pdx", OracleTypes.CURSOR);
            getPDCUsePDx.setString("pdx", pdx);
            getPDCUsePDx.execute();
            ResultSet PDCPDxResultset = (ResultSet) getPDCUsePDx.getObject("pdc_pdx");
            if (PDCPDxResultset.next()) {
                result = PDCPDxResultset.getString("PDC");
            }
        } catch (SQLException ex) {
            result = ex.toString();
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET MDC MAIN TABLE
    public DRGWSResult MDC(final DataSource datasource, final String mdc) {
        DRGWSResult result = utility.DRGWSResult();
        MDC mdcData = new MDC();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetMDC = connection.prepareCall("begin :mdc_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_MDC(:mdcs); end;");
            GetMDC.registerOutParameter("mdc_output", OracleTypes.CURSOR);
            GetMDC.setString("mdc", mdc);
            GetMDC.execute();
            ResultSet mdcresultset = (ResultSet) GetMDC.getObject("mdc_output");
            if (mdcresultset.next()) {
                mdcData.setMDC(mdcresultset.getString("MDC"));
                mdcData.setDESCRIPTION(mdcresultset.getString("DESCRIPTION"));
                mdcData.setLABEL(mdcresultset.getString("LABEL"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(mdcData));
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET DRG MAIN TABLE
    public DRGWSResult DRG(final DataSource datasource, final String dcs, final String drgs) {
        DRGWSResult result = utility.DRGWSResult();
        DRGOutput drgOutput = new DRGOutput();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement Getdrg = connection.prepareCall("begin :drg_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_DRG(:dcs,:drgs); end;");
            Getdrg.registerOutParameter("drg_output", OracleTypes.CURSOR);
            Getdrg.setString("dcs", dcs);
            Getdrg.setString("drgs", drgs);
            Getdrg.execute();
            ResultSet drgresultset = (ResultSet) Getdrg.getObject("drg_output");
            if (drgresultset.next()) {
                drgOutput.setRW(drgresultset.getString("RW"));
                drgOutput.setWTLOS(drgresultset.getString("WTLOS"));
                drgOutput.setOT(drgresultset.getString("OT"));
                drgOutput.setMDF(drgresultset.getString("MDF"));
                drgOutput.setDRGName(drgresultset.getString("DRGNAME"));
                drgOutput.setDRG(drgresultset.getString("DRG"));
                drgOutput.setMDC(drgresultset.getString("MDC"));
                drgOutput.setDC(drgresultset.getString("DC"));
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(drgOutput));
                result.setMessage(drgOutput.getDRGName());
            } else {
                result.setSuccess(false);
            }
        } catch (IOException | SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET DRG MAIN TABLE
    public DRGWSResult MDCProcedure(final DataSource datasource, String icd9code, final String mdcs) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetMDCProcedure = connection.prepareCall("begin :join_icd9_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ICD9_JOIN_TABLE(:icd9code,:mdcs); end;");
            GetMDCProcedure.registerOutParameter("join_icd9_output", OracleTypes.CURSOR);
            GetMDCProcedure.setString("icd9code", icd9code.trim());
            GetMDCProcedure.setString("mdcs", mdcs.trim());
            GetMDCProcedure.execute();
            MDCProcedure mdcProcedure = new MDCProcedure();
            ResultSet MDCProcResultset = (ResultSet) GetMDCProcedure.getObject("join_icd9_output");
            if (MDCProcResultset.next()) {
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
            } else {
                result.setSuccess(false);
            }

        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET Result use PROCEDURE TO CHECK PDC Value 
    public DRGWSResult PDCUseProcedureChecking(final DataSource datasource, String icd9code, final String pdcs) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetPDCProcedure = connection.prepareCall("begin :join_icd9_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PROC_PDC(:icd9code,:pdcs); end;");
            GetPDCProcedure.registerOutParameter("join_icd9_output", OracleTypes.CURSOR);
            GetPDCProcedure.setString("icd9code", icd9code);
            GetPDCProcedure.setString("pdcs", pdcs);
            GetPDCProcedure.execute();
            MDCProcedure mdcProcedure = new MDCProcedure();
            ResultSet PDCProcResultset = (ResultSet) GetPDCProcedure.getObject("join_icd9_output");
            if (PDCProcResultset.next()) {
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
                result.setResult(String.valueOf(mdcProcedure));
                result.setMessage("");
            }
            if (PDCProcResultset.next()) {
                result.setSuccess(true);
                result.setResult(utility.objectMapper().writeValueAsString(mdcProcedure));
            } else {
                result.setSuccess(false);
                result.setMessage("Condition not found");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET PDx Malignancy
    public DRGWSResult PDxMalignancy(final DataSource datasource, final String primaryPDx, final String pdcs) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetMalignantPDx = connection.prepareCall("begin :pdx_malignant := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PDX_MALIGNANCY(:primaryPDx,:pdcs); end;");
            GetMalignantPDx.registerOutParameter("pdx_malignant", OracleTypes.CURSOR);
            GetMalignantPDx.setString("primaryPDx", primaryPDx);
            GetMalignantPDx.setString("pdcs", pdcs);
            GetMalignantPDx.execute();
            ResultSet MalignantResultset = (ResultSet) GetMalignantPDx.getObject("pdx_malignant");
            if (MalignantResultset.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult OR Procedure
    public DRGWSResult ORProcedure(final DataSource datasource, final String orpCode) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetORproce = connection.prepareCall("begin :get_orp := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PROC_ORP(:orpCode); end;");
            GetORproce.registerOutParameter("get_orp", OracleTypes.CURSOR);
            GetORproce.setString("orpCode", orpCode);
            GetORproce.execute();
            ResultSet ORProceResultset = (ResultSet) GetORproce.getObject("get_orp");
            if (ORProceResultset.next()) {
                result.setSuccess(true);
                result.setMessage(ORProceResultset.getString("PROC_SITE"));
                result.setResult(ORProceResultset.getString("PROCGR"));
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET DC
    public DRGWSResult GetDC(final DataSource datasource, final String dcs) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetDC = connection.prepareCall("begin :dcs_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_DC(:dcs); end;");
            GetDC.registerOutParameter("dcs_output", OracleTypes.CURSOR);
            GetDC.setString("dcs", dcs);
            GetDC.execute();
            ResultSet DCResultset = (ResultSet) GetDC.getObject("dcs_output");
            if (DCResultset.next()) {
                DC dcresults = new DC();
                result.setSuccess(true);
                dcresults.setDDCOL(DCResultset.getString("DCCOL"));
                dcresults.setDRGX(DCResultset.getString("DRGX"));
                dcresults.setCNAME(DCResultset.getString("CNAME"));
                dcresults.setDC(DCResultset.getString("DC"));
                dcresults.setMDC(DCResultset.getString("MDC"));
                result.setResult(utility.objectMapper().writeValueAsString(dcresults));
                result.setMessage(DCResultset.getString("DCCOL"));
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET PCOM
    public DRGWSResult GetPCOM(final DataSource datasource, final String code1, final String code2) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetPCOM = connection.prepareCall("begin :pcom := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_PCOM(:code1,:code2); end;");
            GetPCOM.registerOutParameter("pcom", OracleTypes.CURSOR);
            GetPCOM.setString("code1", code1);
            GetPCOM.setString("code2", code2);
            GetPCOM.execute();
            ResultSet PCOMResultset = (ResultSet) GetPCOM.getObject("pcom");
            if (PCOMResultset.next()) {
                PCOM pcom = new PCOM();
                result.setSuccess(true);
                pcom.setCode(PCOMResultset.getString("CODE"));
                pcom.setCode1(PCOMResultset.getString("CODE1"));
                pcom.setCode2(PCOMResultset.getString("CODE2"));
                pcom.setDesc1(PCOMResultset.getString("DESC1"));
                pcom.setDesc2(PCOMResultset.getString("DESC2"));
                pcom.setDescription(PCOMResultset.getString("DESCRIPTION"));
                pcom.setPdc(PCOMResultset.getString("PDC"));
                // result.setResult(utility.objectMapper().writeValueAsString(pcom));
                result.setResult(PCOMResultset.getString("CODE"));
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET CCL
    public DRGWSResult GetCCL(final DataSource datasource, final String ccrows) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetCCL = connection.prepareCall("begin :cclm_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CCL(:ccrows); end;");
            GetCCL.registerOutParameter("cclm_output", OracleTypes.CURSOR);
            GetCCL.setString("ccrows", ccrows);
            GetCCL.execute();
            ResultSet CCLResultset = (ResultSet) GetCCL.getObject("cclm_output");
            if (CCLResultset.next()) {
                CCL cclresults = new CCL();
                result.setSuccess(true);
                cclresults.setCcrow(CCLResultset.getString("CCROW"));
                cclresults.setCcl(CCLResultset.getString("CCL"));
                result.setResult(utility.objectMapper().writeValueAsString(cclresults));
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult OR Procedure
    public DRGWSResult UnralatedANDORProc(final DataSource datasource, final String icd9codes, final String mdccode) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetUnralatedORproce = connection.prepareCall("begin :unralated_or_proc := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_UNRALATED_PROC_ORPROC(:icd9codes,:mdccode); end;");
            GetUnralatedORproce.registerOutParameter("unralated_or_proc", OracleTypes.CURSOR);
            GetUnralatedORproce.setString("icd9codes", icd9codes);
            GetUnralatedORproce.setString("mdccode", mdccode);
            GetUnralatedORproce.execute();
            ResultSet UnralatedORProceResultset = (ResultSet) GetUnralatedORproce.getObject("unralated_or_proc");
            if (UnralatedORProceResultset.next()) {
                result.setSuccess(true);
                result.setResult(UnralatedORProceResultset.getString("PROCGR"));
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult TRAUMAICD10 CHECK FOR POSSIBLE TRAUMA 
    public DRGWSResult TRAUMAICD10(final DataSource datasource, final String sdx) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement conn = connection.prepareCall("begin :trauma_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.TRAUMAICD10(:sdx); end;");
            conn.registerOutParameter("trauma_output", OracleTypes.CURSOR);
            conn.setString("sdx", sdx);
            conn.execute();
            ResultSet connResult = (ResultSet) conn.getObject("trauma_output");
            if (connResult.next()) {
                result.setResult(connResult.getString("TRAUMA"));
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    //public MethodResult TRAUMAICD9 CHECK FOR POSSIBLE TRAUMA 
    public DRGWSResult TRAUMAICD9CM(final DataSource datasource, final String icdproc) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement conn = connection.prepareCall("begin :trauma_output := DRG_SHADOWBILLING.DRGPKGFUNCTION.TRAUMAICD9CM(:icdproc); end;");
            conn.registerOutParameter("trauma_output", OracleTypes.CURSOR);
            conn.setString("icdproc", icdproc);
            conn.execute();
            ResultSet connResult = (ResultSet) conn.getObject("trauma_output");
            if (connResult.next()) {
                result.setResult(connResult.getString("PROC_SITE"));
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    //public MethodResult validcode ICD10CM 
    public DRGWSResult GetValidCodeICD10(final DataSource datasource, final String p_icd10_code) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getValidCode = connection.prepareCall("begin :p_validcode := DRG_SHADOWBILLING.DRGPKGFUNCTION.get_valid_icd10(:p_icd10_code); end;");
            getValidCode.registerOutParameter("p_validcode", OracleTypes.CURSOR);
            getValidCode.setString("p_icd10_code", p_icd10_code.trim());
            getValidCode.execute();
            ResultSet getValidCodeResult = (ResultSet) getValidCode.getObject("p_validcode");
            if (getValidCodeResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET AGE VALIDATION THIS AREA
    public DRGWSResult AgeConfictValidation(final DataSource datasource,
            final String p_pdx_code,
            final String age_day,
            final String age_min_year) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getAgeValidation = connection.prepareCall("begin :age_validation := DRG_SHADOWBILLING.DRGPKGFUNCTION.VALIDATE_AGE(:p_pdx_code,:age_day,:age_min_year); end;");
            getAgeValidation.registerOutParameter("age_validation", OracleTypes.CURSOR);
            getAgeValidation.setString("p_pdx_code", p_pdx_code.trim());
            getAgeValidation.setString("age_day", age_day);
            getAgeValidation.setString("age_min_year", age_min_year);
            getAgeValidation.execute();
            ResultSet getAgeValidationResult = (ResultSet) getAgeValidation.getObject("age_validation");
            if (getAgeValidationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET GENDER VALIDATION THIS AREA
    public DRGWSResult GenderConfictValidation(final DataSource datasource,
            final String p_pdx_code,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getSexValidation = connection.prepareCall("begin :gender_validation := DRG_SHADOWBILLING.DRGPKGFUNCTION.VALIDATE_GENDER(:p_pdx_code,:gender); end;");
            getSexValidation.registerOutParameter("gender_validation", OracleTypes.CURSOR);
            getSexValidation.setString("p_pdx_code", p_pdx_code.trim());
            getSexValidation.setString("gender", gender);
            getSexValidation.execute();
            ResultSet getSexValidationResult = (ResultSet) getSexValidation.getObject("gender_validation");
            if (getSexValidationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // GET SEX PROC VALIDATION THIS AREA
    public DRGWSResult GenderConfictValidationProc(final DataSource datasource,
            final String procode,
            final String gender) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getSexProcValidation = connection.prepareCall("begin :age_proc_validation := DRG_SHADOWBILLING.DRGPKGFUNCTION.PROC_AGE_VALIDATION(:procode,:gender); end;");
            getSexProcValidation.registerOutParameter("age_proc_validation", OracleTypes.CURSOR);
            getSexProcValidation.setString("procode", procode.trim());
            getSexProcValidation.setString("gender", gender);
            getSexProcValidation.execute();
            ResultSet getSexProcValidationResult = (ResultSet) getSexProcValidation.getObject("age_proc_validation");
            if (getSexProcValidationResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult Get AX
    public DRGWSResult AX(final DataSource datasource, final String axcodes, final String requestcode) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            if (requestcode.trim().isEmpty()) {
                result.setSuccess(false);
            } else {
                CallableStatement GetAX = connection.prepareCall("begin :get_ax := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_AX_PARAM(:axcodes); end;");
                GetAX.registerOutParameter("get_ax", OracleTypes.CURSOR);
                GetAX.setString("axcodes", axcodes.trim());
                GetAX.execute();
                ResultSet AXResultset = (ResultSet) GetAX.getObject("get_ax");
                if (AXResultset.next()) {
                    List<String> asdas = Arrays.asList(AXResultset.getString("CODES").split(","));
                    for (int x = 0; x < asdas.size(); x++) {
                        if (requestcode.trim().equals(asdas.get(x).trim())) {
                            result.setResult(requestcode);
                            result.setSuccess(true);
                            break;
                        } else {
                            result.setSuccess(false);
                        }
                    }
                } else {
                    result.setSuccess(false);
                }
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult Get AX
    public DRGWSResult MainCCChecking(final DataSource datasource, final String ccCode, final String mdcCode) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement GetMainCC = connection.prepareCall("begin :maincc := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_MAINCC_USED_ICD10(:ccCode,:mdcCode); end;");
            GetMainCC.registerOutParameter("maincc", OracleTypes.CURSOR);
            GetMainCC.setString("ccCode", ccCode);
            GetMainCC.setString("mdcCode", mdcCode);
            GetMainCC.execute();
            ResultSet MainCCResultset = (ResultSet) GetMainCC.getObject("maincc");
            if (MainCCResultset.next()) {
                result.setResult(ccCode);
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult Get MAJOR OR PROCEDURE
    public DRGWSResult MajorORPRrocedure(final DataSource datasource, final String icd9codes, final String mdcs, final String pdcs) {
        DRGWSResult result = utility.DRGWSResult();
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
            } else {
                result.setSuccess(false);
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //public MethodResult GET_ICD9_MDC
    public DRGWSResult Endovasc(final DataSource datasource, final String proce, final String pdcs, final String mdcs) {
        DRGWSResult result = utility.DRGWSResult();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement Endovasc = connection.prepareCall("begin :get_icd9_cm := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_ICD9_MDC(:proce,:pdcs,:mdcs); end;");
            Endovasc.registerOutParameter("get_icd9_cm", OracleTypes.CURSOR);
            Endovasc.setString("proce", proce);
            Endovasc.setString("pdcs", pdcs);
            Endovasc.setString("mdcs", mdcs);
            Endovasc.execute();
            ResultSet EndovascResult = (ResultSet) Endovasc.getObject("get_icd9_cm");
            if (EndovascResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }

        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //Get PCCL
    public DRGWSResult GetPCCL(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter, final String sdxfinalList) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement ps = connection.prepareCall("call DRG_SHADOWBILLING.DRGPKGPROCEDURE.GET_PCCL(:p_pccl,:pdx,:sdx,:p_dc)");
            ps.registerOutParameter("p_pccl", OracleTypes.NUMBER);
            ps.setString("pdx", grouperparameter.getPdx());
            ps.setString("sdx", sdxfinalList);
            ps.setString("p_dc", drgResult.getDC());
            ps.execute();
            // if (ps.getString("Message").equals("SUCC")) {

            drgResult.setDRG(drgResult.getDC() + "" + ps.getString("p_pccl"));
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setSuccess(true);
//            } else {
//                result.setMessage(ps.getString("Message"));
//            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //Get Validate PCCL Value
    public DRGWSResult ValidatePCCL(final DataSource datasource, final String dcs, final String drgs) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {

            String cclval = drgs.substring(5 - 1, 5);
            switch (Integer.parseInt(cclval)) {
                case 4:
                    DRGWSResult drgname3 = this.DRG(datasource, dcs, dcs + "3");
                    DRGWSResult drgname222 = this.DRG(datasource, dcs, dcs + "2");
                    DRGWSResult drgname111 = this.DRG(datasource, dcs, dcs + "1");
                    if (drgname3.isSuccess()) {
                        result.setResult("3");
                    } else if (drgname222.isSuccess()) {
                        result.setResult("2");
                    } else if (drgname111.isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                case 3:
                    DRGWSResult drgname4 = this.DRG(datasource, dcs, dcs + "4");
                    DRGWSResult drgname2 = this.DRG(datasource, dcs, dcs + "2");
                    DRGWSResult drgname11 = this.DRG(datasource, dcs, dcs + "1");
                    if (drgname4.isSuccess()) {
                        result.setResult("4");
                    } else if (drgname2.isSuccess()) {
                        result.setResult("2");
                    } else if (drgname11.isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                case 2:
                    DRGWSResult drgname333 = this.DRG(datasource, dcs, dcs + "3");
                    DRGWSResult drgname444 = this.DRG(datasource, dcs, dcs + "4");
                    DRGWSResult drgname1 = this.DRG(datasource, dcs, dcs + "1");
                    if (drgname333.isSuccess()) {
                        result.setResult("3");
                    } else if (drgname444.isSuccess()) {
                        result.setResult("4");
                    } else if (drgname1.isSuccess()) {
                        result.setResult("1");
                    }
                    result.setSuccess(true);
                    break;
                case 1:
                    DRGWSResult drgname22 = this.DRG(datasource, dcs, dcs + "2");
                    DRGWSResult drgname33 = this.DRG(datasource, dcs, dcs + "3");
                    DRGWSResult drgname44 = this.DRG(datasource, dcs, dcs + "4");
                    if (drgname22.isSuccess()) {
                        result.setResult("2");
                    } else if (drgname33.isSuccess()) {
                        result.setResult("3");
                    } else if (drgname44.isSuccess()) {
                        result.setResult("4");
                    }
                    result.setSuccess(true);
                    break;
            }

        } catch (NumberFormatException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //Get CCL Valiue
    public int GetCCLValue(final DataSource datasource, final String dccol, final String ccrows) {
        int result = 0;
        try (Connection connection = datasource.getConnection()) {
            CallableStatement getccrowval = connection.prepareCall("begin :cclvalue := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_CCLVALUE(:ccrows); end;");
            getccrowval.registerOutParameter("cclvalue", OracleTypes.CURSOR);
            getccrowval.setString("ccrows", ccrows);
            getccrowval.execute();
            ResultSet cclList = (ResultSet) getccrowval.getObject("cclvalue");
            if (cclList.next()) {
                //Get Value suing inderx off
                String longstring = cclList.getString("CCL");
                int x = Integer.parseInt(dccol);
                String cclval = longstring.substring(x - 1, Integer.parseInt(dccol));
                result = Integer.parseInt(cclval);

            }
        } catch (SQLException ex) {
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //Get Check Exclusion List
    public DRGWSResult CheckExclusionList(final DataSource datasource, final String sdx, final String pdx) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement exclusionlist = connection.prepareCall("begin :getexclusion := DRG_SHADOWBILLING.DRGPKGFUNCTION.GET_EXCLUSION(:sdx,:pdx); end;");
            exclusionlist.registerOutParameter("getexclusion", OracleTypes.CURSOR);
            exclusionlist.setString("sdx", sdx);
            exclusionlist.setString("pdx", pdx);
            exclusionlist.execute();
            ResultSet EndovascResult = (ResultSet) exclusionlist.getObject("getexclusion");
            if (EndovascResult.next()) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    // Clean SDx List
    public String CleanSDxDCDetermination(final DataSource datasource, final String sdxoriglist, final String sdxdcfinder, final String pdx, final String dcs) {
        String result = "";
        try {
            LinkedList<String> SDXoriglist = new LinkedList<>();
            List<String> sdxlist = Arrays.asList(sdxoriglist.split(","));
            LinkedList<Integer> valuelist = new LinkedList<>();
            for (int i = 0; i < sdxlist.size(); i++) {
                SDXoriglist.add(sdxlist.get(i).trim());
            }

            if (sdxdcfinder != null) {
                LinkedList<String> SDxUseDC = new LinkedList<>();
                List<String> sdxfinder = Arrays.asList(sdxdcfinder.split(","));
                for (int i = 0; i < sdxfinder.size(); i++) {
                    SDxUseDC.add(sdxfinder.get(i).trim());
                }
                //===============================
                if (SDxUseDC.size() == 1) {
                    // if the size is one removal process only can be perform
                    SDXoriglist.remove(SDxUseDC.get(0));
//                    //GET DCCOL USING DC
                    String DCCol = "";
                    DRGWSResult detdccol = GetDC(datasource, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = this.CheckExclusionList(datasource, SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = this.GetICD10PreMDC(datasource, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = this.GetCCLValue(datasource, DCCol, getccrowResult.getMessage());
                            valuelist.add(cclvalueresult);
                        }
                    }
                    //Arrange value from highest to lowest
                    Collections.sort(valuelist, Collections.reverseOrder());
                    //Assigning Variable area
                    double B, G, H, I, PCCL, sum = 0;
                    ArrayList<Double> valueresult = new ArrayList<>();
                    //GETTING THE LAST DIGIT PCCL Value
                    for (int a = 0; a < valuelist.size(); a++) {
                        B = -0.4 * a;
                        valueresult.add(valuelist.get(a) * Math.exp(B));
                    }

                    for (int i = 0; i < valueresult.size(); i++) {
                        sum += valueresult.get(i);
                    }
                    G = 1 + sum;
                    H = Math.log(G);
                    I = Math.log(3 / 0.4) / 4;
                    PCCL = H / I;
                    //----------------------------------------------
                    if (Math.round(PCCL) > 4) {
                        result = String.valueOf(4);
                    } else {
                        result = String.valueOf(Math.round(PCCL));
                    }

                } else if (SDxUseDC.isEmpty()) {
                    //GET DCCOL USING DC
                    String DCCol = "";
                    DRGWSResult detdccol = GetDC(datasource, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = this.CheckExclusionList(datasource, SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = GetICD10PreMDC(datasource, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = this.GetCCLValue(datasource, DCCol, getccrowResult.getMessage());
                            valuelist.add(cclvalueresult);
                        }
                    }
                    //Arrange value from highest to lowest
                    Collections.sort(valuelist, Collections.reverseOrder());
                    //Assigning Variable area
                    double B, G, H, I, PCCL, sum = 0;
                    ArrayList<Double> valueresult = new ArrayList<>();
                    //GETTING THE LAST DIGIT PCCL Value
                    for (int a = 0; a < valuelist.size(); a++) {
                        B = -0.4 * a;
                        valueresult.add(valuelist.get(a) * Math.exp(B));
                    }

                    for (int i = 0; i < valueresult.size(); i++) {
                        sum += valueresult.get(i);
                    }
                    G = 1 + sum;
                    H = Math.log(G);
                    I = Math.log(3 / 0.4) / 4;
                    PCCL = H / I;
                    if (Math.round(PCCL) > 4) {
                        result = String.valueOf(4);
                    } else {
                        result = String.valueOf(Math.round(PCCL));
                    }
                    //----------------------------------------------

                } else {
                    // remove all elements inside SDxUseDC from SDXoriglist
                    for (int i = 0; i < SDxUseDC.size(); i++) {
                        SDXoriglist.remove(SDxUseDC.get(i));
                    }
                    //Arrange all SDx Inside use to determinie dc
                    Collections.sort(SDxUseDC, Collections.reverseOrder());
                    //Remove Higher value
                    SDxUseDC.remove(SDxUseDC.get(0));
                    //Combine all value from tow linkedlist
                    for (int i = 0; i < SDxUseDC.size(); i++) {
                        SDXoriglist.add(SDxUseDC.get(i));
                    }

                    //GET DCCOL USING DC
                    String DCCol = "";
                    DRGWSResult detdccol = GetDC(datasource, dcs);
                    if (detdccol.isSuccess()) {
                        DCCol = detdccol.getMessage();
                    }
                    //END OF GETTING DCCOL USING DC
                    for (int x = 0; x < SDXoriglist.size(); x++) {
                        DRGWSResult exclusionlistcheck = CheckExclusionList(datasource, SDXoriglist.get(x), pdx);
                        if (!exclusionlistcheck.isSuccess()) {
                            //GET CCROW USING CLEAN SDX
                            DRGWSResult getccrowResult = GetICD10PreMDC(datasource, SDXoriglist.get(x));
                            //GET CCL VALUE USING SDX CCROW   
                            int cclvalueresult = GetCCLValue(datasource, DCCol, getccrowResult.getMessage());
                            valuelist.add(cclvalueresult);

                        }
                    }
                    //Arrange value from highest to lowest
                    Collections.sort(valuelist, Collections.reverseOrder());
                    //Assigning Variable area
                    double B, G, H, I, PCCL, sum = 0;
                    ArrayList<Double> valueresult = new ArrayList<>();
                    //GETTING THE LAST DIGIT PCCL Value
                    for (int a = 0; a < valuelist.size(); a++) {
                        B = -0.4 * a;
                        valueresult.add(valuelist.get(a) * Math.exp(B));
                    }

                    for (int i = 0; i < valueresult.size(); i++) {
                        sum += valueresult.get(i);
                    }
                    G = 1 + sum;
                    H = Math.log(G);
                    I = Math.log(3 / 0.4) / 4;
                    PCCL = H / I;
                    //----------------------------------------------
                    if (Math.round(PCCL) > 4) {
                        result = String.valueOf(4);
                    } else {
                        result = String.valueOf(Math.round(PCCL));
                    }

                }
            } else {
                //GET DCCOL USING DC
                String DCCol = "";
                DRGWSResult detdccol = GetDC(datasource, dcs);
                if (detdccol.isSuccess()) {
                    DCCol = detdccol.getMessage();
                }
                //END OF GETTING DCCOL USING DC
                for (int x = 0; x < SDXoriglist.size(); x++) {
                    DRGWSResult exclusionlistcheck = CheckExclusionList(datasource, SDXoriglist.get(x), pdx);
                    if (!exclusionlistcheck.isSuccess()) {
                        //GET CCROW USING CLEAN SDX
                        DRGWSResult getccrowResult = GetICD10PreMDC(datasource, SDXoriglist.get(x));
                        //GET CCL VALUE USING SDX CCROW   
                        int cclvalueresult = GetCCLValue(datasource, DCCol, getccrowResult.getMessage());
                        valuelist.add(cclvalueresult);
                    }
                }
                //Arrange value from highest to lowest
                Collections.sort(valuelist, Collections.reverseOrder());
                //Assigning Variable area
                double B, G, H, I, PCCL, sum = 0;
                ArrayList<Double> valueresult = new ArrayList<>();
                //GETTING THE LAST DIGIT PCCL Value
                for (int a = 0; a < valuelist.size(); a++) {
                    B = -0.4 * a;
                    valueresult.add(valuelist.get(a) * Math.exp(B));
                }

                for (int i = 0; i < valueresult.size(); i++) {
                    sum += valueresult.get(i);
                }
                G = 1 + sum;
                H = Math.log(G);
                I = Math.log(3 / 0.4) / 4;
                PCCL = H / I;
                //----------------------------------------------
                if (Math.round(PCCL) > 4) {
                    result = String.valueOf(4);
                } else {
                    result = String.valueOf(Math.round(PCCL));
                }

            }

        } catch (NumberFormatException ex) {
            result = ex.toString();
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //cleaning sdx for PLSQL PCCL Determination
    public String CleanSDxDCDeterminationPLSQL(final DataSource datasource, final String sdxoriglist, final String sdxdcfinder, final String pdx, final String dcs) {
        String result = "";
        try {
            LinkedList<String> SDXoriglist = new LinkedList<>();
            List<String> sdxlist = Arrays.asList(sdxoriglist.split(","));
            for (int i = 0; i < sdxlist.size(); i++) {
                SDXoriglist.add(sdxlist.get(i).trim());
            }

            if (sdxdcfinder != null) {
                LinkedList<String> SDxUseDC = new LinkedList<>();
                List<String> sdxfinder = Arrays.asList(sdxdcfinder.split(","));
                for (int i = 0; i < sdxfinder.size(); i++) {
                    SDxUseDC.add(sdxfinder.get(i).trim());
                }
                //===============================
                if (SDxUseDC.size() == 1) {
                    // if the size is one removal process only can be perform
                    Collections.sort(SDXoriglist, Collections.reverseOrder());
                    SDXoriglist.remove(SDxUseDC.get(0));
                    result = String.join(",", SDXoriglist);
                } else if (SDxUseDC.isEmpty()) {
                    //GET DCCOL USING DC
                    Collections.sort(SDXoriglist, Collections.reverseOrder());
                    result = String.join(",", SDXoriglist);
                } else {
                    // remove all elements inside SDxUseDC from SDXoriglist
                    for (int i = 0; i < SDxUseDC.size(); i++) {
                        SDXoriglist.remove(SDxUseDC.get(i));
                    }
                    //Arrange all SDx Inside use to determinie dc
                    Collections.sort(SDxUseDC, Collections.reverseOrder());
                    //Remove Higher value
                    SDxUseDC.remove(SDxUseDC.get(0));
                    //Combine all value from tow linkedlist
                    for (int i = 0; i < SDxUseDC.size(); i++) {
                        SDXoriglist.add(SDxUseDC.get(i));
                    }
                    result = String.join(",", SDXoriglist);

                }
            } else {
                Collections.sort(SDXoriglist, Collections.reverseOrder());
                result = String.join(",", SDXoriglist);
            }

        } catch (NumberFormatException ex) {
            result = ex.toString();
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    //Get Check Exclusion List
    public DRGWSResult COUNTBMDCICD10CODE(final DataSource datasource, final String icd10code) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGFUNCTION.COUNTBMDCICD10CODE(:icd10code); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.setString("icd10code", icd10code.trim());
            statement.execute();
            ResultSet resultSet = (ResultSet) statement.getObject("v_results");
            if (resultSet.next()) {
                if (Integer.parseInt(resultSet.getString("CODECOUNT")) > 1) {
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                }
            } else {
                result.setSuccess(false);
            }
        } catch (SQLException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

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
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult SeekerDRG(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        ArrayList<DRGOutput> drgList = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGFUNCTION.SeekerDRG(); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.execute();
            ResultSet resultset = (ResultSet) statement.getObject("v_results");
            while (resultset.next()) {
                DRGOutput drg = new DRGOutput();
                drg.setDC(resultset.getString("DC"));
                drg.setDRG(resultset.getString("DRG"));
                drg.setDRGName(resultset.getString("DRGNAME"));
                drg.setMDC(resultset.getString("MDC"));
                drg.setRW(resultset.getString("RW"));
                drgList.add(drg);
            }
            if (drgList.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(drgList));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult SeekerICD9cm(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGFUNCTION.SeekerICD9cm(); end;");
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
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public DRGWSResult SeekerRVS(final DataSource datasource) {
        DRGWSResult result = utility.DRGWSResult();
        result.setSuccess(false);
        result.setMessage("");
        result.setResult("");
        try (Connection connection = datasource.getConnection()) {
            CallableStatement statement = connection.prepareCall("begin :v_results := DRG_SHADOWBILLING.DRGPKGFUNCTION.SeekerRVS(); end;");
            statement.registerOutParameter("v_results", OracleTypes.CURSOR);
            statement.execute();
            ArrayList<RVS> rvsList = new ArrayList<>();
            ResultSet resultset = (ResultSet) statement.getObject("v_results");
            while (resultset.next()) {
                RVS rvs = new RVS();
                rvs.setRvscode(resultset.getString("RVSCODE"));
                rvs.setDescription(resultset.getString("DESCRIPTION"));
                rvsList.add(rvs);
            }
            if (rvsList.size() > 0) {
                result.setMessage("OK");
                result.setResult(utility.objectMapper().writeValueAsString(rvsList));
                result.setSuccess(true);
            } else {
                result.setMessage("N/A");
            }
        } catch (SQLException | IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GrouperMethod.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
