 /**
 * Interfaz: markov.reconocedor.ComunicarArreglo
 * Proyecto: Herramienta Software capaz de interpretar comandos hablados
 *           para el Sistema Opertivo Linux dirigida a personas discapacitadas
 */
   
package com.arianthox.predictor.hmm.audio;

/**
 * Esta interfaz se dise�a con el fin de establecer comunicacion con la 
 * <B>clase markov.reconocedor.MicInput</B>, recordemos que la clase MicInput
 * es la encargada de trabajar directamente con la targeta de sonido <i>(para mas
 * informaci�n sobre como trabajar con la clase MicInput remitace al siguiente link
 * <A HREF="../../markov/reconocedor/dispocitivo/MicInput.html" title="clase in markov.reconocedor">MicInput</A>)</i>, cualquier
 * clase que desee trabajar con la clase MicInput, a parte de implementar esta interfaz
 * y comunicarce con la clase MicInput por medio de su constructor 
 * MicInput(InterfaceMicInput pcm) ("donde pcm es la clase que implementa esta 
 * interfaz") debe saber que mientras el metodo publico y boolean byteArrayComplete() 
 * de la clase markov.reconocedor.MicInput retorne false esa clase debe dormir hasta
 * que la clase MicInput la despierte por medio del metotdo despertarHilo() sobrescrito
 * por la clase que implemente esta interfaz.
 *<BR>
 * Esta interfaz tiene adem�s del metodo despertarHilo() tres metodos mas que sirven
 * para apreciar el comportamiento de la clase MicInput.
 *
 *<br><br>
 *<B>Proyecto:</B> Herramienta Software capaz de interpretar comandos hablados
 *           para el Sistema Opertivo Linux dirigida a personas discapacitadas
 * @author Amalia Johanna Molina Ovallos
 * @author Antony Hern�n Delgado Solano
 * @version 1.0 Noviembre 2 del 2005
 */

public interface InterfaceMicInput {
    /**
     *  El metodo despertarHilo() como su nombre lo dice, se utiliza para
     *  despertar el hilo de la clase que implemento esta interfaz y 
     *  que esta generando instancias de la clase MicInput a fin de obtener
     *  los arreglos de tipo short por medio del metodo publico returnShortArray()
     *  existente en la clase MicInput.
     *
     */
    public abstract void despertarHilo();
    /**
     *  El metodo escuchandoSonido() se sobreescribe para que el programador
     *  pueda mostrar o imprimir algo cuando la clase MicInput esta detectando
     *  sonido.
     */
    public abstract void escuchandoSonido();
    /**
     *  El medoto enEsperaDeSonido() se sobreescribe para que el programador
     *  pueda mostrar o imprimir algo cuando la clase MicInput esta esperando
     *  que el usuario final pronucie alguna palabra
     */
    public abstract void enEsperaDeSonido();
    /**
     *  El metodo procesandoSonido() se sobreescribe para que el programador
     *  pueda mostrar o imprimir algo cuando la clase MicInput ya dejo de 
     *  detecta sonido y ahora lo esta convirtiendo a un arreglo short
     */
    public abstract void procesandoSonido();
}
