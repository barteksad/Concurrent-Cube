package tests;

import concurrentcube.Cube;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    public static void main(String[] args) {
        assertDoesNotThrow(() -> Tests.basic());


        System.out.println("==================== TESTS PASSED! ====================");
    }

    @Test
    private static void basic() throws InterruptedException {

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
    
}
