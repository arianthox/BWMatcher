   
package com.arianthox.predictor.hmm.audio;
  
import java.io.ByteArrayOutputStream;
import java.util.Vector;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.swing.JSlider;
import javax.swing.JToggleButton;


/**
 * La clase <B>MicInput</B> es el pilar de la aplicacin ya que esta clase
 * se ecncarga de reconocer el sonido y despreciar el ruido del ambiente
 * con esta clase se establese comunicacin por medio de la interfaz <A HREF="../../markov/reconocedor/dispocitivo/InterfaceMicInput.html" title="clase in markov.reconocedor.dispocitivo">InterfaceMicInput</A>
 * la cual es recibida en su unico constructor y se toma como referencia en 
 * el atributo pcm.
 * 
 * <h2>Como funciona esta clase?</h2>
 * 
 * En esta clase existen dos hilos que interactuar con el fin de obtener el 
 * sonido que pronuncia el usuario final, los dos hilos son los siguientes :
 * la misma clase MicInput es un hilo, el otro es una clase interna cuyo nombre
 * es HiloReconocedorDeRuido, como su nombre lo indica esta clase se encarga de
 * determinar que es ruido de ambiente y que es sonido producido por el usuario,
 * primero se lansa el hilo de la clase MicInput y este lansa el hilo HiloReconocedorDeRuido
 * despus de que MicInput lansa HiloReconocedorDeRuido el hilo MicInput duerme
 * hasta que el HiloReconocedorDeRuido detecta sonido producido por el usuario final
 * el HiloReconocedorDeRuido despierta a MicInput y MicInput alamacena el sonido
 * producido por el usuario final hasta que HiloReconocedor de ruido detecta que
 * el usuario final ya no pronuncia mas sonidos, entonces MicInput mata al 
 * HiloReconocedorDeRuido y termina su ejecucion. 
 * <br><br>
 * <B>Proyecto:</B> Herramienta Software capaz de interpretar comandos hablados
 *           para el Sistema Opertivo Linux dirigida a personas discapacitadas
 * @author Ricardo Sanchez Delgadp
 * @author Pilar Katherin Quintero
 * @author Diego Rincon
 * @version 1.0 3 Enero de 2006
 * 
 */  
public class MicInput extends Thread {
    /**
     * Este atributo permite capturar la referencia de la clase que ha
     * sobreescrito la interfaz <A HREF="../../markov/reconocedor/dispocitivo/InterfaceMicInput.html" title="clase in markov.reconocedor.dispocitivo">InterfaceMicInput</A>
     * sobre escrita en la clase que desee utilizar esta clase
     */ 
    private InterfaceMicInput pcm;
    /**
     * Unico Constructor de la clase, en el se inician todas las variables de la clase
     * atributos como <b>info</b> que contendra informacion concerniente a la targeta de sonido
     * o vBufferWord que contendra los arreglos que se creen cada vez que se
     * pronuncie una palabra.
     * @param pcm es un argumento de tipo InterfaceMicInput que se asigna al atributo
     * pcm existente en esta clase.
     */    
    public MicInput(InterfaceMicInput pcm) {
        info = new javax.sound.sampled.DataLine.Info(javax.sound.sampled.TargetDataLine.class, format);
        this.pcm=pcm;
        avgEnergyThreshold = 0.0D;
        avgNoiseEnergy = 0.0D;
        byteBufferRead = new byte[3200];
        byteRealBufferRead = new byte[3200];
        numByteRead = 0;
        bufferRead = new short[1600];
        vBufferWord = new Vector<ObjSound>(1,1);        
    }
    /**
     * Este es un metodo sobre escrito en la clase Thread al implementar la interfaz Runnable
     * este metodo es el que corre como un proceso ligero o hilo en la clase MicInput
     * y es aqui dondo se instancia la clase HiloReconocedorDeRuido y este hilo
     * duerme hasta que el HiloReconocedorDeRuido lo despierta para que cree el arreglo
     * y poder comunicarlo a las clases que implementen la InterfaceMicInput he instancien esta clase
     */
    public void run() {
        if(AudioSystem.isLineSupported(info))
            try {
                SampleLine = (TargetDataLine)AudioSystem.getLine(info);
                SampleLine.open(format);
                SampleLine.start();
                micDaemon = new HiloReconocedorDeRuido(this.jSlider,this.jCheck);
                this.setPriority(Thread.MAX_PRIORITY);
                sampleOutputStream = new ByteArrayOutputStream();
                pcm.enEsperaDeSonido();
                esperarRuido();//AQUI ESPERA HASTA QUE HAYA SONIDO
                for(hayRuido = true; hayRuido;) {                    
                    numByteRead = SampleLine.read(byteRealBufferRead, 0, 3200);
                    sampleOutputStream.write(byteRealBufferRead, 0, numByteRead);
                }                
                pcm.procesandoSonido();
                SampleLine.stop();
                numByteRead = SampleLine.read(byteRealBufferRead, 0, 3200);
                sampleOutputStream.write(byteRealBufferRead, 0, numByteRead);
                SampleLine.stop();
                SampleLine.close();
                sampleOutputStream.close();
                tempByteData = sampleOutputStream.toByteArray();
                tempShortData = new short[tempByteData.length / 2];
                for(int l = 0; l < tempShortData.length; l++)
                    tempShortData[l] = (short)((tempByteData[2 * l + 1] << 8) + (tempByteData[2 * l] < 0 ? tempByteData[2 * l] + 256 : tempByteData[2 * l]));
                voicedSample = new ObjSound(tempShortData.length);
                voicedSample.addSound(tempShortData, tempShortData.length);
                vBufferWord.add(voicedSample);
                micDaemon.matarHilo();
                micDaemon=null;
                pcm.despertarHilo();
                
            } catch(Exception e) {
                //System.out.println("Error:"+e);
                
            }else{
                //System.out.println("No hay Soporte PCM");
            }
    }
    /**
     * Este metodo se utiliza para dormir en el acto al hilo que se ejecuta en
     * MicInput, ese hilo dormira hasta que la clase HiloReconocedorDeRuido
     * encuentre sonido que se pueda grabar.
     */
    public synchronized void esperarRuido(){
        try {
            wait();
        } catch(InterruptedException e){
        }
    }
    /**
     * La clase HiloReconocedorDeRuido se encarga de determinar que es ruido
     * de ambiente y que es sonido producido por el usuario final, esta clase
     * tiene tambien la funicion de despertar a el hilo del objeto MicInput
     * cuando encuentra sonido y anexo a sus funiciones el de informar cuando
     * la pronunciacin del sonido por parte del usuario finaliza entonces esta 
     * hilo muere.
     */
    private class HiloReconocedorDeRuido extends Thread {
        boolean color = false;
        double coheficiente=10;
        public HiloReconocedorDeRuido(JSlider feel,JToggleButton check) {
            hayRuido = false;
            this.jCheck=check;
            this.jSlider=feel;
            this.setPriority(Thread.MAX_PRIORITY);
            this.start();
        }
        public void run() {
            java.util.Vector<Object> temporal = new java.util.Vector<Object>();
            while(terminar) {
                lastAvg=avgEnergyThreshold;
                avgNoiseEnergy = 0.0D;
                if(!hayRuido) {
                    numByteRead = SampleLine.read(byteBufferRead, 0, 3200);
                    temporal.add(byteBufferRead);
                    temporal.add(numByteRead);
                } else {
                    try {
                        Thread.sleep(320L);
                    } catch(InterruptedException e) {}
                    this.yield();
                    byteBufferRead = byteRealBufferRead;
                    this.yield();
                }
                for(int i1 = 0; i1 < numByteRead / 2; i1++) {
                    
                    bufferRead[i1] = (short)((byteBufferRead[2 * i1 + 1] << 8) + (byteBufferRead[2 * i1] < 0 ? byteBufferRead[2 * i1] + 256 : byteBufferRead[2 * i1]));
                    avgNoiseEnergy += bufferRead[i1] * bufferRead[i1];
                    
                }
                avgNoiseEnergy /= 4800D;
                lastAvg = avgEnergyThreshold;
                avgEnergyThreshold = avgNoiseEnergy * 36D;
                lastMargen=margen;
                margen=lastAvg/avgEnergyThreshold;
                lastSensibilidad=sensibilidad;
                double x=1;
                /*
                if((sensibilidad*coheficiente)>=50&&(sensibilidad*coheficiente)<=90){
                    x=1;
                    //System.out.println("EstaBien "+coheficiente);
                }else{
                    if((sensibilidad*coheficiente)>90){
                        x=0.5;
                        //System.out.println("Corrige alto "+coheficiente);
                    }else{
                        if((sensibilidad*coheficiente)<10){
                            x=1.1;
                            //System.out.println("Corrige Bajo " +coheficiente);
                        }
                    }
                }*/
                coheficiente*=x;
                
                sensibilidad= (getSliderValue())/coheficiente;
                
                
                if(this.jCheck.isSelected()){
                    max++;
                    min--;
                    if(margen>max){
                        max=margen/2;
                    }
                    if(lastMargen<min){
                        min=lastMargen/2;
                    }
                    
                    
                    sensibilidad=((margen+lastMargen)/2) + (max+min)/2;
                    sensibilidad= (sensibilidad + lastSensibilidad)/2;
                    sensibilidad*=0.9;
                    
                    
                    //margen=margen*(sensibilidad/lastSensibilidad);
                    ////System.out.println("Margen"+margen);
                    jSlider.setValue((int)((sensibilidad)*coheficiente));
                    
                    
                    
                    
                    ////System.out.println("Sensibilidad Estimada de:"+sensibilidad +" Maximo:"+max+" Minimo:"+min);
                }else{
                    max=0;
                    min=100;
                    ////System.out.println("Sensibilidad Dada:"+sensibilidad);
                }
                ////System.out.println("Margen:"+margen+" -Sensibilidad:"+sensibilidad);
                if(margen>sensibilidad){//el que estaba 1070000000D//meyor Silencio 8563229792257//menor Hablando 6440695139257
                    ////System.out.println("Margen: "+margen+" Sensibilidad:" + sensibilidad+" feel:"+getSliderValue());
                    if(!hayRuido) {
                        hayRuido = true;
                        pcm.escuchandoSonido();
                        for(int i = 0; i < temporal.size(); i+=2)
                            sampleOutputStream.write((byte[])temporal.get(i), 0, ((Integer)temporal.get(i+1)).intValue());
                        despertarHilo();
                        this.setPriority(MIN_PRIORITY);
                    }
                    try {
                        Thread.sleep(300L);
                    } catch(InterruptedException e) {}
                    this.yield();
                } else {
                    if(hayRuido) {
                        MicInput.this.stopRecord();
                        matarHilo();
                    }
                    temporal.removeAllElements();
                    hayRuido=false;
                }
            }
        }
        public void matarHilo(){
            terminar = false;
        }
        
        
        public int getSliderValue(){
            return this.jSlider.getValue();
        }
        
        private boolean terminar = true;
        private javax.swing.JToggleButton jCheck=null;
        private javax.swing.JSlider jSlider=null;
        private double lastMargen=0;
        private double lastSensibilidad=0;
        private double margen=0;
        private double max=0;
        private double min=100;
        
    }
    /**
     * Este metodo despierta al hilo de la clase MicInput
     */
    public synchronized void despertarHilo(){
        MicInput.this.interrupt();
    }
    
    /**
     * Este metodo se encarga comunicar la existencia de ruido 
     */
    public void stopRecord() {
        hayRuido = false;
    }
    /**
     * La clase que desee trabajar con la clase MicInput debera dormir
     * mientras que este metodo retorne false
     * @return false si el arreglo que espera el objeto que usa a MicInput
     * an no se termina
     */
    public boolean byteArrayComplete() {
        return vBufferWord.size() > 0;
    }
    /**
     * El metodo newWord() crea una nueva palabra con el conjunto de arregos
     * que se adjuntaron al buffer este buffer almacena arreglos de tipo short[3200]
     * luego la nueba palabra se creara haciendo una variable de tipo short que
     * contenga todos los arreglos que se produjeron.
     */
    public void newWord() {
        tempSampleShortData = vBufferWord.firstElement();
        sampleShortData = new short[tempSampleShortData.getSoundSize()];
        for(int i = 0; i < tempSampleShortData.getSoundSize(); i++)
            sampleShortData[i] = tempSampleShortData.getSoundAt(i);
        
    }
    /**
     * Este metodo se utiliza para eliminar la ultima palabra reconocida.
     */
    public void removeOldWord() {
        if(vBufferWord.size() > 0) {
            vBufferWord.removeElementAt(0);
            vBufferWord.remove(0);
        }
    }
    
    public void setSlider(javax.swing.JSlider feel){
            this.jSlider=feel;
    }
    public void setCheck(javax.swing.JToggleButton check){
        this.jCheck=check;
    }
        
        
    /**
     * Este metodo retorna el arreglo combleto concerniete a la palabra dictada, 
     * Aclaracin: este metodo retornara el arreglo siempre que se halla echo ejecucion
     * del metodo newWord();
     */
    public short[] returnShortArray() {
        return sampleShortData;
    }
    
    
    /**
     * Esta varible se utiliza para determinar el rango que debe escucharce como
     * sonido pronunciado por el usuario.
     */
    public static double margenDeSilencio = 1070000000D;
    private TargetDataLine SampleLine;
    protected static final javax.sound.sampled.AudioFormat.Encoding SAMPLE_ENCODING;
    protected static final float SAMPLE_RATE = 16000F;
    protected static final int SAMPLE_BITS = 16;
    protected static final int SAMPLE_CHANNELS = 1;
    protected static final float SAMPLE_FRAME_RATE = 16000F;
    protected static final int SAMPLE_FRAME_SIZE = 2;
    protected static final boolean SAMPLE_BIG_ENDIAN = false;
    protected static final int SAMPLE_BUFFER_SIZE = 32000;
    protected static final AudioFormat format;    
    private short sampleShortData[];
    /**
     * Este atributo permite contener la informacin tecnica sobre la targeta 
     * de sonido.
     */
    private javax.sound.sampled.DataLine.Info info;
    private final int BUFFER_SIZE = 3200;
    private final double AVG_ENERGY_CONST = 36D;
    private double avgEnergyThreshold=100000000;
    private double avgNoiseEnergy;
    private byte byteBufferRead[];
    private byte byteRealBufferRead[];
    private boolean hayRuido=false;
    private int numByteRead;
    private int numRealByteRead;
    private short bufferRead[];
    private ObjSound voicedSample;
    private ObjSound tempSampleShortData;
    protected Vector<ObjSound> vBufferWord;
    private ByteArrayOutputStream sampleOutputStream;
    private byte tempByteData[];
    private short tempShortData[];
    private double lastAvg=0;
    private javax.swing.JSlider jSlider=null;
    private javax.swing.JToggleButton jCheck=null;
    private double sensibilidad=100;
    private HiloReconocedorDeRuido micDaemon;
    
    static {
        SAMPLE_ENCODING = javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
        format = new AudioFormat(SAMPLE_ENCODING, 16000F, 16, 1, 2, 16000F, false);
    }
}

