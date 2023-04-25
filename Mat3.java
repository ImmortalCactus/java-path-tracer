class Mat3 {
    private double[][] m = new double[3][3];
    
    public Mat3(double[][] m){
        this.m = m.clone();
    }

    public Mat3 t(){
        double[][] a = new double[3][3];
        for(int i=0; i<3; i++) for(int j=0; j<3; j++) a[i][j] = m[j][i];
        return new Mat3(a);
    }

    public Vec3 mul(Vec3 v){
        return new Vec3(
            m[0][0]*v.x() +  m[0][1]*v.y() + m[0][2]*v.z(),
            m[1][0]*v.x() +  m[1][1]*v.y() + m[1][2]*v.z(),
            m[2][0]*v.x() +  m[2][1]*v.y() + m[2][2]*v.z()
        );
    }

    public Mat3 mul(Mat3 b){
        double[][] c = new double[3][3];
        for(int i=0; i<3; i++) for(int j=0; j<3; j++){
            c[i][j] = this.m[i][0] * b.m[0][j] + this.m[i][1] * b.m[1][j] + this.m[i][2] * b.m[2][j];
        }
        return new Mat3(c);
    }

    public static Mat3 rx(double deg){
        double ang = Math.toRadians(deg);
        double[][] a = {
            {1, 0, 0},
            {0, Math.cos(ang), -Math.sin(ang)},
            {0, Math.sin(ang), Math.cos(ang)}
        };
        return new Mat3(a);
    }

    public static Mat3 ry(double deg){
        double ang = Math.toRadians(deg);
        double[][] a = {
            {Math.cos(ang), 0, Math.sin(ang)},
            {0, 1, 0},
            {-Math.sin(ang), 0, Math.cos(ang)}
        };
        return new Mat3(a);
    }

    public static Mat3 rz(double deg){
        double ang = Math.toRadians(deg);
        double[][] a = {
            {Math.cos(ang), -Math.sin(ang), 0},
            {Math.sin(ang), Math.cos(ang), 0},
            {0, 0, 1}
        };
        return new Mat3(a);
    }

    public void print() {
        for(int i=0; i<3; i++) {
            for(int j=0; j<3; j++){
                System.err.print(m[i][j]+" ");
            }
            System.err.print("\n");
        }
    }
}
