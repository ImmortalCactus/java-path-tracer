class PathTracerDemo {
    public static void main(String[] args) throws InterruptedException {
        // Specify the parameters
        double aspectRatio = 3.0/2.0;
        int imageWidth = 1200;
        int imageHeight = (int)(imageWidth / aspectRatio);
        int samplesPerPixel = 100;
        int maxDepth = 50;

        int numThreads = 2;
        // Create the scene hittable

        Sphere sphere0 = new Sphere(new Vec3(0, 0, -1.0), 0.5, new Dielectric(1.5));
        Sphere sphere1 = new Sphere(new Vec3(-1.0, 0, -1.0), 0.5, new Lambertian(new Vec3(0.7, 0.4, 0.4)));
        Sphere sphere2 = new Sphere(new Vec3(1.0, 0, -1.0), 0.5, new Metallic(new Vec3(0.8, 0.6, 0.2), 0.5));
        Sphere floor = new Sphere(new Vec3(0, -100.5, 0), 100, new Lambertian(new Vec3(0.9, 0.9, 0.1)));
        HittableList world = new HittableList();
        world.add(sphere0);
        world.add(sphere1);
        world.add(sphere2);
        world.add(floor);
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

        Scene scene = new Scene(imageHeight, imageWidth, samplesPerPixel, maxDepth, world, cam, numThreads);

        scene.rayTrace();
        scene.output();
    }
}
