package co.com.runtime.kmeans.interfaces;

import java.util.Map;

/**
 * Contract to accomplish by objects to be clustered
 * 
 * @author Camilo Sarmiento
 * 
 * @param <T>
 *            Bounding Type Parameter
 */
public interface KmeansClusterItem<T> {
    /**
     * From: Topic Detection by Clustering Keywords <br/>
     * Authors: Christian Wartena and Rogier Brussee<br/>
     * Telematica Instituut, P.O. Box 589, 7500 AN Enschede, The Netherlands<br/>
     * <br/>
     * An effective way to define "similarity" between two elements is through a
     * metric d(i, j) between the elements i, j satisfying the usual axioms of
     * nonnegativity, identity of indiscernables and triangle inequality. Two
     * elements are more similar if they are closer. For this purpose any
     * monotone increasing function of a metric will suffice and we will call
     * such a function a distance function.
     * 
     * @param other
     *            The item against the distance is going to be calculated
     * @return The distance to another KmeansCluster Item
     */
    public double distance(T other);

    /**
     * Returns the mean Item among the Map
     * 
     * @param elements
     *            The elements Map
     * @return The mean Item
     */
    public T mean(Map<T, Boolean> elements);

    /**
     * Returns the mean Item among the array
     * 
     * @param elements
     *            The elements array
     * @return The mean Item
     */
    public T mean(T[] elements);
}
