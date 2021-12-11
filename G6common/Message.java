package G6common;

import java.io.Serializable;

public class Message implements Serializable {
    public String targetName;
    public double x, y, targetX, targetY; 

    public Message(String _targetName) {
        targetName = _targetName;
    }
}
