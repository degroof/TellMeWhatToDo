package com.stevedegroof.tellmewhattodo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a task with properties such as description, completion status,
 * priority, repeat settings, dependencies, and scheduling details.
 * <p>
 * Tasks can be one-time or repeating. Repeating tasks can be hourly, daily,
 * weekly, monthly, or yearly, with a specified interval.
 * </p>
 * <p>
 * Tasks can have dependencies on other tasks, meaning a task cannot be
 * considered available until its dependencies are met.
 * </p>
 * <p>
 * Scheduling details include specific times (minute), days of the month,
 * days of the week, and months, as well as time ranges (minMinute, maxMinute).
 * </p>
 * <p>
 * The class provides constants for repeat types, priorities, and special
 * values for date/time components (e.g., {@code ANY_DAY_OF_MONTH}, {@code LAST_DAY_OF_MONTH}).
 * </p>
 * <p>
 * Each task has a unique {@link UUID}.
 * </p>
 */

//TODO: Add a "snooze" feature (a snoozed task would not queue until unsnoozed)
//TODO: Possibly add a snooze interval (the task would requeue when the snooze interval has passed)
public class Task
{
    public static final int REPEAT_TYPE_NONE = 0;
    public static final int REPEAT_TYPE_HOURLY = 1;
    public static final int REPEAT_TYPE_DAILY = 2;
    public static final int REPEAT_TYPE_WEEKLY = 3;
    public static final int REPEAT_TYPE_MONTHLY = 4;
    public static final int REPEAT_TYPE_YEARLY = 5;

    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_HIGH = 4;
    public static final int PRIORITY_URGENT = 64;
    public static final int LAST_DAY_OF_MONTH = 32;
    public static final int ANY_DAY_OF_MONTH = 0;
    public static final int ANY_DAY_OF_WEEK = 0;
    public static final int ANY_MONTH = 0;
    public static final int ANY_TIME = -1;
    public static final int END_OF_DAY = 24 * 60 - 1;
    public static final int START_OF_DAY = 0;


    private String description = "";
    private boolean done = false;
    private int weight = PRIORITY_LOW;
    private int repeatType = REPEAT_TYPE_NONE;
    private int repeatInterval = 0;
    private UUID id;
    private final List<UUID> dependencies = new ArrayList<UUID>();
    private long lastRun = 0;
    private int minute = ANY_TIME;
    private int dayOfMonth = ANY_DAY_OF_MONTH;
    private int dayOfWeek = ANY_DAY_OF_WEEK;
    private int month = ANY_MONTH;
    private int minMinute = 0;
    private int maxMinute = 24 * 60 - 1;


    public Task()
    {
        this.id = UUID.randomUUID();
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isDone()
    {
        return done;
    }

    /**
     * Sets the done status of the task.
     * If the task is marked as done, its last run time is updated to the current time.
     * If the task is a repeating task, its done status is reset to false. (repeating tasks are never done)
     *
     * @param done {@code true} to mark the task as done, {@code false} otherwise.
     */
    public void setDone(boolean done)
    {
        this.done = done;
        if (done)
        {
            lastRun = System.currentTimeMillis();
            if (repeatType != REPEAT_TYPE_NONE) //if repeating, set done to false
            {
                this.done = false;
            }
        }
    }

    public UUID getId()
    {
        return id;
    }


    public int getRepeatInterval()
    {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval)
    {
        this.repeatInterval = repeatInterval;
    }

    public int getRepeatType()
    {
        return repeatType;
    }

    public void setRepeatType(int repeatType)
    {
        this.repeatType = repeatType;
    }

    public int getWeight()
    {
        return weight;
    }

    public void setWeight(int weight)
    {
        this.weight = weight;
    }

    public void addDependency(UUID id)
    {
        dependencies.add(id);
    }

    public void removeDependency(UUID id)
    {
        dependencies.remove(id);
    }

    /**
     * Retrieves a list of {@link Task} objects that this task depends on.
     * This method iterates through the UUIDs of the dependencies, fetches the corresponding
     * {@link Task} objects from the {@link Tasks} singleton, and adds them to a list.
     * If a dependency UUID does not correspond to an existing task (e.g., the task
     * might have been deleted), that dependency is removed from this task's list
     * of dependencies to maintain data integrity.
     *
     * @return A {@link List} of {@link Task} objects that this task depends on.
     *         Returns an empty list if there are no dependencies or if all
     *         dependencies are missing.
     */
    public List<Task> getDependencyTasks()
    {
        List<Task> tasks = new ArrayList<>();
        List<UUID> missingDependencies = new ArrayList<>();
        for (UUID dependency : dependencies)
        {
            Task task = Tasks.getInstance().getTask(dependency);
            if (task != null)
            {
                tasks.add(task);
            } else
            {
                missingDependencies.add(dependency); //add to list for cleanup
            }
        }
        for (UUID missingDependency : missingDependencies) //clean up missing dependencies
        {
            dependencies.remove(missingDependency);
        }
        return tasks;
    }


    /**
     * Determines if a task is currently available to be done.
     * A task is available if:
     * - It is not marked as 'done' (unless it's a repeating task).
     * - For repeating tasks, its due date/time has passed.
     * - All its dependencies are met:
     *   - Non-repeating dependencies must be marked as 'done'.
     *   - Repeating dependencies must have their last run time after this task's last run time.
     * This method also cleans up any missing dependencies (dependencies that no longer exist).
     *
     * @return {@code true} if the task is available, {@code false} otherwise.
     */
    public boolean isAvailable()
    {
        //TODO: Need to test all possible combinations of repeating anf non-repeating task dependencies
        boolean available = !done; //if done, not available
        if (repeatType != REPEAT_TYPE_NONE) //unless it repeats, then look at due date/time
        {
            long due = Util.getDueTime(this);
            long now = System.currentTimeMillis();
            available = (due <= now);
        }
        //check that all dependencies are done
        List<UUID> missingDependencies = new ArrayList<>();
        for (UUID dependency : dependencies)
        {
            if (Tasks.getInstance().getTask(dependency) == null)
            {
                missingDependencies.add(dependency);
            } else
            {
                Task depTask = Tasks.getInstance().getTask(dependency);
                if(depTask.getRepeatType() == REPEAT_TYPE_NONE) //dependency is non-repeating -> check done
                {
                    available = available && Tasks.getInstance().getTask(dependency).isDone();
                }
                else  //dependency is repeating -> check last run (repeating tasks are never done)
                {
                    //current last run is on or after dependency's last run, then not available
                    if(depTask.getLastRun() < lastRun) available = false;
                }
            }
        }
        dependencies.removeAll(missingDependencies);
        return available;
    }

    public long getLastRun()
    {
        return lastRun;
    }


    public int getMinute()
    {
        return minute;
    }

    public void setMinute(int minute)
    {
        this.minute = minute;
    }

    public int getDayOfWeek()
    {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek)
    {
        this.dayOfWeek = dayOfWeek;
    }

    public int getDayOfMonth()
    {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth)
    {
        this.dayOfMonth = dayOfMonth;
    }

    public int getMaxMinute()
    {
        return maxMinute;
    }

    public void setMaxMinute(int maxMinute)
    {
        this.maxMinute = maxMinute;
    }

    public int getMinMinute()
    {
        return minMinute;
    }

    public void setMinMinute(int minMinute)
    {
        this.minMinute = minMinute;
    }

    public void setMonth(int month)
    {
        this.month = month;
    }

    public int getMonth()
    {
        return month;
    }

    public void setLastRun(long lastRun)
    {
        this.lastRun = lastRun;
    }

    /**
     * Checks if this task is dependent on the given task.
     * This includes direct dependencies and recursive dependencies (dependencies of dependencies).
     * For these purposes, the task is also considered dependent on itself.
     *
     * @param task The task to check for dependency.
     * @return {@code true} if this task is dependent on the given task, {@code false} otherwise.
     */
    public boolean isDependentOn(Task task)
    {
        if (task == this) return true; //self dependency
        if (dependencies.contains(task.getId())) return true; //direct dependency
        for (Task dependency : getDependencyTasks()) //recursive dependency
        {
            if (dependency.isDependentOn(task))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a human-readable summary of the task's properties.
     * The summary includes the task description, priority, and repetition details (if any).
     *
     * @return A string containing the task summary.
     */
    public String getSummary()
    {
        String summary = "";
        if (description != null && !description.isEmpty())
        {
            summary += description + "\n";
        }
        switch (weight)
        {
            case PRIORITY_LOW:
                summary += "Priority: Low\n";
                break;
            case PRIORITY_MEDIUM:
                summary += "Priority: Medium\n";
                break;
            case PRIORITY_HIGH:
                summary += "Priority: High\n";
                break;
            case PRIORITY_URGENT:
                summary += "Priority: Urgent\n";
                break;
        }
        if (repeatType != REPEAT_TYPE_NONE)
        {
            switch (repeatType)
            {
                case REPEAT_TYPE_HOURLY:
                    summary += "Repeat: Every " + repeatInterval + " hour(s)\n";
                    if (minMinute != START_OF_DAY || maxMinute != END_OF_DAY)
                        summary += "Between " + Util.getTimeString(minMinute) + " and " + Util.getTimeString(maxMinute) + "\n";
                    break;
                case REPEAT_TYPE_DAILY:
                    summary += "Repeat: Every " + repeatInterval + " day(s)\n";
                    if (minute != ANY_TIME)
                        summary += "At " + Util.getTimeString(minute) + "\n";
                    break;
                case REPEAT_TYPE_WEEKLY:
                    summary += "Repeat: Every " + repeatInterval + " week(s)\n";
                    if (dayOfWeek != ANY_DAY_OF_WEEK)
                        summary += "On " + Util.getDayOfWeekName(dayOfWeek) + "\n";
                    if (minute != ANY_TIME)
                        summary += "At " + Util.getTimeString(minute) + "\n";
                    break;
                case REPEAT_TYPE_MONTHLY:
                    summary += "Repeat: Every " + repeatInterval + " month(s)\n";
                    if (dayOfMonth != ANY_DAY_OF_MONTH)
                        summary += "On the " + Util.getDayOfMonthNameShort(dayOfMonth) + "\n";
                    if (minute != ANY_TIME)
                        summary += "At " + Util.getTimeString(minute) + "\n";
                    break;
                case REPEAT_TYPE_YEARLY:
                    summary += "Repeat: Every " + repeatInterval + " year(s)\n";
                    if (dayOfMonth != ANY_DAY_OF_MONTH)
                        summary += "On the " + Util.getDayOfMonthNameShort(dayOfMonth) + "\n";
                    if (month != ANY_MONTH)
                        summary += "In " + Util.getMonthName(month) + "\n";
                    if (minute != ANY_TIME)
                        summary += "At " + Util.getTimeString(minute) + "\n";
                    break;
            }
        }
        return summary;
    }
}
