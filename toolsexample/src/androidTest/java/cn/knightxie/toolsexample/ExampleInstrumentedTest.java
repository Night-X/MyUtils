package cn.knightxie.toolsexample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("cn.knightxie.toolsexample", appContext.getPackageName());

        Intent intent = new Intent(Intent.ACTION_MAIN);
        ComponentName componentName = new ComponentName("jd.video.basecomponent", "jd.video.promotion.PromotionModel");
        intent.setComponent(componentName);
        intent.putExtra("activityId", "346");
        appContext.startActivity(intent);
    }
}
