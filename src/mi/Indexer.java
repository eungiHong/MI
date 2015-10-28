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
	
	private IndexWriter writer;
	private IndexWriterConfig conf;
	private Analyzer analyzer;
	
	public Indexer (Path indexDirectoryPath) throws IOException {
		
		// this directory will contain the indexes
		// �ε����� ������ ���͸� ����
		Directory indexDirectory = FSDirectory.open(indexDirectoryPath);
		
		//create the indexer
		analyzer = new StandardAnalyzer();
		conf = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(indexDirectory, conf);
		
	}
	
	public void close() throws CorruptIndexException, IOException {
		writer.close();
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
		
		document.add(contentField);
		document.add(fileNameField);
		document.add(filePathField);
		
		return document;
	}
	// ������ ��ť��Ʈȭ �� ��, �ε���
	private void indexFile (File file) throws IOException {
		System.out.println("Indexing " + file.getCanonicalPath());
		Document document = getDocument(file);
		writer.addDocument(document); // addDocument�� ���������� �ε��� ������ �����ϴµ�?
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
		return writer.numDocs(); // �ε��� ������ ������ �����ϴµ�?
	}
}
