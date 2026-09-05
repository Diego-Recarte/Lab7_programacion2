package lab7_progra2;


public class WordException extends Exception {

    public WordException(String mensaje) {
        super(mensaje);
    }

    public WordException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    
    public static class ArchivoNoEncontradoException extends WordException {
        public ArchivoNoEncontradoException(String mensaje) {
            super(mensaje);
        }
    }


    public static class ArchivoInvalidoException extends WordException {
        public ArchivoInvalidoException(String mensaje) {
            super(mensaje);
        }
    }

  
    public static class ExtensionInvalidaException extends WordException {
        public ExtensionInvalidaException(String mensaje) {
            super(mensaje);
        }
    }

    
    public static class FormatoDesconocidoException extends WordException {
        public FormatoDesconocidoException(String mensaje) {
            super(mensaje);
        }
    }

   
    public static class VersionNoCompatibleException extends WordException {
        public VersionNoCompatibleException(String mensaje) {
            super(mensaje);
        }
    }

  
    public static class ArchivoCorruptoException extends WordException {
        public ArchivoCorruptoException(String mensaje) {
            super(mensaje);
        }

        public ArchivoCorruptoException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    public static class ArchivoYaExisteException extends WordException {
        public ArchivoYaExisteException(String mensaje) {
            super(mensaje);
        }
    }

    
    public static class ArchivoNoExisteParaGuardarException extends WordException {
        public ArchivoNoExisteParaGuardarException(String mensaje) {
            super(mensaje);
        }
    }

    public static class DatosInvalidosException extends WordException {
        public DatosInvalidosException(String mensaje) {
            super(mensaje);
        }
    }

   
    public static class ErrorEscrituraException extends WordException {
        public ErrorEscrituraException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }

    
    public static class ErrorEditorException extends WordException {
        public ErrorEditorException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}