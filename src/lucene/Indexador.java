package lucene;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Indexador
{

	private IndexWriter writer;
	ArrayList<String> bodies, referencias, titulos, encabezados;

	public Indexador(IndexWriter writer)
    {
        this.writer = writer;
        bodies = new ArrayList<String>();
        referencias = new ArrayList<String>();
        titulos = new ArrayList<String>();
        encabezados = new ArrayList<String>();
	}

	public void parsearHtml (String html)
	{
		org.jsoup.nodes.Document documento = Jsoup.parse(html);
//		System.out.println(documento.body().text());

//		documento.getElementsByTag("title");
//		documento.getElementsByTag("h1");
//		documento.getElementsByTag("h2");
//		documento.getElementsByTag("h3");
//		documento.getElementsByTag("h4");
//		documento.getElementsByTag("h5");
//		documento.getElementsByTag("h6");
//		documento.getElementsByTag("a");

		Elements referencias = documento.getElementsByTag("a");
		for (Element ref : referencias){
			System.out.println(ref.attr("abs:href"));
		}


	}
//	public void indexDocument(String html) throws Exception {
//
//		try {
//			Document document = new Document();
//			document.add(new Field());
//			document.add(new Field("fulltext", fulltext));
//			writer.addDocument(document);
//		}
//		finally {
//			fulltext.close();
//		}
//	}
//
//
//		// obtener stop words
//		FileReader lectorStopWords = new FileReader("stop words.txt");
//		// Se asignan los stopwords al analizador
//		StandardAnalyzer analizador = new StandardAnalyzer(lectorStopWords);
//		//Se define el directorio donde se almacenaran los indices
//		Path rutaDirectorioIndice = Paths.get(".","Proyecto-Lucene/indices");
//		Directory directorioIndice = FSDirectory.open(rutaDirectorioIndice);
//		// Se configura el indezador con el analizador
//		IndexWriterConfig configuracionIndice = new IndexWriterConfig(analizador);
//		// Se crea el indexador
//		writer = new IndexWriter(directorioIndice,configuracionIndice);
//	}
//	public void close() throws CorruptIndexException, IOException
//	{
//		writer.close();
//	}
//	private Document getDocument(File file) throws IOException
//	{
//		Document document = new Document();
//		//index file contents
//		Field contentField = new Field();
//		//index file name
//		Field fileNameField = new Field(LuceneConstants.FILE_NAME,
//			file.getName(),
//			Field.Store.YES,Field.Index.NOT_ANALYZED);
//		//index file path
//		Field filePathField = new Field(LuceneConstants.FILE_PATH,
//			file.getCanonicalPath(),
//			Field.Store.YES,Field.Index.NOT_ANALYZED);
//		document.add(contentField);
//		document.add(fileNameField);
//		document.add(filePathField);
//		return document;
//	}
//	private void indexFile(File file) throws IOException
//	{
//		System.out.println("Indexing "+file.getCanonicalPath());
//		Document document = getDocument(file);
//		writer.addDocument(document);
//	}
//	public int createIndex(String dataDirPath, FileFilter filter) throws IOException
//	{
//		//get all files in the data directory
//		File[] files = new File(dataDirPath).listFiles();
//		for (File file : files)
//		{
//			if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file) )
//			{
//				indexFile(file);
//			}
//		}
//		return writer.numDocs();
//	}
}