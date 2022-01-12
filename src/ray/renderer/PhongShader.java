package ray.renderer;

import ray.light.PointLight;
import ray.brdf.BRDF;
import ray.math.Vector3;
import ray.misc.Color;
import ray.misc.IntersectionRecord;
import ray.misc.Ray;
import ray.misc.Scene;
import ray.sampling.SampleGenerator;

/**
 * A simple renderer used to debug the scene. This renderer uses the
 * material and point light source to render a simple scene. It is a local
 * illumination renderer without shadow.
 */
public class PhongShader implements Renderer {
    
    /**
     * the power term in Phong shader
     */
    private double phongCoeff = 2.5;
    
    public PhongShader() { }
    
    /**
     * Sets the power term
     * 
     * @param a phong coeff
     */
    public void setAlpha(double a) {
        phongCoeff = a;
    }
    
    @Override
    public void rayRadiance(Scene scene, Ray ray, SampleGenerator sampler,
            int sampleIndex, Color outColor) {
        // find if the ray intersect with any surface
        IntersectionRecord iRec = new IntersectionRecord();
        
        scene.getBackground().evaluate(ray.direction, outColor);
        
        if (scene.getFirstIntersection(iRec, ray)) {
            Vector3 viewDir = new Vector3(ray.direction);
            viewDir.scale(-1.);
            
            for(PointLight pl : scene.getPointLights()) {
                applyPhongShading(iRec, viewDir, pl, outColor);
            }
        }
    }
    private void applyPhongShading(IntersectionRecord iRec, Vector3 viewDir, PointLight pt, Color outColor) {
        
        //// diffuse component
        Vector3 lDir = new Vector3();
        lDir.sub(pt.location, iRec.frame.o);
        lDir.normalize();
        
        BRDF brdf = iRec.surface.getMaterial().getBRDF(iRec);
        Color brdfVal = new Color();
        brdf.evaluate(iRec.frame, viewDir, lDir, brdfVal);
                
        Color diff = new Color(pt.diffuse);
        diff.scale(brdfVal);
        diff.scale(viewDir.dot(iRec.frame.w));
                
        outColor.add(diff);
        
        //// specular component
        Vector3 halfDir = new Vector3(lDir);
        halfDir.add(viewDir);
        halfDir.normalize();
        Color spec = new Color(pt.specular);
        double p = halfDir.dot(iRec.frame.w);
        if ( p > 0. ) {
            spec.scale(Math.pow(p, phongCoeff)*0.2);
            outColor.add(spec);
        }
    }
}
