package com.example;

import org.junit.Ignore;
import org.junit.Test;
import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Title;

import static org.junit.Assert.assertTrue;

@Title("This is Test B")
public class TestB {
    @Test
    public void testB1() {
        assertTrue(true);
    }
}

