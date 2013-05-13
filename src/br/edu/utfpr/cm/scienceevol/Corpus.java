package br.edu.utfpr.cm.scienceevol;

import java.io.File;

public class Corpus
{
	private String name;
	
	private File basedir;
	
	private int documentsCount;
	
	private int termsCount;
	
	private int yearsCount;

	public int getDocumentsCount() {
		return documentsCount;
	}

	public void setDocumentsCount(int documentsCount) {
		this.documentsCount = documentsCount;
	}

	public int getTermsCount() {
		return termsCount;
	}

	public void setTermsCount(int termsCount) {
		this.termsCount = termsCount;
	}

	public int getYearsCount() {
		return yearsCount;
	}

	public void setYearsCount(int yearsCount) {
		this.yearsCount = yearsCount;
	}

	public File getBasedir() {
		return basedir;
	}

	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}

	public void setName(String corpusName) {
		this.name = corpusName;
	}

	public String getName() {
		return name;
	}
	
}
