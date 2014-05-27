package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;

import org.pattern.movement.PositionFinder;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class TheTester extends AdvancedRobot {

	private java.util.Hashtable<String, Enemy> enemies;
	private Enemy target;
	private Point2D.Double lastPosition;
	private Point2D.Double nextPosition;
	private Point2D.Double actualPosition;
	private double oldDistance;
	private double energy;

	@Override
	public void run() {
		setColors(Color.white, Color.black, Color.black);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		enemies = new java.util.Hashtable<String, Enemy>();

		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		lastPosition = nextPosition = actualPosition = new Point2D.Double(getX(), getY());

		target = new Enemy();

		while (true) {
			lastPosition = actualPosition;
			actualPosition = new Point2D.Double(getX(), getY());
			energy = getEnergy();

			if (!target.isDead() && getTime() > 9 && energy > 2) {
				doSomething();
			}

			execute();
		}

	}

	private void doSomething() {
		energy = getEnergy();
		double distanceToTarget = actualPosition.distance(target.getPosition());
		Boolean forceSearching = false;

		/* Perform head on target for gun movement */
		double turnGunAmt = (getHeadingRadians() + target.getBearingRadians() - getGunHeadingRadians());
		if (getGunTurnRemaining() < 10) {
			setTurnGunRightRadians(turnGunAmt);
		}

		/* Movement Settings, find the next position */
		double distanceToNewPosition = actualPosition.distance(nextPosition);
		if (distanceToNewPosition < 15 || forceSearching) {
			PositionFinder p = new PositionFinder(enemies, this);
			//Point2D.Double testPoint = p.findBestPoint(200);
			Point2D.Double testPoint = p.findBestPointInRange(200, 100.0);
			nextPosition = testPoint;
			lastPosition = actualPosition;
			forceSearching = false;
		}

		/* Movement to nextPosition */
		else {
			double angle = calcAngle(nextPosition, actualPosition) - getHeadingRadians();
			double direction = 1;

			if (Math.cos(angle) < 0) {
				angle += Math.PI;
				direction = -1;
			}
			setAhead(distanceToNewPosition*direction);
			angle = Utils.normalRelativeAngle(angle);
			setTurnRightRadians(angle);
		}

	}

	public double evaluatePosition(Point2D.Double p) {
		double eval = 0.0;

		for (String key : enemies.keySet()) {
			Enemy tmp = enemies.get(key);
			if (!tmp.isDead()) {
				double dangerousEnemy = Math.min(tmp.getEnergy()/getEnergy(), 2);
				double distance = p.distanceSq(tmp.getPosition());
				eval += dangerousEnemy/distance;
			}
		}
		return eval;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		target = new Enemy(e, this);

		if (enemies.get(target.getName()) == null) {
			enemies.put(target.getName(), target);
		}

		//Point2D enemyPos = calcPoint(actualPosition, e.getDistance(), getHeadingRadians() + e.getBearingRadians());

		if (getOthers() == 0)
			setTurnRadarLeftRadians(getRadarTurnRemainingRadians());

		if (getGunHeat() == 0) {
			fire(3.0);
		}

	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		enemies.remove(event.getName());
	}

	private static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
		return new Point2D.Double(p.x + dist*Math.sin(ang), p.y + dist*Math.cos(ang));
	}

	private static double calcAngle(Point2D.Double p2,Point2D.Double p1){
		return Math.atan2(p2.x - p1.x, p2.y - p1.y);
	}

	@Override
	public void onPaint(Graphics2D g) {
		//g.drawLine((int)target.getX(), (int)target.getY(), (int)getX(), (int)getY());
		g.fillRect((int) nextPosition.getX(),(int) nextPosition.getY(), 10, 10);
	}
}
