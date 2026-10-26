package com.hiennd325.btl_android.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hiennd325.btl_android.R;
import com.hiennd325.btl_android.model.Category;
import com.hiennd325.btl_android.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder> {

    public interface OnTimelineTaskClickListener {
        void onTimelineTaskClick(Task task);
    }

    private final Context context;
    private final List<Integer> hours = new ArrayList<>();
    private final Map<Integer, Task> scheduledTasks = new HashMap<>();
    private final OnTimelineTaskClickListener listener;

    public TimelineAdapter(Context context, OnTimelineTaskClickListener listener) {
        this.context = context;
        this.listener = listener;

        // Generate hours from 6:00 to 18:00
        for (int h = 6; h <= 18; h++) {
            hours.add(h);
        }
    }

    public void setScheduledTasks(List<Task> tasks) {
        scheduledTasks.clear();
        if (tasks != null) {
            for (Task t : tasks) {
                if (t.getStartHour() >= 6 && t.getStartHour() <= 18) {
                    scheduledTasks.put(t.getStartHour(), t);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimelineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline_hour, parent, false);
        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineViewHolder holder, int position) {
        int hour = hours.get(position);
        holder.tvHourLabel.setText(String.format(Locale.US, "%d:00", hour));

        Task task = scheduledTasks.get(hour);
        if (task != null) {
            holder.cardEvent.setVisibility(View.VISIBLE);
            holder.tvEventTitle.setText(task.getTitle());
            holder.tvEventDuration.setText(task.getDurationHours() + "h");

            Category cat = task.getCategory();
            holder.cardEvent.setCardBackgroundColor(ContextCompat.getColor(context, cat.getCardBgColorRes()));

            // Set dot color
            GradientDrawable dotDrawable = new GradientDrawable();
            dotDrawable.setShape(GradientDrawable.OVAL);
            dotDrawable.setColor(ContextCompat.getColor(context, cat.getAccentColorRes()));
            holder.viewEventDot.setBackground(dotDrawable);

            holder.cardEvent.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTimelineTaskClick(task);
                }
            });
        } else {
            holder.cardEvent.setVisibility(View.GONE);
            holder.cardEvent.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return hours.size();
    }

    static class TimelineViewHolder extends RecyclerView.ViewHolder {
        TextView tvHourLabel;
        CardView cardEvent;
        View viewEventDot;
        TextView tvEventTitle;
        TextView tvEventDuration;

        public TimelineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHourLabel = itemView.findViewById(R.id.tv_hour_label);
            cardEvent = itemView.findViewById(R.id.card_timeline_event);
            viewEventDot = itemView.findViewById(R.id.view_event_dot);
            tvEventTitle = itemView.findViewById(R.id.tv_event_title);
            tvEventDuration = itemView.findViewById(R.id.tv_event_duration);
        }
    }
}
