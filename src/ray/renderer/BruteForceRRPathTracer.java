package ray.renderer;

import ray.misc.Color;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;
import ray.math.Point2;
import ray.math.Vector3;
import ray.misc.IntersectionRecord;

public class BruteForceRRPathTracer extends PathTracer {
    protected double survivalProbability = 0.5;

    public void setSurvivalProbability(double val) {
    	this.survivalProbability = val;
    	System.out.println("SET: " + survivalProbability);
    }

    /**
     * @param scene
     * @param ray
     * @param sampler
     * @param sampleIndex
     * @param outColor
     */
    protected void rayRadianceRecursive(Scene scene, Ray ray,
            SampleGenerator sampler, int sampleIndex, int level, Color outColor) {
    	// W4160 TODO (B)
    	//
        // The function should be the same as BruteForcePathTracer *except* the termination
    	// condition. Here please use Russian Roulette to terminate the recursive call.
    	// The survival probability of Russian Roulette is set in the XML file.

      if(Math.random() <= survivalProbability){

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
      else{
        outColor.set(0.0);
      }
    }

}
