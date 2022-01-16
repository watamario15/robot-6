package G6melee;

import robocode.*;
import java.util.Random; // added to use random values
import java.awt.Color;
import robocode.util.Utils;
import java.awt.geom.Rectangle2D;

public class G6MeleeRobot extends AdvancedRobot {
    private Random rnd = new Random();
    private double power = 0.1; // power of the gun
    private Rectangle2D centralRect = new Rectangle2D.Double(400, 400, 1200, 1200); //make a central square
    private boolean battleModeFlag = false;

    public void run() { // G6MeleeRobot's default behavior
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
		
		setTurnRadarRightRadians(100000); // Always search for enemies in all direction

        while(true) randomMovement();
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
        if (e.getDistance() > 500){ // If the enemy is too faraway, go back to the random movement
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
		if(battleModeFlag || e.getDistance() < 100){
        	setTurnRightRadians(e.getBearingRadians());
        	setAhead(e.getDistance());
		}
        
        // Radar
        setTurnRadarLeftRadians(getRadarTurnRemaining());
    }

    public void onHitByBullet(HitByBulletEvent e) { // What to do when you're hit by a bullet

    }

    public void onHitWall(HitWallEvent e) { // What to do when you hit a wall

    }
    
    public void onRobotDeath(RobotDeathEvent e) { // What to do when another robot dies
    	if(getOthers() <= 4) {
    		battleModeFlag = true;
    		power = 3;
    	}
    }

    private void randomMovement() {
        double direction = 1;
        if(rnd.nextBoolean()) direction *= -1;
        setMaxTurnRate(3); // change the turn rate
        
        Rectangle2D fieldRect = new Rectangle2D.Double(80, 80, getBattleFieldWidth()-160, getBattleFieldHeight()-160); // make a safety square in the field
        
        setAhead(100000); // always go ahead
        setTurnRight(direction*(30+rnd.nextDouble()*120)); 
				
		while(getTurnRemaining()!=0){
			double goalDirection = getHeadingRadians();
			while(!fieldRect.contains(getX()+Math.sin(goalDirection)*150, getY()+Math.cos(goalDirection)*150)) {
				goalDirection += direction*.1;
			}
			double turn = Utils.normalRelativeAngle(goalDirection-getHeadingRadians());
			if(Math.abs(turn) > Math.PI/2) {
				turn = Utils.normalRelativeAngle(turn + Math.PI);
				setBack(100);
			}
			setMaxTurnRate(10); // reset the turn rate in order to avoid hitting walls
			if(turn!=0) setTurnRightRadians(turn);
			execute();
			while(!battleModeFlag && centralRect.contains(getX(), getY())){
				setTurnRightRadians(Utils.normalRelativeAngle(Math.atan2(getX()-1000, getY()-1000)-getHeadingRadians()));
				setAhead(1000);
				execute();
			}
		}
    }

    private double bulletVelocity(double power){
        return 20.0 - (3.0*power);
    }
}
