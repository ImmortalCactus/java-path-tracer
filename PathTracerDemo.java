class PathTracerDemo {
    public static void main(String[] args){
        // Specify the parameters
        double aspectRatio = 3.0/2.0;
        int imageWidth = 1200;
        int imageHeight = (int)(imageWidth / aspectRatio);
        int samplesPerPixel = 100;
        int maxDepth = 50;
        // Create the scene hittable

        Sphere sphere0 = new Sphere(new Vec3(1.1, 0, 0), 1, new Lambertian(new Vec3(0.2, 0.8, 0.4)));
        Sphere sphere1 = new Sphere(new Vec3(-1.1, 0, 0), 1, new Metallic(new Vec3(0.7, 0.4, 0.4), 0.1));
        HittableList world = new HittableList();
        world.add(sphere0);
        world.add(sphere1);
        // Specify the materials
        // Add the objects into the scene
        // Specify camera
        Vec3 lookFrom = new Vec3(0, 0, -10);
        Vec3 lookAt = new Vec3(0, 0, 0);
        Vec3 vUp = new Vec3(0, 1, 0);
        double distToFocus = 5.0;
        double aperture = 0;
        Camera cam = new Camera(lookFrom, lookAt, vUp, 20, aspectRatio, aperture, distToFocus);
        // Output to file

        System.out.print("P3\n" + imageWidth + " " + imageHeight + "\n255\n");
        for (int j = imageHeight; j>=0; j--) {
            System.err.print("\rScanlines remaining: "+j+" ");
            System.err.flush();
            for (int i = 0; i < imageWidth; i++) {
                Vec3 pixelColor = new Vec3();
                for (int s = 0; s < samplesPerPixel; ++s) {
                    double u = (i + Math.random()) / (imageWidth-1);
                    double v = (j + Math.random()) / (imageHeight-1);
                    Ray r = cam.getRay(u, v);
                    Vec3 c = rayColor(r, world, maxDepth);
                    pixelColor.addInplace(c);
                }
                writeColor(pixelColor, samplesPerPixel);
            }
        }
        System.out.flush();
        System.err.print("\nDone\n");
    }

    private static final double near = 1e-8;
    private static final double far = Double.POSITIVE_INFINITY;
    

    private static Vec3 rayColor(Ray r, Hittable world, int depth) {
        if (depth <= 0) {
            return new Vec3();
        }
        HitRecord rec = world.hit(r, near, far);
        if (rec.hit) {
            //System.err.println("HIT!!");
            ScatterRecord sRec = rec.mat.scatter(r, rec);
            Vec3 secondRayColor = rayColor(sRec.scattered, world, depth-1);
            
            return new Vec3(sRec.attenuation.x() * secondRayColor.x(),
                            sRec.attenuation.y() * secondRayColor.y(),
                            sRec.attenuation.z() * secondRayColor.z());
    
        }
        Vec3 unitDirection = r.direction().unit();
        double t = 0.5*(unitDirection.y() + 1.0);
        return (new Vec3(1.0, 1.0, 1.0)).mul(1.0-t).add((new Vec3(0.5, 0.7, 1.0)).mul(t));
    }

    private static void writeColor(Vec3 pixelColor, int samplesPerPixel) {
        double r = pixelColor.x();
        double g = pixelColor.y();
        double b = pixelColor.z();

        double scale = 1.0 / samplesPerPixel;
        double gamma = 2.0;
        r = Math.pow(r * scale, 1/gamma);
        g = Math.pow(g * scale, 1/gamma);
        b = Math.pow(b * scale, 1/gamma);
        
        r = (int)(256 * Math.max(Math.min(r, 0.999), 0.0));
        g = (int)(256 * Math.max(Math.min(g, 0.999), 0.0));
        b = (int)(256 * Math.max(Math.min(b, 0.999), 0.0));

        System.out.print(r+" "+g+" "+b+"\n");
    }
}
