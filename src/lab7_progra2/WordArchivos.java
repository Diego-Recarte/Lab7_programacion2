/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_progra2;

/**
 *
 * @author denam
 */

import java.io.*;
import javax.swing.*;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.text.*;
import java.io.File;
import java.util.Arrays;
import java.io.RandomAccessFile;

public class WordArchivos {
    private static final Object LOCK_ARCHIVOS = new Object();

    // --- INICIO CÓDIGO NUEVO PARA TABLAS ---
    // Estructura binaria del archivo (orden fijo):
    //   UTF   nombre del documento
    //   int   cantidad de elementos del cuerpo (texto + tablas, en orden de aparición)
    //   por cada elemento:
    //     int   tipo (0 = fragmento de texto, 1 = tabla)
    //     si tipo == 0 (fragmento de texto):
    //       UTF texto
    //       UTF fuente
    //       int tamaño
    //       int color (RGB)
    //     si tipo == 1 (tabla):
    //       int filas
    //       int columnas
    //       por cada celda (recorrido fila por fila, columna por columna):
    //         UTF     texto de la celda
    //         UTF     fuente
    //         int     tamaño
    //         int     color (RGB)
    //         boolean negrita
    //         boolean cursiva
    //         boolean subrayado
    //         boolean tachado
    private static final int TIPO_TEXTO = 0;
    private static final int TIPO_TABLA = 1;
    // --- FIN CÓDIGO NUEVO PARA TABLAS ---

    public static boolean guardarComo(JTextPane editor, File archivo, String nombre, boolean isGuardarComo) throws IOException{
        
        synchronized( LOCK_ARCHIVOS ){
            ArrayList<Object> elementos = extraerElementos(editor);

            if ((isGuardarComo && !archivo.exists()) ||(!isGuardarComo && archivo.exists()) ){



               try (RandomAccessFile raf =new RandomAccessFile(archivo, "rw")) {

                   raf.setLength(0);
                   raf.writeUTF(nombre);
                   escribirElementos(raf, elementos);

                   return true;
               }
            }else{
                return false;
            }
        }
     
        
        
    }
    private static ArrayList<wordFragmento> extraerFragmentos(JTextPane editor) {
        ArrayList<wordFragmento> lista = new ArrayList<>();

        try {
            StyledDocument doc = editor.getStyledDocument();
            int longitud = doc.getLength();
            int i = 0;

            while (i < longitud) {
                Element elemento = doc.getCharacterElement(i);
                int inicio = elemento.getStartOffset();
                int fin = Math.min(elemento.getEndOffset(), longitud);

                AttributeSet attr = elemento.getAttributes();

                String texto = doc.getText(inicio, fin - inicio);
                String fuente = StyleConstants.getFontFamily(attr);
                int tamano = StyleConstants.getFontSize(attr);
                Color color = StyleConstants.getForeground(attr);
              

                lista.add(new wordFragmento(texto, fuente, tamano, color  ));

                i = fin;
            }
        } catch (Exception e) {
         
        }

        return lista;
    }

    // --- INICIO CÓDIGO NUEVO PARA TABLAS ---
    /**
     * Recorre el documento igual que extraerFragmentos, pero además
     * reconoce las tablas (TablaEditor) incrustadas como componentes y
     * las agrega a la lista en el mismo orden en que aparecen en el texto.
     * Cada elemento de la lista es un wordFragmento (texto) o un TablaEditor (tabla).
     */
    private static ArrayList<Object> extraerElementos(JTextPane editor) {
        ArrayList<Object> lista = new ArrayList<>();

        try {
            StyledDocument doc = editor.getStyledDocument();
            int longitud = doc.getLength();
            int i = 0;

            while (i < longitud) {
                Element elemento = doc.getCharacterElement(i);
                int inicio = elemento.getStartOffset();
                int fin = Math.min(elemento.getEndOffset(), longitud);

                AttributeSet attr = elemento.getAttributes();
                Component componente = StyleConstants.getComponent(attr);

                if (componente instanceof TablaEditor) {
                    lista.add(componente);
                } else {
                    String texto = doc.getText(inicio, fin - inicio);
                    String fuente = StyleConstants.getFontFamily(attr);
                    int tamano = StyleConstants.getFontSize(attr);
                    Color color = StyleConstants.getForeground(attr);

                    lista.add(new wordFragmento(texto, fuente, tamano, color));
                }

                i = fin;
            }
        } catch (Exception e) {

        }

        return lista;
    }

    private static void escribirElementos(RandomAccessFile raf, ArrayList<Object> elementos) throws IOException {
        raf.writeInt(elementos.size());

        for (Object elemento : elementos) {
            if (elemento instanceof TablaEditor) {
                TablaEditor tabla = (TablaEditor) elemento;
                TablaCelda[][] celdas = tabla.getCeldas();
                int filas = tabla.getFilas();
                int columnas = tabla.getColumnas();

                raf.writeInt(TIPO_TABLA);
                raf.writeInt(filas);
                raf.writeInt(columnas);

                for (int f = 0; f < filas; f++) {
                    for (int c = 0; c < columnas; c++) {
                        TablaCelda celda = celdas[f][c];
                        raf.writeUTF(celda.getTexto());
                        raf.writeUTF(celda.getFuente());
                        raf.writeInt(celda.getTamano());
                        raf.writeInt(celda.getColor().getRGB());
                        raf.writeBoolean(celda.isNegrita());
                        raf.writeBoolean(celda.isCursiva());
                        raf.writeBoolean(celda.isSubrayado());
                        raf.writeBoolean(celda.isTachado());
                    }
                }
            } else {
                wordFragmento f = (wordFragmento) elemento;
                raf.writeInt(TIPO_TEXTO);
                raf.writeUTF(f.getTexto());
                raf.writeUTF(f.getFuente());
                raf.writeInt(f.getTamano());
                raf.writeInt(f.getColor().getRGB());
            }
        }
    }
    // --- FIN CÓDIGO NUEVO PARA TABLAS ---

    public static boolean guardar(JTextPane editor, File archivo, String nombre, boolean isGuardarComo) throws IOException{
         ArrayList<Object> elementos = extraerElementos(editor);
         
         if (archivo.exists()){
             

            try (RandomAccessFile raf =new RandomAccessFile(archivo, "rw")) {

                raf.setLength(0);
                raf.writeUTF(nombre);
                escribirElementos(raf, elementos);
                
                return true;
            }
         }else{
             return false;
         }
    }
    
  
    
        public static void abrir(JLabel titulo, JTextPane editor, File archivo)throws IOException, BadLocationException {

            if (archivo == null || !archivo.exists() || !archivo.isFile()) {
                throw new FileNotFoundException("Archivo no válido.");
            }

            try (RandomAccessFile raf = new RandomAccessFile(archivo, "r")) {

                StyledDocument doc = editor.getStyledDocument();
                doc.remove(0, doc.getLength());

                raf.seek(0);

                String nombre = raf.readUTF();
                titulo.setText(nombre);

                int cantidadElementos = raf.readInt();

                for (int i = 0; i < cantidadElementos; i++) {
                    int tipo = raf.readInt();

                    if (tipo == TIPO_TABLA) {
                        // --- INICIO CÓDIGO NUEVO PARA TABLAS ---
                        int filas = raf.readInt();
                        int columnas = raf.readInt();
                        TablaCelda[][] celdas = new TablaCelda[filas][columnas];

                        for (int f = 0; f < filas; f++) {
                            for (int c = 0; c < columnas; c++) {
                                String texto = raf.readUTF();
                                String fuente = raf.readUTF();
                                int tamano = raf.readInt();
                                Color color = new Color(raf.readInt(), true);
                                boolean negrita = raf.readBoolean();
                                boolean cursiva = raf.readBoolean();
                                boolean subrayado = raf.readBoolean();
                                boolean tachado = raf.readBoolean();

                                celdas[f][c] = new TablaCelda(texto, fuente, tamano, color,
                                        negrita, cursiva, subrayado, tachado);
                            }
                        }

                        TablaEditor tabla = new TablaEditor(filas, columnas, celdas);
                        editor.setCaretPosition(doc.getLength());
                        editor.insertComponent(tabla);
                        doc.insertString(doc.getLength(), "\n", null);
                        // --- FIN CÓDIGO NUEVO PARA TABLAS ---
                    } else {
                        String texto = raf.readUTF();
                        String fuente = raf.readUTF();
                        int tamano = raf.readInt();
                        Color color = new Color(raf.readInt(), true);

                        SimpleAttributeSet atributos = new SimpleAttributeSet();
                        StyleConstants.setFontFamily(atributos, fuente);
                        StyleConstants.setFontSize(atributos, tamano);
                        StyleConstants.setForeground(atributos, color);

                        doc.insertString(doc.getLength(), texto, atributos);
                    }
                }
            }
        }
}