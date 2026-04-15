/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.GetPDC;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.methods.validation.ORProcedure;
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
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC10 {

    public GetMDC10() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC10.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC10(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
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
                //AX 99PDX Checking
                if (utility.isValid99PDX(ProcedureList.get(x).trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(x).trim())) {
                    PCXCounter99++;
                }
                if (utility.isValid10PBX(ProcedureList.get(x).trim())) {
                    Counter10PBX++;
                }

                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource, ProcedureList.get(x).trim(), drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }

                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
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
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 10 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            logger.info("Executing MDC10 Method");
            logger.error("Error in MDC10 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

}
