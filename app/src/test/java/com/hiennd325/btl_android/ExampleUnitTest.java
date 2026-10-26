package com.hiennd325.btl_android;

import org.junit.Test;

import static org.junit.Assert.*;

import com.hiennd325.btl_android.model.Category;
import com.hiennd325.btl_android.model.Subtask;
import com.hiennd325.btl_android.model.Task;

import java.util.ArrayList;
import java.util.List;

public class ExampleUnitTest {

    @Test
    public void testTaskAndSubtasks() {
        Task task = new Task(1, "Stretch everyday for 15 mins", Category.HEALTH, "2026-12-26", 6, 1, false);
        assertEquals("Stretch everyday for 15 mins", task.getTitle());
        assertEquals(Category.HEALTH, task.getCategory());
        assertEquals("HEALTH", task.getCategory().getBadgeText());

        List<Subtask> subtasks = new ArrayList<>();
        subtasks.add(new Subtask(10, 1, "Get a notebook", false));
        subtasks.add(new Subtask(11, 1, "Follow the youtube tutorial", true));
        task.setSubtasks(subtasks);

        assertTrue(task.hasSubtasks());
        assertEquals(2, task.getSubtasks().size());
        assertTrue(task.getSubtasks().get(1).isCompleted());
    }

    @Test
    public void testCategoryParsing() {
        assertEquals(Category.HEALTH, Category.fromString("Health"));
        assertEquals(Category.WORK, Category.fromString("WORK"));
        assertEquals(Category.MENTAL_HEALTH, Category.fromString("Mental Health"));
        assertEquals(Category.OTHERS, Category.fromString("Unknown"));
    }
}
