package org.pattern.shooting;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class LinearTargeting {
	
	private AdvancedRobot robot;
	private ScannedRobotEvent enemy;

	public LinearTargeting(AdvancedRobot robot, ScannedRobotEvent enemy) {
		this.robot = robot;
		this.enemy = enemy;
	}
	
	// Move gun is used for a non iterative call
	public void moveGun() {
		double absoluteBearing = robot.getHeadingRadians() + enemy.getBearingRadians();
		robot.setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - 
		    robot.getGunHeadingRadians() + (enemy.getVelocity() * Math.sin(enemy.getHeadingRadians() - 
		    absoluteBearing) / 13.0)));
	}

	public AdvancedRobot getRobot() {
		return robot;
	}

	public ScannedRobotEvent getEnemy() {
		return enemy;
	}

	public void setRobot(AdvancedRobot robot) {
		this.robot = robot;
	}

	public void setEnemy(ScannedRobotEvent enemy) {
		this.enemy = enemy;
	}
	
}
