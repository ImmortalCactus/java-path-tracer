public class Vec3Demo {
    public static void main(String[] args){
        Vec3 a = new Vec3(-3,-4,0);
        Vec3 b = new Vec3(0, 1, 0);
        Vec3 c = a.refract(b, 1.33);
        c.print();
    }
}
