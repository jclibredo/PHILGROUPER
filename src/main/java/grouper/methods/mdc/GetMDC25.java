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
 * @author DRG_SHADOWBILLING
 */
@RequestScoped
public class GetMDC25 {

    public GetMDC25() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC25(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
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
//            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
//            ArrayList<Integer> hierarvalue = new ArrayList<>();
//            ArrayList<String> pdclist = new ArrayList<>();
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

//                DRGWSResult JoinResult =  new GrouperMethod().MDCProcedure(datasource, proc, drgResult.getMDC());
//                if (JoinResult.isSuccess()) {
//                    mdcprocedureCounter++;
//                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
//                    DRGWSResult pdcresult =  new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
//                    if (String.valueOf(pdcresult.isSuccess()).equals("true")) {
//                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
//                        hierarvalue.add(hiarresult.getHIERAR());
//                        pdclist.add(hiarresult.getPDC());
//                    }
//                }
            }

//            String BX25 = "25BX";
//            String CX25 = "25CX";
//            String DX25 = "25DX";
            int Counter25BXSDx = 0;
            int Counter25CXSDx = 0;
            int Counter25DXSDx = 0;
            int Counter25BXPDx = 0;
            int Counter25DXPDx = 0;
            int Counter25CXPDx = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (new GrouperMethod().AX(datasource, "25BX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter25BXSDx++;
                }
                if (new GrouperMethod().AX(datasource, "25CX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter25CXSDx++;
                }
                if (new GrouperMethod().AX(datasource, "25DX", SecondaryList.get(a).trim()).isSuccess()) {
                    Counter25DXSDx++;
                }
            }
            if (new GrouperMethod().AX(datasource, "25BX", grouperparameter.getPdx().trim()).isSuccess()) {
                Counter25BXPDx++;
            }
            if (new GrouperMethod().AX(datasource, "25CX", grouperparameter.getPdx().trim()).isSuccess()) {
                Counter25CXPDx++;
            }
            if (new GrouperMethod().AX(datasource, "25DX", grouperparameter.getPdx()).isSuccess()) {
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
                            if (new GrouperMethod().AX(datasource, "25BX", SecondaryList.get(x).trim()).isSuccess()) {
                                sdxfinder.add(SecondaryList.get(x));
                            }
                        }
                        if (!sdxfinder.isEmpty()) {
                            drgResult.setSDXFINDER(String.join(",", sdxfinder));
                        }
                        drgResult.setDC("2550");
                    } else if (Counter25CXSDx > 0 || Counter25CXPDx > 0) {//HIV-related Malignancy
                        for (int x = 0; x < SecondaryList.size(); x++) {
                            if (new GrouperMethod().AX(datasource, "25CX", SecondaryList.get(x).trim()).isSuccess()) {
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
                            if (new GrouperMethod().AX(datasource, "25DX", SecondaryList.get(x).trim()).isSuccess()) {
                                sdxfinder.add(SecondaryList.get(x).trim());
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
                    if (new GrouperMethod().AX(datasource, "25BX", SecondaryList.get(x).trim()).isSuccess()) {
                        sdxfinder.add(SecondaryList.get(x).trim());
                    }
                }
                if (!sdxfinder.isEmpty()) {
                    drgResult.setSDXFINDER(String.join(",", sdxfinder));
                }
                drgResult.setDC("2550");
            } else if (Counter25CXSDx > 0 || Counter25CXPDx > 0) {//HIV-related Malignancy
                for (int x = 0; x < SecondaryList.size(); x++) {
                    if (new GrouperMethod().AX(datasource, "25CX", SecondaryList.get(x).trim()).isSuccess()) {
                        sdxfinder.add(SecondaryList.get(x).trim());
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
                    if (new GrouperMethod().AX(datasource, "25DX", SecondaryList.get(x).trim()).isSuccess()) {
                        sdxfinder.add(SecondaryList.get(x).trim());
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
            result.setMessage("MDC 25 Done Checking");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC25.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
