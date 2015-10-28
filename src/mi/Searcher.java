package mi;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
	DirectoryReader indexReader;
	IndexSearcher indexSearcher;
	QueryParser queryParser;
	Query query;
	
	public Searcher(Path indexDirectoryPath, String fieldname, Analyzer analyzer) throws IOException {
		Directory indexDirectory = FSDirectory.open(indexDirectoryPath); // �� �־�� �ұ�?
		indexReader = DirectoryReader.open(indexDirectory); // �ε����� �о� ����
		indexSearcher = new IndexSearcher(indexReader); // �ε����� �˻�
		queryParser = new QueryParser(fieldname, analyzer);  // ������ ��ǻ�Ͱ� �����ϴ� ������� ��ȯ�ϴ� QueryParser
	}
	// QueryParser�� �Ľ��� query�� ������ index�� �˻�
	public TopDocs search(String searchQuery) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		query = queryParser.parse(searchQuery);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	// indexSearcher�� ã�Ƴ� ������ ����
	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
		return indexSearcher.doc(scoreDoc.doc);
	}
	public void close() throws IOException {
		indexReader.close();
	}
}