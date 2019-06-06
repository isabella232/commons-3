package io.onedev.commons.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Highlighter {

	public static String highlightRanges(String text, List<LinearRange> ranges, 
			Transformer<String> matchedTransformer, Transformer<String> unmatchedTransformer) {
		StringBuffer buffer = new StringBuffer();
    	int start = 0;
    	for (LinearRange range: RangeUtils.merge(ranges)) {
    		buffer.append(unmatchedTransformer.transform(text.substring(start, range.getFrom())));
    		buffer.append(matchedTransformer.transform(text.substring(range.getFrom(), range.getTo())));
    		start = range.getTo();
    	}
    	buffer.append(unmatchedTransformer.transform(text.substring(start, text.length())));
    	return buffer.toString();
	}

	public static String highlightPatterns(String text, List<Pattern> patterns, 
			Transformer<String> matchedTransformer, Transformer<String> unmatchedTransformer) {
		List<LinearRange> ranges = new ArrayList<>();
		for (Pattern pattern: patterns) {
	    	Matcher matcher = pattern.matcher(text);
	    	while (matcher.find()) 
	    		ranges.add(new LinearRange(matcher.start(), matcher.end()));
		}
		return highlightRanges(text, ranges, matchedTransformer, unmatchedTransformer);
	}
	
	public static String highlightLiterals(String text, List<String> literals, boolean caseSensitive,
			Transformer<String> matchedTransformer, Transformer<String> unmatchedTransformer) {
		String normalizedText;
		if (!caseSensitive)
			normalizedText = text.toLowerCase();
		else
			normalizedText = text;
		List<LinearRange> ranges = new ArrayList<>();
		for (String literal: literals) {
			String normalizedLiteral;
			if (!caseSensitive)
				normalizedLiteral = literal.toLowerCase();
			else
				normalizedLiteral = literal;
			int index = normalizedText.indexOf(normalizedLiteral);
			while (index != -1) {
				LinearRange range = new LinearRange(index, index+normalizedLiteral.length());
				ranges.add(range);
				index = normalizedText.indexOf(normalizedLiteral, range.getTo());
			}
		}
		return highlightRanges(text, ranges, matchedTransformer, unmatchedTransformer);
	}
	
}
