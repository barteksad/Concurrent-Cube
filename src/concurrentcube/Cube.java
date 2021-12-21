// autor : Bartłomiej Sadlej

package concurrentcube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;

import concurrentcube.cubeparts.Block;
import concurrentcube.cubeparts.BoolBlock;
import concurrentcube.cubeparts.ColorType;
import concurrentcube.cubeparts.RotationDirection;
import concurrentcube.cubeparts.SideType;
import concurrentcube.cubeparts.SortSide;
import concurrentcube.cubeparts.TaskType;
import concurrentcube.cubeparts.Vector3i;

public class Cube {
    private static final char[] colorMapping = { '0', '1', '2', '3', '4', '5' };

    private int size;
    private ArrayList<Block> blocks;
    private ArrayList<Integer> layer_numbers;
    private ArrayList<BoolBlock> blocks_availibility;

    private BiConsumer<Integer, Integer> beforeRotation;
    private BiConsumer<Integer, Integer> afterRotation;
    private Runnable beforeShowing;
    private Runnable afterShowing;

    private List<Task> taskQueue = Collections.synchronizedList(new LinkedList<Task>());
    private Semaphore waitUntilFinishedOrAdded = new Semaphore(0);
    private Thread taskStarterThread;

    public Cube(int size, BiConsumer<Integer, Integer> beforeRotation, BiConsumer<Integer, Integer> afterRotation,
            Runnable beforeShowing, Runnable afterShowing) {

        this.size = size;
        this.beforeRotation = beforeRotation;
        this.afterRotation = afterRotation;
        this.beforeShowing = beforeShowing;
        this.afterShowing = afterShowing;

        this.layer_numbers = new ArrayList<Integer>();
        for (int i = -size / 2; i <= size / 2; i++) {
            if (i == 0 && size % 2 == 0)
                continue;
            layer_numbers.add(i);
        }

        this.blocks = new ArrayList<Block>();
        this.blocks_availibility = new ArrayList<>();
        for (int z : layer_numbers) {
            for (int x : layer_numbers) {
                for (int y : layer_numbers) {
                    if (Math.abs(x) == size / 2 || Math.abs(y) == size / 2 || Math.abs(z) == size / 2) {
                        blocks.add(new Block(new Vector3i(x, y, z)));
                        blocks_availibility.add(new BoolBlock(new Vector3i(x, y, z)));
                    }
                }
            }
        }

        taskStarterThread = new Thread(new TaskStarter());
        taskStarterThread.setDaemon(true);
        taskStarterThread.start();
    }

    private boolean checkBlocksAvailibilityAndPossiblyLock(Task t, boolean check) {
        if (check) {
            for (BoolBlock b : t.blocksToPerformOnAvailibility())
                if (!b.is_available())
                    return false;
        }

        for (BoolBlock b : t.blocksToPerformOnAvailibility()) {
            b.lock();
            if (t.type() == TaskType.SHOW)
                b.register_being_shown();
        }

        return true;
    }

    private class TaskStarter implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    waitUntilFinishedOrAdded.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                boolean awake_show = false;

                synchronized (taskQueue) {
                    ListIterator<Task> it = taskQueue.listIterator();

                    while (it.hasNext()) {
                        Task t = it.next();
                        synchronized (t) {
                            if (t.isInterrupted()) {
                                it.remove();
                                continue;
                            } else {
                                boolean success = false;
                                if (awake_show && t.type == TaskType.SHOW)
                                    success = checkBlocksAvailibilityAndPossiblyLock(t, false);
                                else if (!awake_show)
                                    success = checkBlocksAvailibilityAndPossiblyLock(t, true);

                                if (success) {
                                    it.remove();
                                    t.setIsRemovedFromTaskQueueAndLocked();
                                    t.release();

                                    if (t.type == TaskType.SHOW)
                                        awake_show = true;
                                }
                            }

                        }
                    }
                }

            }

        }
    }

    private class Task {
        private TaskType type;
        private ArrayList<BoolBlock> blocksToPerformOnAvailibility;
        private Semaphore semaphore;
        private boolean isInterrupted;
        private boolean isRemovedFromTaskQueueAndLocked;

        public Task(TaskType type, Semaphore semaphore, ArrayList<BoolBlock> blocksToPerformOnAvailibility) {
            this.type = type;
            this.semaphore = semaphore;
            this.blocksToPerformOnAvailibility = blocksToPerformOnAvailibility;
            this.isInterrupted = false;
            this.isRemovedFromTaskQueueAndLocked = false;
        }

        public TaskType type() {
            return this.type;
        }

        public ArrayList<BoolBlock> blocksToPerformOnAvailibility() {
            return blocksToPerformOnAvailibility;
        }

        public void release() {
            semaphore.release();
        }

        public void interrupt() {
            isInterrupted = true;
        }

        public boolean isInterrupted() {
            return isInterrupted;
        }

        public boolean isRemovedFromTaskQueueAndLocked() {
            return isRemovedFromTaskQueueAndLocked;
        }

        public void setIsRemovedFromTaskQueueAndLocked() {
            isRemovedFromTaskQueueAndLocked = true;
        }
    }

    public void rotate(int side, int layer) throws InterruptedException {

        int decoded_layer;
        RotationDirection direction;
        SideType side_type = SideType.values()[side];

        switch (side_type) {
            case UP:
            case FRONT:
            case RIGHT:
                decoded_layer = layer_numbers.get(size - layer - 1);
                direction = RotationDirection.ANTI_CLOCKWISE;
                break;
            case BOTTOM:
            case BACK:
            case LEFT:
                decoded_layer = layer_numbers.get(layer);
                direction = RotationDirection.CLOCKWISE;
                break;
            default:
                // to disable warnings
                direction = RotationDirection.CLOCKWISE;
                decoded_layer = 0;
                assert (false);
        }

        ArrayList<BoolBlock> blocksToPerformOnAvailibility = new ArrayList<>();
        for (BoolBlock b : blocks_availibility)
            switch (side_type) {
                case UP:
                case BOTTOM:
                    if (b.z() != decoded_layer)
                        continue;
                    else
                        blocksToPerformOnAvailibility.add(b);
                    continue;
                case LEFT:
                case RIGHT:
                    if (b.y() != decoded_layer)
                        continue;
                    else
                        blocksToPerformOnAvailibility.add(b);
                    continue;
                case FRONT:
                case BACK:
                    if (b.x() != decoded_layer)
                        continue;
                    else
                        blocksToPerformOnAvailibility.add(b);
                    continue;
                default:
                    assert (false);
            }

        Semaphore waitUntilBlocksAvailable = new Semaphore(0);
        Task task = new Task(TaskType.ROTATE, waitUntilBlocksAvailable, blocksToPerformOnAvailibility);

        try {
            taskQueue.add(task);
            waitUntilFinishedOrAdded.release();
            waitUntilBlocksAvailable.acquire();
        } catch (Exception e) {
            synchronized (task) {
                task.interrupt();
                if (task.isRemovedFromTaskQueueAndLocked())
                    task.blocksToPerformOnAvailibility().forEach((b) -> b.unlock());
                waitUntilFinishedOrAdded.release();
                throw new InterruptedException();
            }
        }

        // synchronized (task) {
        //     if (task.isInterrupted()) {
        //         if (task.isRemovedFromTaskQueueAndLocked())
        //             task.blocksToPerformOnAvailibility().forEach((b) -> b.unlock());
        //         waitUntilFinishedOrAdded.release();
        //         throw new InterruptedException();
        //     }
        // }

        beforeRotation.accept(side, layer);

        ArrayList<Block> blocksToPerformOn = new ArrayList<Block>();
        for (Block b : blocks)
            switch (side_type) {
                case UP:
                case BOTTOM:
                    if (b.z() != decoded_layer)
                        continue;
                    else
                        blocksToPerformOn.add(b);
                    continue;
                case LEFT:
                case RIGHT:
                    if (b.y() != decoded_layer)
                        continue;
                    else
                        blocksToPerformOn.add(b);
                    continue;
                case FRONT:
                case BACK:
                    if (b.x() != decoded_layer)
                        continue;
                    else
                        blocksToPerformOn.add(b);
                    continue;
                default:
                    assert (false);
            }

        for (Block b : blocksToPerformOn) {
            switch (side_type) {

                case UP:
                case BOTTOM:
                    b.rotateZ(direction);
                    break;

                case LEFT:
                case RIGHT:
                    b.rotateY(direction);
                    break;

                case FRONT:
                case BACK:
                    b.rotateX(direction);
                    break;

                default:
                    assert (false);
            }
        }

        afterRotation.accept(side, layer);

        for (BoolBlock b : blocksToPerformOnAvailibility)
            b.unlock();

        waitUntilFinishedOrAdded.release();
    }

    public String show() throws InterruptedException {

        ArrayList<BoolBlock> blocksToPerformOnAvailibility = blocks_availibility;
        Semaphore waitUntilBlocksAvailable = new Semaphore(0);

        Task task = new Task(TaskType.SHOW, waitUntilBlocksAvailable, blocksToPerformOnAvailibility);

        try {
            taskQueue.add(task);
            waitUntilFinishedOrAdded.release();
            waitUntilBlocksAvailable.acquire();
        } catch (InterruptedException e) {
            synchronized (task) {
                task.interrupt();
            }
        }

        synchronized (task) {
            if (task.isInterrupted()) {
                if (task.isRemovedFromTaskQueueAndLocked())
                    task.blocksToPerformOnAvailibility().forEach((b) -> b.register_show_end());
                waitUntilFinishedOrAdded.release();
                throw new InterruptedException();
            }
        }

        beforeShowing.run();

        ArrayList<Block> blocksToPerformOn = blocks;

        StringBuilder acc = new StringBuilder();
        for (SideType side : SideType.values()) {

            ArrayList<Block> sideBlockToSort = new ArrayList<Block>();

            for (Block b : blocksToPerformOn) {
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
                        if (b.x() == layer_numbers.get(0))
                            sideBlockToSort.add(b);
                        break;
                    case BOTTOM:
                        if (b.z() == layer_numbers.get(0))
                            sideBlockToSort.add(b);
                        break;
                    default:
                        assert (false);
                }
            }

            Collections.sort(sideBlockToSort, new SortSide(side));
            Collections.reverse(sideBlockToSort);

            for (Block b : sideBlockToSort) {
                ColorType color = b.getFaceColor(side);
                acc.append(colorMapping[color.ordinal()]);
            }
        }
        
        afterShowing.run();

        for (BoolBlock b : blocksToPerformOnAvailibility) {
            b.register_show_end();
        }


        waitUntilFinishedOrAdded.release();

        return acc.toString();
    }

}