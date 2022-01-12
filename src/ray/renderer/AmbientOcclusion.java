package ray.renderer;

import ray.math.Geometry;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

/**
 * A renderer approximating how bright light should be shining on surfaces
 * 
 * @author cxz (at Columbia)
 */
public class AmbientOcclusion implements Renderer {

    /**
    * the length of the shadow ray. If the shadow ray is longer than
    * <code>length</code>* the size of bounding box, the intersection with
    * objects will be overlooked.
    */
    private double length = 0.1;
    
    public AmbientOcclusion() { }
    
    public void setLength(double d) {
        length = d;
    }
    
    /**
     * Evaluates the illumination component to the camera along the given 
     * {@link Ray} under the "ambient occlusion" assumption, where rays reach 
     * the bounding box increase the brightness of the surface, whereas a ray 
     * which hits any other object contributes no radiance
     * <p>
     * This method must be thread-safe
     *
     * @param scene scene to be rendered
     * @param ray the camera ray along which we compute the radiance
     * @param sampler the sampler is used to generate random numbers for sampling rays
     * @param sampleIndex index of the currently sampled ray in a pixel. The index value will be used in
     *        {@link SampleGenerator#sample(int, int, Point2)} method.
     * @param outColor output radiance for each RGB channel
     * @see ray.renderer.Renderer#rayRadiance(ray.misc.Scene, ray.misc.Ray,
     * ray.sampling.SampleGenerator, int, ray.misc.Color)
     */
    @Override
    public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler,
            int sampleIndex, Color outColor) {
        // find if the ray intersect with any surface
        IntersectionRecord iRec = new IntersectionRecord();
        
        if (scene.getFirstIntersection(iRec, ray)) {
            
            Point2 directSeed = new Point2();
            sampler.sample(1, sampleIndex, directSeed);     // this random variable is for incident direction
            
            // Generate a random incident direction
            Vector3 incDir = new Vector3();
            Geometry.squareToHemisphere(directSeed, incDir);
            iRec.frame.frameToCanonical(incDir);
            
            Ray shadowRay = new Ray(iRec.frame.o, incDir);
            shadowRay.makeOffsetRay();
            
            if ( !scene.getFirstIntersection(iRec, shadowRay) ) {
                outColor.set(0.8);
            } else {
                // determine the length of the shadow ray
                Vector3 exts = scene.getBoundingBoxExtents();
                if ( iRec.t > length * exts.length() ) 
                    outColor.set(0.8);
                else 
                    outColor.set(0.);
            }
            return;
        }
        
        scene.getBackground().evaluate(ray.direction, outColor);
    }

}
