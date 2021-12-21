package concurrentcube.cubeparts;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BoolBlock {
    private Vector3i position;
    private AtomicBoolean is_available;
    private AtomicInteger is_being_shown_by;

    public BoolBlock(Vector3i position) {
        this.position = position;
        is_available = new AtomicBoolean(true);
        is_being_shown_by = new AtomicInteger(0);
    }

    public int x() {
        return position.x();
    }

    public int y() {
        return position.y();
    }

    public int z() {
        return position.z();
    }

    public boolean is_available() {
        return is_available.get();
    }

    public void register_being_shown() {
        is_being_shown_by.incrementAndGet();
    }

    public void register_show_end() {
        if (is_being_shown_by.decrementAndGet() == 0)
            is_available.set(true);
        
    }

    public void lock() {
        is_available.set(false);;
    }

    public void unlock() {
        is_available.set(true);
    }
}
