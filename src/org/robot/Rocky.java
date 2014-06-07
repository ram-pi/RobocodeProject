package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.pattern.movement.Move;
import org.pattern.movement.Projection;
import org.pattern.movement.WallSmoothing;
import org.pattern.movement.WaveSurfer;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.pattern.utils.Costants;
import org.pattern.utils.EnemyInfo;
import org.pattern.utils.PositionFinder;
import org.pattern.utils.VisitCountStorage;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

public class Rocky extends AdvancedRobot {

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

	public Rocky() {
		//meele
		m_en = new Enemy();
		m_enemies = new Hashtable<String, Enemy>();

		m_radarGoingRight = true;
		m_corner = false;

		m_waves = new WaveSurfer(this);
		m_storages = new HashMap<String, VisitCountStorage>();
	}
	@Override
	public void run() {
		boolean meele = getOthers() > 1;
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForRobotTurn(true);
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
			}
			execute();

		} while (true);

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

			m_storages.get(event.getName()).visit(gf);
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
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		
		
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
	}

	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		boolean meele = getOthers() > 1;
		if (meele) {
			String key = event.getName();
			m_enemies.remove(key);
		}
	}

	private void doMeeleShooting() {
		PositionFinder p = new PositionFinder(m_enemies, this);
		m_en = p.findNearest();

		/* Perform head on target for gun movement */
		double turnGunAmt = (getHeadingRadians() + m_en.getBearingRadians() - getGunHeadingRadians());
		turnGunAmt = Utils.normalRelativeAngle(turnGunAmt);
		setTurnGunRightRadians(turnGunAmt);

		if (getGunHeat() == 0) {
			double firePower = 3.0;
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
		
		Projection proj = new Projection(new Point2D.Double(getX(), getY()),
				getHeading(), getVelocity(), m_move.ahead, getTurnRemaining()+m_move.turnRight);
		tickProjection t = proj.projectNextTick();

		/* Movement Settings, find the next position */
		double distanceToNewPosition = actualPosition.distance(m_nextPosition);
		if (m_move.smooth(t.getPosition(), t.getHeading(), proj.getWantedHeading(), m_move.ahead)) {
			//out.println("smooth");
			m_wallSmoothing=true;
			double _turnRight=m_move.turnRight;
			int _ahead=100*m_move.ahead;

			setAhead(_ahead);
			setTurnRight(_turnRight);
		}
		else if (distanceToNewPosition < 15 || m_wallSmoothing==true) {
			m_wallSmoothing=false;
			PositionFinder p = new PositionFinder(m_enemies, this);
			//Point2D.Double testPoint = p.findBestPoint(200);
			//double range = distanceToTarget*0.5;
			//Point2D.Double testPoint = p.findBestPointInRange(attempt, range);
			Point2D.Double testPoint =  p.findBestPointInRangeWithRandomOffset(200);
			m_nextPosition = testPoint;
			//out.println("point");
		}

		/* Movement to nextPosition */
		else {
			Double angle = org.pattern.utils.Utils.calcAngle(m_nextPosition, actualPosition) - getHeadingRadians();
			Double direction = 1.0;

			if (Math.cos(angle) < 0) {
				angle += Math.PI;
				direction = -1.0;
			}
			if(direction>0)
				m_move.ahead = 1;
			else
				m_move.ahead=-1;
			setAhead(distanceToNewPosition*direction);
			angle = Utils.normalRelativeAngle(angle);
			setTurnRightRadians(angle);
		}
	}

	@Override
	public void onPaint(Graphics2D g) {
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
}
