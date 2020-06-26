package lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Buscador {
    public ArrayList<DocumentoEncontrado> buscarDocumento (String directorioIndice, String campoSeleccionado, String consulta, int cantidadPorPagina) {
        ArrayList<DocumentoEncontrado> documentoEncontrados = new ArrayList<DocumentoEncontrado>();

        try {
            Path directorioIndices = Paths.get(".", directorioIndice);

            String queries = "";
            IndexReader lector = DirectoryReader.open(FSDirectory.open(directorioIndices));
            IndexSearcher buscador = new IndexSearcher(lector);
            Analizador analizador = new Analizador();
            analizador.leerStopWords();

            if (campoSeleccionado.equals("titulo"))
                campoSeleccionado = campoSeleccionado+"Buscar";
            QueryParser parser = new QueryParser(campoSeleccionado, analizador.analizadorSimple);
            String consultaSinTildes = analizador.limpiarAcentos(consulta, true);
            Query q = parser.parse(consultaSinTildes);
            System.out.println("Buscando en: " + q.toString(campoSeleccionado));

            // Empieza la busqueda
            long inicio = System.currentTimeMillis();

            buscador.search(q, 100, Sort.RELEVANCE);

            TopDocs resultados = buscador.search(q,cantidadPorPagina);
            ScoreDoc[] documentosEncontrados = resultados.scoreDocs;

            int numTotalHits = Math.toIntExact(resultados.totalHits.value);
            System.out.println(numTotalHits + " documentos relacionados en total");

            int start = 0;
            int end = Math.min(numTotalHits, cantidadPorPagina);

            int posicion = 0;
            float puntaje = 0;
            for (ScoreDoc scoreDoc : documentosEncontrados) {
                puntaje = scoreDoc.score;
                System.out.println("doc=" + scoreDoc.doc + " score=" + scoreDoc.score);
                Document doc = buscador.doc(scoreDoc.doc);
                DocumentoEncontrado documento = new DocumentoEncontrado();

                if (doc != null) {
                    posicion++;
                    documento.setPosicion(posicion);
                    documento.setPuntaje(puntaje);
                    IndexableField campo = doc.getField("tituloBuscar");
                    if (campo != null) {
                        documento.setTituloBuscar(campo.stringValue());
                    }
                    campo = doc.getField("archivo");
                    if (campo != null) {
                        documento.setArchivo(campo.stringValue());
                    }
                    campo = doc.getField("posicionInicial");
                    if (campo != null) {
                        documento.setPosicionInicialDocumento(Integer.parseInt(campo.stringValue()));
                    }
                    campo = doc.getField("largoDocumento");
                    if (campo != null) {
                        documento.setLargoDocumento(Integer.parseInt(campo.stringValue()));
                    }
                    campo = doc.getField("tituloMostrar");
                    if (campo != null) {
                        documento.setTituloMostrar(campo.stringValue());
                    }
                    campo = doc.getField("ref");
                    if (campo != null) {
                        documento.setReferencias(campo.stringValue());
                    }
                    campo = doc.getField("encab");
                    if (campo != null) {
                        documento.setEncabezados(campo.stringValue());
                    }
                    campo = doc.getField("texto");
                    if (campo != null) {
                        documento.setTexto(campo.stringValue());
                    }
                    System.out.println(documento.getArchivo());
                    documentoEncontrados.add(documento);
                }
            }

            long fin = System.currentTimeMillis();
            double tiempo = (double) ((fin - inicio));
            System.out.println(tiempo + " milisegundos");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return documentoEncontrados;
    }
}
