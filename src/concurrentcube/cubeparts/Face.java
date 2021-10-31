package concurrentcube.cubeparts;

public class Face {
    private Vector3i normal;
    private ColorType color;

    public Face(Vector3i normal, ColorType color) {
        this.normal = normal;
        this.color = color; 
    }

    public void rotateX(RotationDirection direction) {
        normal.rotateX(direction);
    }

    public void rotateY(RotationDirection direction) {
        normal.rotateY(direction);
    }

    public void rotateZ(RotationDirection direction) {
        normal.rotateZ(direction);
    }

    public int x() {
        return normal.x();
    }

    public int y() {
        return normal.y();
    }

    public int z() {
        return normal.z();
    }

    public ColorType color() {
        return color;
    }
}
