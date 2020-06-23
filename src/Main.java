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

        Indexador indexador = new Indexador();
        indexador.leerStopWords();
        int cont = 0;
        try {
            ArrayList<ArrayList<String>> archivos = lector.obtenerDocumentos();
            for (ArrayList<String> htmls : archivos) {
                for (String html : htmls) {
                    indexador.indexarContenidos(html);
                    cont++;
                }
            }
            indexador.writer.close();
            System.out.println(cont + " indexados");
        }
        catch (IOException a) {
            System.out.println("Hubo un problema al leer los archivos.");
        }
    }
}