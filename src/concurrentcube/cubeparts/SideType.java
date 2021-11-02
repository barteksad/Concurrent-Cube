package concurrentcube.cubeparts;
// : 0 (góra), 1 (lewo), 2 (przód), 3 (prawo), 4 (tył), 5 (dół)
public enum SideType {
    UP,
    LEFT,
    FRONT,
    RIGHT,
    BACK,
    BOTTOM
}
/*
(0,2)

1115 
1115
1115
1115

2222
2222
2222
2222

0333
0333
0333
0333

4444
4444
4444
4444

3333
5555
5555
5555

*/