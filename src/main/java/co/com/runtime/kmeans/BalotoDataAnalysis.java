package co.com.runtime.kmeans;

import co.com.runtime.kmeans.datatypes.Baloto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class BalotoDataAnalysis {

    private final static int MAX_THREADS = Runtime.getRuntime().availableProcessors();

    private static Logger logger = LogManager.getLogger();

    /**
     * Pseudo-random number generator
     */
    private static Random random;

    static {
        // Pseudo-random number generator seed
        random = new Random(System.currentTimeMillis());
    }

    /**
     * Load Baloto results from a file and stores in an array
     *
     * @param f Input file containing all the Baloto results
     * @return An array of Baloto results
     * @throws IOException
     */
    public static Baloto[] loadBalotoResults(File f) throws IOException {
        List<Baloto> resultList = new ArrayList<>();
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while (line != null) {
            resultList.add(new Baloto(line));
            line = br.readLine();
        }
        br.close();
        fr.close();

        Baloto[] results = resultList.toArray(new Baloto[resultList.size()]);

        shuffle(results);

        return results;
    }

    /**
     * Rearrange the elements of an array in random order.
     *
     * @param a an array
     */
    private static void shuffle(Object[] a) {
        int N = a.length;
        for (int i = 0; i < N; i++) {
            int r = i + uniform(N - i); // between i and N-1
            Object temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
    }

    /**
     * Returns an integer uniformly between 0 (inclusive) and n (exclusive).
     *
     * @param n max range value
     * @return an integer uniformly between 0 (inclusive) and n (exclusive).
     * @throws IllegalArgumentException if <tt>n <= 0</tt>
     */
    private static int uniform(int n) {
        if (n <= 0)
            throw new IllegalArgumentException("Parameter n must be positive");
        return random.nextInt(n);
    }

    public static Map<KmeansCluster<Baloto>, Boolean> doClustering
            (File input, int desiredClusterSize) throws IOException, InterruptedException {
        // Dummy baloto for mean calculation
        Baloto dummy = new Baloto();

        // Load the results
        Baloto[] results = loadBalotoResults(input);

        // Number of clusters K
        int numberOfClusters = results.length / desiredClusterSize;

        // Map to hold the clusters
        Map<KmeansCluster<Baloto>, Boolean> clusters = new ConcurrentHashMap<>();

        // Centroid selection method
        selectCentroids(dummy, results, numberOfClusters, clusters, 42.925);

        ForkJoinPool pool = new ForkJoinPool(MAX_THREADS);

        // Kmeans algorithm for clustering
        // Create sub-tasks for parallel execution
        executeTasks(pool, createTasks(results, clusters));
        waitUntillTasksEnd(pool);
        Double changedCentroidsPercentage = 100D;
        while (changedCentroidsPercentage > 0) {
            int changedCentroids = 0;
            for (KmeansCluster<Baloto> cluster : clusters.keySet()) {
                boolean centroidChanged = false;
                if (cluster.getElements().size() > 0) {
                    centroidChanged = cluster.clearCluster(dummy.mean(cluster
                            .getElements()));
                }
                changedCentroids += (centroidChanged) ? 1 : 0;
            }
            changedCentroidsPercentage = 100 * (double) changedCentroids /
                    (double) clusters.size();
            logger.info("Changed centroids: " + changedCentroids + " from "
                    + clusters.size() + " ("
                    + String.format("%.3f", changedCentroidsPercentage) + "%)");
            executeTasks(pool, createTasks(results, clusters));
            waitUntillTasksEnd(pool);
        }

        return clusters;
    }

    private static void selectCentroids(Baloto dummy, Baloto[] results,
                                        int numberOfClusters, Map<KmeansCluster<Baloto>, Boolean> clusters,
                                        double minDistanceFromOthers) {
        logger.info("Starting the selection of distant centroids");
        long startTime = System.currentTimeMillis();

        // From n objects calculate a point whose attribute values are average
        // of n-objects attribute values.so first initial centroid is average on
        // n-objects.
        Baloto mean = dummy.mean(results);
        clusters.put(new KmeansCluster<>(mean), true);
        logger.info("Firs centroid (mean): " + mean);

        // select next initial centroids from n-objects in such a way that the
        // Euclidean distance of that object is maximum from other selected
        // initial centroids.
        int i = 0;
        while (clusters.size() < numberOfClusters) {
            // Average distance from current keyword to cluster centroids
            Double avgDistance = 0D;
            for (KmeansCluster<Baloto> cluster : clusters.keySet()) {
                avgDistance += results[i].distance(cluster.getCentroid());
            }
            avgDistance = avgDistance / clusters.size();

            // Here is used the following heuristic: In a six dimensions space
            // limited by the range [0-45] the maximum Euclidean distance from
            // the origin is 110,22703842 = Math.sqrt(45*45*6), so we are going
            // to use points apart from other centroids by a minimum distance
            // from others
            if (avgDistance > minDistanceFromOthers) {
                clusters.put(new KmeansCluster<>(results[i]), true);
                logger.info("Centroid found! - distance: " + avgDistance
                        + " - position: " + i + " - Baloto: " + results[i]);
            }
            i++;
            if (i >= results.length) {
                i = 0;
            }
        }

        logger.info("Selection process of distant centroids ended. Time elapsed: "
                + (System.currentTimeMillis() - startTime)
                + ". Keywords analysed: " + i);
    }

    private static void waitUntillTasksEnd(ForkJoinPool pool) {
        while (pool.getActiveThreadCount() > 0)
            ;
    }

    private static List<ClusteringTask<Baloto>> createTasks(Baloto[] results, Map<KmeansCluster<Baloto>, Boolean> clusters) {
        int factor = results.length / MAX_THREADS;

        List<ClusteringTask<Baloto>> tasks = new ArrayList<>();
        int startIndex, endIndex = 0;
        for (int i = 0; i < MAX_THREADS; i++) {
            startIndex = i * factor;
            endIndex = (i + 1) * factor;
            tasks.add(new ClusteringTask<>(results, clusters, startIndex, endIndex));
        }
        if (endIndex < results.length) {
            tasks.add(new ClusteringTask<>(results, clusters, endIndex, results.length));
        }
        return tasks;
    }

    private static void executeTasks(ForkJoinPool pool, List<ClusteringTask<Baloto>> tasks) {
        for (ClusteringTask<Baloto> task : tasks) {
            pool.execute(task);
        }
    }

    public static void main(String[] args) {
        if (args.length >= 2) {
            final String clusterSeparator = "---------------------------------";

            File input = new File(args[0]);
            int desiredClusterSize = Integer.parseInt(args[1]);

            try {
                Map<KmeansCluster<Baloto>, Boolean> clusters = doClustering(input,
                        desiredClusterSize);
                logger.info(clusterSeparator);
                for (KmeansCluster<Baloto> cluster : clusters.keySet()) {
                    logger.info(cluster);
                    logger.info(clusterSeparator);
                }
            } catch (IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
