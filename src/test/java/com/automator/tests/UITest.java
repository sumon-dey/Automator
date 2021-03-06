package com.automator.tests;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import com.automator.businessLayer.opencart.ItemsFunctionality;
import com.automator.handlers.dataHandler.TestSuiteMetaDataHandler;
import com.automator.handlers.fileHandler.PropertyFileHandler;
import com.automator.handlers.reportHandler.FrameworkReportHandler;
import com.automator.handlers.reportHandler.TestCaseExecutionStatus;
import com.aventstack.extentreports.ExtentTest;

@TestPropertySource("classpath:environments/${env}.properties")
@ContextConfiguration("classpath*:application-context.xml")
public class UITest extends AbstractTestNGSpringContextTests {

	private static final Logger log = Logger.getLogger(UITest.class);
	private FrameworkReportHandler frameworkReportHandler;
	private static InheritableThreadLocal<ExtentTest> extentTest = new InheritableThreadLocal<ExtentTest>();
	private TestSuiteMetaDataHandler testSuiteMetaDataHandler;
	private final String configFileRootPath = System.getProperty("user.dir") + File.separator + "src" + File.separator
			+ "test" + File.separator + "resources" + File.separator + "configs" + File.separator;

	@Value("${url}")
	private String url;

	@Test(enabled = true)
	public void shouldValidateTheNavigationLinks(Method testMethod, ITestContext iTestContext) {
		String testSuiteName = iTestContext.getSuite().getName();
		String testMethodName = testMethod.getName();
		PropertyFileHandler propertyFileHandler = new PropertyFileHandler();
		/*
		 * String url = propertyFileHandler.getDataFromPropertiesFile("url",
		 * configFileRootPath + "ProductSearchTest.properties");
		 */
		ItemsFunctionality itemsFunctionality = new ItemsFunctionality(frameworkReportHandler, extentTest.get(),
				testSuiteName, testMethodName);
		itemsFunctionality.launch("Firefox");
		itemsFunctionality.visit(url);
		String[] searchItems = { "Desktops", "Laptops & Notebooks", "Components", "Tablets", "Software",
				"Phones & PDAs", "Cameras", "MP3 Players" };
		for (String searchItem : searchItems) {
			itemsFunctionality.validateNavbarItemIsEnabled(searchItem);
		}
		itemsFunctionality.end();
	}

	@BeforeSuite
	public void testSuiteSetup(ITestContext iTestContext) {
		String testSuiteName = iTestContext.getSuite().getName();
		log.info("=============== Initiating Test Suite: " + testSuiteName + " ===============");
		testSuiteMetaDataHandler = new TestSuiteMetaDataHandler(testSuiteName);
		testSuiteMetaDataHandler.setTestSuiteStartTime(System.currentTimeMillis());
		frameworkReportHandler = new FrameworkReportHandler();
		frameworkReportHandler.initiateExtentReportFormatter(testSuiteName);
		frameworkReportHandler.initiateExcelReportFormatter(testSuiteName);
	}

	@BeforeMethod
	public void testMethodSetup(Method testMethod) {
		String testMethodName = testMethod.getName();
		log.info("=============== Initiating Test method: " + testMethodName + " ===============");
		extentTest.set(frameworkReportHandler.getExtentReports().createTest(testMethodName));
	}

	@AfterMethod
	public void testMethodTeardown(Method testMethod, ITestResult iTestResult, ITestContext iTestContext) {
		String testSuiteName = iTestContext.getSuite().getName();
		frameworkReportHandler.appendOverallResultToExtentReportForEachTest(iTestResult, extentTest.get(),
				testSuiteName);
		String testCaseName = testMethod.getName();
		String testCaseDescription = iTestResult.getMethod().getDescription();
		TestCaseExecutionStatus testCaseExecutionStatus = null;
		if (iTestResult.getStatus() == ITestResult.FAILURE) {
			testCaseExecutionStatus = TestCaseExecutionStatus.FAIL;
		} else if (iTestResult.getStatus() == ITestResult.SUCCESS) {
			testCaseExecutionStatus = TestCaseExecutionStatus.PASS;
		} else if (iTestResult.getStatus() == ITestResult.SKIP) {
			testCaseExecutionStatus = TestCaseExecutionStatus.SKIP;
		}
		String testCaseTime = new SimpleDateFormat("dd-MMM-yyyy hh-mm-ss aa").format(new Date());
		ArrayList<String> testCaseMetaData = new ArrayList<>();
		testCaseMetaData.add(testCaseDescription);
		testCaseMetaData.add(testCaseExecutionStatus.toString());
		testCaseMetaData.add(testCaseTime);
		testSuiteMetaDataHandler.insertDataIntoTestSuiteMetaData(testCaseName, testCaseMetaData);
		log.info("=============== Ending Test method: " + testMethod.getName() + " ===============");
	}

	@AfterSuite
	public void testSuiteTeardown(ITestContext testSuiteName) {
		testSuiteMetaDataHandler.setTestSuiteEndTime(System.currentTimeMillis());
		testSuiteMetaDataHandler.calculateAndSetTestSuiteExecutionTime();
		frameworkReportHandler.flushExtentReport();
		frameworkReportHandler.flushExcelReport(testSuiteMetaDataHandler.getTestSuiteMetaData(),
				testSuiteMetaDataHandler.getTestSuiteExecutionTime());
		log.info("=============== Ending Test Suite: " + testSuiteName.getSuite().getName() + " ===============");
	}

}
