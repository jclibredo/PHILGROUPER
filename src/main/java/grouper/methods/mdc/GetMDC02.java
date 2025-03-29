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
 * @author DRG_SHADOWBILLING
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
//            GrouperMethod gm = new GrouperMethod();
            //   String MDC = drgResult.getMDC();
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
//            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));

            // THIS AREA IS FOR ORProcedure 
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            //CHECK AGE YEAR
//            String pdc2E = "2E";
            int MalignantCount = 0;

            //Malignant Counter for Primay Code (PDx)
            String pcx2 = "2PCX";
            String bx2 = "2BX";
            int Counter2PCX = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            // THIS AREA IS FOR PDC CHECK PROCEDURE COUNTER 2PA
            int mdcprocedureCounter = 0;
            int pdcprocedureCounter2PA = 0;
            int pdcprocedureCounter2PJ = 0;
            int pdcprocedureCounter2PH = 0;
            int pdcprocedureCounter2PB = 0;
//            String pdcs2PB = "2PB";
//            String pdcs2PJ = "2PJ";
//            String pdcs2PA = "2PA";
//            String pdcs2PH = "2PH";

            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            int Counter2PDX = 0;
            for (int y = 0; y < ProcedureList.size(); y++) {
//                String proc = ProcedureList.get(y);
//                DRGWSResult JoinResult = new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(y).trim(), drgResult.getMDC());
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

//                DRGWSResult PdcProcResult2PA = new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "2PA".trim(), drgResult.getMDC());
                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "2PA".trim(), drgResult.getMDC()).isSuccess()) {
                    pdcprocedureCounter2PA++;
                }
//                DRGWSResult PdcProcResult2PJ = new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "2PJ".trim(), drgResult.getMDC());
                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "2PJ".trim(), drgResult.getMDC()).isSuccess()) {
                    pdcprocedureCounter2PJ++;
                }
                DRGWSResult PdcProcResult2PH = new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "2PH".trim(), drgResult.getMDC());
                if (PdcProcResult2PH.isSuccess()) {
                    pdcprocedureCounter2PH++;
                }
                DRGWSResult PdcProcResult2PB = new GrouperMethod().Endovasc(datasource, ProcedureList.get(y), "2PB".trim(), drgResult.getMDC());
                if (PdcProcResult2PB.isSuccess()) {
                    pdcprocedureCounter2PB++;
                }

                DRGWSResult Counter2PDXResult = new GrouperMethod().AX(datasource, "2PDX", ProcedureList.get(y).trim());
                if (Counter2PDXResult.isSuccess()) {
                    Counter2PDX++;
                }
                //AX 99PDX Checking
                DRGWSResult Counter99PDXResult = new GrouperMethod().AX(datasource, "99PDX", ProcedureList.get(y).trim());
                if (Counter99PDXResult.isSuccess()) {
                    PDXCounter99++;
                }

                //AX 99PCX Checking
                DRGWSResult Counter99PCXResult = new GrouperMethod().AX(datasource, "99PCX", ProcedureList.get(y).trim());
                if (Counter99PCXResult.isSuccess()) {
                    PCXCounter99++;
                }

                DRGWSResult Counter2PCXResult = new GrouperMethod().AX(datasource, pcx2.trim(), ProcedureList.get(y).trim());
                if (Counter2PCXResult.isSuccess()) {
                    Counter2PCX++;
                }
                DRGWSResult ORProcedureResult = new GrouperMethod().ORProcedure(datasource, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }
            int CounterPDx2BX = 0;
            DRGWSResult Counter2BXResult = new GrouperMethod().AX(datasource, bx2.trim(), grouperparameter.getPdx());
            if (Counter2BXResult.isSuccess()) {
                CounterPDx2BX++;
            }

            DRGWSResult getMalignantResult = new GrouperMethod().PDxMalignancy(datasource, grouperparameter.getPdx(), "2E".trim());
            if (getMalignantResult.isSuccess()) {
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
                        String drgValue = finaldrgresult.getDRG();
                        DRGWSResult drgname = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgValue);
                        //-----------------------------------------------------------------------
                        if (drgname.isSuccess()) {
                            drgResult.setDRG(drgValue);
                            drgResult.setDRGName(drgname.getMessage());
                        } else {
                            DRGWSResult drgvalues = new GrouperMethod().ValidatePCCL(datasource, drgResult.getDC(), drgValue);
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
                                DRGWSResult drgnames = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG());
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
                DRGWSResult drgname = new GrouperMethod().DRG(datasource, drgResult.getDC(), drgResult.getDRG());
                if (drgname.isSuccess()) {
                    drgResult.setDRGName(drgname.getMessage());
                } else {
                    drgResult.setDRGName("No DRG Name Found");
                }
            }

            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 02 Done Processed");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC02.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
