package org.pattern.shooting;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;
import org.robot.Enemy;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Shooting implements Observer {
	private AdvancedRobot robot;
	private Enemy enemy;
	private double bulletPower;
	private ViewFinder sniper;
	private Point2D shootingAt;
	private shooterType type;
	private List<Bullet> bullets;

	public enum shooterType {
		Circular, MEA, HeadOnTarget
	}

	public Shooting(AdvancedRobot robot) {
		this.robot = robot;
		this.sniper = new ViewFinder(robot);
		this.type = shooterType.HeadOnTarget;
		this.bullets = new LinkedList<>();
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

	public void doShooting(ScannedRobotEvent e) {
		firingSettings();
		sniper.setRobot(robot);
		enemy = new Enemy(e, robot);
		if (type == shooterType.Circular) {
			if (findShootingPointCircularTargeting(enemy)) {
				addBullet();
				robot.fire(this.bulletPower);
			}
		}
		if (type == shooterType.MEA) {
			shotAtMEA(enemy);
			addBullet();
			robot.fire(bulletPower);
		}
		if (type == shooterType.HeadOnTarget) {
			shotHeadOnTarget(enemy);
			addBullet();
			robot.fire(bulletPower);
		}
	}
	
	public void addBullet() {
		Point2D from = new Point2D.Double(robot.getX(), robot.getY());
		int direction = (int) Math.signum(enemy.getVelocity());
		if (direction == 0)
			direction = 1;
		Bullet b = new Bullet(from, robot.getGunHeading(), bulletPower, direction, robot.getTime());
		bullets.add(b);
	}

	public boolean findShootingPointCircularTargeting(Enemy e) {
		Point2D enemyPosition = new Point2D.Double(e.getX(), e.getY());
		int direction = (int) Math.signum(e.getVelocity());
		if (direction == 0)
			direction = 1;
		Projection houdini = new Projection(enemyPosition, e.getHeading(), e.getVelocity(), direction, 0);
		List<tickProjection> possibleTargets = houdini.projectNextTicks(2000);
		for (int i = 0; i < possibleTargets.size(); i++) {
			tickProjection tmpTick = possibleTargets.get(i);
			double tmpDistance = Point.distance(robot.getX(), robot.getY(), tmpTick.getPosition().getX(), tmpTick.getPosition().getY());
			if (Math.abs(i - tmpDistance/(20 - 3*bulletPower)) < 2 ) {
				sniper.rotateGun(tmpTick.getPosition().getX(), tmpTick.getPosition().getY());
				return true;
			}
		}
		return false;
	}

	public void shotAtMEA(Enemy e) {
		sniper.rotateGunMEA(e.getX(), e.getY(), bulletPower, e.getHeading(), e.getVelocity());
	}

	public void shotHeadOnTarget(Enemy e) {
		sniper.rotateGun(e.getX(), e.getY());
	}

	public boolean shouldShot(ScannedRobotEvent enemy) {
		return true;
	}

	public void firingSettings() {
		this.bulletPower = Math.min(3.0, robot.getEnergy());
	}
}
