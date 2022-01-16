package G6team;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Random; // added to use random values
import java.io.IOException;
import G6common.*;

public class G6LeaderRobot extends TeamRobot {
    private double power = 1.5; // Power of the gun
    private double direction = 1; // The direction of the random movement
    private String targetName; // Target robot's name
    private MyInfo[] myInfoArray = new MyInfo[2]; // MyInfo array
    private Rectangle2D fieldRect; // Safety square on the field
    private Random rnd = new Random();
    private int aliveRobots = 3; // LSB is sub1, 2nd is sub2

    public void run() { // G6LeaderRobot's default behavior
        fieldRect = new Rectangle2D.Double(80, 80, getBattleFieldWidth() - 160, getBattleFieldHeight() - 160);
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setAdjustGunForRobotTurn(true); // Set the gun to turn independent from the robot's turn
        setAdjustRadarForGunTurn(true); // Set the radar to turn independent from the gun's turn

        while(true) randomMovement(); // Default behavior: Random movement
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
        if(isTeammate(e.getName())) return; // If the robot is teammate, go back to the default behavior
        
        double absBearing = getHeadingRadians() + e.getBearingRadians(); // Absolute bearing of the enemy

        // if the target name is empty and a scanned robot is "good," broadcast it to other robots
        if(targetName == null && isGoodTarget(e, absBearing)) {
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
            // Adjust the bullet energy
            if(e.getDistance() > 100) power = 1.5;
            else power = 3;

            // Reference: http://robowiki.net/wiki/Robocode/Butthead
            // Linear prediction gun
            double theta = Math.asin(e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / bulletVelocity(power)); // The extra angle the bullet would travel

            // Track enemy
            setTurnRightRadians(e.getBearingRadians());
            setAhead(e.getDistance());

            // Radar
            double radarTurn = Utils.normalRelativeAngle(absBearing - getRadarHeadingRadians());
            double extraTurn = Math.min(Math.atan(36.0 / e.getDistance()), Rules.RADAR_TURN_RATE_RADIANS);
            if(radarTurn < 0) radarTurn -= extraTurn;
            else radarTurn += extraTurn;
            setTurnRadarRightRadians(radarTurn); // Turn the radar

            // Adjust the gun to the predicted location of the enemy
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

    public void onBulletHit(BulletHitEvent e) {
        if (isTeammate(e.getName())) {
            back(50);
            if(rnd.nextBoolean()) turnLeft(30);
            else turnRight(30);
            ahead(100);
        }
    }

    public void onRobotDeath(RobotDeathEvent e){ // What to do when a robot dies
        // If the dead robot is the target robot, empty the target name
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
        else if(e.getName().equals("G6team.G6SubRobot1*")) aliveRobots -= 1;
        else if(e.getName().equals("G6team.G6SubRobot2*")) aliveRobots -= 2;
    }

    public void onMessageReceived(MessageEvent e) {
        if(e.getMessage() instanceof MyInfo) {
            MyInfo _myInfo = (MyInfo)e.getMessage();
            myInfoArray[_myInfo.id] = _myInfo;
        }
    }

    private void randomMovement() {
        if(getTurnRemaining() == 0) {
            if(rnd.nextBoolean()) direction = -direction;
            setMaxTurnRate(3); // Change the turn rate

            setAhead(100000); // Always go ahead
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
            setMaxTurnRate(10); // Reset the turn rate in order to avoid hitting walls
            if(turn != 0) setTurnRightRadians(turn);
        }
        execute();
    }

    private boolean isGoodTarget(ScannedRobotEvent e, double absBearing){
        double myX = getX(), myY = getY();
        double targetX = myX + e.getDistance() * Math.sin(absBearing), targetY = myY + e.getDistance() * Math.cos(absBearing);
        double Gx = 0, Gy = 0;

        // Ensure the enemy isn't on corners
        if((targetX<100 && targetY<100) || (targetX<100 && targetY>700) || (targetX>700 && targetY<100) || (targetX>700 && targetY>700)) return false;
        
        // Ensure teammates are not faraway from the enemy
        if((aliveRobots & 1) != 0 && ((targetX-myInfoArray[0].x)*(targetX-myInfoArray[0].x) + (targetY-myInfoArray[0].y)*(targetY-myInfoArray[0].y)) > getBattleFieldWidth()*getBattleFieldWidth()*4/9){
            return false;
        }
        if((aliveRobots & 2) != 0 && ((targetX-myInfoArray[1].x)*(targetX-myInfoArray[1].x) + (targetY-myInfoArray[1].y)*(targetY-myInfoArray[1].y)) > getBattleFieldWidth()*getBattleFieldWidth()*4/9){
            return false;
        }
        
        // Ensure the center of our team is not faraway from the enemy
        if(aliveRobots >= 3) { // 2 subrobots alive
            Gx = (myX + myInfoArray[0].x + myInfoArray[1].x)/3;
            Gy = (myY + myInfoArray[0].y + myInfoArray[1].y)/3;
        }else if(aliveRobots == 1){ // only subrobot 1 is alive
            Gx = (myX + myInfoArray[0].x)/2;
            Gy = (myY + myInfoArray[0].y)/2;
        }else if(aliveRobots == 2){ // only subrobot 2 is alive
            Gx = (myX + myInfoArray[1].x)/2;
            Gy = (myY + myInfoArray[1].y)/2;
        }
        if((Gx!=0 || Gy!=0) && (((targetX-Gx)*(targetX-Gx)+(targetY-Gy)*(targetY-Gy))>getBattleFieldWidth()*getBattleFieldWidth()/16)) return false;

        return true; // Now, all requirements for a "good target" are met
    }

    private double bulletVelocity(double power) {
        return 20.0 - (3.0 * power);
    }
}