package G6team;

import robocode.*;
import java.awt.Color;
import robocode.util.Utils;
import java.awt.geom.Rectangle2D;
import java.util.Random;//added to use random values

public class G6SubRobot1 extends TeamRobot {
    private double power = 2; // power of the gun

    public void run() { // G6SubRobot1's default behavior
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setTurnRadarRightRadians(100000); // Always search for enemies in all direction

        while(true) randomMovement();
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
        if (e.getDistance() > 300){ // If the enemy is too far away, go back to random movement
            randomMovement();
            return;
        }

        // Reference: http://robowiki.net/wiki/Robocode/Butthead
        // linear prediction gun
        double absBearing = getHeadingRadians() + e.getBearingRadians(); // Absolute bearing of the enemy
        double theta = Math.asin(e.getVelocity()*Math.sin(e.getHeadingRadians()-absBearing)/bulletVelocity(power)); // The extra angle the bullet would travel in
        double gunAngle = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + theta); // The angle the gun needs to turn to hit the enemy
        setTurnGunRightRadians(gunAngle);
        setFire(power);
        
        // Track enemy
        setTurnRightRadians(e.getBearingRadians());
        setAhead(e.getDistance());
        // setTurnRadarRightRadians(absBearing - getRadarHeadingRadians()); // infinite lock
        
        // Radar
		setTurnRadarLeftRadians(getRadarTurnRemaining());
    }

    public void onHitByBullet(HitByBulletEvent e) { // What to do when you're hit by a bullet

    }

    public void onHitWall(HitWallEvent e) { // What to do when you hit a wall

    }

    private void randomMovement(){
        Random rnd = new Random();
		double direction = 1;
		if(rnd.nextBoolean()){
			direction *= -1;
		}
		setMaxTurnRate(3);//change turn rate
		
		Rectangle2D fieldRect = new Rectangle2D.Double(80, 80, getBattleFieldWidth()-160, getBattleFieldHeight()-160); // make a square in the field. it is the safety area.
		setAhead(100000); // always go ahead
		setTurnRight(direction*(30+rnd.nextDouble()*120)); 
		while(getTurnRemaining()!=0){
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
			setMaxTurnRate(10);//reset turn rate in order to avoid walls
			if(turn != 0)setTurnRightRadians(turn);
			execute();
	    }
    }

    private double bulletVelocity(double power){
        return 20.0 - (3.0*power);
    }
}
