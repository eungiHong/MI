package mi;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Searcher {
	DirectoryReader directoryReader; // If your index has changed and you wish to see the changes reflected 
									 // in searching, you should use DirectoryReader.openIfChanged(DirectoryReader) 
									 // to obtain a new reader and then create a new IndexSearcher from that.
	private IndexSearcher indexSearcher;
	private HashMap<String, Analyzer> analyzerPerField;
	private PerFieldAnalyzerWrapper aWrapper;
	private QueryParser queryParser;
	private Query query;
	
	public Searcher(Path indexDirectoryPath, Analyzer analyzer) throws IOException {
		
		Directory indexDirectory = FSDirectory.open(indexDirectoryPath);
		directoryReader = DirectoryReader.open(indexDirectory); 
		indexSearcher = new IndexSearcher(directoryReader);
		
		analyzerPerField = new HashMap<String, Analyzer>();
		aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);
		// Indexer�� �����ϰ� ���ϸ��� KeywordAnalyzer�� �м�
		analyzerPerField.put(LuceneConstants.FILE_NAME, new KeywordAnalyzer());
	}
	
	// QueryParser�� �Ľ��� query�� ������ �ε����� �˻��Ͽ� TopDocs�� ����
	// TopDocs: represents hits returned by IndexSearcher.search(Query, int). It has "totalHits", 
	// the total number of hits for the query, and "scoreDocs", the top hits for the query, as its class fields.
	public TopDocs searchByQueryParser(String searchQuery) throws IOException, ParseException {
		queryParser = new QueryParser(LuceneConstants.CONTENTS, aWrapper); // ������� ������ Term���� ��ȯ�ϴ� QueryParser
																		   // ***fieldName: the default field for query terms.
		//queryParser = new QueryParser(LuceneConstants.FILE_NAME, aWrapper);
		query = queryParser.parse(searchQuery);
		// query�� � ������� �����Ǿ����� Ȯ��
		System.out.println("Generated query form: " + query.toString());
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	// searchQuery�� ������ �� �ϳ��� �����Ͽ� �˻�
	public TopDocs searchByTermQuery(String searchQuery) throws IOException, ParseException {
		Term term = new Term(LuceneConstants.CONTENTS, searchQuery);
		query = new TermQuery(term);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	// searchQuery�� ���λ�(prefix)�� ������ �ִ� ���� �˻�
	// ���� ǥ���Ŀ� asterisk(*)�� �ٿ� ����ϸ� QueryParser�� PrefixQuery�� �ؼ�
	public TopDocs searchByPrefixQuery(String searchQuery) throws IOException, ParseException {
		Term term = new Term(LuceneConstants.CONTENTS, searchQuery);
		query = new PrefixQuery(term);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	
	public TopDocs searchByPhraseQuery(String[] phrase, int slop) throws IOException, ParseException {
		PhraseQuery.Builder builder = new PhraseQuery.Builder();
		for (int i = 0; i < phrase.length; i++) {
			builder.add(new Term(LuceneConstants.CONTENTS, phrase[i]));
		}
		builder.setSlop(slop);
		PhraseQuery phraseQuery = builder.build(); 
		return indexSearcher.search(phraseQuery, LuceneConstants.MAX_SEARCH);
	}
	
	// ���� �Ÿ�(edit distance)�� �̿��Ͽ� searchQuery�� ������ ���� �˻�
	// ���� ǥ���Ŀ� tilde(~)�� �ٿ� ����ϸ� QueryParser�� FuzzyQuery�� �ؼ�
	public TopDocs searchByFuzzyQuery(String searchQuery) throws IOException, ParseException {
		query = new FuzzyQuery(new Term(LuceneConstants.CONTENTS, searchQuery));
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	public Explanation explainTheProcess(TopDocs searchResult) throws IOException {
		return indexSearcher.explain(query, searchResult.totalHits);
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