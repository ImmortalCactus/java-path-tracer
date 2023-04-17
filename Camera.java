class Camera {
    private Vec3 origin;
    private Vec3 lowerLeftCorner;
    private Vec3 horizontal;
    private Vec3 vertical;
    private Vec3 u, v, w;
    private double lensRadius;

    public Camera(
            Vec3 lookFrom,
            Vec3 lookAt,
            Vec3 vUp,
            double vFOV,
            double aspectRatio,
            double aperture,
            double focusDist) {
        double theta = Math.toRadians(vFOV);
        double h = Math.tan(theta/2);
        double viewportHeight = 2.0 * h;
        double viewportWidth = aspectRatio * viewportHeight;

        w = lookFrom.sub(lookAt).unit();
        u = vUp.cross(w).unit();
        v = w.cross(u);

        origin = lookFrom;
        horizontal = u.mul(focusDist * viewportWidth);

        vertical = v.mul(focusDist * viewportHeight);

        lowerLeftCorner = origin.sub(horizontal.div(2.0)).sub(vertical.div(2.0)).sub(w.mul(focusDist));

        lensRadius = aperture / 2.0;
    }

    Ray getRay(double s, double t) {
        return new Ray(origin, lowerLeftCorner.add(horizontal.mul(s)).add(vertical.mul(t)).sub(origin));
    }
}
