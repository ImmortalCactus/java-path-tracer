class Ray {
    private Vec3 orig;
    private Vec3 dir;

    public Ray(Vec3 o, Vec3 d) {
        orig = new Vec3(o);
        dir = d;
    }

    public Vec3 origin() {
        return orig;
    }

    public Vec3 direction() {
        return dir;
    }

    public Vec3 at(double t) {
        return orig.add(dir.mul(t));
    }
}
