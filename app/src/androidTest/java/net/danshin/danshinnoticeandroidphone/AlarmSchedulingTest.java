package net.danshin.danshinnoticeandroidphone;

import static org.junit.Assert.assertNotNull;

import android.app.AlarmManager;
import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalTime;
import java.time.ZoneId;

@RunWith(AndroidJUnit4.class)
public class AlarmSchedulingTest {

    @Test
    public void scheduleNext_doesNotThrow() {
        Context context = ApplicationProvider.getApplicationContext();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assertNotNull(am);

        App.scheduleNext(context, LocalTime.of(9, 0), ZoneId.of("Europe/Moscow"));
    }
}
