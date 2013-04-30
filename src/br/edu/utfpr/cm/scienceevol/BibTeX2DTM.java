package br.edu.utfpr.cm.scienceevol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.MultiHashMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.SnowballStemmer;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.imports.BibtexParser;
import net.sf.jabref.imports.ParserResult;

public class BibTeX2DTM
{
	private static final String BIB_EXTENSION = ".bib";
	
	private static final String DTM_EXTENSION = ".dat";
	
	private HashMap<String, BibtexEntry> cache;
	
	private String baseDir;

	private String name;

	private File bibtexFile;

	private File dtmFileMain;

	private File dtmFileAux;
	
	private File dtmFileVocab;

	private String[] fieldsToImport = { "title", "abstract", "keywords" };

	private MultiHashMap docs;
	
	private Map<String, Integer> terms;

	private Map<String, String> ids;
	
	
	private int i = 0;

	
	public BibTeX2DTM()
	{
		cache = new HashMap<String, BibtexEntry>();
		docs = new MultiHashMap();
		terms = new LinkedHashMap<String, Integer>();
		ids = new HashMap<String, String>();
	}
	
	public void setBibtexFile(File file)
	{
		if (file == null || !file.exists()) {
			throw new IllegalArgumentException("Invalid BibTeX file");
		}

		baseDir = file.getParent();
		name = file.getName();
		name = name.substring(0, name.lastIndexOf("."));

		this.bibtexFile = new File(baseDir, name + BIB_EXTENSION);
		this.dtmFileMain = new File(baseDir, name + "_mult" + DTM_EXTENSION);
		this.dtmFileAux = new File(baseDir, name + "_seq" + DTM_EXTENSION);
		this.dtmFileVocab = new File(baseDir, name + "_vocab" + DTM_EXTENSION);
	}
	

	public File getBibtexFile()
	{
		return bibtexFile;
	}

	
	private BibtexEntry findInCache(BibtexEntry bibtexEntry)
	{
		String title = bibtexEntry.getField("title").toLowerCase();
		String doi = bibtexEntry.getField("doi");
		String key = title;
		if (doi != null) {
			key += doi.toLowerCase();
		}

		if (cache.containsKey(key)) {
			bibtexEntry = cache.get(key);
		} else {
			bibtexEntry.setField("pexid", Integer.toString(i++));
			cache.put(key, bibtexEntry);
		}
	
		return bibtexEntry;
	}

	public void convert() throws IOException
	{
		int id = 0;
		BufferedWriter dtmMainFile = new BufferedWriter(new FileWriter(dtmFileMain));
		BufferedWriter dtmAuxFile = new BufferedWriter(new FileWriter(dtmFileAux));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42, ParserUtils.getStopWordListAsSet());
		ParserResult result = BibtexParser.parse(new FileReader(bibtexFile));
		Collection<BibtexEntry> entries = result.getDatabase().getEntries();
		Iterator<BibtexEntry> iterator = entries.iterator();
		SnowballStemmer stemmer = null;
		
		try {
			Class stemClass = Class.forName("org.tartarus.snowball.ext." + "porter" + "Stemmer");
			stemmer = (SnowballStemmer) stemClass.newInstance();	
		} catch (Exception e) {}
		
		while (iterator.hasNext()) {
			BibtexEntry bibtexEntry = iterator.next();
			bibtexEntry = findInCache(bibtexEntry);

			StringWriter writer = new StringWriter();
			BufferedWriter buffer = new BufferedWriter(writer);
			
			for (String field : fieldsToImport) {
				String value = bibtexEntry.getField(field);
				if (value != null && ! value.trim().isEmpty()) {
					TokenStream stream = analyzer.tokenStream("contents", new StringReader(value));
					CharTermAttribute term = stream.addAttribute(CharTermAttribute.class);
					PositionIncrementAttribute posInc = stream.addAttribute(PositionIncrementAttribute.class);
					OffsetAttribute offset = stream.addAttribute(OffsetAttribute.class);
					TypeAttribute type = stream.addAttribute(TypeAttribute.class);
					
					int position = 0;
					stream.reset();
					while (stream.incrementToken()){
						int increment = posInc.getPositionIncrement();
						if (increment > 0) {
							position = position + increment;
						}
						
						String word = new String(term.buffer(), 0, term.length());
						stemmer.setCurrent(word);
						stemmer.stem();
						word = stemmer.getCurrent();
						System.out.println(word);
						docs.put(bibtexEntry, word);
						if (terms.containsKey(word)) {
							terms.put(word, terms.get(word) + 1);
						} else {
							terms.put(word, 1);
						}
						if (! ids.containsKey(word)) {
							ids.put(word, Integer.toString(id));
							id++;
						}
					}
				}
			}
		}
		/*
		BufferedWriter bwMain = new BufferedWriter(new FileWriter(this.dtmFileMain));
		BufferedWriter bwAux = new BufferedWriter(new FileWriter(this.dtmFileAux));
		
		try {
			bwAux.write("9\n");
			for (int i = 2004; i < 2013; i++) {
				int j = 0;
				Iterator<BibtexEntry> iDocs = docs.keySet().iterator();
				while (iDocs.hasNext()) {
					BibtexEntry entry = iDocs.next();
					if (entry.getField("year").equals(Integer.toString(i))) {
						j++;
						//System.out.print(docs.getCollection(entry).size());
						bwMain.write(Integer.toString(docs.getCollection(entry).size()));
						Iterator<String> iWords = docs.iterator(entry);
						while (iWords.hasNext()) {
							String word2 = iWords.next();
							//System.out.print(" " + ids.get(word2) + ":" + terms.get(word2));
							bwMain.write(" " + ids.get(word2) + ":" + terms.get(word2));
						}
						bwMain.write("\n");
						//System.out.println();
					}
				}
				bwAux.write(j + "\n");
			}
			
			BufferedWriter bwVocab = new BufferedWriter(new FileWriter(this.dtmFileVocab));
			
			Iterator<String> iTerms = terms.keySet().iterator();
			while (iTerms.hasNext()) {
				String s = iTerms.next();
				bwVocab.write(s + "\n");
				//System.out.print(s + "\n");
			}
			
			dtmMainFile.flush();
			dtmMainFile.close();
			bwMain.close();
			bwAux.close();
			bwVocab.close();
		} catch (IOException e) {
		}
		*/
	}
	
	public static void main(String[] args) throws IOException {
		BibTeX2DTM b = new BibTeX2DTM();
		b.setBibtexFile(new File("/Users/filiperoseirocogo/Dropbox/Anais SBSC/sci2/SBSC.bib"));
		b.convert();
	}
}
