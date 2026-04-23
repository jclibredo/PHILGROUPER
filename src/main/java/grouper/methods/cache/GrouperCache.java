/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grouper.methods.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import grouper.structures.GrouperParameter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author LAPTOP
 */
public class GrouperCache {

    // This is where your data lives in memory
    private static List<GrouperParameter> cachedList = new ArrayList<>();

    public static void init() {
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File("D:\\REPORTS\\grouperparameter.json");
        try {
            // Your exact code snippet to load the data
            cachedList = mapper.readValue(
                    jsonFile,
                    new TypeReference<List<GrouperParameter>>() {
            }
            );
//            System.out.println("Successfully cached " + cachedList.size() + " records.");
        } catch (IOException e) {
//            System.err.println("Failed to load JSON to cache: " + e.getMessage());
        }
    }

    public static List<GrouperParameter> getData() {
        if (cachedList.isEmpty()) {
            init(); // Load it if it hasn't been loaded yet
        }
        return cachedList;
    }
}
