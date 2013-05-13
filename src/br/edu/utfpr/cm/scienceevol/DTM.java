package br.edu.utfpr.cm.scienceevol;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ironiacorp.computer.ComputerArchitecture;
import com.ironiacorp.computer.ComputerArchitectureDetector;
import com.ironiacorp.computer.ComputerSystem;
import com.ironiacorp.computer.OperationalSystem;

public class DTM implements Analyzer
{
	public enum ModelType {
		DTM("dtm"),
		DIM("dim"),
		FIXED("fixed");
		
		String name;
		
		private ModelType(String name) {
			this. name = name;
		}

	}
	
	public static final String DTM_OPTION_MODEL = "--model=";

	
	public static final String DEFAULT_EXEC_FILENAME = "dtm";
	
	private ComputerArchitecture arch;
	
	private OperationalSystem os;
	
	private String dtmExecFilename;
	
	private String corpusPrefix;
	
	public static final String DTM_OPTION_CORPUS_PREFIX = "--corpus_prefix=";
	
	private String resultsPrefix;
	
	public static final String DTM_OPTION_RESULTS_PREFIX = "--outname=";

	/**
	 * Number of topics to be considered.
	 */
	private int topics;
	
	public static final String DTM_OPTION_NTOPICS = "--ntopics=";
	
	/**
	 * Number of years that each period under analysis corresponds to.
	 */
	private double yearsPerPeriod;
	
	public static final String DTM_OPTION_YEARS_PERIOD = "--time_resolution=";
	
	/**
	 * Amount of years after which a paper is (usually) cited to.
	 */
	private double paperCitedAfterYears;
	
	public static final String DTM_OPTION_INFLUENCE_MEAN = "--influence_mean_years=";
	
	/**
	 * Standard deviation of amount of years after which a paper is (usually) cited to.
	 */
	private double paperCitedAfterYearsStdDev;


	public static final String DTM_DOCS_PREFIX = "-docs";


	public static final String DTM_TERMS_PREFIX = "-vocab";


	public static final String DTM_SEQUENCE_PREFIX = "-seq";


	public static final String DTM_CORPUS_PREFIX = "-mult";


	public static final String DTM_EXTENSION = ".dat";

	public static final String DTM_OPTION_INFLUENCE_STDDEV = "--influence_stdev_years=";
	
	public static final String[] DEFAULT_OPTIONS = {
		"--save_time=-1",
		"--alpha=0.01",
		"--mode=fit",
		"--rng_seed=0",
		"--initialize_lda=true",
		"--influence_flat_years=-1",
		"--fix_topics=0",
		"--sigma_c=0.0001",
		"--sigma_d=0.0001",
		"--sigma_l=0.0001",
		"--lda_max_em_iter=30",
		"--lda_sequence_min_iter=10",
		"--lda_sequence_max_iter=30",
		"--top_obs_var=0.5",
		"--top_chain_var=0.005"
	};
	
	public DTM()
	{
		ComputerArchitectureDetector archDetector = new ComputerArchitectureDetector();
		arch = archDetector.detectCurrentArchitecture();
		os = ComputerSystem.getCurrentOperationalSystem();
		
		dtmExecFilename = DEFAULT_EXEC_FILENAME + "-" + os.getNickname(arch);
	}

	public String getCorpusPrefix() {
		return corpusPrefix;
	}

	public void setCorpusPrefix(String corpusPrefix) {
		this.corpusPrefix = corpusPrefix;
	}

	public String getResultsPrefix() {
		return resultsPrefix;
	}

	public void setResultsPrefix(String resultsPrefix) {
		this.resultsPrefix = resultsPrefix;
	}
	
	
	
	public String getDtmExecFilename() {
		return dtmExecFilename;
	}

	public void setDtmExecFilename(String dtmExecFilename) {
		this.dtmExecFilename = dtmExecFilename;
	}

	public void suggestSearchPath(File path) {
		os.addExecutableSearchPath(path);
	}
	
	public int getTopics() {
		return topics;
	}

	public void setTopics(int topics) {
		this.topics = topics;
	}

	public double getYearsPerPeriod() {
		return yearsPerPeriod;
	}

	public void setYearsPerPeriod(double yearsPerPeriod) {
		this.yearsPerPeriod = yearsPerPeriod;
	}

	public double getPaperCitedAfterYears() {
		return paperCitedAfterYears;
	}

	public void setPaperCitedAfterYears(double paperCitedAfterYears) {
		this.paperCitedAfterYears = paperCitedAfterYears;
	}

	public double getPaperCitedAfterYearsStdDev() {
		return paperCitedAfterYearsStdDev;
	}

	public void setPaperCitedAfterYearsStdDev(double paperCitedAfterYearsStdDev) {
		this.paperCitedAfterYearsStdDev = paperCitedAfterYearsStdDev;
	}

	public TopicModel runFit()
	{
		File executable = os.findExecutable(dtmExecFilename);
		List<String> parameters = new ArrayList<String>();
		TopicModel model = null;
		
		if (executable == null) {
			throw new IllegalArgumentException("Could not find DTM executable");
		}
		
		for (String defaults : DEFAULT_OPTIONS) {
			parameters.add(defaults);
		}
		
		parameters.add(DTM_OPTION_MODEL + ModelType.DTM.name);
		parameters.add(DTM_OPTION_NTOPICS + topics);
		parameters.add(DTM_OPTION_INFLUENCE_MEAN + paperCitedAfterYears);
		parameters.add(DTM_OPTION_INFLUENCE_STDDEV + paperCitedAfterYearsStdDev);
		parameters.add(DTM_OPTION_YEARS_PERIOD + yearsPerPeriod);
		parameters.add(DTM_OPTION_CORPUS_PREFIX + corpusPrefix);
		parameters.add(DTM_OPTION_RESULTS_PREFIX + resultsPrefix + File.separator + ModelType.DTM.name);
		ProcessBuilder pb = os.exec(executable, parameters);
		pb.inheritIO();
		try {
			Process process = pb.start();
			process.waitFor();
			model = new TopicModel();
			model.setBasedir(resultsPrefix + File.separator + ModelType.DTM.name);
			model.setTopicsCount(topics);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		
		return model;
	}
	
	public TopicModel runInfluence()
	{
		File executable = os.findExecutable(dtmExecFilename);
		List<String> parameters = new ArrayList<String>();
		TopicModel model = null;

		if (executable == null) {
			throw new IllegalArgumentException("Could not find DTM executable");
		}
		
		for (String defaults : DEFAULT_OPTIONS) {
			parameters.add(defaults);
		}
		
		parameters.add(DTM_OPTION_MODEL + ModelType.FIXED.name);
		parameters.add(DTM_OPTION_NTOPICS + topics);
		parameters.add(DTM_OPTION_INFLUENCE_MEAN + paperCitedAfterYears);
		parameters.add(DTM_OPTION_INFLUENCE_STDDEV + paperCitedAfterYearsStdDev);
		parameters.add(DTM_OPTION_YEARS_PERIOD + yearsPerPeriod);
		parameters.add(DTM_OPTION_CORPUS_PREFIX + corpusPrefix);
		parameters.add(DTM_OPTION_RESULTS_PREFIX + resultsPrefix + File.separator + ModelType.FIXED.name);
		ProcessBuilder pb = os.exec(executable, parameters);
		pb.inheritIO();
		try {
			Process process = pb.start();
			process.waitFor();
			model = new TopicModel();
			model.setBasedir(resultsPrefix + File.separator + ModelType.FIXED.name);
			model.setTopicsCount(topics);
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		
		return model;
	}
}
