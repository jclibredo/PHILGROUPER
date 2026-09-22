/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.methods.validation.Endovasc;
import grouper.methods.validation.GetPDC;
import grouper.methods.validation.MDCProcedureMethod;
import grouper.methods.validation.ORProcedure;
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
public class GetMDC08 {

    public GetMDC08() {
    }
    private final Logger logger = (Logger) LogManager.getLogger(GetMDC08.class);
    private final Utility utility = new Utility();

    public DRGWSResult GetMDC08(
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
            AX checkAX = new AX();
            //CHECKING FOR TRAUMA CODES
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            //Checking SDx RadioTherapy and Chemotherapy
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter8PFX = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                String Secon = SecondaryList.get(a);
                if (checkAX.AX(datasource, SchemaName, "99BX", Secon.trim()).isSuccess()) {
                    CartSDx++;
                }
                if (checkAX.AX(datasource, SchemaName, "99CX", Secon.trim()).isSuccess()) {
                    CaCRxSDx++;
                }
            }

            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int Counter8PH = 0;
            int Counter8QA = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();
            for (int y = 0; y < ProcedureList.size(); y++) {
                DRGWSResult ORProcedureResult = new ORProcedure().ORProcedure(datasource, SchemaName, ProcedureList.get(y).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult JoinResult = new MDCProcedureMethod().MDCProcedure(datasource,
                        SchemaName,
                        ProcedureList.get(y).trim(),
                        mdcWithoutZeros,
                        grouperparameter.getGender());
                if (JoinResult.isSuccess()) {
                    mdcprocedureCounter++;
                    MDCProcedure mdcProcedure = utility.objectMapper().readValue(JoinResult.getResult(), MDCProcedure.class);
                    DRGWSResult pdcresult = new GetPDC().GetPDC(datasource,
                            SchemaName,
                            mdcProcedure.getA_PDC(), drgResult.getMDC());
                    if (pdcresult.isSuccess()) {
                        PDC hiarresult = utility.objectMapper().readValue(pdcresult.getResult(), PDC.class);
                        hierarvalue.add(hiarresult.getHIERAR());
                        pdclist.add(hiarresult.getPDC());
                    }
                }
                if (new Endovasc().Endovasc(datasource,
                        SchemaName,
                        ProcedureList.get(y).trim(), "8PH", mdcWithoutZeros).isSuccess()) {
                    Counter8PH++;
                }
                if (new Endovasc().Endovasc(datasource, SchemaName, ProcedureList.get(y).trim(), "8QA", mdcWithoutZeros).isSuccess()) {
                    Counter8QA++;
                }
                //AX 99PDX Checking
                if (checkAX.AX(datasource, SchemaName, "99PDX", ProcedureList.get(y).trim()).isSuccess()) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (checkAX.AX(datasource, SchemaName, "99PCX", ProcedureList.get(y).trim()).isSuccess()) {
                    PCXCounter99++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PEX", ProcedureList.get(y).trim()).isSuccess()) {
                    CartProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PFX", ProcedureList.get(y).trim()).isSuccess()) {
                    CaCRxProc++;
                }
                if (checkAX.AX(datasource, SchemaName, "99PBX", ProcedureList.get(y).trim()).isSuccess()) {
                    PBX99Proc++;
                }
                if (checkAX.AX(datasource, SchemaName, "8PFX", ProcedureList.get(y).trim()).isSuccess()) {
                    Counter8PFX++;
                }
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
                        MDCCodeOptimize getMdcProc = this.mdcProcedure(
                                drgResult.getPDC(),
                                Counter8PH,
                                grouperparameter.getBirthDate(),
                                grouperparameter.getAdmissionDate(),
                                Counter8QA);
                        drgResult.setDC(getMdcProc.getDC());
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
                                Counter8PFX,
                                PBX99Proc,
                                grouperparameter.getBirthDate(),
                                grouperparameter.getAdmissionDate());
                        drgResult.setDC(dc);
                    }
                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("0833");
                    } else {
                        drgResult.setDC("0834");
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
                MDCCodeOptimize getMdcProc = this.mdcProcedure(
                        drgResult.getPDC(),
                        Counter8PH,
                        grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate(),
                        Counter8QA);
                drgResult.setDC(getMdcProc.getDC());
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
                        Counter8PFX,
                        PBX99Proc,
                        grouperparameter.getBirthDate(),
                        grouperparameter.getAdmissionDate());
                drgResult.setDC(dc);
            }
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(true);
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 08 DC Result Has Found");
            } else {
                result = getPCCLResult;
            }
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            logger.info("Executing MDC8 Method");
            logger.error("Error in MDC8 Method : {}", ex.getMessage(), ex);
        }
        return result;

    }

    private MDCCodeOptimize mdcProcedure(
            final String pdc,
            final Integer Counter8PH,
            final String bdate,
            final String admDate,
            final Integer Counter8QA) {
        MDCCodeOptimize result =utility.MDCCodeOptimize();
        result.setPDC("");
        result.setDC("");
        result.setSdxfinder("");
        //CHECKING FOR TRAUMA CODES
        switch (pdc) {
            case "8QE"://Plasmapheresis
                result.setDC("0835");
                break;
            case "8QF"://Multiple (>4) Wound Debridement
                result.setDC("0836");
                break;
            case "8QD":
                result.setDC("0801");
                break;
            case "8QB"://Multiple (2-4) Wound Debridement
                if (Counter8PH > 0) {
                    result.setDC("0828");
                } else {
                    result.setDC("0830");
                }
                break;
            case "8PD"://Spinal Fusion
                result.setDC("0805");
                break;
            case "8PW"://Total Hip Revision
                result.setDC("0824");
                break;
            case "8PF"://Amputation for Musculoskeletal & Connective Tissue Disorders
                result.setDC("0807");
                break;
            case "8PZ"://Partial Hip Revision
                result.setDC("0827");
                break;
            case "8PX"://Total Knee Revision
                result.setDC("0825");
                break;
            case "8PY"://Partial Knee Revision
                result.setDC("0826");
                break;
            case "8PA"://Hip Replacement
                result.setDC("0802");
                break;
            case "8PV"://Partial Hip Replacement
                result.setDC("0823");
                break;
            case "8PE"://Back & Neck Procedure Except Spinal Fusion
                result.setDC("0806");
                break;
            case "8PB"://Knee Replacement
                result.setDC("0803");
                break;
            case "8PC"://Other Major Joint Replacement & Limb Reattach of Lower/Upper Extremities
                result.setDC("0804");
                break;
            case "8PG"://Biopsies of Musculoskeletal & Connective Tissue Disorders
                result.setDC("0808");
                break;
            case "8PJ"://Hip and Femur Procedures Except Replacement
                if (utility.ComputeYear(bdate, admDate) > 17) {
                    result.setDC("0810");
                } else {
                    result.setDC("0811");
                }
                break;
            case "8PH"://Skin Graft Except Hand for MS&CT
                if (Counter8QA > 0) {
                    result.setDC("0829");
                } else {
                    result.setDC("0809");
                }
                break;
            case "8PK"://Knee Procedures Except Replacement
                result.setDC("0812");
                break;
            case "8PU"://Other Musculoskeletal System and Connective Tissue OR Procedures
                result.setDC("0822");
                break;
            case "8QA"://Wound Debridement for MS&CT
                result.setDC("0831");
                break;
            case "8PT"://Arthroscopy
                result.setDC("0821");
                break;
            case "8PL"://Shoulder, Elbow & Forearm Procedures Except Replacement
                result.setDC("0813");
                break;
            case "8PM"://Humerus, Tibia, Fibula & Ankle Procedures Except Replacement
                if (utility.ComputeYear(bdate, admDate) > 17) {
                    result.setDC("0814");
                } else {
                    result.setDC("0815");
                }
                break;
            case "8QC"://Reattachment of Finger
                result.setDC("0832");
                break;
            case "8PS"://Soft Tissue Procedures
                result.setDC("0820");
                break;
            case "8PQ"://Local Excision & Removal of Internal Fixation Devices of Hip & Femur
                result.setDC("0818");
                break;
            case "8PP"://Foot Procedures
                result.setDC("0817");
                break;
            case "8PR"://Local Excision & Removal of Internal Fixation Devices Exc Hip & Femur
                result.setDC("0819");
                break;
            case "8PN"://Wrist & Hand Procedures Except Replacement PDC 8PN 
                result.setDC("0816");
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
            final Integer Counter8PFX,
            final Integer PBX99Proc,
            final String bdate,
            final String admDate) {
        String dc = "";
        switch (pdc) {
            case "8E"://Pathological Fracture and Malignancy
                //Radio+Chemotherapy
                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                    dc = "0868";
                    //Chemotherapy
                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                    dc = "0869";
                    //Radiotherapy
                } else if (CartSDx > 0 && CartProc > 0) {
                    dc = "0870";
                } else if (Counter8PFX > 0) { //##Dx Procedure
                    dc = "0871";
                } else if (PBX99Proc > 0) {//Blood Transfusion
                    dc = "0872";
                } else {//Malignancy 
                    dc = "0854";
                }
                break;
            case "8A"://Fracture of Femur
                dc = "0850";
                break;
            case "8B"://Fracture of Hip and Pelvis
                dc = "0851";
                break;
            case "8C"://Sprain, Strain and Dislocation of Hip, Pelvis and Thigh
                dc = "0852";
                break;
            case "8D"://Osteomyelitis
                dc = "0853";
                break;
            case "8F"://Connective Tissue
                dc = "0855";
                break;
            case "8G"://Septic Arthritis
                dc = "0856";
                break;
            case "8H"://Medical Back Problems
                dc = "0857";
                break;
            case "8J"://Bone Disease and Specific Arthropathies
                dc = "0858";
                break;
            case "8K"://Nonspecific Arthropathies
                dc = "0859";
                break;
            case "8L"://Signs and Symptoms
                dc = "0860";
                break;
            case "8M"://Tendonitis, Myositis and Bursitis
                dc = "0861";
                break;
            case "8N"://Aftercare
                dc = "0862";
                break;
            case "8P"://Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                if (utility.ComputeYear(bdate,
                        admDate) > 17) {
                    dc = "0863";
                } else {
                    dc = "0864";
                }
                break;
            case "8Q"://Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                if (utility.ComputeYear(bdate,
                        admDate) > 17) {
                    dc = "0865";
                } else {
                    dc = "0866";
                }
                break;
            case "8R"://Other Musculoskeletal System and Connective Tissue Diagnoses
                dc = "0867";
                break;
            case "8S"://Major Connective Tissue Dx PDC 8S
                dc = "0873";
                break;
        }
        return dc;

    }

}
