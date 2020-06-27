package lucene;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.EventHandler;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DocumentoEncontrado {

    public Button btnPagina;
    public String tituloMostrar;
    public String tituloBuscar;
    public String encabezados;
    public String referencias;
    public String texto;
    public String archivo;
    public Integer posicion;
    public Float puntaje;
    public int posicionInicialDocumento;
    public int largoDocumento;

    DocumentoEncontrado() {
        tituloBuscar = tituloMostrar = encabezados = referencias = texto = archivo = null;
        posicion = null;
        btnPagina = new Button("Mostrar");
        btnPagina.addEventHandler(
                MouseEvent.MOUSE_PRESSED,
                (e) -> abrirPagina()
        );
        puntaje = null;
        posicionInicialDocumento = largoDocumento = 0;
    }


    public void abrirPagina() {
        StringBuilder paginaWeb = new StringBuilder();
        String restante;

        // Lee el documento HTML de la coleccion a la que pertenece
        try (BufferedReader buffer = new BufferedReader(new FileReader(archivo))) {
            String linea;
            for (int i = 0; i < posicionInicialDocumento; i++) {
                buffer.readLine();
            }
            for (int i = 0; i < largoDocumento; i++){
                linea = buffer.readLine();
                paginaWeb.append(linea);
            }

            System.out.println("PAGINA EXTRAIDA: " + paginaWeb.toString());
            // Crea el archivo del nuevo HTML
            Path ficheroHTML = Paths.get(".","/htmls/"+tituloMostrar+".html");
            FileWriter fileWriter = new FileWriter(ficheroHTML.toString());
            BufferedWriter escritor = new BufferedWriter(fileWriter);
            escritor.write(paginaWeb.toString());
            escritor.close();

            // Abre el html en el navegador
            String ubicacion = ficheroHTML.toAbsolutePath().toString();
            ubicacion = ubicacion.replace(".\\", "\\");
            File htmlFile = new File(ubicacion);

            try {
                Desktop.getDesktop().browse(htmlFile.toURI());
            }
            catch (IOException e){
                System.out.println("Hubo un problema al cargar la pagina web");
            }
        }
        catch (IOException  e) {
            System.out.println("Hubo un error al extraer el html de la coleccion");
        }
    }
    public String getTituloMostrar() {
        return tituloMostrar;
    }

    public void setTituloMostrar(String tituloMostrar) {
        this.tituloMostrar = tituloMostrar;
    }

    public String getTituloBuscar() {
        return tituloBuscar;
    }

    public void setTituloBuscar(String tituloBuscar) {
        this.tituloBuscar = tituloBuscar;
    }

    public String getEncabezados() {
        return encabezados;
    }

    public void setEncabezados(String encabezados) {
        this.encabezados = encabezados;
    }

    public String getReferencias() {
        return referencias;
    }

    public void setReferencias(String referencias) {
        this.referencias = referencias;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Button getBtnPagina() {
        return btnPagina;
    }

    public void setBtnPagina(Button btnPagina) {
        this.btnPagina = btnPagina;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Float getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(Float puntaje) {
        this.puntaje = puntaje;
    }

    public int getPosicionInicialDocumento() {
        return posicionInicialDocumento;
    }

    public void setPosicionInicialDocumento(int posicionInicialDocumento) {
        this.posicionInicialDocumento = posicionInicialDocumento;
    }

    public int getLargoDocumento() {
        return largoDocumento;
    }

    public void setLargoDocumento(int largoDocumento) {
        this.largoDocumento = largoDocumento;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }
}
