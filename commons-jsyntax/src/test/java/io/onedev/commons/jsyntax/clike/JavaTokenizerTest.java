package io.onedev.commons.jsyntax.clike;

import org.junit.Test;

import io.onedev.commons.jsyntax.AbstractTokenizerTest;

public class JavaTokenizerTest extends AbstractTokenizerTest {

	@Test
	public void test() {
		verify(new JavaTokenizer(), new String[]{"clike/clike.js"}, "test.java.txt");
	}

}
