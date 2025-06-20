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

    public DRGWSResult GetMDC28(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
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
                if (utility.isValid99PFX(ProcedureList.get(x).trim())) {
                    CaCRxProc++;
                }
            }
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (utility.isValid99CX(SecondaryList.get(a).trim())) {
                    CaCRxSDx++;
                }
            }
            if (CaCRxSDx > 0 && CaCRxProc > 0) {
                CaCRx++;
            }
            if (new GrouperMethod().AX(datasource, "28EX", grouperparameter.getPdx().toUpperCase().trim()).isSuccess()) {
                Counter28EX++;
            }
            if (new GrouperMethod().AX(datasource, "28BX", grouperparameter.getPdx().toUpperCase().trim()).isSuccess()) {
                Counter28BX++;
            }
            if (new GrouperMethod().AX(datasource, "28CX", grouperparameter.getPdx().toUpperCase().trim()).isSuccess()) {
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
                            switch (pdclist.get(hierarvalue.indexOf(min))) {
                                case "28PB": {//KUB ESWL
                                    drgResult.setDRG("28049");
                                    drgResult.setDC("2804");
                                    break;
                                }
                                case "28PC": {//Cadiac Cath & CAG
                                    drgResult.setDRG("28059");
                                    drgResult.setDC("2805");
                                    break;
                                }
                                case "28PD": {//Cataract Proc
                                    drgResult.setDRG("28069");
                                    drgResult.setDC("2806");
                                    break;
                                }
                                case "28PE": {//Radio-Implant
                                    drgResult.setDRG("28079");
                                    drgResult.setDC("2807");
                                    break;
                                }
                                case "28PF": {//Dialysis AV Shunt
                                    drgResult.setDRG("28089");
                                    drgResult.setDC("2808");
                                    break;
                                }
                                case "28PG": {//Closed reduction int fix
                                    drgResult.setDRG("28099");
                                    drgResult.setDC("2809");
                                    break;
                                }
                                case "28PH": {//Hernia Repair
                                    drgResult.setDRG("28109");
                                    drgResult.setDC("2810");
                                    break;
                                }
                                case "28PJ": {//Hydrocelectomy
                                    drgResult.setDRG("28119");
                                    drgResult.setDC("2811");
                                    break;
                                }
                                case "28PK": {//Cystoscopy
                                    drgResult.setDRG("28129");
                                    drgResult.setDC("2812");
                                    break;
                                }
                                case "28PL": {//Mouth & Tongue Proc
                                    drgResult.setDRG("28139");
                                    drgResult.setDC("2813");
                                    break;
                                }
                                case "28PM": {//Tendon Proc
                                    drgResult.setDRG("28149");
                                    drgResult.setDC("2814");
                                    break;
                                }
                                case "28PN": {//Esophageal Proc
                                    drgResult.setDRG("28159");
                                    drgResult.setDC("2815");
                                    break;
                                }
                                case "28PP": {//Circumcision & oth Penile Proc
                                    drgResult.setDRG("28169");
                                    drgResult.setDC("2816");
                                    break;
                                }
                                case "28PQ": {//Plastic Skin Proc
                                    drgResult.setDRG("28179");
                                    drgResult.setDC("2817");
                                    break;
                                }
                                case "28PR": {//Remove Implant
                                    drgResult.setDRG("28189");
                                    drgResult.setDC("2818");
                                    break;
                                }
                                case "28PS": {//D & C
                                    drgResult.setDRG("28199");
                                    drgResult.setDC("2819");
                                    break;
                                }
                                case "28PT": {//Breast Proc
                                    drgResult.setDRG("28209");
                                    drgResult.setDC("2820");
                                    break;
                                }
                                case "28PU": {//Female sterilization
                                    drgResult.setDRG("28219");
                                    drgResult.setDC("2821");
                                    break;
                                }
                                case "28PV": {//Colonoscopy
                                    drgResult.setDRG("28229");
                                    drgResult.setDC("2822");
                                    break;
                                }
                                case "28PW"://Amputation
                                    drgResult.setDRG("28239");
                                    drgResult.setDC("2823");
                                    break;
                                case "28PX": {//Ear, Nose, Pharynx Proc
                                    drgResult.setDRG("28249");
                                    drgResult.setDC("2824");
                                    break;
                                }
                                case "28PY": {//Debride Open Fracture
                                    drgResult.setDRG("28259");
                                    drgResult.setDC("2825");
                                    break;
                                }
                                case "28PZ": {//Gastroscopy
                                    drgResult.setDRG("28269");
                                    drgResult.setDC("2826");
                                    break;
                                }
                                case "28QA": {//Skin & Nail Proc
                                    drgResult.setDRG("28279");
                                    drgResult.setDC("2827");
                                    break;
                                }
                                case "28QB": {//Cervical Proc
                                    drgResult.setDRG("28289");
                                    drgResult.setDC("2828");
                                    break;
                                }
                                case "28QC": {//Hemodialysis
                                    drgResult.setDRG("28299");
                                    drgResult.setDC("2829");
                                    break;
                                }
                                case "28QD": {//Other Eye Proc
                                    drgResult.setDRG("28309");
                                    drgResult.setDC("2830");
                                    break;
                                }
                                case "28QE": {//Carpal Tunnel Releas
                                    drgResult.setDRG("28319");
                                    drgResult.setDC("2831");
                                    break;
                                }
                                case "28QF": {//Closed Reduction of Dislocation
                                    drgResult.setDRG("28329");
                                    drgResult.setDC("2832");
                                    break;
                                }
                                case "28QG": {//Closed Reduction of Fracture
                                    drgResult.setDRG("28339");
                                    drgResult.setDC("2833");
                                    break;
                                }
                                case "28QH": {//Vulvar & Vagina Proc
                                    drgResult.setDRG("28349");
                                    drgResult.setDC("2834");
                                    break;
                                }
                                case "28QJ": {//Bartholin Gland Proc
                                    drgResult.setDRG("28359");
                                    drgResult.setDC("2835");
                                    break;
                                }
                                case "28QK": {//Urethral Proc
                                    drgResult.setDRG("28369");
                                    drgResult.setDC("2836");
                                    break;
                                }
                                default: {//Other OR Proc
                                    drgResult.setDRG("28379");
                                    drgResult.setDC("2837");
                                    break;
                                }
                            }
                        } else {
                            drgResult.setDRG("28699");
                            drgResult.setDC("2869");
                        }
                        //GO TO METHOD 1
                    }
                    break;
                }
                case "2"://Against Advice Escape,Other
                case "3":
                case "5": {
                    if (finalage < 28) {
                        drgResult.setDRG("28519");
                        drgResult.setDC("2851");
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) < 11) {
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
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) < 11) {
                        if (utility.isValid28PBX(grouperparameter.getPdx())) {
                            drgResult.setDRG("28019");
                            drgResult.setDC("2801");
                        } else {
                            drgResult.setDRG("28629");
                            drgResult.setDC("2862");
                        }
                    } else if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) >= 12
                            && utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) <= 65) {
                        if (utility.isValid28PBX(grouperparameter.getPdx())) {
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
                        } else if (utility.isValid28DX(grouperparameter.getPdx())) {
                            drgResult.setDRG("28669");
                            drgResult.setDC("2866");
                        } else {
                            if (utility.isValid28PBX(grouperparameter.getPdx())) {
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
            //=================================================
            if (drgResult.getDRG() != null) {
                if (new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("Grouper Error");
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

}
