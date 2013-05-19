package br.edu.utfpr.cm.scienceevol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ScienceEvolution
{
	private int topics;
	
	private Corpus corpus;
	
	private TopicModel dtmModel;
	
	private TopicModel influenceModel;
	
	private List<InputStream> inputs;
	
	private File basedir;
	
	private String corpusName;
	
	public ScienceEvolution() 
	{
		inputs = new ArrayList<InputStream>();
	}
	
	public int getTopics() {
		return topics;
	}

	public void setTopics(int topics) {
		this.topics = topics;
	}

	public File getBasedir() {
		return basedir;
	}

	
	public String getCorpusName() {
		return corpusName;
	}

	public void setCorpusName(String corpusName) {
		this.corpusName = corpusName;
	}

	public void setBasedir(File basedir) {
		if (basedir.exists()) {
			if (! basedir.isDirectory()) {
				throw new IllegalArgumentException("It exists, but it is not a directory: " + basedir.getName());
			}
		} else {
			if (! basedir.mkdirs()) {
				throw new IllegalArgumentException("Could not create directory: " + basedir.getName());
			}
		}
		
		this.basedir = basedir;
	}

	public void addInput(InputStream is)
	{
		inputs.add(is);
	}
	

	public void run() throws IOException
	{
		BibTeX2DTM bib2dtm = new BibTeX2DTM();
		
		basedir.mkdirs();
		bib2dtm.setDefaultOutputStreams(basedir, corpusName);
		for (InputStream is : inputs) {
			bib2dtm.addInputStream(is);
		}
		bib2dtm.read();
		bib2dtm.write();
		
		corpus = bib2dtm.getCorpus();
			
		DTM dtm = new DTM();
		dtm.suggestSearchPath(new File("/home/magsilva/Dropbox/Papers/10thSBSC/resources/dtm"));
		// TODO: adicionar caminho para o diretório com o executável com o DTM no computador de vocês
		dtm.suggestSearchPath(new File("/home/magsilva/Dropbox/Papers/10thSBSC/resources/dtm"));
		dtm.setPaperCitedAfterYears(1.0);
		dtm.setPaperCitedAfterYearsStdDev(2.0);
		dtm.setTopics(topics);
		dtm.setYearsPerPeriod(1);
		dtm.setAlpha(0.5);
		dtm.setMinIterations(5);
		dtm.setMaxIterations(100);
		dtm.setCorpusPrefix(basedir.getAbsolutePath() + File.separator + corpusName);
		dtm.setResultsPrefix(basedir.getAbsolutePath() + File.separator);
		dtmModel = dtm.runFit();
		influenceModel = dtm.runInfluence();
	}
	
	public void visualize(int topTermsPerTopic) throws IOException
	{
		Util util = new Util(corpus, dtmModel);
		util.update();
		util.orderResultsPerYearPerTopic();
		util.printXBetterResultsPerYearPerTopic(topTermsPerTopic);
		util.printXBetterResultsPerYearPerTopicInFile(0.01);
	}
	
	public static void main(String[] args) throws Exception {
		ScienceEvolution evolution = new ScienceEvolution();
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
	
		evolution.setBasedir(new File("/tmp/SBSC"));
		evolution.setCorpusName("SBSC");
		evolution.setTopics(15);
		for (String file : files) {
			InputStream is = BibTeX2DTM.class.getResourceAsStream("/" + file);
			evolution.addInput(is);
		}
		evolution.run();
		evolution.visualize(20);
	}
}
