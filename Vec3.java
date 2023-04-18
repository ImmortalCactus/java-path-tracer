class Vec3 {
    protected double[] e;

    public Vec3() {
        e = new double[]{0, 0, 0};
    }

    public Vec3(double e0, double e1, double e2) {
        e = new double[]{e0, e1, e2};
    }

    public Vec3(Vec3 v) {
        e = new double[]{v.e[0], v.e[1], v.e[2]};
    }

    public static Vec3 random(){
        return new Vec3(Math.random()*2-1, Math.random()*2-1, Math.random()*2-1);
    }

    public static Vec3 random(double min, double max){
        return new Vec3(Math.random()*(max-min)+min, Math.random()*(max-min)+min, Math.random()*(max-min)+min);
    }
    
    public static Vec3 randomInUnitSphere() {
        while(true) {
            Vec3 p = Vec3.random();
            if (p.lengthSquared()>=1) continue;
            return p;
        }
    }

    public static Vec3 randomInHemisphere(Vec3 normal){
        Vec3 inUnitSphere = randomInUnitSphere();
        if (inUnitSphere.dot(normal) > 0.0) {
            return inUnitSphere;
        } else {
            return inUnitSphere.neg();
        }
    }
    
    public static Vec3 randomInUnitDisk() {
        while(true) {
            Vec3 p = new Vec3(Math.random()*2-1, Math.random()*2-1, 0);
            if (p.lengthSquared() >= 1) continue;
            return p;
        }
    }

    public double x() {return e[0];}
    public double y() {return e[1];}
    public double z() {return e[2];}
    public void print() {
        System.err.print(String.format("(%f, %f, %f)\n", e[0], e[1], e[2]));
    }
    public Vec3 neg() {
        return new Vec3(-e[0], -e[1], -e[2]);
    }

    public Vec3 add(Vec3 v) {
        return new Vec3(this.e[0]+v.e[0], this.e[1]+v.e[1], this.e[2]+v.e[2]);
    }

    public Vec3 sub(Vec3 v) {
        return this.add(v.neg());
    }

    public Vec3 mul(double t) {
        return new Vec3(this.e[0]*t, this.e[1]*t, this.e[2]*t);
    }

    public Vec3 div(double t) {
        return this.mul(1/t);
    }

    public double dot(Vec3 v) {
        return this.e[0]*v.e[0] + this.e[1]*v.e[1] + this.e[2]*v.e[2];
    }

    public Vec3 cross(Vec3 v) {
        return new Vec3(this.e[1] * v.e[2] - this.e[2] * v.e[1],
                        this.e[2] * v.e[0] - this.e[0] * v.e[2],
                        this.e[0] * v.e[1] - this.e[1] * v.e[0]);
    }

    public Vec3 unit() {
        return this.div(this.length());
    }

    public double lengthSquared() {
        return e[0]*e[0]+e[1]*e[1]+e[2]*e[2];
    }

    public double length() {
        return Math.sqrt(this.lengthSquared());
    }

    public boolean nearZero() {
        final double s = 1e-8;
        return (Math.abs(e[0]) < s) &&  (Math.abs(e[1]) < s) &&  (Math.abs(e[2]) < s);
    }

    public Vec3 reflect(Vec3 normal) {
        Vec3 uThis = this.unit();
        Vec3 uNormal = normal.unit();
        return uThis.add(uNormal.mul(-2*uThis.dot(uNormal)));
    }

    public Vec3 refract(Vec3 normal, double etaiOverEtaT){
        Vec3 uThis = this.unit();
        Vec3 uNormal = normal.unit();
        Vec3 rPerp = uNormal.mul(uThis.dot(uNormal));
        Vec3 rParallel = uThis.sub(rPerp);
        Vec3 tParallel = rParallel.mul(etaiOverEtaT);
        Vec3 tPerp = rPerp.unit().mul(Math.sqrt(1 - tParallel.lengthSquared()));
        return tParallel.add(tPerp);
    }

}
