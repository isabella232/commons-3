package io.onedev.commons.jsyntax;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Splitter;

/**
 * Tokenizers should be thread-safe. Calling any public methods of the tokenizer 
 * should not change any of its internal state
 * 
 * @author robin
 *
 * @param <S>
 */
public abstract class AbstractTokenizer<S> implements Tokenizer {

	@Override
	public List<Tokenized> tokenize(List<String> lines) {
		List<Tokenized> tokenizedLines = new ArrayList<>();
		S state = startState();
		for (String line: lines) {
			List<Long> tokenList = new ArrayList<>();
			if (line.length() > Short.MAX_VALUE) {
				tokenizedLines.add(new Tokenized(line, new long[]{TokenUtils.getToken(0, line.length(), 0)}));
			} else {
				if (line.length() == 0)
					blankLine(state);
				StringStream stream = new StringStream(line);
				while (!stream.eol()) {
					String style = token(stream, state);
					int beginPos = stream.start();
					int endPos = stream.pos();
					if (beginPos < 0)
						beginPos = 0;
					if (endPos > line.length())
						endPos = line.length();
					if (beginPos <= endPos) {
						long token = TokenUtils.getToken(beginPos, endPos, style);
						List<Long> splitted = TokenUtils.splitWhitespace(line, token);
						if (splitted != null)
							tokenList.addAll(splitted);
						else
							tokenList.add(token);
					}
					stream.start(stream.pos());
				}
				tokenizedLines.add(new Tokenized(line, TokenUtils.toArray(tokenList)));
			}
		}
		return tokenizedLines;
	}
	
	public abstract S startState();
 	
	public abstract String token(StringStream stream, S state);
	
	protected void blankLine(S state) {
		
	}
	
	protected int indentUnit() {
		return 2;
	}
	
	protected boolean acceptExtensions(String fileName, String...exts) {
		String thisExt = StringUtils.substringAfterLast(fileName, ".");
		for (String ext: exts) {
			if (ext.equalsIgnoreCase(thisExt))
				return true;
		}
		return false;
	}

	protected boolean acceptPattern(String fileName, Pattern pattern) {
		return pattern.matcher(fileName).matches();
	}
	
	protected static Set<String> wordsOf(String str) {
		Set<String> words = new HashSet<>();
		for (String word: Splitter.on(" ").omitEmptyStrings().trimResults().split(str))
			words.add(word);
		return words;
	}
	
	protected static Set<String> wordsOf(Set<String> words, String str) {
		Set<String> newWords = new HashSet<>(words);
		for (String word: Splitter.on(" ").omitEmptyStrings().trimResults().split(str))
			newWords.add(word);
		return newWords;
	}

}
