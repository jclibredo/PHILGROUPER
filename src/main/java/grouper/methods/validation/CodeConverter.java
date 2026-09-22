/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.utility.Utility;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import oracle.jdbc.OracleTypes;

/**
 *
 * @author LAPTOP
 */
@RequestScoped
public class CodeConverter {

    public CodeConverter() {
    }
    private final Utility utility = new Utility();

    public DRGWSResult RVSCodeConverter(
            final DataSource datasource,
            final String dynamicSchema,
            final String rvsCodes) {

        DRGWSResult result = utility.DRGWSResult();
        result.setMessage("");
        result.setResult("");
        result.setSuccess(false);
        if (rvsCodes == null || rvsCodes.trim().isEmpty()) {
            result.setSuccess(true);
            return result;
        }
        String callSql = String.format("begin :converter := %s.DRGPKGFUNCTION.GET_CONVERTER(:rvs_code); end;", dynamicSchema);
        List<String> rawIcd9cmList = new ArrayList<>();
        try (Connection connection = datasource.getConnection()) {
            String[] codes = rvsCodes.split(",");
            for (String code : codes) {
                String trimmedCode = code.trim();
                if (trimmedCode.isEmpty()) {
                    continue;
                }
                // Prepare statement per iteration to prevent Oracle Cursor reuse conflicts
                try (CallableStatement statement = connection.prepareCall(callSql)) {
                    statement.registerOutParameter("converter", OracleTypes.CURSOR);
                    statement.setString("rvs_code", trimmedCode);
                    statement.execute();
                    try (ResultSet resultSet = (ResultSet) statement.getObject("converter")) {
                        while (resultSet != null && resultSet.next()) {
                            String icd9Code = resultSet.getString("ICD9CODE");
                            if (icd9Code != null && !icd9Code.trim().isEmpty()) {
                                rawIcd9cmList.add(icd9Code.trim());
                            }
                        }
                    }
                }
            }
            // Convert list to a comma-separated string (e.g. "8332,8382,8669,8332,8669,8669")
            String rawResultString = String.join(",", rawIcd9cmList);
            // Apply Option B post-processing transformation
            String formattedResultString = formatICD9Result(rawResultString);
            result.setSuccess(true);
            result.setMessage(rawResultString);
            result.setResult(formattedResultString);
        } catch (Exception ex) {
            result.setMessage("Something went wrong: " + ex.getMessage());
            Logger.getLogger(CodeConverter.class.getName()).log(Level.SEVERE, "Error converting RVS codes", ex);
        }

        return result;
    }

    /**
     * OPTION B HELPER: Takes raw comma-separated string:
     * "8332,8382,8669,8332,8669,8669" Produces grouped output string:
     * "8332,8382,8669,8332>1,8669>1"
     *
     * @param rawResult
     * @return
     */
    public String formatICD9Result(String rawResult) {
        if (rawResult == null || rawResult.trim().isEmpty()) {
            return "";
        }
        String[] items = rawResult.split(",");
        // Step 1: Count frequency while preserving first-appearance order
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String item : items) {
            String code = item.trim();
            if (!code.isEmpty()) {
                counts.put(code, counts.getOrDefault(code, 0) + 1);
            }
        }
        List<String> outputList = new ArrayList<>();
        // Step 2: For each code, add the bare code, and add CODE>1 if it appeared > 1 time
        counts.entrySet().forEach((entry) -> {
            String code = entry.getKey();
            int totalOccurrences = entry.getValue();
            // Always add the first (bare) instance
            outputList.add(code);
            // If it appeared 2 or more times, add the >1 paired instance
            if (totalOccurrences > 1) {
                outputList.add(code + ">1");
            }
        });
        return String.join(",", outputList);
    }

}
