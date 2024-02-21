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
import grouper.utility.DRGUtility;
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
public class GetMDC01 {

    public GetMDC01() {
    }
    private final Utility utility = new Utility();
    private final DRGUtility drgutility = new DRGUtility();
    private final GrouperMethod gm = new GrouperMethod();

    public DRGWSResult GetMDC01(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) throws IOException {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        //   String MDC = drgResult.getMDC();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));

        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));

        // THIS AREA IS FOR CHECKING OF TRACHEOSTOMY AND CONT. MECH VENT
        int PDXCounter99 = 0;
        int procCounter = 0;
        int PCXCounter99 = 0;

        for (int x = 0; x < ProcedureList.size(); x++) {
            String proc = ProcedureList.get(x);
            //AX 99PDX Checking
            if (drgutility.isValid99PDX(proc)) {
                PDXCounter99++;
            }
            //AX 99PCX Checking
            if (drgutility.isValid99PCX(proc)) {
                PCXCounter99++;
            }
        }

        
        
        
        //THIS AREA IS FOR CHECKING FOR TRAUMA AND CART AND CARCX
        int CartSDx = 0;
        int CaCRxSDx = 0;
        int CartProc = 0;
        int CaCRxProc = 0;
        int PBX12Proc = 0;
        int PBX99Proc = 0;
        int PBX1Proc = 0;
        //Checking SDx RadioTherapy and Chemotherapy
        for (int a = 0; a < SecondaryList.size(); a++) {
            String Secon = SecondaryList.get(a);
            if (drgutility.isValid99BX(Secon)) {
                CartSDx++;
            }
            if (drgutility.isValid99CX(Secon)) {
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
        String Pdcs = "1PJ";
        //Checking Procedure RadioTherapy and Chemotherapy
        int Counter1PBX = 0;
        for (int a = 0; a < ProcedureList.size(); a++) {
            String Proce = ProcedureList.get(a);
            DRGWSResult EndoRes = gm.Endovasc(datasource, Proce, Pdcs, drgResult.getMDC());
            if (EndoRes.isSuccess()) {
                EndoCounter++;
            }
            if (drgutility.isValid99PEX(Proce)) {
                CartProc++;
            }
            if (drgutility.isValid99PFX(Proce)) {
                CaCRxProc++;
            }
            if (drgutility.isValid12PBX(Proce)) {
                PBX12Proc++;
            }
            if (drgutility.isValid99PBX(Proce)) {
                PBX99Proc++;
            }
            DRGWSResult JoinResult = gm.MDCProcedure(datasource, Proce, drgResult.getMDC());
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
            DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, Proce);
            if (String.valueOf(ORProcedureResult.isSuccess()).equals("true")) {
                ORProcedureCounter++;
                ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
            }

            String Codes1PBX = "1PBX";
            DRGWSResult Codes1PBXResult = gm.AX(datasource, Codes1PBX, Proce);
            if (Codes1PBXResult.isSuccess()) {
                Counter1PBX++;
            }

        }

        // THIS AREA CHECK 1BX AND ICD10 CODES
        String Codes1BX = "1BX";
        DRGWSResult Codes1BXResult = gm.AX(datasource, Codes1BX, grouperparameter.getPdx());

        // THIS AREA CHECK 1BX AND ICD10 CODES
        String Codes1CX = "1CX";
        DRGWSResult Codes1CXResult = gm.AX(datasource, Codes1CX, grouperparameter.getPdx());
        // THIS AREA WILL START STATEMENT TO FIND DC FOR MDC 1
        try {
            if (PDXCounter99 > 0) { //Check Procedure if Tracheostomy
                if (drgutility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        drgutility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        drgutility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
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
                            if (Codes1BXResult.isSuccess()) {
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
                                if (Codes1CXResult.isSuccess()) {
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
                            if (Codes1BXResult.isSuccess()) {
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
                        if (Codes1BXResult.isSuccess()) {
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
                            if (Codes1CXResult.isSuccess()) {
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
                        if (Codes1BXResult.isSuccess()) {
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
                if (drgutility.isValidDCList(drgResult.getDC())) {
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
            result.setMessage("MDC 1 DC Result Has Found");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC01.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;

    }

}