package br.edu.utfpr.cm.scienceevol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lode.miner.BufferComponent;
import lode.miner.extraction.TypedResourceConsumerStub;
import lode.miner.extraction.txt.TextStreamTokenizer;
import lode.miner.extraction.txt.UnformattedPlainTextStreamTokenizer;
import lode.model.text.UnformattedTextResource;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexEntryType;
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
				if (title1 == null || title2 == null) {
					if (title1 == null && title2 == null) {
						return entry1.getCiteKey().compareTo(entry2.getCiteKey());
					}
					if (title1 == null) {
						return -1;
					} else {
						return 1;
					}
				} else {
					return title1.compareTo(title2);
				}
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
	
	private List<InputStream> inputs;

	private OutputStream dtmMain;
	
	private OutputStream dtmAux;
	
	private OutputStream dtmVocab;
	
	private OutputStream dtmDocs;

	private String[] fieldsToImport = { "title", "abstract", "keywords" };
	
	private List<BibtexEntry> entries;

	private Map<BibtexEntry, Map<String, Integer>> corpus;
	
	private Map<String, Integer> terms;
	
	private boolean useStopwords = true;
	
	private boolean useStemmer = true;
	
	private boolean useLUCut = true;
	
	public BibTeX2DTM()
	{
		terms = new LinkedHashMap<String, Integer>();
		corpus = new HashMap<BibtexEntry, Map<String, Integer>>();
		inputs = new ArrayList<InputStream>();
		entries = new ArrayList<BibtexEntry>();
	}
	
	
	
	public void addInputStream(InputStream is) {
		inputs.add(is);
	}

	

	public OutputStream getDtmMain() {
		return dtmMain;
	}

	public void setDtmMain(OutputStream dtmMain) {
		this.dtmMain = dtmMain;
	}

	public OutputStream getDtmAux() {
		return dtmAux;
	}

	public void setDtmAux(OutputStream dtmAux) {
		this.dtmAux = dtmAux;
	}

	public OutputStream getDtmVocab() {
		return dtmVocab;
	}

	public void setDtmVocab(OutputStream dtmVocab) {
		this.dtmVocab = dtmVocab;
	}

	public OutputStream getDtmDocs() {
		return dtmDocs;
	}

	public void setDtmDocs(OutputStream dtmDocs) {
		this.dtmDocs = dtmDocs;
	}

	public boolean isUseStopwords() {
		return useStopwords;
	}



	public void setUseStopwords(boolean useStopwords) {
		this.useStopwords = useStopwords;
	}



	public boolean isUseStemmer() {
		return useStemmer;
	}

	public void setUseStemmer(boolean useStemmer) {
		this.useStemmer = useStemmer;
	}
	

	public boolean isLUCut() {
		return useLUCut;
	}

	public void setLUCut(boolean useLUCut) {
		this.useLUCut = useLUCut;
	}



	public void setDefaultOutputStreams(File baseDir, String prefixName)
	{
		File dtmFileMain = new File(baseDir, prefixName + DTM_CORPUS_PREFIX + DTM_EXTENSION);
		File dtmFileAux = new File(baseDir, prefixName + DTM_SEQUENCE_PREFIX + DTM_EXTENSION);
		File dtmFileVocab = new File(baseDir, prefixName + DTM_TERMS_PREFIX + DTM_EXTENSION);
		File dtmFileDocs = new File(baseDir, prefixName + DTM_DOCS_PREFIX + DTM_EXTENSION);
		
		try {
			dtmMain = new FileOutputStream(dtmFileMain);
			dtmAux = new FileOutputStream(dtmFileAux);
			dtmVocab = new FileOutputStream(dtmFileVocab);
			dtmDocs = new FileOutputStream(dtmFileDocs);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}
	

	public void write() throws IOException
	{
		BufferedWriter corpusWriter = new BufferedWriter(new OutputStreamWriter(dtmMain));
		BufferedWriter seqWriter = new BufferedWriter(new OutputStreamWriter(dtmAux));
		BufferedWriter termsWriter = new BufferedWriter(new OutputStreamWriter(dtmVocab));
		BufferedWriter docsWriter = new BufferedWriter(new OutputStreamWriter(dtmDocs));
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
			if (iDocs.hasNext()) {
				corpusWriter.write('\n');
			}
			
			// Write document date
			docsWriter.write(entry.getCiteKey());
			if (iDocs.hasNext()) {
				docsWriter.write('\n');
			}
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
			if (iPeriods.hasNext()) {
				seqWriter.write('\n');
			}
		}
		
		// Write vocabulary
		Iterator<String> iTerms = terms.keySet().iterator();
		while (iTerms.hasNext()) {
			String s = iTerms.next();
			termsWriter.write(s);
			if (iTerms.hasNext()) {
				termsWriter.write('\n');
			}
		}
			
		corpusWriter.flush();
		corpusWriter.close();
		seqWriter.flush();
		seqWriter.close();
		termsWriter.flush();
		termsWriter.close();
		docsWriter.flush();
		docsWriter.close();
	}
	
	public void read() throws IOException
	{
		int id = 1;

		// Read data and sort entries by date (1st) and title (2nd)
		for (InputStream is : inputs) {
			ParserResult result = BibtexParser.parse(new InputStreamReader(is));
			Collection<BibtexEntry> currentEntries = result.getDatabase().getEntries();
			Iterator<BibtexEntry> iterator= currentEntries.iterator();
			while (iterator.hasNext()) {
				BibtexEntry entry = iterator.next();
				if (entry.getType().equals(BibtexEntryType.INPROCEEDINGS)) {
					entries.add(entry);
				}
			}
		}
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
					TextStreamTokenizer parser = new UnformattedPlainTextStreamTokenizer();
					TypedResourceConsumerStub<UnformattedTextResource> consumer = new TypedResourceConsumerStub<UnformattedTextResource>(UnformattedTextResource.class);
					PipelinePreprocessor preprocessor = new PipelinePreprocessor();
					
					BufferComponent buffer = new BufferComponent();
					
					preprocessor.setUseStemmer(useStemmer);
					preprocessor.setUseStopwords(useStopwords);
					preprocessor.setLUCut(useLUCut);
					parser.setConsumer(consumer);
					parser.setReader(new StringReader(value));
					parser.setConsumer(buffer);
					buffer.setConsumer(preprocessor.getStart());
					preprocessor.getEnd().setConsumer(consumer);
					parser.start();
					parser.stop();
					String[] words = consumer.getWords();
					for (String word : words) {
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
					parser.reset();
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		BibTeX2DTM b = new BibTeX2DTM();
		String files[] = {
				"SBSC-2004.bib",
				"SBSC-2005.bib",
				"SBSC-2006.bib",
				"SBSC-2007.bib",
				"SBSC-2008.bib",
				"SBSC-2009.bib",
				"SBSC-2010.bib",
				"SBSC-2011.bib",
				"SBSC-2012.bib",
		};
		b.setDefaultOutputStreams(new File("/tmp"), "SBSC");
		for (String file : files) {
			InputStream is = BibTeX2DTM.class.getResourceAsStream("/" + file);
			b.addInputStream(is);
		}
		b.read();
		b.write();
	}
}
