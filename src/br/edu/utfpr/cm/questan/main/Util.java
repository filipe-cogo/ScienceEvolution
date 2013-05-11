package br.edu.utfpr.cm.questan.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import au.com.bytecode.opencsv.CSVReader;

public class Util {

	public final static String BASEDIR = "C://Users//Igor//Dropbox//Anais SBSC//resources//dtm-0.8//dtm//sbsc//output//lda-seq//";
	public final static String VOCAB_FILE = "C://Users//Igor//Dropbox//Anais SBSC//resources//dtm-0.8//dtm//sbsc//SBSC-vocab.dat";
	public final static int QT_ANOS = 9;
	public final static int QT_TOPICOS = 40;
	public final static int QT_TERMOS = 1615;
	public static String[] listaTermos = new String[Util.QT_TERMOS];

	public Util() throws IOException {
		listaTermos = leiaTermos(VOCAB_FILE);
	}

	public String[] leiaTermos(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
		int i = 0;
		String nextLine;
		String[] retorno = new String[Util.QT_TERMOS];
		while ((nextLine = reader.readLine()) != null) {
			retorno[i++] = nextLine;
		}
		reader.close();
		return retorno;
	}

	public void printXBetterResultsPerYearPerTopicInFile(
			Dupla<Integer, Double>[][][] cubao, int qtde) throws IOException {
		FileWriter fw = new FileWriter(new File(BASEDIR + "Year-Topic-Term-Prob.csv"));
		fw.write("Ano;Tópico;Termo;Probabilidade\n");
		for (int ano = 0; ano < QT_ANOS; ano++) {
			for (int topico = 0; topico < QT_TOPICOS; topico++) {
				for (int i = 0; i < qtde; i++)
					fw.write(ano + 
							";" + 
							topico + 
							";" + 
							listaTermos[cubao[ano][topico][i].getX()] + 
							";" + 
							cubao[ano][topico][i].getY() +
							"\n");
			}
		}
		fw.close();
	}
	
	public void printXBetterResultsPerYearPerTopic(
			Dupla<Integer, Double>[][][] cubao, int qtde) {
		for (int ano = 0; ano < QT_ANOS; ano++) {
			System.out.println("\n================================ANO " + ano
					+ "===================================\n");
			for (int topico = 0; topico < QT_TOPICOS; topico++) {
				System.out.println("\n---TOPICO " + topico + " --- ANO: " + ano
						+ " ---\n");
				for (int i = 0; i < qtde; i++)
					System.out
							.println(listaTermos[cubao[ano][topico][i].getX()]
									+ ";" + cubao[ano][topico][i].getY());
			}
		}

	}

	public void gimmeCubao(String dir, Dupla<Integer, Double>[][][] cubao)
			throws FileNotFoundException, IOException {
		CSVReader cubaoReader;
		String[] nextLine;
		for (int topico = 0; topico < QT_TOPICOS; topico++) {
			cubaoReader = new CSVReader(new FileReader(dir + "dist-topic"
					+ topico + ".csv"), ';');
			nextLine = cubaoReader.readNext(); // pula primeira linha
			int termo = 0;
			while ((nextLine = cubaoReader.readNext()) != null) {
				// a primeira coluna do csv é o identificador do termo (por isso
				// começamos em 1)
				for (int ano = 1; ano <= QT_ANOS; ano++) {
					cubao[ano - 1][topico][termo] = new Dupla<Integer, Double>(
							termo, Math.exp(Double.parseDouble(nextLine[ano])));
				}
				termo++;
			}
			cubaoReader.close();
		}
	}

	public void orderResultsPerYearPerTopic(Dupla<Integer, Double>[][][] cubo) {
		for (int ano = 0; ano < QT_ANOS; ano++) {
			for (int topico = 0; topico < QT_TOPICOS; topico++) {
				quickSort(cubo[ano][topico], 0, QT_TERMOS - 1);
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
