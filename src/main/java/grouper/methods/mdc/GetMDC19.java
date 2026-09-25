/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.mdc;

import grouper.methods.validation.AX;
import grouper.structures.DRGOutput;
import grouper.structures.DRGWSResult;
import grouper.structures.GrouperParameter;
import grouper.utility.Utility;
import java.util.ArrayList;
import java.util.Arrays;
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
public class GetMDC19 {

    public GetMDC19() {
    }

    private final Utility utility = new Utility();

    public DRGWSResult GetMDC19(
            final DataSource datasource,
            final String SchemaName,
            final DRGOutput drgResult,
            final GrouperParameter grouperparameter) {
        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        try {
            List<String> ProcedureList = Arrays.asList(grouperparameter.getProc().split(","));
            AX checkAX = new AX();
            int Counter19PBX = 0;
            ArrayList<Integer> ORProcedureCounterList = new ArrayList<>();
            for (int x = 0; x < ProcedureList.size(); x++) {
                String proCs = ProcedureList.get(x).trim();
                DRGWSResult getAx = checkAX.AX(datasource, SchemaName, "19PBX", proCs);
                if (getAx.isSuccess()) {
                    ORProcedureCounterList.add(Integer.valueOf(getAx.getResult()));
                    Counter19PBX++;
                }
            }
            drgResult.setDC(this.principalDiag(drgResult.getPDC(), Counter19PBX));
//            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLResult(datasource, SchemaName, drgResult, grouperparameter);
            DRGWSResult getPCCLResult = new GetPCCLResult().GetPCCLJava(datasource, SchemaName, drgResult, grouperparameter);
            if (getPCCLResult.isSuccess()) {
                result.setSuccess(getPCCLResult.isSuccess());
                result.setResult(getPCCLResult.getResult());
                result.setMessage("MDC 19 Done Checking");
            } else {
                result = getPCCLResult;
            }
        } catch (NumberFormatException ex) {
            result.setMessage("Something went wrong");
            Logger.getLogger(GetMDC19.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;

    }

    private String principalDiag(
            final String pdc,
            final Integer Counter19PBX) {
        String dc = "";
        switch (pdc.toUpperCase()) {
            case "19A"://Acute Psychotic Disorders
                dc = Counter19PBX > 0 ? "1901" : "1950";
                break;
            case "19B"://Chronic Psychotic Disorders
                dc = Counter19PBX > 0 ? "1902" : "1951";
                break;
            case "19C"://Major Affective Disorders
                dc = Counter19PBX > 0 ? "1903" : "1952";
                break;
            case "19D"://Other Affect and Somatoform Disorders
                dc = "1953";
                break;
            case "19E"://Acute Reaction and Psychosocial Dysfunction
                dc = "1954";
                break;
            case "19F"://Anxiety Disorders
                dc = "1955";
                break;
            case "19G"://Eating and Obsessive Compulsive Disorders
                dc = "1956";
                break;
            case "19H"://Personality and Impulse Control Disorders
                dc = "1957";
                break;
            case "19J"://Childhood Mental Disorders
                dc = "1958";
                break;
            case "19K"://Organic Disturbance and Mental Retardation
                dc = "1959";
                break;
            case "19L"://Other Mental Disorders
                dc = "1960";
                break;
            case "19M"://Sexual Dysfunction PDC 19M
                dc = "1961";
                break;
        }
        return dc;
    }

}
