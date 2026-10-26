package com.hiennd325.btl_android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Task implements Serializable {
    private long id;
    private String title;
    private Category category;
    private String date; // Format: "yyyy-MM-dd"
    private int startHour; // -1 if not scheduled, or 6 to 18
    private int durationHours; // e.g., 1, 2, 4
    private boolean isCompleted;
    private List<Subtask> subtasks;

    public Task() {
        this.category = Category.OTHERS;
        this.startHour = -1;
        this.durationHours = 1;
        this.isCompleted = false;
        this.subtasks = new ArrayList<>();
    }

    public Task(long id, String title, Category category, String date, int startHour, int durationHours, boolean isCompleted) {
        this.id = id;
        this.title = title;
        this.category = category != null ? category : Category.OTHERS;
        this.date = date;
        this.startHour = startHour;
        this.durationHours = durationHours > 0 ? durationHours : 1;
        this.isCompleted = isCompleted;
        this.subtasks = new ArrayList<>();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getStartHour() { return startHour; }
    public void setStartHour(int startHour) { this.startHour = startHour; }

    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public List<Subtask> getSubtasks() { return subtasks; }
    public void setSubtasks(List<Subtask> subtasks) { this.subtasks = subtasks; }

    public boolean hasSubtasks() {
        return subtasks != null && !subtasks.isEmpty();
    }
}
