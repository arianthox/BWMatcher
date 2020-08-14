
package com.arianthox.predictor.hmm.dsp;

public class FeatureExtraction {

    protected final static double samplingRate = 16000.0;
    protected final static int frameLength = 512;
    protected final static int shiftInterval = frameLength / 2;
    protected final static int numCepstra = 13;
    protected final static int fftSize = frameLength;
    protected final static double preEmphasisAlpha = 0.95;
    protected final static double lowerFilterFreq = 133.3334;
    protected final static double upperFilterFreq = 6855.4976;
    protected final static int numMelFilters = 23;
    protected static double frames[][];
    protected static double hammingWindow[];
    protected static FourierTransform FourierTransform;

    public static double[][] process(int inputSignal[]) {
        double MFCC[][];

        double outputSignal[] = preEmphasis(inputSignal);

        framing(outputSignal);

        MFCC = new double[frames.length][numCepstra];

        hammingWindow();

        for (int k = 0; k < frames.length; k++) {
            FourierTransform = new FourierTransform();

            double bin[] = magnitudeSpectrum(frames[k]);

            int cbin[] = fftBinIndices();
            double fbank[] = melFilter(bin, cbin);
            double f[] = nonLinearTransformation(fbank);

            double cepc[] = cepCoefficients(f);
            for (int i = 0; i < numCepstra; i++) {
                MFCC[k][i] = cepc[i];
            }
        }

        return MFCC;
    }

    private static int[] fftBinIndices() {
        int cbin[] = new int[numMelFilters + 2];

        cbin[0] = (int) Math.round(lowerFilterFreq / samplingRate * fftSize);
        cbin[cbin.length - 1] = (int) (fftSize / 2);

        for (int i = 1; i <= numMelFilters; i++) {
            double fc = centerFreq(i);
            cbin[i] = (int) Math.round(fc / samplingRate * fftSize);
        }

        return cbin;
    }

    private static double[] melFilter(double bin[], int cbin[]) {
        double temp[] = new double[numMelFilters + 2];

        for (int k = 1; k <= numMelFilters; k++) {
            double num1 = 0, num2 = 0;

            for (int i = cbin[k - 1]; i <= cbin[k]; i++) {
                num1 += ((i - cbin[k - 1] + 1) / (cbin[k] - cbin[k - 1] + 1)) * bin[i];
            }

            for (int i = cbin[k] + 1; i <= cbin[k + 1]; i++) {
                num2 += (1 - ((i - cbin[k]) / (cbin[k + 1] - cbin[k] + 1))) * bin[i];
            }

            temp[k] = num1 + num2;
        }

        double fbank[] = new double[numMelFilters];
        for (int i = 0; i < numMelFilters; i++) {
            fbank[i] = temp[i + 1];
        }

        return fbank;
    }

    private static double[] cepCoefficients(double f[]) {
        double cepc[] = new double[numCepstra];

        for (int i = 0; i < cepc.length; i++) {
            for (int j = 1; j <= numMelFilters; j++) {
                cepc[i] += f[j - 1] * Math.cos(Math.PI * i / numMelFilters * (j - 0.5));
            }
        }

        return cepc;
    }

    private static double[] nonLinearTransformation(double[] fbank) {
        double f[] = new double[fbank.length];
        final double FLOOR = -50;

        for (int i = 0; i < fbank.length; i++) {
            f[i] = Math.log(fbank[i]);

            if (f[i] < FLOOR) f[i] = FLOOR;
        }

        return f;
    }

    protected static double log10(double value) {
        return Math.log(value) / Math.log(10);
    }

    private static double centerFreq(int i) {
        double mel[] = new double[2];
        mel[0] = freqToMel(lowerFilterFreq);
        mel[1] = freqToMel(samplingRate / 2);

        double temp = mel[0] + ((mel[1] - mel[0]) / (numMelFilters + 1)) * i;
        return inverseMel(temp);
    }

    private static double inverseMel(double x) {
        double temp = Math.pow(10, x / 2595) - 1;
        return 700 * (temp);
    }

    protected static double freqToMel(double freq) {
        return 2595 * log10(1 + freq / 700);
    }

    protected static double[] magnitudeSpectrum(double frame[]) {
        double magSpectrum[] = new double[frame.length];

        FourierTransform.computeFFT(frame);

        for (int k = 0; k < frame.length; k++) {
            magSpectrum[k] = Math.pow(FourierTransform.real[k] * FourierTransform.real[k] + FourierTransform.imag[k] * FourierTransform.imag[k], 0.5);
        }

        return magSpectrum;
    }

    private static void hammingWindow() {
        double w[] = new double[frameLength];
        for (int n = 0; n < frameLength; n++) {
            w[n] = 0.54 - 0.46 * Math.cos((2 * Math.PI * n) / (frameLength - 1));
        }
        for (int m = 0; m < frames.length; m++) {
            for (int n = 0; n < frameLength; n++) {
                frames[m][n] *= w[n];
            }
        }
    }

    protected static void framing(double inputSignal[]) {
        double numFrames = (double) inputSignal.length / (double) (frameLength - shiftInterval);

        if ((numFrames / (int) numFrames) != 1) {
            numFrames = (int) numFrames + 1;
        }

        double paddedSignal[] = new double[(int) numFrames * frameLength];
        for (int n = 0; n < inputSignal.length; n++) {
            paddedSignal[n] = inputSignal[n];
        }

        frames = new double[(int) numFrames][frameLength];

        for (int m = 0; m < numFrames; m++) {
            for (int n = 0; n < frameLength; n++) {
                frames[m][n] = paddedSignal[m * (frameLength - shiftInterval) + n];
            }
        }
    }

    protected static double[] preEmphasis(int inputSignal[]) {

        double outputSignal[] = new double[inputSignal.length];

        for (int n = 1; n < inputSignal.length; n++) {
            outputSignal[n] = inputSignal[n] - preEmphasisAlpha * inputSignal[n - 1];
        }

        return outputSignal;
    }
}
