package org.pattern.shooting;

import robocode.AdvancedRobot;
import robocode.util.Utils;

public class ViewFinder {
	
	private AdvancedRobot robot;
	
	public ViewFinder(AdvancedRobot robot) {
		this.robot = robot;
	}	
	
	public void rotateGun(double predictedX, double predictedY) {
		double theta = ShootingUtils.findAngle(predictedX, predictedY, robot.getX(), robot.getY());
		theta = Utils.normalAbsoluteAngleDegrees(theta - robot.getGunHeading());
		theta = Utils.normalRelativeAngleDegrees(theta);
		robot.setTurnGunRight(theta);
	}
	
	/* This function should aim the MEA of an enemy*/
	public void rotateGunMEA(double enemyX, double enemyY, double firePower, double heading, double velocity) {
		double theta = ShootingUtils.findAngle(enemyX, enemyY, robot.getX(), robot.getY());
		theta = Utils.normalAbsoluteAngleDegrees(theta - robot.getGunHeading());
		theta = Utils.normalRelativeAngleDegrees(theta);
		double omega = ShootingUtils.maximumEscapeAngle(firePower);
		heading = Utils.normalRelativeAngleDegrees(heading);
		int direction = (int) Math.signum(velocity);
		if ((heading < 0 && direction == 1) || (heading >= 0 && direction != 1)) {
			double angle = theta + omega;
			robot.setTurnGunRight(angle);
		}
		else if ((heading >= 0 && direction == 1) || (heading < 0 && direction != 1) ) {
			double angle = theta - omega;
			robot.setTurnGunRight(angle);
		}
	}
	
	public AdvancedRobot getRobot() {
		return robot;
	}
	public void setRobot(AdvancedRobot robot) {
		this.robot = robot;
	}
	
	
}
