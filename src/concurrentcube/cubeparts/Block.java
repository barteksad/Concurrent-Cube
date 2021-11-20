package concurrentcube.cubeparts;

public class Block {
    private Vector3i position;
    private Face[] faces = new Face[6];
    private boolean is_available;

    public Block(Vector3i position) {
        this.position = position;
        
        faces[0] = new Face(new Vector3i(0, 0, 1), ColorType.ZERO);
        faces[1] = new Face(new Vector3i(0, -1, 0), ColorType.ONE);
        faces[2] = new Face(new Vector3i(1, 0, 0), ColorType.TWO);
        faces[3] = new Face(new Vector3i(0, 1, 0), ColorType.THREE);
        faces[4] = new Face(new Vector3i(-1, 0, 0), ColorType.FOUR);
        faces[5] = new Face(new Vector3i(0, 0, -1), ColorType.FIVE);

        is_available = true;
    }

    public void rotateX(RotationDirection direction) {
        position.rotateX(direction);
        for(Face f : faces)
            f.rotateX(direction);
    }

    public void rotateY(RotationDirection direction) {
        position.rotateY(direction);
        for(Face f : faces)
            f.rotateY(direction);
    }

    public void rotateZ(RotationDirection direction) {
        position.rotateZ(direction);
        for(Face f : faces)
            f.rotateZ(direction);
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

    public ColorType getFaceColor(SideType side) {

        int _x = 0;
        int _y = 0;
        int _z = 0;

        switch(side) {
            case UP:
                _z = 1;
                break;
            case BOTTOM:
                _z = -1;
                break;
            case LEFT:
                _y = -1;
                break;
            case RIGHT:
                _y = 1;
                break;
            case FRONT:
                _x = 1;
                break;
            case BACK:
                _x = -1;
                break;
            default:
                assert(false);
        }

        for(Face f : faces) {
            if (f.x() == _x && f.y() == _y && f.z() == _z)
                return f.color();
        }

        assert(false);
        return ColorType.ZERO;
    }
}
