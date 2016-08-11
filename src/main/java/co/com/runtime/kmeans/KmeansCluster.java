package co.com.runtime.kmeans;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import co.com.runtime.kmeans.interfaces.KmeansClusterItem;

public class KmeansCluster<T extends KmeansClusterItem<T>> {
    private T               centroid;
    private Map<T, Boolean> elements;

    public KmeansCluster(T centroid) {
        this.elements = new ConcurrentHashMap<>();
        this.centroid = centroid;

    }

    public void add(T elememt) {
        getElements().put(elememt, true);
    }

    /**
     * @return the centroid
     */
    public T getCentroid() {
        return centroid;
    }

    /**
     * Clears the elements of the clusters and fixes a new centroid
     *
     * @param newCentroid The new centroid of the cluster
     */
    public boolean clearCluster(T newCentroid) {
        boolean centroidChanged = !this.centroid.equals(newCentroid);
        this.elements.clear();
        this.centroid = newCentroid;
        return centroidChanged;
    }

    /**
     * @return the elements
     */
    public Map<T, Boolean> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Centroid: ");
        sb.append(centroid);
        sb.append(", Elements: ");
        sb.append(elements.size());
        sb.append(System.getProperty("line.separator"));
        for (T item : getElements().keySet()) {
            sb.append(item.toString());
            sb.append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
}
