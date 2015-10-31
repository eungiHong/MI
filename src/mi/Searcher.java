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
	DirectoryReader directoryReader; // If your index has changed and you wish to see the changes reflected 
									 // in searching, you should use DirectoryReader.openIfChanged(DirectoryReader) 
									 // to obtain a new reader and then create a new IndexSearcher from that.
	IndexSearcher indexSearcher;
	QueryParser queryParser;
	Query query;

	public Searcher(Path indexDirectoryPath, String fieldname, Analyzer analyzer) throws IOException {
		Directory indexDirectory = FSDirectory.open(indexDirectoryPath);
		directoryReader = DirectoryReader.open(indexDirectory); // �ε����� �о� ����
		indexSearcher = new IndexSearcher(directoryReader); // �ε����� �˻�
		queryParser = new QueryParser(LuceneConstants.CONTENTS, analyzer); // ������ ��ǻ�Ͱ� �����ϴ� ������� ��ȯ�ϴ� QueryParser
															// fieldname: the default field for query terms.
	}
	// QueryParser�� �Ľ��� query�� ������ �ε����� �˻��Ͽ� TopDocs�� ����
	// TopDocs: represents hits returned by IndexSearcher.search(Query, int). It has "totalHits", 
	// the total number of hits for the query, and "scoreDocs", the top hits for the query, as its class fields.
	public TopDocs search(String searchQuery)
			throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		query = queryParser.parse(searchQuery);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	// TopDocs���� �ϳ��� hit�� �̷�� ScoreDoc���κ��� ��ü ������ ����
	public Document getDocument(ScoreDoc scoreDoc) throws CorruptIndexException, IOException {
		// indexSearcher.doc = Sugar for indexSearcher.getIndexReader().document(docID)
		// ScoreDoc: Holds one hit in TopDocs. It has "score", the score of this document for the query, 
		// "doc", a hit document's number, and "shardIndex", as its class fields.
		return indexSearcher.doc(scoreDoc.doc);
	}
	public void close() throws IOException {
		directoryReader.close();
	}
}