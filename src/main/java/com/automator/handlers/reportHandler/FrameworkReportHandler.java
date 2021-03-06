package com.automator.handlers.reportHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;

import com.automator.controllers.ScreenshotController;
import com.automator.handlers.exceptionHandler.FrameworkException;
import com.automator.utilities.DateTimeUtility;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;

/**
 * This class handles the creation of different reports like ExtentReport, Excel
 * Report etc. by interacting with individual report handler classes like
 * ExtentReportHandler, ExcelReportHandler
 * 
 * @author Sumon Dey, July 22, 2021
 *
 */
public class FrameworkReportHandler {

	private ExtentReports extentReports;
	private ExtentTest extentTest;
	private String parentReportsFolderPath;
	static String parentReportsFolderPathForSummaryReport;
	private ExcelReportHandler excelReportHandler;
	private String testSuiteExcelReportFilePath;

	public ExtentReports getExtentReports() {
		return extentReports;
	}

	public void setExtentReports(ExtentReports extentReports) {
		this.extentReports = extentReports;
	}

	public ExtentTest getExtentTest() {
		return extentTest;
	}

	public void setExtentTest(ExtentTest extentTest) {
		this.extentTest = extentTest;
	}

	public void initiateExtentReportFormatter(String testSuiteName) {
		try {
			System.setProperty("org.freemarker.loggerLibrary", "none");
			parentReportsFolderPath = System.getProperty("user.dir") + File.separator + "reports" + File.separator
					+ "Run_" + DateTimeUtility.getFormattedCurrentDateTime("dd-MMM-yyyy_hh-mm-ss_aa") + File.separator;
			setExtentReportParentFolderPath(parentReportsFolderPath);
			String extentReportsFolderPath = parentReportsFolderPath + "HTML_Reports";
			String extentReportsFilePath = extentReportsFolderPath + File.separator + testSuiteName + ".html";
			File extentReportFile = new File(extentReportsFilePath);
			ExtentHtmlReporter extentHtmlReporter = new ExtentHtmlReporter(extentReportFile);
			ExtentReportHandler extentReportHandler = new ExtentReportHandler();
			extentHtmlReporter = extentReportHandler.configureExtentHtmlReporter(extentHtmlReporter);
			extentReports = new ExtentReports();
			extentReports.attachReporter(extentHtmlReporter);
			extentReports = extentReportHandler.setSystemInfoInExtentReports(extentReports, testSuiteName);
		} catch (Exception e) {
			throw new FrameworkException("Not able to initiate the generation of ExtentReport", e);
		}
	}

	public void captureAndAttachScreenshotForExtentReport(String extentLog, String testStep, ExtentTest extentTest,
			WebDriver driver, String testSuiteName, String testName) {
		try {
			this.extentTest = extentTest;
			ScreenshotController screenshotController = new ScreenshotController();
			screenshotController.setTestSuiteName(testSuiteName);
			screenshotController.setTestName(testName);
			screenshotController.setScreenShotParentFolderPath(getExtentReportParentFolderPath());
			switch (extentLog) {
			case "info":
				this.extentTest.info(testStep, MediaEntityBuilder
						.createScreenCaptureFromPath(screenshotController.addScreenshotToReport(driver)).build());
				break;
			case "pass":
				this.extentTest.pass(testStep, MediaEntityBuilder
						.createScreenCaptureFromPath(screenshotController.addScreenshotToReport(driver)).build());
				break;
			case "fail":
				this.extentTest.fail(testStep, MediaEntityBuilder
						.createScreenCaptureFromPath(screenshotController.addScreenshotToReport(driver)).build());
				break;
			}
		} catch (Exception e) {
			throw new FrameworkException("Not able to capture and attach screenshot for ExtentReport", e);
		}
	}

	public void appendOverallResultToExtentReportForEachTest(ITestResult iTestResult, ExtentTest extentTest,
			String testSuiteName) {
		try {
			this.extentTest = extentTest;
			ScreenshotController screenshotController = new ScreenshotController();
			screenshotController.setTestSuiteName(testSuiteName);
			screenshotController.setTestName(iTestResult.getMethod().getMethodName());
			screenshotController.setScreenShotParentFolderPath(getExtentReportParentFolderPath());
			if (iTestResult.getStatus() == ITestResult.FAILURE) {
				this.extentTest.fail("Test Failed");
			} else if (iTestResult.getStatus() == ITestResult.SUCCESS) {
				this.extentTest.pass("Test Passed");
			} else if (iTestResult.getStatus() == ITestResult.SKIP) {
				this.extentTest.pass("Test Skipped");
			}
		} catch (Exception e) {
			throw new FrameworkException("Not able to append overall result to the ExtentReport for each test", e);
		}
	}

	public void setExtentReportParentFolderPath(String parentReportsFolderPath) {
		this.parentReportsFolderPath = parentReportsFolderPath;
	}

	public String getExtentReportParentFolderPath() {
		return this.parentReportsFolderPath;
	}

	public void flushExtentReport() {
		extentReports.flush();
	}

	public void initiateExcelReportFormatter(String testSuiteName) {
		excelReportHandler = new ExcelReportHandler();
		setExcelReportParentFolderPath(parentReportsFolderPath);
		String excelReportsFolderPath = parentReportsFolderPath + "Excel_Reports";
		File excelReportsFolderDirectory = new File(excelReportsFolderPath);
		if (!excelReportsFolderDirectory.exists()) {
			excelReportsFolderDirectory.mkdirs();
		}
		testSuiteExcelReportFilePath = excelReportsFolderPath + File.separator + testSuiteName + ".xls";
		excelReportHandler.createTestSuiteExcelReport(testSuiteExcelReportFilePath, testSuiteName);
	}

	public void flushExcelReport(Map<String, ArrayList<String>> testSuiteMetaDataMap, String testSuiteExecutionTime) {
		excelReportHandler.addTestSuiteMetaDataToExcelReport(testSuiteExcelReportFilePath, testSuiteMetaDataMap,
				testSuiteExecutionTime);
	}

	public void setExcelReportParentFolderPath(String parentReportsFolderPath) {
		parentReportsFolderPathForSummaryReport = parentReportsFolderPath;
		this.parentReportsFolderPath = parentReportsFolderPath;
	}

	public String getExcelReportParentFolderPath() {
		return this.parentReportsFolderPath;
	}

}
