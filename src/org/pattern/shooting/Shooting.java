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

public class Shooting implements Observer {
	private AdvancedRobot robot;
	private Enemy enemy;
	private double bulletPower;
	private ViewFinder sniper;
	private shooterType type;
	private List<Bullet> bullets;
	private VirtualGun headOnTargetVG;
	private int ticksToPredict;

	public enum shooterType {
		Circular, MEA, HeadOnTarget
	}

	public Shooting(AdvancedRobot robot) {
		this.robot = robot;
		this.sniper = new ViewFinder(robot);
		this.type = shooterType.Circular;
		this.bullets = new LinkedList<>();
		this.headOnTargetVG = new VirtualGun(robot, null);
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

	public void tryVirtualBulletHoT(Enemy enemy) {
		headOnTargetVG.setEnemy(enemy);
		Bullet b = new Bullet(new Point2D.Double(robot.getX(), robot.getY()),
				ShootingUtils.findAngle(enemy.getX(), enemy.getY(),
						robot.getX(), robot.getX()), bulletPower, robot.getTime(),
						new Point2D.Double(enemy.getX(), enemy.getY()));
		headOnTargetVG.getBullets().add(b);
		System.out.println("Bullet added to headOnTarget VirtualGun");
	}

	public void doShooting(ScannedRobotEvent e) {
		firingSettings();
		sniper.setRobot(robot);
		enemy = new Enemy(e, robot);
		headOnTargetVG.update();
		tryVirtualBulletHoT(enemy);
		checkBestShooting();
		if (type == shooterType.Circular && findShootingPointCircularTargeting(enemy) && robot.getGunHeat() == 0.0) {
			addBullet();
			robot.fire(bulletPower);
		}

		if (type == shooterType.HeadOnTarget && robot.getGunHeat() == 0.0) {
			shotHeadOnTarget(enemy);
			addBullet();
			robot.fire(bulletPower);
		}
	}

	public void checkBestShooting() {
		if (headOnTargetVG.getRatio() > 0.5)
			type = shooterType.HeadOnTarget;
	}

	public void addBullet() {
		Point2D from = new Point2D.Double(robot.getX(), robot.getY());
		Bullet b = new Bullet(from, robot.getGunHeading(), bulletPower, robot.getTime(), null);
		bullets.add(b);
	}

	public boolean findShootingPointCircularTargeting(Enemy e) {
		Point2D enemyPosition = new Point2D.Double(e.getX(), e.getY());
		int direction = (int) Math.signum(e.getVelocity());
		if (direction == 0)
			direction = 1;
		Projection houdini = new Projection(enemyPosition, e.getHeading(), e.getVelocity(), direction, 0);
		List<tickProjection> possibleTargets = houdini.projectNextTicks(ticksToPredict);
		for (int i = 0; i < possibleTargets.size(); i++) {
			tickProjection tmpTick = possibleTargets.get(i);
			double tmpDistance = Point.distance(robot.getX(), robot.getY(), tmpTick.getPosition().getX(), tmpTick.getPosition().getY());
			if (Math.abs(i - tmpDistance/(20 - 3*bulletPower)) < 2 && checkPosition(tmpTick.getPosition())) {
				sniper.rotateGun(tmpTick.getPosition().getX(), tmpTick.getPosition().getY());
				ticksToPredict = 30;
				return true;
			}
		}
		ticksToPredict += 30;
		return false;
	}

	public void shotAtMEA(Enemy e) {
		sniper.rotateGunMEA(e.getX(), e.getY(), bulletPower, e.getHeading(), e.getVelocity());
	}

	public void shotHeadOnTarget(Enemy e) {
		sniper.rotateGun(e.getX(), e.getY());
	}

	public boolean checkPosition(Point2D p) {
		if (p.getX() < 0 || p.getY() < 0 || p.getX() > robot.getBattleFieldWidth() || p.getY() > robot.getBattleFieldHeight()) {
			//System.out.println("The shooting point is over the battlefield!");
			return false;
		}

		return true;
	}

	public VirtualGun getVirtualGun() {
		return this.headOnTargetVG;
	}

	public void firingSettings() {
		this.bulletPower = Math.min(3.0, robot.getEnergy());
	}
}
