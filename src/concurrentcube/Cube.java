package concurrentcube;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;


public class Cube {

    private int size;
    private ArrayList<CubeLayer> sides;

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

        this.sides = new ArrayList<CubeLayer>();
        for(ColorType color : ColorType.values())
                sides.add(new CubeLayer(size, color));
    }

    private boolean checkBlocksAvailibilityAndPossiblyLock(int side, int layer) {
        ArrayList<CubeLayer> toLock = new ArrayList<CubeLayer>();
        ArrayList<Integer> idxs = new ArrayList<Integer>();
        ArrayList<Boolean> if_verticals = new ArrayList<Boolean>();

        switch (SideType.values()[side - 1]) {
            case UP:
                
        }
    }
    private class TaskStarter implements Runnable {

        private List<Thread> runningTasks = new LinkedList<Thread>();

        @Override
        public void run() {
            while(true) {
                try {
                    waitUntilFinishedOrAdded.acquire();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                for(Thread t : runningTasks)
                    if(!t.isAlive())
                        runningTasks.remove(t);

                for(Task t : taskQueue) {
                    if(checkBlocksAvailibilityAndPossiblyLock(t.getSide(), t.getLayer())) {
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
        private int side;
        private int layer;

        public Task(int side, int layer) {
            this.side = side;
            this.layer = layer;
        }

        public int getSide() {
            return side;
        }

        public int getLayer() {
            return layer;
        }
    }

    private class RotationPerformer extends Task implements Runnable {

        public RotationPerformer(int side, int layer) {
            super(side, layer);
        }

        @Override
        public void run() {

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