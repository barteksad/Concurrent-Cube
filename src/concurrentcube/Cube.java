package concurrentcube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

import concurrentcube.cubeparts.Block;
import concurrentcube.cubeparts.RotationDirection;
import concurrentcube.cubeparts.SideType;
import concurrentcube.cubeparts.Vector3i;


public class Cube {

    private int size;
    private ArrayList<Block> blocks;
    private ArrayList<Integer> layer_numbers;

    private BiConsumer<Integer, Integer> beforeRotation;
    private BiConsumer<Integer, Integer> afterRotation;
    private Runnable beforeShowing;
    private Runnable afterShowing;

    private List<Task> taskQueue = Collections.synchronizedList(new LinkedList<Task>());
    private Semaphore waitUntilFinishedOrAdded = new Semaphore(0);
    private Thread taskStarterThread;

    public Cube(int size,
            BiConsumer<Integer, Integer> beforeRotation,
            BiConsumer<Integer, Integer> afterRotation,
            Runnable beforeShowing,
            Runnable afterShowing) {

        this.size = size;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
        this.beforeShowing = beforeShowing;
        this.afterShowing = afterShowing;

        this.layer_numbers = new ArrayList<Integer>();
        for(int i = - size / 2; i <= size / 2; i++) {
            if (i == 0 && size % 2 == 1)
                continue;
            layer_numbers.add(i);
        }
            
        this.blocks = new ArrayList<Block>();
        for(int z : layer_numbers) {
            for(int x : layer_numbers) {
                for(int y : layer_numbers) {
                    if(Math.abs(z) == size / 2) {
                        blocks.add(new Block(new Vector3i(x, y, z)));
                    }
                    else if (Math.abs(x) == size / 2 || Math.abs(y) == size / 2) {
                        blocks.add(new Block(new Vector3i(x, y, z)));
                    }
                }
            }
        }

        taskStarterThread = new Thread(new TaskStarter());
        taskStarterThread.start();
    }

    private boolean checkBlocksAvailibilityAndPossiblyLock(ArrayList<Block> blocksToPerformOn) {
        for(Block b : blocksToPerformOn)
            if(!b.is_available())
                return false;

        for(Block b : blocksToPerformOn)
            b.lock();
        
        return true;
    }
    private class TaskStarter implements Runnable {

        private List<Thread> runningTasks = new LinkedList<Thread>();

        @Override
        public void run() {
            while(true) {
                try {
                    waitUntilFinishedOrAdded.acquire();
                } catch (InterruptedException e) {
                    break;
                }

                for(Thread t : runningTasks)
                    if(!t.isAlive())
                        runningTasks.remove(t);

                for(Task t : taskQueue) {
                    if(checkBlocksAvailibilityAndPossiblyLock(t.blocksToPerformOn())) {
                        Thread taskThread = new Thread(t);
                        taskThread.start();
                        runningTasks.add(taskThread);
                    }
                    else
                        break;
                }
            }
        }
        
    }

    private abstract class Task implements Runnable {
        protected ArrayList<Block> blocksToPerformOn;

        public ArrayList<Block> blocksToPerformOn() {
            return blocksToPerformOn;
        }
    }

    private class RotationPerformer extends Task implements Runnable {

        private int side;
        private int layer;

        private RotationDirection direction;
        private SideType side_type;

        public RotationPerformer(int side, int layer) {
            super();

            this.side = side;
            this.layer = layer;

            int decoded_layer;
            
            side_type = SideType.values()[side];
            switch (side_type) {
                case UP, FRONT, RIGHT:
                    decoded_layer = layer_numbers.get(size - layer - 1);
                    direction = RotationDirection.CLOCKWISE;
                    break;
                case BOTTOM, BACK, LEFT:
                    decoded_layer = layer_numbers.get(layer);
                    direction = RotationDirection.ANTI_CLOCKWISE;
                default:
                    decoded_layer = 0;
                    assert(false);
            }

            ArrayList<Block> gathered = new ArrayList<Block>();
            for(Block b : blocks) {
                switch (side_type) {
                    case UP, BOTTOM:
                        if (b.z() != decoded_layer)
                            continue;
                    case LEFT, RIGHT:
                        if (b.y() != decoded_layer)
                            continue;
                    case FRONT, BACK:
                        if (b.x() != decoded_layer)
                            continue;
                }
                gathered.add(b);

                blocksToPerformOn = gathered;
            }
        }

        @Override
        public void run() {
            beforeRotation.accept(side, layer);

            for(Block b : blocksToPerformOn)
                switch (side_type) {
                    case UP, BOTTOM:
                        b.rotateZ(direction);
                    case LEFT, RIGHT:
                        b.rotateY(direction);
                    case FRONT, BACK:
                        b.rotateX(direction);
                }

            for(Block b : blocksToPerformOn)
                b.unlock();

            afterRotation.accept(side, layer);

            waitUntilFinishedOrAdded.release();
        }
        
    }

    public void rotate(int side, int layer) throws InterruptedException {
        ;
    }

    public String show() throws InterruptedException {
        return "";
    }

    

}