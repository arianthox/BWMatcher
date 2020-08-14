
package com.arianthox.predictor.hmm.dsp;

public class EndPt {

    final static int frameSize = 80;


    public static int[] absCut(int sample[]) {
        if (sample.length > 3200) {
            boolean crossingBoolean[] = zeroCrossingBoolean(sample);
            int energy[] = avgEnergy(sample);
            int crossing[] = zeroCrossing(sample, energy, crossingBoolean);
            int chopped[] = chopping(sample, crossing);
            return chopped;
        } else {
            return sample;
        }
    }

    public static int[] chopping(int sample[], int cut[]) {

        cut[0] *= frameSize;
        cut[1] *= frameSize;
        int chopFile[] = new int[cut[1] - cut[0]];
        for (int c = 0; c < chopFile.length; c++) {
            chopFile[c] = sample[cut[0] + c];
        }

        return chopFile;
    }

    public static int[] avgEnergy(int sample[]) {

        final double energyConst = 1.95;
        int energyCut[] = new int[2];
        double energyFrame[] = new double[(int) (sample.length / frameSize)];
        double runningSum = 0;
        double noiseEnergy = 0;
        double noiseEnergyThreshold = 0;
        int location = 0;
        int backwardLocation = 0;
        boolean belowThreshold = true;
        boolean valleyFound = true;

        energyLoopCheck(sample, energyFrame);

        runningSum = 0;
        for (int c = 0; c < 20; c++) {
            runningSum += energyFrame[c];
        }
        noiseEnergy = runningSum / 20;
        noiseEnergyThreshold = noiseEnergy * energyConst;
        energyCut[1] = energyFrame.length - 22;
        energyCut[0] = 20;

        location = 20;
        belowThreshold = true;
        while ((location < (energyFrame.length - 36)) && (belowThreshold)) {

            if (energyFrame[location] > noiseEnergyThreshold) {

                runningSum = 0;
                for (int c = 1; c < 17; c++) {
                    if (energyFrame[location + c] > noiseEnergyThreshold) {
                        runningSum++;
                    }
                }

                if (runningSum >= 13) {
                    belowThreshold = false;
                    energyCut[0] = location;

                    valleyFound = true;
                    backwardLocation = 0;
                    while ((valleyFound) && (backwardLocation < 16) && ((location - backwardLocation) > 20)) {

                        if (energyFrame[location - backwardLocation - 1] < energyFrame[location - backwardLocation]) {
                            energyCut[0] = location - backwardLocation - 1;
                        } else {
                            valleyFound = false;
                        }
                        backwardLocation++;
                    }
                }
            }
            location++;
        }

        runningSum = 0;
        for (int c = energyFrame.length - 21; c < energyFrame.length; c++) {
            runningSum += energyFrame[c];
        }
        noiseEnergy = runningSum / 20;
        noiseEnergyThreshold = noiseEnergy * energyConst;
        energyCut[1] = energyFrame.length - 22;


        location = energyFrame.length - 22;
        belowThreshold = true;
        while ((location > 35) && (belowThreshold)) {
            if (energyFrame[location] > noiseEnergyThreshold) {
                runningSum = 0;
                for (int c = 1; c < 17; c++) {
                    if (energyFrame[location - c] > noiseEnergyThreshold) {
                        runningSum++;
                    }
                }
                if (runningSum >= 13) {
                    belowThreshold = false;
                    energyCut[1] = location;


                    valleyFound = true;
                    backwardLocation = 0;
                    while ((valleyFound) && (backwardLocation < 16) && ((location + backwardLocation) < (energyFrame.length - 22))) {

                        if (energyFrame[location + backwardLocation + 1] < energyFrame[location + backwardLocation]) {
                            energyCut[1] = location + backwardLocation + 1;
                        } else {
                            valleyFound = false;
                        }
                        backwardLocation++;
                    }
                    energyCut[1]++;
                }
            }
            location--;
        }
        return energyCut;
    }

    private static void energyLoopCheck(int[] sample, double[] energyFrame) {
        double runningSum;
        for (int c = 0; c < energyFrame.length; c++) {
            runningSum = 0;
            for (int d = c * frameSize; d < (c + 1) * frameSize; d++) {
                runningSum += (sample[d] * sample[d]);
            }
            energyFrame[c] = runningSum / frameSize;
        }
    }

    private static double[] energyGraph(int sample[]) {
        double energyFrame[] = new double[(int) (sample.length / frameSize)];
        energyLoopCheck(sample, energyFrame);
        return energyFrame;
    }

    public static boolean[] zeroCrossingBoolean(int sample[]) {
        boolean crossingBoolean[] = new boolean[sample.length];
        for (int c = 0; c < sample.length - 1; c++) {
            if (((sample[c] > 0) && (sample[c + 1] < 0)) || ((sample[c] < 0) && (sample[c + 1] > 0))) {
                crossingBoolean[c] = true;
            }
        }
        return crossingBoolean;
    }


    public static int[] zeroCrossing(int sample[], int energy[], boolean crossing[]) {
        final double crossingConst = 12.5;

        int crossingCut[] = new int[2];
        crossingCut[0] = energy[0];
        crossingCut[1] = energy[1] - 1;

        double crossingFrame[] = new double[(int) (sample.length / frameSize)];

        double crossingSD = 0;
        double IZC = 0;
        double IZCT = 0.15625;

        int crossingPeak = 0;

        double runningSum = 0;

        int location = 0;

        crossingFrameCheck(crossing, crossingFrame);

        runningSum = 0;
        for (int c = 0; c < 20; c++) {
            runningSum += crossingFrame[c];
        }
        IZC = runningSum / 20;

        runningSum = 0;
        for (int c = 0; c < 20; c++) {
            runningSum += crossingFrame[c] * crossingFrame[c];
        }
        crossingSD = Math.sqrt((runningSum / 20) - (IZC * IZC));

        if ((0.15625) > (IZC * 2 * crossingSD)) {
            IZCT = IZC * 2 * crossingSD;
        }

        IZCT *= crossingConst;

        location = crossingCut[0] - 16;
        if (location < 20) {
            location = 20;
        }

        crossingPeak = location;
        while (location != crossingCut[0]) {
            if (crossingFrame[crossingPeak] < crossingFrame[location]) {
                crossingPeak = location;
            }
            location++;
        }

        if (IZCT < crossingFrame[crossingPeak]) {
            crossingCut[0] = crossingPeak;
            location = crossingCut[0] - 10;
            if (location < 20) {
                location = 20;
            }

            crossingPeak = location;
            while (location != crossingCut[0]) {
                if (crossingFrame[crossingPeak] >= crossingFrame[location]) {
                    crossingPeak = location;
                }
                location++;
            }
            crossingCut[0] = crossingPeak;

        }

        runningSum = 0;
        for (int c = crossingFrame.length - 21; c < crossingFrame.length; c++) {
            runningSum += crossingFrame[c];
        }

        IZC = runningSum / 20;

        runningSum = 0;
        for (int c = crossingFrame.length - 21; c < crossingFrame.length; c++) {
            runningSum += crossingFrame[c] * crossingFrame[c];
        }

        crossingSD = Math.sqrt((runningSum / 20) - (IZC * IZC));

        IZCT = 0.15625;
        if ((0.15625) > (IZC * 2 * crossingSD)) {
            IZCT = IZC * 2 * crossingSD;
        }

        IZCT *= crossingConst;

        location = crossingCut[1] + 16;

        if (location > crossingFrame.length - 22) {
            location = crossingFrame.length - 22;
        }

        crossingPeak = location;
        while (location != crossingCut[1]) {
            if (crossingFrame[crossingPeak] < crossingFrame[location]) {
                crossingPeak = location;
            }
            location--;
        }

        if (IZCT < crossingFrame[crossingPeak]) {
            crossingCut[1] = crossingPeak;
            location = crossingCut[1] + 10;
            if (location > crossingFrame.length - 22) {
                location = crossingFrame.length - 22;
            }
            crossingPeak = location;
            while (location != crossingCut[1]) {
                if (crossingFrame[crossingPeak] >= crossingFrame[location]) {
                    crossingPeak = location;
                }
                location--;
            }
            crossingCut[1] = crossingPeak;

        }
        crossingCut[1]++;

        return crossingCut;
    }

    private static void crossingFrameCheck(boolean[] crossing, double[] crossingFrame) {
        double runningSum;
        for (int c = 0; c < crossingFrame.length; c++) {
            runningSum = 0;
            for (int d = c * frameSize; d < (c + 1) * frameSize; d++) {
                if (crossing[d]) {
                    runningSum++;
                }
            }
            crossingFrame[c] = runningSum / frameSize;
        }
    }

    private static double[] crossingGraph(int sample[]) {

        boolean crossing[] = zeroCrossingBoolean(sample);
        double crossingFrame[] = new double[(sample.length / frameSize)];

        crossingFrameCheck(crossing, crossingFrame);
        return crossingFrame;
    }
}
