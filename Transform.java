class Transform extends Hittable {
    private Vec3 transpose;
    private Mat3 rotateMatrix;
    private Mat3 rotateMatrixI;
    private Hittable obj;

    public Transform(Hittable obj, Vec3 transpose, Vec3 rotate) {
        this.transpose = transpose;
        this.obj = obj;
        this.rotateMatrix = Mat3.rz(rotate.z()).mul(Mat3.ry(rotate.y()).mul(Mat3.rx(rotate.x())));
        this.rotateMatrixI =  Mat3.rx(-rotate.x()).mul(Mat3.ry(-rotate.y()).mul(Mat3.rz(-rotate.z())));
    } 

    public HitRecord hit(Ray r, double tMin, double tMax) {
       Vec3 o = r.origin();
       Vec3 d = r.direction();

       o = o.sub(this.transpose);
       o = this.rotateMatrixI.mul(o);
       d = this.rotateMatrixI.mul(d);

       HitRecord modelSpaceHR = this.obj.hit(new Ray(o, d), tMin, tMax);
       
       if(modelSpaceHR.hit == false) return modelSpaceHR;

       Vec3 p = this.rotateMatrix.mul(modelSpaceHR.p).add(this.transpose);
       Vec3 n = this.rotateMatrix.mul(modelSpaceHR.normal); //Needs adjustment if includes other non rigid transforms in the future

       return new HitRecord(
               true,
               modelSpaceHR.t,
               p,
               n,
               modelSpaceHR.frontFace,
               modelSpaceHR.mat);
    }
}
