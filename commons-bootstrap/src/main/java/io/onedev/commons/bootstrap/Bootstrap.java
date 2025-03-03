package io.onedev.commons.bootstrap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Handler;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class Bootstrap {

	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

	public static final int BUFFER_SIZE = 64*1024;
	
	public static final int SOCKET_CONNECT_TIMEOUT = 60000;

	public static final String LOGBACK_CONFIG_FILE_PROPERTY_NAME = "logback.configurationFile";

	private static final String BOOTSTRAP = "io.onedev.commons.bootstrap.Bootstrap";
	
	public static final String APP_LOADER = "io.onedev.commons.loader.AppLoader";
	
	public static File installDir;
	
	private static File libCacheDir;

	public static boolean sandboxMode;

	public static boolean prodMode;
	
	public static Command command;
	
	public static void main(String[] args) {
		try {
			File loadedFrom = new File(Bootstrap.class.getProtectionDomain()
					.getCodeSource().getLocation().toURI().getPath());

			if (new File(loadedFrom.getParentFile(), "bootstrap.keys").exists())
				installDir = loadedFrom.getParentFile().getParentFile();
			else if (new File("target/sandbox").exists())
				installDir = new File("target/sandbox").getAbsoluteFile();
			else
				throw new RuntimeException("Unable to find product directory.");

			boolean launchedFromIDE = false;
			try {
				Class.forName(APP_LOADER);
				launchedFromIDE = true;
			} catch (ClassNotFoundException e) {
			}
			
			if (launchedFromIDE) {
				Map<String, File> systemClasspath = getSystemClasspath();
				if (systemClasspath == null) {
					throw new RuntimeException("File '" + getSystemClasspathFile().getAbsolutePath() 
							+ "' is expected to exist when launched from IDE");
				}
				
		    	Set<String> bootstrapKeys = getBootstrapKeys();
		    	
		    	List<File> bootstrapLibs = new ArrayList<>();
				for (Map.Entry<String, File> entry : systemClasspath.entrySet()) {
					if (bootstrapKeys.contains(entry.getKey())) 
						bootstrapLibs.add(entry.getValue());
				}					
				
				List<URL> urls = new ArrayList<>();
				for (File file: bootstrapLibs)
					urls.add(file.toURI().toURL());
				
				URLClassLoader bootClassLoader = new URLClassLoader(
						urls.toArray(new URL[0]), Bootstrap.class.getClassLoader().getParent());
				
				Class<?> bootstrapClass = bootClassLoader.loadClass(BOOTSTRAP);
				Method mainMethod = bootstrapClass.getDeclaredMethod("main", String[].class);
				mainMethod.invoke(null, (Object)args);
			} else {
				Locale.setDefault(Locale.US);
				/*
				 * Sandbox mode might be checked frequently so we cache the result here
				 * to avoid calling File.exists() frequently.
				 */
				sandboxMode = new File("target/sandbox").exists();
				prodMode = (System.getProperty("prod") != null);

				if (args.length != 0) {
					String[] commandArgs = new String[args.length-1];
					System.arraycopy(args, 1, commandArgs, 0, commandArgs.length);
					command = new Command(args[0], commandArgs);
				} else {
					command = null;
				}

				configureLogging();
				try {
					logger.info("Launching application from '" + installDir.getAbsolutePath() + "'...");

			        File tempDir = getTempDir();
					if (tempDir.exists()) {
						logger.info("Cleaning temp directory...");
						Files.walk(tempDir.toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
					} 
					
					if (!tempDir.mkdirs())
						throw new RuntimeException("Can not create directory '" + tempDir.getAbsolutePath() + "'");
					
					System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());

					libCacheDir = createTempDir("libcache");
					
					List<File> libFiles = new ArrayList<>();

					Map<String, File> systemClasspath = getSystemClasspath();
					if (systemClasspath != null) {
				    	Set<String> bootstrapKeys = getBootstrapKeys();
						for (Map.Entry<String, File> entry : systemClasspath.entrySet()) {
							if (!bootstrapKeys.contains(entry.getKey())) 
								libFiles.add(entry.getValue());
						}					
					} else {
						libFiles.addAll(getLibFiles(getLibDir()));
						cacheLibFiles(getLibDir());
					}
					
					File siteLibDir = new File(getSiteDir(), "lib");
					libFiles.addAll(getLibFiles(siteLibDir));
					cacheLibFiles(siteLibDir);
					libFiles.addAll(getLibFiles(libCacheDir));

					List<URL> urls = new ArrayList<URL>();

					// load our jars first so that we can override classes in third party
					// jars if necessary.
					for (File file : libFiles) {
						if (isPriorityLib(file)) {
							try {
								urls.add(file.toURI().toURL());
							} catch (MalformedURLException e) {
								throw new RuntimeException(e);
							}
						}
					}
					for (File file : libFiles) {
						if (!isPriorityLib(file)) {
							try {
								urls.add(file.toURI().toURL());
							} catch (MalformedURLException e) {
								throw new RuntimeException(e);
							}
						}
					}
					
					ClassLoader appClassLoader = new URLClassLoader(urls.toArray(new URL[0]), 
							Bootstrap.class.getClassLoader());
					Thread.currentThread().setContextClassLoader(appClassLoader);
					
					Lifecycle appLoader;
					try {
						Class<?> appLoaderClass = appClassLoader.loadClass(APP_LOADER);
						appLoader = (Lifecycle) appLoaderClass.newInstance();
						appLoader.start();
					} catch (Exception e) {
						throw unchecked(e);
					}

					Runtime.getRuntime().addShutdownHook(new Thread() {
						public void run() {
							try {
								appLoader.stop();
							} catch (Exception e) {
								throw unchecked(e);
							}
						}
					});
				} catch (Exception e) {
					logger.error("Error booting application", e);
					System.exit(1);
				}				 
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, File> getSystemClasspath() throws Exception {
		File classpathFile = getSystemClasspathFile();
		if (classpathFile.exists()) {
	    	try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(classpathFile))) {
	    		return (Map<String, File>) is.readObject();
	    	}
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private static Set<String> getBootstrapKeys() throws Exception {
    	try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(getBootstrapKeysFile()))) {
    		return (Set<String>) is.readObject();
    	} 
	}
	
	private static File getBootstrapKeysFile() {
		return new File(installDir, "boot/bootstrap.keys");
	}
	
	private static File getSystemClasspathFile() {
		return new File(installDir, "boot/system.classpath");
	}

	private static boolean isPriorityLib(File lib) {
		String entryName = "META-INF/onedev-artifact.properties";
		if (lib.isDirectory()) {
			return new File(lib, entryName).exists();
		} else {
			try (JarFile jarFile = new JarFile(lib)) {
				return jarFile.getJarEntry(entryName) != null;
			} catch (IOException e) {
				throw new RuntimeException(lib.getAbsolutePath(), e);
			} 
		}
	}
	
    public static File createTempDir(String prefix) {
        File temp;

        try {
			temp = File.createTempFile(prefix, "");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

        if (!temp.delete())
            throw new RuntimeException("Could not delete temp file: " + temp.getAbsolutePath());

        if (!temp.mkdirs())
            throw new RuntimeException("Could not create temp directory: " + temp.getAbsolutePath());

        return temp;    
    }
    
    public static File createTempDir() {
    	return createTempDir("temp");
    }

	private static List<File> getLibFiles(File libDir) {
		List<File> libFiles = new ArrayList<>();
		for (File file : libDir.listFiles()) {
			if (file.getName().endsWith(".jar"))
				libFiles.add(file);
		}
		return libFiles;
	}
	
	private static void cacheLibFiles(File libDir) {
		for (File file: libDir.listFiles()) {
			if (file.getName().endsWith(".zip")) {
				unzip(file, libCacheDir);
			}
		}
	}
	
	private static void configureLogging() {
		// Set system properties so that they can be used in logback
		// configuration file.
		if (command != null) {
			System.setProperty("logback.logFile", installDir.getAbsolutePath() + "/logs/" + command.getName() + ".log");
			System.setProperty("logback.fileLogPattern", "%-5level - %msg%n");			
			System.setProperty("logback.consoleLogPattern", "%-5level - %msg%n");
		} else {
			System.setProperty("logback.logFile", installDir.getAbsolutePath() + "/logs/server.log");
			System.setProperty("logback.consoleLogPattern", "%d{HH:mm:ss} %-5level %logger{36} - %msg%n");			
			System.setProperty("logback.fileLogPattern", "%date %-5level [%thread] %logger{36} %msg%n");
		}

		File configFile = new File(installDir, "conf/logback.xml");
		System.setProperty(LOGBACK_CONFIG_FILE_PROPERTY_NAME, configFile.getAbsolutePath());

		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(lc);
			lc.reset();
			configurator.doConfigure(configFile);
		} catch (JoranException je) {
			je.printStackTrace();
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

		// Redirect JDK logging to slf4j
		java.util.logging.Logger jdkLogger = java.util.logging.Logger.getLogger("");
		for (Handler handler : jdkLogger.getHandlers())
			jdkLogger.removeHandler(handler);
		SLF4JBridgeHandler.install();
	}

	public static File getBinDir() {
		return new File(installDir, "bin");
	}
	
	public static File getBootDir() {
		return new File(installDir, "boot");
	}
	
	public static File getLibDir() {
		return new File(installDir, "lib");
	}
	
	public static File getTempDir() {
		if (command != null) 
			return new File(installDir, "temp/" + command.getName());
		else
			return new File(installDir, "temp/server");
	}
	
	public static File getConfDir() {
		return new File(installDir, "conf");
	}
	
	public static File getSiteDir() {
		return new File(installDir, "site");
	}
	
	public static boolean isInDocker() {
		return new File(installDir, "IN_DOCKER").exists();
	}
	
	public static RuntimeException unchecked(Throwable e) {
		if (e instanceof RuntimeException)
			return (RuntimeException) e;
		else
			return new RuntimeException(e);
	}
	
    /**
	 * Unzip files matching specified matcher from specified file to specified directory.
	 * 
	 * @param srcFile 
	 * 		zip file to extract from
	 * @param 
	 * 		destDir destination directory to extract to
	 * @param matcher 
	 * 		if not null, only entries with name matching this param in the zip will be extracted.
	 * 		Use null if you want to extract all entries from the zip file.  
	 */
	public static void unzip(File srcFile, File destDir) {
	    try (InputStream is = new FileInputStream(srcFile);) {
	    	unzip(is, destDir);
	    } catch (Exception e) {
	    	throw unchecked(e);
	    }
	} 	
	
	/**
	 * Unzip files matching specified matcher from input stream to specified directory.
	 * 
	 * @param is
	 * 			input stream to unzip files from. This method will not close the stream 
	 * 			after using it. Caller should be responsible for closing
	 * @param destDir
	 * 			destination directory to extract files to
	 */
	public static void unzip(InputStream is, File destDir) {
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is, BUFFER_SIZE));		
	    try {
		    ZipEntry entry;
		    while((entry = zis.getNextEntry()) != null) {
				if (entry.getName().endsWith("/")) {
					createDir(new File(destDir, entry.getName()));
				} else {		    		
				    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(destDir, entry.getName())), BUFFER_SIZE);) {
				        int count;
				        byte data[] = new byte[BUFFER_SIZE];
				        while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) 
				        	bos.write(data, 0, count);
				        bos.flush();
				    }
				}
		    }
	    } catch (Exception e) {
	    	throw unchecked(e);
	    }
	} 		
	
	public static void createDir(File dir) {
		if (dir.exists()) {
            if (dir.isFile()) {
                throw new RuntimeException("Unable to create directory since the path " +
                		"is already used by a file: " + dir.getAbsolutePath());
            } 
		} else if (!dir.mkdirs()) {
            if (!dir.exists())
                throw new RuntimeException("Unable to create directory: " + dir.getAbsolutePath());
		}
	}
	
}
