/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.utility;

import java.util.ArrayList;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GrouperMethod {

    public GrouperMethod() {
    }
    
    
  

  

    // Get Data from NCLaims and Process for grouper
   

    //Validation for Dagger Asterisk
   

    //UPDATE DRG RESULT AND CHANGE TAGS VALUE
    

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
  

    //public MethodResult GET ACCPDX VALUE FROM ICD10_PREMDC TABLE
  

    //public Method for Insert DRG Auditrail
   

    //public Method for Insert DRG Auditrail
   

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
   

  

    //public MethodResult PROCESS PROCEDURE BEFORE SAVING SA DATA TO DATABASE
   

    //public MethodResult GET PDC MAIN TABLE
  
  
}
