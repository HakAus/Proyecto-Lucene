import lucene.Indexador;
import lucene.Lector;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Main {

    public static void main(String[] args) throws IOException {
        Lector lector = new Lector();
        FileReader lectorStopWords;
//         obtener stop words
        try {
//            Path rutaDirectorioStopWords = Paths.get(".","Proyecto-Lucene/stop words.txt");
//            lectorStopWords= new FileReader(rutaDirectorioStopWords.toString());
            // Se asignan los stopwords al analizador
            StandardAnalyzer analizador = new StandardAnalyzer();
            IndexWriterConfig configuracionIndice = new IndexWriterConfig(analizador);
            Path rutaDirectorioIndice = Paths.get(".","Proyecto-Lucene/indices");
            Directory directorioIndice = FSDirectory.open(rutaDirectorioIndice);
            IndexWriter writer = new IndexWriter(directorioIndice,configuracionIndice);

            Indexador indexador = new Indexador(writer);
            try {
                ArrayList<ArrayList<String>> archivos = lector.obtenerDocumentos();
                for (ArrayList<String> htmls : archivos) {
                    for (String html : htmls) {
                        indexador.parsearHtml(html);
                    }
                }
            }
            catch (IOException a) {
                System.out.println("Hubo un problema al leer los archivos.");
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("No se encontro el archivo de stop words");
        }
    }
}