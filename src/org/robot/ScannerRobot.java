/*
 * Scanning testing. Try to does not turn the radar in useless direction
 */

package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Hashtable;

import org.pattern.movement.Move;
import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.movement.WallSmoothing;
import org.pattern.utils.EnemyInfo;
import org.pattern.utils.PositionFinder;

import robocode.AdvancedRobot;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class ScannerRobot extends AdvancedRobot {

	private Enemy en;
	private EnemyInfo info;
	private Double STICK_LENGTH = 140.0;
	private Point2D nextRadarPoint;
	private Point2D nextPosition;
	private Point2D lastPosition;
	private Boolean radarGoingRight;
	private Boolean corner;
	private Hashtable<String, Enemy> enemies;
	private Boolean meleeRadar;
	private Move move;
	private boolean wallSmoothing;

	@Override
	public void run() {
		setAdjustRadarForRobotTurn(true);


		en = new Enemy();
		enemies = new Hashtable<String, Enemy>();
		info = new EnemyInfo();
		radarGoingRight = true;
		corner = false;
		checkEnemies();
		
		move=new Move(this);
		move.ahead=1;
		wallSmoothing=false;
		
		nextPosition=null;

		setTurnRadarRight(360);

		do {
			checkEnemies();
			doScan();
			doMovement();
			doShooting();
			execute();
		} while(true);
	}

	private void doShooting() {
		PositionFinder p = new PositionFinder(enemies, this);
		en = p.findNearest();

		/* Perform head on target for gun movement */
		double turnGunAmt = (getHeadingRadians() + en.getBearingRadians() - getGunHeadingRadians());
		turnGunAmt = Utils.normalRelativeAngle(turnGunAmt);
		setTurnGunRightRadians(turnGunAmt);


		if (getGunHeat() == 0) {
			double firePower = 3.0;
			fire(firePower);
		}
	}

	private void doMovement() {
		Point2D actualPosition = new Point2D.Double(getX(), getY());
		
		if(nextPosition==null)
			nextPosition=actualPosition;
		
		if (corner) {
			goToCorner();
			out.println("corner");
		} else if (nextPosition == null || nextPosition.equals(actualPosition) || nextPosition.distance(actualPosition) < 15){
			try {
				//PositionFinder f = new PositionFinder(enemies, this);
				//nextPosition = f.generateRandomPoint();
				//nextPosition = f.findBestPointInRangeWithRandomOffset(500);
				gotoPointandSmooth();
			} catch (Exception e) {
				System.out.println(e);
			}

		} else {
			gotoPointandSmooth();
		}
		lastPosition = actualPosition;
	}

	private void gotoPointandSmooth(){
		
		Point2D actualPosition = new Point2D.Double(getX(), getY());
		
		Projection proj = new Projection(new Point2D.Double(getX(), getY()),
				getHeading(), getVelocity(), move.ahead, getTurnRemaining()+move.turnRight);
		tickProjection t = proj.projectNextTick();

		/* Movement Settings, find the next position */
		double distanceToNewPosition = actualPosition.distance(nextPosition);
		if (move.smooth(t.getPosition(), t.getHeading(), proj.getWantedHeading(), move.ahead)) {
			//out.println("smooth");
			wallSmoothing=true;
			double _turnRight=move.turnRight;
			int _ahead=100*move.ahead;

			setAhead(_ahead);
			setTurnRight(_turnRight);
		}
		else if (distanceToNewPosition < 15 || wallSmoothing==true) {
			wallSmoothing=false;
			PositionFinder p = new PositionFinder(enemies, this);
			//Point2D.Double testPoint = p.findBestPoint(200);
			//double range = distanceToTarget*0.5;
			//Point2D.Double testPoint = p.findBestPointInRange(attempt, range);
			Point2D.Double testPoint =  p.findBestPointInRangeWithRandomOffset(200);
			nextPosition = testPoint;
			//out.println("point");
		}

		/* Movement to nextPosition */
		else {
			Double angle = org.pattern.utils.Utils.calcAngle(nextPosition, actualPosition) - getHeadingRadians();
			Double direction = 1.0;

			if (Math.cos(angle) < 0) {
				angle += Math.PI;
				direction = -1.0;
			}
			if(direction>0)
				move.ahead = 1;
			else
				move.ahead=-1;
			setAhead(distanceToNewPosition*direction);
			angle = Utils.normalRelativeAngle(angle);
			setTurnRightRadians(angle);
		}
	}
	
	@Override
	public void onHitWall(HitWallEvent event) {
		
		out.println("wall");
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
		if(direction>0)
			move.ahead = 1;
		else
			move.ahead=-1;
		setAhead(distanceToNewPosition*direction);
		angle = Utils.normalRelativeAngle(angle);
		setTurnRightRadians(angle);
		
	}

	private void goToCorner() {
		nextPosition = new Point2D.Double(getBattleFieldWidth()-40, getBattleFieldHeight()-40);
		goToPoint(nextPosition);
	}

	private void doScan() {
		if (meleeRadar) {
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
		} else {
			if (getRadarTurnRemaining() == 0 && (info == null || (getTime() > info.getLastTimeSaw()+2)))
				setTurnRadarRight(360);

			if (getTime() == info.getLastTimeSaw()+1) {
				setTurnRadarRight(16);
			} else if(getTime() == info.getLastTimeSaw()+2) {
				setTurnRadarLeft(32);
			}
		}

	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		String key = event.getName();
		enemies.remove(key);
	}

	private void checkEnemies() {
		if (getOthers() > 1)
			meleeRadar = true;
		else
			meleeRadar = false;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		en = new Enemy(event, this);
		enemies.put(en.getName(), en);
		info = new EnemyInfo(en, getTime());

		if (!meleeRadar) {
			Double radarTurn = getHeading() - getRadarHeading() + en.getBearing();
			setTurnRadarRight(Utils.normalRelativeAngleDegrees(radarTurn));
		}

		doShooting();
	}

	@Override
	public void onPaint(Graphics2D g) {
		g.setColor(Color.red);
		if (nextRadarPoint != null)
			g.drawLine((int)getX(), (int)getY(), (int)nextRadarPoint.getX(), (int)nextRadarPoint.getY());
		g.fillRect((int) nextPosition.getX(), (int) nextPosition.getY(), 10, 10);
		
		double heading=move.ahead == 1 ? getHeading() : getHeading()+180;
		double endX = getX()+Math.sin(Math.toRadians(heading))*WallSmoothing.STICK_LENGTH;
		double endY = getY()+Math.cos(Math.toRadians(heading))*WallSmoothing.STICK_LENGTH;
		g.drawLine((int) getX(), (int) getY(), (int)endX, (int)endY);
	}
}