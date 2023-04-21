import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

class Model extends Hittable {
    private double[][] vertexArray;
    private int[][] indexArray;
    private Material mat;

    private int numVertex;
    private int numFace;
    private Vec3 boundingSphereCenter;
    private double boundingSphereRadius;

    public Model(String filename, Material mat) throws FileNotFoundException {
        this.mat = mat;
        File obj = new File(filename);
        Scanner scanner = new Scanner(obj);

        while(scanner.hasNextLine()) {
            String data = scanner.nextLine();
            if(data.isEmpty() || !(data.charAt(0) == 'v' || data.charAt(0) == 'f')) continue;
            if(data.charAt(0) == 'v') numVertex += 1; 
            if(data.charAt(0) == 'f') numFace += 1;
        }

        vertexArray = new double[numVertex][3];
        indexArray = new int[numFace][3];
        
        scanner.close();
        int vCount = 0;
        int fCount = 0;

        double[] sumVec = {0, 0, 0};
        
        scanner = new Scanner(obj);
        while(scanner.hasNextLine()) {
            String data = scanner.nextLine();
            if(data.isEmpty() || !(data.charAt(0) == 'v' || data.charAt(0) == 'f')) continue;
            if(data.charAt(0) == 'v') {
                String[] splitted = data.split("\\s+", 4);
                
                vertexArray[vCount][0] = Double.valueOf(splitted[1]);
                vertexArray[vCount][1] = Double.valueOf(splitted[2]);
                vertexArray[vCount][2] = Double.valueOf(splitted[3]);



                sumVec[0] += vertexArray[vCount][0];
                sumVec[1] += vertexArray[vCount][1];
                sumVec[2] += vertexArray[vCount][2];

                vCount++;
            }
            if(data.charAt(0) == 'f') {
                String[] splitted = data.split("\\s+", 4);
                
                indexArray[fCount][0] = Integer.parseInt(splitted[1]);
                indexArray[fCount][1] = Integer.parseInt(splitted[2]);
                indexArray[fCount][2] = Integer.parseInt(splitted[3]);

                fCount++;
            }
        }

        scanner.close();

        sumVec[0] /= numVertex;
        sumVec[1] /= numVertex;
        sumVec[2] /= numVertex;

        boundingSphereCenter = new Vec3(sumVec[0], sumVec[1], sumVec[2]);
        boundingSphereRadius = 0;
        for(int i=0; i<numVertex; i++) {
            boundingSphereRadius = Math.max(boundingSphereRadius, 
                    Math.pow((vertexArray[i][0]-sumVec[0]), 2)+
                    Math.pow((vertexArray[i][1]-sumVec[1]), 2)+
                    Math.pow((vertexArray[i][2]-sumVec[2]), 2));
        }
        System.err.println(numVertex);
        System.err.println(numFace);

        System.err.println(String.format("%f %f %f", boundingSphereCenter.x(), boundingSphereCenter.y(), boundingSphereCenter.z()));
        System.err.println(boundingSphereRadius);
    }

    private boolean inBoundingSphere(Ray r) {
        Vec3 a = boundingSphereCenter.sub(r.origin());
        Vec3 uRayDir = r.direction().unit();
        Vec3 b = uRayDir.mul(a.dot(uRayDir));
        return a.lengthSquared() - b.lengthSquared() < boundingSphereRadius;
    }

    private HitRecord moellerTrumbore(Ray r, int faceIndex, double tMin, double tMax) {
        Vec3 p0 = new Vec3(vertexArray[indexArray[faceIndex][0]-1]); 
        Vec3 p1 = new Vec3(vertexArray[indexArray[faceIndex][1]-1]);
        Vec3 p2 = new Vec3(vertexArray[indexArray[faceIndex][2]-1]);
        
        Vec3 o = r.origin();
        Vec3 d = r.direction();

        Vec3 pp0 = p0.sub(o);
        Vec3 pp1 = p1.sub(o);
        Vec3 pp2 = p2.sub(o);

        double dsq = d.lengthSquared();

        double tpp0 = pp0.dot(d) / dsq;
        double tpp1 = pp1.dot(d) / dsq;
        double tpp2 = pp2.dot(d) / dsq;

        if ( Math.max(tpp0, Math.max(tpp1, tpp2)) < tMin ||
            Math.min(tpp0, Math.min(tpp1, tpp2)) > tMax) return null;

        Vec3 v1 = p1.sub(p0);
        Vec3 v2 = p2.sub(p0);

        Vec3 uNormal = v1.cross(v2).unit();
        Vec3 uDir = d.unit();

        if (Math.abs(uNormal.dot(uDir)) > (1 - 1e-8)) return null;

        double t = (p0.sub(o).dot(uNormal))/(d.dot(uNormal));
        
        if (t < tMin || t > tMax) return null;

        Vec3 projected = o.add(d.mul(t));
        Vec3 vp = projected.sub(p0);

        double c = v2.x() / v2.y();
        double u = (vp.x()-vp.y()*c) / (v1.x()-v1.y()*c);
        double v = (vp.x() - u * v1.x()) / v2.x();

        if (u < 0 || v < 0 || u + v > 1) return null;

        boolean frontFace = true;
        if (uNormal.dot(d) > 0){
            uNormal = uNormal.neg();
            frontFace = false;
        }
        return new HitRecord(
                true,
                t,
                projected,
                uNormal,
                frontFace,
                this.mat);
    }

    public HitRecord hit(Ray r, double tMin, double tMax) {
        double far = tMax;
        if (!inBoundingSphere(r)) {
            return new HitRecord(false);
        }
        HitRecord ret = null;
        for (int i = 0; i < numFace; i++) {
            HitRecord resMT = moellerTrumbore(r, i, tMin, far);
            if (resMT == null) continue;
            if (ret == null || resMT.t < ret.t){
                ret = resMT;
                far = ret.t;
            }
        }
        if (ret == null) return new HitRecord(false);
        return ret;
    }
	     
}
