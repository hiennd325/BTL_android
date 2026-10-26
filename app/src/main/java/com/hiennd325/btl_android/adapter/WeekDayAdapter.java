package com.hiennd325.btl_android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hiennd325.btl_android.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeekDayAdapter extends RecyclerView.Adapter<WeekDayAdapter.DayViewHolder> {

    public static class DayItem {
        public String dayName;   // e.g. "THU"
        public String dayNumber; // e.g. "26"
        public String dateString;// e.g. "2026-12-26"
        public Date date;

        public DayItem(String dayName, String dayNumber, String dateString, Date date) {
            this.dayName = dayName;
            this.dayNumber = dayNumber;
            this.dateString = dateString;
            this.date = date;
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(DayItem day);
    }

    private final Context context;
    private final List<DayItem> days = new ArrayList<>();
    private int selectedPosition = 0;
    private final OnDaySelectedListener listener;

    public WeekDayAdapter(Context context, OnDaySelectedListener listener) {
        this.context = context;
        this.listener = listener;
        generateWeekDays();
    }

    private void generateWeekDays() {
        Calendar cal = Calendar.getInstance();
        // Start 1 day before today or current week
        cal.add(Calendar.DAY_OF_YEAR, -1);

        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.US);
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("d", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        days.clear();
        for (int i = 0; i < 14; i++) {
            Date d = cal.getTime();
            days.add(new DayItem(
                    dayNameFormat.format(d).toUpperCase(),
                    dayNumberFormat.format(d),
                    dateFormat.format(d),
                    d
            ));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        selectedPosition = 1; // Default to index 1 (Today)
    }

    public DayItem getSelectedDay() {
        if (selectedPosition >= 0 && selectedPosition < days.size()) {
            return days.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_week_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayItem item = days.get(position);
        boolean isSelected = (position == selectedPosition);

        holder.tvDayName.setText(item.dayName);
        holder.tvDayNumber.setText(item.dayNumber);

        if (isSelected) {
            holder.layoutDayBox.setBackgroundResource(R.drawable.bg_day_selected);
            holder.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.text_black));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_black));
        } else {
            holder.layoutDayBox.setBackgroundResource(R.drawable.bg_day_normal);
            holder.tvDayName.setTextColor(ContextCompat.getColor(context, R.color.text_gray));
            holder.tvDayNumber.setTextColor(ContextCompat.getColor(context, R.color.text_black));
        }

        holder.itemView.setOnClickListener(v -> {
            int previousPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousPos);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onDaySelected(days.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        View layoutDayBox;
        TextView tvDayName;
        TextView tvDayNumber;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutDayBox = itemView.findViewById(R.id.layout_day_box);
            tvDayName = itemView.findViewById(R.id.tv_day_name);
            tvDayNumber = itemView.findViewById(R.id.tv_day_number);
        }
    }
}
