package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import javax.xml.crypto.Data;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class TheTester extends AdvancedRobot {

	private java.util.Hashtable<String, Enemy> enemies;
	private Enemy target;
	private Point2D.Double lastPosition;
	private Point2D.Double nextPosition;
	private Point2D.Double actualPosition;
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

		/* Perform head on target for gun movement */
		double turnGunAmt = (getHeadingRadians() + target.getBearingRadians() - getGunHeadingRadians());
		if (getGunTurnRemaining() < 10) {
			setTurnGunRightRadians(turnGunAmt);
		}

		/* Movement Settings */
		double distanceToNewPosition = actualPosition.distance(nextPosition);
		if (distanceToNewPosition < 8) {
			// Searching a new destination
			Rectangle2D.Double battlefield = new Rectangle2D.Double(30, 30, getBattleFieldWidth() -60, getBattleFieldHeight() -60);
			Point2D.Double testPoint;

			// Generate random point and evaluate the risk going in that point
			int i = 0;
			while (i < 200) {
				i++;
				Random r = new Random(new Date().getTime());
				double randomX = r.nextDouble()*getBattleFieldWidth()*distanceToTarget;
				double randomY = r.nextDouble()*getBattleFieldHeight()*distanceToTarget;
				testPoint = new Point2D.Double(randomX%getBattleFieldWidth(), randomY%getBattleFieldHeight());
				if (battlefield.contains(testPoint) &&
						(evaluatePosition(testPoint) < evaluatePosition(nextPosition))) {
					nextPosition = testPoint;
				}
			}
			lastPosition = actualPosition;	
		} else {
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
				double distance = actualPosition.distance(tmp.getPosition2().getX(), tmp.getPosition2().getY());
				eval += 1/Math.pow(distance, 2);
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

		Point2D enemyPos = calcPoint(actualPosition, e.getDistance(), getHeadingRadians() + e.getBearingRadians());

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
		g.drawLine((int)target.getX(), (int)target.getY(), (int)getX(), (int)getY());
		g.fillRect((int) nextPosition.getX(),(int) nextPosition.getY(), 10, 10);
	}
}
