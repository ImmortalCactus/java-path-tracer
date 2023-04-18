class PathTracerDemo {
    public static void main(String[] args) throws InterruptedException {
        // Specify the parameters
        double aspectRatio = 3.0/2.0;
        int imageWidth = 1200;
        int imageHeight = (int)(imageWidth / aspectRatio);
        int samplesPerPixel = 100;
        int maxDepth = 50;
        // Create the scene hittable

        Sphere sphere0 = new Sphere(new Vec3(1.1, 1, 0), 1, new Dielectric(1.05));
        Sphere sphere1 = new Sphere(new Vec3(-1.1, 1, 0), 1, new Metallic(new Vec3(0.7, 0.4, 0.4), 0.1));
        Sphere floor = new Sphere(new Vec3(0, -100, 0), 100, new Lambertian(new Vec3(0.9, 0.9, 0.1)));
        HittableList world = new HittableList();
        world.add(sphere0);
        world.add(sphere1);
        world.add(floor);
        // Specify the materials
        // Add the objects into the scene
        // Specify camera
        Vec3 lookFrom = new Vec3(0, 5, -10);
        Vec3 lookAt = new Vec3(0, 0, 0);
        Vec3 vUp = new Vec3(0, 1, 0);
        double distToFocus = 5.0;
        double aperture = 0;
        Camera cam = new Camera(lookFrom, lookAt, vUp, 20, aspectRatio, aperture, distToFocus);
        // Output to file

        PPMArray ppmArrayObj = new PPMArray(imageHeight, imageWidth, samplesPerPixel);

        //System.out.print("P3\n" + imageWidth + " " + imageHeight + "\n255\n");
        for (int j = imageHeight-1; j>=0; j--) {
            System.err.print("\rScanlines remaining: "+j+" ");
            System.err.flush();
            for (int i = 0; i < imageWidth; i++) {
                for (int s = 0; s < samplesPerPixel; ++s) {
                    PathTracingThread t = new PathTracingThread(i, j, ppmArrayObj, cam, world, maxDepth);
                    t.start();
                    t.join();
                }
            }
        }
        System.out.flush();
        System.err.print("\nDone\n");
        ppmArrayObj.print();
    }

    private static final double near = 1e-8;
    private static final double far = Double.POSITIVE_INFINITY;
    

    private static Vec3 rayColor(Ray r, Hittable world, int depth) {
        if (depth <= 0) {
            return new Vec3();
        }
        HitRecord rec = world.hit(r, near, far);
        if (rec.hit) {
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


    static private class PathTracingThread extends Thread {
        private int i;
        private int j;
        private PPMArray ppmArrayObj;
        private Camera cam;
        private Hittable world;
        private int maxDepth;

        public PathTracingThread(int i, int j, PPMArray ppmArrayObj, Camera cam, Hittable world, int maxDepth) {
            this.i = i;
            this.j = j;
            this.ppmArrayObj = ppmArrayObj;
            this.cam = cam;
            this.world = world;
            this.maxDepth = maxDepth;
        }
        
        public void run() {
            int width = ppmArrayObj.getWidth();
            int height = ppmArrayObj.getHeight();

            double u = (i + Math.random()) / (width-1);
            double v = (j + Math.random()) / (height-1);
            Ray r = cam.getRay(u, v);
            Vec3 c = rayColor(r, world, maxDepth);
            ppmArrayObj.addPixel(height-1-j, i, c);
        }
    }
}
