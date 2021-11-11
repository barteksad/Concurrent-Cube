package tests;

import concurrentcube.Cube;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

public class Tests {

    private static final int BASIC_NUM_ROTATION = 10; // should work with any positive integer, bigger -> longer run

    private static final String BASIC1 =
        "000" +
        "000" +
        "000" +

        "111" +
        "111" +
        "111" +

        "222" +
        "222" +
        "222" +

        "333" +
        "333" +
        "333" +
    
        "444" +
        "444" +
        "444" +

        "555" +
        "555" +
        "555";

    private static final String BASIC2 =
        "000" +
        "000" +
        "000" +

        "222" +
        "111" +
        "111" +

        "333" +
        "222" +
        "222" +

        "444" +
        "333" +
        "333" +
    
        "111" +
        "444" +
        "444" +

        "555" +
        "555" +
        "555";

 // 0 (góra), 1 (lewo), 2 (przód), 3 (prawo), 4 (tył), 5 (dół).

    private static final String BASIC3 =
        "000" +
        "000" +
        "333" +

        "110" +
        "110" +
        "110" +

        "222" +
        "222" +
        "222" +

        "533" +
        "533" +
        "533" +
    
        "444" +
        "444" +
        "444" +

        "111" +
        "555" +
        "555";

    // public static void main(String[] args) {
    //     // assertDoesNotThrow(() -> Tests.basic());
    //     // assertDoesNotThrow(() -> Tests.testConcurrent1());
    //     assertDoesNotThrow(() -> Tests.testConcurrent2());


    //     System.out.println("==================== TESTS PASSED! ====================");
    // }

    @Test
    public void basic() throws InterruptedException {

            var rotation_counter = new Object() { int value = 0; };
            var show_counter = new Object() { int value = 0; };
            Cube n3x3 = new Cube(3,                
                (x, y) -> { ++rotation_counter.value; },
                (x, y) -> { ++rotation_counter.value; },
                () -> { ++show_counter.value; },
                () -> { ++show_counter.value; } 
            );
            
            // just show
            assertEquals(BASIC1, n3x3.show());
            assertEquals(2, show_counter.value);

            // up clockwise and return
            n3x3.rotate(0, 0);
            assertEquals(BASIC2, n3x3.show());
            assertEquals(2, rotation_counter.value);
            assertEquals(4, show_counter.value);
            n3x3.rotate(5, 2);

            // third from back clockwise 
            n3x3.rotate(4, 2);
            assertEquals(BASIC3, n3x3.show());
            assertEquals(6, rotation_counter.value);
            assertEquals(6, show_counter.value);



            var rotation_counter2 = new Object() { int value = 0; };
            var show_counter2 = new Object() { int value = 0; };

            Cube n15x15 = new Cube(15,                
                (x, y) -> { ++rotation_counter2.value; },
                (x, y) -> { ++rotation_counter2.value; },
                () -> { ++show_counter2.value; },
                () -> { ++show_counter2.value; }
            );

            // opposite moves
            String before = n15x15.show();

            for(int i = 0; i < BASIC_NUM_ROTATION; i++) {
                n15x15.rotate(4, 7);
            }
            for(int i = 0; i < BASIC_NUM_ROTATION; i++) {
                n15x15.rotate(2, 7);
            }

            for(int i = 0; i < BASIC_NUM_ROTATION; i++) {
                n15x15.rotate(0, 7);
            }
            for(int i = 0; i < BASIC_NUM_ROTATION; i++) {
                n15x15.rotate(5, 7);
            }

            
            for(int i = 0; i < BASIC_NUM_ROTATION; i++) {
                n15x15.rotate(1, 7);
            }
            for(int i = 0; i < BASIC_NUM_ROTATION; i++) {
                n15x15.rotate(3, 7);
            }

            String after = n15x15.show();

            assertEquals(before, after);
    }

    private class Mover1 implements Runnable {

        private Cube cube;

        public Mover1(Cube cube) {
            this.cube = cube;
        }

        @Override
        public void run() {
            try {
                cube.rotate(0, 0);
                Thread.sleep(100);
                cube.rotate(1, 0);
                Thread.sleep(100);
                cube.rotate(2, 0);
                Thread.sleep(100);
                cube.rotate(3, 0);
                Thread.sleep(100);
                cube.rotate(4, 0);
                Thread.sleep(100);
                cube.rotate(5, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testConcurrent1() throws InterruptedException {
        List<Integer> sides = Collections.synchronizedList(new ArrayList<>());
        List<Integer> layers = Collections.synchronizedList(new ArrayList<>());

        Cube n3x3 = new Cube(3,                
            (x, y) -> { sides.add(x); layers.add(y); },
            (x, y) -> { ; },
            () -> { ; },
            () -> { ; } 
        );
        
        List<Thread> workers = new ArrayList<Thread>();

        int how_many_times = 100;
        for(int i = 0; i < how_many_times; i ++)
            workers.add(new Thread(new Mover1(n3x3)));

        for(Thread t: workers)
            t.start();
    
        for(Thread t: workers)
            t.join();

        assertEquals(how_many_times * 6, sides.size());

        Cube n3x3_ground_true = new Cube(3,                
            (x, y) -> { ; },
            (x, y) -> { ; },
            () -> { ; },
            () -> { ; } 
        );

        for(int i = 0; i < layers.size(); i++) {
            n3x3_ground_true.rotate(sides.get(i), layers.get(i));
        }

        assertEquals(n3x3_ground_true.show(), n3x3.show());
    }

    private class RandomMover implements Runnable {

        private Cube cube;
        private int size;
        private int n_moves;

        public RandomMover(Cube cube, int size, int n_moves) {
            this.cube = cube;
            this.size = size;
            this.n_moves = n_moves;
        }

        @Override
        public void run() {
            for(int i = 0; i < n_moves; i++) {
                int side = ThreadLocalRandom.current().nextInt(0, 6);
                int layer = ThreadLocalRandom.current().nextInt(0, size);

                try {
                    cube.rotate(side, layer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Test
    public void testConcurrent2() throws InterruptedException {

        int[] sizes = {1, 3, 7, 16};

        for(int size : sizes) {

            List<Integer> sides = Collections.synchronizedList(new ArrayList<>());
            List<Integer> layers = Collections.synchronizedList(new ArrayList<>());
            Semaphore s1 = new Semaphore(1);
    
            Cube cube = new Cube(size,                
                (x, y) -> {
                    try {
                        s1.acquire();
                    } catch (InterruptedException e) {};
                    sides.add(x); layers.add(y);
                    s1.release();
                },
                (x, y) -> { ; },
                () -> { ; },
                () -> { ; } 
            );

            List<Thread> workers = new ArrayList<Thread>();

            int how_many_workers = 15;
            for(int i = 0; i < how_many_workers; i ++)
                workers.add(new Thread(new RandomMover(cube, size, 100)));
                
                
            for(Thread t: workers)
                t.start();
            
            for(Thread t: workers)
                t.join();
                            
            Cube cube_ground_true = new Cube(size,                
                (x, y) -> { ; },
                (x, y) -> { ; },
                () -> { ; },
                () -> { ; } 
            );
            
            for(int i = 0; i < layers.size(); i++) {
                cube_ground_true.rotate(sides.get(i), layers.get(i));
            }
            
            assertEquals(cube_ground_true.show(), cube.show());
        }
    }
}
