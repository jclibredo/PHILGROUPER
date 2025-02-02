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
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC23 {

    public GetMDC23() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC23(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            //CHECKING FOR TRAUMA CODES
            ArrayList<String> sdxfinder = new ArrayList<>();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            String PBX23 = "23PBX";
            int Counter23PBX = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);
                //AX 99PDX Checking
                if (utility.isValid99PDX(proc.trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(proc.trim())) {
                    PCXCounter99++;
                }
                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc.trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult ResultPBX23 = gm.AX(datasource, PBX23, proc.trim());
                if (String.valueOf(ResultPBX23.isSuccess()).equals("true")) {
                    Counter23PBX++;
                }
                DRGWSResult JoinResult = gm.MDCProcedure(datasource, proc.trim(), drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = gm.GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (String.valueOf(pdcresult.isSuccess()).equals("true")) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }

            String BX23 = "23BX";
            int Counter23BX = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                String SeconSDx = SecondaryList.get(a);
                DRGWSResult ResultBX23 = gm.AX(datasource, BX23, SeconSDx);
                if (String.valueOf(ResultBX23.isSuccess()).equals("true")) {
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
                                        DRGWSResult sdxfinderResult = gm.AX(datasource, BX23, SecondaryList.get(x));
                                        if (sdxfinderResult.isSuccess()) {
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
                                DRGWSResult sdxfinderResult = gm.AX(datasource, BX23, SecondaryList.get(x));
                                if (sdxfinderResult.isSuccess()) {
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
                    //  String sdxfinalList = gm.CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    String sdxfinalList = gm.CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = gm.GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        String drgValue = finaldrgresult.getDRG();
                        DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgValue);
                        //-----------------------------------------------------------------------
                        if (drgname.isSuccess()) {
                            drgResult.setDRG(drgValue);
                            drgResult.setDRGName(drgname.getMessage());
                        } else {
                            DRGWSResult drgvalues = gm.ValidatePCCL(datasource, drgResult.getDC(), drgValue);
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                DRGWSResult drgnames = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                drgResult.setDRGName(drgnames.getMessage());
                            } else {
                                drgResult.setDRG(drgValue);
                                drgResult.setDRGName("Grouper Error");
                            }
                        }
                    } else {
                        drgResult.setDRG("00000");
                        drgResult.setDRGName("Grouper Error");
                    }
                }
                result.setSuccess(true);
                //----------------------------------------------------------------------
            } else {
                result.setSuccess(true);
                DRGWSResult drgname = gm.DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                if (drgname.isSuccess()) {
                    drgResult.setDRGName(drgname.getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }

            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 23 Done Checking");

        } catch (IOException | NumberFormatException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC23.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
