package co.com.runtime.kmeans.test;

import co.com.runtime.kmeans.KmeansCluster;
import co.com.runtime.kmeans.DataAnalysis;
import co.com.runtime.kmeans.datatypes.Keyword;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class DataAnalysisTest extends TestCase {

    public void testNeverEnds() {
        File input = new File("IV6 - RAW keywords.txt");
        final String clusterSeparator = "---------------------------------\n";

        try {
            Map<KmeansCluster<Keyword>, Boolean> clusters = DataAnalysis
                    .doClustering(input, 15);
            System.out.print(clusterSeparator);
            for (KmeansCluster<Keyword> cluster : clusters.keySet()) {
                System.out.println(cluster);
                System.out.print(clusterSeparator);
            }
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        } catch (InterruptedException e) {
            fail();
            e.printStackTrace();
        }
    }

    public void testCreatePositionDictionary() {
        // File input = new File(
        // "D:/workspace_kepler/ExerciseIV6/IV6 - RAW keywords.txt");
        File input = new File("J:/iv/ExerciseIV6/IV6 - RAW keywords.txt");

        try {
            Map<String, Integer> dictionary = DataAnalysis
                    .createPositionDictionary(input);

            for (String word : dictionary.keySet()) {
                System.out.println(word + ", " + dictionary.get(word));
            }
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

    public void testCreateOccurrencesDictionary() {
        // File input = new File(
        // "D:/workspace_kepler/ExerciseIV6/IV6 - RAW keywords.txt");
        File input = new File("J:/iv/ExerciseIV6/IV6 - RAW keywords.txt");

        try {
            Map<String, Integer> dictionary = DataAnalysis
                    .createOccurrencesDictionary(input);

            for (String word : dictionary.keySet()) {
                System.out.println(word + ", " + dictionary.get(word));
            }
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }

    public void testLoadKeywords() {
        // File input = new File(
        // "D:/workspace_kepler/ExerciseIV6/IV6 - RAW keywords.txt");
        File input = new File("J:/iv/ExerciseIV6/IV6 - RAW keywords.txt");

        try {
            Keyword[] keywords = DataAnalysis.loadKeywords(input);
            for (Keyword keyword : keywords) {
                System.out.println(keyword);
            }
        } catch (IOException e) {
            fail();
            e.printStackTrace();
        }
    }
}
