package mi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

public class LuceneTester {
	
	String indexDir = "C:"+File.separator+"Lucene"+File.separator+"Index";
	String dataDir = "C:"+File.separator+"Lucene"+File.separator+"Data";
	Path indexPath = Paths.get(indexDir);
	Indexer indexer;
	Searcher searcher;
	
	public static void main(String[] args) throws org.apache.lucene.queryparser.classic.ParseException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		LuceneTester tester;
		
		try {
			tester = new LuceneTester();
			tester.createIndex();
			System.out.println("\nWhat do you want to find out, you mortal?");
			String question = br.readLine();
			tester.search(question);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	private void createIndex() throws IOException {
		indexer = new Indexer(indexPath);
		int numIndexed;
		long startTime = System.currentTimeMillis();
		numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
		long endTime = System.currentTimeMillis();
		indexer.close();
		System.out.println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms");
	}
	
	private void search(String searchQuery) throws IOException, ParseException, org.apache.lucene.queryparser.classic.ParseException {
		searcher = new Searcher(indexPath, LuceneConstants.CONTENTS, new StandardAnalyzer());
		long startTime = System.currentTimeMillis();
		TopDocs hits = searcher.search(searchQuery);
		long endTime = System.currentTimeMillis();
		
		System.out.println(hits.totalHits + " documents found. Time :" + (endTime - startTime));
		for (ScoreDoc scoreDoc : hits.scoreDocs) {
			Document docCandidate = searcher.getDocument(scoreDoc);
			System.out.println("File Path: " + docCandidate.get(LuceneConstants.FILE_PATH));
			System.out.println("File Name: " + docCandidate.get(LuceneConstants.FILE_NAME));
			System.out.println("File Contents:" + docCandidate.get(LuceneConstants.CONTENTS));
		}
		searcher.close();
	}
}
