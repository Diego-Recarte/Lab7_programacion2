/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lab7_progra2;

/**
 *
 * @author denam
 */



import java.awt.Color;
import java.awt.Component;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class WordArchivos {

    private static final Object LOCK_ARCHIVOS = new Object();

    private static final String FIRMA = "WRD1";
    private static final int VERSION = 2;

    private static final int TIPO_TEXTO = 0;
    private static final int TIPO_TABLA = 1;

    public static boolean guardarComo(JTextPane editor, File archivo,String nombre, boolean isGuardarComo) throws IOException {

        synchronized (LOCK_ARCHIVOS) {
            if ((isGuardarComo && archivo.exists()) || (!isGuardarComo && !archivo.exists())) {
                return false;
            }

            ArrayList<Object> elementos = extraerElementos(editor);

            try (RandomAccessFile raf
                    = new RandomAccessFile(archivo, "rw")) {

                raf.setLength(0);
                raf.writeUTF(FIRMA);
                raf.writeInt(VERSION);
                raf.writeUTF(nombre);

                escribirElementos(raf, elementos);

                return true;
            }
        }
    }

    public static boolean guardar(
            JTextPane editor,
            File archivo,
            String nombre,
            boolean isGuardarComo) throws IOException {

        synchronized (LOCK_ARCHIVOS) {
            if (!archivo.exists()) {
                return false;
            }

            ArrayList<Object> elementos = extraerElementos(editor);

            try (RandomAccessFile raf
                    = new RandomAccessFile(archivo, "rw")) {

                raf.setLength(0);
                raf.writeUTF(FIRMA);
                raf.writeInt(VERSION);
                raf.writeUTF(nombre);

                escribirElementos(raf, elementos);

                return true;
            }
        }
    }

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

                AttributeSet atributos = elemento.getAttributes();
                Component componente =
                        StyleConstants.getComponent(atributos);

                if (componente instanceof TablaEditor) {
                    lista.add(componente);
                } else {
                    String texto =doc.getText(inicio, fin - inicio);

                    String fuente = StyleConstants.getFontFamily(atributos);

                    int tamano =StyleConstants.getFontSize(atributos);

                    Color color =StyleConstants.getForeground(atributos);

                    boolean negrita = StyleConstants.isBold(atributos);

                    boolean cursiva = StyleConstants.isItalic(atributos);

                    boolean subrayado =StyleConstants.isUnderline(atributos);

                    lista.add(new wordFragmento(texto, fuente,tamano, color, negrita,cursiva,subrayado));
                }

                i = fin;
            }
        } catch (BadLocationException e) {
            throw new IllegalStateException(
                    "No se pudo leer el contenido del editor.", e);
        }

        return lista;
    }

    private static void escribirElementos(
            RandomAccessFile raf,
            ArrayList<Object> elementos) throws IOException {

        raf.writeInt(elementos.size());

        for (Object elemento : elementos) {
            if (elemento instanceof TablaEditor) {
                escribirTabla(raf, (TablaEditor) elemento);
            } else if (elemento instanceof wordFragmento) {
                escribirTexto(raf, (wordFragmento) elemento);
            } else {
                throw new IOException("Elemento desconocido dentro del documento.");
            }
        }
    }

    private static void escribirTexto(
            RandomAccessFile raf,
            wordFragmento fragmento) throws IOException {

        raf.writeInt(TIPO_TEXTO);
        raf.writeUTF(fragmento.getTexto());
        raf.writeUTF(fragmento.getFuente());
        raf.writeInt(fragmento.getTamano());
        raf.writeInt(fragmento.getColor().getRGB());
        raf.writeBoolean(fragmento.isNegrita());
        raf.writeBoolean(fragmento.isCursiva());
        raf.writeBoolean(fragmento.isSubrayado());
    }

    private static void escribirTabla(
            RandomAccessFile raf,
            TablaEditor tabla) throws IOException {

        TablaCelda[][] celdas = tabla.getCeldas();
        int filas = tabla.getFilas();
        int columnas = tabla.getColumnas();

        if (filas <= 0 || columnas <= 0) {
            throw new IOException("La tabla tiene dimensiones inválidas.");
        }

        if (celdas == null
                || celdas.length != filas) {
            throw new IOException("Las filas de la tabla son inválidas.");
        }

        raf.writeInt(TIPO_TABLA);
        raf.writeInt(filas);
        raf.writeInt(columnas);

        for (int fila = 0; fila < filas; fila++) {
            if (celdas[fila] == null
                    || celdas[fila].length != columnas) {
                throw new IOException("Las columnas de la tabla son inválidas.");
            }

            for (int columna = 0;
                    columna < columnas;
                    columna++) {

                TablaCelda celda = celdas[fila][columna];

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
    }

    public static void abrir(
            JLabel titulo,
            JTextPane editor,
            File archivo)
            throws IOException, BadLocationException {

        if (archivo == null
                || !archivo.exists()
                || !archivo.isFile()) {
            throw new FileNotFoundException(
                    "El archivo no existe o no es válido.");
        }

        String nombreArchivo =
                archivo.getName().toLowerCase();

        if (!nombreArchivo.endsWith(".wrd")) {
            throw new IOException(
                    "La extensión debe ser .wrd.");
        }

        StyledDocument temporal = new DefaultStyledDocument();
        String nombre;

        try (RandomAccessFile raf
                = new RandomAccessFile(archivo, "r")) {

            String firmaLeida = raf.readUTF();
            int versionLeida = raf.readInt();

            if (!FIRMA.equals(firmaLeida)) {
                throw new IOException(
                        "El archivo no pertenece al formato WRD.");
            }

            if (versionLeida != VERSION) {
                throw new IOException(
                        "Versión de archivo no compatible.");
            }

            nombre = raf.readUTF();

            if (nombre.trim().isEmpty()) {
                throw new IOException(
                        "El nombre del documento está vacío.");
            }

            int cantidadElementos = raf.readInt();

            if (cantidadElementos < 0
                    || cantidadElementos > 100000) {
                throw new IOException(
                        "Cantidad de elementos inválida.");
            }

            for (int i = 0;
                    i < cantidadElementos;
                    i++) {

                leerElemento(raf, temporal);
            }

        } catch (EOFException e) {
            throw new IOException(
                    "El archivo está truncado o corrupto.", e);
        }

        StyledDocument destino = editor.getStyledDocument();
        destino.remove(0, destino.getLength());

        copiarDocumento(temporal, destino);

        titulo.setText(nombre);
    }

    private static void leerElemento(
            RandomAccessFile raf,
            StyledDocument documento)
            throws IOException, BadLocationException {

        int tipo = raf.readInt();

        if (tipo == TIPO_TEXTO) {
            leerTexto(raf, documento);
        } else if (tipo == TIPO_TABLA) {
            leerTabla(raf, documento);
        } else {
            throw new IOException(
                    "Tipo de elemento desconocido: " + tipo);
        }
    }

    private static void leerTexto(RandomAccessFile raf,StyledDocument documento)throws IOException, BadLocationException {

        String texto = raf.readUTF();
        String fuente = raf.readUTF();
        int tamano = raf.readInt();
        Color color = new Color(raf.readInt(), true);

        boolean negrita = raf.readBoolean();
        boolean cursiva = raf.readBoolean();
        boolean subrayado = raf.readBoolean();

        if (tamano <= 0) {throw new IOException("Tamaño de texto inválido.");
        }

        SimpleAttributeSet atributos =crearAtributos(fuente, tamano, color, negrita,cursiva,subrayado,false);

        documento.insertString(documento.getLength(),texto,atributos );
    }

    private static void leerTabla(RandomAccessFile raf,StyledDocument documento)throws IOException {

        int filas = raf.readInt();
        int columnas = raf.readInt();

        if (filas <= 0 || filas > 1000 || columnas <= 0 || columnas > 1000) {
            throw new IOException("Dimensiones de tabla inválidas.");
        }

        TablaCelda[][] celdas =new TablaCelda[filas][columnas];

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0;
                    columna < columnas;
                    columna++) {

                String texto = raf.readUTF();
                String fuente = raf.readUTF();
                int tamano = raf.readInt();
                Color color = new Color(raf.readInt(), true);

                boolean negrita = raf.readBoolean();
                boolean cursiva = raf.readBoolean();
                boolean subrayado = raf.readBoolean();
                boolean tachado = raf.readBoolean();

                if (tamano <= 0) {
                    throw new IOException( "Tamaño de celda inválido.");
                }

                celdas[fila][columna] =new TablaCelda(   texto,fuente,tamano,color,negrita, cursiva, subrayado, tachado);
            }
        }

        TablaEditor tabla =new TablaEditor(filas, columnas, celdas);

        try{
        documento.insertString(documento.getLength(), "\n",  null );
        }catch (BadLocationException ev){
                
                }

        SimpleAttributeSet atributos =new SimpleAttributeSet();

        StyleConstants.setComponent(atributos, tabla);
        try{
        documento.insertString(documento.getLength(), " ",atributos );
        }catch (BadLocationException ev){
            
        }
        try{
        documento.insertString(documento.getLength(),"\n", null);
        }catch(BadLocationException ev){
            
            
        }
    }

    private static SimpleAttributeSet crearAtributos(String fuente, int tamano, Color color, boolean negrita,boolean cursiva,boolean subrayado,boolean tachado) {

        SimpleAttributeSet atributos = new SimpleAttributeSet();

        StyleConstants.setFontFamily( atributos, fuente);

        StyleConstants.setFontSize( atributos, tamano);

        StyleConstants.setForeground(atributos, color);

        StyleConstants.setBold(atributos, negrita);

        StyleConstants.setItalic(atributos, cursiva);

        StyleConstants.setUnderline(atributos, subrayado);

        StyleConstants.setStrikeThrough( atributos, tachado);

        return atributos;
    }

    private static void copiarDocumento(StyledDocument origen,StyledDocument destino)throws BadLocationException {

        for (int i = 0; i < origen.getLength();) {

            Element elemento =origen.getCharacterElement(i);

            int inicio = elemento.getStartOffset();
            int fin = Math.min(elemento.getEndOffset(),origen.getLength());

            String texto = origen.getText( inicio,fin - inicio);

            AttributeSet atributos =elemento.getAttributes();

            destino.insertString( destino.getLength(), texto, atributos);

            i = fin;
        }
    }
}