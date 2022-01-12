package ray.renderer;

import ray.misc.Color;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

import ray.math.Geometry;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.IntersectionRecord;

/**
 * A renderer that computes radiance recursively. It assumes a maximum recursive
 * depth specified by {@link PathTracer#depthLimit}. Therefore, if the
 * light ray has been scattered by certain times, we assume all the energy has
 * been lost.
 */
public class BruteForcePathTracer extends PathTracer {

    /**
     * Evaluates the illumination component recursively, this method is called
     * by {@link PathTracer#rayRadiance(Scene, Ray, SampleGenerator, int, Color)} method.
     *
     * @param scene scene to be rendered
     * @param ray the recursive ray along which we compute the radiance
     * @param sampler the sampler is used to generate random numbers for sampling rays
     * @param sampleIndex index of the currently sampled ray in a pixel. The index value will be used in
     *        {@link SampleGenerator#sample(int, int, Point2)} method.
     * @param level current recursive depth
     * @param outColor output radiance for each RGB channel
     */
    protected void rayRadianceRecursive(Scene scene, Ray ray,
            SampleGenerator sampler, int sampleIndex, int level, Color outColor) {
    	// W4160 TODO (B)
    	//
        // Find the visible surface along the ray, then add emitted and reflected radiance
        // to get the resulting color.
    	//
    	// If the ray depth is less than the limit (depthLimit), you need
    	// 1) compute the emitted light radiance from the current surface if the surface is a light surface
    	// 2) reflected radiance from other lights and objects. You need recursively compute the radiance
    	//    hint: You need to call gatherIllumination(...) method.

    // initialize intersction record
    IntersectionRecord current_intersection_record = new IntersectionRecord();
    // Recursive condition: intersction exists and not past depth limit

    boolean isBeforeDepthLimit = level <= depthLimit;
    boolean doesIntersect = scene.getFirstIntersection(current_intersection_record, ray);
		if (isBeforeDepthLimit && doesIntersect){
				// initialize and obtain emit direction
				Vector3 incidentDir = new Vector3(ray.direction);
        incidentDir.scale(-1);

				// get emit radiance if the intersect surface emits
        outColor.set(0.0);
				if (current_intersection_record.surface.getMaterial().isEmitter()){
					emittedRadiance(current_intersection_record, incidentDir, outColor);
				}

				// Randomly sample
				Point2 seed = new Point2();
				sampler.sample(level, sampleIndex, seed);

				// compute reflect illumination
				Color refRadiance = new Color();
				gatherIllumination(scene, incidentDir, current_intersection_record, sampler, sampleIndex, level, refRadiance);

				// compute the total radiance
				outColor.add(refRadiance);
			}
			else{
				scene.getBackground().evaluate(ray.direction, outColor);
      }

    }
}
