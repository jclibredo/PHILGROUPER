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
 * @author MINOSUN
 */
@RequestScoped
public class GetMDC18 {

    public GetMDC18() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC18(final DataSource datasource, final DRGOutput drgResult, final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
        List<String> SecondaryList = Arrays.asList(grouperparameter.getSdx().split(","));
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        GrouperMethod gm = new GrouperMethod();
        try {
            //CHECKING FOR TRAUMA CODES
            ArrayList<String> sdxfinder = new ArrayList<>();
            int PDXCounter99 = 0;
            int PCXCounter99 = 0;
            int ORProcedureCounter = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proc = ProcedureList.get(x);
                //AX 99PDX Checking
                if (utility.isValid99PDX(proc)) {
                    PDXCounter99++;
                }
                //AX 99PCX Checking
                if (utility.isValid99PCX(proc)) {
                    PCXCounter99++;
                }
                DRGWSResult ORProcedureResult = gm.ORProcedure(datasource, proc);
                if (String.valueOf(ORProcedureResult.isSuccess()).equals("true")) {
                    ORProcedureCounter++;
                    ORProcedureCounterList.add(Integer.valueOf(ORProcedureResult.getResult()));
                }
            }
            int CounterSDxBX18 = 0;
            int CounterPDxBX18 = 0;
            String BX18 = "18BX";

            for (int a = 0; a < SecondaryList.size(); a++) {
                String SeconSDx = SecondaryList.get(a);
                DRGWSResult ResultBX18 = gm.AX(datasource, BX18, SeconSDx);
                if (String.valueOf(ResultBX18.isSuccess()).equals("true")) {
                    CounterSDxBX18++;
                }
            }
            DRGWSResult ResultPDxBX18 = gm.AX(datasource, BX18, grouperparameter.getPdx());
            if (String.valueOf(ResultPDxBX18.isSuccess()).equals("true")) {
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
                                        DRGWSResult sdxfinderResult = gm.AX(datasource, BX18, SecondaryList.get(x));
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
                                DRGWSResult sdxfinderResult = gm.AX(datasource, BX18, SecondaryList.get(x));
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
            result.setMessage("MDC 18 Done Checking");

        } catch (IOException ex) {
            result.setMessage(ex.toString());
            Logger.getLogger(GetMDC18.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

}
