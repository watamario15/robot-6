package G6common;

import robocode.*;
import java.io.Serializable;
import java.util.*;

public class Message implements Serializable {
    String targetName;
    double x,y,targetX,targetY; 

    public Message() {
        System.out.println("PBL yametai");
    }

    public Message(String targetName2) {
        targetName = targetName2;
    }

    public String getTargetName() {
        return targetName;
    }

}
