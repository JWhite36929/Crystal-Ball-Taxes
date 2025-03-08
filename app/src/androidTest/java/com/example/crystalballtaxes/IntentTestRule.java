package com.example.crystalballtaxes;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class IntentTestRule<T extends Activity> implements TestRule {
    private final Class<T> activityClass;
    private Instrumentation instrumentation;
    private T activity;

    public IntentTestRule(Class<T> activityClass) {
        this.activityClass = activityClass;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                instrumentation = InstrumentationRegistry.getInstrumentation();
                base.evaluate();
            }
        };
    }

    public T launchActivity(Intent intent) {
        activity = (T) instrumentation.startActivitySync(intent);
        instrumentation.waitForIdleSync();
        return activity;
    }

    public T getActivity() {
        return activity;
    }
}