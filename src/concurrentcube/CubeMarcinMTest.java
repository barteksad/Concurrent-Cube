package concurrentcube;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

// Krótki komentarz: TestInterruptsDense mnie uwala z prawdopodobieństwem 4%%, polecam ten test.
// Z tak ustawionymi liczbami powtórzeń lokalnie (windows) chodzi 30s

// In case of timeouts, it might be necessary to loosen the limits
// to ensure that there is a deadlock, not just a slow computation.
// Should run around 40 seconds total.
public class CubeMarcinMTest {
    private static int getRandomInt(int range) {
        return Math.abs((int)(Math.random() * range));
    }

    private static final AtomicInteger[] excl = new AtomicInteger[200000]; // number of threads present on layer[i]
    private static AtomicInteger currentAxis = new AtomicInteger(-1);
    private static final AtomicInteger inside = new AtomicInteger(0);
    private static int CubeSizeForBAccepts = 200;
    private static final int timeoutSmall = 2000, timeoutBig = 10000;

    private static final BiConsumer<Integer, Integer> beforeRotationCheckSafety = (integer, integer2) -> {
        int currAxis = currentAxis.get();
        int realAxis = integer < 3 ? integer : (integer == 4 ? 2 : integer == 5 ? 0 : 1);
        if(realAxis != integer)
            integer2 = CubeSizeForBAccepts - 1 - integer2;
        if(currAxis != realAxis && currAxis != -1) {
            System.err.println("BAD AXIS! expected: " + currAxis + ",received: " + realAxis);
            fail();
        }
        if(excl[integer2].get() > 0) {
            System.err.println("TRIED TO ROTATE SAME ROW! " + integer + " " + integer2);
            fail();
        }
        currentAxis = new AtomicInteger(realAxis);
        inside.incrementAndGet();
        excl[integer2].incrementAndGet();
    };
    private static final BiConsumer<Integer, Integer> afterRotationCheckSafety = (integer, integer2) -> {
        if(inside.decrementAndGet() == 0)
            currentAxis = new AtomicInteger(-1);
        excl[integer < 3 ? integer2 : CubeSizeForBAccepts - 1 - integer2].decrementAndGet();
    };
    private static final Runnable beforeShowCheckSafety = () -> {
        if(inside.get() > 0) {
            System.err.println("show while rotating!!");
            fail();
        }
    };
    private static final Runnable afterShowCheckSafety = () -> {
        if(inside.get() > 0) {
            System.err.println("show while rotating!!!");
            fail();
        }
    };

    private static void initExcl(int cubeSize) {
        CubeSizeForBAccepts = cubeSize;
        for(int i = 0; i < CubeSizeForBAccepts; ++i)
            excl[i] = new AtomicInteger(0);
    }

    private static void testSqaures(Cube cube) {
        try {
            String temp = cube.show();
            int[] count = new int[6];
            for(char s : temp.toCharArray())
                count[s - '0']++;
            for(int color = 0; color < 6; ++color)
                if(count[color] != CubeSizeForBAccepts * CubeSizeForBAccepts)
                    System.err.println("zgubiłeś jakieś ścianki! dla koloru" + color + " znaleziono: " + count[color]);
        } catch (InterruptedException e) {
            fail();
        }

    }

    // Small basic tests.
    @Test
    void rotateFacingTop() {
        initExcl(4);
        Cube cube = new Cube(4, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        try {
            cube.rotate(0, 0);
            assertEquals (cube.show(),
                    "0000000000000000" +
                            "2222111111111111" +
                            "3333222222222222" +
                            "4444333333333333" +
                            "1111444444444444" +
                            "5555555555555555");
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    void rotateFacingLeft() {
        initExcl(4);
        Cube cube = new Cube(4, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        try {
            cube.rotate(1, 0);
            assertEquals (cube.show(),
                    "4000400040004000" +
                            "1111111111111111" +
                            "0222022202220222" +
                            "3333333333333333" +
                            "4445444544454445" +
                            "2555255525552555");
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    void rotateFacingFront() {
        initExcl(4);
        Cube cube = new Cube(4, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        try {
            cube.rotate(2, 0);
            assertEquals (cube.show(),
                    "0000000000001111" +
                            "1115111511151115" +
                            "2222222222222222" +
                            "0333033303330333" +
                            "4444444444444444" +
                            "3333555555555555"
            );
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    void rotateFacingRight() {
        initExcl(4);
        Cube cube = new Cube(4, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        try {
            cube.rotate(3, 0);
            assertEquals (cube.show(),
                    "0002000200020002" +
                            "1111111111111111" +
                            "2225222522252225" +
                            "3333333333333333" +
                            "0444044404440444" +
                            "5554555455545554"
            );
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    void rotateFacingBack() {
        initExcl(4);
        Cube cube = new Cube(4, beforeRotationCheckSafety, afterRotationCheckSafety
                , beforeShowCheckSafety, afterShowCheckSafety);
        try {
            cube.rotate(4, 0);
            assertEquals (cube.show(),
                    "3333000000000000" +
                            "0111011101110111" +
                            "2222222222222222" +
                            "3335333533353335" +
                            "4444444444444444" +
                            "5555555555551111"
            );
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Test
    void rotateFacingBottom() {
        initExcl(4);
        Cube cube = new Cube(4, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        try {
            cube.rotate(5, 0);
            assertEquals (cube.show(),
                    "0000000000000000" +
                            "1111111111114444" +
                            "2222222222221111" +
                            "3333333333332222" +
                            "4444444444443333" +
                            "5555555555555555"
            );
        } catch (InterruptedException e) {
            fail();
        }
    }

    private static class Rotator implements Runnable {
        private final int side;
        private final int layer;
        private final Cube kostka;

        Rotator(int side, int layer, Cube kostka) {
            this.side = side;
            this.layer = layer;
            this.kostka = kostka;
        }

        public void run() {
            try {
                if(side == 6)
                    kostka.show();
                else
                    kostka.rotate(side, layer);
            } catch (InterruptedException e) {
                // Discard the exception for the sake of testing.
                //System.err.println("Rotator interrupted");
            }
        }
    }

    // Should run "very" quick
    @Test
    @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void basicMutualExclusionTest() {
        initExcl(2000);
        Cube cube = new Cube(CubeSizeForBAccepts, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);

        Thread[] thread = new Thread[CubeSizeForBAccepts];
        for(int i = 0; i < CubeSizeForBAccepts; ++i) {
            thread[i] = new Thread(new Rotator(0, i, cube));
            thread[i].start();
            // Uncomment to see the difference vs a sequential run.
            /*
            try {
                thread[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }

        for(int i = 0 ; i < CubeSizeForBAccepts; ++i) {
            try {
                thread[i].join();
            } catch (InterruptedException e) {
                System.err.println("interrupted while testing!");
                fail();
            }
        }

        testSqaures(cube);
    }

    // Small test checking if shows don't interfere with rotationg
    @Test
    @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void smallTest() {
        initExcl(3);
        Cube cube = new Cube(3, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        try {
            cube.rotate(1, 0);
            System.out.println(cube.show());
            cube.rotate(1, 1);
            System.out.println(cube.show());
            testSqaures(cube);
        } catch (InterruptedException e) {
            fail();
        }
    }

    // Many rotations of the external layer
    @Test
    @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void bigTestExtSide() {
        initExcl(200);
        int cubeSize = CubeSizeForBAccepts;
        int operct = 500;
        Cube cube = new Cube(cubeSize, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        Rotator[] rotators = new Rotator[operct];
        for(int i = 0; i < operct; ++i) {
            rotators[i] = new Rotator(i < operct / 2 ? 0 : 5, i < operct / 2 ? 0 : cubeSize - 1, cube);
            //xd[i] = new Rotator(getRandomInt(7), getRandomInt(cubeSize), cube);
        }

        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        for(int i = 0; i < operct; ++i)
            futures.add(CompletableFuture.runAsync(rotators[i]));

        for(int i = 0; i < operct; ++i) {
            futures.get(i).join();
            //    System.out.println(i);
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Big random test
    @Test
    @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void bigTestRandomSide() {
        initExcl(200);
        int cubeSize = CubeSizeForBAccepts;
        int operct = 5000;
        Cube cube = new Cube(cubeSize, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        Rotator[] rotators = new Rotator[operct];
        for(int i = 0; i < operct; ++i) {
            //rotators[i] = new Rotator(i < operct / 2 ? 0 : 5, i < operct / 2 ? 0 : cubeSize - 1, cube);
            rotators[i] = new Rotator(getRandomInt(7), getRandomInt(cubeSize), cube);
        }

        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        for(int i = 0; i < operct; ++i)
            futures.add(CompletableFuture.runAsync(rotators[i]));

        for(int i = 0; i < operct; ++i) {
            futures.get(i).join();
            //    System.out.println(i);
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Should run quick, only shows inside.
    @Test
    @Timeout(value = timeoutBig, unit = TimeUnit.MILLISECONDS)
    void bigTestShows() {
        initExcl(200);
        int cubeSize = CubeSizeForBAccepts;
        int operct = 5000;
        Cube cube = new Cube(cubeSize, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        Rotator[] rotators = new Rotator[operct];
        for(int i = 0; i < operct; ++i) {
            //rotators[i] = new Rotator(i < operct / 2 ? 0 : 5, i < operct / 2 ? 0 : cubeSize - 1, cube);
            rotators[i] = new Rotator(6, getRandomInt(cubeSize), cube);
        }

        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        for(int i = 0; i < operct; ++i) {
            futures.add(CompletableFuture.runAsync(rotators[i]));
        }

        for(int i = 0; i < operct; ++i) {
            futures.get(i).join();
            //    System.out.println(i);
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    // Test large number of shows on a large cube utilizing threads.
    // Should run slower if threads are joined immediately after start.
    @Test
    @Timeout(value = timeoutBig, unit = TimeUnit.MILLISECONDS)
    void bigTestShowsOnThreads() {
        initExcl(200);
        int cubeSize = CubeSizeForBAccepts;
        int operct = 1000;
        Cube cube = new Cube(cubeSize, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        Rotator[] rotators = new Rotator[operct];
        for(int i = 0; i < operct; ++i) {
            rotators[i] = new Rotator(6, getRandomInt(cubeSize), cube);
        }

        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i) {
            threads[i] = new Thread(rotators[i]);
            threads[i].start();
            /* try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }*/
            // Uncomment the above to see the difference vs a "sequential" run.
        }

        for(int i = 0; i < operct; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // A sequential test of rotations. The result should always be the same.
    @RepeatedTest(2)
    // @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void SequentialRandomTest() {
        int cubeSize = 21;
        initExcl(cubeSize);
        int operct = 500000;
        Cube cube = new Cube(cubeSize, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        long basic = 4169;
        long mod = 1000696969;
        long step = 2507;
        for(int i = 0; i < operct; ++i) {
            try {
                if(basic % 7 == 6)
                    cube.show();
                else
                    cube.rotate((int)basic % 7, (int)basic % cubeSize);
            } catch (InterruptedException e) {
                fail();
            }
            basic = (basic * basic + step) % mod;
        }

        try {
            testSqaures(cube);
            assertEquals(cube.show(), "002054002500400545300051030140402401554201001311131550045510101255205344402002300440000550310013101054022020455220521405410011000530000330400042200020444001025300004300221525524005002523000043205104321045350052440505004204005522042130431004100023055504110142154442100405050020031000353000505150000000000000000020200000050430202154110320331301115151200334310005225212022500045301025002035131003430510224040442451354100521033024005500300002040134533115450011254101410523444005044122504111101111110111111011111111111111111111011111101111113111111311111101111114111111011111141111111111111511101032114101211040221423033440520544100344111101111110111111011111141111110111111111111151111110111111011111111111110111111211111121111115111111011150431110204011001331222202222340222354402111111111113111111111111101111110111111411111101111115111111011111151111110111111011111111111110111111111205405224141022135452144410110020111015001242022225202222420222252422220202222025222232522223202222220222232022222242222020222222422225212222520222240250223254522452402104305110250011454231222422223252222325222212022222222222424222252522221202222025222242022221202222022222232122220222222020222241550220502422450402325102334220533120043242022222202222420222252522224242222224222252022221242222520222252022220222222420222242022223222222022222303042334000133003503232103222153322224342330333333133333303333332333333133333343333335333333033333303333332333333033333303333330333333133333303333332421330100133005513240431225041322002412330333333033333313333334333333433333343333333333333533333313333333333333233333303333330333333333333333333300202332023033205023404425442432344242044330333333533333303333332333333033333323333333333333333333353333332333333033333303333332333333333333313333440402440313044012024301302332201533142343444141444454544444444444045444444044442424444044444454444440444444243444404044444454444041444444444445434415302440040044002004314043332433433405403444045444414144440424444240444444544445434444044444454244444404444244444414344441404444544444414444440404424120441450244000304140420112114011402331444544444404144443404444545444454044440434444140444424044444404444145444454544442424444443444434044441454505355550555555450555515255555505555452555003203012321201132000545255555555555554555505255555535555055555505455550505555455555555455553525555354555505555552535555350555535255552525555354555235000000503500002000545055555555555252555545355553535555053555525255552505555054555505355554545555052555505055553545555353555545255551555555051555055213000020411331500545455555545555350555535555550505555255555535055554525555555555525055552505555055555");
        } catch (InterruptedException exc) {
            fail();
        }
    }

    // Liveness test with "sequential" rotations.
    // @RepeatedTest(20)
    // @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    @Test
    void testLivenessSimple() {
        Cube cube = new Cube(79, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(79);
        Thread[] threads = new Thread[1000];
        for(int i = 0; i < 1000; ++i)
            threads[i] = new Thread(new Rotator(0, 0, cube));
        for(int i = 0; i < 1000; ++i)
            threads[i].start();
        for(int i = 0; i < 1000; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        // testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Liveness test with one kind of rotations and shows.
    // @RepeatedTest(50)

    // @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    @Test
    void testLivenessWithShows() {
        Cube cube = new Cube(79, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(79);
        Thread[] threads = new Thread[500];
        for(int i = 0; i < 500; ++i)
            threads[i] = new Thread(new Rotator(i % 6 == 0 ? 6 : 0, 0, cube));
        for(int i = 0; i < 500; ++i)
            threads[i].start();
        for(int i = 0; i < 500; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }
        // testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Test of liveness with random rotations that collide fairly often.
    // @RepeatedTest(50)
    // @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    @Test
    void testLivenessRandom() {
        Cube cube = new Cube(79, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(79);
        Thread[] threads = new Thread[500];
        for(int i = 0; i < 500; ++i)
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(3), cube));
        for(int i = 0; i < 500; ++i)
            threads[i].start();
        for(int i = 0; i < 500; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Testing interrupts where each other thread is interrupted.
    // @RepeatedTest(20)
    // @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    @Test
    void testInterrupts2() {
        Cube cube = new Cube(79, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(79);
        int operct = 500;
        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i)
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(CubeSizeForBAccepts), cube));
        for(int i = 0; i < operct; ++i)
            threads[i].start();
        for(int i = 0; i < operct; ++i) {
            if(i % 2 == 0)
                threads[i].interrupt();
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Testing interrupts where each 4th thread is interrupted.
    // @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    @Test
    void testInterrupts4() {
        Cube cube = new Cube(79, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(79);
        int operct = 500;
        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i)
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(CubeSizeForBAccepts), cube));
        for(int i = 0; i < operct; ++i)
            threads[i].start();
        for(int i = 0; i < operct; ++i) {
            if(i % 4 == 0)
                threads[i].interrupt();
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /// Fun fact: ten test mnie uwala xD polecam puszczać z 1000 razy
    // Testing interrupts where each 10th thread is interrupted and operations collide often.
    // @RepeatedTest(100)
    // @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    @Test
    void testInterruptsDense() {
        Cube cube = new Cube(140, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(140);
        int operct = 1000;
        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i)
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(3), cube));
        for(int i = 0; i < operct; ++i)
            threads[i].start();
        for(int i = 0; i < operct; ++i) {
            if(i % 10 == 0)
                threads[i].interrupt();
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Moderately sized test where operations collide often.
    @RepeatedTest(25)
    @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void testDenseNoInterrupts() {
        Cube cube = new Cube(140, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(140);
        int operct = 1000;
        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i)
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(3), cube));
        for(int i = 0; i < operct; ++i)
            threads[i].start();
        for(int i = 0; i < operct; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Testing interrupts where each but the last thread is interrupted.
    @RepeatedTest(20)
    @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void testMostInterrupts() {
        Cube cube = new Cube(89, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(89);
        int operct = 500;
        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i)
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(CubeSizeForBAccepts), cube));
        for(int i = 0; i < operct; ++i)
            threads[i].start();
        for(int i = 0; i < operct; ++i) {
            if(i != operct - 1)
                threads[i].interrupt();
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Testing interrupts with a fairly large cube and operations that collide often.
    @RepeatedTest(10)
    @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void testInterruptsLargeCube() {
        Cube cube = new Cube(666, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(666);
        int operct = 25;
        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i)
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(2), cube));
        for(int i = 0; i < operct; ++i)
            threads[i].start();
        for(int i = 0; i < operct; ++i) {
            if(i % 4 == 0)
                threads[i].interrupt();
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Testing interrupts with a fairly large cube and operations that collide rarely.
    @RepeatedTest(10)
    @Timeout(value = timeoutSmall, unit = TimeUnit.MILLISECONDS)
    void testInterruptsLargeCubeSparse() {
        Cube cube = new Cube(666, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(666);
        int operct = 25;
        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i)
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(666), cube));
        for(int i = 0; i < operct; ++i)
            threads[i].start();
        for(int i = 0; i < operct; ++i) {
            if(i % 4 == 0)
                threads[i].interrupt();
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
        try {
            cube.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Validation test on a very small cube.
    @RepeatedTest(10)
    void testSmallCubeSequential() {
        Cube cube = new Cube(1, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(1);
        for(int i = 0; i < 100; ++i) {
            try {
                cube.rotate(getRandomInt(6), getRandomInt(1));
            } catch (InterruptedException e) {
                fail();
            }
        }
        testSqaures(cube);
    }

    // Safety / liveness test on a very small cube.
    @RepeatedTest(10)
    void testSmallCubeConcurrent() {
        Cube cube = new Cube(1, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(1);
        Thread[] threads = new Thread[1000];
        for(int i = 0; i < 1000; ++i) {
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(1), cube));
            threads[i].start();
        }
        for (int i = 0; i < 1000; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
    }

    // Interrupts test on a very small cube.
    @RepeatedTest(10)
    void testSmallCubeInterrupts() {
        Cube cube = new Cube(1, beforeRotationCheckSafety, afterRotationCheckSafety,
                beforeShowCheckSafety, afterShowCheckSafety);
        initExcl(1);
        int operct = 1000;
        Thread[] threads = new Thread[operct];
        for(int i = 0; i < operct; ++i) {
            threads[i] = new Thread(new Rotator(getRandomInt(7), getRandomInt(1), cube));
            if(i % 20 == 0)
                threads[i].interrupt();
            threads[i].start();
            if(i % 20 == 10)
                threads[i].interrupt();
        }

        for (int i = 0; i < operct; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                fail();
            }
        }

        testSqaures(cube);
    }


}

