
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.GetPDC;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.methods.validation.ORProcedure;
import grouper.methods.validation.PDxMalignancy;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
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
public class GetMDC11 {

    public GetMDC11() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC11(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        int mdcAsInt = Integer.parseInt(drgResult.getMDC());
        String mdcWithoutZeros = String.valueOf(mdcAsInt);
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
            AX getAx = new AX();
            MDCProcedureMethod getMdcProced = new MDCProcedureMethod();
            for (int x = 0; x < ProcedureList.size(); x++) {
                if (getAx.AX(datasource, SchemaName, "99PEX", ProcedureList.get(x).trim()).isSuccess()) {
                    CartProc++;
                }
                if (getAx.AX(datasource, SchemaName, "99PFX", ProcedureList.get(x).trim()).isSuccess()) {
                    CaCRxProc++;
                }

                //AX 99PDX Checking
                if (getAx.AX(datasource, SchemaName, "99PDX", ProcedureList.get(x).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (getAx.AX(datasource, SchemaName, "99PCX", ProcedureList.get(x).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                //AX 11PBX Checking
                if (getAx.AX(datasource, SchemaName, "11PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter11PBX++;
                }

                if (getAx.AX(datasource, SchemaName, "11PCX", ProcedureList.get(x).trim()).isSuccess()) {
                    Counter11PCX++;
                }

                if (getAx.AX(datasource, SchemaName, "99PBX", ProcedureList.get(x).trim()).isSuccess()) {
                    PBX99Proc++;
                }
                //THIS AREA IS FOR CHECKING OF OR PROCEDURE
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
                DRGWSResult JoinResult = getMdcProced.MDCProcedure(datasource, SchemaName,
                        ProcedureList.get(x).trim(),
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }

            for (int a = 0; a < SecondaryList.size(); a++) {
                if (getAx.AX(datasource, SchemaName, "99BX", SecondaryList.get(a).trim()).isSuccess()) {
                    CartSDx++;
                }
                if (getAx.AX(datasource, SchemaName, "99CX", SecondaryList.get(a).trim()).isSuccess()) {
                    CaCRxSDx++;
                }
            }

            //CHECKING OF MALIGNAT PDC USING PRIMARY CODES 
            int Counter11C = 0;
            DRGWSResult Result11C = new PDxMalignancy().PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "11C");
            if (Result11C.isSuccess()) {
                Counter11C++;
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 07
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
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
                        String dc = this.mdcProcedure(
                                SchemaName,
                                Counter11C,
                                Counter11PBX);
                        drgResult.setDC(dc);
                    } else if (ORProcedureCounter > 0) {
                        String dc = this.orProcedure(Collections.max(ORProcedureCounterList));
                        drgResult.setDC(dc);
                    } else {
                        String dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                Counter11PCX,
                                PBX99Proc,
                                grouperparameter.getBirthDate(),
                                grouperparameter.getAdmissionDate(),
                                Counter11PBX,
                                grouperparameter.getDischargeType());
                        drgResult.setDC(dc);
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
                String dc = this.mdcProcedure(
                        SchemaName,
                        Counter11C,
                        Counter11PBX);
                drgResult.setDC(dc);
            } else if (ORProcedureCounter > 0) {
                String dc = this.orProcedure(Collections.max(ORProcedureCounterList));
                drgResult.setDC(dc);
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter11PCX,
                        PBX99Proc,
                        grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate(),
                        Counter11PBX,
                        grouperparameter.getDischargeType());
                drgResult.setDC(dc);
            }
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 11 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC11.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private String mdcProcedure(
            final String pdc,
            final Integer Counter11C,
            final Integer Counter11PBX) {
        String result = "";
        switch (pdc.toUpperCase()) {
            case "11PA"://Kidney Transplant
                result = "1101";
                break;
            case "11PL"://Plasmapheresis
                result = "1115";
                break;
            case "11PC"://Kidney, Ureter and Major Bladder Procedures
                if (Counter11C > 0) {
                    result = "1103";
                } else {
                    result = "1104";
                }
                break;
            case "11PB"://Operative Insertion of Peritoneal Catheter for Dialysis
                result = "1102";
                break;
            case "11PD"://Transurethral Prostatectomy
                result = "1105";
                break;
            case "11PH"://Other Kidney and Urinary Tract OR Procedures
                result = "1109";
                break;
            case "11PF"://Transurethral Procedures, Except Prostatectomy
                result = "1107";
                break;
            case "11PJ"://Ureteroscopy
                if (Counter11PBX > 0) {
                    result = "1116";
                } else {
                    result = "1110";
                }
                break;
            case "11PK"://Cystourethroscopy
                if (Counter11PBX > 0) {
                    result = "1116";
                } else {
                    result = "1111";
                }
                break;
            case "11PG"://Urethral Procedures
                result = "1108";
                break;
            case "11PE"://Minor Bladder Procedures 11PE
                result = "1106";
                break;
        }
        return result;
    }

    private String orProcedure(final Integer ORProcedureCounterList) {
        String dc = "";
        switch (ORProcedureCounterList) {
            case 1:
                dc = "2601";
                break;
            case 2:
                dc = "2602";
                break;
            case 3:
                dc = "2603";
                break;
            case 4:
                dc = "2604";
                break;
            case 5:
                dc = "2605";
                break;
            case 6:
                dc = "2606";
                break;
        }
        return dc;
    }

    private String principalDaignosis(
            final String pdc,
            final Integer CartSDx,
            final Integer CaCRxSDx,
            final Integer CartProc,
            final Integer CaCRxProc,
            final Integer Counter11PCX,
            final Integer PBX99Proc,
            final String bdate,
            final String admDate,
            final Integer Counter11PBX,
            final String dischargeType) {
        String dc = "";
        switch (pdc) {
            //Radio+Chemotherapy
            case "11C"://Admit for Renal Dialysis
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "1161";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "1162";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "1163";
                } else if (Counter11PCX > 0) { //##Dx Procedure
                    dc = "1164";
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "1165";
                } else {//Malignancy 
                    dc = "1153";
                }
                break;

            case "11A"://Chronic Renal Failure
                if (utility.ComputeYear(bdate,
                        admDate) > 17
                        && utility.ComputeDay(bdate, admDate) > 0) {
                    dc = "1150";
                } else {
                    dc = "1151";
                }
                break;
            case "11J"://Acute Renal Failure
                if (utility.ComputeYear(bdate,
                        admDate) > 17
                        && utility.ComputeDay(bdate, admDate) > 0) {
                    if (dischargeType.equals("4")) {
                        dc = "1167";
                    } else {
                        dc = "1159";
                    }
                } else {
                    dc = "1160";
                }
                break;
            case "11B"://Admit for Renal Dialysis
                dc = "1152";
                break;
            case "11D"://Kidney and Urinary Tract Infection
                dc = "1154";
                break;
            case "11E"://Urinary Stone
                if (Counter11PBX > 0) {
                    dc = "1112";
                } else {
                    dc = "1155";
                }
                break;
            case "11F"://Kidney & Urinary Tract Signs & Symptoms
                dc = "1156";
                break;
            case "11G"://Urethral Stricture
                dc = "1157";
                break;
            case "11H"://Other Kidney and Urinary Tract Diagnoses
                dc = "1158";
                break;
            case "11K"://Major Kidney Dx PDC 11K
                dc = "1166";
                break;

        }
        return dc;

    }

}
