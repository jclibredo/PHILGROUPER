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
public class GetMDC01 {

    public GetMDC01() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC01(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
//            GrouperMethod gm = new GrouperMethod();
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));

            // THIS AREA IS FOR CHECKING OF TRACHEOSTOMY AND CONT. MECH VENT
            int PDXCounter99 = 0;
//            int procCounter = 0;
            int PCXCounter99 = 0;
            //THIS AREA IS FOR CHECKING FOR TRAUMA AND CART AND CARCX
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (new GrouperMethod().AX(datasource, "99BX", SecondaryList.get(a).trim()).isSuccess()) {
                    CartSDx++;
                }
                if (new GrouperMethod().AX(datasource, "99CX", SecondaryList.get(a).trim()).isSuccess()) {
                    CaCRxSDx++;
                }
            }
            // CHECLING FOR ENDOVASC 
            int EndoCounter = 0;
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
//            String Pdcs = "1PJ";
            //Checking Procedure RadioTherapy and Chemotherapy
            int Counter1PBX = 0;
            for (int a = 0; a < ProcedureList.size(); a++) {
                //AX 99PDX Checking
                if (new GrouperMethod().AX(datasource, "99PDX", ProcedureList.get(a).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (new GrouperMethod().AX(datasource, "99PCX", ProcedureList.get(a).trim()).isSuccess()) {
                    PCXCounter99++;
                }

                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(a).trim(), "1PJ", drgResult.getMDC()).isSuccess()) {
                    EndoCounter++;
                }
                if (new GrouperMethod().AX(datasource, "99PEX", ProcedureList.get(a).trim()).isSuccess()) {
                    CartProc++;
                }
                if (new GrouperMethod().AX(datasource, "99PFX", ProcedureList.get(a).trim()).isSuccess()) {
                    CaCRxProc++;
                }
                if (new GrouperMethod().AX(datasource, "99PBX", ProcedureList.get(a).trim()).isSuccess()) {
                    PBX99Proc++;
                }
//                DRGWSResult JoinResult = new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(a).trim(), drgResult.getMDC());
                if (new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(a).trim(), drgResult.getMDC()).isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(a).trim(), drgResult.getMDC()).getResult(), MDCProcedure.class);
//                    DRGWSResult pdcresult = new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC()).isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC()).getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                if (new GrouperMethod().ORProcedure(datasource, ProcedureList.get(a).trim()).isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(new GrouperMethod().ORProcedure(datasource, ProcedureList.get(a).trim()).getResult()));
                }
                if (new GrouperMethod().AX(datasource, "1PBX", ProcedureList.get(a).trim()).isSuccess()) {
                    Counter1PBX++;
                }
            }
            // THIS AREA CHECK 1BX AND ICD10 CODES
//            String Codes1BX = "1BX";
//            DRGWSResult Codes1BXResult = new GrouperMethod().AX(datasource, "1BX", grouperparameter.getPdx());

            // THIS AREA CHECK 1BX AND ICD10 CODES
//            String Codes1CX = "1CX";
//            DRGWSResult Codes1CXResult = new GrouperMethod().AX(datasource, "1CX", grouperparameter.getPdx());
            // THIS AREA WILL START STATEMENT TO FIND DC FOR MDC 1
            if (PDXCounter99 > 0) { //Check Procedure if Tracheostomy
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0115");
                    } else {
                        drgResult.setDC("0116");
                    }
                } else if (mdcprocedureCounter > 0) { //MDC Procedure
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
                        case "1PK":   // drgResult.setDC("0116");
                            if (new GrouperMethod().AX(datasource, "1BX", grouperparameter.getPdx()).isSuccess()) {
                                drgResult.setDC("0112");
                            } else {
                                drgResult.setDC("0113");
                            }
                            break;
                        case "1PL"://Plasmapheresis 
                            drgResult.setDC("0117");
                            break;
                        case "1PH"://Intacranial Vasc
                            if (EndoCounter > 0) {  // RECODE THIS AREA TO DOUBLE CHECK
                                drgResult.setDC("0110");
                            } else {
                                if (new GrouperMethod().AX(datasource, "1CX", grouperparameter.getPdx()).isSuccess()) {
                                    drgResult.setDC("0108");
                                } else {
                                    drgResult.setDC("0109");
                                }
                            }
                            break;
                        case "1PC"://SPINAL PROCEDURES
                            drgResult.setDC("0103");
                            break;
                        case "1PB":  //Craniotomy
                            if (new GrouperMethod().AX(datasource, "1BX", grouperparameter.getPdx()).isSuccess()) {
                                drgResult.setDC("0101");
                            } else {
                                drgResult.setDC("0102");
                            }
                            break;
                        case "1PJ": //Endovasc Procedures
                            drgResult.setDC("0114");
                            break;
                        case "1PD": //Extracranial Vascular Procedures
                            drgResult.setDC("0105");
                            break;
                        case "1PA": //Ventricular Shunt Revision
                            drgResult.setDC("0104");
                            break;
                        case "1PF":
                        case "1PG":  //Peripheral & Cranial Nerve & Other Nervous System Procedures
                            drgResult.setDC("0106");
                            break;
                        case "1PE": //Carpal Tunnel Release
                            drgResult.setDC("0107");
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
                        case "1A"://Spinal Disorders and Injuries
                            drgResult.setDC("0150");
                            break;
                        case "1B"://Cerebral Palsy
                            drgResult.setDC("0151");
                            break;
                        case "1C":
                            if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {//Radio+Chemotherapy
                                drgResult.setDC("0170");
                                //Chemotherapy
                            } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                drgResult.setDC("0171");
                                //Radiotherapy
                            } else if (CartSDx > 0 && CartProc > 0) {
                                drgResult.setDC("0172");
                            } else if (Counter1PBX > 0) { //##Dx Procedure
                                drgResult.setDC("0173");
                                //Radiotherapy
                            } else if (PBX99Proc > 0) {//Blood Transfusion
                                drgResult.setDC("0174");
                            } else {
                                drgResult.setDC("0152");
                            }
                            break;
                        case "1D"://Degenerative Disorders
                            drgResult.setDC("0153");
                            break;
                        case "1E"://Multiple Sclerosis and Cerebellar Ataxia
                            drgResult.setDC("0154");
                            break;
                        case "1F"://Specific Cerebrovascular Disorders Except TIA
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("0175");//Transfer
                            } else {
                                drgResult.setDC("0155");//Others
                            }
                            break;
                        case "1G"://Transient Ischemic Attack and Precerebral Occlusions
                            drgResult.setDC("0156");
                            break;
                        case "1H"://Nonspecific Cerebrovascular Diseases
                            drgResult.setDC("0157");
                            break;
                        case "1J"://Cranial and Peripheral Nerve Disorders
                            drgResult.setDC("0158");
                            break;
                        case "1K"://Infections Except Viral Meningitis 
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("0176");
                            } else {
                                drgResult.setDC("0159");
                            }
                            break;
                        case "1L"://Viral Meningitis
                            drgResult.setDC("0160");
                            break;
                        case "1M"://Nontraumatic Stupor and Coma
                            drgResult.setDC("0161");
                            break;
                        case "1N"://Febrile Convulsions
                            drgResult.setDC("0162");
                            break;
                        case "1P"://Seizure Disorders
                            drgResult.setDC("0163");
                            break;
                        case "1Q"://Headaches
                            drgResult.setDC("0164");
                            break;
                        case "1R"://Intracranial Injury
                            drgResult.setDC("0165");
                            break;
                        case "1S"://Skull Fractures
                            drgResult.setDC("0166");
                            break;
                        case "1T"://Other Head Injury
                            drgResult.setDC("0167");
                            break;
                        case "1U"://Other Disorders of Nervous System
                            drgResult.setDC("0168");
                            break;
                        case "1V"://Guillain-Barre Syndrome 1V
                            drgResult.setDC("0169");
                            break;
                    }

                }

            } else if (mdcprocedureCounter > 0) { //MDC Procedure
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
                    case "1PK":   // drgResult.setDC("0116");
                        if (new GrouperMethod().AX(datasource, "1BX", grouperparameter.getPdx()).isSuccess()) {
                            drgResult.setDC("0112");
                        } else {
                            drgResult.setDC("0113");
                        }
                        break;
                    case "1PL"://Plasmapheresis 
                        drgResult.setDC("0117");
                        break;
                    case "1PH"://Intacranial Vasc
                        if (EndoCounter > 0) {  // RECODE THIS AREA TO DOUBLE CHECK
                            drgResult.setDC("0110");
                        } else {
                            if (new GrouperMethod().AX(datasource, "1CX", grouperparameter.getPdx()).isSuccess()) {
                                drgResult.setDC("0108");
                            } else {
                                drgResult.setDC("0109");
                            }
                        }
                        break;
                    case "1PC"://SPINAL PROCEDURES
                        drgResult.setDC("0103");
                        break;
                    case "1PB":  //Craniotomy
                        if (new GrouperMethod().AX(datasource, "1BX", grouperparameter.getPdx()).isSuccess()) {
                            drgResult.setDC("0101");
                        } else {
                            drgResult.setDC("0102");
                        }
                        break;
                    case "1PJ": //Endovasc Procedures
                        drgResult.setDC("0114");
                        break;
                    case "1PD": //Extracranial Vascular Procedures
                        drgResult.setDC("0105");
                        break;
                    case "1PA": //Ventricular Shunt Revision
                        drgResult.setDC("0104");
                        break;
                    case "1PF":
                    case "1PG":  //Peripheral & Cranial Nerve & Other Nervous System Procedures
                        drgResult.setDC("0106");
                        break;
                    case "1PE": //Carpal Tunnel Release
                        drgResult.setDC("0107");
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
                    case "1A":
                        drgResult.setDC("0150");
                        break;
                    case "1B":
                        drgResult.setDC("0151");
                        break;
                    case "1C":
                        //Radio+Chemotherapy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0170");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0171");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("0172");
                        } else if (Counter1PBX > 0) { //##Dx Procedure
                            drgResult.setDC("0173");
                            //Radiotherapy
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("0174");
                        } else {
                            drgResult.setDC("0152");
                        }
                        break;
                    case "1D"://Degenerative Disorders
                        drgResult.setDC("0153");
                        break;
                    case "1E"://Multiple Sclerosis and Cerebellar Ataxia
                        drgResult.setDC("0154");
                        break;
                    case "1F"://Specific Cerebrovascular Disorders Except TIA
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("0175");//Transfer
                        } else {
                            drgResult.setDC("0155");//Others
                        }
                        break;
                    case "1G"://Transient Ischemic Attack and Precerebral Occlusions
                        drgResult.setDC("0156");
                        break;
                    case "1H"://Nonspecific Cerebrovascular Diseases
                        drgResult.setDC("0157");
                        break;
                    case "1J"://Cranial and Peripheral Nerve Disorders
                        drgResult.setDC("0158");
                        break;
                    case "1K"://Infections Except Viral Meningitis
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("0176");
                        } else {
                            drgResult.setDC("0159");
                        }
                        break;
                    case "1L"://Viral Meningitis
                        drgResult.setDC("0160");
                        break;
                    case "1M"://Nontraumatic Stupor and Coma
                        drgResult.setDC("0161");
                        break;
                    case "1N"://Febrile Convulsions
                        drgResult.setDC("0162");
                        break;
                    case "1P"://Seizure Disorders
                        drgResult.setDC("0163");
                        break;
                    case "1Q"://Headaches
                        drgResult.setDC("0164");
                        break;
                    case "1R"://Intracranial Injury
                        drgResult.setDC("0165");
                        break;
                    case "1S"://Skull Fractures
                        drgResult.setDC("0166");
                        break;
                    case "1T"://Other Head Injury
                        drgResult.setDC("0167");
                        break;
                    case "1U"://Other Disorders of Nervous System
                        drgResult.setDC("0168");
                        break;
                    case "1V"://Guillain-Barre Syndrome 1V
                        drgResult.setDC("0169");
                        break;
                }
            }

            //===================================================PROCESS PCCL
            if (drgResult.getDRG() == null) {
                //-------------------------------------------------------------------------------------
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                } else {
                    //----------------------------------------------------------------------
                    //  String sdxfinalList =  new GrouperMethod().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
//                    String sdxfinalList = new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
//                    DRGWSResult getpcclvalue = new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC()));
                    if (new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC())).isSuccess()) {
//                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
//                        String drgValue = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class).getDRG();
                        DRGWSResult drgname = new GrouperMethod().DRG(datasource, drgResult.getDC(), utility.objectMapper().readValue(new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC())).getResult(), DRGOutput.class).getDRG());
                        //-----------------------------------------------------------------------
                        if (drgname.isSuccess()) {
                            drgResult.setDRG(utility.objectMapper().readValue(new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC())).getResult(), DRGOutput.class).getDRG());
                            drgResult.setDRGName(drgname.getMessage());

                        } else {
                            DRGWSResult drgvalues = new GrouperMethod().ValidatePCCL(datasource, drgResult.getDC(), utility.objectMapper().readValue(new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC())).getResult(), DRGOutput.class).getDRG());
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                DRGWSResult drgnames = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                drgResult.setDRGName(drgnames.getMessage());
                            } else {
                                drgResult.setDRG(utility.objectMapper().readValue(new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC())).getResult(), DRGOutput.class).getDRG());
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
//                DRGWSResult drgname = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                if (new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
                }
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 1 DC Result Has Found");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC01.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
