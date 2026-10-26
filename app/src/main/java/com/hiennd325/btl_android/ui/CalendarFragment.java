package com.hiennd325.btl_android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hiennd325.btl_android.R;
import com.hiennd325.btl_android.adapter.TimelineAdapter;
import com.hiennd325.btl_android.adapter.WeekDayAdapter;
import com.hiennd325.btl_android.database.DatabaseHelper;
import com.hiennd325.btl_android.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment implements WeekDayAdapter.OnDaySelectedListener, TimelineAdapter.OnTimelineTaskClickListener {

    private TextView tvCalendarHeaderDate;
    private RecyclerView rvWeekDays;
    private RecyclerView rvTimeline;

    private WeekDayAdapter weekDayAdapter;
    private TimelineAdapter timelineAdapter;
    private DatabaseHelper dbHelper;

    private String selectedDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());

        tvCalendarHeaderDate = view.findViewById(R.id.tv_calendar_header_date);
        rvWeekDays = view.findViewById(R.id.rv_week_days);
        rvTimeline = view.findViewById(R.id.rv_timeline);

        // Week strip setup
        rvWeekDays.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        weekDayAdapter = new WeekDayAdapter(requireContext(), this);
        rvWeekDays.setAdapter(weekDayAdapter);

        // Timeline setup
        rvTimeline.setLayoutManager(new LinearLayoutManager(requireContext()));
        timelineAdapter = new TimelineAdapter(requireContext(), this);
        rvTimeline.setAdapter(timelineAdapter);

        // Initial selection
        WeekDayAdapter.DayItem currentDay = weekDayAdapter.getSelectedDay();
        if (currentDay != null) {
            updateSelectedDay(currentDay);
        } else {
            SimpleDateFormat headerFormat = new SimpleDateFormat("d MMM", Locale.US);
            SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date now = new Date();
            selectedDate = dbFormat.format(now);
            tvCalendarHeaderDate.setText(headerFormat.format(now));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTimeline();
    }

    private void updateSelectedDay(WeekDayAdapter.DayItem day) {
        selectedDate = day.dateString;
        SimpleDateFormat headerFormat = new SimpleDateFormat("d MMM", Locale.US);
        tvCalendarHeaderDate.setText(headerFormat.format(day.date));
        loadTimeline();
    }

    public void loadTimeline() {
        if (selectedDate == null) return;
        List<Task> scheduled = dbHelper.getScheduledTasksForDate(selectedDate);
        timelineAdapter.setScheduledTasks(scheduled);
    }

    @Override
    public void onDaySelected(WeekDayAdapter.DayItem day) {
        updateSelectedDay(day);
    }

    @Override
    public void onTimelineTaskClick(Task task) {
        Intent intent = new Intent(requireContext(), TaskDetailActivity.class);
        intent.putExtra("TASK_ID", task.getId());
        startActivity(intent);
    }
}
