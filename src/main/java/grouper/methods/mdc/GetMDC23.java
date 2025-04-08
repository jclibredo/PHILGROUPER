/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
import grouper.utility.GrouperMethod;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class GetMDC23 {

    public GetMDC23() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC23(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            //CHECKING FOR TRAUMA CODES
            ArrayList<String> sdxfinder = new ArrayList<>();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int ORProcedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int Counter23PBX = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                //AX 99PDX Checking
                if (utility.isValid99PDX(ProcedureList.get(x).trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(x).trim())) {
                    PCXCounter99++;
                }
                DRGWSResult ORProcedureResult = new GrouperMethod().ORProcedure(datasource, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                if (new GrouperMethod().AX(datasource, "23PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter23PBX++;
                }
                DRGWSResult JoinResult = new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(x).trim(), drgResult.getMDC());
                if (JoinResult.isSuccess()) {
//                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }
            int Counter23BX = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (new GrouperMethod().AX(datasource, "23BX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter23BX++;
                }
            }
            if (PDXCounter99 > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (ORProcedureCounter > 0) {
                        switch (Collections.max(ORProcedureCounterList)) {
                            case 6://OR Proc Level 6 AND 5
                            case 5:
                                drgResult.setDC("2308");
                                break;
                            case 4://OR Proc Level 4
                                drgResult.setDC("2307");
                                break;
                            case 3://OR Proc Level 3
                                drgResult.setDC("2306");
                                break;
                            case 2://OR Proc Level 2
                                drgResult.setDC("2305");
                                break;
                            case 1://OR Proc Level 1
                                drgResult.setDC("2304");
                                break;
                        }

                    } else {
                        switch (drgResult.getPDC()) {
                            case "23A"://Rehabilitation
                                if (Counter23BX > 0) {
                                    for (int x = 0; x < SecondaryList.size(); x++) {
                                        if (new GrouperMethod().AX(datasource, "23BX", SecondaryList.get(x).trim()).isSuccess()) {
                                            sdxfinder.add(SecondaryList.get(x));
                                        }
                                    }
                                    if (!sdxfinder.isEmpty()) {
                                        drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                    }
                                    drgResult.setDC("2355");
                                } else {
                                    drgResult.setDC("2350");
                                }
                                break;
                            case "23B"://Signs, Symptoms and Other Abnormal Findings
                                drgResult.setDC("2351");
                                break;
                            case "23C"://Drugs
                                if (Counter23PBX > 0) {
                                    drgResult.setDC("2303");
                                } else {
                                    drgResult.setDC("2352");
                                }
                                break;
                            case "23D"://Other Factors Influencing Health Status PDC 23D
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 54) {
                                    drgResult.setDC("2353");
                                } else {
                                    drgResult.setDC("2354");
                                }
                                break;
                        }
                    }

                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("2311");
                    } else {
                        drgResult.setDC("2312");
                    }
                }

            } else if (ORProcedureCounter > 0) {
                switch (Collections.max(ORProcedureCounterList)) {
                    case 6://OR Proc Level 6 AND 5
                    case 5:
                        drgResult.setDC("2308");
                        break;
                    case 4://OR Proc Level 4
                        drgResult.setDC("2307");
                        break;
                    case 3://OR Proc Level 3
                        drgResult.setDC("2306");
                        break;
                    case 2://OR Proc Level 2
                        drgResult.setDC("2305");
                        break;
                    case 1://OR Proc Level 1
                        drgResult.setDC("2304");
                        break;
                }

            } else {
                switch (drgResult.getPDC()) {
                    case "23A"://Rehabilitation
                        if (Counter23BX > 0) {
                            for (int x = 0; x < SecondaryList.size(); x++) {
                                if (new GrouperMethod().AX(datasource, "23BX", SecondaryList.get(x).trim()).isSuccess()) {
                                    sdxfinder.add(SecondaryList.get(x));
                                }
                            }
                            if (!sdxfinder.isEmpty()) {
                                drgResult.setSDXFINDER(String.join(",", sdxfinder));
                            }
                            drgResult.setDC("2355");
                        } else {
                            drgResult.setDC("2350");
                        }
                        break;
                    case "23B"://Signs, Symptoms and Other Abnormal Findings
                        drgResult.setDC("2351");
                        break;
                    case "23C"://Drugs
                        if (Counter23PBX > 0) {
                            drgResult.setDC("2303");
                        } else {
                            drgResult.setDC("2352");
                        }
                        break;
                    case "23D"://Other Factors Influencing Health Status PDC 23D
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 54) {
                            drgResult.setDC("2353");
                        } else {
                            drgResult.setDC("2354");
                        }
                        break;
                }
            }

            if (drgResult.getDRG() == null) {

                //-------------------------------------------------------------------------------------
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                } else {
                    //----------------------------------------------------------------------
                    //  String sdxfinalList =  new GrouperMethod().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    String sdxfinalList = new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        //-----------------------------------------------------------------------
                        if (new GrouperMethod().DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
                            drgResult.setDRG(finaldrgresult.getDRG());
                            drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).getMessage());
                        } else {
                            DRGWSResult drgvalues = new GrouperMethod().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG());
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                DRGWSResult drgnames = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                drgResult.setDRGName(drgnames.getMessage());
                            } else {
                                drgResult.setDRG(finaldrgresult.getDRG());
                                drgResult.setDRGName("Grouper Error");
                            }
                        }
                    } else {
                        drgResult.setDRG(drgResult.getDC() + "X");
                        drgResult.setDRGName("Grouper Error");
                    }
                }
                //----------------------------------------------------------------------
            } else {
                if (new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 23 Done Checking");
        } catch (IOException | NumberFormatException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC23.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
