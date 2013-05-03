package br.edu.utfpr.cm.scienceevol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.SnowballStemmer;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.imports.BibtexParser;
import net.sf.jabref.imports.ParserResult;

public class BibTeX2DTM
{
	private static class BibTexEntryComparator implements Comparator<BibtexEntry>
	{
		@Override
		public int compare(BibtexEntry entry1, BibtexEntry entry2) {
			int year1 = Integer.parseInt(entry1.getField("year"));
			int year2 = Integer.parseInt(entry2.getField("year"));
			
			if (year1 == year2) {
				String title1 = entry1.getField("title");
				String title2 = entry1.getField("title");
				return title1.compareTo(title2);
			} else {
				return year1 - year2;
			}
		}
		
	}
	
	private static final String DTM_EXTENSION = ".dat";
	
	private static final String DTM_CORPUS_PREFIX = "-mult";

	private static final String DTM_SEQUENCE_PREFIX = "-seq";
	
	private static final String DTM_TERMS_PREFIX = "-vocab";
	
	private static final String DTM_DOCS_PREFIX = "-docs";
	
	private InputStream input;

	private File dtmFileMain;

	private File dtmFileAux;
	
	private File dtmFileVocab;
	
	private File dtmFileDocs;

	private String[] fieldsToImport = { "title", "abstract", "keywords" };
	
	private List<BibtexEntry> entries;

	private Map<BibtexEntry, Map<String, Integer>> corpus;
	
	private Map<String, Integer> terms;
	
	public BibTeX2DTM()
	{
		terms = new HashMap<String, Integer>();
		corpus = new HashMap<BibtexEntry, Map<String, Integer>>();
	}
	
	public void setBibtexFile(File file)
	{
		if (file == null || !file.exists()) {
			throw new IllegalArgumentException("Invalid BibTeX file");
		}

		String baseDir = file.getParent();
		String name = file.getName();
		name = name.substring(0, name.lastIndexOf("."));
		
		try {
			setInputStream(new FileInputStream(file), new File(baseDir), name);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}

	}
	
	public void setInputStream(InputStream is, File baseDir, String prefixName)
	{
		input = is;
		dtmFileMain = new File(baseDir, prefixName + DTM_CORPUS_PREFIX + DTM_EXTENSION);
		dtmFileAux = new File(baseDir, prefixName + DTM_SEQUENCE_PREFIX + DTM_EXTENSION);
		dtmFileVocab = new File(baseDir, prefixName + DTM_TERMS_PREFIX + DTM_EXTENSION);
		dtmFileDocs = new File(baseDir, prefixName + DTM_DOCS_PREFIX + DTM_EXTENSION);
	}
	

	public void write() throws IOException
	{
		BufferedWriter corpusWriter = new BufferedWriter(new FileWriter(dtmFileMain));
		BufferedWriter seqWriter = new BufferedWriter(new FileWriter(dtmFileAux));
		BufferedWriter termsWriter = new BufferedWriter(new FileWriter(dtmFileVocab));
		BufferedWriter docsWriter = new BufferedWriter(new FileWriter(dtmFileDocs));
		List<Integer> periods = new ArrayList<Integer>();
		int currentYear = 0;
		int docsPerPeriod = 0;
		
		
		Iterator<BibtexEntry> iDocs = entries.iterator();
		while (iDocs.hasNext()) {
			BibtexEntry entry = iDocs.next();
			int currentDocYear = Integer.parseInt(entry.getField("year"));
			
			// If year changes, write out how many documents there were in that period
			if (currentYear != currentDocYear) {
				if (currentYear != 0) {
					periods.add(docsPerPeriod);
				}
				docsPerPeriod = 0;
				currentYear = currentDocYear;
			}
			docsPerPeriod++;

			// Write terms of the document
			Map<String, Integer> entryTerms = corpus.get(entry);
			corpusWriter.write(Integer.toString(entryTerms.size()));
			Iterator<String> iWords = entryTerms.keySet().iterator();
			while (iWords.hasNext()) {
				String word = iWords.next();
				corpusWriter.write(" " + terms.get(word) + ":" + entryTerms.get(word));
			}
			corpusWriter.write('\n');
			
			// Write document date
			docsWriter.write(entry.getCiteKey());
			docsWriter.write('\n');
		}
		// Add the last period
		periods.add(docsPerPeriod);
		
		// Write off periods
		seqWriter.write(Integer.toString(periods.size()));
		seqWriter.write('\n');
		Iterator<Integer> iPeriods = periods.iterator();
		while (iPeriods.hasNext()) {
			docsPerPeriod = iPeriods.next();
			seqWriter.write(Integer.toString(docsPerPeriod));
			seqWriter.write('\n');
		}
		
		// Write vocabulary
		Iterator<String> iTerms = terms.keySet().iterator();
		while (iTerms.hasNext()) {
			String s = iTerms.next();
			termsWriter.write(s);
			termsWriter.write('\n');
		}
			
		corpusWriter.close();
		seqWriter.close();
		termsWriter.close();
		docsWriter.close();
	}
	
	public void read() throws IOException
	{
		int id = 1;
		SnowballStemmer stemmer = null;
		ParserResult result;
		Analyzer analyzer;
		StopWordFilter spwFilter;
		
		// Prepare stemmer
		try {
			Class<?> stemClass = Class.forName("org.tartarus.snowball.ext." + "porter" + "Stemmer");
			// stemmer = (SnowballStemmer) stemClass.newInstance();	
		} catch (Exception e) {}
		
		// Prepare Lucene (with stopwords)
		spwFilter = new StopWordFilter();
		// spwFilter.setStemmer(stemmer);
		spwFilter.loadDefaultStopwords();
		analyzer = new StandardAnalyzer(Version.LUCENE_42, spwFilter.getStopWordListAsSet());
		
		// Read data and sort entries by date (1st) and title (2nd)
		result = BibtexParser.parse(new InputStreamReader(input));
		entries = new ArrayList<BibtexEntry>(result.getDatabase().getEntries());
		Collections.sort(entries, new BibTexEntryComparator());
		
		// Process files
		Iterator<BibtexEntry> iterator= entries.iterator();
		while (iterator.hasNext()) {
			BibtexEntry entry = iterator.next();
			Map<String, Integer> entryTerms = new LinkedHashMap<String, Integer>();
				
			corpus.put(entry, entryTerms);
			for (String field : fieldsToImport) {
				String value = entry.getField(field);
				if (value != null && ! value.trim().isEmpty()) {
					TokenStream stream = analyzer.tokenStream("contents", new StringReader(value));
					CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);

					stream.reset();
					while (stream.incrementToken()){
						// Get word and process it (stopword and stemmize)
						String word = new String(term.buffer(), 0, term.length());
						if (stemmer != null) {
							stemmer.setCurrent(word);
							stemmer.stem();
							word = stemmer.getCurrent();
						}
							
						// Account for word in the document
						if (entryTerms.containsKey(word)) {
							entryTerms.put(word, entryTerms.get(word) + 1);
						} else {
							entryTerms.put(word, 1);
						}
						
						// Account for word in the collection, giving them an unique Id
						if (! terms.containsKey(word)) {
							terms.put(word, id);
							id++;
						}
					}
				}
			}
		}
		
		analyzer.close();

	}
	
	public static void main(String[] args) throws IOException {
		BibTeX2DTM b = new BibTeX2DTM();
		InputStream is = BibTeX2DTM.class.getResourceAsStream("/SBSC.bib");
		b.setInputStream(is, new File("/tmp"), "SBSC");
		b.read();
		b.write();
	}
}
