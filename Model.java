import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.ArrayList;
import java.util.Collections;

class Model extends Hittable {
    private Vec3[] vertexArray;
    private int[][] indexArray;
    private Material mat;

    private int numVertex;
    private int numFace;
    private Vec3 boundingSphereCenter;
    private double boundingSphereRadius;

    private Node root;

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

        vertexArray = new Vec3[numVertex];
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
                
                double x = Double.valueOf(splitted[1]);
                double y = Double.valueOf(splitted[2]);
                double z = Double.valueOf(splitted[3]);

                sumVec[0] += x;
                sumVec[1] += y;
                sumVec[2] += z;

                vertexArray[vCount++] = new Vec3(x, y, z);
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
            boundingSphereRadius = Math.max(boundingSphereRadius, vertexArray[i].sub(boundingSphereCenter).lengthSquared());
        }
        System.err.println(numVertex);
        System.err.println(numFace);

        System.err.println(String.format("%f %f %f", boundingSphereCenter.x(), boundingSphereCenter.y(), boundingSphereCenter.z()));
        System.err.println(boundingSphereRadius);

        Vec3 boxHalf = (new Vec3(1, 1, 1)).mul(Math.sqrt(boundingSphereRadius));
        Vec3 min = boundingSphereCenter.sub(boxHalf);
        Vec3 max = boundingSphereCenter.add(boxHalf);
        ArrayList<Integer> initialIndicesList = new ArrayList<>();
        for(int i=0; i<numFace; i++) initialIndicesList.add(i);
        
        root = new Node(min, max, initialIndicesList, 10);
    }

    private boolean inBoundingSphere(Ray r) {
        Vec3 a = boundingSphereCenter.sub(r.origin());
        Vec3 uRayDir = r.direction().unit();
        Vec3 b = uRayDir.mul(a.dot(uRayDir));
        return a.lengthSquared() - b.lengthSquared() < boundingSphereRadius;
    }

    private HitRecord moellerTrumbore(Ray r, int faceIndex, double tMin, double tMax) {
        Vec3 p0 = vertexArray[indexArray[faceIndex][0]-1]; 
        Vec3 p1 = vertexArray[indexArray[faceIndex][1]-1];
        Vec3 p2 = vertexArray[indexArray[faceIndex][2]-1];
        
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
        if (root.intersectBoundingBox(r) < 0) return new HitRecord(false);
        HitRecord hr = root.hit(r, tMin, tMax);
        if (hr == null) return new HitRecord(false);
        return hr;
    }
    
    class Node {
        private Vec3 min;
        private Vec3 max;
        private ArrayList<Integer> faceIndices;
        private ArrayList<Node> childNodes;

        private final static int MIN_TRIANGLES = 30;

        public Node(Vec3 min, Vec3 max, ArrayList<Integer> faceIndices, int depth) {
            this.min = min;
            this.max = max;
            //System.err.println(depth + " " + min.x() + " " + max.x());
            
            if (depth == 0 || faceIndices.size() < MIN_TRIANGLES) {
                if(faceIndices.size() > 0) System.err.println(faceIndices.size());
                this.faceIndices = faceIndices;
                this.childNodes = null;
                return;
            }

            ArrayList<ArrayList<Integer>> childFaceIndices = new ArrayList<ArrayList<Integer>>();
            for(int i = 0; i < 8; i++) childFaceIndices.add(new ArrayList<Integer>());
            this.faceIndices = new ArrayList<Integer>();
            this.childNodes = new ArrayList<Node>();

            Vec3 boxHalfSize = this.max.sub(this.min).div(2);
            faceIndices.forEach((i) -> {
                int[] encodings = new int[3];
                Vec3 mid = this.min.add(boxHalfSize);
                for(int k=0; k<3; k++){
                    Vec3 p = vertexArray[indexArray[i][k]-1];
                    p = p.sub(mid); 
                    encodings[k] = (p.x() < 0 ? 0 : 4) +  (p.y() < 0 ? 0 : 2) + (p.z() < 0 ? 0 : 1);
                }

                if(encodings[0] == encodings[1] && encodings[1] == encodings[2]) {
                    childFaceIndices.get(encodings[0]).add(i);
                } else {
                    this.faceIndices.add(i);
                }
            });
            
            for(int x = 0; x < 2; x++) for(int y = 0; y < 2; y++) for(int z = 0; z < 2; z++){
                Vec3 childMin = this.min.add(new Vec3(x*boxHalfSize.x(),
                                                    y*boxHalfSize.y(),
                                                    z*boxHalfSize.z()));
                Vec3 childMax = this.min.add(new Vec3((x+1)*boxHalfSize.x(),
                                                    (y+1)*boxHalfSize.y(),
                                                    (z+1)*boxHalfSize.z()));
                this.childNodes.add(new Node(childMin,
                                            childMax,
                                            childFaceIndices.get(x*4 + y*2 + z),
                                            depth-1));
            }

        }


        public double intersectBoundingBox(Ray r) {
            Vec3 o = r.origin();
            Vec3 d = r.direction();
            double[] tmin = {(min.x()-o.x())/d.x(), (min.y()-o.y())/d.y(), (min.z()-o.z())/d.z()};
            double[] tmax = {(max.x()-o.x())/d.x(), (max.y()-o.y())/d.y(), (max.z()-o.z())/d.z()};
            
            double[] t1 = {Math.min(tmin[0], tmax[0]), Math.min(tmin[1], tmax[1]), Math.min(tmin[2], tmax[2])};
            double[] t2 = {Math.max(tmin[0], tmax[0]), Math.max(tmin[1], tmax[1]), Math.max(tmin[2], tmax[2])};

            double tnear = Math.max(t1[0], Math.max(t1[1], t1[2]));
            double tfar = Math.min(t2[0], Math.min(t2[1], t2[2]));
             
            final double EPSILON = 1e-8;

            if (tfar < 0 || tnear > tfar) return -1;
            if (tnear < 0) return 0;
            Vec3 p = o.add(d.mul(tnear));   
            if (p.x() >= min.x()-EPSILON && p.x() <= max.x()+EPSILON &&
                p.y() >= min.y()-EPSILON && p.y() <= max.y()+EPSILON &&
                p.z() >= min.z()-EPSILON && p.z() <= max.z()+EPSILON) return tnear;
            return -1;
        }

        public HitRecord hit(Ray r, double tMin, double tMax) {
            HitRecord minHR = null;
            double far = tMax;
            if (childNodes != null) {
                ArrayList<Integer> intersectBoxIndices = new ArrayList<Integer>();
                double[] tVals = new double[8];
                for(int i = 0; i < 8; i++) {
                    tVals[i] = childNodes.get(i).intersectBoundingBox(r);
                    if(tVals[i] >= 0) intersectBoxIndices.add(i);
                }
                Collections.sort(intersectBoxIndices, (i1, i2)->{
                    if (tVals[i1] < tVals[i2]) return -1;
                    if (tVals[i1] > tVals[i2]) return 1;
                    return 0;
                });
                for (int i : intersectBoxIndices) {
                    HitRecord curHR = childNodes.get(i).hit(r, tMin, far);
                    if (curHR != null) {
                        minHR = curHR;
                        break;
                    }
                }
            }
            for (int i : faceIndices) {
                HitRecord resMT = moellerTrumbore(r, i, tMin, far);
                if (resMT == null) continue;
                if (minHR == null || resMT.t < minHR.t){
                    minHR = resMT;
                    far = minHR.t;
                }
            }
            return minHR;
        }
    }
}

