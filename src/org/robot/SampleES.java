package org.robot;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

public class SampleES extends AdvancedRobot {

	boolean nearWall;
	public int moveDirection = 1; // Switch direction from 1 to -1 
	
	@Override
	public void run() {
		isNearWall();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		
		while(true) {
			isNearWall();
			turnRadarRight(360);
			move();
			execute();
		}
	}
	
	public void move() {
		setAhead(100*moveDirection);
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent enemy) {
		squaringOff(enemy);
		// Lock on enemy
		setTurnRadarRight(getHeading() - getRadarHeading() + enemy.getBearing());
		setTurnGunRight(getHeading() - getGunHeading() + enemy.getBearing());
		if (shouldShot(enemy)) {
			firingRight(enemy);
			fire(1.0);
		}
		move();
	}
	
	// Set the values for fire power, farther is the enemy slower is the bullet
	public void firingRight(ScannedRobotEvent enemy) {
		setFire(Math.min(400/enemy.getDistance(), 3.0)); // The fire power have a range of 0.1 to 3.0
	}
	
	public boolean shouldShot(ScannedRobotEvent enemy) {
		double width = getWidth();
		double height = getHeight();
		double maximumDistance = Math.min(width, height);
		return true;
	}
	
	@Override
	public void onHitWall(HitWallEvent event) {
		// Switch direction if we hit a wall
		moveDirection *= -1;
	}
	
	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		// Switch direction if we are hitting by enemy' s bullets
		moveDirection *= -1;
	}
	
	// Check if the robot is near the wall
	public void isNearWall() {
		if (getX() <= 50 || getY() <= 50 || getBattleFieldWidth() - getX() <= 50 || getBattleFieldHeight() - getY() <= 50)
			nearWall = true;
		else
			nearWall = false;
	}
	
	// Set perpendicular to the enemy to avoid better its bullets
	public void squaringOff(ScannedRobotEvent enemy) {
		setTurnRight(enemy.getBearing() + 90);
	}
	
}
