package G6team;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Random; // added to use random values
import G6common.*;

public class G6SubRobot2 extends TeamRobot {
    private double power = 2; // power of the gun
    private String targetName;
    private Message leaderMessage;

    public void run() { // G6SubRobot2's default behavior
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while(true) randomMovement();
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
        if (isTeammate(e.getName()) || !e.getName().equals(targetName)) { // If the robot is teammate/not target, go back to the random movement
            setColors(Color.gray, Color.red, Color.yellow); // debug
            return;
        }
        setColors(Color.gray, Color.yellow, Color.yellow); // debug

        // Reference: http://robowiki.net/wiki/Robocode/Butthead
        // linear prediction gun
        double absBearing = getHeadingRadians() + e.getBearingRadians(); // Absolute bearing of the enemy
        double theta = Math.asin(e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / bulletVelocity(power)); // The extra angle the bullet would travel

        // Track enemy
        setTurnRightRadians(e.getBearingRadians());
        setAhead(e.getDistance());
        // setTurnRadarRightRadians(absBearing - getRadarHeadingRadians()); // infinite lock

        double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
        double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);

        // Adjust the radar turn so it goes that much further in the direction it is going to turn
        // Basically if we were going to turn it left, turn it even more left, if right, turn more right.
        // This allows us to overshoot our enemy so that we get a good sweep that will not slip.
        if(radarTurn < 0) radarTurn -= extraTurn;
        else radarTurn += extraTurn;
        setTurnRadarRightRadians(radarTurn); // Turn the radar

        double gunAngle = Utils.normalRelativeAngle(absBearing - getGunHeadingRadians() + theta);
        setTurnGunRightRadians(gunAngle);
        setFire(power);

        execute();
    }

    public void onHitByBullet(HitByBulletEvent e) { // What to do when you're hit by a bullet

    }

    public void onHitWall(HitWallEvent e) { // What to do when you hit a wall

    }

    private void randomMovement() {
        Random rnd = new Random();
        double direction = 1;
        if(rnd.nextBoolean()) direction *= -1;
        setMaxTurnRate(3); // change the turn rate

        Rectangle2D fieldRect = new Rectangle2D.Double(80, 80, getBattleFieldWidth() - 160, getBattleFieldHeight() - 160); // make a safety square in the field
        setAhead(100000); // always go ahead
        setTurnRight(direction * (30 + rnd.nextDouble() * 120));
        setTurnRadarRightRadians(100000);
        while(getTurnRemaining() != 0) {
            double goalDirection = getHeadingRadians();
            while(!fieldRect.contains(getX() + Math.sin(goalDirection) * 150, getY() + Math.cos(goalDirection) * 150)) {
                goalDirection += direction * .1;
            }
            double turn = robocode.util.Utils.normalRelativeAngle(goalDirection - getHeadingRadians());
            if(Math.abs(turn) > Math.PI / 2) {
                turn = robocode.util.Utils.normalRelativeAngle(turn + Math.PI);
                setBack(100);
            }
            setMaxTurnRate(10); // reset the turn rate to avoid hitting walls
            if(turn != 0) setTurnRightRadians(turn);
            execute();
        }
    }

    public void onMessageReceived(MessageEvent e) {
        leaderMessage = (Message)e.getMessage();
        targetName = leaderMessage.targetName;
        return;
    }

    private double bulletVelocity(double power) {
        return 20.0 - (3.0 * power);
    }
}
