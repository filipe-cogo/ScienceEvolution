package br.edu.utfpr.cm.scienceevol;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import lode.miner.BufferComponent;
import lode.miner.ConditionalDupComponent;
import lode.miner.ConsumerComponent;
import lode.miner.FilterComponent;
import lode.miner.ProducerComponent;
import lode.miner.preprocessing.TagFilter;
import lode.miner.preprocessing.text.AdverbFilter;
import lode.miner.preprocessing.text.InvalidCharRemover;
import lode.miner.preprocessing.text.ItemRemover;
import lode.miner.preprocessing.text.LUCuts;
import lode.miner.preprocessing.text.LowerCaseTransformer;
import lode.miner.preprocessing.text.NumberFilter;
import lode.miner.preprocessing.text.PercentageNumberFilter;
import lode.miner.preprocessing.text.ProperNameTagger;
import lode.miner.preprocessing.text.PunctuationFilter;
import lode.miner.preprocessing.text.SymbolReplacer;
import lode.miner.preprocessing.text.UTFSymbolRemover;
import lode.miner.preprocessing.text.VersionFilter;
import lode.miner.preprocessing.text.WhiteSpaceFilter;
import lode.miner.preprocessing.text.normalizer.LodeNormalizer;
import lode.miner.preprocessing.text.normalizer.Normalizer;
import lode.miner.preprocessing.text.normalizer.NormalizerComponent;
import lode.miner.preprocessing.text.stemmer.MorphaStemmer;
import lode.miner.preprocessing.text.stemmer.Stemmer;
import lode.miner.preprocessing.text.stemmer.StemmerTransformer;
import lode.miner.preprocessing.text.stopword.StopWord;
import lode.miner.preprocessing.text.stopword.StopWordFilter;
import lode.miner.preprocessing.text.stopword.StopWordLoader;
import lode.miner.preprocessing.text.stopword.exact.SetStopword;
import lode.model.text.ProperNameTag;
import lode.model.text.TextResource;

public class TextPipelinePreprocessor
{  
	private ConsumerComponent start;
	
	private ProducerComponent end;
	
	private AtomicBoolean useStemmer;
	
	private AtomicBoolean useStopwords;

	private AtomicBoolean useLUCut;
	
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

	public TextPipelinePreprocessor()
	{
		useStemmer = new AtomicBoolean();
		useStopwords = new AtomicBoolean();
		useLUCut = new AtomicBoolean();
		useStemmer.set(true);
		useStopwords.set(true);
		useLUCut.set(true);
		setupPipeline();
	}
	
	private void setupPipeline()
    {
		// What is left is plain text
		NumberFilter numberFilter = new NumberFilter();
		PercentageNumberFilter percentageFilter = new PercentageNumberFilter();
		ProperNameTagger properNameTagger = new ProperNameTagger();
		TagFilter properNameFilter = new TagFilter();
		LowerCaseTransformer lowercaseTransformer = new LowerCaseTransformer();
		SymbolReplacer symbolReplacer = new SymbolReplacer();
		ItemRemover itemRemover = new ItemRemover();
		AdverbFilter adverbFilter = new AdverbFilter();
		StopWordLoader stopwordLoader = new StopWordLoader();
		StopWordFilter stopwordFilter = new StopWordFilter();
		StopWord stopwords = new SetStopword();
		WhiteSpaceFilter emptyWordFilter = new WhiteSpaceFilter();
		InvalidCharRemover invalidCharRemover = new InvalidCharRemover();
		LUCuts luCutWordFilter = new LUCuts();
		PunctuationFilter punctuationFilter = new PunctuationFilter();
		NormalizerComponent wordSingularizer = new NormalizerComponent();
		Normalizer normalizer = new LodeNormalizer();
		FilterComponent filterComponent = new FilterComponent();
		UTFSymbolRemover utf8Remover = new UTFSymbolRemover();
		VersionFilter versionFilter = new VersionFilter();
		StemmerTransformer stemmerTransformer = new StemmerTransformer();
		// Stemmer stemmer = new SnowballEnglishStemmer();
		Stemmer stemmer = new MorphaStemmer();
		BufferComponent bufferComponent = new BufferComponent();
		ConditionalDupComponent stemmerDecision = new ConditionalDupComponent();
		ConditionalDupComponent stopwordDecision = new ConditionalDupComponent();
		ConditionalDupComponent lucutDecision = new ConditionalDupComponent();
		

		start = emptyWordFilter;
		
		// Discard empty tokens
		emptyWordFilter.setConsumer(punctuationFilter);
		
		// Discard punctuation tokens
		punctuationFilter.setConsumer(numberFilter);
		
		// Identify numbers and percentages
		numberFilter.setConsumer(percentageFilter);
		percentageFilter.setConsumer(filterComponent);
		
		// Allows only TextResources from now on (this will exclude the numbers previously identified
		filterComponent.allowOutputOf(TextResource.class);
		filterComponent.setConsumer(properNameTagger);
		
		// Detect proper names and remote them
		properNameTagger.setConsumer(properNameFilter);
		properNameFilter.add(new ProperNameTag());
		properNameFilter.setConsumer(lowercaseTransformer);
		
		// Make everything lowercase from now on
		lowercaseTransformer.setConsumer(invalidCharRemover);

		// Remove invalid chars
		invalidCharRemover.setConsumer(itemRemover);
		
		// Remove itemization (i, ii, iii, ...)
		itemRemover.setConsumer(wordSingularizer);
				
		// Try to remove some plurals
		wordSingularizer.setNormalizer(normalizer);
		wordSingularizer.setConsumer(lucutDecision);
		
		// Remove words that are too short or too long
		lucutDecision.setCondition(useLUCut);
		lucutDecision.setConsumerTrue(luCutWordFilter);
		lucutDecision.setConsumerFalse(adverbFilter);
		
		luCutWordFilter.setLowCut(1);
		luCutWordFilter.setUpCut(100);
		luCutWordFilter.setConsumer(adverbFilter);
		
		// Remove adverbs
		adverbFilter.setConsumer(symbolReplacer);	
		
		// Replace some UTF-8 symbols into ASCII-equivalents
		symbolReplacer.setConsumer(utf8Remover);
		
		// Remove non-ASCII symbols
		utf8Remover.setConsumer(versionFilter);

		// Remove strings that represents the version of a software
		versionFilter.setConsumer(stopwordDecision);
		
		stopwordDecision.setCondition(useStopwords);
		stopwordDecision.setConsumerTrue(stopwordFilter);
		stopwordDecision.setConsumerFalse(stemmerDecision);

		// Remove stopwords
		stopwordLoader.loadLanguage(stopwords, "/run/media/magsilva/magsilva-1TB/Projects/Lode/lode-miner/resources/", "en");
		stopwordLoader.loadLanguage(stopwords, "/run/media/magsilva/magsilva-1TB/Projects/Lode/lode-miner/resources/", "latin");
		stopwordLoader.load(stopwords, new File("/run/media/magsilva/magsilva-1TB/Projects/ScienceEvolution/resources/stopwords/stopwords-sbsc.txt"));
		stopwordFilter.setStopWord(stopwords);
		stopwordFilter.setConsumer(stemmerDecision);
		
    	stemmerDecision.setCondition(useStemmer);
    	stemmerDecision.setConsumerTrue(stemmerTransformer);
    	stemmerDecision.setConsumerFalse(bufferComponent);

    	stemmerDecision.setConsumer(stemmerTransformer);
		stemmerTransformer.setStemmer(stemmer);
		stemmerTransformer.setConsumer(bufferComponent);
				
		end = bufferComponent;
	}
}
