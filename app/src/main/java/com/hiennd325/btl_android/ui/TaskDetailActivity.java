package com.hiennd325.btl_android.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.hiennd325.btl_android.R;
import com.hiennd325.btl_android.database.DatabaseHelper;
import com.hiennd325.btl_android.model.Category;
import com.hiennd325.btl_android.model.Subtask;
import com.hiennd325.btl_android.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private ImageView btnClose;
    private ImageView btnDeleteTask;
    private EditText etTaskTitle;
    private ChipGroup chipGroupCategory;
    private Chip chipHealth;
    private Chip chipWork;
    private Chip chipMental;
    private Chip chipOthers;

    private Spinner spinnerStartHour;
    private Spinner spinnerDuration;

    private LinearLayout layoutDetailSubtasks;
    private View btnAddSubtaskRow;
    private View btnSave;

    private DatabaseHelper dbHelper;
    private long taskId = -1;
    private Task currentTask;
    private final List<Subtask> currentSubtasks = new ArrayList<>();

    private final String[] hourLabels = {
            "No time schedule",
            "6:00 AM", "7:00 AM", "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM",
            "12:00 PM", "1:00 PM (13:00)", "2:00 PM (14:00)", "3:00 PM (15:00)",
            "4:00 PM (16:00)", "5:00 PM (17:00)", "6:00 PM (18:00)"
    };
    private final int[] hourValues = {-1, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};

    private final String[] durationLabels = {"1 hour", "2 hours", "3 hours", "4 hours"};
    private final int[] durationValues = {1, 2, 3, 4};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupSpinners();
        setupListeners();

        taskId = getIntent().getLongExtra("TASK_ID", -1);
        if (taskId != -1) {
            loadExistingTask(taskId);
        } else {
            btnDeleteTask.setVisibility(View.GONE);
            chipHealth.setChecked(true); // default category
        }
    }

    private void initViews() {
        btnClose = findViewById(R.id.btn_close);
        btnDeleteTask = findViewById(R.id.btn_delete_task);
        etTaskTitle = findViewById(R.id.et_task_title);
        chipGroupCategory = findViewById(R.id.chip_group_category);
        chipHealth = findViewById(R.id.chip_health);
        chipWork = findViewById(R.id.chip_work);
        chipMental = findViewById(R.id.chip_mental);
        chipOthers = findViewById(R.id.chip_others);

        spinnerStartHour = findViewById(R.id.spinner_start_hour);
        spinnerDuration = findViewById(R.id.spinner_duration);

        layoutDetailSubtasks = findViewById(R.id.layout_detail_subtasks);
        btnAddSubtaskRow = findViewById(R.id.btn_add_subtask_row);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupSpinners() {
        ArrayAdapter<String> hourAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, hourLabels);
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStartHour.setAdapter(hourAdapter);

        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, durationLabels);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(durationAdapter);
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());

        btnDeleteTask.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        dbHelper.deleteTask(taskId);
                        Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnAddSubtaskRow.setOnClickListener(v -> showAddSubtaskDialog());

        btnSave.setOnClickListener(v -> saveTask());
    }

    private void loadExistingTask(long id) {
        List<Task> all = dbHelper.getTasks(null, null);
        for (Task t : all) {
            if (t.getId() == id) {
                currentTask = t;
                break;
            }
        }

        if (currentTask != null) {
            btnDeleteTask.setVisibility(View.VISIBLE);
            etTaskTitle.setText(currentTask.getTitle());

            // Select category
            switch (currentTask.getCategory()) {
                case HEALTH: chipHealth.setChecked(true); break;
                case WORK: chipWork.setChecked(true); break;
                case MENTAL_HEALTH: chipMental.setChecked(true); break;
                case OTHERS: chipOthers.setChecked(true); break;
            }

            // Select hour
            int hourIndex = 0;
            for (int i = 0; i < hourValues.length; i++) {
                if (hourValues[i] == currentTask.getStartHour()) {
                    hourIndex = i;
                    break;
                }
            }
            spinnerStartHour.setSelection(hourIndex);

            // Select duration
            int durationIndex = 0;
            for (int i = 0; i < durationValues.length; i++) {
                if (durationValues[i] == currentTask.getDurationHours()) {
                    durationIndex = i;
                    break;
                }
            }
            spinnerDuration.setSelection(durationIndex);

            // Load subtasks
            currentSubtasks.clear();
            if (currentTask.getSubtasks() != null) {
                currentSubtasks.addAll(currentTask.getSubtasks());
            }
            renderSubtasks();
        }
    }

    private void renderSubtasks() {
        layoutDetailSubtasks.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < currentSubtasks.size(); i++) {
            final int index = i;
            final Subtask subtask = currentSubtasks.get(i);

            View view = inflater.inflate(R.layout.item_subtask_row, layoutDetailSubtasks, false);
            CheckBox cb = view.findViewById(R.id.cb_subtask);
            TextView tv = view.findViewById(R.id.tv_subtask_title);
            ImageView ivDelete = view.findViewById(R.id.iv_delete_subtask);

            tv.setText(subtask.getTitle());
            cb.setChecked(subtask.isCompleted());
            ivDelete.setVisibility(View.VISIBLE);

            cb.setOnCheckedChangeListener((bv, checked) -> subtask.setCompleted(checked));

            ivDelete.setOnClickListener(v -> {
                currentSubtasks.remove(index);
                renderSubtasks();
            });

            layoutDetailSubtasks.addView(view);
        }
    }

    private void showAddSubtaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Subtask");

        final EditText input = new EditText(this);
        input.setHint("Subtask title");
        input.setPadding(48, 32, 48, 32);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (!TextUtils.isEmpty(title)) {
                currentSubtasks.add(new Subtask(title));
                renderSubtasks();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveTask() {
        String title = etTaskTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            etTaskTitle.setError("Please enter task title");
            etTaskTitle.requestFocus();
            return;
        }

        Category category = Category.OTHERS;
        int checkedChipId = chipGroupCategory.getCheckedChipId();
        if (checkedChipId == R.id.chip_health) {
            category = Category.HEALTH;
        } else if (checkedChipId == R.id.chip_work) {
            category = Category.WORK;
        } else if (checkedChipId == R.id.chip_mental) {
            category = Category.MENTAL_HEALTH;
        } else if (checkedChipId == R.id.chip_others) {
            category = Category.OTHERS;
        }

        int startHour = hourValues[spinnerStartHour.getSelectedItemPosition()];
        int duration = durationValues[spinnerDuration.getSelectedItemPosition()];

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateStr = (currentTask != null && currentTask.getDate() != null)
                ? currentTask.getDate()
                : dbFormat.format(new Date());

        if (currentTask == null) {
            // New task
            Task newTask = new Task(-1, title, category, dateStr, startHour, duration, false);
            newTask.setSubtasks(currentSubtasks);
            dbHelper.insertTask(newTask);
            Toast.makeText(this, "Task created", Toast.LENGTH_SHORT).show();
        } else {
            // Update existing
            currentTask.setTitle(title);
            currentTask.setCategory(category);
            currentTask.setStartHour(startHour);
            currentTask.setDurationHours(duration);
            currentTask.setSubtasks(currentSubtasks);
            dbHelper.updateTask(currentTask);
            Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
