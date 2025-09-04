package com.stevedegroof.tellmewhattodo;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for common date and time operations, as well as task-related string conversions.
 * This class provides methods for:
 * <ul>
 *     <li>Getting names and integer representations of months, days of the week, and days of the month.</li>
 *     <li>Calculating the due time and default last run time for tasks.</li>
 *     <li>Converting task priority levels and repeat types to human-readable strings.</li>
 *     <li>Formatting time values into strings.</li>
 * </ul>
 */
public class Util
{
    private static final String[] MONTHS = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private static final String[] DAYS_OF_WEEK = new String[]{"Any Day", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    private static final String[] DAYS_OF_MONTH_SHORT = new String[]{"Any Day", "Last Day", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th", "20th", "21st", "22nd", "23rd", "24th", "25th", "26th", "27th", "28th"};
    private static final String[] DAYS_OF_MONTH_LONG = new String[]{"Any Day", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th", "20th", "21st", "22nd", "23rd", "24th", "25th", "26th", "27th", "28th", "29th", "30th","31st"};
    private static final int[] DAYS_OF_MONTH_SHORT_INT = new int[]{Task.ANY_DAY_OF_MONTH, Task.LAST_DAY_OF_MONTH, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28};
    private static final int[] DAYS_OF_MONTH_LONG_INT = new int[]{Task.ANY_DAY_OF_MONTH,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
    private static final String[] DAYS_OF_MONTH = new String[]{"Any Day", "Last Day", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th", "20th", "21st", "22nd", "23rd", "24th", "25th", "26th", "27th", "28th", "29th", "30th","31st"};
    private static final int[] DAYS_OF_MONTH_INT = new int[]{Task.ANY_DAY_OF_MONTH,Task.LAST_DAY_OF_MONTH, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};

    public static String getMonthName(int month)
    {
        return MONTHS[month];
    }

    public static String getDayOfWeekName(int dayOfWeek)
    {
        return DAYS_OF_WEEK[dayOfWeek];
    }

    public static String getDayOfMonthNameShort(int dayOfMonth)
    {
        return DAYS_OF_MONTH_SHORT[dayOfMonth];
    }

    public static String getDayOfMonthNameLong(int dayOfMonth)
    {
        return DAYS_OF_MONTH_LONG[dayOfMonth];
    }

    public static int getDayOfMonthIntShort(String dayOfMonth)
    {
        int index= Arrays.asList(DAYS_OF_MONTH_SHORT).indexOf(dayOfMonth);
        return DAYS_OF_MONTH_SHORT_INT[index];
    }

    public static int getDayOfMonthIntLong(String dayOfMonth)
    {
        int index= Arrays.asList(DAYS_OF_MONTH_LONG).indexOf(dayOfMonth);
        return DAYS_OF_MONTH_LONG_INT[index];
    }

    public static int getDayOfWeekInt(String dayOfWeek)
    {
        return Arrays.asList(DAYS_OF_WEEK).indexOf(dayOfWeek);
    }

    public static int getMonthInt(String month)
    {
        return Arrays.asList(MONTHS).indexOf(month)+1;
    }

    public static String[] getMonths()
    {
        return MONTHS;
    }

    public static String[] getDaysOfWeek()
    {
        return DAYS_OF_WEEK;
    }

    public static String[] getDaysOfMonthShort()
    {
        return DAYS_OF_MONTH_SHORT;
    }

    public static String[] getDaysOfMonthLong()
    {
        return DAYS_OF_MONTH_LONG;
    }

    public static long getDueTime(Task task)
    {
        Calendar due = Calendar.getInstance(); //start with current date and time
        if(task.getLastRun() > 0) due.setTimeInMillis(task.getLastRun()); //if there is a last run, use that instead
        int minute = (task.getMinute()==Task.ANY_TIME)?0:task.getMinute()%60;
        int hour = (task.getMinute()==Task.ANY_TIME)?0:task.getMinute()/60;
        int dayOfMonth = task.getDayOfMonth();
        int dayOfWeek = task.getDayOfWeek();
        switch (task.getRepeatType())
        {
            case Task.REPEAT_TYPE_HOURLY: //repeating 1 or more hours
                //add the number of hours
                due.add(Calendar.HOUR, task.getRepeatInterval());
                int dueMinute = due.get(Calendar.HOUR_OF_DAY)*60+due.get(Calendar.MINUTE);
                if (dueMinute > task.getMaxMinute()) //if after the end of the day, go to next day
                {
                    due.add(Calendar.DAY_OF_MONTH, 1);
                    due.set(Calendar.HOUR_OF_DAY, task.getMinMinute()/60);
                    due.set(Calendar.MINUTE, task.getMinMinute()%60);
                }
                else if(dueMinute < task.getMinMinute()) //if before the start of the day, set to start of day
                {
                    due.set(Calendar.HOUR_OF_DAY, task.getMinMinute()/60);
                    due.set(Calendar.MINUTE, task.getMinMinute()%60);
                }
                break;
            case Task.REPEAT_TYPE_DAILY: //repeating 1 or more days
                due.add(Calendar.DAY_OF_MONTH, task.getRepeatInterval());
                due.set(Calendar.HOUR_OF_DAY, hour);
                due.set(Calendar.MINUTE, minute);
                break;
            case Task.REPEAT_TYPE_WEEKLY: //repeating 1 or more weeks
                due.add(Calendar.WEEK_OF_YEAR, task.getRepeatInterval());
                if (dayOfWeek != Task.ANY_DAY_OF_WEEK) due.set(Calendar.DAY_OF_WEEK, task.getDayOfWeek());
                due.set(Calendar.HOUR_OF_DAY, hour);
                due.set(Calendar.MINUTE, minute);
                break;
            case Task.REPEAT_TYPE_MONTHLY: //repeating 1 or more months
                due.set(Calendar.DAY_OF_MONTH, 1);
                due.add(Calendar.MONTH, task.getRepeatInterval()); //add the number of months
                if (dayOfMonth == Task.LAST_DAY_OF_MONTH) //if last day of month, end of month
                    dayOfMonth = due.getActualMaximum(Calendar.DAY_OF_MONTH);
                else if (dayOfMonth != Task.ANY_DAY_OF_MONTH) //if not any day, use day
                    dayOfMonth = task.getDayOfMonth();
                due.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                due.set(Calendar.HOUR_OF_DAY, hour);
                due.set(Calendar.MINUTE, minute);
                break;
            case Task.REPEAT_TYPE_YEARLY: //repeating 1 or more years
                due.add(Calendar.YEAR, task.getRepeatInterval());
                due.set(Calendar.MONTH, task.getMonth());
                if (dayOfMonth == Task.LAST_DAY_OF_MONTH) //if last day of month, end of month
                    dayOfMonth = due.getActualMaximum(Calendar.DAY_OF_MONTH);
                else if (dayOfMonth != Task.ANY_DAY_OF_MONTH) //if not any day, use day
                    dayOfMonth = task.getDayOfMonth();
                due.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                due.set(Calendar.HOUR_OF_DAY, hour);
                due.set(Calendar.MINUTE, minute);
                break;
            default:
                break;
        }
        Date lastRunDate = new Date(task.getLastRun());
        return due.getTimeInMillis();
    }

    /**
     * Calculates the default last run time for a task.
     * This is used when a task is first created and doesn't have a last run time yet.
     * The default last run time is calculated based on the task's repeat type and interval.
     * For example, if a task repeats daily, the default last run time will be yesterday.
     * If the calculated last run time is in the future, it will be adjusted to be in the past.
     * If the task is non-repeating, the default last run time will be the current time.
     *
     * @param task The task for which to calculate the default last run time.
     * @return The default last run time in milliseconds.
     */
    public static long getDefaultLastRun(Task task)
    {
        Calendar lastRun = Calendar.getInstance();
        long now = System.currentTimeMillis();
        int interval = task.getRepeatInterval();
        switch (task.getRepeatType())
        {
            case Task.REPEAT_TYPE_HOURLY:
                break;
            case Task.REPEAT_TYPE_DAILY:
                if(task.getMinute()!=Task.ANY_TIME)
                {
                    lastRun.set(Calendar.HOUR_OF_DAY, task.getMinute()/60);
                    lastRun.set(Calendar.MINUTE, task.getMinute()%60);
                    lastRun.add(Calendar.DAY_OF_MONTH, ((lastRun.getTimeInMillis() > now)?0:1)-interval);
                }
                break;
            case Task.REPEAT_TYPE_WEEKLY:
                if(task.getMinute()!=Task.ANY_TIME)
                {
                    lastRun.set(Calendar.HOUR_OF_DAY, task.getMinute()/60);
                    lastRun.set(Calendar.MINUTE, task.getMinute()%60);
                }
                if(task.getDayOfWeek()!=Task.ANY_DAY_OF_WEEK)
                {
                    lastRun.set(Calendar.DAY_OF_WEEK, task.getDayOfWeek());
                }
                lastRun.add(Calendar.WEEK_OF_YEAR, ((lastRun.getTimeInMillis() > now)?0:1)-interval);
                break;
            case Task.REPEAT_TYPE_MONTHLY:
                if(task.getMinute()!=Task.ANY_TIME)
                {
                    lastRun.set(Calendar.HOUR_OF_DAY, task.getMinute()/60);
                    lastRun.set(Calendar.MINUTE, task.getMinute()%60);
                }
                if(task.getDayOfMonth()!=Task.ANY_DAY_OF_MONTH)
                {
                    lastRun.set(Calendar.DAY_OF_MONTH, task.getDayOfMonth());
                }
                lastRun.add(Calendar.MONTH, ((lastRun.getTimeInMillis() > now)?0:1)-interval);
                break;
            case Task.REPEAT_TYPE_YEARLY:
                if(task.getMinute()!=Task.ANY_TIME)
                {
                    lastRun.set(Calendar.HOUR_OF_DAY, task.getMinute()/60);
                    lastRun.set(Calendar.MINUTE, task.getMinute()%60);
                }
                if(task.getDayOfMonth()!=Task.ANY_DAY_OF_MONTH)
                {
                    lastRun.set(Calendar.DAY_OF_MONTH, task.getDayOfMonth());
                }
                if(task.getMonth()!=Task.ANY_MONTH)
                {
                    lastRun.set(Calendar.MONTH, task.getMonth());
                }
                lastRun.add(Calendar.YEAR, ((lastRun.getTimeInMillis() > now)?0:1)-interval);
                break;
            default:
                break;
        }
        return lastRun.getTimeInMillis();
    }


    /**
     * Gets a human-readable string representation of a task's priority level.
     *
     * @param weight The integer representation of the task's priority (e.g., Task.PRIORITY_LOW).
     * @return A string like "Low", "Medium", "High", or "Urgent", or an empty string if the weight is not recognized.
     */
    public static String getPriorityString(int weight)
    {
        switch (weight)
        {
            case Task.PRIORITY_LOW:
                return "Low";
                case Task.PRIORITY_MEDIUM:
                return "Medium";
            case Task.PRIORITY_HIGH:
                return "High";
            case Task.PRIORITY_URGENT:
                return "Urgent";
        }
        return "";
    }

    /**
     * Gets the string representation of a repeat type.
     *
     * @param repeatType The repeat type, as defined in {@link Task}.
     * @return The string representation of the repeat type, or an empty string if the repeat type is invalid.
     */
    public static String getRepeatTypeString(int repeatType)
    {
        switch (repeatType)
        {
            case Task.REPEAT_TYPE_HOURLY:
                return "Hour(s)";
            case Task.REPEAT_TYPE_DAILY:
                return "Day(s)";
            case Task.REPEAT_TYPE_WEEKLY:
                return "Week(s)";
            case Task.REPEAT_TYPE_MONTHLY:
                return "Month(s)";
            case Task.REPEAT_TYPE_YEARLY:
                return "Year(s)";
        }
        return "";
    }

    /**
     * Converts minutes since midnight to a time string (e.g., "09:30 AM").
     *
     * @param minutes The number of minutes since midnight. If this is {@link Task#ANY_TIME},
     *                "Set Time" will be returned.
     * @return A string representation of the time in "hh:mm AM/PM" format, or "Set Time".
     */
    public static String getTimeString(int minutes)
    {
        if (minutes == Task.ANY_TIME) return "Set Time";
        int hour = minutes/60;
        int minute = minutes%60;
        String ap = (hour < 12) ? "AM" : "PM";
        if (hour > 12) hour -= 12;
        return String.format("%02d:%02d %s", hour, minute, ap);

    }

    /**
     * Converts a string representation of a day of the month to its integer equivalent.
     * For example, "1st" becomes 1, "Last Day" becomes {@link Task#LAST_DAY_OF_MONTH}.
     *
     * @param string The string representation of the day of the month.
     * @return The integer representation of the day of the month.
     */
    public static int getDayOfMonthInt(String string)
    {
        int index= Arrays.asList(DAYS_OF_MONTH).indexOf(string);
        return DAYS_OF_MONTH_INT[index];
    }
}
