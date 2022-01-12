package ray.renderer;

import ray.material.Material;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.LuminaireSamplingRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

/**
 * A renderer that computes radiance due to emitted and directly reflected light only.
 *
 * @author cxz (at Columbia)
 */
public class DirectOnlyRenderer implements Renderer {

    /**
     * This is the object that is responsible for computing direct illumination.
     */
    DirectIlluminator direct = null;

    /**
     * The default is to compute using uninformed sampling w.r.t. projected solid angle over the hemisphere.
     */
    public DirectOnlyRenderer() {
        this.direct = new ProjSolidAngleIlluminator();
    }

    /**
     * This allows the rendering algorithm to be selected from the input file by substituting an instance
     * of a different class of DirectIlluminator.
     *
     * @param direct  the object that will be used to compute direct illumination
     */
    public void setDirectIlluminator(DirectIlluminator direct) {
        this.direct = direct;
    }

    /**
     * Evaluates the direct illumination component (first-order component) of a radiance arriving
     * to the camera along the given {@link Ray}.
     *
     * @param scene Scene to be rendered
     * @param ray the camera ray along which we compute the radiance
     * @param sampler The sampler is used to generate random numbers for sampling rays
     * @param sampleIndex index of the currently sampled ray in a pixel. The index value will be used in
     *        {@link SampleGenerator#sample(int, int, Point2)} method.
     * @param outColor output radiance for each RGB channel
     */
    public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler, int sampleIndex, Color outColor) {
    	// W4160 TODO (A)
    	// In this function, you need to implement your direct illumination rendering algorithm
    	//
    	// you need:
    	// 1) compute the emitted light radiance from the current surface if the surface is a light surface
    	// 2) direct reflected radiance from other lights. This is by implementing the function
    	//    ProjSolidAngleIlluminator.directIlluminaiton(...), and call direct.directIllumination(...) in this
    	//    function here.

      IntersectionRecord current_intersection_record = new IntersectionRecord();
      // Check if ray intersects anything. Store intersection in current_intersection_record.
      if (scene.getFirstIntersection(current_intersection_record, ray)){
        // Get ray reflected on surface, or hypothetical incident ray that would lead to current ray.
        Vector3 light_incident_ray = new Vector3(ray.direction);
        light_incident_ray.scale(-1);
        // default outcolor but set if hit
        outColor.set(0.0);
        if (current_intersection_record.surface.getMaterial().isEmitter()){
  				emittedRadiance(current_intersection_record, light_incident_ray, outColor);
  			}
        // Randomly sample a seed to help compute direct illumination color
  			Point2 seed = new Point2();
        Color radiance = new Color();
        Vector3 zvec = new Vector3();
  			sampler.sample(sampleIndex, sampleIndex, seed);
        // Store direct illumination color
  			direct.directIllumination(scene, light_incident_ray, zvec, current_intersection_record, seed, radiance);
  			// Add to radiance to final out color
  			outColor.add(radiance);
      }
      else{
        // Default to background color
        scene.getBackground().evaluate(ray.direction, outColor);
      }
    }


    /**
     * Compute the radiance emitted by a surface.
     *
     * @param iRec      Information about the surface point being shaded
     * @param dir          The exitant direction (surface coordinates)
     * @param outColor  The emitted radiance is written to this color
     */
    protected void emittedRadiance(IntersectionRecord iRec, Vector3 dir, Color outColor) {

        // If material is emitting, query it for emission in the relevant direction.
        // If not, the emission is zero.

        Material material = iRec.surface.getMaterial();

        if (material.isEmitter()) {
            LuminaireSamplingRecord lSampRec = new LuminaireSamplingRecord();
            lSampRec.set(iRec);
            lSampRec.emitDir.set(dir);
            material.emittedRadiance(lSampRec, outColor);
            return;
        }
        outColor.set(0, 0, 0);
    }
}
