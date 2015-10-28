package mi;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class LuceneTester {
	
	String indexDir = "C:\\Lucene\\Index";
	String dataDir = "C:\\Lucene\\Data";
	Path indexPath = Paths.get(indexDir);
	Indexer indexer;
	Searcher searcher;
	
	public static void main(String[] args) throws org.apache.lucene.queryparser.classic.ParseException {
		LuceneTester tester;
		try {
			tester = new LuceneTester();
			// tester.createIndex10();
			tester.search10("functional");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	private void createIndex10() throws IOException {
		indexer = new Indexer(indexPath);
		int numIndexed;
		long startTime = System.currentTimeMillis();
		numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
		long endTime = System.currentTimeMillis();
		indexer.close();
		System.out.println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms");
	}
	
	private void search10(String searchQuery) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		searcher = new Searcher(indexPath, LuceneConstants.CONTENTS, new StandardAnalyzer());
		long startTime = System.currentTimeMillis();
		TopDocs hits = searcher.search(searchQuery);
		long endTime = System.currentTimeMillis();
		
		System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime));
		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document doc = searcher.getDocument(scoreDoc);
			System.out.println("File: " + doc.get(LuceneConstants.FILE_PATH));
		}
		searcher.close();
	}
}
