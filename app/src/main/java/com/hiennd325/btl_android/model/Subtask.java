package com.hiennd325.btl_android.model;

import java.io.Serializable;

public class Subtask implements Serializable {
    private long id;
    private long taskId;
    private String title;
    private boolean isCompleted;

    public Subtask() {}

    public Subtask(long id, long taskId, String title, boolean isCompleted) {
        this.id = id;
        this.taskId = taskId;
        this.title = title;
        this.isCompleted = isCompleted;
    }

    public Subtask(String title) {
        this(-1, -1, title, false);
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}
