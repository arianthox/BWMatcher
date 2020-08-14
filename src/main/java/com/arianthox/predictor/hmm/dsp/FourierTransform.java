package com.arianthox.predictor.hmm.dsp;


public class FourierTransform {
    protected static int numPoints;
    public static double real[];
    public static double imag[];

    /**
     * Compute Furier<br>
     *
     * @param signal
     */
    public static void computeFFT(double signal[]) {
        numPoints = signal.length;

        real = new double[numPoints];
        imag = new double[numPoints];

        real = signal;

        for (int i = 0; i < imag.length; i++) {
            imag[i] = 0;
        }
        FFT();
    }

    /**
     * Furier<br>
     */
    private static void FFT() {
        if (numPoints == 1) return;

        final double pi = Math.PI;
        final int numStages = (int) (Math.log(numPoints) / Math.log(2));

        int halfNumPoints = numPoints >> 1;
        int j = halfNumPoints;

        int k = 0;
        for (int i = 1; i < numPoints - 2; i++) {
            if (i < j) {
                double tempReal = real[j];
                double tempImag = imag[j];
                real[j] = real[i];
                imag[j] = imag[i];
                real[i] = tempReal;
                imag[i] = tempImag;
            }

            k = halfNumPoints;

            while (k <= j) {
                j -= k;
                k >>= 1;
            }

            j += k;
        }

        for (int stage = 1; stage <= numStages; stage++) {

            int LE = 1;
            for (int i = 0; i < stage; i++)
                LE <<= 1;

            int LE2 = LE >> 1;
            double UR = 1;
            double UI = 0;

            double SR = Math.cos(pi / LE2);
            double SI = -Math.sin(pi / LE2);

            for (int subDFT = 1; subDFT <= LE2; subDFT++) {
                for (int butterfly = subDFT - 1; butterfly <= numPoints - 1; butterfly += LE) {
                    int ip = butterfly + LE2;

                    double tempReal = real[ip] * UR - imag[ip] * UI;
                    double tempImag = real[ip] * UI + imag[ip] * UR;
                    real[ip] = real[butterfly] - tempReal;
                    imag[ip] = imag[butterfly] - tempImag;
                    real[butterfly] += tempReal;
                    imag[butterfly] += tempImag;
                }

                double tempUR = UR;
                UR = tempUR * SR - UI * SI;
                UI = tempUR * SI + UI * SR;
            }
        }
    }
}
