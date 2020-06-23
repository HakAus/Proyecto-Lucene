package lucene;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.tartarus.snowball.ext.SpanishStemmer;
import ucar.ma2.Index;


public class Indexador
{

	public IndexWriter writer;
	CharArraySet stopWords;
	Analyzer analizador;
	ArrayList<String> bodies, referencias, titulos, encabezados;

	public Indexador()
    {
        this.writer = configurarIndexador();
        bodies = new ArrayList<String>();
        referencias = new ArrayList<String>();
        titulos = new ArrayList<String>();
        encabezados = new ArrayList<String>();
	}

	public void leerStopWords() throws IOException {
		ArrayList<String> listaStopWords = new ArrayList<String>();
		Path ubicacionStopWords = Paths.get(".","stop_words");
		File archivoStopWords = new File(ubicacionStopWords.toString());
		try (BufferedReader lector = new BufferedReader(new FileReader(archivoStopWords))) {
			String stopWord;
			while ((stopWord = lector.readLine()) != null){
				listaStopWords.add(stopWord);
			}
		}
		stopWords = new CharArraySet(listaStopWords,false);
	}

	private static void mostrarTokens(Analyzer analyzer,String text) throws IOException
	{
		TokenStream stream = analyzer.tokenStream(null,new StringReader(text));
		CharTermAttribute caracter = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			System.out.print(" [" + caracter.toString()+ "] ");
		}
		stream.end();
		stream.close();
	}

//	public TokenStream tokenStream(String fieldName, Reader reader) {
//		return new SnowballFilter()new StopFilter(true,
//				new LowerCaseTokenizer(reader),
//				stopWords);
//	}
	public IndexWriter configurarIndexador(){
		analizador = new Analyzer() {

			@Override
			protected TokenStreamComponents createComponents(String nombreCampo) {
				TokenizerEspanol tokenizerEspanol = new TokenizerEspanol();
				// Se asignan los stopwords al analizador
				TokenStream filtroMinusculas = new LowerCaseFilter(tokenizerEspanol);
				TokenStream filtroStopWords = new StopFilter(filtroMinusculas, stopWords);
				// Se hace el stemming de acuerdo a la implementación Snowball del stemmer en español
				SpanishStemmer stemmerEspanol = new SpanishStemmer();
				TokenStream filtro = new SnowballFilter(filtroStopWords,stemmerEspanol);
				return new TokenStreamComponents(tokenizerEspanol, filtro);
			}
		};
		IndexWriterConfig configuracionIndice = new IndexWriterConfig(analizador);
		Path rutaDirectorioIndice = Paths.get(".","/indices");
		try {
			Directory directorioIndice = FSDirectory.open(rutaDirectorioIndice);
			writer = new IndexWriter(directorioIndice, configuracionIndice);
			return writer;
		}
		catch (IOException e){
			System.out.println("Hubo un problema al configurar el indexador");
		}
		return null;
	}

	public void indexarContenidos(String html)
	{
		Document DocumentoLucene = new Document();

		org.jsoup.nodes.Document Html = Jsoup.parse(html);
		// Se indexa el <body>
		String HTML = Html.body().text();
		IndexableField body = new TextField("texto",HTML, Field.Store.YES);

		// Se indexa el <title>
		HTML = Html.getElementsByTag("title").text();
		IndexableField title = new TextField("titulo",HTML,Field.Store.YES);
		// Se indexa los <h?>
		StringBuilder encabezados = new StringBuilder();
		encabezados.append(Html.getElementsByTag("h1").text());
		encabezados.append(Html.getElementsByTag("h2").text());
		encabezados.append(Html.getElementsByTag("h3").text());
		encabezados.append(Html.getElementsByTag("h4").text());
		encabezados.append(Html.getElementsByTag("h5").text());
		encabezados.append(Html.getElementsByTag("h6").text());

		IndexableField headers = new TextField("encab",encabezados.toString(),Field.Store.YES);

		// Se indexa las <a>
		HTML = Html.getElementsByTag("a").text();
		IndexableField links = new TextField("ref",HTML,Field.Store.YES);


		DocumentoLucene.add(headers);
		DocumentoLucene.add(links);
		DocumentoLucene.add(body);
		DocumentoLucene.add(title);

		try {
			writer.addDocument(DocumentoLucene);
		}
		catch (IOException e) {
			System.out.println("Hubo un error en la indexacion");
		}
	}
}