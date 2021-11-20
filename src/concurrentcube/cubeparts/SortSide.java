package concurrentcube.cubeparts;

import java.util.Comparator;

public class SortSide implements Comparator<Block> {

    private SideType side_type;

    public SortSide(SideType side_type) {
        this.side_type = side_type;
    }

    @Override
    public int compare(Block o1, Block o2) {
        
        switch (side_type) {
            case UP:
                if (o1.x() > o2.x())
                    return -1;
                else if ((o1.x() == o2.x()) && o1.y() > o2.y())
                    return -1;
                else return 1;

            case LEFT:
                if (o1.z() < o2.z())
                    return -1;
                else if ((o1.z() == o2.z()) && o1.x() > o2.x())
                    return -1;
                else return 1;

            case RIGHT:
                if (o1.z() < o2.z())
                    return -1;
                else if ((o1.z() == o2.z()) && o1.x() < o2.x())
                    return -1;
                else return 1;

            case BACK:
                if(o1.z() < o2.z())
                    return -1;
                else if ((o1.z() == o2.z()) && o1.y() < o2.y())
                    return -1;
                else return 1;

            case FRONT:
                if (o1.z() < o2.z())
                    return -1;
                else if((o1.z() == o2.z()) && o1.y() > o2.y())
                    return -1;
                return 1;

            case BOTTOM:
                if (o1.x() < o2.x())
                    return -1;
                else if ((o1.x() == o2.x()) && o1.y() > o2.y())
                    return -1;
                else return 1;
            
            default:
                assert(false);
                return 0;
        }
    }
    
}
