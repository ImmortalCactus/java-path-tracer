import java.util.ArrayList;

abstract class Hittable {
    abstract public HitRecord hit(Ray r, double tMin, double tMax);
}
    
class HitRecord {
    public final boolean hit;
    public final Vec3 p;
    public final Vec3 normal;
    public final Material mat;
    public final double t;
    public final boolean frontFace;

    public HitRecord(boolean hit, double t, Vec3 p, Vec3 normal, boolean frontFace, Material mat) {
        this.hit = hit;
        this.p = p;
        this.normal = normal;
        this.mat = mat;
        this.t = t;
        this.frontFace = frontFace;
    };

    public HitRecord(boolean h) {
        assert h == false;
        this.hit = h;
        this.p = null;
        this.normal = null;
        this.mat = null;
        this.t = 0;
        this.frontFace = false;
    }
}

class Sphere extends Hittable {
    private Vec3 center;
    private double radius;
    private Material mat;

    public Sphere(Vec3 c, double r, Material m) {
        center = c;
        radius = r;
        this.mat = m;
    }

    public HitRecord hit(Ray r, double tMin, double tMax) {
        Vec3 oc = r.origin().sub(center);
        double a = r.direction().lengthSquared();
        double halfB = oc.dot(r.direction());
        double c = oc.lengthSquared() - (radius * radius);
                
        double discriminant = halfB*halfB - a*c;
        if (discriminant < 0) {
            return new HitRecord(false);
        }

        double sqrtD = Math.sqrt(discriminant);
        double root = (-halfB - sqrtD) / a;
        if (root < tMin || tMax < root) {
            root = (-halfB + sqrtD) / a;
            if(root < tMin || tMax < root)
                return new HitRecord(false);
        }
        
        Vec3 p = r.at(root);
        Vec3 outwardNormal = p.sub(center).unit();
        boolean frontFace = (outwardNormal.dot(r.direction()) < 0);
        Vec3 normal = frontFace ? outwardNormal : outwardNormal.neg();
        return new HitRecord(
                true,
                root,
                p,
                normal,
                frontFace,
                mat
        );
    }
}



class HittableList extends Hittable {
    private ArrayList<Hittable> objects = new ArrayList<Hittable>();
    
    public HittableList() {};

    public HitRecord hit(Ray r, double tMin, double tMax) {
        double far = tMax;
        HitRecord closestHit = null;
        for(Hittable ob : objects) {
            HitRecord currentHR = ob.hit(r, tMin, far);
            if(currentHR.hit == false) continue;
            if(closestHit == null || closestHit.t > currentHR.t) {
                closestHit = currentHR;
                far = closestHit.t;
            }
        }
        if(closestHit == null) return new HitRecord(false);
        return closestHit;
    }
    public void add(Hittable ob) {
        objects.add(ob);
    }
}
