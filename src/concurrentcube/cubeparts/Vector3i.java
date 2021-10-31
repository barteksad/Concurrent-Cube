package concurrentcube.cubeparts;

public class Vector3i {
    private int x;
    private int y;
    private int z;

    public Vector3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public void rotateX(RotationDirection direction) {
        int new_x;
        int new_y;
        int new_z;

        if (direction == RotationDirection.CLOCKWISE) {
            new_x = x;
            new_y = - z;
            new_z = y;
        }
        else {
            new_x = x;
            new_y = z;
            new_z = - y;
        }

        x = new_x;
        y = new_y;
        z = new_z;
    }

    public void rotateY(RotationDirection direction) {
        int new_x;
        int new_y;
        int new_z;

        if (direction == RotationDirection.CLOCKWISE) {
            new_x = z;
            new_y = y;
            new_z = -x;
        }
        else {
            new_x = - z;
            new_y = y;
            new_z = x;
        }

        x = new_x;
        y = new_y;
        z = new_z;
    }

    public void rotateZ(RotationDirection direction) {

        int new_x;
        int new_y;
        int new_z;

        if (direction == RotationDirection.CLOCKWISE) {
            new_x = - y;
            new_y = x;
            new_z = z;
        }
        else {
            new_x = y;
            new_y = - x;
            new_z = z;
        }

        x = new_x;
        y = new_y;
        z = new_z;
    }
}
