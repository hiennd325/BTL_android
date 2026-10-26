package com.hiennd325.btl_android.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hiennd325.btl_android.R;
import com.hiennd325.btl_android.adapter.TaskAdapter;
import com.hiennd325.btl_android.database.DatabaseHelper;
import com.hiennd325.btl_android.model.Category;
import com.hiennd325.btl_android.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TodayFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private TextView tvHeaderDate;
    private TextView tvCatHealthCount;
    private TextView tvCatWorkCount;
    private TextView tvCatMentalCount;
    private TextView tvCatOthersCount;
    private TextView tvFilterStatus;

    private CardView cardHealth;
    private CardView cardWork;
    private CardView cardMental;
    private CardView cardOthers;

    private RecyclerView rvTasks;
    private TaskAdapter taskAdapter;
    private DatabaseHelper dbHelper;

    private String currentDate;
    private Category selectedCategoryFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());

        // Format dates
        SimpleDateFormat headerDateFormat = new SimpleDateFormat("d MMM", Locale.US);
        SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date now = new Date();
        currentDate = dbDateFormat.format(now);

        // Pre-seed sample data if empty
        dbHelper.seedSampleDataIfEmpty(currentDate);

        // Init views
        tvHeaderDate = view.findViewById(R.id.tv_header_date);
        tvHeaderDate.setText(headerDateFormat.format(now));

        tvCatHealthCount = view.findViewById(R.id.tv_cat_health_count);
        tvCatWorkCount = view.findViewById(R.id.tv_cat_work_count);
        tvCatMentalCount = view.findViewById(R.id.tv_cat_mental_count);
        tvCatOthersCount = view.findViewById(R.id.tv_cat_others_count);
        tvFilterStatus = view.findViewById(R.id.tv_filter_status);

        cardHealth = view.findViewById(R.id.card_health);
        cardWork = view.findViewById(R.id.card_work);
        cardMental = view.findViewById(R.id.card_mental);
        cardOthers = view.findViewById(R.id.card_others);

        rvTasks = view.findViewById(R.id.rv_tasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(requireContext(), this);
        rvTasks.setAdapter(taskAdapter);

        setupCategoryClickListeners();
        setupSwipeToDelete();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    public void loadData() {
        // Load category counts
        Map<Category, Integer> counts = dbHelper.getCategoryCounts(currentDate);
        tvCatHealthCount.setText(counts.get(Category.HEALTH) + " Health");
        tvCatWorkCount.setText(counts.get(Category.WORK) + " Work");
        tvCatMentalCount.setText(counts.get(Category.MENTAL_HEALTH) + " Mental Health");
        tvCatOthersCount.setText(counts.get(Category.OTHERS) + " Others");

        // Load tasks with category filter
        List<Task> tasks = dbHelper.getTasks(currentDate, selectedCategoryFilter);
        taskAdapter.setTasks(tasks);

        if (selectedCategoryFilter != null) {
            tvFilterStatus.setVisibility(View.VISIBLE);
            tvFilterStatus.setText("Filtered by: " + selectedCategoryFilter.getDisplayName() + " (tap card to clear)");
        } else {
            tvFilterStatus.setVisibility(View.GONE);
        }
    }

    private void setupCategoryClickListeners() {
        cardHealth.setOnClickListener(v -> toggleCategoryFilter(Category.HEALTH));
        cardWork.setOnClickListener(v -> toggleCategoryFilter(Category.WORK));
        cardMental.setOnClickListener(v -> toggleCategoryFilter(Category.MENTAL_HEALTH));
        cardOthers.setOnClickListener(v -> toggleCategoryFilter(Category.OTHERS));
    }

    private void toggleCategoryFilter(Category category) {
        if (selectedCategoryFilter == category) {
            selectedCategoryFilter = null;
        } else {
            selectedCategoryFilter = category;
        }
        loadData();
    }

    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            private final Paint paint = new Paint();

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                taskAdapter.removeTaskAt(position);
                loadData(); // Update counts
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    View itemView = viewHolder.itemView;

                    // Draw red background
                    paint.setColor(ContextCompat.getColor(requireContext(), R.color.delete_red));
                    RectF background = new RectF(
                            itemView.getRight() + dX,
                            itemView.getTop(),
                            itemView.getRight(),
                            itemView.getBottom()
                    );
                    c.drawRect(background, paint);

                    // Draw white "Delete" text matching mockup
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(spToPx(15));
                    paint.setAntiAlias(true);
                    paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    paint.setTextAlign(Paint.Align.CENTER);

                    float textX = itemView.getRight() - (Math.abs(dX) / 2);
                    float textY = itemView.getTop() + ((float) (itemView.getBottom() - itemView.getTop()) / 2) + (paint.getTextSize() / 3);

                    if (Math.abs(dX) > 100) {
                        c.drawText("Delete", textX, textY, paint);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(rvTasks);
    }

    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(requireContext(), TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskStatusChanged(Task task, boolean isCompleted) {
        // Can reload data or counts if needed
    }
}
