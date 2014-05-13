package org.pattern.shooting;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;
import org.robot.Enemy;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Shooting implements Observer {
	private AdvancedRobot robot;
	private Enemy enemy;
	private double bulletPower;
	private ViewFinder sniper;
	private Point2D shootingAt;

	public Shooting(AdvancedRobot robot) {
		this.robot = robot;
		sniper = new ViewFinder(robot);
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
		firingSettings();
		sniper.setRobot(robot);
		if (findShootingPoint(enemy)) {
			sniper.moveGunToPoint();
			robot.fire(this.bulletPower);
		}
	}
	
	public boolean findShootingPoint(ScannedRobotEvent enemy) {
		Enemy e = new Enemy(enemy, this.robot);
		Point2D enemyPosition = new Point2D.Double(e.getX(), e.getY());
		int direction = (int) Math.signum(e.getVelocity());
		if (direction == 0)
			direction = 1;
		Projection houdini = new Projection(enemyPosition, e.getHeading(), e.getVelocity(), direction, e.getHeading());
		List<tickProjection> possibleTargets = houdini.projectNextTicks(2000);
		for (int i = 0; i < possibleTargets.size(); i++) {
			tickProjection tmpTick = possibleTargets.get(i);
			double tmpDistance = Point.distance(robot.getX(), robot.getY(), tmpTick.getPosition().getX(), tmpTick.getPosition().getY());
			if (Math.abs(i - tmpDistance/(20 - 3*bulletPower)) < 2 ) {
				sniper.setPointToShot(tmpTick.getPosition().getX(), tmpTick.getPosition().getY());
				return true;
			}
		}
		return false;
	}
	

	public boolean shouldShot(ScannedRobotEvent enemy) {
		return true;
	}

	public void firingSettings() {
		this.bulletPower = Math.min(3.0, robot.getEnergy());
	}
}
