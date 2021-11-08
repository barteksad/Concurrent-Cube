package concurrentcube;

import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

import concurrentcube.cubeparts.Block;
import concurrentcube.cubeparts.ColorType;
import concurrentcube.cubeparts.RotationDirection;
import concurrentcube.cubeparts.SideType;
import concurrentcube.cubeparts.SortSide;
import concurrentcube.cubeparts.Vector3i;


public class Cube {
    private static char[] colorMapping = {'0', '1', '2', '3', '4', '5'};

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
            if (i == 0 && size % 2 == 0)
                continue;
            layer_numbers.add(i);
        }
            
        this.blocks = new ArrayList<Block>();
        for(int z : layer_numbers) {
            for(int x : layer_numbers) {
                for(int y : layer_numbers) {
                    if (Math.abs(x) == size / 2 || Math.abs(y) == size / 2 || Math.abs(z) == size / 2) {
                        blocks.add(new Block(new Vector3i(x, y, z)));
                    }
                }
            }
        }

        taskStarterThread = new Thread(new TaskStarter());
        taskStarterThread.setDaemon(true);
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

        @Override
        public void run() {
            while(true) {
                try {
                    waitUntilFinishedOrAdded.acquire();
                } catch (InterruptedException e) {
                    break;
                }

                for(Task t : taskQueue) {
                    if(checkBlocksAvailibilityAndPossiblyLock(t.blocksToPerformOn())) {
                        t.release();
                        taskQueue.remove(t);
                    }
                    else
                        break;
                }
            }
        }
        
    }

    private class Task {
        private ArrayList<Block> blocksToPerformOn;
        private Semaphore semaphore;

        public Task(ArrayList<Block> blocksToPerformOn, Semaphore semaphore) {
            this.blocksToPerformOn = blocksToPerformOn;
            this.semaphore = semaphore;
        }

        public ArrayList<Block> blocksToPerformOn() {
            return blocksToPerformOn;
        }

        public void release() {
            semaphore.release();
        }
    }


    public void rotate(int side, int layer) throws InterruptedException {

            int decoded_layer;
            RotationDirection direction;
            SideType side_type = SideType.values()[side];

            switch (side_type) {
                case UP, FRONT, RIGHT:
                    decoded_layer = layer_numbers.get(size - layer - 1);
                    direction = RotationDirection.ANTI_CLOCKWISE;
                    break;
                case BOTTOM, BACK, LEFT:
                    decoded_layer = layer_numbers.get(layer);
                    direction = RotationDirection.CLOCKWISE;
                    break;
                default:
                    // to disable warnings
                    direction = RotationDirection.CLOCKWISE;
                    decoded_layer = 0;
                    assert(false);
            }

            ArrayList<Block> blocksToPerformOn = new ArrayList<Block>();
            for(Block b : blocks) {
                switch (side_type) {
                    case UP, BOTTOM:
                        if (b.z() != decoded_layer)
                            continue;
                        else
                            break;
                    case LEFT, RIGHT:
                        if (b.y() != decoded_layer)
                            continue;
                        else
                            break;
                    case FRONT, BACK:
                        if (b.x() != decoded_layer)
                            continue;
                        else
                            break;
                    default:
                        assert(false);
                }
                blocksToPerformOn.add(b);
            }

            Semaphore waitUntilBlocksAvailable = new Semaphore(0);
            Task task = new Task(blocksToPerformOn, waitUntilBlocksAvailable);
            taskQueue.add(task);

            waitUntilFinishedOrAdded.release();

            waitUntilBlocksAvailable.acquire();

            
            beforeRotation.accept(side, layer);

            for(Block b : blocksToPerformOn)
                switch (side_type) {

                    case UP, BOTTOM:
                        b.rotateZ(direction);
                        break;
        
                    case LEFT, RIGHT:
                        b.rotateY(direction);
                        break;

                    case FRONT, BACK:
                        b.rotateX(direction);
                        break;
                    
                    default:
                        assert(false);
                }

            afterRotation.accept(side, layer);

            for(Block b : blocksToPerformOn)
                b.unlock();


            waitUntilFinishedOrAdded.release();
    }

    public String show() throws InterruptedException {

        ArrayList<Block> blocksToPerformOn = blocks;
        Semaphore waitUntilBlocksAvailable = new Semaphore(0);
        Task task = new Task(blocksToPerformOn, waitUntilBlocksAvailable);
        taskQueue.add(task);

        waitUntilFinishedOrAdded.release();

        waitUntilBlocksAvailable.acquire();


        beforeShowing.run();

        StringBuilder acc = new StringBuilder();
        for(SideType side : SideType.values()) {

            ArrayList<Block> sideBlockToSort = new ArrayList<Block>();

            for(Block b : blocksToPerformOn) {
                switch (side) {
                    case UP:
                        if (b.z() == layer_numbers.get(size - 1))
                            sideBlockToSort.add(b);
                        break;
                    case LEFT:
                        if (b.y() == layer_numbers.get(0))
                            sideBlockToSort.add(b);
                        break;
                    case FRONT:
                        if (b.x() == layer_numbers.get(size - 1))
                            sideBlockToSort.add(b);
                        break;
                    case RIGHT:
                        if (b.y() == layer_numbers.get(size - 1))
                            sideBlockToSort.add(b);
                        break;
                    case BACK:
                        if(b.x() == layer_numbers.get(0))
                            sideBlockToSort.add(b);
                        break;
                    case BOTTOM:
                        if (b.z() == layer_numbers.get(0))
                            sideBlockToSort.add(b);
                        break;
                    default:
                        assert(false);
                }
            }

            Collections.sort(sideBlockToSort, new SortSide(side));
            Collections.reverse(sideBlockToSort);
            
            for(Block b : sideBlockToSort) {
                ColorType color = b.getFaceColor(side);
                acc.append(colorMapping[color.ordinal()]);
            }
        }

        afterShowing.run();

        for(Block b : blocksToPerformOn)
            b.unlock();
        
        waitUntilFinishedOrAdded.release();

        return acc.toString();
    }

    

}