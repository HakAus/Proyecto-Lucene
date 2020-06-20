package lucene;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document; 
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException; 
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.Tika;


public class LuceneIndexer
{
    private final Tika tika;
	private IndexWriter writer;

	public LuceneIndexer(Tika tika, IndexWriter writer)
    {
        this.tika = tika;
        this.writer = writer;
	}

	public void indexDocument(File archivo) {
        Document document = new Document();
        document.add(new Field(LuceneConstants.FILE_NAME, archivo.getName(),
                Field.Store.YES,);
	}
		// obtener stop words
		FileReader lectorStopWords = new FileReader("stop words.txt");
		// Se asignan los stopwords al analizador
		StandardAnalyzer analizador = new StandardAnalyzer(lectorStopWords);
		//Se define el directorio donde se almacenaran los indices
		Path rutaDirectorioIndice = Paths.get(".","Proyecto-Lucene/indices");
		Directory directorioIndice = FSDirectory.open(rutaDirectorioIndice);
		// Se configura el indezador con el analizador
		IndexWriterConfig configuracionIndice = new IndexWriterConfig(analizador);
		// Se crea el indexador
		writer = new IndexWriter(directorioIndice,configuracionIndice);
	}
	public void close() throws CorruptIndexException, IOException
	{
		writer.close();
	}
	private Document getDocument(File file) throws IOException
	{
		Document document = new Document();
		//index file contents
		Field contentField = new Field();
		//index file name
		Field fileNameField = new Field(LuceneConstants.FILE_NAME,
			file.getName(),
			Field.Store.YES,Field.Index.NOT_ANALYZED);
		//index file path
		Field filePathField = new Field(LuceneConstants.FILE_PATH,
			file.getCanonicalPath(),
			Field.Store.YES,Field.Index.NOT_ANALYZED);
		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);
		return document;
	}
	private void indexFile(File file) throws IOException
	{
		System.out.println("Indexing "+file.getCanonicalPath());
		Document document = getDocument(file);
		writer.addDocument(document);
	}
	public int createIndex(String dataDirPath, FileFilter filter) throws IOException
	{
		//get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();
		for (File file : files)
		{
			if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file) )
			{
				indexFile(file);
			}
		}
		return writer.numDocs();
	}
}