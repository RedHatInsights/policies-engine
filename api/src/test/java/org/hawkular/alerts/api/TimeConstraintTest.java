package org.hawkular.alerts.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.TimeZone;

import org.hawkular.alerts.api.model.action.TimeConstraint;
import org.junit.Test;

/**
 * @author Jay Shaughnessy
 * @author Lucas Ponce
 */
public class TimeConstraintTest {

    Calendar cal = Calendar.getInstance();
    long timestamp;

    @Test
    public void absoluteTest() {
        TimeConstraint tc = new TimeConstraint("2016.02.01", "2016.02.03", false);

        cal.set(2016, Calendar.FEBRUARY, 1, 0, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 2, 23, 59);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.JANUARY, 31, 23, 59);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 3, 0, 1);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        tc.setStartTime("2016.02.01,03:00");
        tc.setEndTime("2016.02.03,04:34");

        cal.set(2016, Calendar.FEBRUARY, 1, 0, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 3, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 3, 4, 33);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 3, 4, 35);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        // range == false

        tc.setStartTime("2016.02.01,03:00");
        tc.setEndTime("2016.02.03,04:34");
        tc.setInRange(false);

        cal.set(2016, Calendar.FEBRUARY, 1, 0, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 3, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 3, 4, 33);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 3, 4, 35);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 3, 4, 35);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        // tz
        tc.setStartTime("2016.02.03,10:00");
        tc.setEndTime("2016.02.03,18:00");
        tc.setInRange(true);

        Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmtCal.set(2016, Calendar.FEBRUARY, 3, 16, 0);
        timestamp = gmtCal.getTimeInMillis();

        tc.setTimeZoneName("GMT");
        assertTrue(tc.isSatisfiedBy(timestamp));

        tc.setTimeZoneName("GMT-4:00"); // GMT 14:00-22:00
        assertTrue(tc.isSatisfiedBy(timestamp));

        tc.setTimeZoneName("GMT+4:00"); // GMT 06:00-14:00
        assertFalse(tc.isSatisfiedBy(timestamp));
    }

    @Test
    public void relativeTest() {
        TimeConstraint tc = new TimeConstraint("10:00", "13:00");

        cal.set(2016, Calendar.FEBRUARY, 1, 10, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 14, 13, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 9, 59);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.MARCH, 1, 13, 1);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        tc.setStartTime("Feb,10:00");
        tc.setEndTime("Mar,13:00");

        cal.set(2016, Calendar.FEBRUARY, 1, 10, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 14, 12, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.MARCH, 1, 13, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 9, 59);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.MARCH, 1, 13, 1);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.JULY, 14, 12, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        tc.setStartTime("Mon,09:00");
        tc.setEndTime("Fri,18:00");

        cal.set(2016, Calendar.FEBRUARY, 1, 9, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 5, 18, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 8, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 5, 18, 1);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 8, 9, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 12, 18, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        tc.setStartTime("Jul,Mon,09:00");
        tc.setEndTime("Aug,Fri,18:00");

        cal.set(2016, Calendar.JULY, 18, 9, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.AUGUST, 26, 18, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.JULY, 18, 8, 59);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.AUGUST, 26, 18, 1);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.JULY, 17, 9, 1);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.AUGUST, 27, 18, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        // range == false

        tc.setStartTime("10:00");
        tc.setEndTime("13:00");
        tc.setInRange(false);

        cal.set(2016, Calendar.FEBRUARY, 1, 10, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 14, 13, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 9, 59);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.MARCH, 1, 13, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        tc.setStartTime("Feb,10:00");
        tc.setEndTime("Mar,13:00");

        cal.set(2016, Calendar.FEBRUARY, 1, 10, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 14, 12, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.MARCH, 1, 13, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 9, 59);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.MARCH, 1, 13, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.JULY, 14, 12, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        tc.setStartTime("Mon,09:00");
        tc.setEndTime("Fri,18:00");

        cal.set(2016, Calendar.FEBRUARY, 1, 9, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 5, 18, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 8, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 5, 18, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 8, 9, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 12, 18, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        tc.setStartTime("Jul,Mon,09:00");
        tc.setEndTime("Aug,Fri,18:00");

        cal.set(2016, Calendar.JULY, 18, 9, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.AUGUST, 26, 18, 0);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.JULY, 18, 8, 59);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.AUGUST, 26, 18, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.JULY, 17, 9, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.AUGUST, 27, 18, 0);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        // tz
        tc.setStartTime("Jul,Mon,02:00");
        tc.setEndTime("Jul,Fri,18:00");
        tc.setInRange(true);

        Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmtCal.set(2016, Calendar.JULY, 18, 16, 0); // Monday 16:00
        timestamp = gmtCal.getTimeInMillis();

        tc.setTimeZoneName("GMT");
        assertTrue(tc.isSatisfiedBy(timestamp));  // Monday 16:00

        tc.setTimeZoneName("GMT-4:00");
        assertTrue(tc.isSatisfiedBy(timestamp));  // Monday 12:00

        tc.setTimeZoneName("GMT+4:00");
        assertFalse(tc.isSatisfiedBy(timestamp)); // Monday 20:00

        gmtCal.set(2016, Calendar.JULY, 17, 16, 0); // Sunday 16:00
        timestamp = gmtCal.getTimeInMillis();

        tc.setTimeZoneName("GMT");
        assertFalse(tc.isSatisfiedBy(timestamp)); // Sunday 16:00

        tc.setTimeZoneName("GMT-4:00");
        assertFalse(tc.isSatisfiedBy(timestamp)); // Sunday 12:00

        tc.setTimeZoneName("GMT+4:00");
        assertFalse(tc.isSatisfiedBy(timestamp)); // Sunday 20:00

        tc.setTimeZoneName("GMT+10:00");
        assertTrue(tc.isSatisfiedBy(timestamp));  // Monday 02:00
    }

    @Test
    public void inverseIntervals()  {

        TimeConstraint tc = new TimeConstraint("23:00", "01:00");

        cal.set(2016, Calendar.FEBRUARY, 1, 23, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 0, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 0, 59);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.FEBRUARY, 1, 22, 59);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        tc.setStartTime("Nov");
        tc.setEndTime("Feb");

        cal.set(2016, Calendar.NOVEMBER, 1, 23, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.JANUARY, 1, 23, 1);
        timestamp = cal.getTimeInMillis();
        assertTrue(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.OCTOBER, 1, 22, 59);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));

        cal.set(2016, Calendar.MARCH, 1, 22, 59);
        timestamp = cal.getTimeInMillis();
        assertFalse(tc.isSatisfiedBy(timestamp));
    }

    @Test
    public void handleErrors() {

        // Null / empty arguments
        assertThrows(IllegalArgumentException.class, () -> new TimeConstraint(null, null), "It should fail with null arguments");
        assertThrows(IllegalArgumentException.class, () -> new TimeConstraint("", ""),"It should fail with empty arguments" );
        assertThrows(IllegalArgumentException.class, () -> {
            TimeConstraint tc = new TimeConstraint();
            tc.setStartTime(null);},
                "It should fail with startTime null arguments");
        assertThrows(IllegalArgumentException.class, () -> {
            TimeConstraint tc = new TimeConstraint();
            tc.setStartTime("");},
                "It should fail with startTime empty arguments");
        assertThrows(IllegalArgumentException.class,() -> {
            TimeConstraint tc = new TimeConstraint();
            tc.setEndTime(null);},
                "It should fail with endTime null arguments");
        assertThrows(IllegalArgumentException.class,() -> {
            TimeConstraint tc = new TimeConstraint();
            tc.setEndTime("");},
                "It should fail with endTime empty arguments");
        // Bad formats
        assertThrows(IllegalArgumentException.class,() -> new TimeConstraint("badformat", "badformat"), "It should fail with bad formats");
        assertThrows(IllegalArgumentException.class,() -> new TimeConstraint("badformat", "badformat", false), "It should fail with bad formats");

    }

}
