package com.stevedegroof.tellmewhattodo; // Your package name

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter class for displaying tasks in a RecyclerView.
 * This adapter handles the display of each task item, including its description, status,
 * and selection state (checkbox). It also manages interactions with the tasks, such as
 * selection and clicks, through the {@link OnTaskInteractionListener}.
 * <p>
 * The adapter internally maintains a set of selected tasks to manage the selection state
 * of checkboxes. The status of a task (Done, Waiting, Ready) determines the background
 * color of the item view.
 */
public class ViewTaskAdapter extends RecyclerView.Adapter<ViewTaskAdapter.TaskViewHolder> {

    private final List<Task> tasksList;
    private final OnTaskInteractionListener listener;
    private final Set<Task> selectedTasks = new HashSet<>(); // To manage selection state internally

    public interface OnTaskInteractionListener {
        void onTaskSelected(Task task, boolean isSelected);
        void onTaskClicked(Task task);
    }

    public ViewTaskAdapter(List<Task> tasksList, OnTaskInteractionListener listener) {
        this.tasksList = tasksList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_task, parent, false);
        return new TaskViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link TaskViewHolder#itemView} to reflect the item at the
     * given position.
     *
     * It sets the task description, truncating it if it's too long.
     * It also sets the background color and status text of the item view based on the task's status:
     * - "Done" (Red) if the task is completed.
     * - "Waiting" (Orange) if the task is not available.
     * - "Ready" (Green) if the task is available and not done.
     *
     * The method manages the state of the checkbox for task selection. When the checkbox state changes,
     * it updates the internal set of selected tasks and notifies the {@link OnTaskInteractionListener}.
     *
     * It also sets an OnClickListener for the item view itself, notifying the
     * {@link OnTaskInteractionListener} when a task item is clicked.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = tasksList.get(position);

        String fullDescription = currentTask.getDescription();
        int maxLength = 50;
        if (fullDescription.length() > maxLength) {
            holder.descriptionTextView.setText(fullDescription.substring(0, maxLength) + "...");
        } else {
            holder.descriptionTextView.setText(fullDescription);
        }
        Drawable background = holder.itemView.getBackground().mutate();

        if (background instanceof GradientDrawable) {
            GradientDrawable gradientDrawable = (GradientDrawable) background;

            if (currentTask.isDone()) {
                holder.statusTextView.setText("Done");
                gradientDrawable.setColor(Color.RED);
            } else if (!currentTask.isAvailable()) {
                holder.statusTextView.setText("Waiting");
                gradientDrawable.setColor(Color.parseColor("#FFA500")); // Orange
            } else {
                holder.statusTextView.setText("Ready");
                gradientDrawable.setColor(Color.parseColor("#4CAF50")); // Green
            }
        } else {
            if (currentTask.isDone()) {
                holder.statusTextView.setText("Done");
                holder.itemView.setBackgroundColor(Color.RED);
            } else if (!currentTask.isAvailable()) {
                holder.statusTextView.setText("Waiting");
                holder.itemView.setBackgroundColor(Color.parseColor("#FFA500")); // Orange
            } else {
                holder.statusTextView.setText("Ready");
                holder.itemView.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            }
        }


        // Manage checkbox state
        holder.taskSelectedCheckBox.setOnCheckedChangeListener(null);
        holder.taskSelectedCheckBox.setChecked(selectedTasks.contains(currentTask));
        holder.taskSelectedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTasks.add(currentTask);
            } else {
                selectedTasks.remove(currentTask);
            }
            if (listener != null) {
                listener.onTaskSelected(currentTask, isChecked);
            }
        });

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClicked(currentTask);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasksList != null ? tasksList.size() : 0;
    }


    public void clearSelections() {
        selectedTasks.clear();
        notifyDataSetChanged(); // Could be optimized if you know which items were selected
    }


    static class TaskViewHolder extends RecyclerView.ViewHolder {
        MaterialCheckBox taskSelectedCheckBox;
        TextView descriptionTextView;
        TextView statusTextView;
        View itemView;

        TaskViewHolder(View view) {
            super(view);
            itemView = view;
            taskSelectedCheckBox = view.findViewById(R.id.checkbox_task_selected);
            descriptionTextView = view.findViewById(R.id.textview_task_item_description);
            statusTextView = view.findViewById(R.id.textview_task_item_status);
        }
    }
}
