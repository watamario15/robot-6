package G6team;
import robocode.*;
import java.awt.Color;

public class G6SubRobot1 extends TeamRobot
{
	public void run() {	// G6SubRobot1's default behavior
		setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
		setTurnRadarRightRadians(100000); // Always search for enemies in all direction
	}

	public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
		
	}

	public void onHitByBullet(HitByBulletEvent e) { //  What to do when you're hit by a bullet
		
	}
	
	public void onHitWall(HitWallEvent e) { // What to do when you hit a wall
		
	}	
}
