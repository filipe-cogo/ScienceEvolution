package br.edu.utfpr.cm.scienceevol;

import java.util.concurrent.atomic.AtomicBoolean;

import lode.miner.ConsumerComponent;
import lode.miner.ProducerComponent;

public abstract class TextPipelinePreprocessor {

	protected ConsumerComponent start;
	protected ProducerComponent end;
	protected AtomicBoolean useStemmer;
	protected AtomicBoolean useStopwords;
	protected AtomicBoolean useLUCut;

	public TextPipelinePreprocessor() {
		useStemmer = new AtomicBoolean();
		useStopwords = new AtomicBoolean();
		useLUCut = new AtomicBoolean();
		useStemmer.set(true);
		useStopwords.set(true);
		useLUCut.set(true);
		setupPipeline();
	}

	protected abstract void setupPipeline();
	
	public ConsumerComponent getStart() {
		return start;
	}

	public void setStart(ConsumerComponent start) {
		this.start = start;
	}

	public ProducerComponent getEnd() {
		return end;
	}

	public void setEnd(ProducerComponent end) {
		this.end = end;
	}

	public boolean isUseStemmer() {
		return useStemmer.get();
	}

	public void setUseStemmer(boolean useStemmer) {
		this.useStemmer.set(useStemmer);
	}

	public boolean isUseStopwords() {
		return useStopwords.get();
	}

	public void setUseStopwords(boolean useStopwords) {
		this.useStopwords.set(useStopwords);
	}

	public boolean isLUCut() {
		return useLUCut.get();
	}

	public void setLUCut(boolean useLUCut) {
		this.useLUCut.set(useLUCut);
	}

}