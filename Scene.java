import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;

public class Scene {
    private int imageWidth;
    private int imageHeight;
    private int samplesPerPixel;
    private int maxDepth;
    private double near = 1e-8;
    private double far = Double.POSITIVE_INFINITY;

    private Hittable world;
    private Camera cam;
    private PPMArray ppmArrayObj;

    private int numThreads;

    private int scanlinesLeft;

    public Scene(int h, int w, int s, int d, Hittable world, Camera cam, int numThreads) {
        this.imageHeight = h;
        this.imageWidth = w;
        this.samplesPerPixel = s;
        this.maxDepth = d;
        this.numThreads = numThreads;

        this.world = world;
        this.cam = cam;
        this.ppmArrayObj = new PPMArray(imageHeight, imageWidth, samplesPerPixel);

        this.scanlinesLeft = imageHeight;
    }

    public void rayTrace() {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int taskPerThread = imageHeight / numThreads + 1;

        for (int t = 0; t < numThreads; t++){
            final int tt = t;
            executor.execute(()->{
                //int counter = 0;
                for (int j = tt * taskPerThread; j < Math.min((tt + 1) * taskPerThread, imageHeight); j++) {
                    for (int i = 0; i < imageWidth; i++) {
                        for (int s = 0; s < samplesPerPixel; ++s) {
                            double u = (i + ThreadLocalRandom.current().nextDouble(0, 1)) / (imageWidth-1);
                            double v = (j + ThreadLocalRandom.current().nextDouble(0, 1)) / (imageHeight-1);
                            Ray r = cam.getRay(u, v);
                            Vec3 c = rayColor(r, maxDepth);
                            
                            ppmArrayObj.addPixel(imageHeight-1-j, i, c);
                        }
                    }
                    this.scanlinesLeft--;
                    System.err.print("\r"+this.scanlinesLeft+" lines left.");
                    System.err.flush();
                }
            });
        }
        System.err.println("Threads created.");

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            System.err.println("Tasks interrupted");
        }

        System.err.print("\nDone.\n");
    }

    public void output() {
        this.ppmArrayObj.print();
    }

    private Vec3 rayColor(Ray r, int depth) {
        if (depth <= 0) {
            return new Vec3();
        }
        HitRecord rec = world.hit(r, near, far);
        if (rec.hit) {
            //return rec.normal.unit().mul(2).sub(new Vec3(1, 1, 1));

            ScatterRecord sRec = rec.mat.scatter(r, rec);
            Vec3 secondRayColor = rayColor(sRec.scattered, depth-1);
            
            return new Vec3(sRec.attenuation.x() * secondRayColor.x(),
                            sRec.attenuation.y() * secondRayColor.y(),
                            sRec.attenuation.z() * secondRayColor.z());
    
        }
        Vec3 unitDirection = r.direction().unit();
        double t = 0.5*(unitDirection.y() + 1.0);
        return (new Vec3(1.0, 1.0, 1.0)).mul(1.0-t).add((new Vec3(0.5, 0.7, 1.0)).mul(t));
    }
}
