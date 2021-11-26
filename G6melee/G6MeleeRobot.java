package G6melee;

import robocode.*;
import java.util.Random;
import java.awt.Color;
import robocode.util.Utils;
import java.awt.geom.Rectangle2D;


import java.util.Random;//added to use random values

public class G6MeleeRobot extends AdvancedRobot {

    public void run() { // G6MeleeRobot's default behavior
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setTurnRadarRightRadians(100000); // Always search for enemies in all direction

        while(true) {
        	randomMovement();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot

    }

    public void onHitByBullet(HitByBulletEvent e) { // What to do when you're hit by a bullet

    }

    public void onHitWall(HitWallEvent e) { // What to do when you hit a wall

    }
    
    private void randomMovement() {
		Random rnd = new Random();
		double direction = 1;
		if(rnd.nextBoolean()){
			direction *= -1;
		}
		
		Rectangle2D fieldRect = new Rectangle2D.Double(80, 80, getBattleFieldWidth()-160, getBattleFieldHeight()-160); // make a square in the field. it is the safety area.
		setAhead(100000); // always go ahead
		setTurnRight(direction*(30+rnd.nextDouble()*120)); // 
		double startTime = getTime();
		while(/*getTime()-startTime<10.0*/getTurnRemaining()!=0){
			double goalDirection = getHeadingRadians();
			while (!fieldRect.contains(getX()+Math.sin(goalDirection)*150, getY()+
					Math.cos(goalDirection)*150))
			{
				goalDirection += direction*.1;
			}
			double turn =
				robocode.util.Utils.normalRelativeAngle(goalDirection-getHeadingRadians());
			if (Math.abs(turn) > Math.PI/2)
			{
				turn = robocode.util.Utils.normalRelativeAngle(turn + Math.PI);
				setBack(100);
			}
			else
				//setAhead(100);
			if(turn != 0)setTurnRightRadians(turn);
			execute();
		}

    }
}
