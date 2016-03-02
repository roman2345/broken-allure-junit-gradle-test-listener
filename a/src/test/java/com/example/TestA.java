package com.example;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ru.yandex.qatools.allure.Allure;
import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.annotations.TestCaseId;
import ru.yandex.qatools.allure.annotations.Title;
import ru.yandex.qatools.allure.events.StepFinishedEvent;
import ru.yandex.qatools.allure.events.StepStartedEvent;

import static org.junit.Assert.assertTrue;

@Title("This is Test A")
public class TestA {

    private static Allure lifecycle = Allure.LIFECYCLE;

    @Test
    @TestCaseId("1234")
    @Title("Test method title.")
    @Description("Test method description")
    public void aTest1() {
        someStep();
        assertTrue(true);
    }

    @Step("An annotated Step description here..")
    public void someStep() {
        System.out.println("Executing some step.");

        StepStartedEvent startedEvent = new StepStartedEvent("sampleStepInSomeStep").withTitle("Some step called from a test method.");

        lifecycle.fire(startedEvent);

        System.out.println("Doing something in between.");

        StepFinishedEvent finishedEvent = new StepFinishedEvent();

        lifecycle.fire(finishedEvent);
    }

    @Test
    public void aTest2() {
        assertTrue(true);
    }
}

