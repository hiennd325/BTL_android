package com.hiennd325.btl_android.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hiennd325.btl_android.model.Category;
import com.hiennd325.btl_android.model.Subtask;
import com.hiennd325.btl_android.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "todo_app.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_TASKS = "tasks";
    public static final String TABLE_SUBTASKS = "subtasks";

    // Task columns
    public static final String COL_TASK_ID = "id";
    public static final String COL_TASK_TITLE = "title";
    public static final String COL_TASK_CATEGORY = "category";
    public static final String COL_TASK_DATE = "date";
    public static final String COL_TASK_START_HOUR = "start_hour";
    public static final String COL_TASK_DURATION = "duration_hours";
    public static final String COL_TASK_COMPLETED = "is_completed";

    // Subtask columns
    public static final String COL_SUBTASK_ID = "id";
    public static final String COL_SUBTASK_TASK_ID = "task_id";
    public static final String COL_SUBTASK_TITLE = "title";
    public static final String COL_SUBTASK_COMPLETED = "is_completed";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTasksTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_TITLE + " TEXT NOT NULL, "
                + COL_TASK_CATEGORY + " TEXT, "
                + COL_TASK_DATE + " TEXT, "
                + COL_TASK_START_HOUR + " INTEGER DEFAULT -1, "
                + COL_TASK_DURATION + " INTEGER DEFAULT 1, "
                + COL_TASK_COMPLETED + " INTEGER DEFAULT 0)";

        String createSubtasksTable = "CREATE TABLE " + TABLE_SUBTASKS + " ("
                + COL_SUBTASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SUBTASK_TASK_ID + " INTEGER, "
                + COL_SUBTASK_TITLE + " TEXT NOT NULL, "
                + COL_SUBTASK_COMPLETED + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + COL_SUBTASK_TASK_ID + ") REFERENCES " + TABLE_TASKS + "(" + COL_TASK_ID + ") ON DELETE CASCADE)";

        db.execSQL(createTasksTable);
        db.execSQL(createSubtasksTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUBTASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ==================== TASK OPERATIONS ====================

    public long insertTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, task.getTitle());
        cv.put(COL_TASK_CATEGORY, task.getCategory().name());
        cv.put(COL_TASK_DATE, task.getDate());
        cv.put(COL_TASK_START_HOUR, task.getStartHour());
        cv.put(COL_TASK_DURATION, task.getDurationHours());
        cv.put(COL_TASK_COMPLETED, task.isCompleted() ? 1 : 0);

        long id = db.insert(TABLE_TASKS, null, cv);
        task.setId(id);

        if (task.getSubtasks() != null) {
            for (Subtask s : task.getSubtasks()) {
                s.setTaskId(id);
                insertSubtask(s);
            }
        }
        return id;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_TITLE, task.getTitle());
        cv.put(COL_TASK_CATEGORY, task.getCategory().name());
        cv.put(COL_TASK_DATE, task.getDate());
        cv.put(COL_TASK_START_HOUR, task.getStartHour());
        cv.put(COL_TASK_DURATION, task.getDurationHours());
        cv.put(COL_TASK_COMPLETED, task.isCompleted() ? 1 : 0);

        int rows = db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});

        // Replace subtasks
        db.delete(TABLE_SUBTASKS, COL_SUBTASK_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
        if (task.getSubtasks() != null) {
            for (Subtask s : task.getSubtasks()) {
                s.setTaskId(task.getId());
                insertSubtask(s);
            }
        }
        return rows;
    }

    public void updateTaskCompleted(long taskId, boolean isCompleted) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TASK_COMPLETED, isCompleted ? 1 : 0);
        db.update(TABLE_TASKS, cv, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public int deleteTask(long taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SUBTASKS, COL_SUBTASK_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
        return db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public List<Task> getTasks(String dateFilter, Category categoryFilter) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder query = new StringBuilder("SELECT * FROM " + TABLE_TASKS + " WHERE 1=1");
        List<String> args = new ArrayList<>();

        if (dateFilter != null && !dateFilter.isEmpty()) {
            query.append(" AND " + COL_TASK_DATE + " = ?");
            args.add(dateFilter);
        }
        if (categoryFilter != null) {
            query.append(" AND " + COL_TASK_CATEGORY + " = ?");
            args.add(categoryFilter.name());
        }

        query.append(" ORDER BY " + COL_TASK_COMPLETED + " ASC, " + COL_TASK_ID + " ASC");

        Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]));
        if (cursor.moveToFirst()) {
            do {
                Task t = parseTask(cursor);
                t.setSubtasks(getSubtasksForTask(t.getId()));
                list.add(t);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Task> getScheduledTasksForDate(String date) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_TASKS + " WHERE " + COL_TASK_DATE + " = ? AND " + COL_TASK_START_HOUR + " >= 0 ORDER BY " + COL_TASK_START_HOUR + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{date});
        if (cursor.moveToFirst()) {
            do {
                list.add(parseTask(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public Map<Category, Integer> getCategoryCounts(String date) {
        Map<Category, Integer> counts = new HashMap<>();
        for (Category c : Category.values()) {
            counts.put(c, 0);
        }

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COL_TASK_CATEGORY + ", COUNT(*) FROM " + TABLE_TASKS
                + " WHERE " + COL_TASK_DATE + " = ? GROUP BY " + COL_TASK_CATEGORY;
        Cursor cursor = db.rawQuery(query, new String[]{date});
        if (cursor.moveToFirst()) {
            do {
                String catName = cursor.getString(0);
                int count = cursor.getInt(1);
                Category cat = Category.fromString(catName);
                counts.put(cat, count);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return counts;
    }

    private Task parseTask(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TASK_ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE));
        String catName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_CATEGORY));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DATE));
        int startHour = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_START_HOUR));
        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_DURATION));
        boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1;

        return new Task(id, title, Category.fromString(catName), date, startHour, duration, completed);
    }

    // ==================== SUBTASK OPERATIONS ====================

    public long insertSubtask(Subtask subtask) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SUBTASK_TASK_ID, subtask.getTaskId());
        cv.put(COL_SUBTASK_TITLE, subtask.getTitle());
        cv.put(COL_SUBTASK_COMPLETED, subtask.isCompleted() ? 1 : 0);
        long id = db.insert(TABLE_SUBTASKS, null, cv);
        subtask.setId(id);
        return id;
    }

    public void updateSubtaskCompleted(long subtaskId, boolean isCompleted) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_SUBTASK_COMPLETED, isCompleted ? 1 : 0);
        db.update(TABLE_SUBTASKS, cv, COL_SUBTASK_ID + " = ?", new String[]{String.valueOf(subtaskId)});
    }

    public List<Subtask> getSubtasksForTask(long taskId) {
        List<Subtask> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SUBTASKS, null, COL_SUBTASK_TASK_ID + " = ?",
                new String[]{String.valueOf(taskId)}, null, null, COL_SUBTASK_ID + " ASC");
        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SUBTASK_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBTASK_TITLE));
                boolean completed = cursor.getInt(cursor.getColumnIndexOrThrow(COL_SUBTASK_COMPLETED)) == 1;
                list.add(new Subtask(id, taskId, title, completed));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // ==================== SEED SAMPLE DATA ====================

    public void seedSampleDataIfEmpty(String currentDate) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS, null);
        boolean isEmpty = true;
        if (cursor.moveToFirst()) {
            isEmpty = cursor.getInt(0) == 0;
        }
        cursor.close();

        if (isEmpty) {
            // Task 1: "Drink 8 glasses of water" (Health, 6:00, 1h)
            Task t1 = new Task(-1, "Drink 8 glasses of water", Category.HEALTH, currentDate, 6, 1, false);
            insertTask(t1);

            // Task 2: "Edit the PDF" (Work, not scheduled)
            Task t2 = new Task(-1, "Edit the PDF", Category.WORK, currentDate, -1, 1, false);
            insertTask(t2);

            // Task 3: "Write in a gratitude journal" (Mental Health, with subtasks)
            Task t3 = new Task(-1, "Write in a gratitude journal", Category.MENTAL_HEALTH, currentDate, -1, 1, false);
            List<Subtask> subtasks3 = new ArrayList<>();
            subtasks3.add(new Subtask("Get a notebook"));
            subtasks3.add(new Subtask("Follow the youtube tutorial"));
            t3.setSubtasks(subtasks3);
            insertTask(t3);

            // Task 4: "Stretch everyday for 15 mins" (Health)
            Task t4 = new Task(-1, "Stretch everyday for 15 mins", Category.HEALTH, currentDate, -1, 1, false);
            insertTask(t4);

            // Additional tasks to match counts and calendar timeline
            // Task 5: "Get a notebook" (Mental Health, 9:00, 1h)
            Task t5 = new Task(-1, "Get a notebook", Category.MENTAL_HEALTH, currentDate, 9, 1, false);
            insertTask(t5);

            // Task 6: "Work" (Work, 10:00, 4h)
            Task t6 = new Task(-1, "Work", Category.WORK, currentDate, 10, 4, false);
            insertTask(t6);

            // More sample tasks for counts (Work: 5, Health: 6, Mental: 4, Others: 13)
            insertTask(new Task(-1, "Team sync meeting", Category.WORK, currentDate, -1, 1, false));
            insertTask(new Task(-1, "Review PRs", Category.WORK, currentDate, -1, 1, false));
            insertTask(new Task(-1, "Send report email", Category.WORK, currentDate, -1, 1, false));

            insertTask(new Task(-1, "Morning run 3km", Category.HEALTH, currentDate, -1, 1, false));
            insertTask(new Task(-1, "Take vitamin D", Category.HEALTH, currentDate, -1, 1, false));
            insertTask(new Task(-1, "Sleep before 11 PM", Category.HEALTH, currentDate, -1, 1, false));
            insertTask(new Task(-1, "Eye rest exercise", Category.HEALTH, currentDate, -1, 1, false));

            insertTask(new Task(-1, "10 mins meditation", Category.MENTAL_HEALTH, currentDate, -1, 1, false));
            insertTask(new Task(-1, "Read book chapter 5", Category.MENTAL_HEALTH, currentDate, -1, 1, false));

            for (int i = 1; i <= 13; i++) {
                insertTask(new Task(-1, "Daily chore #" + i, Category.OTHERS, currentDate, -1, 1, false));
            }
        }
    }
}
