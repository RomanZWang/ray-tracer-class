package ray.renderer;

import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

import ray.math.Geometry;
import ray.brdf.BRDF;
import ray.material.Material;

/**
 * The base class for all path tracers
 */
public abstract class PathTracer extends DirectOnlyRenderer {

	/**
	 * Depth of recursive path tracing.
	 */
    protected int depthLimit = 5;

    /**
     * 1 if there is a background color
     * 0 if there is no background color (the background is black)
     */
    protected int backgroundIllumination = 1;

    /**
     * Set the depthLimit. This function will be called by the parser to
     * set the parameter value from scene configuration file.
     *
     * @param depthLimit the maximum recursive depth
     */
    public void setDepthLimit(int depthLimit) { this.depthLimit = depthLimit; }

    public void setBackgroundIllumination(int backgroundIllumination) { this.backgroundIllumination = backgroundIllumination; }

    @Override
    public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, Color outColor) {
        rayRadianceRecursive(scene, ray, sampler, sampleIndex, 0, outColor);
    }

    protected abstract void rayRadianceRecursive(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, int level, Color outColor);

    /**
     * Evaluates a Monte Carlo estimate of reflected radiance due to direct
     * and/or indirect illumination.
     *
     * @param scene scene to be rendered
     * @param outDir output direction of the light
     * @param iRec information about the intersection points
     * @param sampler the sampler is used to generate random numbers for
     *  sampling rays
     * @param sampleIndex index of the currently sampled ray in a pixel.
     * The index value will be used in
     *        {@link SampleGenerator#sample(int, int, Point2)} method.
     * @param level current recursive depth
     * @param outColor output radiance for each RGB channel
     */
    public void gatherIllumination(Scene scene, Vector3 outDir,
            IntersectionRecord iRec, SampleGenerator sampler,
            int sampleIndex, int level, Color outColor) {
    	// W4160 TODO (B)
    	//
        // This method computes a Monte Carlo estimate of reflected radiance due to direct and/or indirect
        // illumination.  It generates samples uniformly wrt. the projected solid angle measure:
        //
        //    f = brdf * radiance
        //    p = 1 / pi
        //    g = f / p = brdf * radiance * pi
    	// You need:
    	//   1. Generate a random incident direction according to proj solid angle
    	//      pdf is constant 1/pi
    	//   2. Recursively find incident radiance from that direction
    	//   3. Estimate the reflected radiance: brdf * radiance / pdf = pi * brdf * radiance
    	//
    	// Here you need to use Geometry.squareToPSAHemisphere that you implemented earlier in this function

      if(level <= depthLimit){
        // Generate seed
          Point2 seed = new Point2();
          sampler.sample(1, sampleIndex, seed);

          // Uniform sample reflection over PSA hemisphere
          Vector3 sampled_direction = new Vector3();
          Geometry.squareToPSAHemisphere(seed, sampled_direction);

          // Transform the sampled reflection to world view
          iRec.frame.frameToCanonical(sampled_direction);
          sampled_direction.normalize();

          // Create intersector ray
          Ray ray = new Ray(iRec.frame.o, sampled_direction);
          ray.makeOffsetRay();

          // Initialize intersection record
          IntersectionRecord current_intersection_record = new IntersectionRecord();

          Color current_irradiance = new Color();
          Material material = iRec.surface.getMaterial();
          BRDF brdf = material.getBRDF(iRec);
          double factor = brdf.pdf(iRec.frame, outDir, sampled_direction);

          boolean does_intersect = scene.getFirstIntersection(current_intersection_record, ray);
          boolean brdf_not_null = brdf!=null;
          if (does_intersect && brdf_not_null){
            // compute BRDF
            brdf.evaluate(iRec.frame, outDir, sampled_direction, current_irradiance);
            Color current_color = new Color(current_irradiance);
            Color next_irradiance = new Color();
            // recurse
            rayRadianceRecursive(scene, ray, sampler, sampleIndex, level + 1, next_irradiance);
            // scale current irrad and add to existing out color
            current_color.scale(next_irradiance);

            current_color.scale(Math.PI);
            outColor.set(current_color);

          }
      }
      else{
        outColor.set(0.0);
      }
    }
}
