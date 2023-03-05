package kanban.model;

import kanban.exceptions.TaskException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    public Task task;
    public ZonedDateTime startTime = ZonedDateTime.of(
            2023,
            3,
            4,
            15,
            0,
            0,
            0,
            ZoneId.of("UTC+03:00"));

    @BeforeEach
    public void beforeEach() {
        task = new Task("a", "b");
    }

    @Test
    public void setTimeProperties() {
        TaskException exception = assertThrows(TaskException.class,
                () -> task.setTimeProperties(null, 15));
        assertEquals("Task can not have null startTime", exception.getMessage());

        exception = assertThrows(TaskException.class,
                () -> task.setTimeProperties(startTime, -60));
        assertEquals("Task can not have negative duration", exception.getMessage());

        exception = assertThrows(TaskException.class,
                () -> task.setTimeProperties(startTime, 0));
        assertEquals("Task can not have 0 duration", exception.getMessage());

        task.setTimeProperties(startTime, 60);
        assertEquals(startTime, task.getStartTime());
        assertEquals(60, task.getDuration());
    }

    @Test
    public void resetTimeProperties() {
        assertFalse(task.areTimePropertiesSet());

        task.resetTimeProperties();
        assertFalse(task.areTimePropertiesSet());

        task.setTimeProperties(startTime, 60);
        assertTrue(task.areTimePropertiesSet());
        task.resetTimeProperties();
        assertFalse(task.areTimePropertiesSet());
    }

    @Test
    public void getEndTime() {
        TaskException exception = assertThrows(TaskException.class, () -> task.getEndTime());
        assertEquals("Can not getEndTime from task with no TimeProperties", exception.getMessage());

        task.setTimeProperties(startTime, 60);
        assertTrue(task.getEndTime().isEqual(startTime.plusMinutes(60)));
    }

    @Test
    public void changeZoneId() {
        TaskException exception = assertThrows(TaskException.class, () -> task.changeZoneId(null));
        assertEquals("Can not change ZoneId to null", exception.getMessage());

        ZoneId newZoneId = ZoneId.of("UTC");
        String stringOfTaskWithNoTimePropertiesBeforeChangeZoneId = task.toString();
        task.changeZoneId(newZoneId);
        assertEquals(stringOfTaskWithNoTimePropertiesBeforeChangeZoneId, task.toString());

        task.setTimeProperties(startTime, 60);
        task.changeZoneId(newZoneId);
        assertNotEquals(startTime, task.getStartTime());
        assertTrue(startTime.isEqual(task.getStartTime()));
    }
}