package br.edu.utfpr.cm.scienceevol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;


public class StopWordFilter
{
	private static final String[] DEFAULT_STOPWORD_FILES = {
		"stopwords-1.txt",
		"stopwords-2.txt",
		"stopwords-ranks-default.txt",
		"stopwords-ranks-google.txt",
		"stopwords-ranks-mysql.txt",
		"stopwords-sbsc.txt",
		"usado.txt"
	};
	
	private Set<String> stopwords;
	
	public StopWordFilter() {
		stopwords = new TreeSet<String>();
	}
	
	public void reset() {
		stopwords.clear();
	}
	
	public void loadDefaultStopwords() {
		for (String resource : DEFAULT_STOPWORD_FILES) {
			InputStream is = StopWordFilter.class.getResourceAsStream("/stopwords/" + resource);
			InputStreamReader streamReader = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(streamReader);
			String word = null;
			try {
				while ((word = reader.readLine()) != null) {
					stopwords.add(word);
				}
			} catch (IOException e) {
				throw new UnsupportedOperationException("Error reading file " +  resource, e);
			}
		}
	}
	
	public CharArraySet getStopWordListAsSet(){
		CharArraySet set = new CharArraySet(Version.LUCENE_42, stopwords, true);
		return set;
	}
}
