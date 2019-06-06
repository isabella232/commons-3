package io.onedev.commons.utils;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.net.ssl.SSLHandshakeException;

public class ExceptionUtils extends org.apache.commons.lang3.exception.ExceptionUtils {
	
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> T find(Throwable throwable, Class<T> exceptionClass) {
        if (exceptionClass.isAssignableFrom(throwable.getClass()))
            return (T) throwable;
        Set<Throwable> examinedCauses = new HashSet<Throwable>(); 
        examinedCauses.add(throwable);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (exceptionClass.isAssignableFrom(cause.getClass()))
                return (T) cause;
            examinedCauses.add(cause);
            
            // cause.getCause() may return a previously examined cause
            if (examinedCauses.contains(cause.getCause()))
            	break;
            cause = cause.getCause();
        }
        return null;
    }
	
	public static RuntimeException unchecked(Throwable e) {
		if (e instanceof RuntimeException)
			return (RuntimeException) e;
		else
			return new RuntimeException(e);
	}

	@Nullable
	public static String suggestSolution(Exception e) {
		if (find(e, SSLHandshakeException.class) != null && System.getProperty("java.vendor").contains("Oracle")) {
			File securityDir;
			String javaHome = System.getProperty("java.home");
			if (javaHome != null) {
				if (new File(javaHome, "lib/security/local_policy.jar").exists()) {
					securityDir = new File(javaHome, "lib/security");
				} else if (new File(javaHome, "jre/lib/security/local_policy.jar").exists()) {
					securityDir = new File(javaHome, "jre/lib/security");
				} else {
					securityDir = null;
				}
			} else {
				securityDir = null;
			}
			String downloadUrl;
			String javaVersion = System.getProperty("java.version");
			if (javaVersion.startsWith("1.8.")) {
				downloadUrl = "http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html";
			} else {
				downloadUrl = null;
			}
			return String.format("You probably need to download Oracle JCE unlimited strength policy package %s, "
					+ "and place extracted jar files into %s to overwrite existing files, followed by restarting OneDev", 
					downloadUrl!=null?"from \"" + downloadUrl + "\"":"for your JVM version", 
					securityDir!=null?"directory \""+securityDir.getAbsolutePath()+"\"":"security directory of your JVM");
		}
		return null;
	}
	
}
