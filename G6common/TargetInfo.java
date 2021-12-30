package G6common;

import java.io.Serializable;

public class TargetInfo implements Serializable {
    public String targetName;

    public TargetInfo(String _targetName) {
        targetName = _targetName;
    }
}
