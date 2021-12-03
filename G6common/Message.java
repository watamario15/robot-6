package G6common;

import robocode.*;
import java.io.Serializable;

public class Message implements Serializable {
    int x, y, targetX, targetY;
    String targetName;
}