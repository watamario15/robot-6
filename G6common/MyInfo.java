package G6common;

import java.io.Serializable;

public class MyInfo implements Serializable {
    public double x, y;
    public int id; // sub1: 0, sub2: 1

    public MyInfo(double _x, double _y, int _id) {
        x = _x;
        y = _y;
        id = _id;
    }
}
