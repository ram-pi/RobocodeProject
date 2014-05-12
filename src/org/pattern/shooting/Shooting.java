package org.pattern.shooting;

import java.util.Observable;
import java.util.Observer;

import org.robot.Enemy;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Shooting implements Observer {
	private AdvancedRobot robot;
	private Enemy enemy;
	private double bulletPower;
	private ViewFinder sniper;
	private LinearTargeting naiveShooter;

	public Shooting(AdvancedRobot robot) {
		this.robot = robot;
		sniper = new ViewFinder(robot, null);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof AdvancedRobot) {
			robot = (AdvancedRobot) arg;
			System.out.println("Robot was updated");
		}
		if (arg instanceof Enemy) {
			enemy = (Enemy) enemy;	
		}
	}

	public void doShooting(ScannedRobotEvent enemy) {
		
		sniper.setRobot(robot);
		sniper.setEnemy(enemy);
		// Set the gun in the direction of the enemy
		//robot.setTurnGunRight(robot.getHeading() - robot.getGunHeading() + enemy.getBearing());
		if (shouldShot(enemy)) {
			//sniper.moveGunInPredictedPosition();
			//sniper.moveGunNaive();
			sniper.moveGunWithLinearTargeting();
			firingSettings(enemy);
			robot.fire(this.bulletPower);
		}
	}

	public boolean shouldShot(ScannedRobotEvent enemy) {
		return true;
	}

	// Set the values for fire power, farther is the enemy slower is the bullet
	public void firingSettings(ScannedRobotEvent enemy) {
		//this.bulletPower = Math.min(400/enemy.getDistance(), 3.0);
		this.bulletPower = Math.min(3.0, robot.getEnergy());
	}
}
