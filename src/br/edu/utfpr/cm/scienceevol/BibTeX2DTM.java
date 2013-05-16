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

import com.ironiacorp.datastructure.array.ArrayUtil;


import lode.miner.TypedResourceConsumerStub;
import lode.miner.extraction.txt.TextStreamTokenizer;
import lode.miner.extraction.txt.UnformattedPlainTextStreamTokenizer;
import lode.model.text.TextResource;
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
	
	private List<InputStream> inputs;

	private OutputStream dtmMain;
	
	private OutputStream dtmAux;
	
	private OutputStream dtmVocab;
	
	private OutputStream dtmDocs;

	private String[] fieldsToImport = { "title", "abstract", "keywords" };
	
	private String[] fieldsToImportAsNLP = { "title", "abstract" };

	
	private List<BibtexEntry> entries;

	private Map<BibtexEntry, Map<String, Integer>> corpus;
	
	private Map<String, Integer> terms;
	
	private List<Integer> periods;
	
	private boolean useStopwords = true;
	
	private boolean useStemmer = true;
	
	private boolean useLUCut = true;
	
	private File basedir;
	
	private String corpusName;
	
	public BibTeX2DTM()
	{
		terms = new LinkedHashMap<String, Integer>();
		corpus = new HashMap<BibtexEntry, Map<String, Integer>>();
		inputs = new ArrayList<InputStream>();
		entries = new ArrayList<BibtexEntry>();
	}
	
	public Corpus getCorpus()
	{
		Corpus corpus = new Corpus();
		corpus.setTermsCount(terms.size());
		corpus.setDocumentsCount(entries.size());
		corpus.setYearsCount(periods.size());
		corpus.setBasedir(basedir);
		corpus.setName(corpusName);
		
		return corpus;
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



	public void setDefaultOutputStreams(File baseDir, String corpusName)
	{
		File dtmFileMain = new File(baseDir, corpusName + DTM.DTM_CORPUS_PREFIX + DTM.DTM_EXTENSION);
		File dtmFileAux = new File(baseDir, corpusName + DTM.DTM_SEQUENCE_PREFIX + DTM.DTM_EXTENSION);
		File dtmFileVocab = new File(baseDir, corpusName + DTM.DTM_TERMS_PREFIX + DTM.DTM_EXTENSION);
		File dtmFileDocs = new File(baseDir, corpusName + DTM.DTM_DOCS_PREFIX + DTM.DTM_EXTENSION);
		this.basedir = baseDir;
		this.corpusName = corpusName;
		
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
		int currentYear = 0;
		int docsPerPeriod = 0;
		
		
		periods = new ArrayList<Integer>();
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

		NLPTextPreprocessor nlpPreprocessor = new NLPTextPreprocessor();
		TextStreamTokenizer parser = new UnformattedPlainTextStreamTokenizer();
		TextPipelinePreprocessor preprocessor = new TextPipelinePreprocessor();

		preprocessor.setUseStemmer(useStemmer);
		preprocessor.setUseStopwords(useStopwords);
		preprocessor.setLUCut(useLUCut);

		
		
        // Process files
		Iterator<BibtexEntry> iterator= entries.iterator();
		while (iterator.hasNext()) {
			BibtexEntry entry = iterator.next();
			Map<String, Integer> entryTerms = new LinkedHashMap<String, Integer>();
			DTMDocumentProcessorComponent documentProcessor = new DTMDocumentProcessorComponent(terms, entryTerms);
			corpus.put(entry, entryTerms);
			for (String field : fieldsToImport) {
				String value = entry.getField(field);
				if (value != null && ! value.trim().isEmpty()) {
					if (ArrayUtil.has(fieldsToImportAsNLP, field)) {
				        TypedResourceConsumerStub<TextResource> consumer = new TypedResourceConsumerStub<>(TextResource.class);
						TextResource textResource = new UnformattedTextResource();
						StringBuilder sb = new StringBuilder();
						textResource.setText(value);
						nlpPreprocessor.setConsumer(consumer);
						nlpPreprocessor.consume(textResource);
						String[] words = consumer.getWords();
						for (String word : words) {
							sb.append(word);
							sb.append(" ");
						}
						value = sb.toString();
					}
					
					parser.setReader(new StringReader(value));
					parser.setConsumer(preprocessor.getStart());
					preprocessor.getEnd().setConsumer(documentProcessor);
					parser.start();
					parser.stop();
				}
			}
		}
	}
}
