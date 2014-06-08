package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.pattern.movement.Move;
import org.pattern.movement.Projection;
import org.pattern.movement.WallSmoothing;
import org.pattern.movement.WaveSurfer;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.pattern.radar.Radar;
import org.pattern.utils.Costants;
import org.pattern.utils.EnemyInfo;
import org.pattern.utils.PositionFinder;
import org.pattern.utils.VisitCountStorage;
import org.pattern.utils.VisitCountStorageSegmented;

import com.sun.org.apache.bcel.internal.Constants;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import robocode.util.Utils;

public class Rocky extends AdvancedRobot implements Observer{

	private Boolean m_radarGoingRight;
	private Point2D m_nextPosition;
	private Point2D m_lastPosition;
	private boolean m_corner;
	private Enemy m_en;
	public WaveSurfer m_waves;
	private boolean m_wallSmoothing;
	public Map<String, VisitCountStorage> m_storages;

	private Hashtable<String, Enemy> m_enemies;
	private Move m_move;

	// on paint meele
	private Point2D m_nextRadarPoint;


	private Radar o_radar;
	private WaveSurfer o_waves;
	List<GBulletFiredEvent> o_firedBullets;
	private static VisitCountStorageSegmented o_riskStorage;
	private static VisitCountStorageSegmented o_gfStorage;

	Point2D o_toGo = null;
	boolean o_pointsSurfing = false;
	boolean o_orbitSurfing = true;

	// on paint ovo
	public List<Shape> o_toDraw;
	private int o_ahead;
	private double o_maxDistance;
	private PositionFinder positionFinder;

	public Rocky() {
		//meele
		m_en = new Enemy();
		m_enemies = new Hashtable<String, Enemy>();

		m_radarGoingRight = true;
		m_corner = false;

		m_waves = new WaveSurfer(this);
		m_storages = new HashMap<String, VisitCountStorage>();

		//OvO
		o_radar = new Radar(this);
		o_radar.addObserver(this);
		o_waves = new WaveSurfer(this);
		o_firedBullets = new LinkedList<>();
		if (o_gfStorage == null)
			o_gfStorage = new VisitCountStorageSegmented();
		if (o_riskStorage == null)
			o_riskStorage = new VisitCountStorageSegmented();

		o_toDraw = new LinkedList<>();

		positionFinder = new PositionFinder();
	}
	@Override
	public void run() {
		boolean meele = getOthers() > 1;
		setColors(Color.green, Color.yellow, Color.red);
		setBulletColor(Color.red);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);

		o_maxDistance = Math.sqrt(getBattleFieldWidth() * getBattleFieldWidth()
				+ getBattleFieldHeight() * getBattleFieldHeight());

		if (meele) {
			m_move = new Move(this);
		} 


		do {
			meele = getOthers() > 1;
			if (meele) {
				m_waves.removePassedWaves();
				doMeeleScan();
				doMeeleMovement();
				doMeeleShooting();
			} else {
				o_waves.removePassedWaves();
				o_updateFiredBullets();
				doOvOscan();
				doOvoMovement();
				doOvoShotting();
			}
			execute();

		} while (true);

	}

	private void doOvoShotting() {


	}
	private void doOvoMovement() {
		double _ahead = 0;
		double _turnRight = 0;

		Point2D myPosition = new Point2D.Double(getX(), getY());
		GBulletFiredEvent nearestWave = o_waves.getNearestWave();
		double angle = 0;
		Move m = new Move(this);
		if (nearestWave == null && o_radar.getLockedEnemy() != null) {
			Enemy e = o_radar.getLockedEnemy();
			angle = org.pattern.utils.Utils
					.absBearingPerpendicular(new Point2D.Double(getX(),
							getY()), e.getPosition(), 1);
			m.move(angle, getHeading());
			Projection proj = new Projection(
					new Point2D.Double(getX(), getY()), getHeading(),
					getVelocity(), m.ahead, getTurnRemaining() + m.turnRight);
			tickProjection t = proj.projectNextTick();
			m.smooth(t.getPosition(), t.getHeading(), proj.getWantedHeading(), m.ahead);
		} else if (o_toGo == null && nearestWave != null && o_pointsSurfing ) {
			o_toGo = o_pointsSurfing(nearestWave);
		} else if (nearestWave != null && o_orbitSurfing) {
			Enemy e = o_radar.getLockedEnemy() == null ? nearestWave.getFiringRobot() : o_radar.getLockedEnemy();
			angle  = o_orbitSurfing(nearestWave, e);
			m.move(angle, getHeading());
			m.smooth(myPosition, getHeading(), m.turnRight, m.ahead);

		}

		if (o_toGo != null && o_pointsSurfing) {
			double togoAngle = org.pattern.utils.Utils.absBearing(new Point2D.Double(getX(), getY()), o_toGo);
			m.move(togoAngle, getHeading());
			Projection proj = new Projection(
					new Point2D.Double(getX(), getY()), getHeading(),
					getVelocity(), m.ahead, getTurnRemaining() + m.turnRight);
			tickProjection t = proj.projectNextTick();
			//			if (m.smooth(t.getPosition(), t.getHeading(), proj.getWantedHeading(), m.ahead)) 
			//				toGo = null;
			m.smooth(t.getPosition(), t.getHeading(), proj.getWantedHeading(), m.ahead);
		}

		boolean drawTogo = false;
		if (drawTogo && o_toGo != null) {
			Rectangle2D rect = new Rectangle2D.Double(o_toGo.getX()-2, o_toGo.getY()-2, 4, 4);
			o_toDraw.add(rect);
		}

		//		m.move(surfAngle, getHeading());
		//		Projection proj = new Projection(
		//				new Point2D.Double(getX(), getY()), getHeading(),
		//				getVelocity(), m.ahead, getTurnRemaining() + m.turnRight);
		//		tickProjection t = proj.projectNextTick();
		//
		//		m.smooth(t.getPosition(), t.getHeading(), proj.getWantedHeading(),
		//				m.ahead);
		//
		o_ahead = m.ahead;
		_turnRight = m.turnRight;
		_ahead = o_ahead * 100;

		setAhead(_ahead);
		setTurnRight(_turnRight);

	}
	private void doOvOscan() {
		o_radar.doScan();

	}
	private void doMeeleScan() {

		Point2D pos = new Point2D.Double(getX(), getY());
		Double nextRightHeading = getRadarHeading() + 45;
		Double nextLeftHeading = getRadarHeading() - 45;
		Point2D nextRadarPointRight = org.pattern.utils.Utils.calcPoint(pos,
				Costants.RADAR_STICK_LENGTH, Math.toRadians(nextRightHeading));
		Point2D nextRadarPointLeft = org.pattern.utils.Utils.calcPoint(pos,
				Costants.RADAR_STICK_LENGTH, Math.toRadians(nextLeftHeading));
		Point2D actualRadarPoint = org.pattern.utils.Utils.calcPoint(pos,
				Costants.RADAR_STICK_LENGTH, Math.toRadians(getRadarHeading()));

		if (m_radarGoingRight
				&& !org.pattern.utils.Utils.pointInBattlefield(this,
						actualRadarPoint)
						&& !org.pattern.utils.Utils.pointInBattlefield(this,
								nextRadarPointRight)) {
			System.out.println("Safe rotation of 120 degrees.");
			setTurnRadarRight(120);
		} else if (!m_radarGoingRight
				&& !org.pattern.utils.Utils.pointInBattlefield(this,
						actualRadarPoint)
						&& !org.pattern.utils.Utils.pointInBattlefield(this,
								nextRadarPointLeft)) {
			System.out.println("Safe rotation of 120 degrees.");
			setTurnRadarLeft(120);
		} else if (m_radarGoingRight) {
			setTurnRadarRight(45);
			m_nextRadarPoint = nextRadarPointRight;
			if (!org.pattern.utils.Utils.pointInBattlefield(this,
					nextRadarPointRight)) {
				m_radarGoingRight = false;
			}
		} else if (!m_radarGoingRight) {
			setTurnRadarLeft(45);
			m_nextRadarPoint = nextRadarPointLeft;
			if (!org.pattern.utils.Utils.pointInBattlefield(this,
					nextRadarPointLeft)) {
				m_radarGoingRight = true;
			}
		}

		/* 1v1 scan ? */
		// else {
		// if (getRadarTurnRemaining() == 0 && (info == null || (getTime() >
		// info.getLastTimeSaw()+2)))
		// setTurnRadarRight(360);
		//
		// if (getTime() == info.getLastTimeSaw()+1) {
		// setTurnRadarRight(16);
		// } else if(getTime() == info.getLastTimeSaw()+2) {
		// setTurnRadarLeft(32);
		// }
		// }
	}

	private void doMeeleMovement() {
		Point2D actualPosition = new Point2D.Double(getX(), getY());

		if(m_nextPosition==null)
			m_nextPosition=actualPosition;

		if (m_corner) {
			m_goToCorner();
			out.println("corner");
		} else if (m_nextPosition == null || m_nextPosition.equals(actualPosition) || m_nextPosition.distance(actualPosition) < 15){
			try {
				//PositionFinder f = new PositionFinder(enemies, this);
				//m_nextPosition = f.generateRandomPoint();
				//m_nextPosition = f.findBestPointInRangeWithRandomOffset(500);
				m_gotoPointandSmooth();
			} catch (Exception e) {
				System.out.println(e);
			}

		} else {
			m_gotoPointandSmooth();
		}
		m_lastPosition = actualPosition;
	}

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		boolean meele = getOthers() > 1;
		if (meele) {
			GBulletFiredEvent wave = m_waves.getNearestWave();
			Point2D myPos = new Point2D.Double(getX(), getY());

			// TODO we lost a wave
			if (wave == null
					|| Math.abs(myPos.distance(wave.getFiringPosition())
							- (getTime() - wave.getFiringTime())
							* wave.getVelocity()) > Costants.SURFING_MAX_DISTANE_HITTED_WAVE)
				return;

			double firingOffset = org.pattern.utils.Utils.firingOffset(
					wave.getFiringPosition(), wave.getTargetPosition(), myPos);
			double gf = firingOffset > 0 ? firingOffset / wave.getMaxMAE()
					: -firingOffset / wave.getMinMAE();
			if (m_storages.get(event.getName()) != null)
				m_storages.get(event.getName()).visit(gf);
		} else {
			GBulletFiredEvent wave = o_waves.getNearestWave();
			Point2D myPos = new Point2D.Double(getX(), getY());

			// TODO we lost a wave
			if (Math.abs(myPos.distance(wave.getFiringPosition())
					- (getTime() - wave.getFiringTime()) * wave.getVelocity()) > 50)
				return;

			double firingOffset = o_firingOffset(wave.getFiringPosition(),
					wave.getTargetPosition(), myPos);
			double gf = firingOffset > 0 ? firingOffset / wave.getMaxMAE()
					: -firingOffset / wave.getMinMAE();


			o_riskStorage.visit(wave.getSnapshot(),gf);
		}

	}

	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		boolean meele = getOthers() > 1;
		if (meele) {
			Point2D bulletPosition = new Point2D.Double(event.getBullet()
					.getX(), event.getBullet().getY());

			GBulletFiredEvent hittedWave = null;
			for (GBulletFiredEvent wave : m_waves.getWaves()) {
				if (Math.abs(bulletPosition.distance(wave.getFiringPosition())
						- ((getTime() - wave.getFiringTime()) * event
								.getBullet().getVelocity())) < 20) {
					hittedWave = wave;
					break;
				}
			}

			if (hittedWave == null)
				return;

			double firingOffset = org.pattern.utils.Utils.firingOffset(
					hittedWave.getFiringPosition(),
					hittedWave.getTargetPosition(), bulletPosition);
			double mae = firingOffset > 0 ? hittedWave.getMaxMAE() : hittedWave
					.getMinMAE();
			double gf = firingOffset > 0 ? firingOffset / mae : -firingOffset
					/ mae;

			m_storages.get(event.getBullet().getName()).visit(gf);
			m_waves.getWaves().remove(hittedWave);
			return;
		} else {
			Point2D bulletPosition = new Point2D.Double(event.getBullet().getX(),
					event.getBullet().getY());

			GBulletFiredEvent hittedWave = null;
			for (GBulletFiredEvent wave : o_waves.getWaves()) {
				if (Math.abs(bulletPosition.distance(wave.getFiringPosition())
						- ((getTime() - wave.getFiringTime()) * event.getBullet()
								.getVelocity())) < Costants.SURFING_BULLET_HIT_BULLET_DISTANCE) {
					hittedWave = wave;
					break;
				}
			}

			if (hittedWave == null)
				return;

			double firingOffset = o_firingOffset(hittedWave.getFiringPosition(),
					hittedWave.getTargetPosition(), bulletPosition);
			double mae = firingOffset > 0 ? hittedWave.getMaxMAE() : hittedWave
					.getMinMAE();
			double gf = firingOffset > 0 ? firingOffset / mae : -firingOffset / mae;


			o_riskStorage.visit(hittedWave.getSnapshot(), gf);

			o_waves.getWaves().remove(hittedWave);
			return;
		}
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {

		boolean meele = getOthers() > 1;
		if (meele) {
			Enemy enemy = m_enemies.get(event.getName());

			if (enemy == null) {	
				enemy = new Enemy(event, this);	
				m_enemies.put(enemy.getName(), enemy);
				m_storages.put(enemy.getName(), new VisitCountStorage());
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
				m_waves.addWave(gBulletFiredEvent);

			}


			enemy.updateEnemy(event, this);
			m_enemies.put(enemy.getName(), enemy);

			//doShooting(); 
		} else {
			o_radar.consumeScannedRobotEvent(event);

			Enemy e = new Enemy(event, this);


			double distance = event.getDistance();

			double firePower = 0;
			Point2D myPosition = new Point2D.Double(getX(), getY());

			if (getEnergy() < Costants.FIREPOWER_ENERGY_TRESHOLD)
				firePower = Costants.FIREPOWER_FP_UNDER_TRESHOLD;
			else
				firePower = 3 - (distance / o_maxDistance) * 3;

			BitSet snapshot = org.pattern.utils.Utils.getSnapshot(this, e);
			double angle = org.pattern.utils.Utils.getFiringAngle(o_gfStorage, myPosition, e, firePower, snapshot, this);

			//In case of no segmentation
			//			               double bestGF = gfStorage.getPeak();
			//			               double mae = 0;
			//			               int cw = 0;
			//			               if (bestGF > 0) {
			//			                       cw = 1;
			//			               } else {
			//			                       cw = -1;
			//		               }
			//			
			//			               mae = Math.abs(getMAE(myPosition, e.getPosition(), e.getHeading(),
			//			                               e.getVelocity(), 20 - firePower * 3, cw));
			//			
			//			              double angle = bestGF * mae;

			double absBearing = org.pattern.utils.Utils.absBearing(myPosition, e.getPosition());
			double bearing = absBearing + angle;
			setTurnGunRight(robocode.util.Utils.normalRelativeAngleDegrees(bearing
					- getGunHeading()));

			if (getGunHeat() == 0 && Math.abs(getGunTurnRemaining()) < Costants.GUN_MAX_DISPLACEMENT_DEGREE) {
				GBulletFiredEvent bullet = new GBulletFiredEvent();

				bullet.setEnergy(firePower);
				bullet.setFiringPosition(myPosition); // TODO take the next tick
				// position?
				bullet.setFiringTime(getTime());
				bullet.setTargetPosition(e.getPosition());
				bullet.setVelocity(20 - firePower * 3);
				bullet.setSnapshot(snapshot);
				o_setWaveMAE(bullet, e.getHeading(), e.getVelocity());

				o_firedBullets.add(bullet);

				setFire(firePower);
			}
		}
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		boolean meele = getOthers() > 1;
		if (meele) {
			String key = event.getName();
			m_enemies.remove(key);
		}
	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		boolean meele = getOthers() > 1;
		if (meele)
			return;

		o_radar.consumeHitAnotherRobotEvent(event);
	}


	private void doMeeleShooting() {
		//PositionFinder p = new PositionFinder(m_enemies, this);
		positionFinder.setEnemies(m_enemies);
		positionFinder.setRobot(this);
		m_en = positionFinder.findNearest();
		if (m_en == null)
			return;

		/* Perform head on target for gun movement */
		double turnGunAmt = (getHeadingRadians() + m_en.getBearingRadians() - getGunHeadingRadians());
		turnGunAmt = Utils.normalRelativeAngle(turnGunAmt);
		setTurnGunRightRadians(turnGunAmt);

		if (getGunHeat() == 0) {
			double firePower = 3 - (m_en.getDistance() / o_maxDistance) * 3;
			fire(firePower);
		}
	}

	private void m_goToCorner() {
		m_nextPosition = new Point2D.Double(getBattleFieldWidth() - 40,
				getBattleFieldHeight() - 40);
		m_goToPoint(m_nextPosition);
	}

	private void m_goToPoint(Point2D nextPosition) {
		Move move = new Move(this);
		Point2D actualPosition = new Point2D.Double(getX(), getY());
		Double distanceToNewPosition = actualPosition.distance(nextPosition);
		Double angle = org.pattern.utils.Utils.absBearing(actualPosition,
				nextPosition);

		move.move(angle, getHeading());

		setAhead(distanceToNewPosition * move.ahead);
		setTurnRight(move.turnRight);

	}

	private void m_gotoPointandSmooth() {

		Point2D actualPosition = new Point2D.Double(getX(), getY());
		Move m = new Move(this);

		if (m_nextPosition != null) {
			if(m_nextPosition.distance(actualPosition) < org.pattern.utils.Costants.POINT_MIN_DIST_NEXT_POINT){

				//PositionFinder p = new PositionFinder(m_enemies, this);
				positionFinder.setEnemies(m_enemies);
				positionFinder.setRobot(this);
				m_nextPosition = positionFinder.findBestPointInRangeWithRandomOffset(200);

				//debug 
				for (GBulletFiredEvent wave : m_waves.getWaves()) {
					positionFinder.riskFromWaveDebug(wave, m_nextPosition);
				}

			}
			double absBearing = org.pattern.utils.Utils.absBearing(actualPosition, m_nextPosition);
			m.move(absBearing, getHeading());

			setTurnRight(m.turnRight);
			if (getTurnRemaining() > 0.001) {
				setAhead(0);
			} else {
				setMaxVelocity(8.);
				setAhead(m_nextPosition.distance(actualPosition)*m.ahead);
			}


			return;
		}

		//		Projection proj = new Projection(new Point2D.Double(getX(), getY()),
		//				getHeading(), getVelocity(), m_move.ahead, getTurnRemaining()+m_move.turnRight);
		//		tickProjection t = proj.projectNextTick();
		//
		//		/* Movement Settings, find the next position */
		//		double distanceToNewPosition = actualPosition.distance(m_nextPosition);
		//		if (m_move.smooth(t.getPosition(), t.getHeading(), proj.getWantedHeading(), m_move.ahead)) {
		//			//out.println("smooth");
		//			m_wallSmoothing=true;
		//			double _turnRight=m_move.turnRight;
		//			int _ahead=100*m_move.ahead;
		//
		//			setAhead(_ahead);
		//			setTurnRight(_turnRight);
		//		}
		//		else if (distanceToNewPosition < 15 || m_wallSmoothing==true) {
		//			m_wallSmoothing=false;
		//			PositionFinder p = new PositionFinder(m_enemies, this);
		//			//Point2D.Double testPoint = p.findBestPoint(200);
		//			//double range = distanceToTarget*0.5;
		//			//Point2D.Double testPoint = p.findBestPointInRange(attempt, range);
		//			Point2D.Double testPoint =  p.findBestPointInRangeWithRandomOffset(200);
		//			m_nextPosition = testPoint;
		//			//out.println("point");
		//		}
		//
		//		/* Movement to nextPosition */
		//		else {
		//			Double angle = org.pattern.utils.Utils.calcAngle(m_nextPosition, actualPosition) - getHeadingRadians();
		//			Double direction = 1.0;
		//
		//			if (Math.cos(angle) < 0) {
		//				angle += Math.PI;
		//				direction = -1.0;
		//			}
		//			if(direction>0)
		//				m_move.ahead = 1;
		//			else
		//				m_move.ahead=-1;
		//			setAhead(distanceToNewPosition*direction);
		//			angle = Utils.normalRelativeAngle(angle);
		//			setTurnRightRadians(angle);
		//		}
	}

	private double o_orbitSurfing(GBulletFiredEvent wave, Enemy e) {
		Move m = new Move(this);
		Point2D myPos = new Point2D.Double(getX(), getY());
		double angle, ret = 0;
		double minRisk = Double.MAX_VALUE;

		for (int orbitDirection = -1; orbitDirection < 2; orbitDirection += 2) { 
			angle = org.pattern.utils.Utils.absBearingPerpendicular(myPos, e.getPosition(), orbitDirection);
			m.move(angle, getHeading());
			double risk = o_surfWave(wave, m.turnRight, m.ahead);
			if (risk < minRisk) {
				minRisk = risk;
				ret = angle;
			}
		}
		return ret;

	}

	private double o_surfWave(GBulletFiredEvent nearestWave,
			double bearingOffset, int direction) {
		Point2D myPosition = new Point2D.Double(getX(), getY());

		Projection projection = new Projection(myPosition, getHeading(),
				getVelocity(), direction, bearingOffset);

		tickProjection tick = projection.projectNextTick();
		int timeElapsed = (int) (getTime() - nearestWave.getFiringTime());
		Move m = new Move(this);

		while (tick.getPosition().distance(nearestWave.getFiringPosition()) > (timeElapsed + tick
				.getTick()) * nearestWave.getVelocity()) {
			tick = projection.projectNextTick();

			if (m.smooth(tick.getPosition(), tick.getHeading(),
					projection.getWantedHeading(), direction)) {
				projection.setWantedDirection(m.ahead);
				projection.setWantedHeading(tick.getHeading() + m.turnRight);
			}
		}

		double firingOffset = o_firingOffset(nearestWave.getFiringPosition(),
				nearestWave.getTargetPosition(), tick.getPosition());

		double _mae = firingOffset > 0 ? nearestWave.getMaxMAE() : nearestWave
				.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset / _mae : -firingOffset
				/ _mae;

		boolean drawSurf = false;
		if (drawSurf) {
			for (tickProjection t : projection.getProjections()) {
				Rectangle2D rect = new Rectangle2D.Double(t.getPosition()
						.getX() - 2, t.getPosition().getY() - 2, 4, 4);
				o_toDraw.add(rect);
			}
		}

		double risk = org.pattern.utils.Utils.getDanger(gf, Math.abs(_mae), o_riskStorage, nearestWave);
		return risk;
	}

	private Point2D o_pointsSurfing(GBulletFiredEvent wave) {
		Point2D toGo = null;
		Point2D enemyPosition = o_radar.getLockedEnemy() == null ? wave
				.getFiringRobot().getPosition() : o_radar
				.getLockedEnemy().getPosition();
				Enemy e = o_radar.getLockedEnemy() == null ? wave
						.getFiringRobot() : o_radar.getLockedEnemy();
						double minRisk = Double.MAX_VALUE;

						for (Point2D p : org.pattern.utils.Utils.generatePoints(this, e)) {
							if (p.distance(enemyPosition) < Costants.POINT_MIN_DIST_ENEMY) 
								continue;

							double gf = org.pattern.utils.Utils.getProjectedGF(this, wave, p);
							double mae = gf > 0 ? wave.getMaxMAE() : wave.getMinMAE();
							double risk = org.pattern.utils.Utils.getDanger(gf, Math.abs(mae), o_riskStorage, wave);


							if (risk < minRisk) {
								minRisk = risk;
								toGo = p;
							}

						}
						out.println("surfing at gf "+org.pattern.utils.Utils.getProjectedGF(this, wave, toGo));
						return toGo;
	}

	private void o_updateFiredBullets() {
		Enemy e = o_radar.getLockedEnemy();
		List<GBulletFiredEvent> toRemove = new LinkedList<>();

		if (e == null)
			return;

		for (GBulletFiredEvent bullet : o_firedBullets) {
			double distanceFromTarget = bullet.getFiringPosition().distance(
					e.getPosition());
			double distanceTravelled = (getTime() - bullet.getFiringTime())
					* bullet.getVelocity();

			if (distanceFromTarget - distanceTravelled < Costants.GF_DIST_REMOVE_BULLET) {
				toRemove.add(bullet);
				continue;
			}

			if (Math.abs(distanceFromTarget - distanceTravelled) < Costants.GF_DIST_BULLET_HIT) {
				double firingOffset = o_firingOffset(bullet.getFiringPosition(),
						bullet.getTargetPosition(), e.getPosition());
				double _mae = firingOffset > 0 ? bullet.getMaxMAE() : bullet
						.getMinMAE();
				double gf = firingOffset > 0 ? firingOffset / _mae
						: -firingOffset / _mae;

				//gfStorage.decay(1.1);
				o_gfStorage.visit(bullet.getSnapshot(), gf);

				toRemove.add(bullet);

				// Rectangle2D _fpos = new
				// Rectangle2D.Double(bullet.getFiringPosition().getX()-6,
				// bullet.getFiringPosition().getY()-6, 12, 12);
				// toDraw.add(_fpos);
				//
				// Rectangle2D _tpos = new
				// Rectangle2D.Double(bullet.getTargetPosition().getX()-6,
				// bullet.getTargetPosition().getY()-6, 12, 12);
				// toDraw.add(_tpos);

			}
		}

		for (GBulletFiredEvent b : toRemove) {
			o_firedBullets.remove(b);
		}

	}

	private double o_firingOffset(Point2D firingPosition, Point2D targetPosition,
			Point2D hitPosition) {
		double firingBearing = robocode.util.Utils
				.normalAbsoluteAngleDegrees(org.pattern.utils.Utils.absBearing(firingPosition,
						hitPosition));
		double bearing = robocode.util.Utils.normalAbsoluteAngleDegrees(org.pattern.utils.Utils
				.absBearing(firingPosition, targetPosition));

		double ret;
		if (firingBearing > bearing)
			ret = firingBearing - bearing;
		else
			ret = -(bearing - firingBearing);

		return robocode.util.Utils.normalRelativeAngleDegrees(ret);
	}

	private void o_setWaveMAE(GBulletFiredEvent wave, double heading,
			double velocity) {

		double mae[] = new double[2];
		for (int orbitDirection = -1; orbitDirection < 2; orbitDirection += 2) {

			mae[orbitDirection == -1 ? 0 : 1] = org.pattern.utils.Utils.getMAE(
					wave.getFiringPosition(), wave.getTargetPosition(),
					heading, velocity, wave.getVelocity(), orbitDirection, this);
		}
		wave.setMinMAE(Math.min(mae[0], mae[1]));
		wave.setMaxMAE(Math.max(mae[0], mae[1]));
		return;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof GBulletFiredEvent) {
			GBulletFiredEvent wave = (GBulletFiredEvent) arg1;
			o_setWaveMAE(wave, getHeading(), getVelocity());
			o_waves.addWave(wave);

		}
	}

	@Override
	public void onPaint(Graphics2D g) {
		boolean meele = getOthers() > 1;
		if (meele) {
			g.setColor(Color.red);
			if (m_nextRadarPoint != null)
				g.drawLine((int) getX(), (int) getY(),
						(int) m_nextRadarPoint.getX(),
						(int) m_nextRadarPoint.getY());

			if (m_nextPosition != null)
				g.fillRect((int) m_nextPosition.getX(),
						(int) m_nextPosition.getY(), 10, 10);

			double heading = getVelocity() > 0.0 ? getHeading()
					: getHeading() + 180;
			double endX = getX() + Math.sin(Math.toRadians(heading))
					* WallSmoothing.STICK_LENGTH;
			double endY = getY() + Math.cos(Math.toRadians(heading))
					* WallSmoothing.STICK_LENGTH;
			g.drawLine((int) getX(), (int) getY(), (int) endX, (int) endY);

			boolean drawWave = true;
			if (drawWave) {
				for (GBulletFiredEvent wave : m_waves.getWaves()) {
					drawWaveAndMae(wave, g);
				}
			}
		} else {
			boolean paintWS = true;
			boolean drawGF = true;
			boolean drawWave = true;

			int STICK_LENGTH = (int)Costants.STICK_LENGTH;
			int MINIMUM_RADIUS = (int)Costants.MINIMUM_RADIUS;
			Color c = g.getColor();

			//			drawVisitCountStorageSegmented(gfStorage, g, 20, 20);
			//			drawVisitCountStorageSegmented(riskStorage, g, 400, 20);

			if (paintWS) {
				g.setColor(Color.magenta);
				Rectangle2D safeBF = new Rectangle2D.Double(18, 18,
						getBattleFieldWidth() - 36, getBattleFieldHeight() - 36);
				g.draw(safeBF);

				Point2D center1 = new Point2D.Double(getX()
						+ Math.sin(Math.toRadians(getHeading() - 90))
						* MINIMUM_RADIUS, getY()
						+ Math.cos(Math.toRadians(getHeading() - 90))
						* MINIMUM_RADIUS);
				Point2D center2 = new Point2D.Double(getX()
						+ Math.sin(Math.toRadians(getHeading() + 90))
						* MINIMUM_RADIUS, getY()
						+ Math.cos(Math.toRadians(getHeading() + 90))
						* MINIMUM_RADIUS);

				drawPoint(center1, 10, g);
				drawPoint(center2, 10, g);

				double heading = o_ahead == 1 ? getHeading() : getHeading() + 180;
				g.drawLine((int) getX(), (int) getY(),
						(int) (getX() + Math.sin(Math.toRadians(heading))
								* STICK_LENGTH),
								(int) (getY() + Math.cos(Math.toRadians(heading))
										* STICK_LENGTH));

				g.drawArc((int) (center1.getX() - MINIMUM_RADIUS),
						(int) (center1.getY() - MINIMUM_RADIUS),
						MINIMUM_RADIUS * 2, MINIMUM_RADIUS * 2, 0, 360);
				g.drawArc((int) (center2.getX() - MINIMUM_RADIUS),
						(int) (center2.getY() - MINIMUM_RADIUS),
						MINIMUM_RADIUS * 2, MINIMUM_RADIUS * 2, 0, 360);

			}
			c = g.getColor();
			g.setColor(Color.RED);

			if (drawWave) {
				for (GBulletFiredEvent wave : o_waves.getWaves()) {
					drawWaveAndMae(wave, g);
				}
			}

			g.setColor(Color.GREEN);
			if (drawGF) {
				for (GBulletFiredEvent wave : o_firedBullets) {
					drawWaveAndMae(wave, g);
				}
			}



		}

		Color c = g.getColor();
		g.setColor(Color.BLUE);
		for (Shape s : o_toDraw) {
			g.draw(s);
		}
		g.setColor(c);

		o_toDraw.clear();


	}

	private void drawWaveAndMae(GBulletFiredEvent wave, Graphics2D g) {
		double maeLength = 300;
		double radius = wave.getVelocity() * (getTime() - wave.getFiringTime());
		g.drawArc((int) (wave.getFiringPosition().getX() - radius), (int) (wave
				.getFiringPosition().getY() - radius), (int) radius * 2,
				(int) radius * 2, 0, 360);
		g.drawRect((int) wave.getFiringPosition().getX() - 5, (int) wave
				.getFiringPosition().getY() - 5, 10, 10);

		double absBearing = org.pattern.utils.Utils.absBearing(
				wave.getFiringPosition(), wave.getTargetPosition());

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

	}

	@Override
	public void onWin(WinEvent event) {
		setColors(Color.black, Color.black, Color.black);
	}

}
