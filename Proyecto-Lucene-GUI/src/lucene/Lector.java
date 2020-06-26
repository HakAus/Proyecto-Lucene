package lucene;

import javafx.scene.control.Alert;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Lector {

    public ArrayList<String> htmlsDeCadaArchivo;

    public ArrayList<String> obtenerDocumentos(String nombreDocumento) throws IOException {
        htmlsDeCadaArchivo = new ArrayList<String>();
        ArrayList<String> documentosHtml = new ArrayList<String>();
        String separador = File.separator;
        Path ruta = Paths.get(".", "/input/"+nombreDocumento);
        File archivo = new File(ruta.toString());
        if (archivo.exists())
            htmlsDeCadaArchivo = extraerHTML(archivo);
        else {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("ERROR");
            alerta.setHeaderText("No se encontro el documento");
            alerta.setContentText("El archivo indicado no existe en la el directorio /input");
            alerta.show();
        }
        return htmlsDeCadaArchivo;
    }

    public ArrayList<String> extraerHTML (File archivo) throws IOException {
        ArrayList<String> documentosHtml = new ArrayList<String>();
        StringBuilder nuevoDocumento = new StringBuilder();
        int contador = 0;
        boolean documentoIniciado = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.equals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">")) {
                    documentoIniciado = true;
                    nuevoDocumento.setLength(0);    // Lectura nueva
                    contador++;
                } else if (linea.equals("</html>")) {
                    documentoIniciado = false;
                    documentosHtml.add(nuevoDocumento.toString());
                } else if (documentoIniciado) {
                    nuevoDocumento.append(linea);
                }
            }
        }
        return documentosHtml;
    }

}
