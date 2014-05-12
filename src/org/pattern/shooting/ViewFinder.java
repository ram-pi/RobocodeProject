package org.pattern.shooting;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class ViewFinder {
	
	private AdvancedRobot robot;
	private ScannedRobotEvent enemy;
	private double enemyX, enemyY;
	private double oldHeading, changedHeading;
	private List<CoordinatesInfo> pastInfos;
	
	public ViewFinder(AdvancedRobot robot, ScannedRobotEvent enemy) {
		this.robot = robot;
		this.enemy = enemy;
		this.oldHeading = -1;
		this.pastInfos = new ArrayList<>();
	}
	
	public void calculateEnemyCoordinates() {
		double absoluteBearing = robot.getHeading() + enemy.getBearing();
		enemyX = robot.getX() + enemy.getDistance()*Math.sin(absoluteBearing);
		enemyY = robot.getY() + enemy.getDistance()*Math.cos(absoluteBearing);
	}
	
	public void calculateEnemyRotation() {
		double actualHeading = enemy.getHeading();
		if (oldHeading == -1) {
			oldHeading = actualHeading;
			changedHeading = 0.0;
		} else {
			changedHeading = actualHeading - oldHeading;
			oldHeading = actualHeading;
		}
	}
	
	public void moveGunInPredictedPosition() {
		calculateEnemyCoordinates();
		calculateEnemyRotation();
		double absoluteBearing = robot.getHeading() + enemy.getBearing();
		robot.setTurnGunRight(robot.getGunHeading() - absoluteBearing);
		CoordinatesInfo c = new CoordinatesInfo(enemyX, enemyY, enemy.getHeading(), enemy.getVelocity());
		pastInfos.add(c);
	}
	
	public void moveGunNaive() {
		//double absoluteBearing = robot.getHeading() + enemy.getBearing();
		double angle = robot.getHeading() - robot.getGunHeading() + enemy.getBearing();
		robot.setTurnGunRight(Utils.normalRelativeAngleDegrees(angle));
		CoordinatesInfo c = new CoordinatesInfo(enemyX, enemyY, enemy.getHeading(), enemy.getVelocity());
		pastInfos.add(c);
	}
	
	public void moveGunWithLinearTargeting() {
		double myX = robot.getX();
		double myY = robot.getY();
		double bulletPower = Math.min(3.0, robot.getEnergy());
		double absoluteBearing = robot.getHeading() + enemy.getBearing();
		calculateEnemyCoordinates();
		double enemyHeading = enemy.getHeading();
		double enemyVelocity = enemy.getVelocity();
		
		/* Iterative Part */
		double deltaTime = 0;
		double battlefieldHeight = robot.getBattleFieldHeight();
		double battlefieldWidth = robot.getBattleFieldWidth();
		double predictedX = enemyX;
		double predictedY = enemyY;		
//		while((++deltaTime) * (20.0 - 3.0 * bulletPower) < Point2D.Double.distance(myX, myY, enemyX, enemyY)) {
//			predictedX += Math.sin(enemyHeading) * enemyVelocity;
//			predictedY += Math.cos(enemyHeading) * enemyVelocity;
//			if (predictedX < 18.0 || predictedY < 18 || predictedX > battlefieldWidth - 18.0 || predictedY > battlefieldHeight - 18-0) {
//				predictedX = Math.min(Math.max(18.0, predictedX), battlefieldWidth - 18.0);
//				predictedY = Math.min(Math.max(18.0, predictedY), battlefieldHeight - 18.0);
//				break;	
//			}
//		}
		
		predictedX += 1.0;
		predictedY += 1.0;
		System.out.println("Actual Enemy Position : " + enemyX + " " + enemyY);
		System.out.println("Prediction : " + predictedX + " " + predictedY);
		/* The prediction is correct, error moving gun */
		double theta = Utils.normalAbsoluteAngle(Math.atan2(predictedY - myY, predictedX - myX));
		System.out.println("theta -> " + theta);
		robot.setTurnGunRightRadians(Utils.normalRelativeAngle(theta - robot.getGunHeadingRadians()));
	}
	
	/* This function had to find the angle for rotate the gun for aiming a point */
	public void findAngleToMoving(double predictedX, double predictedY) {
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
