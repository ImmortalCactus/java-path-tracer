public class Vec3Demo {
    public static void main(String[] args) throws NumberFormatException{
        int n = Integer.parseInt(args[0]);
        for (int i=0; i<n; i++){
            Vec3 v = Vec3.random();
            System.out.println("x: "+v.x()+", y: "+v.y()+", z: "+v.z());
        }
    }
}
