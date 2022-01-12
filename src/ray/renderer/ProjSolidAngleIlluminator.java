package ray.renderer;

import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Scene;

import ray.misc.LuminaireSamplingRecord;
import ray.misc.Ray;
import ray.math.Geometry;

import ray.brdf.BRDF;
import ray.material.Material;

/**
 * This class computes direct illumination at a surface by the simplest possible approach: it estimates
 * the integral of incident direct radiance using Monte Carlo integration with a uniform sampling
 * distribution.
 *
 * @author srm, Changxi Zheng (at Columbia)
 */
public class ProjSolidAngleIlluminator extends DirectIlluminator {


    public void directIllumination(Scene scene, Vector3 incDir, Vector3 outDir,
            IntersectionRecord iRec, Point2 seed, Color outColor) {
    	// W4160 TODO (A)
    	// This method computes a Monte Carlo estimate of reflected radiance due to direct illumination.  It
        // generates samples uniformly wrt. the projected solid angle measure:
        //
        //    f = brdf * radiance
        //    p = 1 / pi
        //    g = f / p = brdf * radiance * pi
        //
        // The same code could be interpreted as an integration wrt. solid angle, as follows:
        //
        //    f = brdf * radiance * cos_theta
        //    p = cos_theta / pi
        //    g = f / p = brdf * radiance * pi
    	//
    	// As a hint, here are a few steps when I code this function
    	// 1. Generate a random incident direction according to proj solid angle
        //    pdf is constant 1/pi
    	// 2. Find incident radiance from that direction
    	// 3. Estimate reflected radiance using brdf * radiance / pdf = pi * brdf * radiance

      // randomly sample using PSA hemisphere
      Geometry.squareToPSAHemisphere(seed, incDir);

      // transform sample to world view
      iRec.frame.frameToCanonical(incDir);
      incDir.normalize();

      // create ray used for direct illumination tester
      Ray ray = new Ray(iRec.frame.o, incDir);
      ray.makeOffsetRay(); // avoid self intersection

      IntersectionRecord tester_intersection_record = new IntersectionRecord();
      // obtain edge case test conditions
      boolean is_emitter_value = scene.getFirstIntersection(tester_intersection_record, ray) && tester_intersection_record.surface.getMaterial().isEmitter();
      Material material = iRec.surface.getMaterial();
      BRDF surface_brdf = material.getBRDF(iRec);
      boolean surface_valid = surface_brdf != null;
      // check if surface is not empty and is emitter
      if (is_emitter_value && surface_valid){
          // retrieve brdf irrad value
          Color irradiance = new Color();
          surface_brdf.evaluate(iRec.frame, incDir, outDir, irradiance);
          // find incident radiance
	        scene.incidentRadiance(iRec.frame.o, incDir, outColor);
          // scale by reflectance and pdf inv
	        outColor.scale(irradiance);
	        outColor.scale(Math.PI);
      }
      else{
        outColor.set(0.0, 0.0, 0.0);
      }
    }
}
