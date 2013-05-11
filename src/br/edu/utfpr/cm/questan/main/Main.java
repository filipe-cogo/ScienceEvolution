package br.edu.utfpr.cm.questan.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;

public class Main {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		Util util = new Util();
		// Double[ano][topico][termo]
		Dupla<Integer, Double>[][][] cubao = (Dupla<Integer, Double>[][][]) new Dupla<?, ?>[Util.QT_ANOS][Util.QT_TOPICOS][Util.QT_TERMOS];
		util.gimmeCubao(Util.BASEDIR, cubao);
		util.orderResultsPerYearPerTopic(cubao);
		util.printXBetterResultsPerYearPerTopic(cubao, 30);
		util.printXBetterResultsPerYearPerTopicInFile(cubao, 30);
	}

}
