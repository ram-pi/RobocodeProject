/*
 * Scanning testing. Try to does not turn the radar in useless direction 
 */

package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Hashtable;

import org.pattern.utils.PositionFinder;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class ScannerRobot extends AdvancedRobot {

	private Enemy en;
	private Double STICK_LENGTH = 140.0;
	private Point2D nextRadarPoint;
	private Point2D nextPosition;
	private Point2D lastPosition;
	private Boolean radarGoingRight;
	private Boolean corner;
	private Hashtable<String, Enemy> enemies;

	@Override
	public void run() {
		setAdjustRadarForRobotTurn(true);


		en = new Enemy();
		enemies = new Hashtable<String, Enemy>();
		radarGoingRight = true;
		corner = false;
		setTurnRadarRight(360);

		do {
			doScan();
			doMovement();
			doShooting();
			execute();
		} while(true);
	}

	private void doShooting() {
		/* Perform head on target for gun movement */
//		double turnGunAmt = (getHeadingRadians() + en.getBearingRadians() - getGunHeadingRadians());
//		if (getGunTurnRemaining() < 10) {
//			setTurnGunRightRadians(turnGunAmt);
//		}
//		
		if (getGunHeat() == 0) {
			double firePower = Math.min(500 / en.getDistance(), 3);
			fire(firePower);
		}
	}

	private void doMovement() {
		Point2D actualPosition = new Point2D.Double(getX(), getY());
		if (corner) {
			goToCorner();
		} else if (nextPosition == null || nextPosition.equals(actualPosition) || nextPosition.distance(actualPosition) < 15){
			try {
				PositionFinder f = new PositionFinder(enemies, this);
				//nextPosition = f.generateRandomPoint();
				nextPosition = f.findBestPointInRangeWithRandomOffset(300);
				goToPoint(nextPosition);
			} catch (Exception e) {
				System.out.println(e);
			}
			
		} else {
			goToPoint(nextPosition);
		}
		lastPosition = actualPosition;
	}

	private void goToPoint(Point2D nextPosition) {
		Point2D actualPosition = new Point2D.Double(getX(), getY());
		Double distanceToNewPosition = actualPosition.distance(nextPosition);
		Double angle = org.pattern.utils.Utils.calcAngle(nextPosition, actualPosition) - getHeadingRadians();
		Double direction = 1.0;
		if (Math.cos(angle) < 0) {
			angle += Math.PI;
			direction = -1.0;
		}
		setAhead(distanceToNewPosition*direction);
		angle = Utils.normalRelativeAngle(angle);
		setTurnRightRadians(angle);
	}

	private void goToCorner() {
		nextPosition = new Point2D.Double(getBattleFieldWidth()-40, getBattleFieldHeight()-40);
		goToPoint(nextPosition);
	}

	private void doScan() {
		Point2D pos = new Point2D.Double(getX(), getY());
		Double nextRightHeading = getRadarHeading()+45;
		Double nextLeftHeading = getRadarHeading()-45;
		Point2D nextRadarPointRight = org.pattern.utils.Utils.calcPoint(pos, STICK_LENGTH, Math.toRadians(nextRightHeading));
		Point2D nextRadarPointLeft = org.pattern.utils.Utils.calcPoint(pos, STICK_LENGTH, Math.toRadians(nextLeftHeading));
		Point2D actualRadarPoint = org.pattern.utils.Utils.calcPoint(pos, STICK_LENGTH, Math.toRadians(getRadarHeading()));
		if (radarGoingRight && !org.pattern.utils.Utils.pointInBattlefield(this, actualRadarPoint) && !org.pattern.utils.Utils.pointInBattlefield(this, nextRadarPointRight)) {
			System.out.println("Safe rotation of 120 degrees.");
			setTurnRadarRight(120);
		}
		else if (!radarGoingRight && !org.pattern.utils.Utils.pointInBattlefield(this, actualRadarPoint) && !org.pattern.utils.Utils.pointInBattlefield(this, nextRadarPointLeft)) {
			System.out.println("Safe rotation of 120 degrees.");
			setTurnRadarLeft(120);
		}
		else if (radarGoingRight) {
			setTurnRadarRight(45);
			nextRadarPoint = nextRadarPointRight;
			if (!org.pattern.utils.Utils.pointInBattlefield(this, nextRadarPointRight)) {
				radarGoingRight = false;
			}
		} else if (!radarGoingRight) {
			setTurnRadarLeft(45);
			nextRadarPoint = nextRadarPointLeft;
			if (!org.pattern.utils.Utils.pointInBattlefield(this, nextRadarPointLeft)) {
				radarGoingRight = true;
			}
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		en = new Enemy(event, this);
		enemies.put(en.getName(), en);

		if (getGunHeat() < 1.0) {
			System.out.println("Prepare to target the enemy...");
			Double radarTurn = getHeading() - getRadarHeading() + en.getBearing();
			setTurnRadarRight(Utils.normalRelativeAngleDegrees(radarTurn));
		} else {
			doScan();
		}
		
		doShooting();
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.red);
		g.drawLine((int)getX(), (int)getY(), (int)nextRadarPoint.getX(), (int)nextRadarPoint.getY());
		g.fillRect((int) nextPosition.getX(), (int) nextPosition.getY(), 10, 10);
	}
}
