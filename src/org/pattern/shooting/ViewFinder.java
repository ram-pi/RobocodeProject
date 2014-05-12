package org.pattern.shooting;

import robocode.AdvancedRobot;
import robocode.util.Utils;

public class ViewFinder {
	
	private AdvancedRobot robot;
	private double aimX, aimY;
	
	public ViewFinder(AdvancedRobot robot) {
		this.robot = robot;
	}
	
	public void setPointToShot(double aimX, double aimY) {
		this.aimX = aimX;
		this.aimY = aimY;
	}
	
	public void moveGunToPoint() {
		rotateGun(aimX, aimY);
	}	
	
	public void rotateGun(double predictedX, double predictedY) {
		double theta = ShootingUtils.findAngle(predictedX, predictedY, robot.getX(), robot.getY());
		theta = Utils.normalAbsoluteAngleDegrees(theta - robot.getGunHeading());
		theta = Utils.normalRelativeAngleDegrees(theta);
		robot.setTurnGunRight(theta);
	}
	
	public AdvancedRobot getRobot() {
		return robot;
	}
	public void setRobot(AdvancedRobot robot) {
		this.robot = robot;
	}
	
	
}
