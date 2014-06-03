package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;


import org.pattern.movement.Move;
import org.pattern.movement.Projection;
import org.pattern.movement.WaveSurfer;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.pattern.radar.Radar;
import org.pattern.utils.Utils;
import org.pattern.utils.VisitCountStorage;



import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;


public class OneAOneMovement extends AdvancedRobot implements Observer {

	int ahead = 1;
	double randomAngle;

	List<Shape> toDraw = new LinkedList<>();

	Radar radar;
	WaveSurfer waves;
	
	VisitCountStorage riskStorage, gfStorage;

	double _targetingStorage[];
	public int NUM_BINS = 43;
	List<GBulletFiredEvent> firedBullets;

	double maxDistance;

	public OneAOneMovement() {
		radar = new Radar(this);
		radar.addObserver(this);
		waves = new WaveSurfer(this);

		riskStorage = new VisitCountStorage();
		gfStorage = new VisitCountStorage();

		firedBullets = new LinkedList<>();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof GBulletFiredEvent) {
			GBulletFiredEvent wave = (GBulletFiredEvent) arg1;
			setWaveMAE(wave, getHeading(), getVelocity());
			waves.addWave(wave);

		}
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

		double firingOffset = firingOffset(hittedWave.getFiringPosition(),
				hittedWave.getTargetPosition(), bulletPosition);
		double mae = firingOffset > 0 ? hittedWave.getMaxMAE() : hittedWave
				.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset / mae : -firingOffset / mae;

		riskStorage.visit(gf);
		waves.getWaves().remove(hittedWave);
		return;

	};

	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		GBulletFiredEvent wave = waves.getNearestWave();
		Point2D myPos = new Point2D.Double(getX(), getY());

		// TODO we lost a wave
		if (Math.abs(myPos.distance(wave.getFiringPosition())
				- (getTime() - wave.getFiringTime()) * wave.getVelocity()) > 50)
			return;

		double firingOffset = firingOffset(wave.getFiringPosition(),
				wave.getTargetPosition(), myPos);
		double gf = firingOffset > 0 ? firingOffset / wave.getMaxMAE()
				: -firingOffset / wave.getMinMAE();

		riskStorage.visit(gf);

	}

	private double getMAE(Point2D firingPosition, Point2D targetPosition,
			double targetHeading, double targetVelocity, double waveVelocity,
			int cw) {

		double angle = org.pattern.utils.Utils.absBearingPerpendicular(
				targetPosition, firingPosition, cw);
		Move m = new Move(this);
		m.move(angle, targetHeading);
		// TODO use values 2 ticks before detecting the wave
		Projection projection = new Projection(targetPosition, targetHeading,
				targetVelocity, m.ahead, m.turnRight);

		tickProjection tick = projection.projectNextTick();
		double tempMae = cw == -1 ? Double.MAX_VALUE : Double.MIN_VALUE;

		while (tick.getPosition().distance(firingPosition) > tick.getTick()
				* waveVelocity) {
			tick = projection.projectNextTick();

			if (cw == -1) {
				tempMae = Math.min(
						tempMae,
						firingOffset(firingPosition, targetPosition,
								tick.getPosition()));
			} else {
				tempMae = Math.max(
						tempMae,
						firingOffset(firingPosition, targetPosition,
								tick.getPosition()));
			}

			if (m.smooth(tick.getPosition(), tick.getHeading(), projection.getWantedHeading(), m.ahead)) {
					projection.setWantedDirection(m.ahead);
					projection.setWantedHeading(tick.getHeading() + m.turnRight);
			}
		}

		return tempMae;
	}

	private void setWaveMAE(GBulletFiredEvent wave, double heading,
			double velocity) {

		double mae[] = new double[2];
		for (int orbitDirection = -1; orbitDirection < 2; orbitDirection += 2) {

			mae[orbitDirection == -1 ? 0 : 1] = getMAE(
					wave.getFiringPosition(), wave.getTargetPosition(),
					heading, velocity, wave.getVelocity(), orbitDirection);
		}
		wave.setMinMAE(Math.min(mae[0], mae[1]));
		wave.setMaxMAE(Math.max(mae[0], mae[1]));
		return;
	}

	@Override
	public void run() {
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		int cw = 1;
		Random r = new Random();
		maxDistance = Math.sqrt(getBattleFieldWidth() * getBattleFieldWidth()
				+ getBattleFieldHeight() * getBattleFieldHeight());

		while (true) {
			radar.doScan();
			updateFiredBullets();

			double _ahead = 0;
			double _turnRight = 0;

			waves.removePassedWaves();
			Move m = new Move(this);
			GBulletFiredEvent nearestWave = waves.getNearestWave();
			double angle;
			if (nearestWave == null && radar.getLockedEnemy() != null) {
				Enemy e = radar.getLockedEnemy();
				angle = org.pattern.utils.Utils
						.absBearingPerpendicular(new Point2D.Double(getX(),
								getY()), e.getPosition(), cw);
				m.move(angle, getHeading());
			}
			else if (nearestWave != null) {
				Point2D enemyPosition = radar.getLockedEnemy() == null ? nearestWave
						.getFiringRobot().getPosition() : radar
						.getLockedEnemy().getPosition();
				double minRisk = Double.MAX_VALUE;
				for (int orbitDirection = -1; orbitDirection < 2; orbitDirection += 2) {
					angle = org.pattern.utils.Utils.absBearingPerpendicular(
							new Point2D.Double(getX(), getY()), enemyPosition,
							orbitDirection);
					
					m.move(angle, getHeading());
					
					double risk = surfWave(nearestWave, m.turnRight, m.ahead);
					if (risk < minRisk) {
						minRisk = risk;
						cw = orbitDirection;
					}
				}
			}

			

			Projection proj = new Projection(
					new Point2D.Double(getX(), getY()), getHeading(),
					getVelocity(), m.ahead, getTurnRemaining() + m.turnRight);
			tickProjection t = proj.projectNextTick();
			
			m.smooth(t.getPosition(), t.getHeading(), proj.getWantedHeading(), m.ahead);
			
			ahead = m.ahead;
			_turnRight = m.turnRight;
			_ahead = ahead * 100;
			
			setAhead(_ahead);
			setTurnRight(_turnRight);
			execute();
		}
	}

	private void updateFiredBullets() {
		GBulletFiredEvent hittedBullet;
		Enemy e = radar.getLockedEnemy();
		List<GBulletFiredEvent> toRemove = new LinkedList<>();

		if (e == null)
			return;

		for (GBulletFiredEvent bullet : firedBullets) {
			double distanceFromTarget = bullet.getFiringPosition().distance(
					e.getPosition());
			double distanceTravelled = (getTime() - bullet.getFiringTime())
					* bullet.getVelocity();

			if (distanceFromTarget - distanceTravelled < -50) {
				toRemove.add(bullet);
				continue;
			}

			if (Math.abs(distanceFromTarget - distanceTravelled) < 30) {
				double firingOffset = firingOffset(bullet.getFiringPosition(),
						bullet.getTargetPosition(), e.getPosition());
				double _mae = firingOffset > 0 ? bullet.getMaxMAE() : bullet
						.getMinMAE();
				double gf = firingOffset > 0 ? firingOffset / _mae
						: -firingOffset / _mae;

				gfStorage.visit(gf);
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
			firedBullets.remove(b);
		}

	}


	private double surfWave(GBulletFiredEvent nearestWave,
			double bearingOffset, int direction) {
		Point2D myPosition = new Point2D.Double(getX(), getY());
		Point2D enemyPosition = radar.getLockedEnemy() == null ? nearestWave
				.getFiringRobot().getPosition() : radar.getLockedEnemy()
				.getPosition();

		Projection projection = new Projection(myPosition, getHeading(),
				getVelocity(), direction, bearingOffset);

		tickProjection tick = projection.projectNextTick();
		int timeElapsed = (int) (getTime() - nearestWave.getFiringTime());
		Move m = new Move(this);
		
		while (tick.getPosition().distance(nearestWave.getFiringPosition()) > (timeElapsed + tick
				.getTick()) * nearestWave.getVelocity()) {
			tick = projection.projectNextTick();
			
			if (m.smooth(tick.getPosition(), tick.getHeading(), projection.getWantedHeading(), direction)) {
				projection.setWantedDirection(m.ahead);
				projection.setWantedHeading(tick.getHeading() + m.turnRight);
			}
		}


		double firingOffset = firingOffset(nearestWave.getFiringPosition(),
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
				toDraw.add(rect);
			}
		}

		return riskStorage.getVisits(gf);
	}

	private double firingOffset(Point2D firingPosition, Point2D targetPosition,
			Point2D hitPosition) {
		double firingBearing = robocode.util.Utils
				.normalAbsoluteAngleDegrees(Utils.absBearing(firingPosition,
						hitPosition));
		double bearing = robocode.util.Utils.normalAbsoluteAngleDegrees(Utils
				.absBearing(firingPosition, targetPosition));

		double ret;
		if (firingBearing > bearing)
			ret = firingBearing - bearing;
		else
			ret = -(bearing - firingBearing);

		return robocode.util.Utils.normalRelativeAngleDegrees(ret);
	}


	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		radar.consumeScannedRobotEvent(event);

		Enemy e = new Enemy(event, this);

		double distance = event.getDistance();

		double firePower = 0;
		Point2D myPosition = new Point2D.Double(getX(), getY());

		if (getEnergy() < 30)
			firePower = 1.;
		else
			firePower = 3 - (distance / maxDistance) * 3;

		double bestGF = gfStorage.getPeak();
		double mae = 0;
		int cw = 0;
		if (bestGF > 0) {
			cw = 1;
		} else {
			cw = -1;
		}

		mae = getMAE(myPosition, e.getPosition(), e.getHeading(),
				e.getVelocity(), 20 - firePower * 3, cw);

		double angle = cw * bestGF * mae;
		double absBearing = Utils.absBearing(myPosition, e.getPosition());
		angle += absBearing;
		setTurnGunRight(robocode.util.Utils.normalRelativeAngleDegrees(angle
				- getGunHeading()));

		if (getGunHeat() == 0) {
			GBulletFiredEvent bullet = new GBulletFiredEvent();

			bullet.setEnergy(firePower);
			bullet.setFiringPosition(myPosition); // TODO take the next tick
													// position?
			bullet.setFiringTime(getTime() + 1);
			bullet.setTargetPosition(e.getPosition());
			bullet.setVelocity(20 - firePower * 3);
			setWaveMAE(bullet, e.getHeading(), e.getVelocity());

			firedBullets.add(bullet);

			setFire(firePower);
		}
	}

//	private double getAimingBearing(ScannedRobotEvent event, double firePower) {
//		double angle;
//		Enemy enemy = new Enemy(event, this);
//
//		Projection proj = new Projection(enemy.getPosition(),
//				enemy.getHeading(), enemy.getVelocity(),
//				(int) Math.signum(enemy.getVelocity()), 0);
//
//		tickProjection tick = proj.projectNextTick();
//		while (tick.getPosition().distance(getX(), getY()) > tick.getTick()
//				* (20 - 3 * firePower)) {
//			tick = proj.projectNextTick();
//		}
//
//		angle = Utils.absBearing(new Point2D.Double(getX(), getY()),
//				tick.getPosition());
//
//		return angle;
//	}

	@Override
	public void onHitRobot(HitRobotEvent event) {
		radar.consumeHitAnotherRobotEvent(event);
	}

	@Override
	public void onPaint(Graphics2D g) {

		int STICK_LENGTH = 140;
		int MINIMUM_RADIUS = 114;
		super.onPaint(g);
		boolean paintWS = true;
		
		if (paintWS) {		
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
			
			double heading = ahead == 1 ? getHeading() : getHeading() + 180;
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
		Color c = g.getColor();
		g.setColor(Color.RED);

		for (GBulletFiredEvent wave : waves.getWaves()) {
			drawWaveAndMae(wave, g);
		}

		g.setColor(Color.GREEN);
		for (GBulletFiredEvent wave : firedBullets) {
			drawWaveAndMae(wave, g);
		}
		
		g.setColor(c);

		for (Shape s : toDraw) {
			g.draw(s);
		}

		toDraw.clear();
	}
	
	private void drawPoint(Point2D point, int size, Graphics2D g) {
		g.fillRect((int)(point.getX()-size/2), (int)(point.getY()-size/2), size, size);
	}
	private void drawWaveAndMae(GBulletFiredEvent wave, Graphics2D g) {
		double maeLength = 300;
		double radius = wave.getVelocity()
				* (getTime() - wave.getFiringTime());
		g.drawArc((int) (wave.getFiringPosition().getX() - radius),
				(int) (wave.getFiringPosition().getY() - radius),
				(int) radius * 2, (int) radius * 2, 0, 360);
		g.drawRect((int) wave.getFiringPosition().getX() - 5, (int) wave
				.getFiringPosition().getY() - 5, 10, 10);

		double absBearing = Utils.absBearing(wave.getFiringPosition(),
				wave.getTargetPosition());

		drawPoint(wave.getFiringPosition(), 4, g);
		drawPoint(wave.getTargetPosition(), 4, g);
		// //draw MAE
		g.drawLine((int) wave.getFiringPosition().getX(), (int) wave
				.getFiringPosition().getY(), (int) (wave
				.getTargetPosition().getX()), (int) (wave
				.getTargetPosition().getY()));

		g.drawLine(
				(int) wave.getFiringPosition().getX(),
				(int) wave.getFiringPosition().getY(),
				(int) (wave.getFiringPosition().getX() + Math.sin(Math
						.toRadians(absBearing + wave.getMaxMAE()))
						* maeLength),
				(int) (wave.getFiringPosition().getY() + Math.cos(Math
						.toRadians(absBearing + wave.getMaxMAE()))
						* maeLength));

		g.drawLine(
				(int) wave.getFiringPosition().getX(),
				(int) wave.getFiringPosition().getY(),
				(int) (wave.getFiringPosition().getX() + Math.sin(Math
						.toRadians(absBearing + wave.getMinMAE()))
						* maeLength),
				(int) (wave.getFiringPosition().getY() + Math.cos(Math
						.toRadians(absBearing + wave.getMinMAE()))
						* maeLength));
	}
}
