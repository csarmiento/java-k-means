package co.com.runtime.kmeans.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;
import co.com.runtime.kmeans.BalotoDataAnalysis;
import co.com.runtime.kmeans.KmeansCluster;
import co.com.runtime.kmeans.datatypes.Baloto;

public class BalotoDataAnalysisTest extends TestCase {
    public void testNeverEnds() {
        // File input = new File("D:/workspace_kepler/ExerciseIV6/baloto.txt");
        final File input = new File("baloto.txt");
        final String clusterSeparator = "---------------------------------";
        final File output = new File("clustersBaloto.txt");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(output));
            Map<KmeansCluster<Baloto>, Boolean> clusters = BalotoDataAnalysis
                    .doClustering(input, 10);
            bw.append(clusterSeparator);
            bw.newLine();
            for (KmeansCluster<Baloto> cluster : clusters.keySet()) {
                bw.append(cluster.toString());
                bw.append(clusterSeparator);
                bw.newLine();
            }
            System.out.println("File: " + output.getAbsolutePath() + " updated");
        } catch (IOException | InterruptedException e) {
            fail();
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    fail();
                    e.printStackTrace();
                }
            }
        }
    }
}
