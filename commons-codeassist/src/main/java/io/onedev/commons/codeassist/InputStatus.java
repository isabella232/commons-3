package io.onedev.commons.codeassist;

import java.io.Serializable;

import io.onedev.commons.utils.LinearRange;

public class InputStatus implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String content;
	
	private final int caret;

	public InputStatus(String content, int caret) {
		this.content = content;
		this.caret = caret;
	}
	
	public InputStatus(String content) {
		this(content, content.length());
	}
	
	public String getContent() {
		return content;
	}

	public int getCaret() {
		return caret;
	}
	
	public String getContentBeforeCaret() {
		return content.substring(0, caret);
	}
	
	public String getContentAfterCaret() {
		return content.substring(caret, content.length());
	}
	
	public String replace(LinearRange range, String replaceWith) {
		return content.substring(0, range.getFrom()) + replaceWith + content.substring(range.getTo());
	}
	
	@Override
	public String toString() {
		return content + ":" + caret;
	}
	
}
