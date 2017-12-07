/* 
 *  This code belongs to the Semantic Access and Retrieval (SAR) group of the 
 *  Information Systems Laboratory (ISL) of the 
 *  Institute of Computer Science (ICS) of the  
 *  Foundation for Research and Technology - Hellas (FORTH)
 *  Nobody is allowed to use, copy, distribute, or modify this work.
 *  It is published for reasons of research results reproducibility.
 *  (c) 2017 Semantic Access and Retrieval group, All rights reserved
 */

package gr.forth.ics.isl.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Sgo
 */
public class Utils {

    public static Double setDoublePrecsion(double num, int scale) {
        Double truncatedDouble = BigDecimal.valueOf(num)
                .setScale(scale, RoundingMode.HALF_UP)
                .doubleValue();

        return truncatedDouble;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static void saveObject(Object result, String fileName) throws FileNotFoundException, IOException {
        // Write to disk with FileOutputStream
        FileOutputStream f_out = new FileOutputStream("src/main/resources/savedObjects/" + fileName + ".data");

        // Write object with ObjectOutputStream
        ObjectOutputStream obj_out = new ObjectOutputStream(f_out);

        // Write object out to disk
        obj_out.writeObject(result);
    }

    public static Object getSavedObject(String fileName) throws FileNotFoundException, IOException, ClassNotFoundException {

        // Read from disk using FileInputStream
        FileInputStream f_in = new FileInputStream("src/main/resources/savedObjects/" + fileName + ".data");

        // Read object using ObjectInputStream
        ObjectInputStream obj_in
                = new ObjectInputStream(f_in);

        // Read an object
        Object expResult = obj_in.readObject();

        return expResult;
    }
}
