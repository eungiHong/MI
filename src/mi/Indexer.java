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
		// 인덱스를 저장할 디렉터리 설정
		Directory indexDirectory = FSDirectory.open(indexDirectoryPath);
		
		//create the indexer
		analyzer = new StandardAnalyzer();
		conf = new IndexWriterConfig(analyzer);
		writer = new IndexWriter(indexDirectory, conf);
		
	}
	
	public void close() throws CorruptIndexException, IOException {
		writer.close();
	}
	
	// 파일을 읽어서 다큐먼트화
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
	// 파일을 다큐먼트화 한 뒤, 인덱싱
	private void indexFile (File file) throws IOException {
		System.out.println("Indexing " + file.getCanonicalPath());
		Document document = getDocument(file);
		writer.addDocument(document); // addDocument가 실질적으로 인덱스 파일을 생성하는듯?
	}
	
	// 메소드를 혼합하여 실질적으로 인덱스 생성
	public int createIndex(String dataDirPath, FileFilter filter) throws IOException {
		//get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();
		
		for (File file : files) {
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				indexFile(file);
			}
		}
		return writer.numDocs(); // 인덱스 문서의 개수를 리턴하는듯?
	}
}
