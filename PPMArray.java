public class PPMArray {
    double[][][] colorPixels;
    int width;
    int height;
    int numSamples;

    public PPMArray(int h, int w, int s) {
        colorPixels = new double[h][w][3];
        width = w;
        height = h;
        numSamples = s;
    }

    synchronized public void addPixel(int hIndex, int wIndex, Vec3 color) {
        colorPixels[hIndex][wIndex][0] += color.x();
        colorPixels[hIndex][wIndex][1] += color.y();
        colorPixels[hIndex][wIndex][2] += color.z();
    }

    public void print() {
        System.out.print("P3\n" + width + " " + height + "\n255\n");
        for (int j = 0; j<height; j++) {
            for (int i = 0; i < width; i++) {
                writeColor(j, i);
            }
        }
    }

    private void writeColor(int hIndex, int wIndex) {
        double[] colorSum = colorPixels[hIndex][wIndex];

        double r = colorSum[0];
        double g = colorSum[1];
        double b = colorSum[2];

        double scale = 1.0 / numSamples;
        double gamma = 2.2;
        r = Math.pow(r * scale, 1/gamma);
        g = Math.pow(g * scale, 1/gamma);
        b = Math.pow(b * scale, 1/gamma);
        
        r = (int)(256 * Math.max(Math.min(r, 0.999), 0.0));
        g = (int)(256 * Math.max(Math.min(g, 0.999), 0.0));
        b = (int)(256 * Math.max(Math.min(b, 0.999), 0.0));

        System.out.print(r+" "+g+" "+b+"\n");
    }
}
