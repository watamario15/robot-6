package G6team;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Random; // added to use random values
import java.io.IOException;
import G6common.*;

public class G6LeaderRobot extends TeamRobot {
    private double power = 1.5; // power of the gun
    private double direction = 1; // The direction of the random movement
    private String targetName; // Target robot's name
    private Rectangle2D fieldRect; // safe square in the field
    private Random rnd = new Random();

    public void run() { // G6LeaderRobot's default behavior
        fieldRect = new Rectangle2D.Double(80, 80, getBattleFieldWidth() - 160, getBattleFieldHeight() - 160);
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setAdjustGunForRobotTurn(true); // Set the gun to turn independent from the robot's turn
        setAdjustRadarForGunTurn(true); // Set the radar to turn independent from the gun's turn

        while(true) randomMovement(); // Default behavior: Random movement
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
        if(isTeammate(e.getName())) return; // If the robot is teammate, go back to the default behavior

        // Adjust the bullet energy
        if(e.getDistance() > 100) power = 1.5;
        else power = 3;

        // Reference: http://robowiki.net/wiki/Robocode/Butthead
        // linear prediction gun
        double absBearing = getHeadingRadians() + e.getBearingRadians(); // Absolute bearing of the enemy
        double theta = Math.asin(e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / bulletVelocity(power));
        double targetX = getX() + e.getDistance() * Math.sin(absBearing);
        double targetY = getY() + e.getDistance() * Math.cos(absBearing);

        // if the target name right now is empty, find the enemy on the corner, and send it to other robots as the target
        if(targetName == null && ((targetX<100 && targetY<100) || (targetX<100 && targetY>700) || (targetX>700 && targetY<100) || (targetX>700 && targetY>700))) { // When the target robot is dead or undecided
            //setColors(Color.gray, Color.blue, Color.yellow); // debug
            targetName = e.getName();
            TargetInfo targetMessage = new TargetInfo(targetName);
            try {
                broadcastMessage(targetMessage);
            } catch (IOException ex) {
                out.println("Unable to broadcast an order.");
                ex.printStackTrace(out);
            }
        }
        
        // when the scanned robot is the current target
        if(e.getName().equals(targetName)) {
            // Track enemy
            setTurnRightRadians(e.getBearingRadians());
            setAhead(e.getDistance());

            // Radar
            // setTurnRadarLeftRadians(getRadarTurnRemaining());
            // setTurnRadarRightRadians(absBearing - getRadarHeadingRadians()); // infinitelock
            double radarTurn =  Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
            double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);

            // Adjust the radar turn so it goes that much further in the direction it is going to turn
            // overshoot the enemy to get a good sweep of them
            if(radarTurn < 0) radarTurn -= extraTurn;
            else radarTurn += extraTurn;
            setTurnRadarRightRadians(radarTurn); //Turn the radar

            // adjust the gun to the predicted location of the enemy
            double gunAngle = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + theta);
            setTurnGunRightRadians(gunAngle);
            setFire(power);
        }
        execute();
    }

    public void onHitByBullet(HitByBulletEvent e) { // What to do when you're hit by a bullet

    }

    public void onHitWall(HitWallEvent e) { // What to do when you hit a wall

    }
/*
    public void onBulletHit(BulletHitEvent e) {
        if (isTeammate(e.getName())) {
            int deg = 30;
            Random rnd = new Random();
            if (rnd.nextBoolean())
                turnLeft(deg);
            else
                turnRight(deg);
        }
    }
*/
    public void onRobotDeath(RobotDeathEvent e){ // What to do when a robot dies
        // if the death robot is the ctarget robot, empty the target name
        if(e.getName().equals(targetName)){
            targetName = null;
            TargetInfo targetMessage = new TargetInfo(null);
            try {
                broadcastMessage(targetMessage);
            } catch (IOException ex) {
                out.println("Unable to broadcast an order.");
                ex.printStackTrace(out);
            }
        }
    }

    private void randomMovement() {
        if(getTurnRemaining() == 0) {
            if(rnd.nextBoolean()) direction = -direction;
            setMaxTurnRate(3); // change the turn rate

            setAhead(100000); // always go ahead
            setTurnRight(direction * (30 + rnd.nextDouble() * 120));
            setTurnRadarRightRadians(100000);
        } else {
            double goalDirection = getHeadingRadians();
            while (!fieldRect.contains(getX() + Math.sin(goalDirection) * 150, getY() + Math.cos(goalDirection) * 150)) {
                goalDirection += direction * .1;
            }
            double turn = robocode.util.Utils.normalRelativeAngle(goalDirection - getHeadingRadians());
            if (Math.abs(turn) > Math.PI / 2) {
                turn = robocode.util.Utils.normalRelativeAngle(turn + Math.PI);
                setBack(100);
            }
            setMaxTurnRate(10); // reset the turn rate in order to avoid hitting walls
            if(turn != 0) setTurnRightRadians(turn);
        }
        execute();
    }

    private double bulletVelocity(double power) {
        return 20.0 - (3.0 * power);
    }
}