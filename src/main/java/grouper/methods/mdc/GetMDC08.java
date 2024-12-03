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
public class GetMDC08 {

    public GetMDC08() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC08(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
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
            //Checking SDx RadioTherapy and Chemotherapy
            int CartSDx = 0;
            int CaCRxSDx = 0;
            int CartProc = 0;
            int CaCRxProc = 0;
            int PBX99Proc = 0;
            int Counter8PFX = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                String Secon = SecondaryList.get(a);
                if (utility.isValid99BX(Secon.toUpperCase().trim())) {
                    CartSDx++;
                }
                if (utility.isValid99CX(Secon.toUpperCase().trim())) {
                    CaCRxSDx++;
                }
            }

            //Checking Procedure RadioTherapy and Chemotherapy
            for (int a = 0; a < ProcedureList.size(); a++) {
                String Proce = ProcedureList.get(a);
                //AX 99PDX Checking
                if (utility.isValid99PDX(Proce.trim())) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(Proce.trim())) {
                    PCXCounter99++;
                }

                if (utility.isValid99PEX(Proce.trim())) {
                    CartProc++;
                }
                if (utility.isValid99PFX(Proce.trim())) {
                    CaCRxProc++;
                }
                if (utility.isValid99PBX(Proce.trim())) { //Blood Transfusion AX 99PBX
                    PBX99Proc++;
                }
                DRGWSResult Result8PFX = gm.AX(datasource, "8PFX", Proce.trim());
                if (Result8PFX.isSuccess()) {
                    Counter8PFX++;
                }
            }

            //THIS AREA IS FOR CHECKING OF OR PROCEDURE
            int ORProcedureCounter = 0;
            int mdcprocedureCounter = 0;
            int Counter8PH = 0;
            int Counter8QA = 0;
            String PH8 = "8PH";
            String QA8 = "8QA";

            String PCX8 = "8PCX";
            int CounterPCX8 = 0;
            String PDX8 = "8PDX";
            int CounterPDX8 = 0;
            String PEX8 = "8PEX";
            int CounterPEX8 = 0;
            String CombinationResult = "";
            if (CounterPCX8 > 0 && CounterPDX8 > 0) {
                //CounterPCX8CounterPDX8 = "true";
                CombinationResult = "CounterPCX8CounterPDX8";
            } else if (CounterPCX8 > 0 && CounterPEX8 > 0) {
                //CounterPCX8CounterPEX8 = "true";
                CombinationResult = "CounterPCX8CounterPEX8";
            } else if (CounterPDX8 > 0 && CounterPEX8 > 0) {
                CombinationResult = "CounterPDX8CounterPEX8";
            } else if (CounterPCX8 > 1 || CounterPDX8 > 1) {
                CombinationResult = "PDC8ORPDX8";
            }

            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            ArrayList<Integer> hierarvalue = new ArrayList<>();
            ArrayList<String> pdclist = new ArrayList<>();

            //  String models = String.join(",", secondaryList);
            for (int y = 0; y < ProcedureList.size(); y++) {
                String procs = ProcedureList.get(y);

                DRGWSResult Result8PCX = gm.AX(datasource, PCX8, procs);
                if (String.valueOf(Result8PCX.isSuccess()).equals("true")) {
                    CounterPCX8++;
                }
                DRGWSResult Result8PDX = gm.AX(datasource, PDX8, procs);
                if (String.valueOf(Result8PDX.isSuccess()).equals("true")) {
                    CounterPDX8++;
                }
                DRGWSResult Result8PEX = gm.AX(datasource, PEX8, procs);
                if (String.valueOf(Result8PEX.isSuccess()).equals("true")) {
                    CounterPEX8++;
                }
                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, procs);
                if (String.valueOf(ORProcedureResult.isSuccess()).equals("true")) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
                DRGWSResult JoinResult = gm.MDCProcedure(datasource, procs, drgResult.getMDC());
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
                DRGWSResult PH8Result = gm.Endovasc(datasource, procs, PH8, drgResult.getMDC());
                if (String.valueOf(PH8Result.isSuccess()).equals("true")) {
                    Counter8PH++;
                }
                DRGWSResult QA8Result = gm.Endovasc(datasource, procs, QA8, drgResult.getMDC());
                if (String.valueOf(QA8Result.isSuccess()).equals("true")) {
                    Counter8QA++;
                }
            }
            //THIS AREA IS FOR CHECKING OF MDC PROCEDURE

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
                            case "8QE"://Plasmapheresis
                                drgResult.setDC("0835");
                                break;
                            case "8QF"://Multiple (>4) Wound Debridement
                                drgResult.setDC("0836");
                                break;
                            case "8QD":
                            case "CounterPCX8CounterPDX8":
                            case "CounterPCX8CounterPEX8":
                            case "CounterPDX8CounterPEX8":
                            case "PDC8ORPDX8":
                                drgResult.setDC("0801");
                                break;
                            case "8QB"://Multiple (2-4) Wound Debridement
                                if (Counter8PH > 0) {
                                    drgResult.setDC("0828");
                                } else {
                                    drgResult.setDC("0830");
                                }
                                break;
                            case "8PD"://Spinal Fusion
                                drgResult.setDC("0805");
                                break;
                            case "8PW"://Total Hip Revision
                                drgResult.setDC("0824");
                                break;
                            case "8PF"://Amputation for Musculoskeletal & Connective Tissue Disorders
                                drgResult.setDC("0807");
                                break;
                            case "8PZ"://Partial Hip Revision
                                drgResult.setDC("0827");
                                break;
                            case "8PX"://Total Knee Revision
                                drgResult.setDC("0825");
                                break;
                            case "8PY"://Partial Knee Revision
                                drgResult.setDC("0826");
                                break;
                            case "8PA"://Hip Replacement
                                drgResult.setDC("0802");
                                break;
                            case "8PV"://Partial Hip Replacement
                                drgResult.setDC("0823");
                                break;
                            case "8PE"://Back & Neck Procedure Except Spinal Fusion
                                drgResult.setDC("0806");
                                break;
                            case "8PB"://Knee Replacement
                                drgResult.setDC("0803");
                                break;
                            case "8PC"://Other Major Joint Replacement & Limb Reattach of Lower/Upper Extremities
                                drgResult.setDC("0804");
                                break;
                            case "8PG"://Biopsies of Musculoskeletal & Connective Tissue Disorders
                                drgResult.setDC("0808");
                                break;
                            case "8PJ"://Hip and Femur Procedures Except Replacement
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                                    drgResult.setDC("0810");
                                } else {
                                    drgResult.setDC("0811");
                                }
                                break;
                            case "8PH"://Skin Graft Except Hand for MS&CT
                                if (Counter8QA > 0) {
                                    drgResult.setDC("0829");
                                } else {
                                    drgResult.setDC("0809");
                                }
                                break;
                            case "8PK"://Knee Procedures Except Replacement
                                drgResult.setDC("0812");
                                break;
                            case "8PU"://Other Musculoskeletal System and Connective Tissue OR Procedures
                                drgResult.setDC("0822");
                                break;
                            case "8QA"://Wound Debridement for MS&CT
                                drgResult.setDC("0831");
                                break;
                            case "8PT"://Arthroscopy
                                drgResult.setDC("0821");
                                break;
                            case "8PL"://Shoulder, Elbow & Forearm Procedures Except Replacement
                                drgResult.setDC("0813");
                                break;
                            case "8PM"://Humerus, Tibia, Fibula & Ankle Procedures Except Replacement
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) > 17) {
                                    drgResult.setDC("0814");
                                } else {
                                    drgResult.setDC("0815");
                                }
                                break;
                            case "8QC"://Reattachment of Finger
                                drgResult.setDC("0832");
                                break;
                            case "8PS"://Soft Tissue Procedures
                                drgResult.setDC("0820");
                                break;
                            case "8PQ"://Local Excision & Removal of Internal Fixation Devices of Hip & Femur
                                drgResult.setDC("0818");
                                break;
                            case "8PP"://Foot Procedures
                                drgResult.setDC("0817");
                                break;
                            case "8PR"://Local Excision & Removal of Internal Fixation Devices Exc Hip & Femur
                                drgResult.setDC("0819");
                                break;
                            case "8PN"://Wrist & Hand Procedures Except Replacement PDC 8PN 
                                drgResult.setDC("0816");
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
                            case "8E"://Pathological Fracture and Malignancy
                                //Radio+Chemotherapy
                                if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0868");
                                    //Chemotherapy
                                } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                                    drgResult.setDC("0869");
                                    //Radiotherapy
                                } else if (CartSDx > 0 && CartProc > 0) {
                                    drgResult.setDC("0870");
                                } else if (Counter8PFX > 0) { //##Dx Procedure
                                    drgResult.setDC("0871");
                                } else if (PBX99Proc > 0) {//Blood Transfusion
                                    drgResult.setDC("0872");
                                } else {//Malignancy 
                                    drgResult.setDC("0854");
                                }
                                break;
                            case "8A"://Fracture of Femur
                                drgResult.setDC("0850");
                                break;
                            case "8B"://Fracture of Hip and Pelvis
                                drgResult.setDC("0851");
                                break;
                            case "8C"://Sprain, Strain and Dislocation of Hip, Pelvis and Thigh
                                drgResult.setDC("0852");
                                break;
                            case "8D"://Osteomyelitis
                                drgResult.setDC("0853");
                                break;
                            case "8F"://Connective Tissue
                                drgResult.setDC("0855");
                                break;
                            case "8G"://Septic Arthritis
                                drgResult.setDC("0856");
                                break;
                            case "8H"://Medical Back Problems
                                drgResult.setDC("0857");
                                break;
                            case "8J"://Bone Disease and Specific Arthropathies
                                drgResult.setDC("0858");
                                break;
                            case "8K"://Nonspecific Arthropathies
                                drgResult.setDC("0859");
                                break;
                            case "8L"://Signs and Symptoms
                                drgResult.setDC("0860");
                                break;
                            case "8M"://Tendonitis, Myositis and Bursitis
                                drgResult.setDC("0861");
                                break;
                            case "8N"://Aftercare
                                drgResult.setDC("0862");
                                break;
                            case "8P"://Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) > 17) {
                                    drgResult.setDC("0863");
                                } else {
                                    drgResult.setDC("0864");
                                }
                                break;
                            case "8Q"://Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                                if (utility.ComputeYear(grouperparameter.getBirthDate(),
                                        grouperparameter.getAdmissionDate()) > 17) {
                                    drgResult.setDC("0865");
                                } else {
                                    drgResult.setDC("0866");
                                }
                                break;
                            case "8R"://Other Musculoskeletal System and Connective Tissue Diagnoses
                                drgResult.setDC("0867");
                                break;
                            case "8S"://Major Connective Tissue Dx PDC 8S
                                drgResult.setDC("0873");
                                break;
                        }
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
                switch (pdclist.get(hierarvalue.indexOf(min))) {
                    case "8QE"://Plasmapheresis
                        drgResult.setDC("0835");
                        break;
                    case "8QF"://Multiple (>4) Wound Debridement
                        drgResult.setDC("0836");
                        break;
                    case "8QD":
                    case "CounterPCX8CounterPDX8":
                    case "CounterPCX8CounterPEX8":
                    case "CounterPDX8CounterPEX8":
                    case "PDC8ORPDX8":
                        drgResult.setDC("0801");
                        break;
                    case "8QB"://Multiple (2-4) Wound Debridement
                        if (Counter8PH > 0) {
                            drgResult.setDC("0828");
                        } else {
                            drgResult.setDC("0830");
                        }
                        break;
                    case "8PD"://Spinal Fusion
                        drgResult.setDC("0805");
                        break;
                    case "8PW"://Total Hip Revision
                        drgResult.setDC("0824");
                        break;
                    case "8PF"://Amputation for Musculoskeletal & Connective Tissue Disorders
                        drgResult.setDC("0807");
                        break;
                    case "8PZ"://Partial Hip Revision
                        drgResult.setDC("0827");
                        break;
                    case "8PX"://Total Knee Revision
                        drgResult.setDC("0825");
                        break;
                    case "8PY"://Partial Knee Revision
                        drgResult.setDC("0826");
                        break;
                    case "8PA"://Hip Replacement
                        drgResult.setDC("0802");
                        break;
                    case "8PV"://Partial Hip Replacement
                        drgResult.setDC("0823");
                        break;
                    case "8PE"://Back & Neck Procedure Except Spinal Fusion
                        drgResult.setDC("0806");
                        break;
                    case "8PB"://Knee Replacement
                        drgResult.setDC("0803");
                        break;
                    case "8PC"://Other Major Joint Replacement & Limb Reattach of Lower/Upper Extremities
                        drgResult.setDC("0804");
                        break;
                    case "8PG"://Biopsies of Musculoskeletal & Connective Tissue Disorders
                        drgResult.setDC("0808");
                        break;
                    case "8PJ"://Hip and Femur Procedures Except Replacement
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                            drgResult.setDC("0810");
                        } else {
                            drgResult.setDC("0811");
                        }
                        break;
                    case "8PH"://Skin Graft Except Hand for MS&CT
                        if (Counter8QA > 0) {
                            drgResult.setDC("0829");
                        } else {
                            drgResult.setDC("0809");
                        }
                        break;
                    case "8PK"://Knee Procedures Except Replacement
                        drgResult.setDC("0812");
                        break;
                    case "8PU"://Other Musculoskeletal System and Connective Tissue OR Procedures
                        drgResult.setDC("0822");
                        break;
                    case "8QA"://Wound Debridement for MS&CT
                        drgResult.setDC("0831");
                        break;
                    case "8PT"://Arthroscopy
                        drgResult.setDC("0821");
                        break;
                    case "8PL"://Shoulder, Elbow & Forearm Procedures Except Replacement
                        drgResult.setDC("0813");
                        break;
                    case "8PM"://Humerus, Tibia, Fibula & Ankle Procedures Except Replacement
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                            drgResult.setDC("0814");
                        } else {
                            drgResult.setDC("0815");
                        }
                        break;
                    case "8QC"://Reattachment of Finger
                        drgResult.setDC("0832");
                        break;
                    case "8PS"://Soft Tissue Procedures
                        drgResult.setDC("0820");
                        break;
                    case "8PQ"://Local Excision & Removal of Internal Fixation Devices of Hip & Femur
                        drgResult.setDC("0818");
                        break;
                    case "8PP"://Foot Procedures
                        drgResult.setDC("0817");
                        break;
                    case "8PR"://Local Excision & Removal of Internal Fixation Devices Exc Hip & Femur
                        drgResult.setDC("0819");
                        break;
                    case "8PN"://Wrist & Hand Procedures Except Replacement PDC 8PN 
                        drgResult.setDC("0816");
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
                    case "8E"://Pathological Fracture and Malignancy
                        //Radio+Chemotherapy
                        if (CartSDx > 0 && CaCRxSDx > 0 && CartProc > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0868");
                            //Chemotherapy
                        } else if (CaCRxSDx > 0 && CaCRxProc > 0) {
                            drgResult.setDC("0869");
                            //Radiotherapy
                        } else if (CartSDx > 0 && CartProc > 0) {
                            drgResult.setDC("0870");
                        } else if (Counter8PFX > 0) { //##Dx Procedure
                            drgResult.setDC("0871");
                        } else if (PBX99Proc > 0) {//Blood Transfusion
                            drgResult.setDC("0872");
                        } else {//Malignancy 
                            drgResult.setDC("0854");
                        }
                        break;
                    case "8A"://Fracture of Femur
                        drgResult.setDC("0850");
                        break;
                    case "8B"://Fracture of Hip and Pelvis
                        drgResult.setDC("0851");
                        break;
                    case "8C"://Sprain, Strain and Dislocation of Hip, Pelvis and Thigh
                        drgResult.setDC("0852");
                        break;
                    case "8D"://Osteomyelitis
                        drgResult.setDC("0853");
                        break;
                    case "8F"://Connective Tissue
                        drgResult.setDC("0855");
                        break;
                    case "8G"://Septic Arthritis
                        drgResult.setDC("0856");
                        break;
                    case "8H"://Medical Back Problems
                        drgResult.setDC("0857");
                        break;
                    case "8J"://Bone Disease and Specific Arthropathies
                        drgResult.setDC("0858");
                        break;
                    case "8K"://Nonspecific Arthropathies
                        drgResult.setDC("0859");
                        break;
                    case "8L"://Signs and Symptoms
                        drgResult.setDC("0860");
                        break;
                    case "8M"://Tendonitis, Myositis and Bursitis
                        drgResult.setDC("0861");
                        break;
                    case "8N"://Aftercare
                        drgResult.setDC("0862");
                        break;
                    case "8P"://Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                            drgResult.setDC("0863");
                        } else {
                            drgResult.setDC("0864");
                        }
                        break;
                    case "8Q"://Fracture, Sprain, Strain and Dislocation of Forearm, Hand and Foot
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 17) {
                            drgResult.setDC("0865");
                        } else {
                            drgResult.setDC("0866");
                        }
                        break;
                    case "8R"://Other Musculoskeletal System and Connective Tissue Diagnoses
                        drgResult.setDC("0867");
                        break;
                    case "8S"://Major Connective Tissue Dx PDC 8S
                        drgResult.setDC("0873");
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

            result.setMessage("MDC 08 Done Checking");
            result.setResult(utility.objectMapper().writeValueAsString(drgResult));
        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC08.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
