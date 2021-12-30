package G6team;

import robocode.*;
import robocode.util.Utils;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Random; // added to use random values
import G6common.*;
import java.io.IOException;

public class G6SubRobot2 extends TeamRobot {
    private double power = 1.5; // power of the gun
    private double direction = 1; // The direction of the random movement
    private long interval = 0;
    private MyInfo[] myInfoArray = new MyInfo[2]; // MyInfo array
    private int timeCounter;
    private String intervalName;
    private String targetName, leaderName = "G6team.G6LeaderRobot*";
    private String myName;
    private TargetInfo leaderMessage;
    private Rectangle2D fieldRect; // safety square in the field
    private Random rnd = new Random();

    public void run() { // G6SubRobot2's default behavior
        fieldRect = new Rectangle2D.Double(80, 80, getBattleFieldWidth() - 160, getBattleFieldHeight() - 160);
        myName = getName();
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while(true) randomMovement();
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
        if(!isPass(e)) return;

        timeCounter++;
        if(timeCounter >= 30){
            timeCounter = 0;
            MyInfo myInfo = new MyInfo(getX(), getY(), 1);
            try {
                broadcastMessage(myInfo);
            } catch (IOException ex) {
                out.println("Unable to broadcast my information.");
                ex.printStackTrace(out);
            }
        }

        // Adjust the bullet energy
        if(e.getDistance() > 100) power = 1.5;
        else power = 3;

        // Reference: http://robowiki.net/wiki/Robocode/Butthead
        // linear prediction gun
        double absBearing = getHeadingRadians() + e.getBearingRadians(); // Absolute bearing of the enemy
        double theta = Math.asin(e.getVelocity() * Math.sin(e.getHeadingRadians() - absBearing) / bulletVelocity(power)); // The extra angle the bullet would travel
        double targetX = getX() + e.getDistance() * Math.sin(absBearing);
        double targetY = getY() + e.getDistance() * Math.cos(absBearing);
        
        // targeting single enemy and he's gone to corner
        if(targetName!=null && targetName.equals(e.getName()) && (targetX<100 && targetY<100) || (targetX<100 && targetY>700) || (targetX>700 && targetY<100) || (targetX>700 && targetY>700)){
            interval = getTime();
            intervalName = e.getName();
        }

        // when this robot is the leader, broadcast a message
        if(leaderName.equals(myName) && targetName == null && !(((targetX<100 && targetY<100) || (targetX<100 && targetY>700) || (targetX>700 && targetY<100) || (targetX>700 && targetY>700)))) { // When the target robot is dead or undecided
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
        timeCounter++;
        if(timeCounter >= 30){
            timeCounter = 0;
            MyInfo myInfo = new MyInfo(getX(), getY(), 1);
            try {
                broadcastMessage(myInfo);
            } catch (IOException ex) {
                out.println("Unable to broadcast my information.");
                ex.printStackTrace(out);
            }
        }

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
                leaderName = myName;
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

    public void onMessageReceived(MessageEvent e) {
        if(e.getMessage() instanceof TargetInfo) {
            leaderMessage = (TargetInfo)e.getMessage();
            targetName = leaderMessage.targetName;
        }else if(e.getMessage() instanceof EnergyInfo) {
            if(getEnergy() > ((EnergyInfo)e.getMessage()).energy) leaderName = myName;
            else leaderName = "G6team.G6SubRobot1*";
        }else if(e.getMessage() instanceof MyInfo) {
            MyInfo _myInfo = (MyInfo)e.getMessage();
            myInfoArray[_myInfo.id] = _myInfo;
        }
    }

    private boolean isPass(ScannedRobotEvent e){
        String enemyName = e.getName();

        if(isTeammate(enemyName)) return false;
        if(leaderName.equals(myName)){ // This robot is the leader
            if(enemyName.equals(targetName)) return true;
            else return false;
        }else{
            if(targetName!=null && !enemyName.equals(targetName)) return false; // If the robot is teammate/not target, go back to the random movement
            else{
                if(intervalName!=null){
                    if(targetName.equals(intervalName)){
                        if(getTime()-interval <= 300) return false;
                        else{
                            intervalName = null;
                            interval = 0;
                            return true;
                        }
                    }
                    else return true;
                }
                else return true;
            }
        }
    }
    private double bulletVelocity(double power) {
        return 20.0 - (3.0 * power);
    }
}
