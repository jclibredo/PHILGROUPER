/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
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
public class GetMDC18 {

    public GetMDC18() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC18(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
            //CHECKING FOR TRAUMA CODES
            ArrayList<String> sdxfinder = new ArrayList<>();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int ORProcedureCounter = 0;
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
                DRGWSResult ORProcedureResult = new GrouperMethod().ORProcedure(datasource, ProcedureList.get(x).trim());
                if (ORProcedureResult.isSuccess()) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }
            int CounterSDxBX18 = 0;
            int CounterPDxBX18 = 0;
            for (int a = 0; a < SecondaryList.size(); a++) {
                if (new GrouperMethod().AX(datasource, "18BX", SecondaryList.get(a).trim()).isSuccess()) {
                    CounterSDxBX18++;
                }
            }
            if (new GrouperMethod().AX(datasource, "18BX", grouperparameter.getPdx()).isSuccess()) {
                CounterPDxBX18++;
            }
            //CONDITIONAL STATEMENT WILL START THIS AREA FOR MDC 16
            if (PDXCounter99 > 0) { //CHECK FOR TRACHEOSTOMY 
                if (utility.ComputeLOS(grouperparameter.getAdmissionDate(),
                        utility.Convert24to12(grouperparameter.getTimeAdmission()),
                        grouperparameter.getDischargeDate(),
                        utility.Convert24to12(grouperparameter.getTimeDischarge())) < 21) {
                    if (ORProcedureCounter > 0) {
                        switch (Collections.max(ORProcedureCounterList)) {
                            case 6://OR Proc Level 6
                                drgResult.setDC("1806");
                                break;
                            case 5://OR Proc Level 5
                                drgResult.setDC("1805");
                                break;
                            case 4://OR Proc Level 4
                                drgResult.setDC("1804");
                                break;
                            case 3://OR Proc Level 3
                                drgResult.setDC("1803");
                                break;
                            case 2://OR Proc Level 2
                                drgResult.setDC("1802");
                                break;
                            case 1://OR Proc Level 1
                                drgResult.setDC("1801");
                                break;
                        }
                    } else {
                        switch (drgResult.getPDC()) {
                            case "18A"://Septicemia
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                    if (grouperparameter.getDischargeType().equals("4")) {
                                        drgResult.setDC("1872");
                                    } else {
                                        drgResult.setDC("1850");
                                    }
                                } else {
                                    drgResult.setDC("1851");
                                }
                                break;
                            case "18B"://Postop & Posttraumatic Infections
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 54) {
                                    drgResult.setDC("1852");
                                } else {
                                    drgResult.setDC("1853");
                                }
                                break;
                            case "18C"://Malaria
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                    drgResult.setDC("1854");
                                } else {
                                    drgResult.setDC("1855");
                                }
                                break;
                            case "18D"://CounterPDxBX18
                                if (CounterSDxBX18 > 0 || CounterPDxBX18 > 0) {
                                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                        drgResult.setDC("1870");
                                    } else {
                                        drgResult.setDC("1871");
                                    }
                                    for (int x = 0; x < SecondaryList.size(); x++) {
                                        if (new GrouperMethod().AX(datasource, "18BX", SecondaryList.get(x).trim()).isSuccess()) {
                                            sdxfinder.add(SecondaryList.get(x));
                                        }
                                    }
                                    if (!sdxfinder.isEmpty()) {
                                        drgResult.setSDXFINDER(String.join(",", sdxfinder));
                                    }

                                } else {
                                    if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                        drgResult.setDC("1856");
                                    } else {
                                        drgResult.setDC("1857");
                                    }
                                }
                                break;

                            case "18E"://Fever of Unknown Origin
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                    drgResult.setDC("1858");
                                } else {
                                    drgResult.setDC("1859");
                                }
                                break;
                            case "18F"://Viral Illness Except Dengue
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                    drgResult.setDC("1860");
                                } else {
                                    drgResult.setDC("1861");
                                }
                                break;
                            case "18G"://Fungal Diseases
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                    drgResult.setDC("1862");
                                } else {
                                    drgResult.setDC("1863");
                                }
                                break;
                            case "18H"://Other Infectious & Parasitic Diseases
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                    drgResult.setDC("1864");
                                } else {
                                    drgResult.setDC("1865");
                                }
                                break;
                            case "18J"://Melioidosis
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                    drgResult.setDC("1866");
                                } else {
                                    drgResult.setDC("1867");
                                }
                                break;
                            case "18K"://Leptospirosis
                                if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                    drgResult.setDC("1868");
                                } else {
                                    drgResult.setDC("1869");
                                }
                                break;
                        }
                    }

                } else {
                    if (PCXCounter99 > 0) {
                        drgResult.setDC("1808");
                    } else {
                        drgResult.setDC("1809");
                    }
                }

            } else if (ORProcedureCounter > 0) {
                switch (Collections.max(ORProcedureCounterList)) {
                    case 6://OR Proc Level 6
                        drgResult.setDC("1806");
                        break;
                    case 5://OR Proc Level 5
                        drgResult.setDC("1805");
                        break;
                    case 4://OR Proc Level 4
                        drgResult.setDC("1804");
                        break;
                    case 3://OR Proc Level 3
                        drgResult.setDC("1803");
                        break;
                    case 2://OR Proc Level 2
                        drgResult.setDC("1802");
                        break;
                    case 1://OR Proc Level 1
                        drgResult.setDC("1801");
                        break;
                }

                //==================================================================               
            } else {
                switch (drgResult.getPDC()) {
                    case "18A"://Septicemia
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                            if (grouperparameter.getDischargeType().equals("4")) {
                                drgResult.setDC("1872");
                            } else {
                                drgResult.setDC("1850");
                            }
                        } else {
                            drgResult.setDC("1851");
                        }
                        break;
                    case "18B"://Postop & Posttraumatic Infections
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 54) {
                            drgResult.setDC("1852");
                        } else {
                            drgResult.setDC("1853");
                        }
                        break;
                    case "18C"://Malaria
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                            drgResult.setDC("1854");
                        } else {
                            drgResult.setDC("1855");
                        }
                        break;
                    case "18D"://CounterPDxBX18
                        if (CounterSDxBX18 > 0 || CounterPDxBX18 > 0) {
                            if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                drgResult.setDC("1870");
                            } else {
                                drgResult.setDC("1871");
                            }

                            for (int x = 0; x < SecondaryList.size(); x++) {
                                DRGWSResult sdxfinderResult = new GrouperMethod().AX(datasource, "18BX", SecondaryList.get(x).trim());
                                if (sdxfinderResult.isSuccess()) {
                                    sdxfinder.add(SecondaryList.get(x));
                                }
                            }
                            if (!sdxfinder.isEmpty()) {
                                drgResult.setSDXFINDER(String.join(",", sdxfinder));
                            }
                        } else {
                            if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                                drgResult.setDC("1856");
                            } else {
                                drgResult.setDC("1857");
                            }
                        }
                        break;
                    case "18E"://Fever of Unknown Origin
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                            drgResult.setDC("1858");
                        } else {
                            drgResult.setDC("1859");
                        }
                        break;
                    case "18F"://Viral Illness Except Dengue
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                            drgResult.setDC("1860");
                        } else {
                            drgResult.setDC("1861");
                        }
                        break;
                    case "18G"://Fungal Diseases
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                            drgResult.setDC("1862");
                        } else {
                            drgResult.setDC("1863");
                        }
                        break;
                    case "18H"://Other Infectious & Parasitic Diseases
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                            drgResult.setDC("1864");
                        } else {
                            drgResult.setDC("1865");
                        }
                        break;
                    case "18J"://Melioidosis
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                            drgResult.setDC("1866");
                        } else {
                            drgResult.setDC("1867");
                        }
                        break;
                    case "18K"://Leptospirosis
                        if (utility.ComputeYear(grouperparameter.getBirthDate(), grouperparameter.getAdmissionDate()) > 14) {
                            drgResult.setDC("1868");
                        } else {
                            drgResult.setDC("1869");
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
                            DRGWSResult drgvalues = new GrouperMethod().ValidatePCCL(datasource, drgResult.getDC(), finaldrgresult.getDRG());
                            if (drgvalues.isSuccess()) {
                                drgResult.setDRG(drgResult.getDC() + drgvalues.getResult());
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
            result.setMessage("MDC 18 Done Checking");
        } catch (IOException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC18.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
