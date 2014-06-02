package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import org.pattern.movement.MAE;
import org.pattern.movement.Projection;
import org.pattern.movement.WaveSurfer;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.pattern.radar.Radar;
import org.pattern.utils.Utils;

import com.sun.corba.se.impl.interceptors.PINoOpHandlerImpl;
import com.sun.xml.internal.messaging.saaj.packaging.mime.Header;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;
import sun.font.EAttribute;

public class OneAOneMovement extends AdvancedRobot implements Observer{

	int ahead = 1;
	double randomAngle;
	
	List<Shape> toDraw = new LinkedList<>();


	final int STICK_LENGTH = 140;
	final int MINIMUM_RADIUS = 114;
	
	
	long lastBulletFiredTime = Long.MAX_VALUE;
	boolean bulletJustFired = false;
	boolean followRandom = false;
	boolean fire = false;
	Radar radar;
	
	WaveSurfer waves;
	
	
	double maxDistance;

	public OneAOneMovement() {
		radar = new Radar(this);
		radar.addObserver(this);
		waves = new WaveSurfer(this);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof GBulletFiredEvent) {
			lastBulletFiredTime = getTime();
			bulletJustFired = true;
			GBulletFiredEvent wave = (GBulletFiredEvent)arg1;
			setWaveMAE(wave);
			waves.addWave(wave);
			
		}
	}
	
	@Override
	public void onBulletHitBullet(BulletHitBulletEvent event) {
		Point2D bulletPosition = new Point2D.Double(event.getBullet().getX(), event.getBullet().getY());
		
		GBulletFiredEvent hittedWave = null;
		for (GBulletFiredEvent wave : waves.getWaves()) {
			if (Math.abs(bulletPosition.distance(wave.getFiringPosition()) - ((getTime() - wave.getFiringTime()) * event.getBullet().getVelocity())) < 20) {
				hittedWave = wave;
				break;
			}
		}
		
		if (hittedWave == null)
			return;
		
		double firingOffset = firingOffset(hittedWave.getFiringPosition(), hittedWave.getTargetPosition(), bulletPosition);
		double mae = firingOffset > 0 ? hittedWave.getMaxMAE() : hittedWave.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset/mae : - firingOffset/mae;
		
		waves.hit(gf);
		waves.getWaves().remove(hittedWave);
		return;
		
	};
	
	@Override
	public void onHitByBullet(HitByBulletEvent event) {
		GBulletFiredEvent wave = waves.getNearestWave(); 		
		Point2D myPos = new Point2D.Double(getX(), getY());
		
		//TODO we lost a wave
		if (Math.abs(myPos.distance(wave.getFiringPosition()) - (getTime() - wave.getFiringTime()) * wave.getVelocity()) > 50)
			return;
		
		double firingOffset = firingOffset(wave.getFiringPosition(), wave.getTargetPosition(), myPos);
		double gf = firingOffset > 0 ? firingOffset / wave.getMaxMAE() : - firingOffset / wave.getMinMAE();
		
		waves.hit(gf);
		
		
	}
	
	
	private void setWaveMAE(GBulletFiredEvent wave) {
		
		int _ahead;
		double mae[] = new double[2];
		for (int orbitDirection = -1; orbitDirection < 2; orbitDirection+=2) {
			
			double angle = org.pattern.utils.Utils.absBearingPerpendicular(wave.getTargetPosition(), wave.getFiringPosition(), orbitDirection);
			
			_ahead = 1;				
			if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - getHeading())) > 90) {
				_ahead = -1;
				angle += 180;
			}

			angle = robocode.util.Utils.normalRelativeAngleDegrees(angle - getHeading());
			//TODO use values 2 ticks before detecting the wave
			Projection projection = new Projection(wave.getTargetPosition(), 
					getHeading(),
					getVelocity(),
					_ahead,
					angle);
			
			tickProjection tick = projection.projectNextTick();
			double tempMae = orbitDirection == -1 ?Double.MAX_VALUE : Double.MIN_VALUE;
			
			while(tick.getPosition().distance(wave.getFiringPosition()) > tick.getTick() * wave.getVelocity()) {
				tick = projection.projectNextTick();
				
				if (orbitDirection == -1) {
					tempMae = Math.min(tempMae, firingOffset(wave.getFiringPosition(), wave.getTargetPosition(), tick.getPosition()));
				} else {
					tempMae = Math.max(tempMae, firingOffset(wave.getFiringPosition(), wave.getTargetPosition(), tick.getPosition()));
				}
				
				
				if (stickCollide(tick.getPosition(), _ahead == 1 ? tick.getHeading() : tick.getHeading()+180)) {
					Point2D center1 = new Point2D.Double(tick.getPosition().getX() + Math.sin(Math.toRadians(tick.getHeading()-90))*MINIMUM_RADIUS,
							tick.getPosition().getY() + Math.cos(Math.toRadians(tick.getHeading()-90))*MINIMUM_RADIUS);
					Point2D center2 = new Point2D.Double(tick.getPosition().getX() + Math.sin(Math.toRadians(tick.getHeading()+90))*MINIMUM_RADIUS,
							tick.getPosition().getY() + Math.cos(Math.toRadians(tick.getHeading()+90))*MINIMUM_RADIUS);
					Boolean smoothC1 = canSmooth(center1);
					Boolean smoothC2 = canSmooth(center2);
					if (!smoothC1 && !smoothC2) {
						projection.setWantedDirection(_ahead * -1);
						
					} else if (smoothC1 && _ahead == 1 || smoothC2 && _ahead == -1) {
						if (projection.getWantedHeading() - tick.getHeading() > -4) {
							projection.setWantedDirection(_ahead);
							projection.setWantedHeading(tick.getHeading() -4);
						}
		
					} else if (smoothC2 && _ahead == 1 || smoothC1 && _ahead == -1) {
						if (projection.getWantedHeading() - tick.getHeading() < 4) {
							projection.setWantedDirection(_ahead);
							projection.setWantedHeading(tick.getHeading() + 4);
						}
					}
				}
			}
			
			mae[orbitDirection == -1 ? 0 : 1] = tempMae;
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
		maxDistance = Math.sqrt(getBattleFieldWidth()*getBattleFieldWidth()+getBattleFieldHeight()*getBattleFieldHeight());
		

		while(true) {
			radar.doScan();
			
			double _ahead = 0;
			double _turnRight = 0;
			
			waves.removePassedWaves();
			
			GBulletFiredEvent nearestWave = waves.getNearestWave();

			
			if (nearestWave != null) {
				Point2D enemyPosition = radar.getLockedEnemy() == null ? nearestWave.getFiringRobot().getPosition() : radar.getLockedEnemy().getPosition();
				double minRisk = Double.MAX_VALUE;
				for (int orbitDirection = -1; orbitDirection < 2; orbitDirection+=2) {
					double angle = 0;
					
					angle = org.pattern.utils.Utils.absBearingPerpendicular(new Point2D.Double(getX(), getY()), enemyPosition, orbitDirection);
					
					ahead = 1;				
					if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - getHeading())) > 90) {
						ahead = -1;
						angle += 180;
					}
	
					_turnRight = robocode.util.Utils.normalRelativeAngleDegrees(angle - getHeading());
					
					double risk = surfWave(nearestWave, _turnRight, ahead);
					if (risk < minRisk) {
						minRisk = risk;
						cw = orbitDirection;
					}
				}
			}
			
			if (getTime() - lastBulletFiredTime > 7) {
				followRandom = false;
			}
			
			
			if (!followRandom && radar.getLockedEnemy() != null) {
				Enemy e = radar.getLockedEnemy();
				double angle = org.pattern.utils.Utils.absBearingPerpendicular(new Point2D.Double(getX(), getY()), e.getPosition(), cw);
				
				ahead=1;
				if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - getHeading())) > 90) {
					ahead = -1;
					angle += 180;
				}

				_turnRight = robocode.util.Utils.normalRelativeAngleDegrees(angle - getHeading());
			}
			
			_ahead = ahead*100;
			Projection proj = new Projection(new Point2D.Double(getX(), getY()), getHeading(), getVelocity(), ahead, getTurnRemaining()+_turnRight);
			tickProjection t = proj.projectNextTick();
			if (stickCollide(t.getPosition(), ahead == 1 ? getHeading() : getHeading()+180)) {
				Point2D center1 = new Point2D.Double(getX() + Math.sin(Math.toRadians(getHeading()-90))*MINIMUM_RADIUS,
						getY() + Math.cos(Math.toRadians(getHeading()-90))*MINIMUM_RADIUS);
				Point2D center2 = new Point2D.Double(getX() + Math.sin(Math.toRadians(getHeading()+90))*MINIMUM_RADIUS,
						getY() + Math.cos(Math.toRadians(getHeading()+90))*MINIMUM_RADIUS);
				Boolean smoothC1 = canSmooth(center1);
				Boolean smoothC2 = canSmooth(center2);
				if (!smoothC1 && !smoothC2) {
					_ahead = ahead * -100;
					System.out.println("No way out");
				} else if (smoothC1 && ahead == 1 || smoothC2 && ahead == -1) {
					if (_turnRight > -4)
						_turnRight = -4;
	
				} else if (smoothC2 && ahead == 1 || smoothC1 && ahead == -1) {
					if (_turnRight < 4)
						_turnRight = 4;
				}
			}
			
			setAhead(_ahead);
			setTurnRight(_turnRight);
			execute();
		}
	}
	
	private double surfWave(GBulletFiredEvent nearestWave, double bearingOffset, int direction) {
		Point2D myPosition = new Point2D.Double(getX(), getY());
		Point2D enemyPosition = radar.getLockedEnemy() == null ? nearestWave.getFiringRobot().getPosition() : radar.getLockedEnemy().getPosition();
		
		Projection projection = new Projection(myPosition, 
				getHeading(), 
				getVelocity(), 
				direction, 
				bearingOffset);
		
		tickProjection tick = projection.projectNextTick();
		int timeElapsed = (int)(getTime() - nearestWave.getFiringTime());
		
		while(tick.getPosition().distance(nearestWave.getFiringPosition()) > (timeElapsed + tick.getTick()) * nearestWave.getVelocity()) {
			tick = projection.projectNextTick();
			
			if (stickCollide(tick.getPosition(), direction == 1 ? tick.getHeading() : tick.getHeading()+180)) {
				Point2D center1 = new Point2D.Double(tick.getPosition().getX() + Math.sin(Math.toRadians(tick.getHeading()-90))*MINIMUM_RADIUS,
						tick.getPosition().getY() + Math.cos(Math.toRadians(tick.getHeading()-90))*MINIMUM_RADIUS);
				Point2D center2 = new Point2D.Double(tick.getPosition().getX() + Math.sin(Math.toRadians(tick.getHeading()+90))*MINIMUM_RADIUS,
						tick.getPosition().getY() + Math.cos(Math.toRadians(tick.getHeading()+90))*MINIMUM_RADIUS);
				Boolean smoothC1 = canSmooth(center1);
				Boolean smoothC2 = canSmooth(center2);
				if (!smoothC1 && !smoothC2) {
					projection.setWantedDirection(direction * -1);
					
				} else if (smoothC1 && direction == 1 || smoothC2 && direction == -1) {
					if (projection.getWantedHeading() - tick.getHeading() > -4) {
						projection.setWantedDirection(direction);
						projection.setWantedHeading(tick.getHeading() -4);
					}
	
				} else if (smoothC2 && direction == 1 || smoothC1 && direction == -1) {
					if (projection.getWantedHeading() - tick.getHeading() < 4) {
						projection.setWantedDirection(direction);
						projection.setWantedHeading(tick.getHeading() + 4);
					}
				}
			}
		}
		
		for (tickProjection t : projection.getProjections()) {
			Rectangle2D rect = new Rectangle2D.Double(t.getPosition().getX()-2, t.getPosition().getY()-2, 4, 4);
			toDraw.add(rect);
		}
		
		double firingOffset = firingOffset(nearestWave.getFiringPosition(), nearestWave.getTargetPosition(), tick.getPosition());
		
		
		out.println("FiringOff: "+firingOffset);
		double _mae = firingOffset > 0 ? nearestWave.getMaxMAE() : nearestWave.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset/_mae : - firingOffset/_mae;
		out.println("MAE: " + _mae);
		out.println("GF: " + (firingOffset > 0 ? firingOffset/_mae : - firingOffset/_mae));
		out.println();
		
		Rectangle2D _fpos = new Rectangle2D.Double(nearestWave.getFiringPosition().getX()-6, nearestWave.getFiringPosition().getY()-6, 12, 12);
		toDraw.add(_fpos);

		Rectangle2D _tpos = new Rectangle2D.Double(nearestWave.getTargetPosition().getX()-6, nearestWave.getTargetPosition().getY()-6, 12, 12);
		toDraw.add(_tpos);
		
		Line2D line = new Line2D.Double(nearestWave.getFiringPosition().getX(), nearestWave.getFiringPosition().getY(), tick.getPosition().getX(), tick.getPosition().getY());
		toDraw.add(line);
		
		return waves.getDanger(gf);
	}

	private double firingOffset(Point2D firingPosition, Point2D targetPosition, Point2D hitPosition) {
		double firingBearing = robocode.util.Utils.normalAbsoluteAngleDegrees(Utils.absBearing(firingPosition, hitPosition));
		double bearing = robocode.util.Utils.normalAbsoluteAngleDegrees(Utils.absBearing(firingPosition, targetPosition));
		
		
		double ret;
		if (firingBearing > bearing)
			ret =  firingBearing - bearing;
		else
			ret = - (bearing - firingBearing);
		
		return robocode.util.Utils.normalRelativeAngleDegrees(ret);
	}

	private Boolean canSmooth (Point2D center) {
		Point2D up, down, left, right;
		up = new Point2D.Double(center.getX(), center.getY()+MINIMUM_RADIUS);
		down = new Point2D.Double(center.getX(), center.getY()-MINIMUM_RADIUS);
		left = new Point2D.Double(center.getX()-MINIMUM_RADIUS, center.getY());
		right = new Point2D.Double(center.getX()+MINIMUM_RADIUS, center.getY());
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
		if (!safeBF.contains(up) || !safeBF.contains(right) || !safeBF.contains(down) || !safeBF.contains(left)) {
			return false;
		}
		return true;
	}

	private Boolean stickCollide (Point2D p, Double heading) {
		
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);

		Double endX = p.getX()+Math.sin(Math.toRadians(heading))*STICK_LENGTH;
		Double endY = p.getY()+Math.cos(Math.toRadians(heading))*STICK_LENGTH;
		

		if (!safeBF.contains(endX, endY)) {
			return true;
		}


		return false;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		radar.consumeScannedRobotEvent(event);
		
		Enemy e = new Enemy(event, this);
    
		double distance = event.getDistance();
		
		double firePower = 0;
		
		if (getEnergy() < 30)
			firePower = 1.;
		else
			firePower = 3 - (distance / maxDistance) * 3; 
				
				
		double angle = getAimingBearing(event, firePower);
		setTurnGunRight(robocode.util.Utils.normalRelativeAngleDegrees(angle - getGunHeading()));
		
		if (getGunHeat() == 0) {
			setFire(firePower);
		}
	}
	
	private double getAimingBearing(ScannedRobotEvent event, double firePower) {
		double angle;
		Enemy enemy = new Enemy(event, this);
		
		Projection proj = new Projection(enemy.getPosition(),
					enemy.getHeading(), 
					enemy.getVelocity(), 
					(int)Math.signum(enemy.getVelocity()),
					0);
		
		tickProjection tick = proj.projectNextTick();
		while(tick.getPosition().distance(getX(), getY()) > tick.getTick() * (20 - 3 * firePower)) {
			tick = proj.projectNextTick();
		}
		
		angle = Utils.absBearing(new Point2D.Double(getX(),getY()), tick.getPosition());
		
		return angle;
	}
	
	@Override
	public void onHitRobot(HitRobotEvent event) {
		radar.consumeHitAnotherRobotEvent(event);
	}
	
	
	@Override
	public void onPaint(Graphics2D g) {
		super.onPaint(g);
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
		g.draw(safeBF);
		
		
		Point2D center1 = new Point2D.Double(getX() + Math.sin(Math.toRadians(getHeading()-90))*MINIMUM_RADIUS,
				getY() + Math.cos(Math.toRadians(getHeading()-90))*MINIMUM_RADIUS);
		Point2D center2 = new Point2D.Double(getX() + Math.sin(Math.toRadians(getHeading()+90))*MINIMUM_RADIUS,
				getY() + Math.cos(Math.toRadians(getHeading()+90))*MINIMUM_RADIUS);
		g.fillRect((int)center1.getX()-5, (int)center1.getY()-5, 10, 10);
		g.fillRect((int)center2.getX()-5, (int)center2.getY()-5, 10, 10);

		
		double heading = ahead == 1? getHeading() : getHeading() + 180;
		g.drawLine((int) getX(), (int) getY(), (int)(getX()+Math.sin(Math.toRadians(heading))*STICK_LENGTH), (int)(getY()+Math.cos(Math.toRadians(heading))*STICK_LENGTH));

		g.drawArc((int)(center1.getX()-MINIMUM_RADIUS), (int)(center1.getY()-MINIMUM_RADIUS), MINIMUM_RADIUS*2, MINIMUM_RADIUS*2, 0, 360);
		g.drawArc((int)(center2.getX()-MINIMUM_RADIUS), (int)(center2.getY()-MINIMUM_RADIUS), MINIMUM_RADIUS*2, MINIMUM_RADIUS*2, 0, 360);
		
		
		GBulletFiredEvent wave = waves.getNearestWave();
		
		int maeLength = 200;
		if (wave != null) {
			double radius = wave.getVelocity() * (getTime() - wave.getFiringTime());
			g.drawArc((int)(wave.getFiringPosition().getX() - radius), (int)(wave.getFiringPosition().getY() - radius), (int)radius*2, (int)radius*2, 0, 360);
			g.drawRect((int)wave.getFiringPosition().getX() - 5, (int)wave.getFiringPosition().getY() - 5, 10, 10);
			
			
			double absBearing = Utils.absBearing(wave.getFiringPosition(), wave.getTargetPosition());
			
			//draw MAE
			Color c = g.getColor();
			g.setColor(new Color(255,0,0));
			g.drawLine((int)wave.getFiringPosition().getX(), 
					(int)wave.getFiringPosition().getY(), 
					(int)(wave.getFiringPosition().getX() + Math.sin(Math.toRadians(absBearing + wave.getMaxMAE())) * maeLength), 
					(int)(wave.getFiringPosition().getY() + Math.cos(Math.toRadians(absBearing + wave.getMaxMAE())) * maeLength));
			
			g.drawLine((int)wave.getFiringPosition().getX(), 
					(int)wave.getFiringPosition().getY(), 
					(int)(wave.getFiringPosition().getX() + Math.sin(Math.toRadians(absBearing + wave.getMinMAE())) * maeLength), 
					(int)(wave.getFiringPosition().getY() + Math.cos(Math.toRadians(absBearing + wave.getMinMAE())) * maeLength));
			g.setColor(c);
		}
		
		for (Shape s : toDraw) {
			g.draw(s);
		}
		toDraw.clear();
	}
	
}
