package kanban.model;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeIntervalTest {
    ZonedDateTime firstTimePoint = ZonedDateTime.of(
            2023,
            3,
            4,
            15,
            0,
            0,
            0,
            ZoneId.of("UTC+03:00"));
    ZonedDateTime secondTimePoint = firstTimePoint.plusMinutes(5);
    ZonedDateTime thirdTimePoint = secondTimePoint.plusMinutes(5);
    ZonedDateTime forthTimePoint = thirdTimePoint.plusMinutes(5);

    @Test
    public void doIntersect() {
        assertFalse(TimeInterval.doIntersect(
                new TimeInterval(firstTimePoint, secondTimePoint),
                new TimeInterval(thirdTimePoint, forthTimePoint)
        ));
        assertFalse(TimeInterval.doIntersect(
                new TimeInterval(thirdTimePoint, forthTimePoint),
                new TimeInterval(firstTimePoint, secondTimePoint)
        ));

        assertFalse(TimeInterval.doIntersect(
                new TimeInterval(firstTimePoint, secondTimePoint),
                new TimeInterval(secondTimePoint, thirdTimePoint)
        ));
        assertFalse(TimeInterval.doIntersect(
                new TimeInterval(secondTimePoint, thirdTimePoint),
                new TimeInterval(firstTimePoint, secondTimePoint)
        ));

        assertTrue(TimeInterval.doIntersect(
                new TimeInterval(firstTimePoint, thirdTimePoint),
                new TimeInterval(secondTimePoint, forthTimePoint)
        ));
        assertTrue(TimeInterval.doIntersect(
                new TimeInterval(secondTimePoint, forthTimePoint),
                new TimeInterval(firstTimePoint, thirdTimePoint)
        ));

        assertTrue(TimeInterval.doIntersect(
                new TimeInterval(firstTimePoint, secondTimePoint),
                new TimeInterval(firstTimePoint, thirdTimePoint)
        ));
        assertTrue(TimeInterval.doIntersect(
                new TimeInterval(firstTimePoint, thirdTimePoint),
                new TimeInterval(firstTimePoint, secondTimePoint)
        ));

        assertTrue(TimeInterval.doIntersect(
                new TimeInterval(firstTimePoint, thirdTimePoint),
                new TimeInterval(secondTimePoint, thirdTimePoint)
        ));
        assertTrue(TimeInterval.doIntersect(
                new TimeInterval(secondTimePoint, thirdTimePoint),
                new TimeInterval(firstTimePoint, thirdTimePoint)
        ));

        assertTrue(TimeInterval.doIntersect(
                new TimeInterval(firstTimePoint, forthTimePoint),
                new TimeInterval(secondTimePoint, thirdTimePoint)
        ));
        assertTrue(TimeInterval.doIntersect(
                new TimeInterval(secondTimePoint, thirdTimePoint),
                new TimeInterval(firstTimePoint, forthTimePoint)
        ));
    }

    @Test
    public void getIntersection() {
        assertEquals(new TimeInterval(secondTimePoint, thirdTimePoint),
                TimeInterval.getIntersection(
                        new TimeInterval(firstTimePoint, thirdTimePoint),
                        new TimeInterval(secondTimePoint, forthTimePoint)
                )
        );
        assertEquals(new TimeInterval(secondTimePoint, thirdTimePoint),
                TimeInterval.getIntersection(
                        new TimeInterval(secondTimePoint, forthTimePoint),
                        new TimeInterval(firstTimePoint, thirdTimePoint)
                )
        );

        assertEquals(new TimeInterval(firstTimePoint, secondTimePoint),
                TimeInterval.getIntersection(
                        new TimeInterval(firstTimePoint, secondTimePoint),
                        new TimeInterval(firstTimePoint, thirdTimePoint)
                )
        );
        assertEquals(new TimeInterval(firstTimePoint, secondTimePoint),
                TimeInterval.getIntersection(
                        new TimeInterval(firstTimePoint, thirdTimePoint),
                        new TimeInterval(firstTimePoint, secondTimePoint)
                )
        );

        assertEquals(new TimeInterval(secondTimePoint, thirdTimePoint),
                TimeInterval.getIntersection(
                        new TimeInterval(firstTimePoint, thirdTimePoint),
                        new TimeInterval(secondTimePoint, thirdTimePoint)
                )
        );
        assertEquals(new TimeInterval(secondTimePoint, thirdTimePoint),
                TimeInterval.getIntersection(
                        new TimeInterval(secondTimePoint, thirdTimePoint),
                        new TimeInterval(firstTimePoint, thirdTimePoint)
                )
        );

        assertEquals(new TimeInterval(secondTimePoint, thirdTimePoint),
                TimeInterval.getIntersection(
                        new TimeInterval(firstTimePoint, forthTimePoint),
                        new TimeInterval(secondTimePoint, thirdTimePoint)
                )
        );
        assertEquals(new TimeInterval(secondTimePoint, thirdTimePoint),
                TimeInterval.getIntersection(
                        new TimeInterval(secondTimePoint, thirdTimePoint),
                        new TimeInterval(firstTimePoint, forthTimePoint)
                )
        );
    }
}