package org.traccar.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class DateBuilder {
    private Calendar calendar;

    public DateBuilder() {
        this(TimeZone.getTimeZone("UTC"));
    }

    public DateBuilder(Date time) {
        this(time, TimeZone.getTimeZone("UTC"));
    }

    public DateBuilder(TimeZone timeZone) {
        this(new Date(0L), timeZone);
    }

    public DateBuilder(Date time, TimeZone timeZone) {
        this.calendar = Calendar.getInstance(timeZone);
        this.calendar.clear();
        this.calendar.setTimeInMillis(time.getTime());
    }

    public DateBuilder setYear(int year) {
        if (year < 100) {
            year += 2000;
        }
        this.calendar.set(1, year);
        return this;
    }

    public DateBuilder setMonth(int month) {
        this.calendar.set(2, month - 1);
        return this;
    }

    public DateBuilder setDay(int day) {
        this.calendar.set(5, day);
        return this;
    }

    public DateBuilder setDate(int year, int month, int day) {
        return setYear(year).setMonth(month).setDay(day);
    }

    public DateBuilder setDateReverse(int day, int month, int year) {
        return setDate(year, month, day);
    }

    public DateBuilder setCurrentDate() {
        Calendar now = Calendar.getInstance(this.calendar.getTimeZone());
        return setYear(now.get(1))
                .setMonth(now.get(2) + 1)
                .setDay(now.get(5));
    }

    public DateBuilder setHour(int hour) {
        this.calendar.set(11, hour);
        return this;
    }

    public DateBuilder setMinute(int minute) {
        this.calendar.set(12, minute);
        return this;
    }

    public DateBuilder addMinute(int minute) {
        this.calendar.add(12, minute);
        return this;
    }

    public DateBuilder setSecond(int second) {
        this.calendar.set(13, second);
        return this;
    }

    public DateBuilder addSeconds(long seconds) {
        this.calendar.setTimeInMillis(this.calendar.getTimeInMillis() + seconds * 1000L);
        return this;
    }

    public DateBuilder setMillis(int millis) {
        this.calendar.set(14, millis);
        return this;
    }

    public DateBuilder addMillis(long millis) {
        this.calendar.setTimeInMillis(this.calendar.getTimeInMillis() + millis);
        return this;
    }

    public DateBuilder setTime(int hour, int minute, int second) {
        return setHour(hour).setMinute(minute).setSecond(second);
    }

    public DateBuilder setTimeReverse(int second, int minute, int hour) {
        return setHour(hour).setMinute(minute).setSecond(second);
    }

    public DateBuilder setTime(int hour, int minute, int second, int millis) {
        return setHour(hour).setMinute(minute).setSecond(second).setMillis(millis);
    }

    public Date getDate() {
        return this.calendar.getTime();
    }
}


/* Location:              C:\User\\user\Documents\Ensurity Mobile [Client]\Latest App\traccar\tracker-server.jar!\org\traccar\helper\DateBuilder.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */