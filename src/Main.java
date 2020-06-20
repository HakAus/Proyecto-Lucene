import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ContentHandlerDecorator;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class Main {



    public static String parseToPlainText() throws IOException, SAXException, TikaException {
        BodyContentHandler handler = new BodyContentHandler(-1);

        Parser parser = new AutoDetectParser();
        Metadata metadata = new Metadata();
        ParseContext pcontext = new ParseContext();
        Path rutaDirectorioArchivo= Paths.get(".","/input/wiki-p2.txt");
        File initialFile = new File(rutaDirectorioArchivo.toUri());

        try (InputStream stream = new FileInputStream(initialFile)) {
            parser.parse(stream, handler, metadata, pcontext);
        }
        handler.
    }

    public static void main(String[] args) throws IOException, TikaException, SAXException {

        parseToPlainText();

    }

}