package G6melee;

import robocode.*;
import java.awt.Color;

import java.util.Random;//added to use random values

public class G6MeleeRobot extends AdvancedRobot {
    double direction = 100000;

    public void run() { // G6MeleeRobot's default behavior
        setColors(Color.gray, Color.yellow, Color.yellow); // body, gun, radar
        setTurnRadarRightRadians(100000); // Always search for enemies in all direction

        // random movement
        Random rnd = new Random();
        double x, y, height = getBattleFieldHeight();
        double distance = height / 3;

        while (true) {
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
        // end of random movement
    }

    public void onScannedRobot(ScannedRobotEvent e) { // What to do when you see another robot
        int okDistance = 200;
        if (getDistance() <= okDistance) {
            // ButtHead no nanigashika wo suru
            double absoluteBearing;
            double bearing;
            double lead;

            // gun
            // normalRelativeAngle:角度の正規化([-pi,pi)にする)
            // getHeadingRadians:向きをラジアンで得る．
            // e.getBearingRadians:敵の相対位置(自機からの)をラジアンで得る．
            // getVelocity:速度を得る．
            setTurnGunRightRadians(Utils
                    .normalRelativeAngle((absoluteBearing = getHeadingRadians() + (bearing = e.getBearingRadians()))
                            + (lead = Math
                                    .asin(e.getVelocity() / 14 * Math.sin(e.getHeadingRadians() - absoluteBearing)))
                            - getGunHeadingRadians()));
            setTurnGunRightRadians(Utils.normalRelativeAngle(getHeadingRadians() + e.getBearingRadians()
                    + Math.asin(e.getVelocity() / 14 * Math.sin(e.getHeadingRadians() - absoluteBearing))
                    - getGunHeadingRadians()));
            // orbiting
            setAhead(direction);
            setTurnRightRadians(
                    Math.cos(bearing - ((absoluteBearing = e.getDistance()) - 160) * (direction / 35000000)));
            setFire(2);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) { // What to do when you're hit by a bullet

    }

    public void onHitWall(HitWallEvent e) { // What to do when you hit a wall

    }
}
