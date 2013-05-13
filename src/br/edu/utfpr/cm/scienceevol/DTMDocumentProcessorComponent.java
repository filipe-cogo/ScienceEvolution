package br.edu.utfpr.cm.scienceevol;

import java.util.Map;


import lode.miner.PipelineUtil;
import lode.miner.TypicalPipelineComponent;
import lode.model.Element;
import lode.model.text.TextResource;

public class DTMDocumentProcessorComponent extends TypicalPipelineComponent
{
	private Map<String, Integer> terms;
	
	private Map<String, Integer> entryTerms;
	
	public DTMDocumentProcessorComponent(Map<String, Integer> terms, Map<String, Integer> entryTerms)
	{
		this.terms = terms;
		this.entryTerms = entryTerms;
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
		String word = textResource.getText(); 

		// Account for word in the document
		if (entryTerms.containsKey(word)) {
			entryTerms.put(word, entryTerms.get(word) + 1);
		} else {
			entryTerms.put(word, 1);
		}
			
		// Account for word in the collection, giving them an unique Id
		if (! terms.containsKey(word)) {
			terms.put(word, terms.size() + 1);
		}
	}

}
