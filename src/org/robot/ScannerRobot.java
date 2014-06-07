/*
 * Scanning testing. Try to does not turn the radar in useless direction
 */

package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Map;



import org.pattern.movement.Move;
import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.movement.WallSmoothing;

import org.pattern.movement.WaveSurfer;
import org.pattern.radar.GBulletFiredEvent;
import org.pattern.radar.UpdatedEnemiesListEvent;
import org.pattern.utils.Costants;


import org.pattern.movement.WaveSurfer;
import org.pattern.radar.GBulletFiredEvent;
import org.pattern.radar.UpdatedEnemiesListEvent;
import org.pattern.utils.Costants;
import org.pattern.utils.EnemyInfo;
import org.pattern.utils.PositionFinder;
import org.pattern.utils.VisitCountStorage;




import robocode.AdvancedRobot;


import robocode.HitWallEvent;

import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;

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
	public Map<String, VisitCountStorage> storages;
	private Boolean meleeRadar;

	public WaveSurfer waves;

	private Move move;
	private boolean wallSmoothing;
	private Boolean HoT;
	private Point2D aimingPoint;



	@Override
	public void run() {
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);

		en = new Enemy();
		enemies = new Hashtable<String, Enemy>();
		info = new EnemyInfo();
		radarGoingRight = true;
		corner = false;
		HoT = true;
		checkEnemies();
		waves = new WaveSurfer(this);
		storages = new HashMap<String, VisitCountStorage>();

		move=new Move(this);
		move.ahead=1;
		wallSmoothing=false;


		nextPosition=null;

		setTurnRadarRight(360);

		do {
			waves.removePassedWaves();
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
		if (en == null)
			return;
		
		Point2D myPos = new Point2D.Double(getX(), getY());

		if (HoT) {
			/* Perform head on target for gun movement */
			aimingPoint = new Point2D.Double(en.getX(), en.getY());
			double turnGunAmt = (getHeadingRadians() + en.getBearingRadians() - getGunHeadingRadians());
			turnGunAmt = Utils.normalRelativeAngle(turnGunAmt);
			setTurnGunRightRadians(turnGunAmt);
		} else {
			/* Perform circular targeting */
			Rectangle2D battlefield = new Rectangle2D.Double(0, 0, getBattleFieldWidth(), getBattleFieldHeight());
			long when = calcTimeToReachEnemy();
			aimingPoint = org.pattern.utils.Utils.getFuturePoint(en, when);
			if (!battlefield.contains(aimingPoint)) {
				HoT = true;
				return;
			}
			double theta = Utils.normalAbsoluteAngle(Math.atan2(aimingPoint.getX() - getX(), aimingPoint.getY() - getY()));
			setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
		}

		if (getGunHeat() == 0) {
			double firePower = 3.0;
			fire(firePower);
		}
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		GBulletFiredEvent wave = waves.getNearestWave();
		Point2D myPos = new Point2D.Double(getX(), getY());


		// TODO we lost a wave
		if (wave == null || Math.abs(myPos.distance(wave.getFiringPosition())
				- (getTime() - wave.getFiringTime()) * wave.getVelocity()) > Costants.SURFING_MAX_DISTANE_HITTED_WAVE)
			return;

		double firingOffset = org.pattern.utils.Utils.firingOffset(wave.getFiringPosition(),
				wave.getTargetPosition(), myPos);
		double gf = firingOffset > 0 ? firingOffset / wave.getMaxMAE()
				: -firingOffset / wave.getMinMAE();

		storages.get(event.getName()).visit(gf);

	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		Point2D bulletPosition = new Point2D.Double(event.getBullet().getX(),
				event.getBullet().getY());

		GBulletFiredEvent hittedWave = null;
		for (GBulletFiredEvent wave : waves.getWaves()) {
			if (Math.abs(bulletPosition.distance(wave.getFiringPosition())
					- ((getTime() - wave.getFiringTime()) * event.getBullet()
							.getVelocity())) < 20) {
				hittedWave = wave;
				break;
			}
		}

		if (hittedWave == null)
			return;

		double firingOffset = org.pattern.utils.Utils.firingOffset(hittedWave.getFiringPosition(),
				hittedWave.getTargetPosition(), bulletPosition);
		double mae = firingOffset > 0 ? hittedWave.getMaxMAE() : hittedWave
				.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset / mae : -firingOffset / mae;

		storages.get(event.getBullet().getName()).visit(gf);
		waves.getWaves().remove(hittedWave);
		return;

	}



	private long calcTimeToReachEnemy() {
		double firepower = 3.0;
		double bulletSpeed = 20 - firepower*3;
		return (long) (en.getDistance()/bulletSpeed);
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

		if (getOthers() <= 4)
			HoT = false;
		else 
			HoT = true;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {

		info = new EnemyInfo(en, getTime());
		Enemy enemy = enemies.get(event.getName());

		if (enemy == null) {	
			enemy = new Enemy(event, this);	
			enemies.put(enemy.getName(), enemy);
			storages.put(enemy.getName(), new VisitCountStorage());
		}

		if (getTime() - enemy.getLastUpdated() < Costants.TIME_THRESHOLD && 
				(enemy.getEnergy() - event.getEnergy()) > 0. &&
				(enemy.getEnergy() - event.getEnergy()) < 3.1) {

			GBulletFiredEvent gBulletFiredEvent = new GBulletFiredEvent();
			gBulletFiredEvent.setFiringRobot(enemy);
			gBulletFiredEvent.setEnergy(enemy.getEnergy() - event.getEnergy());
			gBulletFiredEvent.setVelocity(20 - 3 * (enemy.getEnergy() - event.getEnergy()));
			gBulletFiredEvent.setFiringTime(getTime()-1);
			gBulletFiredEvent.setFiringPosition(enemy.getPosition());//TODO this or the updated one?
			gBulletFiredEvent.setTargetPosition(new Point2D.Double(getX(), getY()));
			org.pattern.utils.Utils.setWaveMAE(gBulletFiredEvent, getHeading(), getVelocity(), this);
			waves.addWave(gBulletFiredEvent);

		}


		enemy.updateEnemy(event, this);
		enemies.put(enemy.getName(), enemy);

		if (!meleeRadar) {
			Double radarTurn = getHeading() - getRadarHeading() + enemy.getBearing();
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


		boolean drawWave = true;
		if (drawWave) {
			for (GBulletFiredEvent wave : waves.getWaves()) {
				drawWaveAndMae(wave, g);
			}
		}
	}

	private void drawWaveAndMae(GBulletFiredEvent wave, Graphics2D g) {
		double maeLength = 300;
		double radius = wave.getVelocity() * (getTime() - wave.getFiringTime());
		g.drawArc((int) (wave.getFiringPosition().getX() - radius), (int) (wave
				.getFiringPosition().getY() - radius), (int) radius * 2,
				(int) radius * 2, 0, 360);
		g.drawRect((int) wave.getFiringPosition().getX() - 5, (int) wave
				.getFiringPosition().getY() - 5, 10, 10);

		double absBearing = org.pattern.utils.Utils.absBearing(wave.getFiringPosition(),
				wave.getTargetPosition());

		drawPoint(wave.getFiringPosition(), 4, g);
		drawPoint(wave.getTargetPosition(), 4, g);
		// //draw MAE
		g.drawLine((int) wave.getFiringPosition().getX(), (int) wave
				.getFiringPosition().getY(), (int) (wave.getTargetPosition()
						.getX()), (int) (wave.getTargetPosition().getY()));

		g.drawLine(
				(int) wave.getFiringPosition().getX(),
				(int) wave.getFiringPosition().getY(),
				(int) (wave.getFiringPosition().getX() + Math.sin(Math
						.toRadians(absBearing + wave.getMaxMAE())) * maeLength),
						(int) (wave.getFiringPosition().getY() + Math.cos(Math
								.toRadians(absBearing + wave.getMaxMAE())) * maeLength));

		g.drawLine(
				(int) wave.getFiringPosition().getX(),
				(int) wave.getFiringPosition().getY(),
				(int) (wave.getFiringPosition().getX() + Math.sin(Math
						.toRadians(absBearing + wave.getMinMAE())) * maeLength),
						(int) (wave.getFiringPosition().getY() + Math.cos(Math
								.toRadians(absBearing + wave.getMinMAE())) * maeLength));
	}
	private void drawPoint(Point2D point, int size, Graphics2D g) {
		g.fillRect((int) (point.getX() - size / 2),
				(int) (point.getY() - size / 2), size, size);

		if (aimingPoint != null) {
			g.setColor(Color.white);
			g.fillRect((int) aimingPoint.getX(), (int) aimingPoint.getY(), 10, 10);
		}
	}
}
