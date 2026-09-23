/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.validation;

import grouper.structures.DRGWSResult;
import grouper.structures.ICD10PreMDCResult;
import grouper.utility.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;

/**
 *
 * @author LAPTOP
 */
@RequestScoped
public class DRGDetermination {

    private final Utility utility = new Utility();
//    private static final Logger logger = Logger.getLogger(DRGDetermination.class.getName());
    // Internal data structure to bind SDx attributes together during calculations

    private static class SdxItem {

        String code;
        String mainCc;
        int ccRow;
        int ccl;

        public SdxItem(String code) {
            this.code = code;
            this.mainCc = "";
            this.ccRow = 0;
            this.ccl = 0;
        }
    }

    public String CleanSDxDCDetermination(
            final DataSource datasource,
            final String SchemaName,
            final String sdxorig,
            final String sdxdc,
            final String pdx,
            final String dcs) {

        GetICD10PreMDC getI10 = new GetICD10PreMDC();
        GetCCLValue getCCLVal = new GetCCLValue();
        CheckExclusionList getExclu = new CheckExclusionList();
        GetDC getDC = new GetDC();

        try {
            String sdxoriglist = "";
            String sdxdcsfinder = Optional.ofNullable(sdxdc).orElse("");
            String sdxoriglis = Optional.ofNullable(sdxorig).orElse("");
            if (!sdxdcsfinder.equals("") && !sdxoriglis.equals("")) {
                // 1. Convert dataB into a Set of strings to exclude
                Set<String> excludeSet = Arrays.stream(sdxdcsfinder.split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet());

                // 2. Filter dataA by keeping elements NOT present in excludeSet
                String result = Arrays.stream(sdxoriglis.split(","))
                        .map(String::trim)
                        .filter(item -> !excludeSet.contains(item))
                        .collect(Collectors.joining(","));
                sdxoriglist = result;
            } else {
                sdxoriglist = sdxoriglis;
            }
            // 1. Parse comma-separated secondary diagnoses (SDX)
            List<SdxItem> sdxList = new ArrayList<>();
            if (sdxoriglist != null && !sdxoriglist.trim().isEmpty()) {
                for (String token : sdxoriglist.split(",")) {
                    String trimmed = token.trim();
                    if (!trimmed.isEmpty()) {
                        sdxList.add(new SdxItem(trimmed));
                    }
                }
            }
            // Cap list size to 12 items as specified in the Stata logic
//            if (sdxList.size() > 12) {
//                sdxList = sdxList.subList(0, 12);
//            }
            // If no SDX codes exist, return PCCL = 0
            if (sdxList.isEmpty()) {
                return "0";
            }
            // 2. Lookup Adjacent DRG / DC Column
            String dcCol = "";
            DRGWSResult detDcCol = getDC.GetDC(datasource, SchemaName, dcs);
            if (detDcCol.isSuccess()) {
                dcCol = detDcCol.getMessage();
            }
            // 3. Populate CCROW, MAINCC, and Initial CCL per SDX
            for (SdxItem item : sdxList) {
                DRGWSResult ccRowResult = getI10.GetICD10PreMDC(datasource, SchemaName, item.code);
                if (ccRowResult.isSuccess()) {
                    ICD10PreMDCResult premdc = utility.objectMapper().readValue(ccRowResult.getResult(), ICD10PreMDCResult.class);
                    item.ccRow = Integer.parseInt(ccRowResult.getMessage());
                    item.mainCc = Optional.ofNullable(premdc.getMainCC()).orElse(""); // Assuming getResult() holds MAINCC
                }
                item.ccl = getCCLVal.GetCCLValue(datasource, SchemaName, dcCol, String.valueOf(item.ccRow));
            }
            // 4. Initial Exclusion Check against Principal Diagnosis (PDX)
            sdxList.forEach((item) -> {
                DRGWSResult exclCheck = getExclu.CheckExclusionList(datasource, SchemaName, item.mainCc, pdx);
                if (exclCheck.isSuccess()) {
                    item.ccl = 0;
                }
            });
            // 5. First Sort: Descending CCL, then Descending Code String (Z-A)
            sortSdxList(sdxList);
            // 6. Pairwise Recursive Exclusion Matrix (SDx_i vs SDx_n)
            for (int i = 0; i < sdxList.size(); i++) {
                SdxItem itemI = sdxList.get(i);
                for (int n = i + 1; n < sdxList.size(); n++) {
                    SdxItem itemN = sdxList.get(n);
                    // Skip comparison if either CCL is already 0
                    if (itemI.ccl == 0 || itemN.ccl == 0) {
                        continue;
                    }
                    // Check if itemI excludes itemN using itemI's code and itemN's mainCc
                    DRGWSResult pairExclCheck = getExclu.CheckExclusionList(
                            datasource, SchemaName, itemI.code, itemN.mainCc);
                    if (pairExclCheck.isSuccess()) {
                        itemN.ccl = 0; // Reset excluded diagnosis CCL to 0
                    }
                }
            }
            // 7. Second Sort: Re-sort by updated CCL (descending) and Code (Z-A)
            sortSdxList(sdxList);
            // 8. Calculate Pre-PCCL Weight Sum
            double alpha = 0.4;
            double wsum = 0.0;
            for (int i = 0; i < sdxList.size(); i++) {
                int cclVal = sdxList.get(i).ccl;
                if (cclVal > 0) {
                    // Formula matches Stata: ccl * exp(-alpha * (i - 1)) [0-indexed in Java]
                    wsum += cclVal * Math.exp(-alpha * i);
                }
            }
            if (wsum <= 0) {
                return "0";
            }
            // 9. Logarithmic PCCL Scoring Step
            double num = Math.log(1.0 + wsum);
            double denom = Math.log(3.0 / alpha) / 4.0;
            long x = Math.round(num / denom);
            int pccl;
            if (x > 4) {
                pccl = 4;
            } else if (x < 0) {
                pccl = 0;
            } else {
                pccl = (int) x;
            }
            return String.valueOf(pccl);
        } catch (NumberFormatException | IOException ex) {
            return "Something went wrong";
        }
    }

    /**
     * Sorts the list by CCL descending, then SDX Code string descending (Z-A)
     */
    private void sortSdxList(List<SdxItem> list) {
        list.sort((a, b) -> {
            if (b.ccl != a.ccl) {
                return Integer.compare(b.ccl, a.ccl);
            }
            return b.code.compareTo(a.code);
        });
    }

}
