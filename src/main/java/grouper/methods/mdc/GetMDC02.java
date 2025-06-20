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
public class GetMDC02 {

    public GetMDC02() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC02(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            // THIS AREA IS FOR ORProcedure 
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            //CHECK AGE YEAR
//            String pdc2E = "2E";
            int MalignantCount = 0;
            //Malignant Counter for Primay Code (PDx)
            int Counter2PCX = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            // THIS AREA IS FOR PDC CHECK PROCEDURE COUNTER 2PA
            int mdcprocedureCounter = 0;
            int pdcprocedureCounter2PA = 0;
            int pdcprocedureCounter2PJ = 0;
            int pdcprocedureCounter2PH = 0;
            int pdcprocedureCounter2PB = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int Counter2PDX = 0;
            for (int y = 0; y < ProcedureList.size(); y++) {
                if (new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(y).trim(), drgResult.getMDC()).isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(y).trim(), drgResult.getMDC()).getResult(), MDCProcedure.class);
//                    DRGWSResult pdcresult = new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC().trim(), drgResult.getMDC());
                    if (new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC().trim(), drgResult.getMDC()).isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC().trim(), drgResult.getMDC()).getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }

                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "2PA".trim(), drgResult.getMDC()).isSuccess()) {
                    pdcprocedureCounter2PA++;
                }
                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "2PJ".trim(), drgResult.getMDC()).isSuccess()) {
                    pdcprocedureCounter2PJ++;
                }
                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "2PH".trim(), drgResult.getMDC()).isSuccess()) {
                    pdcprocedureCounter2PH++;
                }
                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(y), "2PB".trim(), drgResult.getMDC()).isSuccess()) {
                    pdcprocedureCounter2PB++;
                }
                if (new GrouperMethod().AX(datasource, "2PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter2PDX++;
                }
                //AX 99PDX Checking
                if (new GrouperMethod().AX(datasource, "99PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (new GrouperMethod().AX(datasource, "99PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                if (new GrouperMethod().AX(datasource, "2PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter2PCX++;
                }
                DRGWSResult ORProcedureResult = new GrouperMethod().ORProcedure(datasource, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }
            int CounterPDx2BX = 0;
            if (new GrouperMethod().AX(datasource, "2BX", grouperparameter.getPdx()).isSuccess()) {
                CounterPDx2BX++;
            }
            if (new GrouperMethod().PDxMalignancy(datasource, grouperparameter.getPdx(), "2E".trim()).isSuccess()) {
                MalignantCount++;
            }
            //Condition Start this area   
            if (PDXCounter99 > 0) {
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) >= 21) {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0214");
                    } else {
                        drgResult.setDC("0215");
                    }

                } else if (pdcprocedureCounter2PA > 0) {  //Retina Procedure
                    drgResult.setDC("0201");
                } else if (pdcprocedureCounter2PJ > 0) {//Keratoplasty
                    drgResult.setDC("0209");
                } else if (pdcprocedureCounter2PH > 0) { //Other mech Vitrectomy
                    if (Counter2PDX > 0) { //Cataract Frag/Asp
                        drgResult.setDC("0206");
                    } else {
                        drgResult.setDC("0201");
                    }
                } else if (pdcprocedureCounter2PB > 0) { //Enuc & Orbit Procedure
                    if (MalignantCount > 0) { //PDx Malignancy (PDC 2E)
                        drgResult.setDC("0210");
                    } else {
                        drgResult.setDC("0202");
                    }
                } else if (Counter2PCX > 0 && CounterPDx2BX > 0) { // Major Aye Injury with OR 
                    drgResult.setDC("0203");
                } else if (mdcprocedureCounter > 0) { // MDC Procedure
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
                        case "2PK": { //Multiple Major Lens
                            drgResult.setDC("0211");
                            break;
                        }
                        case "2PL": {//Multiple Other Lens
                            drgResult.setDC("0212");
                            break;
                        }
                        case "2PE": { //Major Lens
                            drgResult.setDC("0206");
                            break;
                        }
                        case "2PM": { //Major Procedures for Lacrimal System
                            drgResult.setDC("0213");
                            break;
                        }
                        case "2PD": { //Intraoc Procedures Except lens & Retina
                            drgResult.setDC("0205");
                            break;
                        }
                        case "2PF": {//Other Lens
                            drgResult.setDC("0207");
                            break;
                        }
                        case "2PG": {//Other Eye Procedures 2PG
                            drgResult.setDC("0208");
                            break;
                        }
                    }

                } else if (ORProcedureCounter > 0) { //Check if OR Procedure
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

                } else { //Principal Diagnosis
                    switch (drgResult.getPDC()) {
                        case "2C"://Hyphema and Trauma
                            drgResult.setDC("0250");
                            break;
                        case "2A"://Acute Major Infections
                            if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                    grouperparameter.getAdmissionDate()) > 54) {
                                drgResult.setDC("0251");
                            } else {
                                drgResult.setDC("0252");
                            }
                            break;
                        case "2E"://Malignancy
                            drgResult.setDC("0255");
                            break;
                        case "2B"://Neurological & Vasc Disorders
                            drgResult.setDC("0253");
                            break;
                        case "2D": //Other Disorders of the Eye PDC 2D
                            drgResult.setDC("0254");
                            break;
                    }
                }

                //START HERE
            } else if (pdcprocedureCounter2PA > 0) {  //Retina Procedure
                drgResult.setDC("0201");
            } else if (pdcprocedureCounter2PJ > 0) {//Keratoplasty
                drgResult.setDC("0209");
            } else if (pdcprocedureCounter2PH > 0) { //Other mech Vitrectomy
                if (Counter2PDX > 0) { //Cataract Frag/Asp
                    drgResult.setDC("0206");
                } else {
                    drgResult.setDC("0201");
                }
            } else if (pdcprocedureCounter2PB > 0) { //Enuc & Orbit Procedure
                if (MalignantCount > 0) { //PDx Malignancy (PDC 2E)
                    drgResult.setDC("0210");
                } else {
                    drgResult.setDC("0202");
                }

            } else if (Counter2PCX > 0 && CounterPDx2BX > 0) { // Major Aye Injury with OR 
                drgResult.setDC("0203");

            } else if (mdcprocedureCounter > 0) { // MDC Procedure
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
                    case "2PK": //Multiple Major Lens
                        drgResult.setDC("0211");
                        break;
                    case "2PL": //Multiple Other Lens
                        drgResult.setDC("0212");
                        break;
                    case "2PE": //Major Lens
                        drgResult.setDC("0206");
                        break;
                    case "2PM": //Major Procedures for Lacrimal System
                        drgResult.setDC("0213");
                        break;
                    case "2PD": //Intraoc Procedures Except lens & Retina
                        drgResult.setDC("0205");
                        break;
                    case "2PF": //Other Lens
                        drgResult.setDC("0207");
                        break;
                    case "2PG": //Other Eye Procedures 2PG
                        drgResult.setDC("0208");
                        break;
                }

            } else if (ORProcedureCounter > 0) { //Check if OR Procedure
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

            } else { //Principal Diagnosis

                switch (drgResult.getPDC()) {
                    case "2C"://Hyphema and Trauma
                        drgResult.setDC("0250");
                        break;
                    case "2A"://Acute Major Infections
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 54) {
                            drgResult.setDC("0251");
                        } else {
                            drgResult.setDC("0252");
                        }
                        break;
                    case "2E"://Malignancy
                        drgResult.setDC("0255");
                        break;
                    case "2B"://Neurological & Vasc Disorders
                        drgResult.setDC("0253");
                        break;
                    case "2D": //Other Disorders of the Eye PDC 2D
                        drgResult.setDC("0254");
                        break;
                }

            }

            if (drgResult.getDRG() == null) {
                //-------------------------------------------------------------------------------------
                if (utility.isValidDCList(drgResult.getDC())) {
                    drgResult.setDRG(drgResult.getDC() + "9");
                } else {
                    //----------------------------------------------------------------------
                    //  String sdxfinalList =  new GrouperMethod().CleanSDxDCDetermination(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    String sdxfinalList = new GrouperMethod().CleanSDxDCDeterminationPLSQL(datasource, grouperparameter.getSdx(), drgResult.getSDXFINDER(), grouperparameter.getPdx(), drgResult.getDC());
                    DRGWSResult getpcclvalue = new GrouperMethod().GetPCCL(datasource, drgResult, grouperparameter, sdxfinalList);
                    if (getpcclvalue.isSuccess()) {
                        DRGOutput finaldrgresult = utility.objectMapper().readValue(getpcclvalue.getResult(), DRGOutput.class);
                        //-----------------------------------------------------------------------
                        if (new GrouperMethod().DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
                            drgResult.setDRG(finaldrgresult.getDRG());
                            drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), finaldrgresult.getDRG()).getMessage());
                        } else {
                            if (new GrouperMethod().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG()).isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + new GrouperMethod().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG()).getResult());
                                DRGWSResult drgnames = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                                drgResult.setDRGName(drgnames.getMessage());
                            } else {
                                drgResult.setDRG(finaldrgresult.getDRG());
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
                if (new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).isSuccess()) {
                    drgResult.setDRGName(new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG()).getMessage());
                } else {
                    drgResult.setDRGName("No DRG Name Found");
                }
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 02 Done Processed");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC02.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
