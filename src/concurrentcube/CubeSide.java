package concurrentcube;

import java.util.ArrayList;

public class CubeSide {

    private int size;
    private ArrayList<Color> blocks;

    public CubeSide(int size, Color color) {
        this.size = size;
        this.blocks = new ArrayList<Color>();
        for(int i = 0; i < size*size; i ++)
            blocks.add(color);
    }

    public ArrayList<Color> getBlocks(int idx, boolean if_vertical) {
        ArrayList<Color> acc = new ArrayList<>();

        int pos = if_vertical ? idx : idx * size;
        int step = if_vertical ? size : 1;

        for(int i = 0; i < size; i ++) {
            acc.add(blocks.get(pos));
            pos += step;
        }

        return acc;
    }

    public void setBlocks(int idx, boolean if_vertical, ArrayList<Color> new_colors) {
        int pos = if_vertical ? idx : idx * size;
        int step = if_vertical ? size : 1;

        for(Color c: new_colors) {
            blocks.set(pos, c);
            pos += step;
        }
    }
    
}
