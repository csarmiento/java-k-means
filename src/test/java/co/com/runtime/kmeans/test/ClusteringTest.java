package co.com.runtime.kmeans.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import co.com.runtime.kmeans.datatypes.Keyword;
import junit.framework.TestCase;

public class ClusteringTest extends TestCase {
    public void testFakeClustering1() {
        // File input = new File(
        // "D:/workspace_kepler/ExerciseIV6/IV6 - RAW keywords.txt");
        File input = new File("J:/iv/ExerciseIV6/IV6 - RAW keywords.txt");

        String centroidString = "rush university college of nursing";
        Keyword centroidKw = new Keyword(centroidString);
        System.out.println(centroidString);
        System.out.println("-------------");

        try {
            FileReader fr = new FileReader(input);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            Keyword otherKw;
            while (line != null) {
                otherKw = new Keyword(line);

                double distance = centroidKw.distance(otherKw);
                System.out.println(distance + "\t" + line);

                line = br.readLine();
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            fail();
            e.printStackTrace();
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

    public void testFakeClustering() {
        File input = new File(
                "D:/workspace_kepler/ExerciseIV6/IV6 - RAW keywords.txt");
        // File input = new File("J:/iv/ExerciseIV6/IV6 - RAW keywords.txt");

        String centroidString = "college university online colleges degree community school schools state california florida programs texas education law universities jobs nursing";
        Keyword centroidKw = new Keyword(centroidString);
        System.out.println(centroidString);
        System.out.println("-------------");

        try {
            FileReader fr = new FileReader(input);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            Keyword otherKw;
            while (line != null) {
                otherKw = new Keyword(line);

                double dissimilarity = centroidKw.distance(otherKw);
                if (dissimilarity < 68) {
                    System.out.println(dissimilarity + "\t" + line);
                }

                line = br.readLine();
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            fail();
            e.printStackTrace();
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }
}
