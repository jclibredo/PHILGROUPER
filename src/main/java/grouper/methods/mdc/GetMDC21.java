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
public class GetMDC21 {

    public GetMDC21() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC21(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
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
                DRGWSResult JoinResult = new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(x).trim(), drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }
            if (PDXCounter99 > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        //Loop through the array  
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            //Compare elements of array with min  
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        switch (pdclist.get(hierarvalue.indexOf(min))) {
                            case "21PF"://Multiple Wound Debridement
                                drgResult.setDC("2107");
                                break;
                            case "21PB"://Skin Graft
                                drgResult.setDC("2102");
                                break;
                            case "21PD"://Other OR Procedures for Injuries
                            case "21PE":
                                drgResult.setDC("2104");
                                break;
                            case "21PC"://Hand procedures
                                drgResult.setDC("2103");
                                break;
                            case "21PA"://Wound Debridement PDC 21PA
                                drgResult.setDC("2101");
                                break;
                        }
                    } else if (ORProcedureCounter > 0) {
                        switch (Collections.max(ORProcedureCounterList)) {
                            case 6://OR Proc Level 6
                                drgResult.setDC("2606");
                                break;
                            case 5://OR Proc Level 5
                                drgResult.setDC("2605");
                                break;
                            case 4://OR Proc Level 4
                                drgResult.setDC("2604");
                                break;
                            case 3://OR Proc Level 3
                                drgResult.setDC("2603");
                                break;
                            case 2://OR Proc Level 2
                                drgResult.setDC("2602");
                                break;
                            case 1://OR Proc Level 1
                                drgResult.setDC("2601");
                                break;
                        }

                    } else {
                        switch (drgResult.getPDC()) {
                            case "21A"://Traumatic Injury
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                                    drgResult.setDC("2150");
                                } else {
                                    drgResult.setDC("2151");
                                }
                                break;
                            case "21B"://Allergic Reaction
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                                    drgResult.setDC("2152");
                                } else {
                                    drgResult.setDC("2153");
                                }
                                break;
                            case "21C"://Drugs
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                                    drgResult.setDC("2154");
                                } else {
                                    drgResult.setDC("2155");
                                }
                                break;
                            case "21D"://Complications of Treatment
                                drgResult.setDC("2156");
                                break;
                            case "21E"://Other Injury, Poisoning and Toxic Effects Diagnoses PDC 21E
                                drgResult.setDC("2157");
                                break;
                        }
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("2105");
                    } else {
                        drgResult.setDC("2106");
                    }
                }

            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                //Loop through the array  
                for (int i = 0; i < hierarvalue.size(); i++) {
                    //Compare elements of array with min  
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                switch (pdclist.get(hierarvalue.indexOf(min))) {
                    case "21PF"://Multiple Wound Debridement
                        drgResult.setDC("2107");
                        break;
                    case "21PB"://Skin Graft
                        drgResult.setDC("2102");
                        break;
                    case "21PD"://Other OR Procedures for Injuries
                    case "21PE":
                        drgResult.setDC("2104");
                        break;
                    case "21PC"://Hand procedures
                        drgResult.setDC("2103");
                        break;
                    case "21PA"://Wound Debridement PDC 21PA
                        drgResult.setDC("2101");
                        break;
                }

            } else if (ORProcedureCounter > 0) {
                switch (Collections.max(ORProcedureCounterList)) {
                    case 6://OR Proc Level 6
                        drgResult.setDC("2606");
                        break;
                    case 5://OR Proc Level 5
                        drgResult.setDC("2605");
                        break;
                    case 4://OR Proc Level 4
                        drgResult.setDC("2604");
                        break;
                    case 3://OR Proc Level 3
                        drgResult.setDC("2603");
                        break;
                    case 2://OR Proc Level 2
                        drgResult.setDC("2602");
                        break;
                    case 1://OR Proc Level 1
                        drgResult.setDC("2601");
                        break;
                }

            } else {
                switch (drgResult.getPDC()) {
                    case "21A"://Traumatic Injury
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                            drgResult.setDC("2150");
                        } else {
                            drgResult.setDC("2151");
                        }
                        break;
                    case "21B"://Allergic Reaction
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                            drgResult.setDC("2152");
                        } else {
                            drgResult.setDC("2153");
                        }
                        break;
                    case "21C"://Drugs
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                            drgResult.setDC("2154");
                        } else {
                            drgResult.setDC("2155");
                        }
                        break;
                    case "21D"://Complications of Treatment
                        drgResult.setDC("2156");
                        break;
                    case "21E"://Other Injury, Poisoning and Toxic Effects Diagnoses PDC 21E
                        drgResult.setDC("2157");
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
            result.setMessage("MDC 21 Done Checking");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC21.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
