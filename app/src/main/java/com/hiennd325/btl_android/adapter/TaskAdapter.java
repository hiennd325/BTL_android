package com.hiennd325.btl_android.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hiennd325.btl_android.R;
import com.hiennd325.btl_android.database.DatabaseHelper;
import com.hiennd325.btl_android.model.Category;
import com.hiennd325.btl_android.model.Subtask;
import com.hiennd325.btl_android.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskStatusChanged(Task task, boolean isCompleted);
    }

    private final Context context;
    private List<Task> tasks = new ArrayList<>();
    private final OnTaskClickListener listener;
    private final DatabaseHelper dbHelper;

    public TaskAdapter(Context context, OnTaskClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public void setTasks(List<Task> newTasks) {
        this.tasks = new ArrayList<>(newTasks);
        notifyDataSetChanged();
    }

    public Task getTaskAt(int position) {
        if (position >= 0 && position < tasks.size()) {
            return tasks.get(position);
        }
        return null;
    }

    public void removeTaskAt(int position) {
        if (position >= 0 && position < tasks.size()) {
            Task task = tasks.get(position);
            dbHelper.deleteTask(task.getId());
            tasks.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final View layoutContent;
        private final CheckBox cbTask;
        private final TextView tvTitle;
        private final TextView tvCategoryBadge;
        private final LinearLayout layoutSubtasksContainer;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutContent = itemView.findViewById(R.id.layout_task_content);
            cbTask = itemView.findViewById(R.id.cb_task);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvCategoryBadge = itemView.findViewById(R.id.tv_task_category_badge);
            layoutSubtasksContainer = itemView.findViewById(R.id.layout_subtasks_container);
        }

        public void bind(Task task) {
            tvTitle.setText(task.getTitle());

            // Checkbox state
            cbTask.setOnCheckedChangeListener(null);
            cbTask.setChecked(task.isCompleted());
            updateTextStyle(task.isCompleted());

            cbTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                updateTextStyle(isChecked);
                dbHelper.updateTaskCompleted(task.getId(), isChecked);
                if (listener != null) {
                    listener.onTaskStatusChanged(task, isChecked);
                }
            });

            // Category badge
            Category cat = task.getCategory();
            tvCategoryBadge.setText(cat.getBadgeText());
            tvCategoryBadge.setTextColor(ContextCompat.getColor(context, cat.getBadgeTextColorRes()));

            // Set badge background dynamically
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setShape(GradientDrawable.RECTANGLE);
            badgeBg.setCornerRadius(dpToPx(4));
            badgeBg.setColor(ContextCompat.getColor(context, cat.getBadgeBgRes()));
            tvCategoryBadge.setBackground(badgeBg);

            // Subtasks display
            layoutSubtasksContainer.removeAllViews();
            if (task.hasSubtasks()) {
                layoutSubtasksContainer.setVisibility(View.VISIBLE);
                LayoutInflater inflater = LayoutInflater.from(context);

                for (Subtask subtask : task.getSubtasks()) {
                    View subtaskView = inflater.inflate(R.layout.item_subtask_row, layoutSubtasksContainer, false);
                    CheckBox cbSubtask = subtaskView.findViewById(R.id.cb_subtask);
                    TextView tvSubtaskTitle = subtaskView.findViewById(R.id.tv_subtask_title);

                    tvSubtaskTitle.setText(subtask.getTitle());
                    cbSubtask.setChecked(subtask.isCompleted());
                    tvSubtaskTitle.setTextColor(ContextCompat.getColor(context, R.color.text_black));

                    cbSubtask.setOnCheckedChangeListener((bv, checked) -> {
                        subtask.setCompleted(checked);
                        dbHelper.updateSubtaskCompleted(subtask.getId(), checked);
                    });

                    layoutSubtasksContainer.addView(subtaskView);
                }
            } else {
                layoutSubtasksContainer.setVisibility(View.GONE);
            }

            // Click listener on entire item to edit
            layoutContent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(task);
                }
            });
        }

        private void updateTextStyle(boolean completed) {
            // In template mockup, checked items keep clean text with checkmark icon
            tvTitle.setTextColor(ContextCompat.getColor(context, R.color.text_black));
        }

        private float dpToPx(float dp) {
            return dp * context.getResources().getDisplayMetrics().density;
        }
    }
}
