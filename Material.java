public interface Material{
    public ScatterRecord scatter(Ray rIn, HitRecord rec);
}

class ScatterRecord {
    public final boolean notAbsorbed;
    public final Vec3 attenuation;
    public final Ray scattered;

    public ScatterRecord(boolean notAbsorbed, Vec3 attenuation, Ray scattered) {
        this.notAbsorbed = notAbsorbed;
        this.attenuation = attenuation;
        this.scattered = scattered;
    };
}
class Lambertian implements Material {
    private Vec3 albedo;

    Lambertian(Vec3 a) {
        albedo = new Vec3(a);
    }

    public ScatterRecord scatter(Ray rIn, HitRecord rec) {
        Vec3 scatterDirection = Vec3.randomInHemisphere(rec.normal);
        if(scatterDirection.nearZero()) {
            scatterDirection = rec.normal;
        }

        return new ScatterRecord(
                    true,
                    albedo,
                    new Ray(rec.p, scatterDirection)
                );
    }
}

class Metallic implements Material {
    private double fuzz;
    private Vec3 albedo;

    Metallic(Vec3 a, double f) {
        albedo = a;
        fuzz = f;
    }

    public ScatterRecord scatter(Ray rIn, HitRecord rec) {
        Vec3 reflected = rIn.direction().reflect(rec.normal);

        return new ScatterRecord(
            true,
            albedo,
            new Ray(rec.p, reflected)
        );
    }
}
