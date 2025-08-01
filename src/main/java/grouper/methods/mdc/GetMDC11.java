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
public class GetMDC11 {

    public GetMDC11() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC11(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter11PBX = 0;
            int Counter11PCX = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                if (utility.isValid99PEX(ProcedureList.get(x).trim())) {
                    CartProc++;
                }
                if (utility.isValid99PFX(ProcedureList.get(x).trim())) {
                    CaCRxProc++;
                }
                //AX 99PDX Checking
                if (utility.isValid99PDX(ProcedureList.get(x).trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(x).trim())) {
                    PCXCounter99++;
                }
                //AX 11PBX Checking
                if (utility.isValid11PBX(ProcedureList.get(x).trim())) {
                    Counter11PBX++;
                }
                if (new GrouperMethod().AX(datasource, "11PCX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter11PCX++;
                }
                //THIS AREA IS FOR CHECKING OF OR PROCEDURE
                DRGWSResult ORProcedureResult = new GrouperMethod().ORProcedure(datasource, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
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

            for (int a = 0; a < SecondaryList.size(); a++) {
                if (utility.isValid99BX(SecondaryList.get(a).trim())) {
                    CartSDx++;
                }
                if (utility.isValid99CX(SecondaryList.get(a).trim())) {
                    CaCRxSDx++;
                }
            }

            //CHECKING OF MALIGNAT PDC USING PRIMARY CODES 
            int Counter11C = 0;
            DRGWSResult Result11C = new GrouperMethod().PDxMalignancy(datasource, grouperparameter.getPdx(), "11C");
            if (Result11C.isSuccess()) {
                Counter11C++;
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(), utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
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
                            case "11PA"://Kidney Transplant
                                drgResult.setDC("1101");
                                break;
                            case "11PL"://Plasmapheresis
                                drgResult.setDC("1115");
                                break;
                            case "11PC"://Kidney, Ureter and Major Bladder Procedures
                                if (Counter11C > 0) {
                                    drgResult.setDC("1103");
                                } else {
                                    drgResult.setDC("1104");
                                }
                                break;
                            case "11PB"://Operative Insertion of Peritoneal Catheter for Dialysis
                                drgResult.setDC("1102");
                                break;
                            case "11PD"://Transurethral Prostatectomy
                                drgResult.setDC("1105");
                                break;
                            case "11PH"://Other Kidney and Urinary Tract OR Procedures
                                drgResult.setDC("1109");
                                break;
                            case "11PF"://Transurethral Procedures, Except Prostatectomy
                                drgResult.setDC("1107");
                                break;
                            case "11PJ"://Ureteroscopy
                                if (Counter11PBX > 0) {
                                    drgResult.setDC("1116");
                                } else {
                                    drgResult.setDC("1110");
                                }
                                break;
                            case "11PK"://Cystourethroscopy
                                if (Counter11PBX > 0) {
                                    drgResult.setDC("1116");
                                } else {
                                    drgResult.setDC("1111");
                                }
                                break;
                            case "11PG"://Urethral Procedures
                                drgResult.setDC("1108");
                                break;
                            case "11PE"://Minor Bladder Procedures 11PE
                                drgResult.setDC("1106");
                                break;

                        }

                    } else if (ORProcedureCounter > 0) {
                        switch (Collections.max(ORProcedureCounterList)) {
                            case 1:
                                drgResult.setDC("2601");
                                break;
                            case 2:
                                drgResult.setDC("2602");
                                break;
                            case 3:
                                drgResult.setDC("2603");
                                break;
                            case 4:
                                drgResult.setDC("2604");
                                break;
                            case 5:
                                drgResult.setDC("2605");
                                break;
                            case 6:
                                drgResult.setDC("2606");
                                break;
                        }

                    } else {

                        switch (drgResult.getPDC()) {
                            //Radio+Chemotherapy
                            case "11C"://Admit for Renal Dialysis
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("1161");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("1162");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("1163");
                                } else if (Counter11PCX > 0) { //##Dx Procedure
                                    drgResult.setDC("1164");
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("1165");
                                } else {//Malignancy 
                                    drgResult.setDC("1153");
                                }
                                break;

                            case "11A"://Chronic Renal Failure
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) >= 17
                                        && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                                    drgResult.setDC("1150");
                                } else {
                                    drgResult.setDC("1151");
                                }
                                break;
                            case "11J"://Acute Renal Failure
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) >= 17
                                        && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                                    if (grouperparameter.getDischargeType().equals("4")) {
                                        drgResult.setDC("1167");
                                    } else {
                                        drgResult.setDC("1159");
                                    }
                                } else {
                                    drgResult.setDC("1160");
                                }
                                break;
                            case "11B"://Admit for Renal Dialysis
                                drgResult.setDC("1152");
                                break;
                            case "11D"://Kidney and Urinary Tract Infection
                                drgResult.setDC("1154");
                                break;
                            case "11E"://Urinary Stone
                                if (Counter11PBX > 0) {
                                    drgResult.setDC("1112");
                                } else {
                                    drgResult.setDC("1155");
                                }
                                break;
                            case "11F"://Kidney & Urinary Tract Signs & Symptoms
                                drgResult.setDC("1156");
                                break;
                            case "11G"://Urethral Stricture
                                drgResult.setDC("1157");
                                break;
                            case "11H"://Other Kidney and Urinary Tract Diagnoses
                                drgResult.setDC("1158");
                                break;
                            case "11K"://Major Kidney Dx PDC 11K
                                drgResult.setDC("1166");
                                break;

                        }

                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("1113");
                    } else {
                        drgResult.setDC("1114");
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
                    case "11PA"://Kidney Transplant
                        drgResult.setDC("1101");
                        break;
                    case "11PL"://Plasmapheresis
                        drgResult.setDC("1115");
                        break;
                    case "11PC"://Kidney, Ureter and Major Bladder Procedures
                        if (Counter11C > 0) {
                            drgResult.setDC("1103");
                        } else {
                            drgResult.setDC("1104");
                        }
                        break;
                    case "11PB"://Operative Insertion of Peritoneal Catheter for Dialysis
                        drgResult.setDC("1102");
                        break;
                    case "11PD"://Transurethral Prostatectomy
                        drgResult.setDC("1105");
                        break;
                    case "11PH"://Other Kidney and Urinary Tract OR Procedures
                        drgResult.setDC("1109");
                        break;
                    case "11PF"://Transurethral Procedures, Except Prostatectomy
                        drgResult.setDC("1107");
                        break;
                    case "11PJ"://Ureteroscopy
                        if (Counter11PBX > 0) {
                            drgResult.setDC("1116");
                        } else {
                            drgResult.setDC("1110");
                        }
                        break;
                    case "11PK"://Cystourethroscopy
                        if (Counter11PBX > 0) {
                            drgResult.setDC("1116");
                        } else {
                            drgResult.setDC("1111");
                        }
                        break;
                    case "11PG"://Urethral Procedures
                        drgResult.setDC("1108");
                        break;
                    case "11PE"://Minor Bladder Procedures 11PE
                        drgResult.setDC("1106");
                        break;

                }

            } else if (ORProcedureCounter > 0) {
                switch (Collections.max(ORProcedureCounterList)) {
                    case 1:
                        drgResult.setDC("2601");
                        break;
                    case 2:
                        drgResult.setDC("2602");
                        break;
                    case 3:
                        drgResult.setDC("2603");
                        break;
                    case 4:
                        drgResult.setDC("2604");
                        break;
                    case 5:
                        drgResult.setDC("2605");
                        break;
                    case 6:
                        drgResult.setDC("2606");
                        break;
                }

            } else {

                switch (drgResult.getPDC()) {
                    //Radio+Chemotherapy
                    case "11C"://Admit for Renal Dialysis
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("1161");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("1162");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("1163");
                        } else if (Counter11PCX > 0) { //##Dx Procedure
                            drgResult.setDC("1164");
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("1165");
                        } else {//Malignancy 
                            drgResult.setDC("1153");
                        }
                        break;
                    case "11A"://Chronic Renal Failure
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 17
                                && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                            drgResult.setDC("1150");
                        } else {
                            drgResult.setDC("1151");
                        }
                        break;
                    case "11J"://Acute Renal Failure
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 17
                                && utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("1167");
                            } else {
                                drgResult.setDC("1159");
                            }
                        } else {
                            drgResult.setDC("1160");
                        }
                        break;
                    case "11B"://Admit for Renal Dialysis
                        drgResult.setDC("1152");
                        break;
                    case "11D"://Kidney and Urinary Tract Infection
                        drgResult.setDC("1154");
                        break;
                    case "11E"://Urinary Stone
                        if (Counter11PBX > 0) {
                            drgResult.setDC("1112");
                        } else {
                            drgResult.setDC("1155");
                        }
                        break;
                    case "11F"://Kidney & Urinary Tract Signs & Symptoms
                        drgResult.setDC("1156");
                        break;
                    case "11G"://Urethral Stricture
                        drgResult.setDC("1157");
                        break;
                    case "11H"://Other Kidney and Urinary Tract Diagnoses
                        drgResult.setDC("1158");
                        break;
                    case "11K"://Major Kidney Dx PDC 11K
                        drgResult.setDC("1166");
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
            result.setMessage("MDC 11 Done Checking");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC11.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
