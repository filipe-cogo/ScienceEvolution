package br.edu.utfpr.cm.scienceevol;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class UtilTest
{
	private Util util;
	
	@Before
	public void setUp() throws Exception {
	}
	

	@Test
	public void testLeiaTermos() throws Exception {
		String[] terms;
		Corpus corpus = new Corpus();
		corpus.setBasedir(new File("/tmp/SBSC"));
		corpus.setName("SBSC");
		corpus.setTermsCount(1611);
		
		util = new Util(corpus, null);
		terms = util.leiaTermos();
		assertEquals("process", terms[0]);
		assertEquals("role", terms[61]);
		assertEquals("pilot", terms[885]);
		assertEquals("originate", terms[1609]);
		assertEquals("end", terms[1610]);
		try {
			assertEquals("", terms[1611]);
			fail();
		} catch (IndexOutOfBoundsException e) {}
	}

	@Test
	public void testGimmeCubao() throws Exception {
		Corpus corpus = new Corpus();
		corpus.setBasedir(new File("/tmp/SBSC"));
		corpus.setName("SBSC");
		corpus.setTermsCount(1611);
		corpus.setYearsCount(9);
		
		TopicModel model = new TopicModel();
		model.setBasedir("/tmp/SBSC/dtm");
		model.setTopicsCount(25);

		util = new Util(corpus, model);
		util.gimmeCubao();
		for (int i = 0; i < corpus.getYearsCount(); i++) {
			for (int j = 0; j < model.getTopicsCount(); j++) {
				for (int k = 0; k < corpus.getTermsCount(); k++) {
					System.out.println(i + ", " + j + ", " + k);
					assertNotNull(util.cube[i][j][k]);
					assertNotNull(util.cube[i][j][k].getX());
					assertNotNull(util.cube[i][j][k].getY());
				}
			}
		}
		/*
		// year, topic, terms
		util.gimmeCubao();
		assertEquals(0, util.cube[0][0][0].getX().intValue());
		assertEquals(-8.52288256915622, util.cube[0][0][0].getY(), .005);
		assertEquals(0, util.cube[1][0][0].getX().intValue());
		assertEquals(-8.52484518460519, util.cube[1][0][0].getY(), .005);
		*/
		util.update();
		util.orderResultsPerYearPerTopic();
		util.printXBetterResultsPerYearPerTopic(10);
	}
}
