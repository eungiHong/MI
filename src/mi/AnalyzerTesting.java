package mi;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class AnalyzerTesting {
	
	private static final String[] strings = {"Colorless green sleeps furiously", "I think that Blake knows that car.", "AB&C Corportation (abc@naver.com)", "Haskell.txt"};
	private static final Analyzer[] analyzers = new Analyzer[] {new WhitespaceAnalyzer(), new SimpleAnalyzer(), new StopAnalyzer(), new StandardAnalyzer()};
	public static void main(String[] args) throws IOException {
		for (int i = 0; i < strings.length; i++) {
			analyze(strings[i]);
		}
	}
	private static void analyze(String text) throws IOException {
		System.out.println("Analyzing \"" + text + "\"\n");
		for (int i = 0; i < analyzers.length; i++) {
			Analyzer analyzer = analyzers[i];
			System.out.println(analyzer.getClass().getName() + ":");
			// A TokenStream enumerates the sequence tokens, either from Fields or a Document or from query text.
			// TokenStream is an abstract class which has Tokenizer and TokenFilter as its subclasses.
			// analyzer.tokenStream returns a TokenStream suitable for fieldName (in this case, "contents"), tokenizing the contents of "text".
			TokenStream stream = analyzer.tokenStream("contents", text); 
			
			try {
				// reset() is called by a consumer (i.e, IndexWriter) before begins consumption using incrementToken()
				stream.reset();
				// A consumer uses incrementToken() to advance stream to the next token.
				while (stream.incrementToken()) {
					System.out.println(stream.toString());
				}
				// end() is called by a consumer after the last token has been consumed, after incrementToken() returns false.
				stream.end();
			} finally {
				// close() releases resources associated with the stream.
				stream.close();
			}
			System.out.println("\n");
		}
	}
}
