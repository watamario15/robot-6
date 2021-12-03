package G6team;

import robocode.*;
import java.awt.Color;
import robocode.util.Utils;
import java.util.Random;//added to use random values

public class G6SubRobot1 extends TeamRobot {
    private double power = 2; // power of the gun

    public void run() { // G6SubRobot1's default behavior
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setTurnRadarRightRadians(100000); // Always search for enemies in all direction

        randomMovement();
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
        double x, y, height = getBattleFieldHeight();
        double distance = height / 3;

        if (rnd.nextBoolean()) {
            turnRight(30 + rnd.nextDouble() * 120);
        } else {
            turnLeft(30 + rnd.nextDouble() * 120);
        }
        setAhead(distance);
        execute();
        while (getVelocity() > 0) {
            x = getX();
            y = getY();
            execute();
            if (x < 50 || x > height - 50 || y < 50 || y > height - 50) {
                back(distance - getDistanceRemaining());
            }
        }
    }

    private double bulletVelocity(double power){
        return 20.0 - (3.0*power);
    }
}
