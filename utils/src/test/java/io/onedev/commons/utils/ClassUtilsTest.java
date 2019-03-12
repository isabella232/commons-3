package io.onedev.commons.utils;

import org.junit.Assert;
import org.junit.Test;

import io.onedev.commons.utils.ClassUtils;

public class ClassUtilsTest {
	
	@Test
	public void testGetResourceAsStream() {
		String packageName = ClassUtils.class.getPackage().getName();
		Assert.assertNotNull(ClassUtils.getResourceAsStream(
				null, packageName.replace('.', '/') + "/ClassUtils.class"));
		Assert.assertNotNull(ClassUtils.getResourceAsStream(
				ClassUtils.class, "ClassUtils.class"));
	}
	
}
