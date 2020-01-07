package com.arianthox.predictor.hmm;


import com.arianthox.predictor.hmm.dsp.EndPt;
import com.arianthox.predictor.hmm.dsp.FeatureExtraction;
import com.arianthox.predictor.hmm.vq.CodeBook;
import com.arianthox.predictor.hmm.vq.Point;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

@Component
public class EngineManager {


    private Engine engine = new Engine("./index.vql","./storage/");;


    private RandomAccessFile vqlFile;

    private void addKey(String key){
        try {
            try {
                vqlFile =new RandomAccessFile("./index.vql","rw");
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
            vqlFile.seek(vqlFile.length());
            vqlFile.writeBytes("\r\n");
            vqlFile.writeBytes(key);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            vqlFile.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        engine = new Engine("./index.vql","./storage/");
    }

    public void trainKey(String key,List<short[]> allSamples){

        short[][] samples;
        int index=allSamples.size();

        samples = new short[index][];
        while(index>=1){
            index--;
            samples[index]=allSamples.get(index);
        }

        int i = 0;
        Vector<String> vector = new Vector<String>(1, 1);
        for(int j = 0; j < allSamples.size(); j++) {
            short absCut[] = EndPt.absCut(samples[j]);
            double featureExtracted[][] = FeatureExtraction.process(absCut);
            for(int i1 = 0; i1 < featureExtracted.length; i1++) {
                String s = "";
                for(int k1 = 1; k1 < featureExtracted[i1].length; k1++)
                    s = s + featureExtracted[i1][k1] + " ";
                vector.add(s);
                i++;
            }
        }
        double ad[][] = new double[i][12];
        for(int k = 0; k < i; k++) {
            StringTokenizer stringtokenizer = new StringTokenizer((String)vector.elementAt(k));
            for(int j1 = 0; j1 < 12; j1++)
                ad[k][j1] = Double.parseDouble(stringtokenizer.nextToken());
        }
        Point apoint[] = new Point[i];
        for(int l = 0; l < i; l++)
            apoint[l] = new Point(ad[l]);
        try{
            CodeBook codebook1 = new CodeBook(apoint);
            codebook1.saveToFile(key+".vq");
            codebook1.saveToFileUVQ(key+".uvq",samples);

            addKey(key);

        }catch(java.lang.NullPointerException e){
            e.printStackTrace();
        }

    }


//
//    private void reTraining(String key,short[] sample) {
//        FileReader fr;
//        Vector<short[]> allSamples=new Vector<short[]>();
//
//        File fileURecord
//
//        try {
//
//            if(fileURecord.exists()){
//
//                fr = new FileReader(fileURecord.getPath());
//                BufferedReader br = new BufferedReader(fr);
//                String row;
//                while((row=br.readLine())!=null){
//                    StringTokenizer st = new StringTokenizer(row);
//                    String col;
//                    short[] linea=new short[st.countTokens()];
//                    int token=0;
//                    int count=st.countTokens();
//                    while(token<count){
//                        col=st.nextToken();
//                        linea[token]=Short.parseShort(col);
//                        token++;
//                    }
//                    allSamples.add(linea);
//                }
//            }
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        //Incorpora la nueva muestra
//        allSamples.add(sample);
//
//
//        int index=allSamples.size();
//        samples = new short[index][];
//        while(index>=1){
//            index--;
//            samples[index]=allSamples.get(index);
//        }
//
//
//        int i = 0;
//        Vector<String> vector = new Vector<String>(1, 1);
//        for(int j = 0; j < num_samples; j++) {
//            short aword0[] = EndPt.absCut(samples[j]);
//            double ad1[][] = FeatureExtraction.process(aword0);
//            for(int i1 = 0; i1 < ad1.length; i1++) {
//                String s = "";
//                for(int k1 = 1; k1 < ad1[i1].length; k1++)
//                    s = s + ad1[i1][k1] + " ";
//                vector.add(s);
//                i++;
//            }
//        }
//        double ad[][] = new double[i][12];
//        for(int k = 0; k < i; k++) {
//            StringTokenizer stringtokenizer = new StringTokenizer((String)vector.elementAt(k));
//            for(int j1 = 0; j1 < 12; j1++)
//                ad[k][j1] = Double.parseDouble(stringtokenizer.nextToken());
//        }
//        Point apoint[] = new Point[i];
//        for(int l = 0; l < i; l++)
//            apoint[l] = new Point(ad[l]);
//        try{
//            CodeBook codebook1 = new CodeBook(apoint);
//            codebook1.saveToFile(fileRecord.getPath());
//            codebook1.saveToFileUVQ(fileURecord.getPath(),samples);
//            if(this.edit==false){
//                addWord(recordField.getText().trim());
//            }
//        }catch(java.lang.NullPointerException e){
//            //System.out.println("run line 678");
//        }
//        jLabelProcesar.setText("Comando Entrenado");
//        try {
//            Thread.sleep(300);
//        } catch(java.lang.InterruptedException e) {}
//
//        samples = (short[][])null;
//        samples = new short[num_samples][];
//
//        jLabelProcesar.setText("En Espera...");
//
//    }




}

