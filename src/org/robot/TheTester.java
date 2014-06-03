package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pattern.utils.PositionFinder;
import org.pattern.movement.Projection;
import org.pattern.movement.WallSmoothing;
import org.pattern.movement.Projection.tickProjection;
import java.util.Hashtable;
import java.util.List;

import org.pattern.utils.PositionFinder;
import org.pattern.utils.Wave;

import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class TheTester extends AdvancedRobot {

	private java.util.Hashtable<String, Enemy> enemies;
	private Hashtable<String, Integer> hitEnemyTable;
	private Enemy target;
	private Point2D.Double lastPosition;
	private Point2D.Double nextPosition;
	private Point2D.Double actualPosition;
	private double oldDistance;
	private double energy;
	private int ahead;
	private WallSmoothing wallSmoothing;
	private List<Wave> enemyWaves;

	@Override
	public void run() {
		setColors(Color.black, Color.black, Color.black);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		enemies = new java.util.Hashtable<String, Enemy>();
		hitEnemyTable = new Hashtable<String, Integer>();

		lastPosition = nextPosition = actualPosition = new Point2D.Double(getX(), getY());

		target = new Enemy();
		
		ahead=1;
		
		wallSmoothing=new WallSmoothing(this);

		do {

			setTurnRadarRight(360);
			doScan();
			lastPosition = actualPosition;
			actualPosition = new Point2D.Double(getX(), getY());
			execute();
		} while(true);

	}

	private void doScan() {
		if (getRadarTurnRemaining() != 0)
			setTurnRadarRight(45);
		else
			setTurnRadarRight(getRadarTurnRemaining());
	}


	private void doMovementAndGun() {

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
			if (distanceToNewPosition < 140 ) {
				PositionFinder p = new PositionFinder(enemies, this);
				// Point2D.Double testPoint = p.findBestPoint(200);
				Point2D.Double testPoint = p.findBestPointInRange(200, 100.0);
				nextPosition = testPoint;
				lastPosition = actualPosition;
			}
		}
		else if (distanceToNewPosition < 15) {
			PositionFinder p = new PositionFinder(enemies, this);
			int attempt = 200;
			//Point2D.Double testPoint = p.findBestPoint(200);
			//double range = distanceToTarget*0.5;
			//Point2D.Double testPoint = p.findBestPointInRange(attempt, range);
			Point2D.Double testPoint =  p.findBestPointInRangeWithRandomOffset(attempt);
			nextPosition = testPoint;
			lastPosition = actualPosition;
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

	@Override
	public void onScannedRobot(ScannedRobotEvent e) {
		target = new Enemy(e, this);

		Double radarTurn = getHeading() - getRadarHeading() + target.getBearing();
		setTurnRadarRight(Utils.normalRelativeAngleDegrees(radarTurn));

		enemies.put(target.getName(), target);
		
		doMovementAndGun();
		
		if (getGunHeat() == 0) {
			double firePower = Math.min(500 / target.getDistance(), 3);
			fire(firePower);
		}

	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		enemies.remove(event.getName());
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		System.out.println(event.getName() + "Hit me");
		if(hitEnemyTable.contains(event.getName())) {
			int times = hitEnemyTable.get(event.getName());
			times++;
			hitEnemyTable.put(event.getName(), times);
		} else 
			hitEnemyTable.put(event.getName(), 1);

		int max = -1;
		Enemy toTarget = new Enemy();
		for (String key : hitEnemyTable.keySet()) {
			int tmp = hitEnemyTable.get(key);
			if (tmp > max && enemies.contains(key)) {
				max = tmp;
				toTarget = enemies.get(key);
			}
		}

		// Target the robot that shot to us most times
		if (target != null) {
			double turnGunAmt = (getHeadingRadians() + toTarget.getBearingRadians() - getGunHeadingRadians());
			setTurnGunRightRadians(turnGunAmt);
			target = toTarget;
		}
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
