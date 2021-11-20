package concurrentcube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import org.junit.jupiter.api.Test;

public class Tests {

    private static final int BASIC_NUM_ROTATION = 1000; 
    private static final int CONCURRENT_NUM_THREADS = 100;
    private static final int CONCURRENT_N_MOVES = 100;

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

    private static final String BASIC4 = 
        "000" +
        "341" +
        "250" +

        "141" +
        "211" +
        "235" +
        
        "542" +
        "502" +
        "352" +

        "323" +
        "334" +
        "334" +
        
        "444" +
        "150" +
        "110" +

        "425" +
        "025" +
        "105";
    public static void main(String[] args) {
        // testowanie używa asercji więc trzeba uruchamiać z : java -ea Tests.java 
        // na students mi zajmuje ok 10 sek
        // basic - testujemy czy pojedyńcze obroty są poprawne
        // testConcurrent1/2 - zapamiętujemy w jakiej kolejności są wywoływane współbieżnie oborty
        // i sprawdzamy czy wykonując je w takiej samej kolejności sekwencyjnie dają taki sam wynik
        try {
            Tests.basic();
            Tests.testConcurrent1();
            Tests.testConcurrent2();
        } catch (InterruptedException e) {
            assert(false);
        }
        
        System.out.println("==================== TESTS PASSED! ====================");
    }

    public static void basic() throws InterruptedException {

            var rotation_counter = new Object() { int value = 0; };
            var show_counter = new Object() { int value = 0; };
            Cube n3x3 = new Cube(3,                
                (x, y) -> { ++rotation_counter.value; },
                (x, y) -> { ++rotation_counter.value; },
                () -> { ++show_counter.value; },
                () -> { ++show_counter.value; } 
            );
            
            // just show
            assert(BASIC1.equals(n3x3.show()));
            assert(2 == show_counter.value);

            // up clockwise and return
            n3x3.rotate(0, 0);
            assert(BASIC2.equals(n3x3.show()));
            assert(2 == rotation_counter.value);
            assert(4 == show_counter.value);
            n3x3.rotate(5, 2);

            // third from back clockwise 
            n3x3.rotate(4, 2);
            assert(BASIC3.equals(n3x3.show()));
            assert(6 == rotation_counter.value);
            assert(6 == show_counter.value);



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

            assert(before.equals(after));

            var rotation_counter3 = new Object() { int value = 0; };
            var show_counter3 = new Object() { int value = 0; };
            Cube n3x3_2 = new Cube(3,                
                (x, y) -> { ++rotation_counter3.value; },
                (x, y) -> { ++rotation_counter3.value; },
                () -> { ++show_counter3.value; },
                () -> { ++show_counter3.value; } 
            );

            n3x3_2.rotate(5, 2);
            n3x3_2.rotate(3, 1);
            n3x3_2.rotate(0, 0);
            n3x3_2.rotate(2, 2);
            n3x3_2.rotate(3, 1);
            n3x3_2.rotate(3, 2);
            n3x3_2.rotate(4, 0);
            n3x3_2.rotate(5, 2);
            n3x3_2.rotate(3, 1);
            n3x3_2.rotate(0, 0);

            assert(BASIC4.equals(n3x3_2.show()));
            assert(show_counter3.value == 2);
    }

    private static class Mover1 implements Runnable {

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

    public static void testConcurrent1() throws InterruptedException {
        List<Integer> sides = Collections.synchronizedList(new ArrayList<>());
        List<Integer> layers = Collections.synchronizedList(new ArrayList<>());
        Semaphore s1 = new Semaphore(1);


        Cube n3x3 = new Cube(3,                
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

        for(int i = 0; i < CONCURRENT_NUM_THREADS; i ++)
            workers.add(new Thread(new Mover1(n3x3)));

        for(Thread t: workers)
            t.start();
    
        for(Thread t: workers)
            t.join();

        assert(CONCURRENT_NUM_THREADS * 6 == sides.size());

        Cube n3x3_ground_true = new Cube(3,                
            (x, y) -> { ; },
            (x, y) -> { ; },
            () -> { ; },
            () -> { ; } 
        );

        for(int i = 0; i < layers.size(); i++) {
            n3x3_ground_true.rotate(sides.get(i), layers.get(i));
        }

        assert(n3x3_ground_true.show().equals(n3x3.show()));
    }

    private static class RandomMover implements Runnable {

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


    
    public static void testConcurrent2() throws InterruptedException {

        int[] sizes = {1, 2, 3, 4, 5, 7, 16};

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

            for(int i = 0; i < CONCURRENT_NUM_THREADS; i ++)
                workers.add(new Thread(new RandomMover(cube, size, CONCURRENT_N_MOVES)));
                
                
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
            
            assert(cube_ground_true.show().equals(cube.show()));
        }
    }
}
