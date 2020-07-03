package lucene;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Buscador {
    // Variables
    IndexReader lector;
    IndexSearcher buscador;
    Analizador analizadores;
    Alert msgError, msgAlerta;
    ArrayList<ArrayList<DocumentoEncontrado>> documentosEncontrados;
    Label lblDocumentosEncontrados, lblTiempoConsulta, lblDocumentosColeccion;
    int cantidadPaginas;
    Pattern patronConsulta;

    Buscador (Analizador _analizadores, Label _lblDocumentosEncontrados, Label _lblTiempoConsulta, Label _lblDocumentosColeccion) {
        msgError = new Alert(Alert.AlertType.ERROR);
        msgError.setTitle("ERROR");
        msgAlerta = new Alert(Alert.AlertType.WARNING);
        msgAlerta.setTitle("ALERTA");
        analizadores = _analizadores;
        lblDocumentosEncontrados = _lblDocumentosEncontrados;
        lblTiempoConsulta = _lblTiempoConsulta;
        lblDocumentosColeccion = _lblDocumentosColeccion;
        patronConsulta = Pattern.compile("(?<titulo>titulo:)(?<vtitulo>[^ ]*)|(?<ref>ref:)(?<vref>[^ ]*)|(?<texto>texto:)(?<vtexto>[^ ]*)|(?<encab>encab:)(?<vencab>[^ ]*)");
    }

    // Metodos
    private static String tokensToString(TokenStream stream) throws IOException
    {
        StringBuilder tokens = new StringBuilder();
        CharTermAttribute caracter = stream.addAttribute(CharTermAttribute.class);
        stream.reset();
        while (stream.incrementToken()) {
            tokens.append(" ").append(caracter.toString()).append(" ");
        }
        stream.end();
        stream.close();
        return tokens.toString();
    }

    public String quitarStopWords(String campo, String texto){
        try {
            TokenStream streamTextoSinStopWords =  analizadores.analizadorRemoverStopWords.tokenStream(campo,texto);
            return tokensToString(streamTextoSinStopWords);
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("Hubo una error al remover los stop words de " + campo);
            msgError.show();
        }
        return null;
    }

    public String sacarRaices(String campo, String texto) {
        TokenStream streamTextoConRaices = analizadores.analizadorConStemming.tokenStream(campo,texto);
        try {
            return tokensToString(streamTextoConRaices);
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("No se pudo hacer el stemming");
            msgError.show();
        }
        return null;
    }

    public Query prepararConsulta(String campoSeleccionado, String textoConsulta, boolean personalidada) {
        Query consulta;
        Analyzer analizadorSeleccionado = null;

        if (personalidada){
            Matcher comparacion = patronConsulta.matcher(textoConsulta);
            StringBuilder partesConsulta = new StringBuilder();
            String cadenaFiltrada;
            if (comparacion.group("titulo") != null) {
                cadenaFiltrada = quitarStopWords("titulo",comparacion.group("vtitulo"));
                partesConsulta.append(comparacion.group("titulo")).append(cadenaFiltrada + " ");
                analizadorSeleccionado = analizadores.analizadorRemoverStopWords;
            }
            if (comparacion.group("ref") != null) {
                cadenaFiltrada = quitarStopWords("ref",comparacion.group("vref"));
                partesConsulta.append(comparacion.group("ref")).append(cadenaFiltrada + " ");
                analizadorSeleccionado = analizadores.analizadorRemoverStopWords;
            }
            if (comparacion.group("texto") != null) {
                cadenaFiltrada = quitarStopWords("texto",comparacion.group("vtexto"));
                partesConsulta.append(comparacion.group("texto")).append(cadenaFiltrada + " ");
                analizadorSeleccionado = analizadores.analizadorConStemming;
            }
            if (comparacion.group("encab") != null) {
                cadenaFiltrada = quitarStopWords("encab",comparacion.group("vencab"));
                partesConsulta.append(comparacion.group("encab")).append(cadenaFiltrada + " ");
                analizadorSeleccionado = analizadores.analizadorConStemming;
            }
            campoSeleccionado = "texto";
            textoConsulta = partesConsulta.toString();
            System.out.println("Consulta personalidada: " + textoConsulta);
        }
        else {
            if (campoSeleccionado.equals("")) {
                campoSeleccionado = "texto";
                analizadorSeleccionado = analizadores.analizadorConStemming;
            }
            else if (campoSeleccionado.equals("titulo") || campoSeleccionado.equals("ref")){
                analizadorSeleccionado = analizadores.analizadorRemoverStopWords;
            }
            else {
                analizadorSeleccionado = analizadores.analizadorConStemming;
            }
        }
        QueryParser parser = new QueryParser(campoSeleccionado, analizadorSeleccionado);
        String consultaSinTildes = analizadores.limpiarAcentos(textoConsulta, true);
        try {
            System.out.println("consultaSinTildes:" + consultaSinTildes);
            consulta = parser.parse(consultaSinTildes);
            System.out.println("consulta despues de parsing:" + consulta);
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
                                                           String textoConsulta, int cantidadPorPagina, boolean personalizada) {

        documentosEncontrados = new ArrayList<ArrayList<DocumentoEncontrado>>();

        try {
            Path directorioIndices = Paths.get(".", directorioIndice);
            lector = DirectoryReader.open(FSDirectory.open(directorioIndices));
            System.out.println(lector.numDocs());
            lblDocumentosColeccion.setText("Documentos en la colección: " + lector.numDocs());
        }
        catch (IOException e){
            msgError.setTitle("ERROR");
            msgError.setHeaderText("Huno un error al abrir el directorio de índices");
            msgError.setContentText("Por favor, verifique que la dirección suministrada es correcta");
        }

        buscador = new IndexSearcher(lector);

        Query consulta = prepararConsulta(campoSeleccionado,textoConsulta, personalizada);

        if (consulta != null){
            ejecutarConsulta(consulta,cantidadPorPagina);
        }

        return documentosEncontrados;
    }
}
