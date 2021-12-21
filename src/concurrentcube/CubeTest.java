package concurrentcube;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.Test;

public class CubeTest {

    private static final int BASIC_NUM_ROTATION = 1000;

    private static final int CONCURRENT_NUM_THREADS = 100;
    private static final int CONCURRENT_N_MOVES = 100;

    private static final int IF_CONCURRENT_IS_CONCURRENT_FULL_ROTATIONS = 1000;

    private static final int SHOW_CONCURRENT_RUN_N_TIMES = 10;
    private static final int SHOW_CONCURRENT = 100;

    private static final int INTERRUPT_NUM_THREADS = 80;
    private static final int INTERRUPT_N_MOVES = 100;
    private static final int N_INTERRUPT = INTERRUPT_NUM_THREADS * INTERRUPT_N_MOVES * 1000;

    private static final String BASIC1 = "000" +
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

    private static final String BASIC2 = "000" +
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

    private static final String BASIC3 = "000" +
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

    private static final String BASIC4 = "000" +
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
        // basic - testujemy czy pojedyńcze obroty są poprawne

        // testConcurrent1/2 - zapamiętujemy w jakiej kolejności są wywoływane
        // współbieżnie oborty
        // i sprawdzamy czy wykonując je w takiej samej kolejności jak zgłoszone przez
        // beforeRotation, sekwencyjnie dają taki sam wynik

        // testConcurrentRotationResult - test według opisu z moodla, wątki które nie
        // powinny się blokować czekają na barierze przed w afterRotation.
        // gdyby nie wykonywały się współbieżnie program by się nie wykonywał dalej

        // testShowConcurrent - sprawdzamy ile wątków jednocześnie jest dopuszczonych do
        // show

        // testInterrupt - odpalamy wątki tak jak w testConcurrent i z głównego wątku
        // robimy interrupt na losowych wątkach
        // sprawdamy, czy obroty zgłoszone przez beforeRotation zostały rzeczywiście
        // wywołane

        try {
            // CubeTest.basic();
            // System.out.println("Basic passed");
            // CubeTest.testConcurrentRotationResult1();
            // System.out.println("Concurrent1 passed");
            // CubeTest.testConcurrentRotationResult2();
            // System.out.println("Concurrent2 passed");
            // CubeTest.testIfRotateConcurrent();
            // System.out.println("IfRotateConcurrent passed");
            // CubeTest.testShowConcurrent();
            CubeTest.testInterrupt();
            System.out.println("Interrupt passed");
            System.out.println("==================== TESTS PASSED! ====================");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public static void basic() throws InterruptedException {

        var rotation_counter = new Object() {
            int value = 0;
        };
        var show_counter = new Object() {
            int value = 0;
        };
        Cube n3x3 = new Cube(3,
                (x, y) -> {
                    ++rotation_counter.value;
                },
                (x, y) -> {
                    ++rotation_counter.value;
                },
                () -> {
                    ++show_counter.value;
                },
                () -> {
                    ++show_counter.value;
                });

        assertEquals(BASIC1, n3x3.show());
        assertEquals(2, show_counter.value);

        n3x3.rotate(0, 0);
        assertEquals(BASIC2, n3x3.show());
        assertEquals(2, rotation_counter.value);
        assertEquals(4, show_counter.value);
        n3x3.rotate(5, 2);

        n3x3.rotate(4, 2);
        assertEquals(BASIC3, n3x3.show());
        assertEquals(6, rotation_counter.value);
        assertEquals(6, show_counter.value);

        var rotation_counter2 = new Object() {
            int value = 0;
        };
        var show_counter2 = new Object() {
            int value = 0;
        };

        Cube n15x15 = new Cube(15,
                (x, y) -> {
                    ++rotation_counter2.value;
                },
                (x, y) -> {
                    ++rotation_counter2.value;
                },
                () -> {
                    ++show_counter2.value;
                },
                () -> {
                    ++show_counter2.value;
                });

        String before = n15x15.show();

        for (int i = 0; i < BASIC_NUM_ROTATION; i++) {
            n15x15.rotate(4, 7);
        }
        for (int i = 0; i < BASIC_NUM_ROTATION; i++) {
            n15x15.rotate(2, 7);
        }

        for (int i = 0; i < BASIC_NUM_ROTATION; i++) {
            n15x15.rotate(0, 7);
        }
        for (int i = 0; i < BASIC_NUM_ROTATION; i++) {
            n15x15.rotate(5, 7);
        }

        for (int i = 0; i < BASIC_NUM_ROTATION; i++) {
            n15x15.rotate(1, 7);
        }
        for (int i = 0; i < BASIC_NUM_ROTATION; i++) {
            n15x15.rotate(3, 7);
        }

        String after = n15x15.show();

        assert (before.equals(after));

        var rotation_counter3 = new Object() {
            int value = 0;
        };
        var show_counter3 = new Object() {
            int value = 0;
        };
        Cube n3x3_2 = new Cube(3,
                (x, y) -> {
                    ++rotation_counter3.value;
                },
                (x, y) -> {
                    ++rotation_counter3.value;
                },
                () -> {
                    ++show_counter3.value;
                },
                () -> {
                    ++show_counter3.value;
                });

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

        assertEquals(BASIC4, n3x3_2.show());
        assertEquals(show_counter3.value, 2);
    }

    public static class Mover1 implements Runnable {

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
    public static void testConcurrentRotationResult1() throws InterruptedException {
        List<Integer> sides = Collections.synchronizedList(new ArrayList<>());
        List<Integer> layers = Collections.synchronizedList(new ArrayList<>());
        Semaphore s1 = new Semaphore(1);

        Cube n3x3 = new Cube(3,
                (x, y) -> {
                    try {
                        s1.acquire();
                    } catch (InterruptedException e) {
                    }
                    ;
                    sides.add(x);
                    layers.add(y);
                    s1.release();
                },
                (x, y) -> {
                    ;
                },
                () -> {
                    ;
                },
                () -> {
                    ;
                });

        List<Thread> workers = new ArrayList<Thread>();

        for (int i = 0; i < CONCURRENT_NUM_THREADS; i++)
            workers.add(new Thread(new Mover1(n3x3)));

        for (Thread t : workers)
            t.start();

        for (Thread t : workers)
            t.join();

        assertEquals(CONCURRENT_NUM_THREADS * 6, sides.size());

        Cube n3x3_ground_true = new Cube(3,
                (x, y) -> {
                    ;
                },
                (x, y) -> {
                    ;
                },
                () -> {
                    ;
                },
                () -> {
                    ;
                });

        for (int i = 0; i < layers.size(); i++) {
            n3x3_ground_true.rotate(sides.get(i), layers.get(i));
        }

        assertEquals(n3x3_ground_true.show(), n3x3.show());
    }

    public static class RandomMover implements Runnable {

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
            for (int i = 0; i < n_moves; i++) {
                int side = ThreadLocalRandom.current().nextInt(0, 6);
                int layer = ThreadLocalRandom.current().nextInt(0, size);

                try {
                    cube.rotate(side, layer);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static class Shower implements Runnable {

        private Cube cube;

        public Shower(Cube cube) {
            this.cube = cube;

        }

        @Override
        public void run() {
            try {
                cube.show();
            } catch (InterruptedException e) {
            }
        }
    }

    @Test
    public static void testConcurrentRotationResult2() throws InterruptedException {

        int[] sizes = { 1, 2, 3, 4, 5, 7, 16 };

        for (int size : sizes) {

            List<Integer> sides = Collections.synchronizedList(new ArrayList<>());
            List<Integer> layers = Collections.synchronizedList(new ArrayList<>());
            Semaphore s1 = new Semaphore(1);

            Cube cube = new Cube(size,
                    (x, y) -> {
                        try {
                            s1.acquire();
                        } catch (InterruptedException e) {
                        }
                        ;
                        sides.add(x);
                        layers.add(y);
                        s1.release();
                    },
                    (x, y) -> {
                        ;
                    },
                    () -> {
                        ;
                    },
                    () -> {
                        ;
                    });

            List<Thread> workers = new ArrayList<Thread>();

            for (int i = 0; i < CONCURRENT_NUM_THREADS; i++)
                workers.add(new Thread(new RandomMover(cube, size, CONCURRENT_N_MOVES)));

            for (int i = 0; i < CONCURRENT_NUM_THREADS / 10; i++)
                workers.add(new Thread(new Shower(cube)));

            for (Thread t : workers)
                t.start();

            for (Thread t : workers)
                t.join();

            Cube cube_ground_true = new Cube(size,
                    (x, y) -> {
                        ;
                    },
                    (x, y) -> {
                        ;
                    },
                    () -> {
                        ;
                    },
                    () -> {
                        ;
                    });

            for (int i = 0; i < layers.size(); i++) {
                cube_ground_true.rotate(sides.get(i), layers.get(i));
            }

            assertEquals(cube_ground_true.show(), cube.show());
        }
    }

    public static class FixedMover implements Runnable {

        private Cube cube;
        private int side;
        private int layer;

        public FixedMover(Cube cube, int side, int layer) {
            this.cube = cube;
            this.side = side;
            this.layer = layer;
        }

        @Override
        public void run() {

            for (int i = 0; i < 4 * IF_CONCURRENT_IS_CONCURRENT_FULL_ROTATIONS; i++) {
                try {
                    cube.rotate(side, layer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public static void testIfRotateConcurrent() throws InterruptedException {

        int size = 10;

        CyclicBarrier b = new CyclicBarrier(size);

        Cube cube = new Cube(size,
                (x, y) -> {
                    ;
                },
                (x, y) -> {
                    try {
                        b.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        e.printStackTrace();
                    }
                },
                () -> {
                    ;
                },
                () -> {
                    ;
                });

        String before = cube.show();

        List<Thread> workers = new ArrayList<Thread>();

        for (int i = 0; i < size; i++)
            workers.add(new Thread(new FixedMover(cube, 0, i)));

        for (Thread t : workers)
            t.start();

        for (Thread t : workers)
            t.join();

        assertEquals(before, cube.show());
    }

    @Test
    public static void testShowConcurrent() throws InterruptedException {

        int size = 5;
        for (int n = 0; n < SHOW_CONCURRENT_RUN_N_TIMES; n++) {
            System.out.print((n + 1) + "/" + SHOW_CONCURRENT_RUN_N_TIMES + " ");

            AtomicInteger current_showing = new AtomicInteger(0);
            List<Integer> history = Collections.synchronizedList(new LinkedList<Integer>());
            Cube cube = new Cube(size,
                    (x, y) -> {
                        ;
                    },
                    (x, y) -> {
                        ;
                    },
                    () -> {
                        history.add(current_showing.incrementAndGet());
                    },
                    () -> {
                        current_showing.decrementAndGet();
                    });

            String before = cube.show();

            List<Thread> workers = new ArrayList<Thread>();

            for (int i = 0; i < SHOW_CONCURRENT; i++)
                workers.add(new Thread(new Shower(cube)));

            for (Thread t : workers)
                t.start();

            for (Thread t : workers)
                t.join();

            System.out.println(" max concurrently showing : " + Collections.max(history));

            assertEquals(before, cube.show());
        }
    }

    @Test
    public static void testInterrupt() throws InterruptedException {

        int[] sizes = { 1, 2, 3, 4, 5, 7 };

        for (int size : sizes) {

            List<Integer> sides = Collections.synchronizedList(new ArrayList<>());
            List<Integer> layers = Collections.synchronizedList(new ArrayList<>());
            Semaphore s1 = new Semaphore(1);

            Cube cube = new Cube(size,
                    (x, y) -> {
                        try {
                            s1.acquire();
                        } catch (InterruptedException e) {
                        }
                        ;
                        sides.add(x);
                        layers.add(y);
                        s1.release();
                    },
                    (x, y) -> {
                    },
                    () -> {
                    },
                    () -> {
                    });

            List<Thread> workers = new ArrayList<Thread>();

            for (int i = 0; i < INTERRUPT_NUM_THREADS; i++)
                workers.add(new Thread(new RandomMover(cube, size, INTERRUPT_N_MOVES)));

            for (int i = 0; i < INTERRUPT_NUM_THREADS / 10; i++)
                workers.add(new Thread(new Shower(cube)));

            for (Thread t : workers)
                t.start();

            Random r = new Random();
            for (int i = 0; i < N_INTERRUPT; i++) {
                Thread t = workers.get(r.nextInt(workers.size()));
                t.interrupt();
            }

            for (Thread t : workers)
                t.join();

            Cube cube_ground_true = new Cube(size,
                    (x, y) -> {
                        ;
                    },
                    (x, y) -> {
                        ;
                    },
                    () -> {
                        ;
                    },
                    () -> {
                        ;
                    });

            for (int i = 0; i < layers.size(); i++) {
                cube_ground_true.rotate(sides.get(i), layers.get(i));
            }

            assertEquals(cube_ground_true.show(), cube.show());
        }
    }
}
