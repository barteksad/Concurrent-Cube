package concurrentcube.cubeparts;

public class BoolBlock {
    private Vector3i position;
    private boolean is_available;

    public BoolBlock(Vector3i position) {
        this.position = position;
        is_available = true;
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
        return is_available;
    }

    public void lock() {
        is_available = false;
    }

    public void unlock() {
        is_available = true;
    }
}
