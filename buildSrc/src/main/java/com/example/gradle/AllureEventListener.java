package com.example.gradle;

import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import ru.yandex.qatools.allure.events.*;
import ru.yandex.qatools.allure.experimental.LifecycleListener;

public class AllureEventListener extends LifecycleListener {

    Logger logger = Logging.getLogger(this.getClass());

    public void fire(StepStartedEvent event) {
        logger.log(LogLevel.INFO, "Received step started event: " + event.getName());
    }

    public void fire(StepEvent event) {
        logger.log(LogLevel.INFO, "Received a step event: " + event.toString());
    }

    public void fire(StepFinishedEvent event) {
        logger.log(LogLevel.INFO, "Received step finished event.");
    }

    public void fire(TestCaseStartedEvent event) {
        logger.log(LogLevel.INFO, "Received test started event: " + event.getName() + "/" + event.getSuiteUid());
    }

    public void fire(TestCaseEvent event) { 
    }

    public void fire(TestCaseFinishedEvent event) { 
    }

    public void fire(TestSuiteEvent event) { 
    }

    public void fire(TestSuiteFinishedEvent event) { 
    }

    public void fire(ClearStepStorageEvent event) { 
    }

    public void fire(ClearTestStorageEvent event) { 
    }
    
}
