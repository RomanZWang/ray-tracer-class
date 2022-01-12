package ray.surface;

import ray.accel.AxisAlignedBoundingBox;
import ray.material.Material;
import ray.math.Geometry;
import ray.math.Point2;
import ray.math.Point3;
import ray.math.Vector3;
import ray.misc.IntersectionRecord;
import ray.misc.LuminaireSamplingRecord;
import ray.misc.Ray;

/**
 * Represents a sphere as a center and a radius.
 *
 * @author ags
 */
public class Sphere extends Surface {

    /** Material for this sphere. */
    protected Material material = Material.DEFAULT_MATERIAL;

    /** The center of the sphere. */
    protected final Point3 center = new Point3();

    /** The radius of the sphere. */
    protected double radius = 1.0;

    /**
     * Default constructor, creates a sphere at the origin with radius 1.0
     */
    public Sphere() {
    }

    /**
     * The explicit constructor. This is the only constructor with any real code
     * in it. Values should be set here, and any variables that need to be
     * calculated should be done here.
     *
     * @param newCenter The center of the new sphere.
     * @param newRadius The radius of the new sphere.
     * @param newMaterial The material of the new sphere.
     */
    public Sphere(Vector3 newCenter, double newRadius, Material newMaterial) {
        material = newMaterial;
        center.set(newCenter);
        radius = newRadius;
        updateArea();
    }

    public void updateArea() {
    	area = 4 * Math.PI * radius*radius;
    	oneOverArea = 1. / area;
    }

    /**
     * @see ray.surface.Surface#getMaterial()
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * @see ray.surface.Surface#setMaterial(ray.material.Material)
     */
    public void setMaterial(Material material) {
        this.material = material;
    }

    /**
     * Returns the center of the sphere in the input Point3
     * @param outPoint output space
     */
    public void getCenter(Point3 outPoint) {
        outPoint.set(center);
    }

    /**
     * @param center The center to set.
     */
    public void setCenter(Point3 center) {
        this.center.set(center);
    }

    /**
     * @return Returns the radius.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius The radius to set.
     */
    public void setRadius(double radius) {
        this.radius = radius;
        updateArea();
    }

    public boolean chooseSamplePoint(Point3 p, Point2 seed, LuminaireSamplingRecord lRec) {
        Geometry.squareToSphere(seed, lRec.frame.w);
        lRec.frame.o.set(center);
        lRec.frame.o.scaleAdd(radius, lRec.frame.w);
        lRec.frame.initFromW();
        lRec.pdf = oneOverArea;
        lRec.emitDir.sub(p, lRec.frame.o);
        return true;
    }

    /**
     * @see ray.surface.Surface#intersect(ray.misc.IntersectionRecord,
     *      ray.misc.Ray)
     */
    public boolean intersect(IntersectionRecord outRecord, Ray ray) {
        // Question already provided by accident in starter code
        // Rename the common vectors so I don't have to type so much
        Vector3 d = ray.direction;
        Point3 c = center;
        Point3 o = ray.origin;

        // Compute some factors used in computation
        double qx = o.x - c.x;
        double qy = o.y - c.y;
        double qz = o.z - c.z;
        double dd = d.squaredLength();
        double qd = qx * d.x + qy * d.y + qz * d.z;
        double qq = qx * qx + qy * qy + qz * qz;

        // solving the quadratic equation for t at the pts of intersection
        // dd*t^2 + (2*qd)*t + (qq-r^2) = 0
        double discriminantsqr = (qd * qd - dd * (qq - radius * radius));

        // If the discriminant is less than zero, there is no intersection
        if (discriminantsqr < 0) {
            return false;
        }

        // Otherwise check and make sure that the intersections occur on the ray (t
        // > 0) and return the closer one
        double discriminant = Math.sqrt(discriminantsqr);
        double t1 = (-qd - discriminant) / dd;
        double t2 = (-qd + discriminant) / dd;
        double t = 0;
        if (t1 > ray.start && t1 < ray.end) {
            t = t1;
        }
        else if (t2 > ray.start && t2 < ray.end) {
            t = t2;
        }
        else {
            return false; // Neither intersection was in the ray's half line.
        }

        // There was an intersection, fill out the intersection record
        outRecord.t = t;
        ray.evaluate(outRecord.frame.o, t);
        outRecord.surface = this;
        outRecord.frame.w.sub(outRecord.frame.o, center);
        outRecord.frame.initFromW();
        return true;
    }

    /**
     * @see Object#toString()
     */
    public String toString() {
        return "sphere " + center + " " + radius + " " + material + " end";
    }

    /**
     * @see ray.surface.Surface#addToBoundingBox(ray.accel.AxisAlignedBoundingBox)
     */
    public void addToBoundingBox(AxisAlignedBoundingBox inBox) {
        inBox.add(center.x - radius, center.y - radius, center.z - radius);
        inBox.add(center.x + radius, center.y + radius, center.z + radius);
    }

}
