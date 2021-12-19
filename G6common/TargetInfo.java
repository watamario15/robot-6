package G6common;

import java.io.Serializable;

public class TargetInfo implements Serializable {
    public String targetName;
    public double x, y, targetX, targetY;

    public TargetInfo(String _targetName) {
        targetName = _targetName;
    }
}
