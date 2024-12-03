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
import java.text.ParseException;
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
public class GetMDC10 {

    public GetMDC10() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC10(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int Counter10PBX = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);
                //AX 99PDX Checking
                if (utility.isValid99PDX(proc.trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(proc.trim())) {
                    PCXCounter99++;
                }
                if (utility.isValid10PBX(proc.trim())) {
                    Counter10PBX++;
                }

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

                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc.trim());
                if (String.valueOf(ORProcedureResult.isSuccess()).equals("true")) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
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
                            case "10PB"://Pituitary
                                drgResult.setDC("1001");
                                break;
                            case "10PC"://Amputation of Lower Limb
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 59) {
                                    drgResult.setDC("1003");
                                } else {
                                    drgResult.setDC("1004");
                                }
                                break;
                            case "10PE"://Procedure for Obesity
                                drgResult.setDC("1005");
                                break;
                            case "10PA"://Adrenal
                                drgResult.setDC("1002");
                                break;
                            case "10PF"://Parathyroid
                                drgResult.setDC("1007");
                                break;
                            case "10PD"://Skin Grafts and Wound Debridement
                                drgResult.setDC("1006");
                                break;
                            case "10PG"://Thyroid
                                drgResult.setDC("1008");
                                break;
                            case "10PJ"://Other Endocrine, Nutritional & Metabolic OR Procedures
                                drgResult.setDC("1010");
                                break;
                            case "10PH"://Thyroglossal PDC 10PH
                                drgResult.setDC("1009");
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
                            case "10A"://Diabetes with Complicated PDx
                                if (Counter10PBX > 0) {
                                    drgResult.setDC("1057");
                                } else {
                                    drgResult.setDC("1050");
                                }
                                break;
                            case "10B"://Severe Metabolic Disorders
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                                    drgResult.setDC("1051");
                                } else {
                                    drgResult.setDC("1052");
                                }
                                break;
                            case "10C"://Nutritional and Misc. Metabolic Disorders
                                drgResult.setDC("1053");
                                break;
                            case "10D"://Inborn Errors of Metabolism
                                drgResult.setDC("1054");
                                break;
                            case "10E"://Endocrine Disorders
                                drgResult.setDC("1055");
                                break;
                            case "10F"://Diabetes without Complicated PDx PDC 10F
                                drgResult.setDC("1056");
                                break;

                        }

                    }

                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("1011");
                    } else {
                        drgResult.setDC("1012");
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
                    case "10PB"://Pituitary
                        drgResult.setDC("1001");
                        break;
                    case "10PC"://Amputation of Lower Limb
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 59) {
                            drgResult.setDC("1003");
                        } else {
                            drgResult.setDC("1004");
                        }
                        break;
                    case "10PE"://Procedure for Obesity
                        drgResult.setDC("1005");
                        break;
                    case "10PA"://Adrenal
                        drgResult.setDC("1002");
                        break;
                    case "10PF"://Parathyroid
                        drgResult.setDC("1007");
                        break;
                    case "10PD"://Skin Grafts and Wound Debridement
                        drgResult.setDC("1006");
                        break;
                    case "10PG"://Thyroid
                        drgResult.setDC("1008");
                        break;
                    case "10PJ"://Other Endocrine, Nutritional & Metabolic OR Procedures
                        drgResult.setDC("1010");
                        break;
                    case "10PH"://Thyroglossal PDC 10PH
                        drgResult.setDC("1009");
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
                    case "10A"://Diabetes with Complicated PDx
                        if (Counter10PBX > 0) {
                            drgResult.setDC("1057");
                        } else {
                            drgResult.setDC("1050");
                        }
                        break;
                    case "10B"://Severe Metabolic Disorders
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                            drgResult.setDC("1051");
                        } else {
                            drgResult.setDC("1052");
                        }
                        break;
                    case "10C"://Nutritional and Misc. Metabolic Disorders
                        drgResult.setDC("1053");
                        break;
                    case "10D"://Inborn Errors of Metabolism
                        drgResult.setDC("1054");
                        break;
                    case "10E"://Endocrine Disorders
                        drgResult.setDC("1055");
                        break;
                    case "10F"://Diabetes without Complicated PDx PDC 10F
                        drgResult.setDC("1056");
                        break;
                }
            }

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
                    drgResult.setDRGName("Grouper Error");
                }
            }
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 10 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC10.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
