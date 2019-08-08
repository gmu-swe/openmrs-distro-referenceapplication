package org.openmrs.reference;

import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openmrs.reference.page.HomePage;
import org.openmrs.reference.page.ReferenceApplicationLoginPage;
import org.openmrs.uitestframework.page.LoginPage;
import org.openmrs.uitestframework.page.Page;
import org.openmrs.uitestframework.test.TestBase;
import org.openqa.selenium.By;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertEquals;

/**
 * Each test class should extend ReferenceApplicationTestBase.
 *
 * Guidelines:
 * 1. Tests should be added under https://github.com/openmrs/openmrs-distro-referenceapplication/tree/master/ui-tests/src/test/java/org/openmrs/reference
 * 2. Each test class should be named starting with a verb, which best describes an action that is being tested, e.g. SearchActiveVisitsTest. By convention all test class names must end with Test.
 * 3. In general each class should contain one test method (annotated with @Test) and the test method should start with a verb and can provide a more detailed description of what is being tested than the class, e.g. searchActiveVisitsByPatientNameOrIdOrLastSeenTest.
 * 4. The test method should not visit more than 10 pages and should have 3-15 steps.
 * 5. You must not access Driver in a test. It is only allowed to perform actions calling methods in classes extending Page.
 * 6. Each test class should start from homePage and extend ReferenceApplicationTestBase.
 * 7. It is not allowed to instantiate classes extending Page in test classes. They must be returned from Page's actions e.g. ActiveVisitsPage activeVisitsPage = homePage.goToActiveVisitsSearch();
 * 8. Each page should have a corresponding class, which extends Page and it should be added under https://github.com/openmrs/openmrs-distro-referenceapplication/tree/master/ui-tests/src/main/java/org/openmrs/reference/page
 * 9. The page class should be named after page's title and end with Page.
 * 10. It is not allowed to call Driver's methods in a page. You should be calling methods provided by the Page superclass.
 *
 */
@RunWith(Parameterized.class)
public class ReferenceApplicationTestBase extends TestBase {

	private static final By SELECTED_LOCATION = By.id("selected-location");
	protected HomePage homePage;

	public ReferenceApplicationTestBase() {
		super();
	}

	@Before
	public void before() throws Exception {
	    homePage = new HomePage(page);
	}


	@Parameterized.Parameter
	public String platform;
	@Parameterized.Parameter(1)
	public String browser;
	@Parameterized.Parameter(2)
	public String browserVersion;
	private static volatile boolean serverFailure = false;

	@Parameterized.Parameters(
			name = "{index}: {0} {1} {2}"
	)
	public static Collection<Object[]> getBrowsers() {
//		return Arrays.asList(
//				new Object[]{"Linux", "Firefox", "42"},
//				new Object[]{"Linux", "Chrome", "48.0"});
		return Collections.singletonList(new Object[]{"Linux", "Chrome", "48.0"});
	}

	public String getLocationUuid(Page page){
		return page.findElement(SELECTED_LOCATION).getAttribute("location-uuid");
	}

	@Override
	protected LoginPage getLoginPage() {
		return new ReferenceApplicationLoginPage(driver);
	}


	/*
	Copied from TomcatBaseTest
	 */
	// Embedded tomcat instance
	private static Tomcat tomcat = null;
	// Path of the base directory used by Tomcat
	private static final String tomcatBaseDir = System.getProperty("tomcat.base.directory", "tomcat");
	// Path of the directory to which web application files are extracted
	private static final String webAppsBaseDir = System.getProperty("webapps.base.directory", "webapps");
	// Set of names of the web applications already added to tomcat
	private static final HashSet<String> addedWebApps = new HashSet<>();

	@Rule
	public final ExternalResource tomcatResource = new ExternalResource() {
		@Override
		protected void before() throws Throwable {
//			if(tomcat == null) {
//				try {
//					// Setup the embedded server
//					tomcat = new Tomcat();
//					tomcat.setBaseDir(tomcatBaseDir);
//					tomcat.getHost().setAppBase(tomcatBaseDir);
//					String protocol = Http11NioProtocol.class.getName();
//					Connector connector = new Connector(protocol);
//					// Listen on localhost
//					connector.setAttribute("address", InetAddress.getByName("localhost").getHostAddress());
//					// Use a random free port
//					connector.setPort(Integer.valueOf(System.getProperty("tomcat.port")));
//					tomcat.getService().addConnector(connector);
//					tomcat.setConnector(connector);
////				tomcat.setSilent(true);
//					tomcat.getHost().setDeployOnStartup(true);
//					tomcat.getHost().setAutoDeploy(true);
//					// Reduce logging
////				System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
////				replaceRootLoggerHandlers();
//					tomcat.enableNaming();
//					tomcat.init();
//					tomcat.start();
//					System.out.println("Tomcat is up!");
//
//					//Deploy the app
//					String warFile = "../package/target/distro/web/openmrs.war";
//					unzipWar(warFile, "openmrs");
//					tomcat.addWebapp("/openmrs", new File(webAppsBaseDir, "openmrs").getCanonicalPath());
//
//					//Now hit the installation page
//					waitForInstallation();
//
//					// Add the shutdown hook to stop the embedded server
//					Runnable shutdown = new Runnable() {
//						@Override
//						public void run() {
//							try {
//								try {
//									// Stop and destroy the server
//									if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
//										if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
//											tomcat.stop();
//										}
//										tomcat.destroy();
//										tomcat = null;
//									}
//								} finally {
//									// Delete tomcat's temporary working directory
//									FileUtils.deleteDirectory(new File(tomcatBaseDir));
//								}
//							} catch (Throwable t) {
//								t.printStackTrace();
//							}
//						}
//					};
//					Runtime.getRuntime().addShutdownHook(new Thread(shutdown));
//				} catch (Throwable thr) {
//					thr.printStackTrace();
//					throw thr;
//				}
//			}
		}
	};

	private static final int INSTALL_TIMEOUT = 1000*60*10; //10 minutes
	private static void waitForInstallation(){
		long start = System.currentTimeMillis();
		while(true){
			long now = System.currentTimeMillis();
			String status = pingInstallationPage();
			System.out.println(status);
			if(status.contains("Enter your username"))
				return;
			try {
				if(now - start > INSTALL_TIMEOUT) {
					System.err.println(status);
					throw new IllegalStateException("Application failed to start within timeout, see current status above");
				}
				Thread.sleep(1000*60);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private static String pingInstallationPage(){
		String result = null;
		URIBuilder builder =  new URIBuilder()
				.setScheme("http")
				.setHost(tomcat.getHost().getName())
				.setPort(tomcat.getConnector().getLocalPort()).setPath("/openmrs");
		CloseableHttpClient client = HttpClientBuilder.create().build();
		try(CloseableHttpResponse response = client.execute(new HttpGet(builder.build()))) {
			HttpEntity entity = response.getEntity();
			if(entity != null) {
				if(entity.getContentLength() != -1 || entity.isChunked()) {
					result = EntityUtils.toString(entity);
				}
			}
			EntityUtils.consume(entity);
		} catch(Exception e) {
			// Exceptions may occur on test reruns
		}
		return result;

	}


	/* Returns a newly created file handler with the specified logging level that logs to a file in the base tomcat directory. */
	private static Handler createFileHandler() throws IOException {
		// Ensure that the base directory exists
		File baseDirFile = new File(tomcatBaseDir);
		if(!baseDirFile.isDirectory() && !baseDirFile.mkdirs()) {
			System.err.println("Failed to make base directory for embedded tomcat.");
		}
		Handler fileHandler = new FileHandler(tomcatBaseDir + File.separator + "catalina.out", true);
		fileHandler.setFormatter(new SimpleFormatter());
		fileHandler.setLevel(Level.INFO);
		fileHandler.setEncoding("UTF-8");
		return fileHandler;
	}

	/* Removes any existing handlers for the specified logger and adds the specified handler. */
	private static void replaceRootLoggerHandlers() throws IOException {
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setUseParentHandlers(false);
		// Change the level of any existing handlers to OFF
		for(Handler h : rootLogger.getHandlers()) {
			h.setLevel(Level.OFF);
		}
		// Add a file handler for INFO level logging
		rootLogger.addHandler(createFileHandler());
	}

	/* If the directory for the web-app with the specified name does not exists creates it by unzipping the war for the
	 * web-app. */
	private static void unzipWar(String warFile, String shortName) throws IOException {
		File dir = new File(webAppsBaseDir, shortName);
		if(!dir.isDirectory()) {
			// Unzip war into the directory only if it does not already exists
			File webAppWar = new File(warFile);
			if(!webAppWar.isFile()) {
				throw new RuntimeException("Could not find war file for: " + warFile);
			}
			try(ZipFile zipFile = new ZipFile(webAppWar)) {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while(entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					File entryDestination = new File(dir, entry.getName());
					if(entry.isDirectory()) {
						if(!entryDestination.isDirectory() && !entryDestination.mkdirs()) {
							throw new RuntimeException("Failed to make directory for: " + entryDestination);
						}
						// Add velocity.properties file
						File velocityPropFile = new File(entryDestination, "velocity.properties");
						try {
							org.apache.commons.io.FileUtils.writeStringToFile(velocityPropFile,
									"runtime.log.logsystem.class=org.apache.velocity.runtime.log.NullLogChute", Consts.UTF_8, false);
						} catch(Exception e) {
							//
						}
					} else {
						if(!entryDestination.getParentFile().isDirectory() && !entryDestination.getParentFile().mkdirs()) {
							throw new RuntimeException("Failed to make directory for: " + entryDestination.getParentFile());
						}
						if(entry.getName().endsWith("log4j.properties")) {
							// Write different logging properties
							writeLog4JProperties(entryDestination);
						} else {
							InputStream in = zipFile.getInputStream(entry);
							OutputStream out = new FileOutputStream(entryDestination);
							IOUtils.copy(in, out);
							IOUtils.closeQuietly(in);
							out.close();
						}
					}
				}
			}
		}
	}

	/* Writes log4j properties to disable logging to the specified file. */
	private static void writeLog4JProperties(File file) throws IOException {
		String content =
				"log4j.rootLogger=OFF, stdout\n" +
						"log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n" +
						"log4j.appender.stdout.layout=org.apache.log4j.SimpleLayout\n";
		org.apache.commons.io.FileUtils.writeStringToFile(file,
				content, Consts.UTF_8, false);
	}
}
