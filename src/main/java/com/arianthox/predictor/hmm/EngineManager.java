package com.arianthox.predictor.hmm;

import com.arianthox.predictor.hmm.dsp.EndPt;
import com.arianthox.predictor.hmm.dsp.FeatureExtraction;
import com.arianthox.predictor.hmm.engine.CodeBook;
import com.arianthox.predictor.hmm.engine.HiddenMarkov;
import com.arianthox.predictor.hmm.persistence.PatternFileIO;
import com.arianthox.predictor.hmm.vq.Points;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.extern.java.Log;
import org.springframework.stereotype.Component;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/*
 * @author: r.sanchez
 */
@Component
@Log
public class EngineManager {


    private final transient HashMap<String, Tuple2<CodeBook, HiddenMarkov>> codeBookHashMap = new HashMap<>();

    private final static String BASE_PATH = "./hmm/";

    private final static String VQL_EXT = ".vql";

    private final static String VQ_EXT = ".vq";

    private final static String UVQ_EXT = ".uvq";

    private final static String HMM_EXT = ".hmm";

    private final static String VQL_FILE = "index" + VQL_EXT;

    private final transient Path vqlFile = Paths.get(BASE_PATH + VQL_FILE);

    private final transient Path basePath = Paths.get(BASE_PATH);



    private final transient PatternFileIO patternFileIO;

    public EngineManager(PatternFileIO patternFileIO) {
        this.patternFileIO = patternFileIO;
        List<String> keys = loadKeys();
        keys.stream().forEach(key -> {
            try {
                codeBookHashMap.put(key, Tuples.of(new CodeBook(Paths.get(BASE_PATH + key + VQ_EXT)), new HiddenMarkov(Paths.get(BASE_PATH + key + HMM_EXT))));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


    }

    private void initialize() {
        try {

            if (!Files.exists(basePath, LinkOption.NOFOLLOW_LINKS)) {
                Files.createDirectory(basePath);
            }

            if (!Files.exists(vqlFile, LinkOption.NOFOLLOW_LINKS)) {
                Files.createFile(vqlFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addKey(String key, List<int[]> sampleList) {


        int index = sampleList.size();
        int[][] samples = new int[index][];
        while (index >= 1) {
            index--;
            samples[index] = sampleList.get(index);
        }

        int i = 0;
        Vector<String> vector = new Vector<String>(1, 1);
        for (int j = 0; j < sampleList.size(); j++) {
            int absCut[] = EndPt.absCut(samples[j]);
            double featureExtracted[][] = FeatureExtraction.process(absCut);
            for (int i1 = 0; i1 < featureExtracted.length; i1++) {
                String s = "";
                for (int k1 = 1; k1 < featureExtracted[i1].length; k1++)
                    s = s + featureExtracted[i1][k1] + " ";
                vector.add(s);
                i++;
            }
        }
        double ad[][] = new double[i][12];
        for (int k = 0; k < i; k++) {
            StringTokenizer stringtokenizer = new StringTokenizer((String) vector.elementAt(k));
            for (int j1 = 0; j1 < 12; j1++)
                ad[k][j1] = Double.parseDouble(stringtokenizer.nextToken());
        }
        Points[] points = new Points[i];
        for (int l = 0; l < i; l++)
            points[l] = new Points(ad[l]);

        try {
            CodeBook codeBook = new CodeBook(points);

            codeBook.setPatternFileIO(patternFileIO);

            codeBook.save(Paths.get(BASE_PATH + key + VQ_EXT));

            codeBook.saveUVQ(Paths.get(BASE_PATH + key + UVQ_EXT), samples);

            HiddenMarkov hiddenMarkov = codeBook.generateHiddenMarkov();

            hiddenMarkov.setPatternFileIO(patternFileIO);

            hiddenMarkov.save(Paths.get(BASE_PATH + key + HMM_EXT));

            addKey(key, codeBook, hiddenMarkov);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> loadKeys() {
        initialize();
        List<String> keys = new ArrayList<>();

        try (BufferedReader bufferedReader = Files.newBufferedReader(vqlFile)) {
            String key;
            while ((key = bufferedReader.readLine()) != null) {
                keys.add(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keys;
    }

    private void appendKey(String key) {
        try (BufferedWriter writer = Files.newBufferedWriter(vqlFile, StandardOpenOption.APPEND)) {
            try {
                writer.write(key);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void addKey(String key, final CodeBook codeBook, final HiddenMarkov hiddenMarkov) {
        loadKeys().stream().filter(s -> s.equalsIgnoreCase(key)).findAny().ifPresentOrElse(s -> {
        }, () -> {
            appendKey(key);
            codeBookHashMap.put(key, Tuples.of(codeBook, hiddenMarkov));
        });

    }


    public HashMap<String, Double> matchKey(int signal[]) {
        int signalAfterEndPoints[] = EndPt.absCut(signal);

        double[][] mfcc = FeatureExtraction.process(signalAfterEndPoints);

        Points pts[] = new Points[mfcc.length];
        for (int index = 0; index < mfcc.length; index++) {
            double temp[] = new double[mfcc[index].length - 1];

            for (int i = 1; i < mfcc[index].length; i++) {
                temp[i - 1] = mfcc[index][i];
            }
            pts[index] = new Points(temp);
        }


        final AtomicDouble lowestDistortion = new AtomicDouble(Double.MAX_VALUE);
        final AtomicReference<Map.Entry<String, Tuple2<CodeBook, HiddenMarkov>>> entryAtomicReference = new AtomicReference<>();


        HashMap<String, Double> distortionMap = new HashMap<>();
        HashMap<String, Double> viterbiMap = new HashMap<>();
        HashMap<String, Double> distortionAvgMap = new HashMap<>();


        codeBookHashMap.entrySet().stream().forEach(entry -> {
            CodeBook codeBook = entry.getValue().getT1();
            double distortion = codeBook.getDistortion(pts);
            int[] quantized = codeBook.quantize(pts);

            double viterbi = entry.getValue().getT2().viterbi(quantized);
            double probs = entry.getValue().getT2().getProbability(quantized);
            viterbiMap.put(entry.getKey(), viterbi);

            Logger.getAnonymousLogger().fine(String.format("Key:%s - Distortion: %f - Viterbi: %f - Probs: %f", entry.getKey(), distortion, viterbi, probs));

            distortionMap.put(entry.getKey(), distortion);
            if (distortion < lowestDistortion.get()) {
                lowestDistortion.set(distortion);
                entryAtomicReference.set(entry);
            }
        });

        distortionMap.entrySet().stream().forEach(entry -> {
            double avgDistortion = (lowestDistortion.get() / distortionMap.get(entry.getKey()).doubleValue());
            Logger.getAnonymousLogger().fine(String.format("Key:%s - Avg Distortion: %f", entry.getKey(), avgDistortion));
            distortionAvgMap.put(entry.getKey(), avgDistortion);
        });

        return distortionAvgMap.entrySet().stream().filter(stringDoubleEntry -> stringDoubleEntry.getValue() > 0.9).collect(Collectors.toMap(o -> o.getKey(), o -> o.getValue(), (aDouble, aDouble2) -> aDouble
                , HashMap::new));


    }

}
