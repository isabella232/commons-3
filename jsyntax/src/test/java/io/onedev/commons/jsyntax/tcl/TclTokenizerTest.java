package io.onedev.commons.jsyntax.tcl;

import org.junit.Test;

import io.onedev.commons.jsyntax.AbstractTokenizerTest;
import io.onedev.commons.jsyntax.tcl.TclTokenizer;

public class TclTokenizerTest extends AbstractTokenizerTest{
	@Test
	public void test() {
		verify(new TclTokenizer(), new String[] {"tcl/tcl.js"}, "test.tcl");
	}
}