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
public class GetMDC06 {

    public GetMDC06() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC06(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            //THIS AREA IS FOR CHECKING OF RADIO AND CHECMO
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX6Proc = 0;
            int PBX99Proc = 0;
            //Checking SDx RadioTherapy and Chemotherapy
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (utility.isValid99BX(SecondaryList.get(a).trim())) {
                    CartSDx++;
                }
                if (utility.isValid99CX(SecondaryList.get(a).trim())) {
                    CaCRxSDx++;
                }
            }

            int MalignantCount = 0;
            if (new GrouperMethod().PDxMalignancy(datasource, grouperparameter.getPdx(), "6A").isSuccess()) {
                MalignantCount++;
            }
            //Maj Dig Dis AX 6BX
            int Ax6BXCount = 0;
            if (new GrouperMethod().AX(datasource, "6BX", grouperparameter.getPdx()).isSuccess()) {
                Ax6BXCount++;
            }
            //Inguinal or Femoral PDC 6PH
            int Counter6PH = 0;
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE
            int mdcprocedureCounter = 0;
            int ORProcedureCounter = 0;
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int y = 0; y < ProcedureList.size(); y++) {
                DRGWSResult JoinResult = new GrouperMethod().MDCProcedure(datasource, ProcedureList.get(y).trim(), drgResult.getMDC());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GrouperMethod().GetPDC(datasource, mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (String.valueOf(pdcresult.isSuccess()).equals("true")) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                //Inguinal or Femoral PDC 6PH
                if (new GrouperMethod().Endovasc(datasource, ProcedureList.get(y).trim(), "6PH", drgResult.getMDC()).isSuccess()) {
                    Counter6PH++;
                }
                DRGWSResult ORProcedureResult = new GrouperMethod().ORProcedure(datasource, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                //AX 99PDX Checking
                if (utility.isValid99PDX(ProcedureList.get(y).trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(ProcedureList.get(y).trim())) {
                    PCXCounter99++;
                }
                if (utility.isValid99PEX(ProcedureList.get(y).trim())) {
                    CartProc++;
                }
                if (utility.isValid99PFX(ProcedureList.get(y).trim())) {
                    CaCRxProc++;
                }
                DRGWSResult PBX6ProcResult = new GrouperMethod().AX(datasource, "6PBX", ProcedureList.get(y).trim());
                if (PBX6ProcResult.isSuccess()) {//Dx Procedure
                    PBX6Proc++;
                }
                if (utility.isValid99PBX(ProcedureList.get(y).trim())) { //Blood Transfusion AX 99PBX
                    PBX99Proc++;
                }
            }
            //CONDITIONAL STATEMENT STARTS HERE FOR MDC 06

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

                            case "6PS"://Lap Stomach, Eso & Duodenum
                                drgResult.setDC("0627");
                                break;
                            case "6PV"://Stomach ,Eso & Duodenum Resection
                                if (MalignantCount > 0) {
                                    drgResult.setDC("0630");
                                } else {
                                    drgResult.setDC("0631");
                                }
                                break;
                            case "6PE"://Other Stomach ,Eso & Duodenum
                                if (MalignantCount > 0) {
                                    drgResult.setDC("0601");
                                } else {
                                    drgResult.setDC("0602");
                                }
                                break;
                            case "6PA"://Rectal Resection
                                drgResult.setDC("0604");
                                break;
                            case "6PB"://Major Small and Large Bowel
                                drgResult.setDC("0603");
                                break;
                            case "6PT"://Lap Peritoneal Adhesiolysis
                                drgResult.setDC("0628");
                                break;
                            case "6PK"://Other Digestive System OR Procedures
                                if (MalignantCount > 0) {
                                    drgResult.setDC("0613");
                                } else {
                                    drgResult.setDC("0614");
                                }
                                break;
                            case "6PM"://Complex Therapeutic Gastroscopy
                                if (Ax6BXCount > 0) {
                                    drgResult.setDC("0616");
                                } else {
                                    drgResult.setDC("0617");
                                }
                                break;
                            case "6PD"://Minor Small and Large Bowel
                                drgResult.setDC("0608");
                                break;
                            case "6PC"://Peritoneal Adhesiolysis
                                drgResult.setDC("0605");
                                break;
                            case "6PG":
                            case "6PH":
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) > 14) {
                                    if (Counter6PH > 0) { //IF TRUE
                                        drgResult.setDC("0610");
                                    } else {
                                        drgResult.setDC("0611");//IF FALSE
                                    }
                                } else {
                                    drgResult.setDC("0612");
                                }
                                break;

                            case "6PL"://Pyloromyotomy procedure
                                drgResult.setDC("0615");
                                break;
                            case "6PU"://Lap Appendectomy
                                drgResult.setDC("0629");
                                break;
                            case "6PJ"://Appendectomy
                                if (utility.isValid6CX(grouperparameter.getPdx())) {
                                    drgResult.setDC("0632");
                                } else {
                                    drgResult.setDC("0607");
                                }
                                break;
                            case "6PN"://Other Gastroscopy
                                if (Ax6BXCount > 0) {
                                    if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                                            utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                            grouperparameter.getDischargeDate(),
                                            utility.Convert24to12(grouperparameter.getTimeDischarge())) > 0) {
                                        drgResult.setDC("0619");
                                    } else {
                                        drgResult.setDRG("06209");
                                        drgResult.setDC("0620");
                                    }
                                } else {
                                    if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                                            utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                            grouperparameter.getDischargeDate(),
                                            utility.Convert24to12(grouperparameter.getTimeDischarge())) > 0) {
                                        drgResult.setDC("0621");
                                    } else {
                                        drgResult.setDRG("06229");
                                        drgResult.setDC("0622");
                                    }
                                }
                                break;
                            case "6PP"://Complex Therpeutic Colonoscopy
                                drgResult.setDC("0623");
                                break;
                            case "6PQ"://Other Colonoscopy
                                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                        grouperparameter.getDischargeDate(),
                                        utility.Convert24to12(grouperparameter.getTimeDischarge())) > 0) {
                                    drgResult.setDC("0624");
                                } else {
                                    drgResult.setDRG("06259");
                                    drgResult.setDC("0625");
                                }
                                break;
                            case "6PF"://Anal and Stomal
                                drgResult.setDC("0609");
                                break;
                            case "6PR"://Dilatation of Intestine 6PR
                                drgResult.setDRG("06269");
                                drgResult.setDC("0626");
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
                            case "6A":
                                //Radio+Chemotherapy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0668");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0669");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("0670");
                                } else if (PBX6Proc > 0) { //##Dx Procedure
                                    drgResult.setDC("0671");
                                    //Radiotherapy
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("0672");
                                } else {//Malignancy 
                                    if (grouperparameter.getDischargeType().equals("4")) {
                                        drgResult.setDC("0673");
                                    } else {
                                        drgResult.setDC("0650");
                                    }
                                }
                                break;
                            case "6B"://G.I. Hemorrhage
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) > 64) {
                                    drgResult.setDC("0651");
                                } else {
                                    drgResult.setDC("0652");
                                }
                                break;
                            case "6C"://Complicated Peptic Ulcer
                                drgResult.setDC("0653");
                                break;
                            case "6D"://Uncomplicated Peptic Ulcer
                                drgResult.setDC("0654");
                                break;
                            case "6E"://Inflammatory Bowel Diseases
                                drgResult.setDC("0655");
                                break;
                            case "6F"://G.I. Obstruction
                                if (grouperparameter.getDischargeType().equals("4")) {//Transfer
                                    drgResult.setDC("0674");
                                } else {
                                    drgResult.setDC("0656");//Others
                                }
                                break;
                            case "6G"://Gastroenteritis
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) > 9) {
                                    drgResult.setDC("0657");
                                } else {
                                    drgResult.setDC("0658");
                                }
                                break;
                            case "6H"://Misc Digestive Disorder
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) > 9) {
                                    drgResult.setDC("0666");
                                } else {
                                    drgResult.setDC("0667");
                                }
                                break;
                            case "6J"://Other Digestive System Diagnoses
                                if (grouperparameter.getDischargeType().equals("4")) {
                                    drgResult.setDC("0675");
                                } else {
                                    drgResult.setDC("0660");
                                }
                                break;
                            case "6K"://Abdominal Pain or Mesenteric Adenitis
                                drgResult.setDC("0661");
                                break;
                            case "6L"://Intestinal Helminthiases
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) > 9) {
                                    drgResult.setDC("0662");
                                } else {
                                    drgResult.setDC("0663");
                                }
                                break;
                            case "6M"://Esophagitis, Gastritis & Dyspepsia PDC 6M
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 9) {
                                    drgResult.setDC("0664");
                                } else {
                                    drgResult.setDC("0665");
                                }
                                break;
                        }

                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0633");
                    } else {
                        drgResult.setDC("0634");
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
                    case "6PS"://Lap Stomach, Eso & Duodenum
                        drgResult.setDC("0627");
                        break;
                    case "6PV"://Stomach ,Eso & Duodenum Resection
                        if (MalignantCount > 0) {
                            drgResult.setDC("0630");
                        } else {
                            drgResult.setDC("0631");
                        }
                        break;
                    case "6PE"://Other Stomach ,Eso & Duodenum
                        if (MalignantCount > 0) {
                            drgResult.setDC("0601");
                        } else {
                            drgResult.setDC("0602");
                        }
                        break;
                    case "6PA"://Rectal Resection
                        drgResult.setDC("0604");
                        break;
                    case "6PB"://Major Small and Large Bowel
                        drgResult.setDC("0603");
                        break;
                    case "6PT"://Lap Peritoneal Adhesiolysis
                        drgResult.setDC("0628");
                        break;
                    case "6PK"://Other Digestive System OR Procedures
                        if (MalignantCount > 0) {
                            drgResult.setDC("0613");
                        } else {
                            drgResult.setDC("0614");
                        }
                        break;
                    case "6PM"://Complex Therapeutic Gastroscopy
                        if (Ax6BXCount > 0) {
                            drgResult.setDC("0616");
                        } else {
                            drgResult.setDC("0617");
                        }
                        break;
                    case "6PD"://Minor Small and Large Bowel
                        drgResult.setDC("0608");
                        break;
                    case "6PC"://Peritoneal Adhesiolysis
                        drgResult.setDC("0605");
                        break;
                    case "6PG":
                    case "6PH":
                        if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                grouperparameter.getAdmissionDate()) > 14) {
                            if (Counter6PH > 0) { //IF TRUE
                                drgResult.setDC("0610");
                            } else {
                                drgResult.setDC("0611");//IF FALSE
                            }
                        } else {
                            drgResult.setDC("0612");
                        }
                        break;
                    case "6PL"://Pyloromyotomy procedure
                        drgResult.setDC("0615");
                        break;
                    case "6PU"://Lap Appendectomy
                        drgResult.setDC("0629");
                        break;
                    case "6PJ"://Appendectomy
                        if (utility.isValid6CX(grouperparameter.getPdx())) {
                            drgResult.setDC("0632");
                        } else {
                            drgResult.setDC("0607");
                        }
                        break;
                    case "6PN"://Other Gastroscopy
                        if (Ax6BXCount > 0) {
                            if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                    grouperparameter.getDischargeDate(),
                                    utility.Convert24to12(grouperparameter.getTimeDischarge())) > 0) {
                                drgResult.setDC("0619");
                            } else {
                                drgResult.setDRG("06209");
                                drgResult.setDC("0620");
                            }
                        } else {
                            if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                                    utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                    grouperparameter.getDischargeDate(),
                                    utility.Convert24to12(grouperparameter.getTimeDischarge())) > 0) {
                                drgResult.setDC("0621");
                            } else {
                                drgResult.setDRG("06229");
                                drgResult.setDC("0622");
                            }
                        }
                        break;
                    case "6PP"://Complex Therpeutic Colonoscopy
                        drgResult.setDC("0623");
                        break;
                    case "6PQ"://Other Colonoscopy
                        if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                                utility.Convert24to12(grouperparameter.getTimeAdmission()),
                                grouperparameter.getDischargeDate(),
                                utility.Convert24to12(grouperparameter.getTimeDischarge())) > 0) {
                            drgResult.setDC("0624");
                        } else {
                            drgResult.setDRG("06259");
                            drgResult.setDC("0625");
                        }
                        break;
                    case "6PF"://Anal and Stomal
                        drgResult.setDC("0609");
                        break;
                    case "6PR"://Dilatation of Intestine 6PR
                        drgResult.setDRG("06269");
                        drgResult.setDC("0626");
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
                    case "6A":
                        //Radio+Chemotherapy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0668");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0669");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("0670");
                        } else if (PBX6Proc > 0) { //##Dx Procedure
                            drgResult.setDC("0671");
                            //Radiotherapy
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("0672");
                        } else {//Malignancy 
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("0673");
                            } else {
                                drgResult.setDC("0650");
                            }
                        }
                        break;

                    case "6B"://G.I. Hemorrhage
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 64) {
                            drgResult.setDC("0651");
                        } else {
                            drgResult.setDC("0652");
                        }
                        break;
                    case "6C"://Complicated Peptic Ulcer
                        drgResult.setDC("0653");
                        break;
                    case "6D"://Uncomplicated Peptic Ulcer
                        drgResult.setDC("0654");
                        break;
                    case "6E"://Inflammatory Bowel Diseases
                        drgResult.setDC("0655");
                        break;
                    case "6F"://G.I. Obstruction
                        if (grouperparameter.getDischargeType().equals("4")) {//Transfer
                            drgResult.setDC("0674");
                        } else {
                            drgResult.setDC("0656");//Others
                        }

                        break;
                    case "6G"://Gastroenteritis
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 9) {
                            drgResult.setDC("0657");
                        } else {
                            drgResult.setDC("0658");
                        }

                        break;
                    case "6H"://Misc Digestive Disorder
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 9) {
                            drgResult.setDC("0666");
                        } else {
                            drgResult.setDC("0667");
                        }
                        break;
                    case "6J"://Other Digestive System Diagnoses
                        if (grouperparameter.getDischargeType().equals("4")) {
                            drgResult.setDC("0675");
                        } else {
                            drgResult.setDC("0660");
                        }
                        break;
                    case "6K"://Abdominal Pain or Mesenteric Adenitis
                        drgResult.setDC("0661");
                        break;
                    case "6L"://Intestinal Helminthiases
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 9) {
                            drgResult.setDC("0662");
                        } else {
                            drgResult.setDC("0663");
                        }
                        break;
                    case "6M"://Esophagitis, Gastritis & Dyspepsia PDC 6M
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 9) {
                            drgResult.setDC("0664");
                        } else {
                            drgResult.setDC("0665");
                        }
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
                    drgResult.setDRGName("Grouper Error");
                }
            }
            result.setSuccess(true);
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
            result.setMessage("MDC 06 Done Checking");

        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC06.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }
}
