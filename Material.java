import java.util.concurrent.ThreadLocalRandom;

abstract class Material{
    abstract public ScatterRecord scatter(Ray rIn, HitRecord rec);
}

class ScatterRecord {
    public final Vec3 attenuation;
    public final Ray scattered;

    public ScatterRecord(Vec3 attenuation, Ray scattered) {
        this.attenuation = attenuation;
        this.scattered = scattered;
    };
}
class Lambertian extends Material {
    private Vec3 albedo;

    public Lambertian(Vec3 a) {
        albedo = a;
    }

    public ScatterRecord scatter(Ray rIn, HitRecord rec) {
        Vec3 scatterDirection = Vec3.randomInHemisphere(rec.normal);
        if(scatterDirection.nearZero()) {
            scatterDirection = rec.normal;
        }

        return new ScatterRecord(
                    albedo,
                    new Ray(rec.p, scatterDirection)
                );
    }
}

class Metallic extends Material {
    private double fuzz;
    private Vec3 albedo;

    public Metallic(Vec3 a, double f) {
        albedo = a;
        fuzz = f;
    }

    public ScatterRecord scatter(Ray rIn, HitRecord rec) {
        Vec3 reflected = rIn.direction().reflect(rec.normal);
        Vec3 fuzzed = reflected.add(Vec3.randomInUnitSphere().mul(fuzz));

        if (fuzzed.dot(rec.normal) <= 0) fuzzed = reflected;
        return new ScatterRecord(
            albedo,
            new Ray(rec.p, fuzzed)
        );
    }
}

class Dielectric extends Material {
    private double ior;
    
    public Dielectric(double eta) {
        ior = eta;
    }

    public ScatterRecord scatter(Ray rIn, HitRecord rec) {
        double refractRatio = rec.frontFace? 1/ior : ior; // \eta_i / \eta_t
        double cosTheta = -rIn.direction().unit().dot(rec.normal.unit());
        double sinTheta = Math.sqrt(1-Math.pow(cosTheta, 2));

        Vec3 transmitted;

        if(refractRatio * sinTheta >= 1 || ThreadLocalRandom.current().nextDouble(0, 1) <= reflectance(refractRatio, cosTheta)){
            transmitted = rIn.direction().reflect(rec.normal);
        } else {
            transmitted = rIn.direction().refract(rec.normal, refractRatio);
        }

        return new ScatterRecord(new Vec3(1.0, 1.0, 1.0), new Ray(rec.p, transmitted));
    }

    private double reflectance(double eta, double cosTheta) {
        double r0 = Math.pow((1-eta)/(1+eta), 2);
        return r0 + (1-r0) * Math.pow(1-cosTheta, 5);
    }
}
