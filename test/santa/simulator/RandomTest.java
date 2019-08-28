/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package santa.simulator;

import java.util.ArrayList;
import java.io.FileWriter;

/**
 *
 * @author Bradley R. Jones
 */
public class RandomTest {
    
    private static final long SEED = 1989;
    
    public RandomTest() {
    }
    
    // Not a real test
    public ArrayList<Long> generatePoissons() {
        Random.setSeed(SEED);
        
        final long count = 1000;
        final double mean1 = 1.0;
        final double mean2 = 100000.0;
        final double mean3 = 0.1;
        
        ArrayList<Long> list = new ArrayList<Long>();
        
        System.out.println("Mean: " + mean1);
        for (long i = 0; i < count; i++)  
            list.add(Random.nextPoisson(mean1));
        System.out.println("Mean: " + mean2);
        for (long i = 0; i < count; i++)
            list.add(Random.nextPoisson(mean2));
        System.out.println("Mean: " + mean3);
        for (long i = 0; i < count; i++)
            list.add(Random.nextPoisson(mean3));
        
        return list;
    }
    
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                String filename = args[0];                
                FileWriter writer = new FileWriter(filename);
                
                RandomTest test = new RandomTest();
                ArrayList<Long> poissons = test.generatePoissons();
                
                for (long value: poissons) {
                    writer.write("" + value + "\n");
                }
                
                writer.close();
            } else {
                throw new Exception("Need to specify output filename.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
