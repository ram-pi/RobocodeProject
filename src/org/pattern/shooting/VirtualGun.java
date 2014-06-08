package org.pattern.shooting;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.utils.Costants;
import org.pattern.utils.Utils;
import org.robot.Enemy;

import com.sun.org.apache.bcel.internal.Constants;

import robocode.AdvancedRobot;

/* Perform a virtual gun that should simulate more bullet' s path */

public class VirtualGun {
	private List<Bullet> bullets;
	private AdvancedRobot robot;
	private Enemy enemy;
	int score = 0;
	private int passed = 0;
	
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getPassed() {
		return passed;
	}

	public void setPassed(int passed) {
		this.passed = passed;
	}

	public VirtualGun(AdvancedRobot robot, Enemy enemy) {
		this.robot = robot;
		this.enemy = enemy;
		this.bullets = new LinkedList<>();
	}
	
	public void fire(double absBearing, double firePower) {
		Bullet b = new Bullet();
		Point2D myPosition = new Point2D.Double(robot.getX(), robot.getY());
		b.setActualPosition(myPosition);
		b.setFrom(myPosition);
		b.setPower(firePower);
		b.setBearing(absBearing);
		b.setTime(robot.getTime());
		bullets.add(b);
	}
	
	public void checkEnemy(Enemy e) {
		double vel;
		List<Bullet> toRemove = new LinkedList<>();
		for (Bullet b : bullets) {
			vel = 20 - 3 * b.getPower();
			if (Math.abs(e.getPosition().distance(b.getFrom()) - (robot.getTime() - b.getTime()) * vel) < Costants.GF_DIST_BULLET_HIT) {
				double botWidth = Math.toDegrees(36/b.getFrom().distance(e.getPosition()));
				double perfectAngle = Utils.absBearing(b.getFrom(), e.getPosition());
				if (Math.abs(perfectAngle - b.getBearing()) < botWidth/2){
					score++;
				} else {
					passed++;
				}
				
				
				toRemove.add(b);
			}
			if (e.getPosition().distance(b.getFrom()) < (robot.getTime() - b.getTime()) * vel) {
				toRemove.add(b);
			}
		}
		bullets.removeAll(toRemove);
	}
	
	public void update() {
		for (Bullet b : bullets) {
			long timeElapsed = robot.getTime() - b.getTime();
			double bulletSpeed = 20 - 3*getBulletPower();
			double spaceWalked = (double) timeElapsed * bulletSpeed;
			double startX = b.getActualPosition().getX();
			double startY = b.getActualPosition().getY();
//			double angle = ShootingUtils.findAngle(b.getTargetPosition().getX(), b.getTargetPosition().getY(), startX, startY);
			double angle = b.getBearing();
			/*
			startX = startX + (spaceWalked*(Math.sin(Math.toRadians(b.getBearing()))));
			startY = startY + (spaceWalked*(Math.cos(Math.toRadians(b.getBearing()))));
			*/
			startX = startX + (spaceWalked*(Math.cos(Math.toRadians(90 - angle))));
			startY = startY + (spaceWalked*(Math.sin(Math.toRadians(90 - angle))));
			b.setTime(robot.getTime());
			Point2D tmp = new Point2D.Double(startX, startY);
			b.setActualPosition(tmp);
			
			/* Verify if the bullet is out the arena */
			if (startX < 0 || startX > robot.getBattleFieldWidth() || startY < 0 || startY > robot.getBattleFieldHeight())
				b.setPassed(true);
			
			/* Verify if the bullet over pass the enemy */
			if (b.getFrom().getX() > b.getActualPosition().getX() && enemy.getX() > b.getActualPosition().getX()) {
				b.setPassed(true);
			} else if (b.getFrom().getX() < b.getActualPosition().getX() && enemy.getX() < b.getActualPosition().getX()) {
				b.setPassed(true);
			} else if (b.getFrom().getY() > b.getActualPosition().getY() && enemy.getY() > b.getActualPosition().getY()) {
				b.setPassed(true);
			} else if (b.getFrom().getY() < b.getActualPosition().getY() && enemy.getY() < b.getActualPosition().getY()) {
				b.setPassed(true);
			}
			
			/* Verify if the bullet takes the enemy */
			if (startX == enemy.getX() && startY == enemy.getY() || Point2D.distance(startX, startY, enemy.getX(), enemy.getY()) < 5) {
				b.setEnemyFired(true);
				b.setPassed(true);
				System.out.println("Enemy shot...");
			}
		}
	}
	
	public double getRatio() {
		int shotWell = 0;
		int shotPassed = 0;
		for (Bullet b : bullets) {
			if (b.isPassed() && b.isEnemyFired()) 
				shotWell++;
			if (b.isPassed())
				shotPassed++;
		}
		if (shotWell == 0)
			return 0.0;
		
		return (double) shotPassed / (double) shotWell;
	}
	
	public double getBulletPower() {
		double bulletPower = Math.min(3.0, robot.getEnergy());
		return bulletPower;
	}
	
	public void consumeOnPaint(Graphics2D g) {
		for (Bullet b : bullets) {
			if (!b.isPassed())
				g.drawRect((int)b.getActualPosition().getX(), (int)b.getActualPosition().getY(), 10, 10);
		}
	}

	public List<Bullet> getBullets() {
		return bullets;
	}

	public AdvancedRobot getRobot() {
		return robot;
	}

	public Enemy getEnemy() {
		return enemy;
	}

	public void setBullets(List<Bullet> bullets) {
		this.bullets = bullets;
	}

	public void setRobot(AdvancedRobot robot) {
		this.robot = robot;
	}

	public void setEnemy(Enemy enemy) {
		this.enemy = enemy;
	}
}
