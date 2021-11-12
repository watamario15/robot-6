package G6team;

import robocode.*;
import java.awt.Color;

import java.util.Random;//added to use random values

public class G6SubRobot1 extends TeamRobot {
    public void run() { // G6SubRobot1's default behavior
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

    }

    public void onHitByBullet(HitByBulletEvent e) { // What to do when you're hit by a bullet

    }

    public void onHitWall(HitWallEvent e) { // What to do when you hit a wall

    }
}
