package com.stevedegroof.tellmewhattodo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.List;

/**
 * Adapter for displaying a list of possible dependency tasks in a RecyclerView.
 * This adapter allows users to select or deselect tasks that the main task depends on.
 */
public class DependencyTaskAdapter extends RecyclerView.Adapter<DependencyTaskAdapter.DependencyViewHolder> { // Specify ViewHolder type here
    private final List<Task> allPossibleDependencyTasks;
    private final Task task;
    private final OnDependencyTaskInteractionListener listener;

    public interface OnDependencyTaskInteractionListener {
        void onDependencyTaskChecked(Task task, boolean isChecked);
     }

    public DependencyTaskAdapter(Task task ,List<Task> allPossibleDependencyTasks , OnDependencyTaskInteractionListener listener) {
        this.allPossibleDependencyTasks = allPossibleDependencyTasks;
        this.listener = listener;
        this.task=task;
    }

    @NonNull
    @Override
    public DependencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dependency_task, parent, false);
        return new DependencyViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link DependencyViewHolder#itemView} to reflect the item at the given
     * position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull DependencyViewHolder holder, int position) {
        if (allPossibleDependencyTasks != null) {
            Task dependencyTask = allPossibleDependencyTasks.get(position);

            // Set the task description
            holder.taskDescriptionTextView.setText(dependencyTask.getDescription());
            List<Task> dependencies = task.getDependencyTasks();
            boolean checked = dependencies.contains(dependencyTask);
            holder.taskSelectedCheckBox.setChecked(checked);

            holder.taskSelectedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onDependencyTaskChecked(dependencyTask, isChecked);
                }
                if (isChecked)
                    task.addDependency(dependencyTask.getId());
                else
                    task.removeDependency(dependencyTask.getId());

             });


            holder.itemView.setOnClickListener(v -> {
               holder.taskSelectedCheckBox.toggle(); // Example: clicking item toggles checkbox
            });
        }
    }

    @Override
    public int getItemCount() {
        return allPossibleDependencyTasks != null ? allPossibleDependencyTasks.size() : 0;
    }



    /**
     * ViewHolder class for dependency task items.
     * <p>
     * This class holds the views for a single dependency task item in the RecyclerView.
     * It includes a MaterialCheckBox for selecting the dependency and a TextView for displaying
     * the task description.
     */
    public static class DependencyViewHolder extends RecyclerView.ViewHolder {
        public MaterialCheckBox taskSelectedCheckBox;
        public TextView taskDescriptionTextView;

        public DependencyViewHolder(@NonNull View itemView) {
            super(itemView);
            taskSelectedCheckBox = itemView.findViewById(R.id.checkbox_dependency_selected);
            taskDescriptionTextView = itemView.findViewById(R.id.textview_dependency_description);
        }
    }
}
