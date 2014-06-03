package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pattern.movement.PositionFinder;
import org.pattern.movement.Projection;
import org.pattern.movement.WallSmoothing;
import org.pattern.movement.Projection.tickProjection;

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
	private int ahead;
	private WallSmoothing wallSmoothing;

	@Override
	public void run() {
		setColors(Color.white, Color.black, Color.black);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		enemies = new java.util.Hashtable<String, Enemy>();

		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

		lastPosition = nextPosition = actualPosition = new Point2D.Double(getX(), getY());

		target = new Enemy();
		
		ahead=1;
		
		wallSmoothing=new WallSmoothing(this);

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

		Projection proj = new Projection(new Point2D.Double(getX(), getY()),
				getHeading(), getVelocity(), ahead, getTurnRemaining());
		tickProjection t = proj.projectNextTick();
		
		/* Movement Settings, find the next position */
		double distanceToNewPosition = actualPosition.distance(nextPosition);
		if (wallSmoothing.doSmoothing(ahead, t)) {
			if (distanceToNewPosition < 140 || forceSearching) {
				PositionFinder p = new PositionFinder(enemies, this);
				// Point2D.Double testPoint = p.findBestPoint(200);
				Point2D.Double testPoint = p.findBestPointInRange(200, 100.0);
				nextPosition = testPoint;
				lastPosition = actualPosition;
				forceSearching = false;
			}
		}
		else if (distanceToNewPosition < 15 || forceSearching) {
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
			ahead = (int) direction;
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
		
		g.setColor(Color.BLUE);
		//g.drawLine((int)target.getX(), (int)target.getY(), (int)getX(), (int)getY());
		g.fillRect((int) nextPosition.getX(),(int) nextPosition.getY(), 10, 10);
		
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
		g.draw(safeBF);
		
		g.fillRect((int)wallSmoothing.getCenter1().getX()-5, (int)wallSmoothing.getCenter1().getY()-5, 10, 10);
		g.fillRect((int)wallSmoothing.getCenter2().getX()-5, (int)wallSmoothing.getCenter2().getY()-5, 10, 10);

		double heading=ahead == 1 ? getHeading() : getHeading()+180;
		double endX = getX()+Math.sin(Math.toRadians(heading))*WallSmoothing.STICK_LENGTH;
		double endY = getY()+Math.cos(Math.toRadians(heading))*WallSmoothing.STICK_LENGTH;
		g.drawLine((int) getX(), (int) getY(), (int)endX, (int)endY);

		g.drawArc((int)(wallSmoothing.getCenter1().getX()-WallSmoothing.MINIMUM_RADIUS), (int)(wallSmoothing.getCenter1().getY()-WallSmoothing.MINIMUM_RADIUS), WallSmoothing.MINIMUM_RADIUS*2, WallSmoothing.MINIMUM_RADIUS*2, 0, 360);
		g.drawArc((int)(wallSmoothing.getCenter2().getX()-WallSmoothing.MINIMUM_RADIUS), (int)(wallSmoothing.getCenter2().getY()-WallSmoothing.MINIMUM_RADIUS), WallSmoothing.MINIMUM_RADIUS*2, WallSmoothing.MINIMUM_RADIUS*2, 0, 360);
	
	}
}
