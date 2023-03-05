package kanban.model;

import java.time.ZonedDateTime;

public class OccupiedTimeInterval<T> extends TimeInterval {
    private final T value;

    public OccupiedTimeInterval(ZonedDateTime from, ZonedDateTime to, T value) {
        super(from, to);
        this.value = value;
    }

    public OccupiedTimeInterval(TimeInterval timeInterval, T value) {
        super(timeInterval.getFrom(), timeInterval.getTo());
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}