import java.io.FileNotFoundException;  // Import this class to handle errors

class PathTracerDemo {
    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        // Specify the parameters
        double aspectRatio = 3.0/2.0;
        int imageWidth = 1200;
        int imageHeight = (int)(imageWidth / aspectRatio);
        int samplesPerPixel = 20;
        int maxDepth = 20;

        int numThreads = 8;
        // Create the scene hittable

        HittableList world = new HittableList();
        //Model glassBunny = new Model("./model/bunny.obj", new Dielectric(1.5));
        Model metalBunny = new Model("./model/teapot.obj", new Metallic(new Vec3(1.0, 0.2, 0.2), 0));
        Sphere sphere1 = new Sphere(new Vec3(0, 0.4, -0.4), 0.3, new Dielectric(1.1));
        Sphere floor = new Sphere(new Vec3(0, -100, 0), 100, new Lambertian(new Vec3(0.1, 0.5, 0.3)));
        Transform transBunny = new Transform(metalBunny, new Vec3(0, 0, 0), new Vec3(0, 180, 0));
        world.add(transBunny);
        //world.add(sphere1);
        //world.add(floor);
        // Specify the materials
        // Add the objects into the scene
        // Specify camera
        //Vec3 lookFrom = new Vec3(0.28, 0.5, 0.28);
        Vec3 lookFrom = new Vec3(-10, 26, -15);
        Vec3 lookAt = new Vec3(0, 0, 0);
        Vec3 vUp = new Vec3(0, 1, 0);
        double distToFocus = 1.8;
        double aperture = 0.0;
        Camera cam = new Camera(lookFrom, lookAt, vUp, 20, aspectRatio, aperture, distToFocus);
        // Output to file

        Scene scene = new Scene(imageHeight, imageWidth, samplesPerPixel, maxDepth, metalBunny, cam, numThreads);
        System.err.println("Start path tracing.");
        scene.rayTrace();
        scene.output();
    }
}
