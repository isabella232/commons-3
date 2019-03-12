package io.onedev.commons.utils;

import static io.onedev.commons.utils.PathUtils.matchLongest;
import static io.onedev.commons.utils.PathUtils.matchSegments;
import static io.onedev.commons.utils.PathUtils.parseRelative;
import static io.onedev.commons.utils.PathUtils.resolveSibling;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.junit.Test;

import com.google.common.collect.Lists;

public class PathUtilsTest {

	@Test
	public void shouldMatchLongest() {
		Collection<String> basePaths = Lists.newArrayList("/path1/path2", "/path1/path2/path3");
		assertEquals(matchLongest(basePaths, "/path1/path2/path3/path4"), "/path1/path2/path3");
		assertNull(matchLongest(basePaths, "/path1/path23"));
		assertNull(matchLongest(basePaths, "/path1/path3"));
		
		basePaths = Lists.newArrayList("/path1/path2", "/path1\\");
		assertEquals(matchLongest(basePaths, "path1\\path234"), "/path1\\");
		
		basePaths = Lists.newArrayList("/asset1/asset2", "/asset1");
		assertEquals(matchLongest(basePaths, "/asset1/asset2/test.html"), "/asset1/asset2");
	}
	
	@Test
	public void shouldMatchSegments() {
		assertEquals("0-5", matchSegments("hello/world", "hello", true).toString());
		assertEquals("5-16", matchSegments("just/hello/world/do", "hello/world", true).toString());
		assertEquals("13-18", matchSegments("hello0/world/hello.gif", "hello", true).toString());
		assertNull(matchSegments("mayhello/worldtwo", "hello/world", true));
		assertNull(matchSegments("hello0/world/hello.gif", "hello", false));
	}

	@Test
	public void testResolveSibling() {
		assertEquals(resolveSibling("hello", "world"), "world");
		assertEquals(resolveSibling("hello/", "world"), "hello/world");
		assertEquals(resolveSibling("hello/world", "file"), "hello/file");
		assertEquals(resolveSibling("hello/world", "/file"), "/file");
	}
	
	@Test
	public void shouldParseRelative() {
		assertEquals(parseRelative("/path1/path2", "/path1"), "/path2");
		assertEquals(parseRelative("\\path1/path2", "/path1\\"), "/path2");
		assertEquals(parseRelative("path1\\path2/", "path1"), "/path2");
		assertEquals(parseRelative("/path1/path2", "/path1/path2"), "");
		assertNull(parseRelative("/path1/path2", "/path1/path3"));
		assertNull(parseRelative("/path1/path23", "/path1/path2"));
	}
	
}
