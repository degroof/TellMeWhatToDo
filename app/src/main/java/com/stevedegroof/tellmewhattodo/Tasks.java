package com.stevedegroof.tellmewhattodo;

import android.content.Context;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Represents a collection of tasks.
 * This class is a singleton.
 * It provides methods for adding, retrieving, removing, and managing tasks.
 * Tasks can be loaded from and saved to a JSON file.
 */
public class Tasks
{
    private HashMap<UUID, Task> tasks = new HashMap<>();
    private UUID currentTaskId = null;

    private static Tasks instance;

    private static final String TASKS_FILE_NAME = "tasks.json";

    public static Tasks getInstance()
    {
        if (instance == null)
        {
            instance = new Tasks();
        }
        return instance;
    }

    public void putTask(Task task)
    {
        tasks.put(task.getId(), task);
    }

    public Task getTask(UUID id)
    {
        return tasks.get(id);
    }

    public void removeTask(UUID id)
    {
        tasks.remove(id);
    }

    public List<Task> getTasks()
    {
        return new ArrayList<>(this.tasks.values());
    }

    public List<Task> getAvailableTasks()
    {
        List<Task> availableTasks = new ArrayList<>();
        for (Task task : tasks.values())
        {
            if (task.isAvailable())
                availableTasks.add(task);
        }
        return availableTasks;
    }

    /**
     * Gets the next task to be performed based on the priority levels of the available tasks.
     * The higher the priority level of a task, the more likely it is to be selected.
     * If no tasks are available, returns null.
     *
     * @return The next task to be performed, or null if no tasks are available.
     */
    public Task getNextTask()
    {
        List<Task> availableTasks = getAvailableTasks();
        if (!availableTasks.isEmpty())
        {
            //get the total of all weights
            int totalWeight = 0;
            for (Task task : availableTasks)
            {
                totalWeight += task.getWeight();
            }
            //get a random number between 0 and the total weight
            int randomWeight = (int) (Math.random() * totalWeight);
            //get the task with the random weight
            int currentWeight = 0;
            for (Task task : availableTasks)
            {
                currentWeight += task.getWeight();
                if (currentWeight >= randomWeight)
                {
                    currentTaskId = task.getId();
                    return task;
                }
            }
        }
        currentTaskId = null;
        return null; //no tasks available
    }

    public UUID getCurrentTaskId()
    {
        return currentTaskId;
    }

    public void setCurrentTaskId(UUID currentTaskId)
    {
        this.currentTaskId = currentTaskId;
    }

    /**
     * Loads tasks from a JSON file.
     * The tasks are stored in a file named "tasks.json" in the application's private storage.
     * If the file does not exist or is empty, no tasks are loaded.
     *
     * @param ctx The context to use for accessing the file system.
     */
    public void load(Context ctx)
    {
        Gson gson = new Gson();
        String json = "";
        tasks.clear();
        StringBuilder sb;
        try
        {
            FileInputStream fis = ctx.openFileInput(TASKS_FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                sb.append(line);
            }
            json = sb.toString();
            bufferedReader.close();
            if (!json.isEmpty())
            {
                Tasks obj = gson.fromJson(json, this.getClass());
                this.tasks = obj.tasks;
                this.currentTaskId = obj.currentTaskId;
            }
        } catch (Exception e)
        {
        }
    }


    /**
     * Saves the current state of tasks to a file.
     * <p>
     * This method serializes the Tasks object into a JSON string using Gson
     * and writes it to a private file named "tasks.json" within the application's
     * internal storage.
     * </p>
     *
     * @param ctx The context used to access file operations.
     */
    public void save(Context ctx)
    {
        FileOutputStream outputStream;

        final Gson gson = new Gson();
        String serializedRecipes = gson.toJson(this, this.getClass());

        try
        {
            outputStream = ctx.openFileOutput(TASKS_FILE_NAME, Context.MODE_PRIVATE);
            outputStream.write(serializedRecipes.getBytes());
            outputStream.close();
        } catch (Exception e)
        {
        }
    }
}
