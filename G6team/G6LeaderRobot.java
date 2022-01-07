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
    private MyInfo[] myInfoArray = new MyInfo[2]; // MyInfo array
    private Rectangle2D fieldRect; // safe square in the field
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

        // Adjust the bullet energy
        if(e.getDistance() > 100) power = 1.5;
        else power = 3;

        // Reference: http://robowiki.net/wiki/Robocode/Butthead
        // linear prediction gun
        double absBearing = getHeadingRadians() + e.getBearingRadians(); // Absolute bearing of the enemy
        double theta = Math.asin(e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / bulletVelocity(power));

        // if the target name right now is empty, find the enemy on the non-corner, and send it to other robots as the target
        if(targetName == null && isGoodTarget(e, absBearing)) { // When the target robot is dead or undecided
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

    public void onBulletHit(BulletHitEvent e) {
        if (isTeammate(e.getName())) {
            int deg = 30;
            Random rnd = new Random();
            back(50);
            if(rnd.nextBoolean()) turnLeft(deg);
            else turnRight(deg);
            ahead(100);
        }
    }

    public void onRobotDeath(RobotDeathEvent e){ // What to do when a robot dies
        // if the death robot is the target robot, empty the target name
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

    public void onMessageReceived(MessageEvent e) {
        if(e.getMessage() instanceof MyInfo) {
            MyInfo _myInfo = (MyInfo)e.getMessage();
            myInfoArray[_myInfo.id] = _myInfo;
        }
    }

    private boolean isGoodTarget(ScannedRobotEvent e, double absBearing){
        double myX = getX(), myY = getY();
        double targetX = myX + e.getDistance() * Math.sin(absBearing), targetY = myY + e.getDistance() * Math.cos(absBearing);
        double Gx = 0, Gy = 0;

        if((targetX<100 && targetY<100) || (targetX<100 && targetY>700) || (targetX>700 && targetY<100) || (targetX>700 && targetY>700)) return false;
        else {
            if((aliveRobots & 1) != 0 && ((targetX-myInfoArray[0].x)*(targetX-myInfoArray[0].x) + (targetY-myInfoArray[0].y)*(targetY-myInfoArray[0].y)) > getBattleFieldWidth()*getBattleFieldWidth()*4/9){
                return false;
            }
            if((aliveRobots & 2) != 0 && ((targetX-myInfoArray[1].x)*(targetX-myInfoArray[1].x) + (targetY-myInfoArray[1].y)*(targetY-myInfoArray[1].y)) > getBattleFieldWidth()*getBattleFieldWidth()*4/9){
                return false;
            }

            if(aliveRobots>=3) { // 2 subrobots alive
                Gx = (myX + myInfoArray[0].x + myInfoArray[1].x)/3;
                Gy = (myY + myInfoArray[0].y + myInfoArray[1].y)/3;
            }else if(aliveRobots == 1){ // only subrobot 1 is alive
                Gx = (myX + myInfoArray[0].x)/2;
                Gy = (myY + myInfoArray[0].y)/2;
            }else if(aliveRobots == 2){ // only subrobot 2 is alive
                Gx = (myX + myInfoArray[1].x)/2;
                Gy = (myY + myInfoArray[1].y)/2;
            }
            if(Gx!=0 && Gy!=0 && (((targetX-Gx)*(targetX-Gx)+(targetY-Gy)*(targetY-Gy))>getBattleFieldWidth()*getBattleFieldWidth()/16)) return false;
            else return true;
        }
    }

    private double bulletVelocity(double power) {
        return 20.0 - (3.0 * power);
    }
}