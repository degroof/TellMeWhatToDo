package com.stevedegroof.tellmewhattodo; // Use your actual package name

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Activity for adding a new task or editing an existing one.
 * This activity provides a user interface for inputting task details such as description,
 * priority, recurrence settings, and dependencies on other tasks.
 * <p>
 * To edit an existing task, an Intent must be passed to this activity containing
 * an extra with the key {@link #EXTRA_TASK_ID_TO_EDIT} and the String representation
 * of the {@link UUID} of the task to be edited.
 * <p>
 * The layout for this activity is defined in {@code R.layout.activity_add_edit_task}.
 * It includes fields for:
 * <ul>
 *     <li>Task description</li>
 *     <li>Task priority (Low, Medium, High, Urgent)</li>
 *     <li>Whether the task repeats</li>
 *     <li>Repeat interval and unit (Hours, Days, Weeks, Months, Years)</li>
 *     <li>Specific repeat conditions based on the unit (e.g., day of week for weekly repeats,
 *         day of month for monthly repeats, month and day of month for yearly repeats)</li>
 *     <li>Time constraints for repeated tasks (specific time, or a window between min and max time for hourly repeats)</li>
 *     <li>Dependencies on other tasks (selecting which tasks must be completed before this one)</li>
 * </ul>
 * <p>
 * Key functionalities include:
 * <ul>
 *     <li>Initializing UI elements and populating dropdowns for priority, repeat units, months, and days of the week.</li>
 *     <li>Handling the "edit mode" by pre-filling fields if a task ID is provided.</li>
 *     <li>Dynamically updating the visibility and content of repeat detail fields based on user selections.</li>
 *     <li>Displaying time pickers for setting repeat times.</li>
 *     <li>Validating user input before saving the task.</li>
 *     <li>Saving the new or updated task to the application's task list and persisting it.</li>
 *     <li>Managing task dependencies through a RecyclerView and a {@link DependencyTaskAdapter}.</li>
 * </ul>
 */
public class AddEditTaskActivity extends ParentActivity
{
    public static final String EXTRA_TASK_ID_TO_EDIT = "1001";

    private AutoCompleteTextView autocompletePriority;
    private AutoCompleteTextView autocompleteRepeatUnit;
    private AutoCompleteTextView autocompleteRepeatMonth;
    private AutoCompleteTextView autocompleteRepeatDayOfWeek;
    private AutoCompleteTextView autocompleteRepeatDayOfMonth;
    private Button buttonCancel;
    private TextInputLayout edittextRepeatInterval, edittextDescription;

    private TextView labelOn, labelAnd, labelAt;

    private MaterialCheckBox checkboxRepeats;
    private LinearLayout layoutRepeatsDetails, layoutDay, layoutTime;

    private TextInputLayout textInputLayoutRepeatMonth;
    private TextInputLayout textInputLayoutRepeatDayOfWeek;
    private TextInputLayout textInputLayoutRepeatDayOfMonth;
    private Button buttonRepeatTime;
    private Toolbar toolbarAddTask;

    private RecyclerView recyclerViewDependencies;
    private Button buttonRepeatMaxTime, buttonRepeatMinTime, buttonRepeatAnyTime, buttonSave;

    private Task taskToAddEdit;

    private ConstraintLayout dependencies;

    private DependencyTaskAdapter dependencyTaskAdapter;
    private List<Task> allPossibleDependencyTasks;
    private boolean isEditMode = false;

    /**
     * Initializes the activity, sets up UI elements, and handles task creation or editing.
     * <p>
     * This method is called when the activity is first created. It performs the following actions:
     * <ul>
     *     <li>Sets the content view for the activity.</li>
     *     <li>Finds and initializes all UI elements such as AutoCompleteTextViews, CheckBoxes, Buttons, Layouts, etc.</li>
     *     <li>Sets up the toolbar for the activity.</li>
     *     <li>Initializes the RecyclerView for displaying task dependencies.</li>
     *     <li>Sets up listeners for UI interactions, such as checkbox changes and button clicks.</li>
     *     <li>Determines if the activity is in "edit mode" (editing an existing task) or "add mode" (creating a new task) by checking the intent extras.</li>
     *     <li>If in edit mode, it retrieves the task to be edited and populates the UI fields with its data.</li>
     *     <li>If in add mode, it initializes a new Task object.</li>
     *     <li>Populates dropdowns for priority, repeat units, months, and days of the week.</li>
     *     <li>Sets up the adapter for the dependency RecyclerView.</li>
     *     <li>Adjusts the visibility of repeat detail fields based on the selected repeat unit.</li>
     *     <li>Sets the title of the toolbar based on whether it's adding or editing a task.</li>
     * </ul>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        // Initialize UI elements
        autocompletePriority = findViewById(R.id.autocomplete_priority);
        checkboxRepeats = findViewById(R.id.checkbox_repeats);
        layoutRepeatsDetails = findViewById(R.id.layout_repeats_details);
        layoutDay = findViewById(R.id.layout_day);
        layoutTime = findViewById(R.id.layout_time);
        autocompleteRepeatUnit = findViewById(R.id.autocomplete_repeat_unit);
        textInputLayoutRepeatMonth = findViewById(R.id.text_input_layout_repeat_month);
        autocompleteRepeatMonth = findViewById(R.id.autocomplete_repeat_month);
        textInputLayoutRepeatDayOfWeek = findViewById(R.id.text_input_layout_repeat_day_of_week);
        autocompleteRepeatDayOfWeek = findViewById(R.id.autocomplete_repeat_day_of_week);
        textInputLayoutRepeatDayOfMonth = findViewById(R.id.text_input_layout_repeat_day_of_month);
        autocompleteRepeatDayOfMonth = findViewById(R.id.autocomplete_repeat_day_of_month);
        buttonRepeatTime = findViewById(R.id.button_repeat_time);
        buttonRepeatMinTime = findViewById(R.id.button_repeat_min_time);
        buttonRepeatMaxTime = findViewById(R.id.button_repeat_max_time);
        buttonRepeatAnyTime = findViewById(R.id.button_repeat_any_time);
        labelOn = findViewById(R.id.text_input_layout_repeat_on);
        labelAnd = findViewById(R.id.text_input_layout_repeat_and);
        labelAt = findViewById(R.id.text_input_layout_repeat_at);
        buttonSave = findViewById(R.id.button_save);
        edittextRepeatInterval = findViewById(R.id.text_input_layout_repeat_interval);
        edittextDescription = findViewById(R.id.text_input_layout_description);
        dependencies = findViewById(R.id.layout_dependencies);
        recyclerViewDependencies = findViewById(R.id.recycler_view_dependencies); // Initialize RecyclerView
        buttonCancel = findViewById(R.id.button_cancel);

        toolbarAddTask = findViewById(R.id.toolbarAddTask);
        setSupportActionBar(toolbarAddTask);


        setShortMonth(); //set short month names

        // Set up checkbox listener
        checkboxRepeats.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            if (isChecked)
            {
                layoutRepeatsDetails.setVisibility(View.VISIBLE);
                updateRepeatOnFieldsVisibility(autocompleteRepeatUnit.getText().toString());
            } else
            {
                layoutRepeatsDetails.setVisibility(View.GONE);
            }
        });

        // Set up button listeners
        buttonRepeatTime.setOnClickListener(v -> showTimePicker());
        buttonRepeatMaxTime.setOnClickListener(v -> showMaxTimePicker());
        buttonRepeatMinTime.setOnClickListener(v -> showMinTimePicker());
        buttonRepeatAnyTime.setOnClickListener(v -> setAnyTime());
        buttonSave.setOnClickListener(v -> saveTask());

        // Set up RecyclerView
        recyclerViewDependencies.setLayoutManager(new LinearLayoutManager(this));


        buttonCancel.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume()
    {
        super.onResume();


        // Check if in edit mode
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TASK_ID_TO_EDIT))
        {
            String taskIdStr = intent.getStringExtra(EXTRA_TASK_ID_TO_EDIT);
            UUID taskId = UUID.fromString(taskIdStr);
            Task currentEditingTask = Tasks.getInstance().getTask(taskId);
            if (currentEditingTask != null)
            {
                isEditMode = true;
                taskToAddEdit = currentEditingTask;
            }
        } else
        {
            taskToAddEdit = new Task();
            isEditMode = false;
        }

        //list dependencies
        allPossibleDependencyTasks = getPossibleDependencyTasks();


        dependencyTaskAdapter = new DependencyTaskAdapter(taskToAddEdit, allPossibleDependencyTasks, new DependencyTaskAdapter.OnDependencyTaskInteractionListener()
        {
            @Override
            public void onDependencyTaskChecked(Task task, boolean isChecked)
            {
                if (isChecked)
                    taskToAddEdit.addDependency(task.getId());
                else
                    taskToAddEdit.removeDependency(task.getId());
            }
        });
        recyclerViewDependencies.setAdapter(dependencyTaskAdapter);
        if (allPossibleDependencyTasks.isEmpty())
            dependencies.setVisibility(View.GONE);
        else
            dependencies.setVisibility(View.VISIBLE);
        // Set up priority and repeat unit dropdowns
        //TODO: Move this array to Util and add lookup methods
        String[] priorities = new String[]{getApplicationContext().getString(R.string.Low), getApplicationContext().getString(R.string.Medium), getApplicationContext().getString(R.string.High), getApplicationContext().getString(R.string.Urgent)};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                priorities
        );
        autocompletePriority.setAdapter(priorityAdapter);

        String[] repeatUnits = new String[]{getApplicationContext().getString(R.string.repeat_hours), getApplicationContext().getString(R.string.repeat_days), getApplicationContext().getString(R.string.repeat_weeks), getApplicationContext().getString(R.string.repeat_months), getApplicationContext().getString(R.string.repeat_years)};
        ArrayAdapter<String> repeatUnitAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                repeatUnits
        );
        autocompleteRepeatUnit.setAdapter(repeatUnitAdapter);
        autocompleteRepeatUnit.setOnItemClickListener((parent, view, position, id) ->
        {
            String selectedUnit = (String) parent.getItemAtPosition(position);
            updateRepeatOnFieldsVisibility(selectedUnit);
        });

        // Set up month and day of week dropdowns
        String[] months = Util.getMonths(getApplicationContext());
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, months);
        autocompleteRepeatMonth.setAdapter(monthAdapter);

        String[] daysOfWeek = Util.getDaysOfWeek(getApplicationContext());
        ArrayAdapter<String> dayOfWeekAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, daysOfWeek);
        autocompleteRepeatDayOfWeek.setAdapter(dayOfWeekAdapter);

        dependencyTaskAdapter.notifyDataSetChanged();

        if (autocompleteRepeatUnit.getText() != null && autocompleteRepeatUnit.getText() != null)
        {
            updateRepeatOnFieldsVisibility(autocompleteRepeatUnit.getText().toString());
        } else
        {
            updateRepeatOnFieldsVisibility(null);
        }

        //set title
        if (isEditMode)
        {
            populateFieldsForEdit();
            toolbarAddTask.setTitle(R.string.edit_task);
        } else
        {
            taskToAddEdit = new Task();
            toolbarAddTask.setTitle(R.string.add_task);
        }
    }

    /**
     * Populates the input fields with the data from the task being edited.
     * This method is called when the activity is opened in edit mode.
     * It sets the values for description, priority, repeat settings (type, interval, month, day of week, day of month, times),
     * and updates the visibility of repeat-related fields based on the repeat type.
     * Finally, it notifies the dependency task adapter that the data has changed.
     */
    private void populateFieldsForEdit()
    {
        edittextDescription.getEditText().setText(taskToAddEdit.getDescription());
        autocompletePriority.setText(Util.getPriorityString(getApplicationContext(), taskToAddEdit.getWeight()), false);
        checkboxRepeats.setChecked(taskToAddEdit.getRepeatType() != Task.REPEAT_TYPE_NONE);
        String repeatType = Util.getRepeatTypeString(getApplicationContext(), taskToAddEdit.getRepeatType());
        autocompleteRepeatUnit.setText(repeatType, false);
        edittextRepeatInterval.getEditText().setText(String.valueOf(taskToAddEdit.getRepeatInterval()));
        autocompleteRepeatMonth.setText(Util.getMonthName(getApplicationContext(), taskToAddEdit.getMonth()), false);
        autocompleteRepeatDayOfWeek.setText(Util.getDayOfWeekName(getApplicationContext(), taskToAddEdit.getDayOfWeek()), false);
        autocompleteRepeatDayOfMonth.setText(Util.getDayOfMonthNameShort(getApplicationContext(), taskToAddEdit.getDayOfMonth()), false);
        int maxMinute = taskToAddEdit.getMaxMinute();
        if (maxMinute == Task.END_OF_DAY)
            buttonRepeatMaxTime.setText(getResources().getString(R.string.set_time));
        else
            buttonRepeatMaxTime.setText(Util.getTimeString(getApplicationContext(), maxMinute));

        int minMinute = taskToAddEdit.getMinMinute();
        if (minMinute == Task.START_OF_DAY)
            buttonRepeatMinTime.setText(getResources().getString(R.string.set_time));
        else
            buttonRepeatMinTime.setText(Util.getTimeString(getApplicationContext(), minMinute));

        int minute = taskToAddEdit.getMinute();
        if (minute == Task.ANY_TIME)
            buttonRepeatTime.setText(getResources().getString(R.string.set_time));
        else
            buttonRepeatTime.setText(Util.getTimeString(getApplicationContext(), minute));


        updateRepeatOnFieldsVisibility(repeatType);
        if (!buttonRepeatTime.getText().toString().equals(getResources().getString(R.string.set_time)))
        {
            buttonRepeatAnyTime.setVisibility(View.VISIBLE);
            buttonRepeatAnyTime.setText(getResources().getString(R.string.any_time));
        }

        if (!buttonRepeatMinTime.getText().toString().equals(getResources().getString(R.string.set_time)))
        {
            buttonRepeatAnyTime.setVisibility(View.VISIBLE);
            buttonRepeatAnyTime.setText(getResources().getString(R.string.any_times));
        }

        if (!buttonRepeatMaxTime.getText().toString().equals(getResources().getString(R.string.set_time)))
        {
            buttonRepeatAnyTime.setVisibility(View.VISIBLE);
            buttonRepeatAnyTime.setText(getResources().getString(R.string.any_times));
        }

        dependencyTaskAdapter.notifyDataSetChanged();
    }


    /**
     * Retrieves a list of tasks that can be set as dependencies for the current task being added or edited.
     * <p>
     * This method filters out tasks that would create a circular dependency.
     * If the activity is in edit mode (isEditMode is true), it also excludes the current task.
     *
     * @return A List of Task objects that can be dependencies for the current task.
     */
    private List<Task> getPossibleDependencyTasks()
    {
        List<Task> tasks = Tasks.getInstance().getTasks();
        List<Task> possibleDependencyTasks = new ArrayList<>();
        for (Task task : tasks)
        {
            if (!isEditMode || !task.isDependentOn(taskToAddEdit))
            {
                possibleDependencyTasks.add(task);
            }
        }
        return possibleDependencyTasks;
    }

    /**
     * Validates and saves the task details entered by the user.
     * <p>
     * This method performs the following actions:
     * 1.  Retrieves and validates the task description. If empty, an error is flagged.
     * 2.  Retrieves and validates the task priority. If empty, an error is flagged. Otherwise, sets the task's weight based on the selected priority (Low, Medium, High, Urgent).
     * 3.  If the "repeats" checkbox is checked:
     * a.  Validates the repeat interval. If empty, not a number, or not positive, an error is flagged.
     * b.  Retrieves and validates the repeat unit (Hour(s), Day(s), Week(s), Month(s), Year(s)). If empty, an error is flagged.
     * c.  Sets the task's repeat type and associated repeat parameters (min/max time, day of week, day of month, month) based on the selected repeat unit by calling helper methods:
     * {@link #setTime()}, {@link #setWeekDay()}, {@link #setDayOfMonth()}, {@link #setMonth()}.
     * d.  Performs additional validation for repeat parameters based on the selected unit (e.g., day of week for weekly repeats, day of month for monthly/yearly repeats, month for yearly repeats). If any required field is empty, an error is flagged.
     * 4.  Validates that the minimum time is before the maximum time if both are set. If not, an error is flagged.
     * 5.  If any validation errors occur, an alert dialog is displayed listing all errors. The user can dismiss this dialog.
     * 6.  If there are no errors, a confirmation dialog is displayed summarizing the task details.
     * a.  If the user confirms, the {@link #save()} method is called to persist the task.
     * b.  If the user cancels, the dialog is dismissed.
     * </p>
     */
    private void saveTask()
    {
        boolean isError = false;
        String error = "";
        String summary = "";
        //TODO: Move hardcoded strings to resources
        if (edittextDescription.getEditText() == null || edittextDescription.getEditText().getText().toString().isEmpty())
        {
            isError = true;
            error = getString(R.string.description_can_t_be_empty);
        }
        taskToAddEdit.setDescription(edittextDescription.getEditText().getText().toString());
        if (autocompletePriority.getText() == null || autocompletePriority.getText().toString().isEmpty())
        {
            isError = true;
            error += getString(R.string.priority_can_t_be_empty);
        } else
        {
            taskToAddEdit.setWeight(Util.getPriorityInt(getApplicationContext(), autocompletePriority.getText().toString()));
        }
        if (checkboxRepeats.isChecked())
        {
            if (edittextRepeatInterval.getEditText() == null || edittextRepeatInterval.getEditText().getText().toString().isEmpty())
            {
                isError = true;
                error += getString(R.string.repeat_interval_can_t_be_empty);
            } else
            {
                String repeatIntervalStr = edittextRepeatInterval.getEditText().getText().toString();
                try
                {
                    int repeatInterval = Integer.parseInt(repeatIntervalStr);
                    taskToAddEdit.setRepeatInterval(repeatInterval);
                    if (repeatInterval <= 0)
                    {
                        isError = true;
                        error += getString(R.string.repeat_interval_should_be_a_positive_number);
                    }
                } catch (NumberFormatException e)
                {
                    isError = true;
                    error += getString(R.string.repeat_interval_should_be_a_number);
                }
            }
            //get repeat type
            if (autocompleteRepeatUnit.getText() == null || autocompleteRepeatUnit.getText().toString().isEmpty())
            {
                isError = true;
                error += getString(R.string.repeat_type_can_t_be_empty);
            } else
            {
                String repeatType = autocompleteRepeatUnit.getText().toString();
                int repeatTypeInt = Util.getRepeatTypeInt(getApplicationContext(), repeatType);
                switch (repeatTypeInt)
                {
                    case Task.REPEAT_TYPE_HOURLY:
                        taskToAddEdit.setRepeatType(Task.REPEAT_TYPE_HOURLY);
                        if (buttonRepeatMinTime.getText() == null || buttonRepeatMinTime.getText().toString().isEmpty() || buttonRepeatMinTime.getText().toString().equals(getResources().getString(R.string.set_time)))
                        {
                            taskToAddEdit.setMinMinute(Task.START_OF_DAY);
                        } else
                        {
                            String time = buttonRepeatMinTime.getText().toString();
                            taskToAddEdit.setMinute(Util.getTimeInt(getApplicationContext(), time));
                        }
                        if (buttonRepeatMaxTime.getText() == null || buttonRepeatMaxTime.getText().toString().isEmpty() || buttonRepeatMaxTime.getText().toString().equals(getResources().getString(R.string.set_time)))
                        {
                            taskToAddEdit.setMaxMinute(Task.END_OF_DAY);
                        } else
                        {
                            String time = buttonRepeatMaxTime.getText().toString();
                            taskToAddEdit.setMinute(Util.getTimeInt(getApplicationContext(), time));
                        }
                        break;
                    case Task.REPEAT_TYPE_DAILY:
                        taskToAddEdit.setRepeatType(Task.REPEAT_TYPE_DAILY);
                        setTime();
                        break;
                    case Task.REPEAT_TYPE_WEEKLY:
                        taskToAddEdit.setRepeatType(Task.REPEAT_TYPE_WEEKLY);
                        setTime();
                        setWeekDay();
                        break;
                    case Task.REPEAT_TYPE_MONTHLY:
                        taskToAddEdit.setRepeatType(Task.REPEAT_TYPE_MONTHLY);
                        setTime();
                        setDayOfMonth();
                        break;
                    case Task.REPEAT_TYPE_YEARLY:
                        taskToAddEdit.setRepeatType(Task.REPEAT_TYPE_YEARLY);
                        setTime();
                        setDayOfMonth();
                        setMonth();
                        break;
                    default:
                        isError = true;
                        error += getString(R.string.repeat_type_can_t_be_empty);
                }

                switch (repeatTypeInt)
                {
                    case Task.REPEAT_TYPE_WEEKLY:
                        if (autocompleteRepeatDayOfWeek.getText() == null || autocompleteRepeatDayOfWeek.getText().toString().isEmpty())
                        {
                            isError = true;
                            error += getString(R.string.day_of_week_can_t_be_empty);
                        }
                        break;
                    case Task.REPEAT_TYPE_MONTHLY:
                        if (autocompleteRepeatDayOfMonth.getText() == null || autocompleteRepeatDayOfMonth.getText().toString().isEmpty())
                        {
                            isError = true;
                            error += getString(R.string.day_of_month_can_t_be_empty);
                        }
                        break;
                    case Task.REPEAT_TYPE_YEARLY:
                        if (autocompleteRepeatDayOfMonth.getText() == null || autocompleteRepeatDayOfMonth.getText().toString().isEmpty())
                        {
                            isError = true;
                            error += getString(R.string.day_of_month_can_t_be_empty);
                        }
                        if (autocompleteRepeatMonth.getText() == null || autocompleteRepeatMonth.getText().toString().isEmpty())
                        {
                            isError = true;
                            error += getString(R.string.month_can_t_be_empty);
                        }
                        break;
                    default:
                        break;
                }

            }

        } else taskToAddEdit.setRepeatType(Task.REPEAT_TYPE_NONE);
        int maxTime = taskToAddEdit.getMaxMinute();
        int minTime = taskToAddEdit.getMinMinute();
        if (minTime >= maxTime)
        {
            isError = true;
            error += getString(R.string.from_time_should_be_earlier_than_to_time);
        }
        if (isError)
        {
            new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                    .setTitle("Hold On...")
                    .setMessage(error).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    }).show();
        } else
        {
            summary = taskToAddEdit.getSummary(getApplicationContext());
            summary = getString(R.string.you_want_to) + summary;
            summary += getString(R.string.does_that_look_right);
            new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme)
                    .setTitle("Just Checking...")
                    .setMessage(summary)
                    .setPositiveButton(R.string.yep, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            save();
                        }
                    })
                    .setNegativeButton(R.string.nope, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    /**
     * Saves the task to the list of tasks.
     * If the task is new (last run time is 0), it sets a default last run time.
     * Then, it adds or updates the task in the Tasks singleton, saves the tasks to persistent storage,
     * and finishes the activity.
     */
    private void save()
    {
        if (taskToAddEdit.getLastRun() == 0)
            taskToAddEdit.setLastRun(Util.getDefaultLastRun(getApplicationContext(), taskToAddEdit));
        Tasks.getInstance().putTask(taskToAddEdit);
        Tasks.getInstance().save(this);
        finish();
    }

    /**
     * Sets the repeat time for the task.
     * If the repeat time button text is null, empty, or "Whenever",
     * the task's minute is set to Task.ANY_TIME.
     */
    private void setTime()
    {
        if (buttonRepeatTime.getText() == null || buttonRepeatTime.getText().toString().isEmpty() || buttonRepeatTime.getText().toString().equals(getResources().getString(R.string.whenever)) || buttonRepeatTime.getText().toString().equals(getResources().getString(R.string.set_time)))
        {
            taskToAddEdit.setMinute(Task.ANY_TIME);
        } else
        {
            String time = buttonRepeatTime.getText().toString();
            taskToAddEdit.setMinute(Util.getTimeInt(getApplicationContext(), time));
        }
    }

    /**
     * Sets the day of the week for the task being added or edited.
     * If the autocomplete field for the day of the week is empty or null,
     * the day of the week for the task is set to {@link Task#ANY_DAY_OF_WEEK}.
     * Otherwise, the day of the week is parsed from the autocomplete field
     * and set for the task.
     */
    private void setWeekDay()
    {
        if (autocompleteRepeatDayOfWeek == null || autocompleteRepeatDayOfWeek.getText() == null || autocompleteRepeatDayOfWeek.getText().toString().isEmpty())
        {
            taskToAddEdit.setDayOfWeek(Task.ANY_DAY_OF_WEEK);
        } else
        {
            String dayOfWeek = autocompleteRepeatDayOfWeek.getText().toString();
            taskToAddEdit.setDayOfWeek(Util.getDayOfWeekInt(getApplicationContext(), dayOfWeek));
        }
    }

    /**
     * Sets the day of the month for the task based on the user's selection.
     * If no day is selected or "Any Day" is chosen, it defaults to Task.ANY_DAY_OF_MONTH.
     * Otherwise, it parses the selected day string and sets the corresponding integer value.
     */
    private void setDayOfMonth()
    {
        if (autocompleteRepeatDayOfMonth == null || autocompleteRepeatDayOfMonth.getText() == null || autocompleteRepeatDayOfMonth.getText().toString().isEmpty() || autocompleteRepeatDayOfMonth.getText().toString().equals("Any Day"))
        {
            taskToAddEdit.setDayOfMonth(Task.ANY_DAY_OF_MONTH);
        } else
        {
            taskToAddEdit.setDayOfMonth(Util.getDayOfMonthInt(getApplicationContext(), autocompleteRepeatDayOfMonth.getText().toString()));
        }
    }


    /**
     * Sets the month for the task repetition.
     * If the autocompleteRepeatMonth field is empty or null, the month is set to Task.ANY_MONTH.
     * Otherwise, the month is parsed from the autocompleteRepeatMonth field and set accordingly.
     */
    private void setMonth()
    {
        if (autocompleteRepeatMonth == null || autocompleteRepeatMonth.getText() == null || autocompleteRepeatMonth.getText().toString().isEmpty())
        {
            taskToAddEdit.setMonth(Task.ANY_MONTH);
        } else
        {
            String month = autocompleteRepeatMonth.getText().toString();
            taskToAddEdit.setMonth(Util.getMonthInt(getApplicationContext(), month));
        }
    }

    /**
     * Sets the task to be repeatable at any time of the day.
     * This method updates the UI elements related to time selection (repeat time, max time, min time)
     * to reflect that the task can occur at any time. It also sets the task's internal
     * time properties (minMinute, maxMinute, minute) to their "any time" equivalent values.
     * Finally, it hides the "Any Time" button as it's no longer relevant once "any time" is selected.
     */
    private void setAnyTime()
    {
        buttonRepeatTime.setText(R.string.set_time);
        buttonRepeatMaxTime.setText(R.string.set_time);
        buttonRepeatMinTime.setText(R.string.set_time);
        taskToAddEdit.setMinMinute(Task.START_OF_DAY);
        taskToAddEdit.setMaxMinute(Task.END_OF_DAY);
        taskToAddEdit.setMinute(Task.ANY_TIME);
        buttonRepeatAnyTime.setVisibility(View.GONE);
    }


    /**
     * Updates the visibility of the repeat "on" fields based on the selected repeat unit.
     * This method controls which input fields (day of week, month, day of month, time pickers)
     * are visible and relevant for the chosen repetition frequency (e.g., weekly, monthly, hourly).
     * <p>
     * If the "Repeats" checkbox is not checked, or if no unit is selected,
     * all specific repeat detail fields (day and time layouts) are hidden.
     * <p>
     * For WEEKS: Shows the day of the week selector.
     * For MONTHS: Shows the day of the month selector.
     * For YEARS: Shows both the month and day of the month selectors.
     * For DAYS: Hides the time selectors.
     * For HOURS: Shows min/max time pickers.
     *
     * @param selectedUnit The string representation of the selected repeat unit (e.g., "Week(s)", "Month(s)").
     *                     Can be null or empty if no unit is selected or if repeats are off.
     */
    private void updateRepeatOnFieldsVisibility(String selectedUnit)
    {
        labelOn.setText(R.string.on);
        if (!checkboxRepeats.isChecked() || selectedUnit == null || selectedUnit.isEmpty())
        {
            layoutDay.setVisibility(View.GONE);
            layoutTime.setVisibility(View.GONE);
            return;
        }

        buttonRepeatTime.setVisibility(View.VISIBLE);
        labelOn.setVisibility(View.VISIBLE);
        labelAt.setVisibility(View.VISIBLE);
        buttonRepeatMaxTime.setVisibility(View.GONE);
        buttonRepeatMinTime.setVisibility(View.GONE);
        labelAnd.setVisibility(View.GONE);
        layoutDay.setVisibility(View.VISIBLE);
        layoutTime.setVisibility(View.VISIBLE);
        buttonRepeatAnyTime.setVisibility(View.VISIBLE);
        labelAt.setText(R.string.at);
        buttonRepeatAnyTime.setText(R.string.any_time);
        buttonRepeatAnyTime.setVisibility(View.GONE);

        if (selectedUnit.equals(getApplicationContext().getString(R.string.repeat_weeks)))
        {
            textInputLayoutRepeatDayOfWeek.setVisibility(View.VISIBLE);
            textInputLayoutRepeatMonth.setVisibility(View.GONE);
            textInputLayoutRepeatDayOfMonth.setVisibility(View.GONE);
        } else if (selectedUnit.equals(getApplicationContext().getString(R.string.repeat_months)))
        {
            textInputLayoutRepeatDayOfWeek.setVisibility(View.GONE);
            textInputLayoutRepeatMonth.setVisibility(View.GONE);
            textInputLayoutRepeatDayOfMonth.setVisibility(View.VISIBLE);
            setShortMonth();
        } else if (selectedUnit.equals(getApplicationContext().getString(R.string.repeat_years)))
        {
            textInputLayoutRepeatMonth.setVisibility(View.VISIBLE);
            textInputLayoutRepeatDayOfMonth.setVisibility(View.VISIBLE);
            textInputLayoutRepeatDayOfWeek.setVisibility(View.GONE);
            setLongMonth();
        } else if (selectedUnit.equals(getApplicationContext().getString(R.string.repeat_days)))
        {
            labelAt.setText(R.string.at);
            layoutDay.setVisibility(View.GONE);
        } else
        { // Hourly
            layoutDay.setVisibility(View.GONE);
            labelAt.setText(R.string.between);
            buttonRepeatTime.setVisibility(View.GONE);
            buttonRepeatMaxTime.setVisibility(View.VISIBLE);
            buttonRepeatMinTime.setVisibility(View.VISIBLE);
            labelAnd.setVisibility(View.VISIBLE);
            buttonRepeatAnyTime.setText(R.string.any_times);
        }
    }

    /**
     * Sets the adapter for the day of the month autocomplete text view to use the long month (1st-31st).
     */
    private void setLongMonth()
    {
        String[] daysOfMonthList = Util.getDaysOfMonthLong(getApplicationContext());
        ArrayAdapter<String> dayOfMonthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, daysOfMonthList);
        autocompleteRepeatDayOfMonth.setAdapter(dayOfMonthAdapter);
    }

    /**
     * Sets the adapter for the day of the month autocomplete text view to use the short month (1st-28th).
     */
    private void setShortMonth()
    {
        String[] daysOfMonthList = Util.getDaysOfMonthShort(getApplicationContext());
        ArrayAdapter<String> dayOfMonthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, daysOfMonthList);
        autocompleteRepeatDayOfMonth.setAdapter(dayOfMonthAdapter);
    }

    private void showTimePicker()
    {
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour((isEditMode && taskToAddEdit.getMinute() != Task.ANY_TIME) ? taskToAddEdit.getMinute() / 60 : 12)
                .setMinute((isEditMode && taskToAddEdit.getMinute() != Task.ANY_TIME) ? taskToAddEdit.getMinute() % 60 : 0)
                .setTitleText("Select Repeat Time");

        final MaterialTimePicker materialTimePicker = builder.build();

        materialTimePicker.addOnPositiveButtonClickListener(dialog ->
        {
            int hour = materialTimePicker.getHour();
            int minute = materialTimePicker.getMinute();
            String amPm = (hour < 12) ? getApplicationContext().getString(R.string.am) : getApplicationContext().getString(R.string.pm);
            if (hour == 0)
            {
                hour = 12; // Midnight case for 12-hour format
            } else if (hour > 12)
            {
                hour -= 12; // Convert 24-hour to 12-hour format
            }
            taskToAddEdit.setMinute(hour * 60 + minute);
            buttonRepeatTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm));
            buttonRepeatAnyTime.setVisibility(View.VISIBLE);
        });
        materialTimePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void showMaxTimePicker()
    {
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(isEditMode ? taskToAddEdit.getMaxMinute() / 60 : 12)
                .setMinute(isEditMode ? taskToAddEdit.getMaxMinute() % 60 : 0)
                .setTitleText("Select Latest Time");

        final MaterialTimePicker materialTimePicker = builder.build();

        materialTimePicker.addOnPositiveButtonClickListener(dialog ->
        {
            int hour = materialTimePicker.getHour();
            int minute = materialTimePicker.getMinute();
            taskToAddEdit.setMaxMinute(hour * 60 + minute);
            String amPm = (hour < 12) ? getApplicationContext().getString(R.string.am) : getApplicationContext().getString(R.string.pm);
            if (hour == 0)
            {
                hour = 12; // Midnight case for 12-hour format
            } else if (hour > 12)
            {
                hour -= 12; // Convert 24-hour to 12-hour format
            }
            buttonRepeatMaxTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm));
            buttonRepeatAnyTime.setVisibility(View.VISIBLE);
        });
        materialTimePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void showMinTimePicker()
    {
        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(isEditMode ? taskToAddEdit.getMinMinute() / 60 : 12)
                .setMinute(isEditMode ? taskToAddEdit.getMinMinute() % 60 : 0)
                .setTitleText("Select Earliest Time");

        final MaterialTimePicker materialTimePicker = builder.build();

        materialTimePicker.addOnPositiveButtonClickListener(dialog ->
        {
            int hour = materialTimePicker.getHour();
            int minute = materialTimePicker.getMinute();
            taskToAddEdit.setMinMinute(hour * 60 + minute);
            String amPm = (hour < 12) ? getApplicationContext().getString(R.string.am) : getApplicationContext().getString(R.string.pm);
            if (hour == 0)
            {
                hour = 12; // Midnight case for 12-hour format
            } else if (hour > 12)
            {
                hour -= 12; // Convert 24-hour to 12-hour format
            }
            buttonRepeatMinTime.setText(String.format(Locale.getDefault(), "%02d:%02d %s", hour, minute, amPm));
            buttonRepeatAnyTime.setVisibility(View.VISIBLE);
        });
        materialTimePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }


}
