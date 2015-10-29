package mi;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
	
	private IndexWriter indexWriter;
	private IndexWriterConfig config;
	private Analyzer analyzer;
	
	public Indexer (Path indexDirectoryPath) throws IOException {
		// �ε����� ������ ���͸� ��� ����
		Directory indexDirectory = FSDirectory.open(indexDirectoryPath);	
		// Indexer ����
		analyzer = new StandardAnalyzer();
		config = new IndexWriterConfig(analyzer);
		indexWriter = new IndexWriter(indexDirectory, config);
	}
	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}
	// ������ �о ��ť��Ʈȭ
	private Document getDocument(File file) throws IOException {
		Document document = new Document();
		//index file contents
		@SuppressWarnings("deprecation")
		Field contentField = new Field(LuceneConstants.CONTENTS, new FileReader(file));
		//index file name
		@SuppressWarnings("deprecation")
		Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		//index file path
		@SuppressWarnings("deprecation")
		Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), Field.Store.YES, Field.Index.NOT_ANALYZED);
		
		document.add(contentField);  // Adds a field to a document.
		document.add(fileNameField);
		document.add(filePathField);	
		return document;
	}
	// ������ ��ť��Ʈȭ �� ��, indexWriter�� �߰�
	private void indexFile (File file) throws IOException {
		System.out.println("Indexing " + file.getCanonicalPath());
		Document document = getDocument(file);
		indexWriter.addDocument(document); // adds a document to this index
	}
	// �޼ҵ带 ȥ���Ͽ� ���������� �ε��� ����
	public int createIndex(String dataDirPath, FileFilter filter) throws IOException {
		//get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();
		for (File file : files) {
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				indexFile(file);
			}
		}
		return indexWriter.numDocs(); // Returns total number of documents in this index, including docs not yet 
									  // flushed (still in the RAM buffer), and including deletions.
	}
}
