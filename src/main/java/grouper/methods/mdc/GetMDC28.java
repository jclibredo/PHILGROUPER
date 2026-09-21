/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.DRG;
import grouper.methods.validation.GetPDC;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.structures.MDCCodeOptimize;
import grouper.structures.MDCProcedure;
import grouper.structures.PDC;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class GetMDC28 {

    public GetMDC28() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC28(
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
            AX checkAX = new AX();
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int finalage = 0;
            if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 0) {
                finalage = utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) * 365;
            } else {
                finalage = utility.ComputeDay(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
            }
            int mdcprocedureCounter = 0;
            int CaCRxSDx = 0;
            int CaCRxProc = 0;
            int CaCRx = 0;
            int Counter28EX = 0;
            int Counter28BX = 0;
            int Counter28CX = 0;
            for (int x = 0; x < ProcedureList.size(); x++) {
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, SchemaName,
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
                if (checkAX.AX(datasource, SchemaName, "99PFX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
                    CaCRxProc++;
                }
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (checkAX.AX(datasource, SchemaName, "99CX", SecondaryList.get(a).trim()).isSuccess()) {//Dx Procedure
                    CaCRxSDx++;
                }
            }
            if (CaCRxSDx > 0 && CaCRxProc > 0) {
                CaCRx++;
            }
            if (checkAX.AX(datasource, SchemaName, "28EX", grouperparameter.getPdx().toUpperCase().trim()).isSuccess()) {
                Counter28EX++;
            }
            if (checkAX.AX(datasource, SchemaName, "28BX", grouperparameter.getPdx().toUpperCase().trim()).isSuccess()) {
                Counter28BX++;
            }
            if (checkAX.AX(datasource, SchemaName, "28CX", grouperparameter.getPdx().toUpperCase().trim()).isSuccess()) {
                Counter28CX++;
            }
            // PROCESS BEGINS HERE
            switch (grouperparameter.getDischargeType()) {
                case "1": {//Approve
                    if (finalage < 28) {
                        drgResult.setDRG("28509");
                        drgResult.setDC("2850");
                    } else {
                        if (Counter28EX > 0 && CaCRx > 0) {
                            drgResult.setDRG("28689");
                            drgResult.setDC("2868");
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
                            MDCCodeOptimize getDcResult = this.MDCProcedures(drgResult.getPDC());
                            drgResult.setDRG(getDcResult.getDRG());
                            drgResult.setDC(getDcResult.getDC());
                        } else {
                            drgResult.setDRG("28699");
                            drgResult.setDC("2869");
                        }
                    }
                    break;
                }
                case "2"://Against Advice Escape,Other
                case "3":
                case "5": {
                    if (finalage < 28) {
                        drgResult.setDRG("28519");
                        drgResult.setDC("2851");
                    } else if (finalage >= 28
                            && utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) < 11) {
                        drgResult.setDRG("28529");
                        drgResult.setDC("2852");
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 12
                            && utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) <= 65) {
                        drgResult.setDRG("28539");
                        drgResult.setDC("2853");
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 65) {
                        drgResult.setDRG("28549");
                        drgResult.setDC("2854");
                    }
                    break;
                }
                case "4": {
                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) < 28) {
                        drgResult.setDRG("28559");
                        drgResult.setDC("2855");
                    } else if (finalage >= 28
                            && utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) <= 11) {
                        drgResult.setDRG("28569");
                        drgResult.setDC("2856");
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 12
                            && utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) <= 65) {
                        if (Counter28BX > 0) {//CVA AX 28BX
                            drgResult.setDRG("28579");
                            drgResult.setDC("2857");
                        } else if (Counter28CX > 0) {//AMI AX 28CX
                            drgResult.setDRG("28589");
                            drgResult.setDC("2858");
                        } else {//Others
                            drgResult.setDRG("28599");
                            drgResult.setDC("2859");
                        }
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 65) {
                        drgResult.setDRG("28609");
                        drgResult.setDC("2860");
                    }
                    break;
                }
                case "8":
                case "9": {
                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) < 28) {
                        drgResult.setDRG("28619");
                        drgResult.setDC("2861");
                    } else if (finalage >= 28
                            && utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) <= 11) {
                        if (checkAX.AX(datasource, SchemaName, "28PBX", grouperparameter.getPdx().trim()).isSuccess()) {
                            drgResult.setDRG("28019");
                            drgResult.setDC("2801");
                        } else {
                            drgResult.setDRG("28629");
                            drgResult.setDC("2862");
                        }
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 12
                            && utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) <= 65) {
                        if (checkAX.AX(datasource, SchemaName, "28PBX", grouperparameter.getPdx().trim()).isSuccess()) {
                            drgResult.setDRG("28029");
                            drgResult.setDC("2802");
                        } else {
                            drgResult.setDRG("28639");
                            drgResult.setDC("2863");
                        }
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 65) {
                        if (Counter28BX > 0) {
                            drgResult.setDRG("28649");
                            drgResult.setDC("2864");
                        } else if (Counter28CX > 0) {
                            drgResult.setDRG("28659");
                            drgResult.setDC("2865");
                        } else if (checkAX.AX(datasource, SchemaName, "28DX", grouperparameter.getPdx().trim()).isSuccess()) {
                            drgResult.setDRG("28669");
                            drgResult.setDC("2866");
                        } else {
                            if (checkAX.AX(datasource, SchemaName, "28PBX", grouperparameter.getPdx().trim()).isSuccess()) {
                                drgResult.setDRG("28039");
                                drgResult.setDC("2803");
                            } else {
                                drgResult.setDRG("28679");
                                drgResult.setDC("2867");
                            }
                        }
                    }
                    break;
                }
            }
            DRG checkDRG = new DRG();
            if (drgResult.getDRG() != null) {
                if (checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(checkDRG.DRG(datasource, SchemaName, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("DRG code grouper provide not exist in the library");
                }
            }
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 28 Done Checking");
            result.setSuccess(true);
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC28.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    private MDCCodeOptimize MDCProcedures(final String pdc) {
        MDCCodeOptimize result = utility.MDCCodeOptimize();
        result.setDC("");
        result.setDRG("");
        switch (pdc.toUpperCase()) {
            case "28PB": {//KUB ESWL
                result.setDRG("28049");
                result.setDC("2804");
                break;
            }
            case "28PC": {//Cadiac Cath & CAG
                result.setDRG("28059");
                result.setDC("2805");
                break;
            }
            case "28PD": {//Cataract Proc
                result.setDRG("28069");
                result.setDC("2806");
                break;
            }
            case "28PE": {//Radio-Implant
                result.setDRG("28079");
                result.setDC("2807");
                break;
            }
            case "28PF": {//Dialysis AV Shunt
                result.setDRG("28089");
                result.setDC("2808");
                break;
            }
            case "28PG": {//Closed reduction int fix
                result.setDRG("28099");
                result.setDC("2809");
                break;
            }
            case "28PH": {//Hernia Repair
                result.setDRG("28109");
                result.setDC("2810");
                break;
            }
            case "28PJ": {//Hydrocelectomy
                result.setDRG("28119");
                result.setDC("2811");
                break;
            }
            case "28PK": {//Cystoscopy
                result.setDRG("28129");
                result.setDC("2812");
                break;
            }
            case "28PL": {//Mouth & Tongue Proc
                result.setDRG("28139");
                result.setDC("2813");
                break;
            }
            case "28PM": {//Tendon Proc
                result.setDRG("28149");
                result.setDC("2814");
                break;
            }
            case "28PN": {//Esophageal Proc
                result.setDRG("28159");
                result.setDC("2815");
                break;
            }
            case "28PP": {//Circumcision & oth Penile Proc
                result.setDRG("28169");
                result.setDC("2816");
                break;
            }
            case "28PQ": {//Plastic Skin Proc
                result.setDRG("28179");
                result.setDC("2817");
                break;
            }
            case "28PR": {//Remove Implant
                result.setDRG("28189");
                result.setDC("2818");
                break;
            }
            case "28PS": {//D & C
                result.setDRG("28199");
                result.setDC("2819");
                break;
            }
            case "28PT": {//Breast Proc
                result.setDRG("28209");
                result.setDC("2820");
                break;
            }
            case "28PU": {//Female sterilization
                result.setDRG("28219");
                result.setDC("2821");
                break;
            }
            case "28PV": {//Colonoscopy
                result.setDRG("28229");
                result.setDC("2822");
                break;
            }
            case "28PW"://Amputation
                result.setDRG("28239");
                result.setDC("2823");
                break;
            case "28PX": {//Ear, Nose, Pharynx Proc
                result.setDRG("28249");
                result.setDC("2824");
                break;
            }
            case "28PY": {//Debride Open Fracture
                result.setDRG("28259");
                result.setDC("2825");
                break;
            }
            case "28PZ": {//Gastroscopy
                result.setDRG("28269");
                result.setDC("2826");
                break;
            }
            case "28QA": {//Skin & Nail Proc
                result.setDRG("28279");
                result.setDC("2827");
                break;
            }
            case "28QB": {//Cervical Proc
                result.setDRG("28289");
                result.setDC("2828");
                break;
            }
            case "28QC": {//Hemodialysis
                result.setDRG("28299");
                result.setDC("2829");
                break;
            }
            case "28QD": {//Other Eye Proc
                result.setDRG("28309");
                result.setDC("2830");
                break;
            }
            case "28QE": {//Carpal Tunnel Releas
                result.setDRG("28319");
                result.setDC("2831");
                break;
            }
            case "28QF": {//Closed Reduction of Dislocation
                result.setDRG("28329");
                result.setDC("2832");
                break;
            }
            case "28QG": {//Closed Reduction of Fracture
                result.setDRG("28339");
                result.setDC("2833");
                break;
            }
            case "28QH": {//Vulvar & Vagina Proc
                result.setDRG("28349");
                result.setDC("2834");
                break;
            }
            case "28QJ": {//Bartholin Gland Proc
                result.setDRG("28359");
                result.setDC("2835");
                break;
            }
            case "28QK": {//Urethral Proc
                result.setDRG("28369");
                result.setDC("2836");
                break;
            }
            default: {//Other OR Proc
                result.setDRG("28379");
                result.setDC("2837");
                break;
            }
        }
        return result;
    }

}
