package G6team;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Random; // added to use random values
import G6common.*;
import java.io.IOException;

public class G6SubRobot2 extends TeamRobot {
    private double power = 2; // power of the gun
    private double direction = 1; // The direction of the random movement
    private String targetName, leaderName = "G6team.G6LeaderRobot*";
    private TargetInfo leaderMessage;
    private Rectangle2D fieldRect; // safety square in the field
    private Random rnd = new Random();

    public void run() { // G6SubRobot2's default behavior
        fieldRect = new Rectangle2D.Double(80, 80, getBattleFieldWidth() - 160, getBattleFieldHeight() - 160);
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while(true) randomMovement();
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
        if (isTeammate(e.getName()) || (!leaderName.equals(getName()) && targetName!=null && !e.getName().equals(targetName))) return; // If the robot is teammate/not target, go back to the random movement

        // Reference: http://robowiki.net/wiki/Robocode/Butthead
        // linear prediction gun
        double absBearing = getHeadingRadians() + e.getBearingRadians(); // Absolute bearing of the enemy
        double theta = Math.asin(e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / bulletVelocity(power)); // The extra angle the bullet would travel
        double targetX = getX() + e.getDistance() * Math.sin(absBearing);
        double targetY = getY() + e.getDistance() * Math.cos(absBearing);

        if(leaderName.equals(getName()) && targetName == null && ((targetX<100 && targetY<100) || (targetX<100 && targetY>700) || (targetX>700 && targetY<100) || (targetX>700 && targetY>700))) { // When the target robot is dead or undecided
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

    public void onRobotDeath(RobotDeathEvent e){ // What to do when a robot dies
        if(e.getName().equals(leaderName)){
            if(getTeammates() != null){
                try {
                    broadcastMessage(new EnergyInfo(getEnergy()));
                } catch (IOException ex) {
                    out.println("Unable to broadcast my energy information.");
                    ex.printStackTrace(out);
                }
            }else{
                leaderName = getName();
            }
        }
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

    public void onMessageReceived(MessageEvent e) {
        if(e.getMessage() instanceof TargetInfo) {
            leaderMessage = (TargetInfo)e.getMessage();
            targetName = leaderMessage.targetName;
        }else if(e.getMessage() instanceof EnergyInfo) {
            if(getEnergy() > ((EnergyInfo)e.getMessage()).energy) leaderName = getName();
            else leaderName = "G6team.G6SubRobot1*";
        }
        return;
    }

    private double bulletVelocity(double power) {
        return 20.0 - (3.0 * power);
    }
}
