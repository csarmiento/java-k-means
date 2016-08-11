package co.com.runtime.kmeans;

import co.com.runtime.kmeans.interfaces.KmeansClusterItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.RecursiveAction;

/**
 * Represents a parallel task for the K-means algorithm for clustering, by
 * assigning a subset of items of the whole data set, to the nearest cluster.
 *
 * @param <T> A generic object that implements the <tt>KmeansClusterItem</tt>
 *            interface
 * @author Camilo Sarmiento
 * @see co.com.runtime.kmeans.interfaces.KmeansClusterItem
 */
public class ClusteringTask<T extends KmeansClusterItem<T>> extends RecursiveAction {
    private static final long serialVersionUID = 1L;

    private static Logger logger = LogManager.getLogger();

    private T[]                            completeDataSet;
    private Map<KmeansCluster<T>, Boolean> clusters;
    private int                            startIndex;
    private int                            endIndex;

    public ClusteringTask(T[] completeDataSet, Map<KmeansCluster<T>, Boolean> clusters, int startIndex, int endIndex) {
        this.completeDataSet = completeDataSet;
        this.clusters = clusters;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    protected void compute() {
        logger.info("Task started [" + startIndex + " ," + endIndex + "]");

        // Assign each object to the cluster with the minimum distance.
        for (int i = startIndex; i < endIndex; i++) {
            Double minDistance = null;
            KmeansCluster<T> nearestCluster = null;
            for (KmeansCluster<T> cluster : clusters.keySet()) {
                double distance = completeDataSet[i].distance(cluster.getCentroid());
                if (minDistance == null) {
                    minDistance = distance;
                    nearestCluster = cluster;
                } else {
                    if (minDistance > distance) {
                        minDistance = distance;
                        nearestCluster = cluster;
                    }
                }
            }
            if (nearestCluster != null) {
                nearestCluster.add(completeDataSet[i]);
            }
        }
        logger.info("Task ended [" + startIndex + " ," + endIndex + "]");
    }

}
