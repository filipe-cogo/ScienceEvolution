package br.edu.utfpr.cm.scienceevol;

import java.io.File;

import lode.miner.BufferComponent;
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
import lode.miner.preprocessing.text.stemmer.SnowballEnglishStemmer;
import lode.miner.preprocessing.text.stemmer.Stemmer;
import lode.miner.preprocessing.text.stemmer.StemmerTransformer;
import lode.miner.preprocessing.text.stopword.StopWord;
import lode.miner.preprocessing.text.stopword.StopWordFilter;
import lode.miner.preprocessing.text.stopword.StopWordLoader;
import lode.miner.preprocessing.text.stopword.exact.SetStopword;
import lode.model.text.ProperNameTag;
import lode.model.text.TextResource;

public class PipelinePreprocessor
{  
	private ConsumerComponent start;
	
	private ProducerComponent end;
	
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

	public PipelinePreprocessor()
	{
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
		wordSingularizer.setConsumer(luCutWordFilter);
		
		// Remove words that are too short or too long
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
		versionFilter.setConsumer(stopwordFilter);
		
		// Remove stopwords
		stopwordLoader.loadLanguage(stopwords, "/run/media/magsilva/magsilva-1TB/Projects/Lode/lode-miner/resources/", "en");
		stopwordLoader.loadLanguage(stopwords, "/run/media/magsilva/magsilva-1TB/Projects/Lode/lode-miner/resources/", "latin");
		stopwordLoader.load(stopwords, new File("/run/media/magsilva/magsilva-1TB/Projects/ScienceEvolution/resources/stopwords/stopwords-sbsc.txt"));
		stopwordFilter.setStopWord(stopwords);
		stopwordFilter.setConsumer(stemmerTransformer);
		
		stemmerTransformer.setStemmer(stemmer);
		stemmerTransformer.setConsumer(bufferComponent);
				
				
		end = bufferComponent;
	}
}
