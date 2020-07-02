package lucene;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Buscador {
    // Variables
    IndexReader lector;
    IndexSearcher buscador;
    Analizador analizadores;
    Alert msgError, msgAlerta;
    ArrayList<ArrayList<DocumentoEncontrado>> documentosEncontrados;
    Label lblDocumentosEncontrados, lblTiempoConsulta;
    int cantidadPaginas;
    int paginaActual;

    Buscador (Analizador _analizadores, Label _lblDocumentosEncontrados, Label _lblTiempoConsulta) {
        msgError = new Alert(Alert.AlertType.ERROR);
        msgError.setTitle("ERROR");
        msgAlerta = new Alert(Alert.AlertType.WARNING);
        msgAlerta.setTitle("ALERTA");
        analizadores = _analizadores;
        lblDocumentosEncontrados = _lblDocumentosEncontrados;
        lblTiempoConsulta = _lblTiempoConsulta;
    }

    // Metodos
    public Query prepararConsulta(String campoSeleccionado, String textoConsulta) {
        Query consulta;
        Analyzer analizadorSeleccionado;

        if (campoSeleccionado.equals("titulo")) {
            campoSeleccionado = campoSeleccionado + "Buscar";
            analizadorSeleccionado = analizadores.analizadorRemoverStopWords;
        }
        else if (campoSeleccionado.equals("ref"))
            analizadorSeleccionado = analizadores.analizadorRemoverStopWords;
        else
            analizadorSeleccionado = analizadores.analizadorConStemming;

        QueryParser parser = new QueryParser(campoSeleccionado, analizadorSeleccionado);
        String consultaSinTildes = analizadores.limpiarAcentos(textoConsulta, true);
        try {
            consulta = parser.parse(consultaSinTildes);
            return consulta;
        }
        catch (ParseException e){
            msgAlerta.setTitle("ERROR");
            msgAlerta.setHeaderText("Lo sentimos, hubo un problema al procesar la consulta");
            msgAlerta.setContentText("Inténtelo de nuevo.");
            msgAlerta.show();
        }
        return null;
    }

    public void ejecutarConsulta(Query consulta, int cantidadPorPagina){

        long inicio = System.currentTimeMillis();
        int numeroResultados = 0;

        try {
            TopDocs resultados = buscador.search(consulta, cantidadPorPagina);
            ScoreDoc[] docResultados = resultados.scoreDocs;

            numeroResultados = Math.toIntExact(resultados.totalHits.value);
            cantidadPaginas = (int) Math.ceil((float) numeroResultados / (float) cantidadPorPagina);

            int posicion = 0;
            float puntaje = 0;

            for (int i = 0; i < cantidadPaginas; i++){
                ArrayList<DocumentoEncontrado> lista = new ArrayList<>();
                for (ScoreDoc scoreDoc : docResultados) {
                    puntaje = scoreDoc.score;
                    Document doc = buscador.doc(scoreDoc.doc);
                    DocumentoEncontrado documento = new DocumentoEncontrado();
                    if (doc != null) {
                        posicion++;
                        documento.llenarDatos(posicion, puntaje, doc);
                    }
                    lista.add(documento);
                }
                documentosEncontrados.add(lista);
                docResultados = buscador.searchAfter(docResultados[docResultados.length-1],consulta,cantidadPorPagina).scoreDocs;
            }
            System.out.println("Tamano de la lista de listas: " + documentosEncontrados.size());
            long fin = System.currentTimeMillis();
            float tiempo = (float) ((fin - inicio))/1000;
            lblDocumentosEncontrados.setText(numeroResultados + " documentos encontrados.");
            lblTiempoConsulta.setText("Duración: " + tiempo + " segundos");
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("Hubo un error al ejecutar la consulta");
            msgError.show();
        }
    }

    public ArrayList<ArrayList<DocumentoEncontrado>> buscarDocumento (String directorioIndice, String campoSeleccionado,
                                                           String textoConsulta, int cantidadPorPagina) {

        documentosEncontrados = new ArrayList<ArrayList<DocumentoEncontrado>>();

        try {
            Path directorioIndices = Paths.get(".", directorioIndice);
            lector = DirectoryReader.open(FSDirectory.open(directorioIndices));
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("Huno un error al abrir el directorio de índices");
            msgError.setContentText("Por favor, verifique que la dirección suministrada es correcta");
        }

        buscador = new IndexSearcher(lector);

        Query consulta = prepararConsulta(campoSeleccionado,textoConsulta);

        if (consulta != null){
            ejecutarConsulta(consulta,cantidadPorPagina);
        }

        return documentosEncontrados;
    }
}
