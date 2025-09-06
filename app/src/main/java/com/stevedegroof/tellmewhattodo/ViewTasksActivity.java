package com.stevedegroof.tellmewhattodo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing, managing, and interacting with a list of tasks.
 * This activity displays all tasks in a RecyclerView, allowing users to:
 * - View task details.
 * - Select multiple tasks for batch operations.
 * - Delete selected tasks.
 * - Requeue (mark as not done) selected tasks.
 * - Navigate to add a new task.
 * - Navigate to edit an existing task.
 */
public class ViewTasksActivity extends ParentActivity
{
    private RecyclerView recyclerViewAllTasks;
    private ViewTaskAdapter viewTaskAdapter;
    private List<Task> allTasksList;
    private List<Task> selectedTasksList;

    private Button buttonDeleteTasks;
    private Button buttonRequeueTasks;
    private Button buttonCancelViewTasks;

    private Button buttonAddTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_tasks);
        Toolbar toolbar = findViewById(R.id.toolbarViewTasks);
        setSupportActionBar(toolbar);

        // Initialize UI components
        recyclerViewAllTasks = findViewById(R.id.recycler_view_all_tasks);
        buttonDeleteTasks = findViewById(R.id.button_delete_tasks);
        buttonRequeueTasks = findViewById(R.id.button_requeue_tasks);
        buttonCancelViewTasks = findViewById(R.id.button_cancel_view_tasks);
        buttonAddTask = findViewById(R.id.button_add_tasks);
        selectedTasksList = new ArrayList<>();
        allTasksList = Tasks.getInstance().getTasks();
        // Set up RecyclerView
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerViewAllTasks.getLayoutManager();
        if (layoutManager != null)
        {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                    recyclerViewAllTasks.getContext(),
                    layoutManager.getOrientation()
            );
            recyclerViewAllTasks.addItemDecoration(dividerItemDecoration);
        }

        // Set up adapter
        viewTaskAdapter = new ViewTaskAdapter(allTasksList, new ViewTaskAdapter.OnTaskInteractionListener()
        {
            @Override
            public void onTaskSelected(Task task, boolean isSelected)
            {
                if (isSelected)
                {
                    if (!selectedTasksList.contains(task))
                    {
                        selectedTasksList.add(task);
                    }
                } else
                {
                    selectedTasksList.remove(task);
                }
                updateActionButtonsState();
            }

            @Override
            public void onTaskClicked(Task task)
            {
                Intent intent = new Intent(ViewTasksActivity.this, AddEditTaskActivity.class);
                intent.putExtra(AddEditTaskActivity.EXTRA_TASK_ID_TO_EDIT, task.getId().toString());
                startActivity(intent);
            }
        });
        recyclerViewAllTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAllTasks.setAdapter(viewTaskAdapter);

        // Set up action buttons
        buttonDeleteTasks.setOnClickListener(v -> confirmDeleteSelectedTasks());
        buttonRequeueTasks.setOnClickListener(v -> requeueSelectedTasks());
        buttonCancelViewTasks.setOnClickListener(v -> finish());
        buttonAddTask.setOnClickListener(this::buttonAddClick);


    }

    /**
     * Updates the enabled state of the action buttons (Delete and Requeue)
     * based on whether any tasks are currently selected.
     * If at least one task is selected, the buttons are enabled.
     * Otherwise, they are disabled.
     */
    private void updateActionButtonsState()
    {
        boolean hasSelections = !selectedTasksList.isEmpty();
        buttonDeleteTasks.setEnabled(hasSelections);
        buttonRequeueTasks.setEnabled(hasSelections);
    }

    /**
     * Prompts the user to confirm the deletion of selected tasks.
     * If no tasks are selected, a toast message is displayed.
     * Otherwise, an AlertDialog is shown to confirm the deletion. If confirmed,
     * the {@link #deleteSelectedTasks()} method is called.
     */
    private void confirmDeleteSelectedTasks()
    {
        //TODO: Move hardcoded strings to resources
        if (selectedTasksList.isEmpty())
        {
            Toast.makeText(this, R.string.no_tasks_selected_to_delete, Toast.LENGTH_SHORT).show();
            return;
        }


        new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                .setTitle(R.string.confirm_delete)
                .setMessage(String.format(getString(R.string.are_you_sure_you_want_to_delete_d_task_s), selectedTasksList.size()))
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteSelectedTasks())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Deletes the tasks that are currently selected by the user.
     * This method performs the following actions:
     * 1. Checks if any tasks are selected. If not, it returns immediately.
     * 2. Iterates through the {@code selectedTasksList} and removes each task from the main {@code Tasks} singleton instance.
     * 3. Removes all selected tasks from the {@code allTasksList} which is used by the adapter.
     * 4. Clears the {@code selectedTasksList}.
     * 5. Notifies the {@code viewTaskAdapter} that the data set has changed to update the UI.
     * 6. Saves the current state of tasks using {@code Tasks.getInstance().save(this)}.
     * 7. Calls {@code updateActionButtonsState()} to enable/disable action buttons based on selections.
     */
    private void deleteSelectedTasks()
    {
        if (selectedTasksList.isEmpty()) return;

        for (Task task : selectedTasksList)
        {
            Tasks.getInstance().removeTask(task.getId());
        }
        allTasksList.removeAll(selectedTasksList);
        selectedTasksList.clear();
        viewTaskAdapter.notifyDataSetChanged();
        Tasks.getInstance().save(this);
        updateActionButtonsState();
    }

    /**
     * Handles the click event for the "Add Task" button.
     * This method creates an Intent to start the AddEditTaskActivity,
     * allowing the user to add a new task.
     *
     * @param view The view that was clicked (the "Add Task" button).
     */
    public void buttonAddClick(View view)
    {
        Intent intent = new Intent(this, AddEditTaskActivity.class);
        startActivity(intent);
    }

    /**
     * Requeues the selected tasks.
     * Sets the 'done' status of each selected task to false,
     * updates the adapter to reflect changes, saves the task list,
     * clears the selection, and updates the state of action buttons.
     * If no tasks are selected, a toast message is displayed.
     */
    private void requeueSelectedTasks()
    {
        //TODO: It only makes sense to requeue non-repeating tasks that are not done. Not sure if it matters, though.
        //TODO: Move hardcoded strings to resources
        if (selectedTasksList.isEmpty())
        {
            Toast.makeText(this, "No tasks selected to requeue.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Task task : selectedTasksList)
        {
            task.setDone(false);
        }
        viewTaskAdapter.notifyDataSetChanged();
        Tasks.getInstance().save(this);

        viewTaskAdapter.clearSelections();
        selectedTasksList.clear();
        updateActionButtonsState();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        allTasksList.clear();
        allTasksList.addAll(Tasks.getInstance().getTasks());
        viewTaskAdapter.notifyDataSetChanged();
    }

}
