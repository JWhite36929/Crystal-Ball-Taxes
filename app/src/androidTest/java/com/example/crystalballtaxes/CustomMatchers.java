package com.example.crystalballtaxes;

import android.view.View;
import androidx.test.espresso.matcher.BoundedMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class CustomMatchers {

    public static Matcher<View> withMinimumVisibleTime(final long minimumMillis) {
        return new BoundedMatcher<View, View>(View.class) {
            private long startTime;

            @Override
            public void describeTo(Description description) {
                description.appendText("with minimum visible time: " + minimumMillis + "ms");
            }

            @Override
            protected boolean matchesSafely(View item) {
                if (startTime == 0) {
                    startTime = System.currentTimeMillis();
                    return true;
                }
                return System.currentTimeMillis() - startTime >= minimumMillis;
            }
        };
    }
}