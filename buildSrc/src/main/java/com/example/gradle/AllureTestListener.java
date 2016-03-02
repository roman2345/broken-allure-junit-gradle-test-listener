package com.example.gradle;

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.testing.TestDescriptor;
import org.gradle.api.tasks.testing.TestListener;
import org.gradle.api.tasks.testing.TestResult;
import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.events.*;
import ru.yandex.qatools.allure.model.Label;
import ru.yandex.qatools.allure.model.LabelName;
import ru.yandex.qatools.allure.utils.AnnotationManager;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;


public class AllureTestListener implements TestListener {
   Logger logger = Logging.getLogger(this.getClass());

   private ClassLoader testClassLoader = null;

   private static Allure lifecycle = Allure.LIFECYCLE;

   private final Map<String, String> suiteNameToUidMap = new HashMap<String, String>();

   public AllureTestListener() {
        lifecycle.addListener(new AllureEventListener());
    }

    @Override
    public void beforeSuite(TestDescriptor suite) {
        if (suite.getClassName() != null) {
            startSuite(suite);
        }
    }

    public void startSuite(TestDescriptor suite) {
        try {
            Class testSuiteClass = getTestClassLoader().loadClass(suite.getClassName());

            String suiteUid = generateSuiteUid(suite, suite.getClassName());

            logger.log(LogLevel.INFO, "Sending TestSuiteStartedEvent for UID '" + suiteUid + "'.");

            TestSuiteStartedEvent event = new TestSuiteStartedEvent(suiteUid, suite.getClassName());

            AnnotationManager am = new AnnotationManager(testSuiteClass.getAnnotations());

            am.update(event);

            event.withLabels(createLabel(LabelName.FRAMEWORK, "Gradle/JUnit"));

            getLifecycle().fire(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void beforeTest(TestDescriptor test) {
        if (test.getClassName() == null || test.getName() == null) {
            return;
        }

        String suiteUid = getSuiteUid(test);

        System.out.println("Sending TestCaseStartedEvent for test: " + test + " and suite with UID: " + suiteUid);

        TestCaseStartedEvent event = new TestCaseStartedEvent(suiteUid, test.getName());
        
        logger.log(LogLevel.INFO, "Started test: " + test);

        try {
            Class actualTestClass = getTestClassLoader().loadClass(test.getClassName());

            logger.log(LogLevel.INFO, "Loaded the test class: " + test.getClassName());

            AnnotationManager am = new AnnotationManager(actualTestClass.getAnnotations());

            am.update(event);

            fireClearStepStorage();
            getLifecycle().fire(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void afterTest(TestDescriptor test, TestResult result) {
        if (test.getClassName() == null || test.getName() == null) {
            return;
        }

        logger.log(LogLevel.INFO, "Finished test: " + test.toString());

       /* if (result.getResultType().equals(TestResult.ResultType.FAILURE)) {
            fireTestCaseFailure(result.getException());
        }
        else if (result.getResultType().equals(TestResult.ResultType.SKIPPED)) {
            startFakeTestCase(test);
            getLifecycle().fire(new TestCasePendingEvent().withMessage("I don't know why it was ignored/skipped."));
            finishFakeTestCase();
        }*/

        System.out.println("Sending TestCaseFinishedEvent.");

        getLifecycle().fire(new TestCaseFinishedEvent());
    }


    @Override
    public void afterSuite(TestDescriptor suite, TestResult result) {
        if (suite.getClassName() == null || suite.getName() == null) {
            return;
        }

        try {
            for (Map.Entry<String, String> uidEntry : suiteNameToUidMap.entrySet()) {
                logger.log(LogLevel.INFO, "***" + uidEntry.getKey() + "=" + uidEntry.getValue());
            }

            logger.log(LogLevel.INFO, "After suite: " + suite.getClassName() + "/" + suite.getName() + "/" + suite.getParent());

            String suiteUid = getSuiteUid(suite);

            logger.log(LogLevel.INFO, "Sending TestSuiteFinishedEvent for UID '" + suiteUid + "'.");

            TestSuiteFinishedEvent event = new TestSuiteFinishedEvent(suiteUid);

            getLifecycle().fire(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** CUSTOM METHODS **/
    private ClassLoader getTestClassLoader() throws MalformedURLException {
        if (testClassLoader == null) {
            logger.log(LogLevel.INFO, "Preparing to load test classes.");

            String testClassPath = System.getProperty("user.dir") + "/a/build/classes/test";
            logger.log(LogLevel.INFO, "Test classes path: " + testClassPath);

            File testClassDir = new File(testClassPath);

            URL url = testClassDir.toURL();
            URL[] urls = new URL[]{ url };

            testClassLoader = new URLClassLoader(urls, getClass().getClassLoader());
        }

        return testClassLoader;
    }

   public String generateSuiteUid(TestDescriptor test, String suiteName) {
        String uid = UUID.randomUUID().toString();

        synchronized (suiteNameToUidMap) {
            suiteNameToUidMap.put(suiteName, uid);
        }

        return uid;
    }


    public String getSuiteUid(TestDescriptor description) {
        String suiteName = description.getClassName();

        if (! suiteNameToUidMap.containsKey(suiteName)) {
            logger.log(LogLevel.INFO, "Could not find KEY for: '" + description + "'. Generating one.");
            return generateSuiteUid(description, description.getClassName());
        }

        return suiteNameToUidMap.get(suiteName);
    }

/*
    public String getIgnoredMessage(Description description) {
        Ignore ignore = description.getAnnotation(Ignore.class);
        return ignore == null || ignore.value().isEmpty() ? "Test ignored (without reason)!" : ignore.value();
    }
*/

    public void startFakeTestCase(TestDescriptor description) {
        try {
            String suiteUid = getSuiteUid(description);

            TestCaseStartedEvent event = new TestCaseStartedEvent(suiteUid, description.getName());

            Class actualTestClass = getTestClassLoader().loadClass(description.getClassName()); //Class.forName(description.getClassName());

            AnnotationManager am = new AnnotationManager(actualTestClass.getAnnotations());
            am.update(event);

            fireClearStepStorage();
            getLifecycle().fire(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void finishFakeTestCase() {
        getLifecycle().fire(new TestCaseFinishedEvent());
    }

    public void fireTestCaseFailure(Throwable throwable) {
        if (throwable instanceof AssumptionViolatedException) {
            getLifecycle().fire(new TestCaseCanceledEvent().withThrowable(throwable));
        } else {
            getLifecycle().fire(new TestCaseFailureEvent().withThrowable(throwable));
        }
    }

    public void fireClearStepStorage() {
        getLifecycle().fire(new ClearStepStorageEvent());
    }

    public Allure getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Allure lifecycle) {
        this.lifecycle = lifecycle;
    }

    public static Label createLabel(LabelName name, String value) {
        return new Label().withName(name.value()).withValue(value);
    }

    public boolean isGradleTestClass(TestDescriptor descriptor) {
        return descriptor.getClassName().equals(descriptor.getName());
    }
}


