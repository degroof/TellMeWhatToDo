package com.stevedegroof.tellmewhattodo;

import android.content.Context;

import com.google.gson.Gson;

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

    private static final int[] DAYS_OF_MONTH_SHORT_INT = new int[]{Task.ANY_DAY_OF_MONTH, Task.LAST_DAY_OF_MONTH, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28};
    private static final int[] DAYS_OF_MONTH_LONG_INT = new int[]{Task.ANY_DAY_OF_MONTH,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};
     private static final int[] DAYS_OF_MONTH_INT = new int[]{Task.ANY_DAY_OF_MONTH,Task.LAST_DAY_OF_MONTH, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31};

     private static final int[] PRIORITY_INT = new int[]{Task.PRIORITY_LOW, Task.PRIORITY_MEDIUM, Task.PRIORITY_HIGH, Task.PRIORITY_URGENT};
    private static final int[] REPEAT_TYPE_INT = new int[]{Task.REPEAT_TYPE_HOURLY, Task.REPEAT_TYPE_DAILY, Task.REPEAT_TYPE_WEEKLY, Task.REPEAT_TYPE_MONTHLY, Task.REPEAT_TYPE_YEARLY};

    public static String getMonthName(Context context, int month)
    {
        if (month == 0) return null;
        return context.getResources().getStringArray(R.array.months_array)[month-1];
    }

    public static String getDayOfWeekName(Context context, int dayOfWeek)
    {
        return context.getResources().getStringArray(R.array.days_of_week_array)[dayOfWeek];
    }

    public static String getDayOfMonthNameShort(Context context, int dayOfMonth)
    {
        int index=-1;
        for(int i=0;i<DAYS_OF_MONTH_SHORT_INT.length;i++)
        {
            if(DAYS_OF_MONTH_SHORT_INT[i]==dayOfMonth)
            {
                index=i;
                break;
            }
        }
        return context.getResources().getStringArray(R.array.days_of_month_short_array)[index];
    }

    public static String getDayOfMonthNameLong(Context context, int dayOfMonth)
    {
        int index=-1;
        for(int i=0;i<DAYS_OF_MONTH_LONG_INT.length;i++)
        {
            if(DAYS_OF_MONTH_LONG_INT[i]==dayOfMonth)
            {
                index=i;
                break;
            }
        }
        return context.getResources().getStringArray(R.array.days_of_month_long_array)[index];
    }

    public static int getDayOfMonthIntShort(Context context, String dayOfMonth)
    {
        int index= Arrays.asList(context.getResources().getStringArray(R.array.days_of_month_short_array)).indexOf(dayOfMonth);
        return DAYS_OF_MONTH_SHORT_INT[index];
    }

    public static int getDayOfMonthIntLong(Context context, String dayOfMonth)
    {
        int index= Arrays.asList(context.getResources().getStringArray(R.array.days_of_month_long_array)).indexOf(dayOfMonth);
        return DAYS_OF_MONTH_LONG_INT[index];
    }

    public static int getDayOfWeekInt(Context context, String dayOfWeek)
    {
        return Arrays.asList(context.getResources().getStringArray(R.array.days_of_week_array)).indexOf(dayOfWeek);
    }

    public static int getMonthInt(Context context, String month)
    {
        return Arrays.asList(context.getResources().getStringArray(R.array.months_array)).indexOf(month)+1;
    }

    public static String[] getMonths(Context context)
    {
        return context.getResources().getStringArray(R.array.months_array);
    }

    public static String[] getDaysOfWeek(Context context)
    {
        return context.getResources().getStringArray(R.array.days_of_week_array);
    }

    public static String[] getDaysOfMonthShort(Context context)
    {
        return context.getResources().getStringArray(R.array.days_of_month_short_array);
    }

    public static String[] getDaysOfMonthLong(Context context)
    {
        return context.getResources().getStringArray(R.array.days_of_month_long_array);
    }

    public static long getDueTime(Context context, Task task)
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
    public static long getDefaultLastRun(Context context, Task task)
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
     * @return A string representing the priority, or an empty string if the weight is not recognized.
     */
    public static String getPriorityString(Context context, int weight)
    {
        switch (weight)
        {
            case Task.PRIORITY_LOW:
                return context.getString(R.string.Low); 
            case Task.PRIORITY_MEDIUM:
                return context.getString(R.string.Medium);
            case Task.PRIORITY_HIGH:
                return context.getString(R.string.High);
            case Task.PRIORITY_URGENT:
                return context.getString(R.string.Urgent);
        }
        return "";
    }

    /**
     * Gets the string representation of a repeat type.
     *
     * @param repeatType The repeat type, as defined in {@link Task}.
     * @return The string representation of the repeat type, or an empty string if the repeat type is invalid.
     */
    public static String getRepeatTypeString(Context context, int repeatType)
    {
        switch (repeatType)
        {
            case Task.REPEAT_TYPE_HOURLY:
                return context.getString(R.string.repeat_hours);
            case Task.REPEAT_TYPE_DAILY:
                return context.getString(R.string.repeat_days);
            case Task.REPEAT_TYPE_WEEKLY:
                return context.getString(R.string.repeat_weeks);
            case Task.REPEAT_TYPE_MONTHLY:
                return context.getString(R.string.repeat_months);
            case Task.REPEAT_TYPE_YEARLY:
                return context.getString(R.string.repeat_years);
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
    public static String getTimeString(Context context, int minutes)
    {
        if (minutes == Task.ANY_TIME) return context.getString(R.string.set_time);
        int hour = minutes/60;
        int minute = minutes%60;
        String ap = (hour < 12) ? context.getString(R.string.am) : context.getString(R.string.pm);
        if (hour > 12) hour -= 12;
        if (hour == 0) hour = 12;
        return String.format("%02d:%02d %s", hour, minute, ap);
    }

    /**
     * Converts a string representation of a day of the month to its integer equivalent.
     * For example, "1st" becomes 1, "Last Day" becomes {@link Task#LAST_DAY_OF_MONTH}.
     *
     * @param string The string representation of the day of the month.
     * @return The integer representation of the day of the month.
     */
    public static int getDayOfMonthInt(Context context, String string)
    {
        int index= Arrays.asList(context.getResources().getStringArray(R.array.days_of_month_array)).indexOf(string);
        return DAYS_OF_MONTH_INT[index];
    }

    /**
     * Converts a time string (e.g., "09:30 AM") to minutes since midnight.
     *
     * @param time The time string in "hh:mm AM/PM" format.
     * @return The number of minutes since midnight.
     */
    public static int getTimeInt(Context context, String time) {
        String[] parts = time.split("[:\\s]");
        int hour = Integer.parseInt(parts[0]);
        if (time.endsWith(context.getString(R.string.pm)) && hour != 12) hour += 12;
        else if (time.endsWith(context.getString(R.string.am)) && hour == 12) hour = 0;
        int minute = Integer.parseInt(parts[1]);
        return hour * 60 + minute;
    }

    /**
     * Validates a JSON string representing a list of tasks and extracts their descriptions.
     *
     * This method first checks if the JSON string contains the key `"tasks":`.
     * If not, or if any other parsing error occurs, it throws an {@link IllegalArgumentException}.
     * Otherwise, it parses the JSON into a {@link Tasks} object and concatenates the
     * descriptions of all tasks, separated by "||".
     *
     * @param json The JSON string to validate and parse.
     * @return A string containing all task descriptions, separated by "||".
     * @throws IllegalArgumentException If the JSON string is invalid or does not contain the "tasks" key.
     */
    public static String validateJson(Context context, String json)
    {
        try
        {
            if (!json.contains("\"tasks\":"))
            {
                throw new IllegalArgumentException("Invalid JSON");
            }
            Tasks tasks = new Gson().fromJson(json, Tasks.class);
            String descriptions = "";
            for (Task task : tasks.getTasks())
                descriptions += task.getDescription() + "||";
            return descriptions;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Invalid JSON");
        }
    }

    public static int getPriorityInt(Context context,String priority)
    {
        int index= Arrays.asList(context.getResources().getStringArray(R.array.priority_array)).indexOf(priority);
        return PRIORITY_INT[index];
    }

    public static int getRepeatTypeInt(Context applicationContext, String repeatType) {
        int index= Arrays.asList(applicationContext.getResources().getStringArray(R.array.repeat_type_array)).indexOf(repeatType);
        return Util.REPEAT_TYPE_INT[index];
    }
}
