package br.edu.utfpr.cm.scienceevol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Util {

	private String[] listaTermos;

	private Corpus corpus;
	
	private TopicModel model;
	
	protected Dupla<Integer, Double>[][][] cube;
	
	public Util(Corpus corpus, TopicModel model) 
	{
		this.corpus = corpus;
		this.model = model;
	}
	
	public void update()
	{
		try {
			listaTermos = leiaTermos();
			gimmeCubao();
			orderResultsPerYearPerTopic();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not load required data (terms)", e);
		}
	}

	protected String[] leiaTermos() throws Exception
	{
		System.out.println("Lendo termos de: " + corpus.getBasedir() + File.separator + corpus.getName() + DTM.DTM_TERMS_PREFIX + DTM.DTM_EXTENSION);
		File file = new File(corpus.getBasedir() + File.separator + corpus.getName() + DTM.DTM_TERMS_PREFIX + DTM.DTM_EXTENSION);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String nextLine;
		String[] retorno = new String[corpus.getTermsCount()];
		for (int i = 0; (nextLine = reader.readLine()) != null; i++) {
			retorno[i] = nextLine;
		}
		reader.close();
		return retorno;
	}

	protected void gimmeCubao() throws Exception
	{
		cube = (Dupla<Integer, Double>[][][]) new Dupla<?, ?>[corpus.getYearsCount()][model.getTopicsCount()][corpus.getTermsCount()];
		for (int topic = 0; topic < model.getTopicsCount(); topic++) {
			String filename = String.format("%stopic-%03d-var-e-log-prob.dat",
					model.getBasedir() + File.separator + "lda-seq" + File.separator, topic);
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String text = null;
			
			int term = 0;
			do {
				for (int year = 0; year < corpus.getYearsCount(); year++) {
					text = reader.readLine();
					if (text == null || text.length() == 0) {
						reader.close();
						throw new IllegalArgumentException("Invalid input data");
					}
					double probabibility = Math.exp(Double.parseDouble(text));
					cube[year][topic][term] = new Dupla<Integer, Double>(term, probabibility);
				}
				term++;
			} while (term < corpus.getTermsCount());
<<<<<<< HEAD
=======
			
>>>>>>> Chage in Util.java in order to adjust the ordering of probabilities of terms in topics.
			reader.close();
		}
	}


<<<<<<< HEAD
	public void printXBetterResultsPerYearPerTopicInFile(double probability) throws IOException {
		FileWriter fw = new FileWriter(new File(
				corpus.getBasedir() + File.separator +
				DTM.ModelType.DTM.name + File.separator +
				"lda-seq" + File.separator +
				"Year-Topic-Term-Prob.csv"));
		fw.write("Year;Topic;Term;Probability\n");
		for (int ano = 0; ano < corpus.getYearsCount(); ano++) {
			for (int topico = 0; topico < model.getTopicsCount(); topico++) {
				for (int term = 0; cube[ano][topico][term].getY() > probability && term < (corpus.getTermsCount() - 1); term++) {
=======
	
	public void printXBetterResultsPerYearPerTopicInFile(int qtde) throws Exception {
		update();
		FileWriter fw = new FileWriter(new File("/Users/filiperoseirocogo/Documents/workspace/Cubao/Year-Topic-Term-Prob.csv"));
		//FileWriter fw = new FileWriter(new File(
		//	corpus.getBasedir() + File.separator +
		//		DTM.ModelType.DTM + File.separator +
		//		"lda-seq" + File.separator +
		//		"Year-Topic-Term-Prob.csv"));
		fw.write("Year;Topic;Term;Probability\n");
		for (int ano = 0; ano < corpus.getYearsCount(); ano++) {
			for (int topico = 0; topico < model.getTopicsCount(); topico++) {
				for (int i = 0; i < qtde; i++){
					//System.out.println(i);
					//System.out.println(cube[ano][topico][i]);
					System.out.println(ano + 
							";" + 
							topico + 
							";" + 
							listaTermos[cube[ano][topico][i].getX()] + 
							";" + 
							cube[ano][topico][i].getY() +
							"\n");
>>>>>>> Chage in Util.java in order to adjust the ordering of probabilities of terms in topics.
					fw.write(ano + 
							";" + 
							topico + 
							";" + 
							listaTermos[cube[ano][topico][term].getX()] + 
							";" + 
							cube[ano][topico][term].getY() +
							"\n");
				}
			}
		}
		fw.close();
	}
<<<<<<< HEAD
=======
	
>>>>>>> Chage in Util.java in order to adjust the ordering of probabilities of terms in topics.
	
	public void printXBetterResultsPerYearPerTopic(int qtde) {
		for (int ano = 0; ano < corpus.getYearsCount(); ano++) {
			System.out.println("\n=========================ANO " + ano + " ==============================\n");
			for (int topico = 0; topico < model.getTopicsCount(); topico++) {
				System.out.println("\n---TOPICO " + topico + " --- ANO: " + ano 	+ " ---\n");
				for (int i = 0; i < qtde; i++) {
					if (cube[ano][topico][i].getY() >= 0.01) {
						System.out.println(listaTermos[cube[ano][topico][i].getX()] + ";" + cube[ano][topico][i].getY());
					}
				}
			}
		}

	}


	public void orderResultsPerYearPerTopic() {
		for (int ano = 0; ano < corpus.getYearsCount(); ano++) {
			for (int topico = 0; topico < model.getTopicsCount(); topico++) {
				quickSort(cube[ano][topico], 0, corpus.getTermsCount() - 1);
			}
		}
	}

	public void quickSort(Dupla<Integer, Double>[] v, int inicio, int fim) {
		if (inicio < fim) {
			int pivo = inicio, i = fim;
			Dupla<Integer, Double> vPivo = v[pivo];
			while (pivo < i) {
				if (vPivo.getY() < v[i].getY()) {
					v[pivo] = v[i];
					pivo = i;
					i = inicio + 1;
					while (pivo > i) {
						if (vPivo.getY() > v[i].getY()) {
							v[pivo] = v[i];
							pivo = i;
							i = fim;
							break;
						} else
							i++;
					}
				} else
					i--;
			}
			v[pivo] = vPivo;
			quickSort(v, inicio, pivo - 1);
			quickSort(v, pivo + 1, fim);
		}
	}

}
