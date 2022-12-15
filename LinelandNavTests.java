package edu.yu.introtoalgs;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LinelandNavTests {
    @Test
    public void Exceptions(){
        assertThrows(IllegalArgumentException.class, () -> {
            LinelandNavigation nav = new LinelandNavigation(0, 3);});
        assertThrows(IllegalArgumentException.class, () -> {
            LinelandNavigation nav = new LinelandNavigation(8, 0);});
        assertThrows(IllegalArgumentException.class, () -> {
            LinelandNavigation nav = new LinelandNavigation(1000008, 3);});
        assertThrows(IllegalArgumentException.class, () -> {
            LinelandNavigation nav = new LinelandNavigation(200, 3000000);});
        LinelandNavigation nav = new LinelandNavigation(10, 400);
        assertThrows(IllegalArgumentException.class, () -> {
            nav.addMinedLineSegment(0,90);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            nav.addMinedLineSegment(8,1000000);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            nav.addMinedLineSegment(100,90);
        });
    }

    @Test
    public void noMines1() {
        LinelandNavigation nav = new LinelandNavigation(10, 1);
        assertEquals(10, nav.solveIt());
    }
    @Test
    public void LMtest4 () {
        LinelandNavigation ln = new LinelandNavigation(100, 10);
        ln.addMinedLineSegment(42, 50);
        ln.addMinedLineSegment(31, 31);
        int expected = 0;
        int actual = ln.solveIt();
        assertEquals(expected, actual);
    }
    @Test
    public void AutoTest100 () {
        final int moves = 10;
        final int goal = 100;
        int limit = goal + moves;
        LinelandNavigation ln = new LinelandNavigation(goal, moves);
        for (int i = moves, c = 0; i <= limit; i += moves, c++) {
            ln.addMinedLineSegment(i-c, i-c);
          //  System.out.println(i-c);
        }
        int expected = 0;
        int actual = ln.solveIt();
        assertEquals(expected, actual);
    }

    @Test
    public void noMines2() {
        LinelandNavigation nav = new LinelandNavigation(10, 3);
        assertEquals(4, nav.solveIt());
    }

    @Test
    public void noMines3() {
        LinelandNavigation nav = new LinelandNavigation(10, 5);
        assertEquals(2, nav.solveIt());
    }

    @Test
    public void bunchOMines(){
        LinelandNavigation nav2 = new LinelandNavigation(10, 3);
        nav2.addMinedLineSegment(5, 5);
        assertEquals(4, nav2.solveIt());

        // Test with a mined line segment that blocks the entire forward path
        LinelandNavigation nav3 = new LinelandNavigation(10, 5);
        nav3.addMinedLineSegment(1, 10);
        assertEquals(0, nav3.solveIt());

        LinelandNavigation nav4 = new LinelandNavigation(10, 4);
        nav4.addMinedLineSegment(5, 6);
        assertEquals(3, nav4.solveIt());

        LinelandNavigation nav5 = new LinelandNavigation(23, 6);
        nav4.addMinedLineSegment(4, 5);
        nav4.addMinedLineSegment(11, 11);
        nav4.addMinedLineSegment(13, 17);
        assertEquals(4, nav5.solveIt());

        LinelandNavigation nav8 = new LinelandNavigation(10, 2);
        nav8.addMinedLineSegment(5, 6);
        nav8.addMinedLineSegment(8, 8);
        assertEquals(0, nav8.solveIt());
        LinelandNavigation nav9 = new LinelandNavigation(10, 3);
        nav9.addMinedLineSegment(6, 6);
        nav9.addMinedLineSegment(8, 8);
        int ans=nav9.solveIt();
        assertEquals(5, ans);

        // Mine on the goal
        LinelandNavigation nav10 = new LinelandNavigation(10, 2);
        nav10.addMinedLineSegment(10, 10);
        assertEquals(7, nav10.solveIt());

        LinelandNavigation nav11 = new LinelandNavigation(17, 2);
        nav11.addMinedLineSegment(4, 4);
        nav11.addMinedLineSegment(9, 9);
        nav11.addMinedLineSegment(14, 14);
        assertEquals(13, nav11.solveIt());

        // Should move backwards to node 2 in order to make the jump avoiding the mined line segment
        LinelandNavigation nav6 = new LinelandNavigation(10, 3);
        nav6.addMinedLineSegment(6, 6);
        assertEquals(5, nav6.solveIt());

        LinelandNavigation nav12 = new LinelandNavigation(15, 4);
        nav12.addMinedLineSegment(8, 8);
        nav12.addMinedLineSegment(10, 10);
        assertEquals(5, nav12.solveIt());

        LinelandNavigation nav13 = new LinelandNavigation(16, 3);
        nav13.addMinedLineSegment(6, 7);
        nav13.addMinedLineSegment(9, 9);
        nav13.addMinedLineSegment(14, 14);
        assertEquals(8, nav13.solveIt());

        LinelandNavigation nav14 = new LinelandNavigation(30, 10);
        nav14.addMinedLineSegment(20, 20);
        assertEquals(5, nav14.solveIt());

        // Should move back to nodes 2 and 5 in order to make the jump avoiding the mined line segments
        LinelandNavigation nav7 = new LinelandNavigation(15, 4);
        nav7.addMinedLineSegment(7, 8);
        nav7.addMinedLineSegment(10, 10);
        assertEquals(6, nav7.solveIt());


    }

    @Test
    public void holyCrap(){
        LinelandNavigation nav = new LinelandNavigation(999_000, 3);
        for (int i = 1; i < 1_000_000; i *= 4) {
            nav.addMinedLineSegment(i , i + 1);
        }
        assertEquals(333_000, nav.solveIt());
    }
}
