package co.com.runtime.kmeans;

import co.com.runtime.kmeans.datatypes.Keyword;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class DataAnalysis {

    private final static int MAX_THREADS = Runtime.getRuntime()
            .availableProcessors();

    private static Logger logger = LogManager.getLogger();

    /**
     * Pseudo-random number generator
     */
    private static Random random;

    /**
     * Words that can distort the semantic matching between keywords
     */
    private final static String[] cleanningSet = {"@", "#", "-", "*", "&",
            "%", ".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "_",
            "0f", "a", "in", "of", "for", "and", "the", "to", "at", "on", "or",
            "by"};
    private final static Set<String> removableStrings;

    static {
        //Pseudo-random number generator seed
        long seed = System.currentTimeMillis();
        random = new Random(seed);

        removableStrings = new TreeSet<>();
        removableStrings.addAll(Arrays.asList(cleanningSet));
    }

    /**
     * Method that creates a dictionary of the unique words in a keywords file
     *
     * @param f Input file containing all the keywords
     * @return A Map representing the unique words in a keywords file associated
     * with an Id
     * @throws IOException
     */
    public static Map<String, Integer> createPositionDictionary(File f) throws IOException {
        Map<String, Integer> dictionary = new HashMap<>();
        Integer position = 0;

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while (line != null) {
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
                String word = st.nextToken().toLowerCase();
                if (!removableStrings.contains(word)) {
                    Integer temp = dictionary.get(word);
                    if (temp == null) {
                        dictionary.put(word, position++);
                    }
                }
            }
            line = br.readLine();
        }
        br.close();
        fr.close();

        return dictionary;
    }

    /**
     * Method that creates a histogram of the words in a keywords file
     *
     * @param f Input file containing all the keywords
     * @return A Map representing the histogram of the words in a keywords file
     * @throws IOException
     */
    public static Map<String, Integer> createOccurrencesDictionary(File f)
            throws IOException {
        Map<String, Integer> dictionary = new HashMap<>();

        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while (line != null) {
            StringTokenizer st = new StringTokenizer(line);
            while (st.hasMoreTokens()) {
                String word = st.nextToken().toLowerCase();
                if (!removableStrings.contains(word)) {
                    Integer counter = dictionary.get(word);
                    if (counter != null) {
                        dictionary.put(word, counter + 1);
                    } else {
                        dictionary.put(word, 1);
                    }
                }
            }
            line = br.readLine();
        }
        br.close();
        fr.close();
        return dictionary;
    }

    /**
     * Load keywords from a file and stores in an array
     *
     * @param f Input file containing all the keywords
     * @return An array of Keyword objects
     * @throws IOException
     */
    public static Keyword[] loadKeywords(File f) throws IOException {
        List<Keyword> kwList = new ArrayList<>();
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);

        String line = br.readLine();
        while (line != null) {
            kwList.add(new Keyword(line));
            line = br.readLine();
        }
        br.close();
        fr.close();

        Keyword[] keywords = kwList.toArray(new Keyword[kwList.size()]);

        shuffle(keywords);

        return keywords;
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
     * Returns an integer uniformly between 0 (inclusive) and N (exclusive).
     *
     * @param N max range value
     * @return an integer uniformly between 0 (inclusive) and N (exclusive).
     * @throws IllegalArgumentException if <tt>N <= 0</tt>
     */
    private static int uniform(int N) {
        if (N <= 0)
            throw new IllegalArgumentException("Parameter N must be positive");
        return random.nextInt(N);
    }

    public static Map<KmeansCluster<Keyword>, Boolean> doClustering(File input,
                                                                    int desiredClusterSize) throws IOException, InterruptedException {
        // Dummy keyword for mean calculation
        Keyword dummy = new Keyword();

        // Load the keywords
        Keyword[] keywords = loadKeywords(input);

        // Number of clusters K
        int numberOfClusters = keywords.length / desiredClusterSize;

        // List to hold the clusters
        Map<KmeansCluster<Keyword>, Boolean> clusters = new ConcurrentHashMap<>();

        // TODO use a better centroid selection method
        selectCentroids(keywords, numberOfClusters, clusters, 89.9D);

        ForkJoinPool pool = new ForkJoinPool(MAX_THREADS);

        // Kmeans algorithm for clustering
        // Create sub-tasks for parallel execution
        executeTasks(pool, createTasks(keywords, clusters));
        waitUntillTasksEnd(pool);
        Double changedCentroidsPercentage = 100D;
        while (changedCentroidsPercentage > 2) {
            int changedCentroids = 0;
            for (KmeansCluster<Keyword> cluster : clusters.keySet()) {
                boolean centroidChanged = cluster.clearCluster(dummy
                        .mean(cluster.getElements()));
                changedCentroids += (centroidChanged) ? 1 : 0;
            }
            changedCentroidsPercentage = 100 * (double) changedCentroids
                    / (double) clusters.size();
            logger.info("Changed centroids: " + changedCentroids + " from "
                    + clusters.size() + " ("
                    + String.format("%.3f", changedCentroidsPercentage) + "%)");
            executeTasks(pool, createTasks(keywords, clusters));
            waitUntillTasksEnd(pool);
        }

        return clusters;
    }

    /**
     * Method that performs the selection of distant centroids based on a
     * minimum distance parameter as an heuristic to improve K-means algorithm
     *
     * @param keywords              The keywords to be analyzed
     * @param numberOfClusters      Desired number of clusters (K)
     * @param clusters              A Map where the clusters are going to be stored
     * @param minDistanceFromOthers Minimum distance between centroids
     */
    private static void selectCentroids(Keyword[] keywords,
                                        int numberOfClusters, Map<KmeansCluster<Keyword>, Boolean> clusters,
                                        double minDistanceFromOthers) {
        logger.info("Starting the selection of mid-distant centroids");
        long startTime = System.currentTimeMillis();
        clusters.put(new KmeansCluster<>(keywords[0]), true);

        // Try to select mid-distance centroids from each other
        int i = 0;
        while (i < keywords.length && clusters.size() < numberOfClusters) {
            // Average distance from current keyword to cluster centroids
            Double avgDistance = 0D;
            for (KmeansCluster<Keyword> cluster : clusters.keySet()) {
                avgDistance += keywords[i].distance(cluster.getCentroid());
            }
            avgDistance = avgDistance / clusters.size();
            // Just an heuristic to choose mid-distance centroids
            if (avgDistance > minDistanceFromOthers) {
                clusters.put(new KmeansCluster<>(keywords[i]), true);
                logger.info("Centroid found! - distance: " + avgDistance
                        + " - position: " + i + " - keyword: " + keywords[i]);
            }
            i++;
        }
        logger.info("Selection process of mid-distant centroids ended. Time elapsed: "
                + (System.currentTimeMillis() - startTime)
                + ". Keywords analysed: " + i);
    }

    private static void waitUntillTasksEnd(ForkJoinPool pool) {
        while (pool.getActiveThreadCount() > 0) ;
    }

    private static List<ClusteringTask<Keyword>> createTasks(
            Keyword[] keywords, Map<KmeansCluster<Keyword>, Boolean> clusters) {
        int factor = keywords.length / MAX_THREADS;

        List<ClusteringTask<Keyword>> tasks = new ArrayList<>();
        int startIndex, endIndex = 0;
        for (int i = 0; i < MAX_THREADS; i++) {
            startIndex = i * factor;
            endIndex = (i + 1) * factor;
            tasks.add(new ClusteringTask<>(keywords, clusters,
                    startIndex, endIndex));
        }
        if (endIndex < keywords.length) {
            tasks.add(new ClusteringTask<>(keywords, clusters, endIndex,
                    keywords.length));
        }
        return tasks;
    }

    private static void executeTasks(ForkJoinPool pool,
                                     List<ClusteringTask<Keyword>> tasks) {
        for (ClusteringTask<Keyword> task : tasks) {
            pool.execute(task);
        }
    }

    public static void main(String[] args) {
        if (args.length >= 2) {
            final String clusterSeparator = "---------------------------------";

            File input = new File(args[0]);
            int desiredClusterSize = Integer.parseInt(args[1]);

            try {
                Map<KmeansCluster<Keyword>, Boolean> clusters = doClustering(input,
                        desiredClusterSize);
                logger.info(clusterSeparator);
                for (KmeansCluster<Keyword> cluster : clusters.keySet()) {
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
