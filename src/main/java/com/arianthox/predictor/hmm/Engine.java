package com.arianthox.predictor.hmm;

import java.io.*;
import java.util.*;

import com.arianthox.predictor.hmm.dsp.*;
import com.arianthox.predictor.hmm.dsp.FeatureExtraction;
import com.arianthox.predictor.hmm.vq.*;
import com.arianthox.predictor.hmm.hmm.Markov;



public class Engine {


    private String keys[];
    private boolean useHMM = false;
    private Markov hmmModels[];
    private CodeBook codebooks[];
    private int num_words = 0;

    public Engine(String dict, String folder){
        getDict(dict);
        
        codebooks = new CodeBook[num_words];
        for (int i = 0; i < num_words; i++){
            codebooks[i] = new CodeBook(folder + keys[i] + ".vq");
        }
        
    }
    
    public Engine(String dict, String pathForHMM, String pathForCodebook){
        useHMM = true;
        getDict(dict);
        
        codebooks = new CodeBook[1];
        codebooks[0] = new CodeBook(pathForCodebook);
        
        hmmModels = new Markov[num_words];
        for (int i = 0; i < num_words; i++){
            hmmModels[i] = new Markov(pathForHMM + keys[i] + ".hmm");
        }
    }
    
    public HashMap<String,Double> recognizeKey(short signal[]){
        short signalAfterEndPoint[] = EndPt.absCut(signal);

        double mfcc[][] = FeatureExtraction.process(signalAfterEndPoint);
        
        int iy=0,jx=0;
        for(double y[]:mfcc){
            for(double x:y){
                jx++;
            }
            iy++;
        }

        Point pts[] = new Point[mfcc.length];
        for (int index = 0; index < mfcc.length; index++){
            double temp[] = new double[mfcc[index].length - 1];
            
            for (int i = 1; i < mfcc[index].length; i++){
                temp[i - 1] = mfcc[index][i];
            }
            pts[index] = new Point(temp);
        }
        
        if (useHMM){

            int quantized[] = codebooks[0].quantize(pts);
            
            double probs[] = new double[keys.length];
            
            for (int j = 0; j < keys.length; j++){
                probs[j] = hmmModels[j].viterbi(quantized);
            }
            
            double highest = Double.NEGATIVE_INFINITY;
            int wordIndex = -1;
            
            for (int j = 0; j < keys.length; j++){
                if (probs[j] > highest){
                    highest = probs[j];
                    wordIndex = j;
                }
            }
            HashMap<String,Double> x=new HashMap<String,Double>();
            x.put(keys[wordIndex],0d);
            return x;
        }
        else{
            
            int lowest_index = -1;
            double lowest_dist = Double.MAX_VALUE;
            double lowest_avgDist=0;
            String lowest_word=new String();
            double dist = 0;
            double avgDist=0;
            
            
            HashMap<String,Double> wordList=new HashMap<String,Double>();
            HashMap<String,Double> wordAvg=new HashMap<String,Double>();
            HashMap<String,Double> resultList=new HashMap<String,Double>();
            
            
            for (int index = 0; index < keys.length; index++){
                dist = codebooks[index].getDistortion(pts);
                //System.out.println("Distorsion:"+dist+" Para la palabra:"+words[index]+"\n");
                wordList.put(keys[index],dist);
                
                if (dist < lowest_dist){
                    lowest_dist = dist;
                    lowest_index = index;
                    lowest_word= keys[index];
                    //System.out.println("Nueva Distorsion:"+lowest_dist+" Palabra mas Parecida:"+words[lowest_index]+"\n");
                }
            }
            
            Set set= wordList.keySet(); 
            Iterator iter = set.iterator(); 
            while(iter.hasNext()){
                String curWord=(String)iter.next();
                double curDist=(lowest_dist/((Double)wordList.get(curWord)).doubleValue()*100);
                //avgDist+=curDist;
                //System.out.println("Word:"+curWord+" Por:"+curDist);
                wordAvg.put(curWord,curDist);
            }
            //System.out.println("Suma:"+avgDist);
            //avgDist=avgDist/wordAvg.size();
            //lowest_avgDist=avgDist/(lowest_dist);
            //lowest_avgDist=(double)wordAvg.get(lowest_word)-(lowest_avgDist*10);
            //System.out.println("Tolerancia Superior a:"+lowest_avgDist);
            set= wordAvg.keySet(); 
            iter = set.iterator(); 
            while(iter.hasNext()){
                String curWord=(String)iter.next();
                double curAvg= (double)wordAvg.get(curWord);
                if(curAvg>90/*(lowest_avgDist)*/){
                    resultList.put(curWord,curAvg);
                    
                }
            }
            //System.out.println("Promedio de Aceptacion:"+(lowest_avgDist));
            return resultList;
        }
    }
    
     private void getDict(String dict){
        try{
            FileReader fr = new FileReader(dict);
            BufferedReader br = new BufferedReader(fr);
            String tmp = "";
            Vector<String> v = new Vector<String>(1, 1);
            num_words = 0;
            
            while ( (tmp = br.readLine()) != null ){
                num_words++;
                v.add(tmp);
            }
            
            keys = new String[num_words];
            for (int i = 0; i < num_words; i++){
                keys[i] = (String)v.elementAt(i);
            }
            
            br.close();
        }
        catch(Exception e){
            System.out.println("Error:"+e.getMessage());
        }
    }
}