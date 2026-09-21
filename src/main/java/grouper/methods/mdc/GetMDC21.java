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
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC21 {

    public GetMDC21() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC21(
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
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            AX checkAX = new AX();
            for (int x = 0; x < ProcedureList.size(); x++) {
                //AX 99PDX Checking
                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
                    PDXCounter99++;
                }
                //AX 99PCX Checking  PCXCounter99++;
                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(x).trim()).isSuccess()) {//Dx Procedure
                    PCXCounter99++;
                }
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
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
            }
            if (PDXCounter99 > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
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
                        String getDc = this.mdcProcedure(drgResult.getPDC());
                        drgResult.setDC(getDc);
                    } else if (ORProcedureCounter > 0) {
                        String dc = this.orProcedure(Collections.max(ORProcedureCounterList));
                        drgResult.setDC(dc);
                    } else {
                        String getDc = this.principalDaignosis(drgResult.getPDC(), grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
                        drgResult.setDC(getDc);
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("2105");
                    } else {
                        drgResult.setDC("2106");
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
                String getDc = this.mdcProcedure(drgResult.getPDC());
                drgResult.setDC(getDc);
            } else if (ORProcedureCounter > 0) {
                String dc = this.orProcedure(Collections.max(ORProcedureCounterList));
                drgResult.setDC(dc);
            } else {
                String getDc = this.principalDaignosis(drgResult.getPDC(), grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate());
                drgResult.setDC(getDc);
            }
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 21 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC21.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    private String mdcProcedure(
            final String pdc) {
        String result = "";
        switch (pdc.toUpperCase()) {
            case "21PF"://Multiple Wound Debridement
                result = "2107";
                break;
            case "21PB"://Skin Graft
                result = "2102";
                break;
            case "21PD"://Other OR Procedures for Injuries
            case "21PE":
                result = "2104";
                break;
            case "21PC"://Hand procedures
                result = "2103";
                break;
            case "21PA"://Wound Debridement PDC 21PA
                result = "2101";
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
            final String bdate,
            final String admDate) {
        String dc = "";
        switch (pdc.toUpperCase()) {
            case "21A"://Traumatic Injury
                if (utility.ComputeYear(bdate, admDate) > 17) {
                    dc = "2150";
                } else {
                    dc = "2151";
                }
                break;
            case "21B"://Allergic Reaction
                if (utility.ComputeYear(bdate, admDate) > 17) {
                    dc = "2152";
                } else {
                    dc = "2153";
                }
                break;
            case "21C"://Drugs
                if (utility.ComputeYear(bdate, admDate) > 17) {
                    dc = "2154";
                } else {
                    dc = "2155";
                }
                break;
            case "21D"://Complications of Treatment
                dc = "2156";
                break;
            case "21E"://Other Injury, Poisoning and Toxic Effects Diagnoses PDC 21E
                dc = "2157";
                break;
        }
        return dc;

    }

}
