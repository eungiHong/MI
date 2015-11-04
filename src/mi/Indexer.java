package mi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
	
	private IndexWriter indexWriter;
	private IndexWriterConfig config;
	private HashMap<String, Analyzer> analyzerPerField;
	private PerFieldAnalyzerWrapper aWrapper;

	public Indexer (Path indexDirectoryPath) throws IOException {
		// �ε����� ������ ���͸� ��� ����
		Directory indexDirectory = FSDirectory.open(indexDirectoryPath);
		
		analyzerPerField = new HashMap<String, Analyzer>();
		aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);
		// ���ϸ��� ��� KeywordAnalyzer�� �м� (default�� StandardAnalyzer)
		analyzerPerField.put(LuceneConstants.FILE_NAME, new KeywordAnalyzer());
		
		config = new IndexWriterConfig(aWrapper);
		indexWriter = new IndexWriter(indexDirectory, config);
	}
	
	public void close() throws CorruptIndexException, IOException {
		indexWriter.close();
	}
	
	// ������ �о ��ť��Ʈȭ
	private Document getDocument(File file) throws IOException {
		Document document = new Document();
		
		// index file content (stored)
		BufferedReader in = new BufferedReader(new FileReader(file));
		String contents = "";
		while (true) {
			contents = contents + " " + in.readLine();
			if (in.readLine() == null) break;
		}
		in.close();
		Field contentField = new TextField(LuceneConstants.CONTENTS, contents, Field.Store.YES);
				
		// index file content (not stored)
		// Field contentField = new TextField(LuceneConstants.CONTENTS, new FileReader(file));
		
		// index file name
		Field fileNameField = new StringField(LuceneConstants.FILE_NAME, file.getName(), Field.Store.YES);
		//index file path
		Field filePathField = new StringField(LuceneConstants.FILE_PATH, file.getCanonicalPath(), Field.Store.YES);
		
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
		File[] files = new File(dataDirPath).listFiles(); // dataDirPath ���� �����̳� ���丮�� ����Ʈ�� ��� ��ȯ
		for (File file : files) {
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				indexFile(file);
			}
		}
		return indexWriter.numDocs(); // Returns total number of documents in this index, including docs not yet 
									  // flushed (still in the RAM buffer), and including deletions.
	}
}
