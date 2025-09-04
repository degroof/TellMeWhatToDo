package com.stevedegroof.tellmewhattodo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The main activity of the "Tell Me What To Do" application.
 * This activity serves as the primary user interface for interacting with tasks.
 * It allows users to:
 * - View the current task.
 * - Request a new task ("Tell me what to do").
 * - Mark the current task as done.
 * - Navigate to add a new task.
 * - Navigate to view all tasks.
 *
 * The activity also includes logic to periodically check for repeating tasks
 * that may have become available and updates the UI accordingly.
 * It ensures the application is displayed in portrait mode on certain devices.
 */
public class MainActivity extends AppCompatActivity
{

    private TextView currentTaskDescription;
    private Button buttonTell;
    private Button buttonDone;
    private Button buttonAdd;
    private Button buttonView;

    ScheduledExecutorService service;
    Handler handler;

    /**
     * Called when the activity is first created.
     * This is where you should do all of your normal static set up: create views, bind data to lists, etc.
     * This method also provides you with a Bundle containing the activity's previously frozen state, if there was one.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Force portrait mode for some phone models
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        int hdp = (int) (height / displayMetrics.density);
        double aspectRatio = (double) width / (double) height;
        if (width > height && aspectRatio > 1.5 && hdp < 600)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //Initialize UI elements
        currentTaskDescription = findViewById(R.id.current_task_description);
        buttonTell = findViewById(R.id.button_tell);
        buttonDone = findViewById(R.id.button_yes);
        buttonAdd = findViewById(R.id.button_add);
        buttonView = findViewById(R.id.button_view);


        buttonTell.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonTellClick(v);
            }
        });

        buttonDone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonDoneClick(v);
            }
        });

        buttonAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonAddClick(v);
            }
        });

        buttonView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                buttonViewClick(v);
            }
        });


        updateUI();


        //Periodically check for repeating tasks becoming available
        service = Executors.newSingleThreadScheduledExecutor();
        handler = new Handler(Looper.getMainLooper());

        service.scheduleWithFixedDelay(() -> {
            handler.post(() ->
            {
                if(Tasks.getInstance().getCurrentTaskId() == null  || Tasks.getInstance().getTask(Tasks.getInstance().getCurrentTaskId()) == null || Tasks.getInstance().getTask(Tasks.getInstance().getCurrentTaskId()).isDone())
                {
                    updateUI();
                }
            });
        }, 0, 5, TimeUnit.MINUTES);


    }


    /**
     * Handles the click event of the "Tell me what to do" button.
     * Retrieves a random available task, displays its description,
     * saves the current state of tasks, and updates the UI.
     *
     * @param view The view that was clicked (the "Tell me what to do" button).
     */
    public void buttonTellClick(View view)
    {
        Task currentTask = Tasks.getInstance().getNextTask();
        Tasks.getInstance().save(this);
        if (currentTask != null)
        {
            currentTaskDescription.setText(currentTask.getDescription());
        }
        Tasks.getInstance().save(this);
        updateUI();
    }

    /**
     * Handles the "Done" button click event.
     * Marks the current task as done, clears the current task ID, saves the tasks, and updates the UI.
     *
     * @param view The view that was clicked (the "Done" button).
     */
    public void buttonDoneClick(View view)
    {
        UUID currentTaskId = Tasks.getInstance().getCurrentTaskId();
        Task currentTask = Tasks.getInstance().getTask(currentTaskId);
        currentTask.setDone(true);
        Tasks.getInstance().setCurrentTaskId(null);
        Tasks.getInstance().save(this);
        updateUI();
    }

    /**
     * Handles the click event for the "Add Task" button.
     * Launches the {@link AddEditTaskActivity} to allow the user to add a new task.
     *
     * @param view The view that was clicked (the "Add Task" button).
     */
    public void buttonAddClick(View view)
    {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        startActivity(intent);
    }

    /**
     * Handles the click event for the "View Tasks" button.
     * This method creates an Intent to start the ViewTasksActivity, which displays a list of all tasks.
     *
     * @param view The view that was clicked (the "View Tasks" button).
     */
    public void buttonViewClick(View view)
    {
        Intent intent = new Intent(this, ViewTasksActivity.class);
        startActivity(intent);
    }


    /**
     * Updates the UI based on the current task status.
     * <p>
     * This method checks if there is a current task and whether it is done.
     * It then updates the visibility of the "Done" and "Tell me what to do" buttons
     * and sets the text of the current task description accordingly.
     * </p>
     * <p>
     * If there is a current task that is not done:
     * <ul>
     *     <li>The task description is displayed.</li>
     *     <li>The "Done" button is visible.</li>
     *     <li>The "Tell me what to do" button is hidden.</li>
     * </ul>
     * If there is no current task but tasks are available:
     * <ul>
     *     <li>The "Done" button is hidden.</li>
     *     <li>The "Tell me what to do" button is visible.</li>
     *     <li>A message indicating readiness for another task is displayed.</li>
     * </ul>
     * If there is no current task and no tasks are available:
     * <ul>
     *     <li>A message indicating no tasks are available is displayed.</li>
     *     <li>Both the "Done" and "Tell me what to do" buttons are hidden.</li>
     * </ul>
     * </p>
     */
    private void updateUI()
    {
        int taskCount = Tasks.getInstance().getAvailableTasks().size();
        boolean tasksAvailable = taskCount > 0;
        UUID currentTaskId = Tasks.getInstance().getCurrentTaskId();
        Task currentTask = currentTaskId == null ? null : Tasks.getInstance().getTask(currentTaskId);
        boolean currentTaskDone = currentTask != null && currentTask.isDone();
        if (currentTask != null && !currentTaskDone) //task is not null and not done
        {
            currentTaskDescription.setText(currentTask.getDescription());
            buttonDone.setVisibility(View.VISIBLE);
            buttonTell.setVisibility(View.GONE);
        } else if (tasksAvailable)//tasks available and no active task
        {
            buttonDone.setVisibility(View.GONE);
            buttonTell.setVisibility(View.VISIBLE);
            currentTaskDescription.setText(getResources().getString(R.string.ready_for_another_task));
        } else //no tasks available and no active task
        {
            currentTaskDescription.setText(R.string.no_tasks);
            buttonDone.setVisibility(View.GONE);
            buttonTell.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Tasks.getInstance().load(this);
        updateUI();
    }



}
