package lucene;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.sun.javafx.css.parser.Token;
import javafx.scene.control.Alert;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.tartarus.snowball.ext.SpanishStemmer;


public class Indexador
{
	Analizador analizadores;
	public IndexWriter writer;
	CharArraySet stopWords;
	ArrayList<String> bodies, referencias, titulos, encabezados;

	public Indexador() {
        bodies = new ArrayList<String>();
        referencias = new ArrayList<String>();
        titulos = new ArrayList<String>();
        encabezados = new ArrayList<String>();
        analizadores = new Analizador();
	}

	private static String obtenerTokens(TokenStream stream) throws IOException
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
	private static void mostrarTokens(Analyzer analyzer,String text) throws IOException
	{
		TokenStream stream = analyzer.tokenStream(null,new StringReader(text));
		CharTermAttribute caracter = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			System.out.print(" [" + caracter.toString()+ "]" + "\n");
		}
		stream.end();
		stream.close();
	}

	public void configurarIndexador(String directorioIndices, boolean actualizar) throws IOException {
		Analizador analizador = new Analizador();
		analizador.leerStopWords();
		Path rutaDirectorioIndice = Paths.get(".",directorioIndices);
		Directory directorioIndice = FSDirectory.open(rutaDirectorioIndice);
		IndexWriterConfig configuracionIndice = new IndexWriterConfig(analizador.analizadorSimple);
		if (actualizar)
			configuracionIndice.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
		else
			configuracionIndice.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		try {
			writer = new IndexWriter(directorioIndice, configuracionIndice);
		}
		catch (IOException e){
			System.out.println("Hubo un problema al configurar el indexador jaja");
		}
	}

	public String sacarRaices(String campo, String texto) {
		TokenStream streamTextoConRaices = analizadores.analizadorConStemming.tokenStream(campo,texto);
		try {
			return obtenerTokens(streamTextoConRaices);
		}
		catch (IOException e){
			Alert alerta = new Alert(Alert.AlertType.ERROR);
			alerta.setTitle("ERROR");
			alerta.setHeaderText("No se pudo hacer el stemming");
			alerta.show();
		}
		return null;
	}

	public String quitarStopWords(String campo, String texto){
		try {
			TokenStream streamTextoSinStopWords =  analizadores.analizadorRemoverStopWords.tokenStream(campo,texto);
			return obtenerTokens(streamTextoSinStopWords);
		}
		catch (IOException e){
			Alert alerta = new Alert(Alert.AlertType.ERROR);
			alerta.setTitle("ERROR");
			alerta.setHeaderText("Hubo una error al remover los stop words de " + campo);
			alerta.show();
		}
		return null;
	}

	public void indexarContenidos(String html) {
		try {
			analizadores.leerStopWords();
		}
		catch(IOException e) {
			Alert alerta = new Alert(Alert.AlertType.ERROR);
			alerta.setTitle("ERROR");
			alerta.setHeaderText("Hubo una error al intentar leer los stop words");
			alerta.show();
		}
		Document DocumentoLucene = new Document();

		org.jsoup.nodes.Document Html = Jsoup.parse(html);

		String HTML;

		// Se indexan primero los valores SIN STEMMING
		// Se indexa el <title>
		HTML = Html.getElementsByTag("title").text();
		HTML = quitarStopWords("titulo",HTML);
		HTML = analizadores.limpiarAcentos(HTML);

		IndexableField title = null;
		if (HTML != null)
			title = new TextField("titulo",HTML,Field.Store.YES);
		else
			System.out.println("El texto de los titulos esta vacio O.o");

		// Se indexa las <a>
		HTML = Html.getElementsByTag("a").text();
		HTML = quitarStopWords("ref",HTML);
		HTML = analizadores.limpiarAcentos(HTML);
		IndexableField links = null;
		if (HTML != null)
			links = new TextField("ref",HTML, Field.Store.YES);
		else
			System.out.println("El texto de las referencias esta vacio O.o");

		// Se indexan los campos CON STEMMING
		// Se indexa el body del html
		HTML = Html.body().text();
		HTML = sacarRaices("texto",HTML);
		HTML = analizadores.limpiarAcentos(HTML);
		IndexableField body = new TextField("texto",HTML, Field.Store.YES);
		// Se indexa los <h?>
		StringBuilder encabezados = new StringBuilder();
		encabezados.append(Html.getElementsByTag("h1").text());
		encabezados.append(Html.getElementsByTag("h2").text());
		encabezados.append(Html.getElementsByTag("h3").text());
		encabezados.append(Html.getElementsByTag("h4").text());
		encabezados.append(Html.getElementsByTag("h5").text());
		encabezados.append(Html.getElementsByTag("h6").text());

		HTML = encabezados.toString();
		HTML = sacarRaices("encab",HTML);
		HTML = analizadores.limpiarAcentos(HTML);
		IndexableField headers = new TextField("encab",HTML, Field.Store.YES);

		DocumentoLucene.add(links);
		DocumentoLucene.add(title);
		DocumentoLucene.add(headers);
		DocumentoLucene.add(body);

		try {
			writer.addDocument(DocumentoLucene);
		}
		catch (IOException e) {
			System.out.println("Hubo un error en la indexacion");
		}
	}
}