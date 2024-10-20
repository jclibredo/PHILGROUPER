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
 * @author MinoSun
 */
@RequestScoped
public class GetMDC03 {

    public GetMDC03() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC03(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter)   {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            //CHECKING FOR 3PDX USING PROC
            int Counter3PDX = 0;
            int Counter3PBX = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PCX3Proc = 0;
            int PBX99Proc = 0;
            int Counter3PEX = 0;
            int Counter3BX = 0;
            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                String Secon = SecondaryList.get(a);
                DRGWSResult Result99BX = gm.AX(datasource, "99BX", Secon.toUpperCase().trim());
                if (Result99BX.isSuccess()) {
                    CartSDx++;
                }
                DRGWSResult Result99CX = gm.AX(datasource, "99CX", Secon.toUpperCase().trim());
                if (Result99CX.isSuccess()) {
                    CaCRxSDx++;
                }

            }
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            for (int y = 0; y < ProcedureList.size(); y++) {
                String proc = ProcedureList.get(y);
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
                //AX 3PBX Checking
                DRGWSResult Result3PBX = gm.AX(datasource, "3PBX", proc.trim());
                if (Result3PBX.isSuccess()) {
                    Counter3PBX++;
                }
                //AX 3PEX Checking
                DRGWSResult Result3PEX = gm.AX(datasource, "3PEX", proc.trim());
                if (Result3PEX.isSuccess()) {
                    Counter3PEX++;
                }
                //AX 99PEX Checking
                DRGWSResult Result99PEX = gm.AX(datasource, "99PEX", proc.trim());
                if (Result99PEX.isSuccess()) {
                    CartProc++;
                }
                //AX 99PFX Checking
                DRGWSResult Result99PFX = gm.AX(datasource, "99PFX", proc.trim());
                if (Result99PFX.isSuccess()) {
                    CaCRxProc++;
                }

                //AX 3PCX Checking
                DRGWSResult Result3PCX = gm.AX(datasource, "3PCX", proc.trim());
                if (Result3PCX.isSuccess()) {
                    PCX3Proc++;
                }
                //AX 99PBX Checking
                DRGWSResult Result99PBX = gm.AX(datasource, "99PBX", proc.trim());
                if (Result99PBX.isSuccess()) {
                    PBX99Proc++;
                }

                //AX 3PDX Checking
                DRGWSResult Result3PDX = gm.AX(datasource, "3PDX", proc.trim());
                if (Result3PDX.isSuccess()) {
                    Counter3PDX++;
                }

                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc.trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult Result99PDX = gm.AX(datasource, "99PDX", proc.trim());
                if (Result99PDX.isSuccess()) {
                    PDXCounter99++;
                }

                //AX 99PCX Checking
                DRGWSResult Result99PCX = gm.AX(datasource, "99PCX", proc.trim());
                if (Result99PCX.isSuccess()) {
                    PCXCounter99++;
                }
            }

            DRGWSResult Result3BX = gm.AX(datasource, "3BX", grouperparameter.getPdx().toUpperCase().trim());
            if (Result3BX.isSuccess()) {
                Counter3BX++;
            }

//CONDITIONAL STATEMENT STARTS HERE
            if (PDXCounter99 > 0) {
                if (Counter3BX > 0) {
                    if (Counter3PDX > 0) { //Procedures for upper airway obstruction
                        drgResult.setDC("0322");
                    } else {
                        drgResult.setDC("0323");
                    }
                } else {
                    if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                            utility.Convert24to12(grouperparameter.getTimeAdmission()),
                            grouperparameter.getDischargeDate(),
                            utility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
                        if (PCXCounter99 > 0) {
                            drgResult.setDC("0318");
                        } else {
                            drgResult.setDC("0319");
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
                            case "3PT":   //Laryngectomy
                                drgResult.setDC("0320");
                                break;
                            case "3PA":
                                if (Counter3PEX > 0) {
                                    drgResult.setDC("0321");
                                } else {
                                    drgResult.setDC("0301");
                                }
                                break;
                            case "3PR"://Major Sinus Procedures
                                drgResult.setDC("0316");
                                break;
                            case "3PC"://Maxillo Surgery
                                drgResult.setDC("0303");
                                break;
                            case "3PQ"://Mastoidectomy & Inner Ear Procedures
                                drgResult.setDC("0315");
                                break;
                            case "3PP"://Minor Head and Neck Procedures
                                drgResult.setDC("0314");
                                break;
                            case "3PD"://Salivary Procedures
                                drgResult.setDC("0304");
                                break;
                            case "3PL"://Other Head and Neck Procedures
                                drgResult.setDC("0310");
                                break;
                            case "3PE"://Minor Nose & Sinus Procedures
                                drgResult.setDC("0305");
                                break;
                            case "3PJ"://Rhinoplasty
                                drgResult.setDC("0308");
                                break;
                            case "3PN"://Pharyngeal & Laryngeal Procedures
                                drgResult.setDC("0313");
                                break;
                            case "3PB"://Cleft Lip and Palate Repair
                                drgResult.setDC("0302");
                                break;
                            case "3PH"://Miscellaneous Ear, Nose, Mouth and Throat Procedures
                                drgResult.setDC("0307");
                                break;
                            case "3PG"://Mouth Procedures
                                drgResult.setDC("0306");
                                break;
                            case "3PK"://Tonsil and/or Adenoidectomy
                                drgResult.setDC("0309");
                                break;
                            case "3PS"://Tympanoplasty & Other Ear Procedures
                                drgResult.setDC("0317");
                                break;
                            case "3PM"://Myringotomy with Tube Insertion 3PM
                                drgResult.setDC("0311");
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

                    } else {//PRINCIPAL DIAGNOSIS
                        switch (drgResult.getPDC()) {
                            case "3A":
                                //Radio+Chemotherapy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0358");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0359");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("0360");
                                } else if (PCX3Proc > 0) { //##Dx Procedure
                                    drgResult.setDC("0361");
                                    //Radiotherapy
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("0362");
                                } else {
                                    drgResult.setDC("0350");
                                }
                                break;
                            case "3B"://Dysequilibrium
                                drgResult.setDC("0351");
                                break;
                            case "3C"://Epistaxis
                                drgResult.setDC("0352");
                                break;
                            case "3D"://Otitis Media and Upper Respiratory Infection
                                drgResult.setDC("0353");
                                break;
                            case "3E"://Epiglottitis and Cellulitis of Face-Neck
                                drgResult.setDC("0354");
                                break;
                            case "3F"://Nasal Trauma and Deformity
                                drgResult.setDC("0355");
                                break;
                            case "3G"://Other Ear, Noes, Mouth and Throat Diagnoses
                                drgResult.setDC("0356");
                                break;
                            case "3H"://Dental and Oral 3H
                                if (Counter3PBX > 0) {
                                    drgResult.setDC("0312");
                                } else {
                                    drgResult.setDC("0357");
                                }
                                break;

                        }

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
                    case "3PT":   //Laryngectomy
                        drgResult.setDC("0320");
                        break;
                    case "3PA":
                        if (Counter3PEX > 0) {
                            drgResult.setDC("0321");
                        } else {
                            drgResult.setDC("0301");
                        }
                        break;
                    case "3PR"://Major Sinus Procedures
                        drgResult.setDC("0316");
                        break;
                    case "3PC"://Maxillo Surgery
                        drgResult.setDC("0303");
                        break;
                    case "3PQ"://Mastoidectomy & Inner Ear Procedures
                        drgResult.setDC("0315");
                        break;
                    case "3PP"://Minor Head and Neck Procedures
                        drgResult.setDC("0314");
                        break;
                    case "3PD"://Salivary Procedures
                        drgResult.setDC("0304");
                        break;
                    case "3PL"://Other Head and Neck Procedures
                        drgResult.setDC("0310");
                        break;
                    case "3PE"://Minor Nose & Sinus Procedures
                        drgResult.setDC("0305");
                        break;
                    case "3PJ"://Rhinoplasty
                        drgResult.setDC("0308");
                        break;
                    case "3PN"://Pharyngeal & Laryngeal Procedures
                        drgResult.setDC("0313");
                        break;
                    case "3PB"://Cleft Lip and Palate Repair
                        drgResult.setDC("0302");
                        break;
                    case "3PH"://Miscellaneous Ear, Nose, Mouth and Throat Procedures
                        drgResult.setDC("0307");
                        break;
                    case "3PG"://Mouth Procedures
                        drgResult.setDC("0306");
                        break;
                    case "3PK"://Tonsil and/or Adenoidectomy
                        drgResult.setDC("0309");
                        break;
                    case "3PS"://Tympanoplasty & Other Ear Procedures
                        drgResult.setDC("0317");
                        break;
                    case "3PM"://Myringotomy with Tube Insertion 3PM
                        drgResult.setDC("0311");
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

            } else {//PRINCIPAL DIAGNOSIS
                switch (drgResult.getPDC()) {
                    case "3A":
                        //Radio+Chemotherapy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0358");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0359");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("0360");
                        } else if (PCX3Proc > 0) { //##Dx Procedure
                            drgResult.setDC("0361");
                            //Radiotherapy
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("0362");
                        } else {
                            drgResult.setDC("0350");
                        }
                        break;
                    case "3B"://Dysequilibrium
                        drgResult.setDC("0351");
                        break;
                    case "3C"://Epistaxis
                        drgResult.setDC("0352");
                        break;
                    case "3D"://Otitis Media and Upper Respiratory Infection
                        drgResult.setDC("0353");
                        break;
                    case "3E"://Epiglottitis and Cellulitis of Face-Neck
                        drgResult.setDC("0354");
                        break;
                    case "3F"://Nasal Trauma and Deformity
                        drgResult.setDC("0355");
                        break;
                    case "3G"://Other Ear, Noes, Mouth and Throat Diagnoses
                        drgResult.setDC("0356");
                        break;
                    case "3H"://Dental and Oral 3H
                        if (Counter3PBX > 0) {//Extraction and Restoration
                            drgResult.setDC("0312"); //
                        } else {
                            drgResult.setDC("0357");
                        }
                        break;
                }
            }

            // PROCESS PCCL
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
                    drgResult.setDRGName("No DRG Name Found");
                }
            }

            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 03 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC03.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
}
