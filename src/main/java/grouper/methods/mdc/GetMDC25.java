/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
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
 * @author MinoSun
 */
@RequestScoped
public class GetMDC25 {

    public GetMDC25() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC25(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
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
//            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
//            ArrayList<Integer> hierarvalue = new ArrayList<>();
//            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);
                //AX 99PDX Checking
                if (utility.isValid99PDX(proc)) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(proc)) {
                    PCXCounter99++;
                }
                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc);
                if (String.valueOf(ORProcedureResult.isSuccess()).equals("true")) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }

//                DRGWSResult JoinResult = gm.MDCProcedure(datasource, proc, drgResult.getMDC());
//                if (JoinResult.isSuccess()) {
//                    mdcprocedureCounter++;
//                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
//                    DRGWSResult pdcresult = gm.GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
//                    if (String.valueOf(pdcresult.isSuccess()).equals("true")) {
//                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
//                        hierarvalue.add(hiarresult.getHIERAR());
//                        pdclist.add(hiarresult.getPDC());
//                    }
//                }
            }

            String BX25 = "25BX";
            String CX25 = "25CX";
            String DX25 = "25DX";
            int Counter25BXSDx = 0;
            int Counter25CXSDx = 0;
            int Counter25DXSDx = 0;
            int Counter25BXPDx = 0;
            int Counter25DXPDx = 0;
            int Counter25CXPDx = 0;

            for (int a = 0; a < SecondaryList.size(); a++) {
                DRGWSResult ResultBX25SDx = gm.AX(datasource, BX25, SecondaryList.get(a));
                if (String.valueOf(ResultBX25SDx.isSuccess()).equals("true")) {
                    Counter25BXSDx++;
                }
                DRGWSResult ResultCX25SDx = gm.AX(datasource, CX25, SecondaryList.get(a));
                if (String.valueOf(ResultCX25SDx.isSuccess()).equals("true")) {
                    Counter25CXSDx++;
                }
                DRGWSResult ResultDX25SDx = gm.AX(datasource, DX25, SecondaryList.get(a));
                if (String.valueOf(ResultDX25SDx.isSuccess()).equals("true")) {
                    Counter25DXSDx++;
                }
            }
            DRGWSResult ResultBX25PDx = gm.AX(datasource, BX25, grouperparameter.getPdx());
            if (String.valueOf(ResultBX25PDx.isSuccess()).equals("true")) {
                Counter25BXPDx++;
            }
            DRGWSResult ResultCX25PDx = gm.AX(datasource, CX25, grouperparameter.getPdx());
            if (ResultCX25PDx.isSuccess()) {
                Counter25CXPDx++;
            }
            DRGWSResult ResultDX25PDx = gm.AX(datasource, DX25, grouperparameter.getPdx());
            if (ResultDX25PDx.isSuccess()) {
                Counter25DXPDx++;
            }
            if (PDXCounter99 > 0) {//Trache-ostomy
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (ORProcedureCounter > 0) {
                        switch (Collections.max(ORProcedureCounterList)) {
                            case 6://OR Proc Level 6
                                drgResult.setDC("2506");
                                break;
                            case 5://OR Proc Level 5
                                drgResult.setDC("2505");
                                break;
                            case 4://OR Proc Level 4
                                drgResult.setDC("2504");
                                break;
                            case 3://OR Proc Level 3
                                drgResult.setDC("2503");
                                break;
                            case 2://OR Proc Level 2
                                drgResult.setDC("2502");
                                break;
                            case 1://OR Proc Level 1
                                drgResult.setDC("2501");
                                break;
                        }
                    } else if (Counter25BXSDx > 0 || Counter25BXPDx > 0) {//HIV-related CNS Diseases
                        for (int x = 0; x < SecondaryList.size(); x++) {
                            DRGWSResult sdxfinderResult = gm.AX(datasource, BX25, SecondaryList.get(x));
                            if (sdxfinderResult.isSuccess()) {
                                sdxfinder.add(SecondaryList.get(x));
                            }
                        }
                        if (!sdxfinder.isEmpty()) {
                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
                        }
                        drgResult.setDC("2550");
                    } else if (Counter25CXSDx > 0 || Counter25CXPDx > 0) {//HIV-related Malignancy
                        for (int x = 0; x < SecondaryList.size(); x++) {
                            DRGWSResult sdxfinderResult = gm.AX(datasource, CX25, SecondaryList.get(x));
                            if (sdxfinderResult.isSuccess()) {
                                sdxfinder.add(SecondaryList.get(x));
                            }
                        }
                        if (!sdxfinder.isEmpty()) {
                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
                        }
                        drgResult.setDC("2551");
                    } else {
                        
                        if (Counter25DXSDx > 0 || Counter25DXPDx > 0) {//HIV-related Infection
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("2554");
                            } else {
                                drgResult.setDC("2552");
                            }
                        } else {//Other HIV-related Condition
                            drgResult.setDC("2553");
                        }
                        for (int x = 0; x < SecondaryList.size(); x++) {
                            DRGWSResult sdxfinderResult = gm.AX(datasource, DX25, SecondaryList.get(x));
                            if (sdxfinderResult.isSuccess()) {
                                sdxfinder.add(SecondaryList.get(x));
                            }
                        }
                        if (!sdxfinder.isEmpty()) {
                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
                        }
                    }
                } else {
                    if (PCXCounter99 > 0) {//Cont. Mech Vent 96+ hr
                        drgResult.setDC("2508");
                    } else {
                        drgResult.setDC("2509");
                    }
                }
            } else if (ORProcedureCounter > 0) {
                switch (Collections.max(ORProcedureCounterList)) {
                    case 6://OR Proc Level 6
                        drgResult.setDC("2506");
                        break;
                    case 5://OR Proc Level 5
                        drgResult.setDC("2505");
                        break;
                    case 4://OR Proc Level 4
                        drgResult.setDC("2504");
                        break;
                    case 3://OR Proc Level 3
                        drgResult.setDC("2503");
                        break;
                    case 2://OR Proc Level 2
                        drgResult.setDC("2502");
                        break;
                    case 1://OR Proc Level 1
                        drgResult.setDC("2501");
                        break;
                }
            } else if (Counter25BXSDx > 0 || Counter25BXPDx > 0) {//HIV-related CNS Diseases
                for (int x = 0; x < SecondaryList.size(); x++) {
                    DRGWSResult sdxfinderResult = gm.AX(datasource, BX25, SecondaryList.get(x));
                    if (sdxfinderResult.isSuccess()) {
                        sdxfinder.add(SecondaryList.get(x));
                    }
                }
                if (!sdxfinder.isEmpty()) {
                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
                }
                drgResult.setDC("2550");
            } else if (Counter25CXSDx > 0 || Counter25CXPDx > 0) {//HIV-related Malignancy
                for (int x = 0; x < SecondaryList.size(); x++) {
                    DRGWSResult sdxfinderResult = gm.AX(datasource, CX25, SecondaryList.get(x));
                    if (sdxfinderResult.isSuccess()) {
                        sdxfinder.add(SecondaryList.get(x));
                    }
                }
                if (!sdxfinder.isEmpty()) {
                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
                }
                drgResult.setDC("2551");
            } else {
                if (Counter25DXSDx > 0 || Counter25DXPDx > 0) {//HIV-related Infection
                    if (grouperparameter.getDischargeType().equals("4")) {//Transfer
                        drgResult.setDC("2554");
                    } else {//Other
                        drgResult.setDC("2552");
                    }
                } else {//Other HIV-related Condition
                    drgResult.setDC("2553");
                }

                for (int x = 0; x < SecondaryList.size(); x++) {
                    DRGWSResult sdxfinderResult = gm.AX(datasource, DX25, SecondaryList.get(x));
                    if (sdxfinderResult.isSuccess()) {
                        sdxfinder.add(SecondaryList.get(x));
                    }
                }
                if (!sdxfinder.isEmpty()) {
                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
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
            result.setMessage("MDC 25 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC25.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
