package concurrentcube;

import java.util.ArrayList;

public class CubeLayer {

    private int size;
    private ArrayList<ColorType> blocks;
    private ArrayList<Boolean> availibility;

    public CubeLayer(int size, ColorType color) {
        this.size = size;
        this.blocks = new ArrayList<ColorType>();
        this.availibility = new ArrayList<Boolean>();
        for(int i = 0; i < size*size; i ++) {
            blocks.add(color);
            availibility.add(true);
        }
    }

    public ArrayList<ColorType> getBlocks(int idx, boolean if_vertical) {
        ArrayList<ColorType> acc = new ArrayList<>();

        int pos = if_vertical ? idx : idx * size;
        int step = if_vertical ? size : 1;

        for(int i = 0; i < size; i ++) {
            acc.add(blocks.get(pos));
            pos += step;
        }

        return acc;
    }

    public void setBlocks(int idx, boolean if_vertical, ArrayList<ColorType> new_colors) {
        int pos = if_vertical ? idx : idx * size;
        int step = if_vertical ? size : 1;

        for(ColorType c: new_colors) {
            blocks.set(pos, c);
            pos += step;
        }
    }

    public boolean checkBlocksAvailibility(int idx, boolean if_vertical) {

        if (idx == -1) {
            for (boolean b : availibility)
                if(!b)
                    return false;
            return true;
        }
        else {

            int pos = if_vertical ? idx : idx * size;
            int step = if_vertical ? size : 1;
            
            for(int i = 0; i < size; i ++) {
                if(!availibility.get(pos))
                return false;
                pos += step;
            }
            
            return true;   
        }
    }

    public void lockBlocks(int idx, boolean if_vertical) {
        if (idx == -1) {
            for(int i = 0; i < size * size; i++)
                availibility.set(i, false);
        }
        else {
            int pos = if_vertical ? idx : idx * size;
            int step = if_vertical ? size : 1;
            
            for(int i = 0; i < size; i ++) {
                availibility.set(pos, false);
                pos += step;
            }
        }
    }

    public void unlockBlocks(int idx, boolean if_vertical) {
        if (idx == -1) {
            for(int i = 0; i < size * size; i++)
                availibility.set(i, true);
        }
        else {
            int pos = if_vertical ? idx : idx * size;
            int step = if_vertical ? size : 1;
            
            for(int i = 0; i < size; i ++) {
                availibility.set(pos, true);
                pos += step;
            }
        }
    }
    
}
