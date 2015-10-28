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
		Directory indexDirectory = FSDirectory.open(indexDirectoryPath); // 꼭 있어야 할까?
		indexReader = DirectoryReader.open(indexDirectory); // 인덱스를 읽어 들임
		indexSearcher = new IndexSearcher(indexReader); // 인덱스를 검색
		queryParser = new QueryParser(fieldname, analyzer);  // 질문을 컴퓨터가 이해하는 방식으로 변환하는 QueryParser
	}
	// QueryParser가 파싱한 query를 가지고 index를 검색
	public TopDocs search(String searchQuery) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		query = queryParser.parse(searchQuery);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	// indexSearcher가 찾아낸 문서를 리턴
	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
		return indexSearcher.doc(scoreDoc.doc);
	}
	public void close() throws IOException {
		indexReader.close();
	}
}