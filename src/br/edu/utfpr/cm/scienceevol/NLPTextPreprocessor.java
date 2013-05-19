package br.edu.utfpr.cm.scienceevol;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import lode.miner.PipelineUtil;
import lode.miner.TypicalPipelineComponent;
import lode.model.Element;
import lode.model.text.TextResource;
import lode.model.text.UnformattedTextResource;

public class NLPTextPreprocessor extends TypicalPipelineComponent
{
	private Tokenizer tokenizer;
	
	private POSTaggerME tagger;
	
	public NLPTextPreprocessor()
	{
		// InputStream modelIn = NLPTextPreprocessor.class.getResourceAsStream("en/nlp/en-token.bin");
		// InputStream rules_POS = NLPTextPreprocessor.class.getResourceAsStream("en/nlp/en-pos-maxent.bin");
		TokenizerModel model = null;
        POSModel modelPOS = null;
        
        try {
    		InputStream modelIn = new FileInputStream("/run/media/magsilva/magsilva-1TB/Projects/Lode/lode-miner/resources/en/nlp/en-token.bin");
    		InputStream rules_POS = new FileInputStream("/run/media/magsilva/magsilva-1TB/Projects/Lode/lode-miner/resources/en/nlp/en-pos-maxent.bin");
        	model = new TokenizerModel(modelIn);
        	modelPOS = new POSModel(rules_POS);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }

        tokenizer = new TokenizerME(model);
        tagger = new POSTaggerME(modelPOS);
	}
	
	
	@Override
	public Class<? extends Element>[] getInputResourceTypes() {
		return PipelineUtil.getResourceTypes(TextResource.class);
	}

	@Override
	public Class<? extends Element>[] getOutputResourceTypes() {
		return PipelineUtil.getResourceTypes(TextResource.class);
	}

	@Override
	protected void process(Element resource) {
		TextResource textResource = (TextResource) resource;
		

        String paras[] = tokenizer.tokenize(textResource.getText());

        String tags[] = tagger.tag(paras);
        ArrayList<String> words = new ArrayList<>();
        for (int i = 0; i < tags.length; i++) {
        	if (tags[i].compareTo("CC") != 0 && tags[i].compareTo("CD") != 0
                && tags[i].compareTo("DT") != 0 && tags[i].compareTo("EX") != 0
                && tags[i].compareTo("IN") != 0 && tags[i].compareTo("JJR") != 0
                && tags[i].compareTo("JJS") != 0 && tags[i].compareTo("LS") != 0
                && tags[i].compareTo("MD") != 0 && tags[i].compareTo("PDT") != 0
                && tags[i].compareTo("POS") != 0 && tags[i].compareTo("PRP") != 0
                && tags[i].compareTo("PRP$") != 0 && tags[i].compareTo("RB") != 0
                && tags[i].compareTo("RBR") != 0 && tags[i].compareTo("RBS") != 0
                && tags[i].compareTo("RP") != 0 && tags[i].compareTo("SYM") != 0
                && tags[i].compareTo("TO") != 0 && tags[i].compareTo("UH") != 0
                && tags[i].compareTo("VB") != 0 && tags[i].compareTo("VBD") != 0
                && tags[i].compareTo("VBG") != 0 && tags[i].compareTo("VBN") != 0
                && tags[i].compareTo("VBP") != 0 && tags[i].compareTo("VBZ") != 0
                && tags[i].compareTo("WDT") != 0 && tags[i].compareTo("WP") != 0
                && tags[i].compareTo("WP$") != 0 && tags[i].compareTo("WRB") != 0
			) {
                words.add(paras[i]);
        	}
        }
        
        for (String word : words) {
        	UnformattedTextResource newTextResource = new UnformattedTextResource();
        	newTextResource.setText(word);
        	deliver(newTextResource);
        }
	}
	

}
