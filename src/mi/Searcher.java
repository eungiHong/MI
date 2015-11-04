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
		// Indexer와 동일하게 파일명은 KeywordAnalyzer로 분석
		analyzerPerField.put(LuceneConstants.FILE_NAME, new KeywordAnalyzer());
	}
	
	// QueryParser가 파싱한 query를 가지고 인덱스를 검색하여 TopDocs를 리턴
	// TopDocs: represents hits returned by IndexSearcher.search(Query, int). It has "totalHits", 
	// the total number of hits for the query, and "scoreDocs", the top hits for the query, as its class fields.
	public TopDocs searchByQueryParser(String searchQuery) throws IOException, ParseException {
		queryParser = new QueryParser(LuceneConstants.CONTENTS, aWrapper); // 사용자의 질문을 Term으로 변환하는 QueryParser
																		   // ***fieldName: the default field for query terms.
		//queryParser = new QueryParser(LuceneConstants.FILE_NAME, aWrapper);
		query = queryParser.parse(searchQuery);
		// query가 어떤 모양으로 생성되었는지 확인
		System.out.println("Generated query form: " + query.toString());
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	// searchQuery를 가지고 텀 하나를 생성하여 검색
	public TopDocs searchByTermQuery(String searchQuery) throws IOException, ParseException {
		Term term = new Term(LuceneConstants.CONTENTS, searchQuery);
		query = new TermQuery(term);
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	// searchQuery를 접두사(prefix)로 가지고 있는 텀을 검색
	// 질의 표현식에 asterisk(*)를 붙여 사용하면 QueryParser가 PrefixQuery로 해석
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
	
	// 편집 거리(edit distance)를 이용하여 searchQuery와 유사한 텀을 검색
	// 질의 표현식에 tilde(~)를 붙여 사용하면 QueryParser가 FuzzyQuery로 해석
	public TopDocs searchByFuzzyQuery(String searchQuery) throws IOException, ParseException {
		query = new FuzzyQuery(new Term(LuceneConstants.CONTENTS, searchQuery));
		return indexSearcher.search(query, LuceneConstants.MAX_SEARCH);
	}
	
	public Explanation explainTheProcess(TopDocs searchResult) throws IOException {
		return indexSearcher.explain(query, searchResult.totalHits);
	}
	
	// TopDocs에서 하나의 hit을 이루는 ScoreDoc으로부터 전체 문서를 리턴
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