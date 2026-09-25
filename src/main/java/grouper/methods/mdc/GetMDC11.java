
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
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int mdcAsInt = Integer.parseInt(drgResult.getMDC());
            String mdcWithoutZeros = String.valueOf(mdcAsInt);
            AX getAx = new AX();
            GetPDC getPdc = new GetPDC();
            PDxMalignancy pdxMalig = new PDxMalignancy();
            MDCProcedureMethod getMdcProced = new MDCProcedureMethod();
            ORProcedure orProc = new ORProcedure();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter11PBX = 0;
            int Counter11PCX = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter11C = 0;
            long age = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
            for (int x = 0; x < ProcedureList.size(); x++) {
                String procS = ProcedureList.get(x).trim();
                CartProc += getAx.AX(datasource, SchemaName, "99PEX", procS).isSuccess() ? 1 : 0;
                CaCRxProc += getAx.AX(datasource, SchemaName, "99PFX", procS).isSuccess() ? 1 : 0;
                PDXCounter99 += getAx.AX(datasource, SchemaName, "99PDX", procS).isSuccess() ? 1 : 0;
                PCXCounter99 += getAx.AX(datasource, SchemaName, "99PCX", procS).isSuccess() ? 1 : 0;
                Counter11PBX += getAx.AX(datasource, SchemaName, "11PBX", procS).isSuccess() ? 1 : 0;
                Counter11PCX += getAx.AX(datasource, SchemaName, "11PCX", procS).isSuccess() ? 1 : 0;
                PBX99Proc += getAx.AX(datasource, SchemaName, "99PBX", procS).isSuccess() ? 1 : 0;
                //THIS AREA IS FOR CHECKING OF OR PROCEDURE
                DRGWSResult ORProcedureResult = orProc.ORProcedure(datasource, SchemaName, procS);
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
                DRGWSResult JoinResult = getMdcProced.MDCProcedure(datasource, SchemaName,
                        procS,
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = getPdc.GetPDC(datasource, SchemaName, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                String sdxCode = SecondaryList.get(a).trim();
                CartSDx += getAx.AX(datasource, SchemaName, "99BX", sdxCode).isSuccess() ? 1 : 0;
                CaCRxSDx += getAx.AX(datasource, SchemaName, "99CX", sdxCode).isSuccess() ? 1 : 0;
            }
            Counter11C += pdxMalig.PDxMalignancy(datasource, SchemaName, grouperparameter.getPdx(), "11C").isSuccess() ? 1 : 0;
            if (PDXCounter99 > 0) {
                long los = utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(), utility.Convert24to12(grouperparameter.getTimeDischarge()));
                if (los < 21) {
                    if (mdcprocedureCounter > 0) {
                        int min = hierarvalue.get(0);
                        for (int i = 0; i < hierarvalue.size(); i++) {
                            if (hierarvalue.get(i) < min) {
                                min = hierarvalue.get(i);
                            }
                        }
                        drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                        drgResult.setDC(this.mdcProcedure(SchemaName, Counter11C, Counter11PBX));
                    } else if (ORProcedureCounter > 0) {
                        drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
                    } else {
                        String dc = this.principalDaignosis(
                                drgResult.getPDC(),
                                CartSDx,
                                CaCRxSDx,
                                CartProc,
                                CaCRxProc,
                                Counter11PCX,
                                PBX99Proc,
                                age,
                                Counter11PBX,
                                grouperparameter.getDischargeType());
                        drgResult.setDC(dc);
                    }
                } else {
                    drgResult.setDC(PCXCounter99 > 0 ? "1113" : "1114");
                }
            } else if (mdcprocedureCounter > 0) {
                int min = hierarvalue.get(0);
                for (int i = 0; i < hierarvalue.size(); i++) {
                    if (hierarvalue.get(i) < min) {
                        min = hierarvalue.get(i);
                    }
                }
                drgResult.setPDC(pdclist.get(hierarvalue.indexOf(min)));
                drgResult.setDC(this.mdcProcedure(SchemaName, Counter11C, Counter11PBX));
            } else if (ORProcedureCounter > 0) {
                drgResult.setDC(this.orProcedure(Collections.max(ORProcedureCounterList)));
            } else {
                String dc = this.principalDaignosis(
                        drgResult.getPDC(),
                        CartSDx,
                        CaCRxSDx,
                        CartProc,
                        CaCRxProc,
                        Counter11PCX,
                        PBX99Proc,
                        age,
                        Counter11PBX,
                        grouperparameter.getDischargeType());
                drgResult.setDC(dc);
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
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
            case "11PA": {//Kidney Transplant
                result = "1101";
                break;
            }
            case "11PL": {//Plasmapheresis
                result = "1115";
                break;
            }
            case "11PC": {//Kidney, Ureter and Major Bladder Procedures
                result = Counter11C > 0 ? "1103" : "1104";
                break;
            }
            case "11PB": {//Operative Insertion of Peritoneal Catheter for Dialysis
                result = "1102";
                break;
            }
            case "11PD": {//Transurethral Prostatectomy
                result = "1105";
                break;
            }
            case "11PH": {//Other Kidney and Urinary Tract OR Procedures
                result = "1109";
                break;
            }
            case "11PF": {//Transurethral Procedures, Except Prostatectomy
                result = "1107";
                break;
            }
            case "11PJ": {//Ureteroscopy
                result = Counter11PBX > 0 ? "1116" : "1110";
                break;
            }
            case "11PK": {//Cystourethroscopy
                result = Counter11PBX > 0 ? "1116" : "1111";
                break;
            }
            case "11PG": {//Urethral Procedures
                result = "1108";
                break;
            }
            case "11PE": {//Minor Bladder Procedures 11PE
                result = "1106";
                break;
            }
        }
        return result;
    }

    private String orProcedure(final Integer ORProcedureCounterList) {
        String dc = "";
        switch (ORProcedureCounterList) {
            case 1: {
                dc = "2601";
                break;
            }
            case 2: {
                dc = "2602";
                break;
            }
            case 3: {
                dc = "2603";
                break;
            }
            case 4: {
                dc = "2604";
                break;
            }
            case 5: {
                dc = "2605";
                break;
            }
            case 6: {
                dc = "2606";
                break;
            }
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
            final Long age,
            final Integer Counter11PBX,
            final String dischargeType) {
        String dc = "";
        switch (pdc) {
            //Radio+Chemotherapy
            case "11C": {//Admit for Renal Dialysis
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
            }
            case "11A": {//Chronic Renal Failure
                dc = age > 17 ? "1150" : "1151";
                break;
            }
            case "11J": {//Acute Renal Failure
                dc = (age > 17) ? ("4".equals(dischargeType) ? "1167" : "1159") : "1160";
                break;
            }
            case "11B": {//Admit for Renal Dialysis
                dc = "1152";
                break;
            }
            case "11D": {//Kidney and Urinary Tract Infection
                dc = "1154";
                break;
            }
            case "11E": {//Urinary Stone
                dc = Counter11PBX > 0 ? "1112" : "1155";
                break;
            }
            case "11F": {//Kidney & Urinary Tract Signs & Symptoms
                dc = "1156";
                break;
            }
            case "11G": {//Urethral Stricture
                dc = "1157";
                break;
            }
            case "11H": {//Other Kidney and Urinary Tract Diagnoses
                dc = "1158";
                break;
            }
            case "11K": {//Major Kidney Dx PDC 11K
                dc = "1166";
            }
            break;

        }
        return dc;

    }

}
