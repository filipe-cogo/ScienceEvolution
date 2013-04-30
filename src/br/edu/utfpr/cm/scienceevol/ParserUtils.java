package br.edu.utfpr.cm.scienceevol;

import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

public class ParserUtils {
	private static final String[] ADDITIONAL_STOP_WORDS = { "a's", "able", "about",
        "above", "according", "accordingly", "across", "actually", "after",
        "afterwards", "again", "against", "ain't", "all", "allow",
        "allows", "almost", "alone", "along", "already", "also",
        "although", "always", "am", "among", "amongst", "an", "and",
        "another", "any", "anybody", "anyhow", "anyone", "anything",
        "anyway", "anyways", "anywhere", "apart", "appear", "appreciate",
        "appropriate", "are", "aren't", "around", "as", "aside", "ask",
        "asking", "associated", "at", "available", "away", "awfully", "be",
        "became", "because", "become", "becomes", "becoming", "been",
        "before", "beforehand", "behind", "being", "believe", "below",
        "beside", "besides", "best", "better", "between", "beyond", "both",
        "brief", "but", "by", "c'mon", "c's", "came", "can", "can't",
        "cannot", "cant", "cause", "causes", "certain", "certainly",
        "changes", "clearly", "co", "com", "come", "comes", "concerning",
        "consequently", "consider", "considering", "contain", "containing",
        "contains", "corresponding", "could", "couldn't", "course",
        "currently", "definitely", "described", "despite", "did", "didn't",
        "different", "do", "does", "doesn't", "doing", "don't", "done",
        "down", "downwards", "during", "each", "edu", "eg", "eight",
        "either", "else", "elsewhere", "enough", "entirely", "especially",
        "et", "etc", "even", "ever", "every", "everybody", "everyone",
        "everything", "everywhere", "ex", "exactly", "example", "except",
        "far", "few", "fifth", "first", "five", "followed", "following",
        "follows", "for", "former", "formerly", "forth", "four", "from",
        "further", "furthermore", "get", "gets", "getting", "given",
        "gives", "go", "goes", "going", "gone", "got", "gotten",
        "greetings", "had", "hadn't", "happens", "hardly", "has", "hasn't",
        "have", "haven't", "having", "he", "he's", "hello", "help",
        "hence", "her", "here", "here's", "hereafter", "hereby", "herein",
        "hereupon", "hers", "herself", "hi", "him", "himself", "his",
        "hither", "hopefully", "how", "howbeit", "however", "i'd", "i'll",
        "i'm", "i've", "ie", "if", "ignored", "immediate", "in",
        "inasmuch", "inc", "indeed", "indicate", "indicated", "indicates",
        "inner", "insofar", "instead", "into", "inward", "is", "isn't",
        "it", "it'd", "it'll", "it's", "its", "itself", "just", "keep",
        "keeps", "kept", "know", "known", "knows", "last", "lately",
        "later", "latter", "latterly", "least", "less", "lest", "let",
        "let's", "like", "liked", "likely", "little", "look", "looking",
        "looks", "ltd", "mainly", "many", "may", "maybe", "me", "mean",
        "meanwhile", "merely", "might", "more", "moreover", "most",
        "mostly", "much", "must", "my", "myself", "name", "namely", "nd",
        "near", "nearly", "necessary", "need", "needs", "neither", "never",
        "nevertheless", "new", "next", "nine", "no", "nobody", "non",
        "none", "noone", "nor", "normally", "not", "nothing", "novel",
        "now", "nowhere", "obviously", "of", "off", "often", "oh", "ok",
        "okay", "old", "on", "once", "one", "ones", "only", "onto", "or",
        "other", "others", "otherwise", "ought", "our", "ours",
        "ourselves", "out", "outside", "over", "overall", "own",
        "particular", "particularly", "per", "perhaps", "placed", "please",
        "plus", "possible", "presumably", "probably", "provides", "que",
        "quite", "qv", "rather", "rd", "re", "really", "reasonably",
        "regarding", "regardless", "regards", "relatively", "respectively",
        "right", "said", "same", "saw", "say", "saying", "says", "second",
        "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems",
        "seen", "self", "selves", "sensible", "sent", "serious",
        "seriously", "seven", "several", "shall", "she", "should",
        "shouldn't", "since", "six", "so", "some", "somebody", "somehow",
        "someone", "something", "sometime", "sometimes", "somewhat",
        "somewhere", "soon", "sorry", "specified", "specify", "specifying",
        "still", "sub", "such", "sup", "sure", "t's", "take", "taken",
        "tell", "tends", "th", "than", "thank", "thanks", "thanx", "that",
        "that's", "thats", "the", "their", "theirs", "them", "themselves",
        "then", "thence", "there", "there's", "thereafter", "thereby",
        "therefore", "therein", "theres", "thereupon", "these", "they",
        "they'd", "they'll", "they're", "they've", "think", "third",
        "this", "thorough", "thoroughly", "those", "though", "three",
        "through", "throughout", "thru", "thus", "to", "together", "too",
        "took", "toward", "towards", "tried", "tries", "truly", "try",
        "trying", "twice", "two", "un", "under", "unfortunately", "unless",
        "unlikely", "until", "unto", "up", "upon", "us", "use", "used",
        "useful", "uses", "using", "usually", "value", "various", "very",
        "via", "viz", "vs", "want", "wants", "was", "wasn't", "way", "we",
        "we'd", "we'll", "we're", "we've", "welcome", "well", "went",
        "were", "weren't", "what", "what's", "whatever", "when", "whence",
        "whenever", "where", "where's", "whereafter", "whereas", "whereby",
        "wherein", "whereupon", "wherever", "whether", "which", "while",
        "whither", "who", "who's", "whoever", "whole", "whom", "whose",
        "why", "will", "willing", "wish", "with", "within", "without",
        "won't", "wonder", "would", "wouldn't", "yes", "yet", "you",
        "you'd", "you'll", "you're", "you've", "your", "yours", "yourself",
        "yourselves", "zero", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", 
        /*inicio da lista complementar, stop words no contexto de cscw*/
        "achieve", "algorithm", "analysis", "analyzes", "analysed", "application", 
        "apply", "approach", "approaches", "article", "articles", "based", "carried", 
        "collaboration", "collaborative", "collaborate", "common", "computer", 
        "computers", "computing", "computational", "conducted", "data", "demonstrates", 
        "describes", "design", "designed", "designing", "developed", "discuss", 
        "due", "eminently", "enable", "ethnography", "evaluate", "evaluating", 
        "evaluation", "experimental", "exploring", "found", "generated", "good", 
        "hypothesizes", "identify", "implements", "improve", "improvement", "interested", 
        "interviu", "interesting", "investigation", "investigated", "investigates", "involving", 
        "lack", "line", "long", "made", "make", "main", "mechanisms", "mek", "method", "methods", 
        "methodology", "methodologies", "microsoft", "obtain", "obtained", "order", "oriented", "overview", 
        "paper", "papers", "performed", "preliminary", "present", "presents", "problem", "problems", 
        "problematic", "project", "propose", "proposed", "proposes", "provide", "provided", "purpose", 
        "quantitative", "qualitative", "reach", "recently", "related", "relevant", "research", 
        "researchers", "result", "results", "resulted", "science", "short", "showed", 
        "slmeetingroom", "study", "studies", "studied", "support", "supports", "supported", "system", 
        "systems", "takes", "technique", "techniques", "test", "tests", "ten", "tool", "tools", "treg", 
        "uff", "understand", "user", "users", "wgwsoa", "works", "xagent"};
	
	public static Reader getStopWordListAsReader(){
		StringBuffer sb = new StringBuffer();
		for(String s : ParserUtils.ADDITIONAL_STOP_WORDS){
			sb.append(s + " ");
		}
		
		Reader r = new StringReader(sb.toString());
		return r;
	}
	
	public static CharArraySet getStopWordListAsSet(){
		CharArraySet set = new CharArraySet(Version.LUCENE_40, Arrays.asList(ParserUtils.ADDITIONAL_STOP_WORDS), true);
		
		return set;
	}
	
	public static String[] getStopWordList(){
		return ParserUtils.ADDITIONAL_STOP_WORDS;
	}
}
