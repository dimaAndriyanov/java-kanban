package kanban.model;

import kanban.exceptions.TimeIntervalException;

import java.time.ZonedDateTime;
import java.util.Objects;

public class TimeInterval {
    private final ZonedDateTime from;
    private final ZonedDateTime to;

    public TimeInterval(ZonedDateTime from, ZonedDateTime to) {
        if (from == null || to == null) {
            throw new TimeIntervalException("Can not create TimeInterval with null borders");
        }
        if (from.isEqual(to)) {
            throw new TimeIntervalException("Can not create 0 TimeInterval");
        }
        if (to.isBefore(from)) {
            throw new TimeIntervalException("from must be before to");
        }
        this.from = from;
        this.to = to;
    }

    public ZonedDateTime getFrom() {
        return from;
    }

    public ZonedDateTime getTo() {
        return to;
    }

    public static boolean doIntersect(TimeInterval firstInterval, TimeInterval secondInterval) {
        return !(firstInterval.to.isBefore(secondInterval.from)
                || firstInterval.to.isEqual(secondInterval.from)
                || secondInterval.to.isBefore(firstInterval.from)
                || secondInterval.to.isEqual(firstInterval.from));
    }

    public static TimeInterval getIntersection(TimeInterval firstInterval, TimeInterval secondInterval) {
        if (firstInterval.from.isBefore(secondInterval.from)
            || firstInterval.from.isEqual(secondInterval.from)) {
            if (firstInterval.to.isBefore(secondInterval.to)
                || firstInterval.to.isEqual(secondInterval.to)) {
                return new TimeInterval(secondInterval.from, firstInterval.to);
            } else {
                return secondInterval;
            }
        } else {
            if (firstInterval.to.isBefore(secondInterval.to)
                    || firstInterval.to.isEqual(secondInterval.to)) {
                return firstInterval;
            } else {
                return new TimeInterval(firstInterval.from, secondInterval.to);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeInterval that = (TimeInterval) o;
        return from.isEqual(that.from) && to.isEqual(that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}