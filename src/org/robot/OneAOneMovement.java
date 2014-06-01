package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.pattern.radar.Radar;
import org.pattern.utils.Utils;

import com.sun.xml.internal.messaging.saaj.packaging.mime.Header;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class OneAOneMovement extends AdvancedRobot implements Observer{

	int ahead = 1;
	double randomAngle;


	final int STICK_LENGTH = 140;
	final int MINIMUM_RADIUS = 114;
	
	
	long lastBulletFiredTime = Long.MAX_VALUE;
	boolean bulletJustFired = false;
	boolean followRandom = false;
	boolean fire = false;
	Radar radar;

	public OneAOneMovement() {
		radar = new Radar(this);
		radar.addObserver(this);
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
		if (arg1 instanceof GBulletFiredEvent) {
			lastBulletFiredTime = getTime();
			bulletJustFired = true;
		}
	}
	
	
	@Override
	public void run() {
		setAdjustRadarForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustGunForRobotTurn(true);
		int cw = 1;
		Random r = new Random();

		

		while(true) {
			radar.doScan();
			
			double _ahead = 0;
			double _turnRight = 0;
			
			if (bulletJustFired) {
				double angle = r.nextInt(30);
				cw = r.nextInt(2);
				if (cw == 0)
					cw = -1;
				
				out.println("orbiting " + cw);
				
//				angle = org.pattern.utils.Utils.absBearingPerpendicular(new Point2D.Double(getX(), getY()), radar.getLockedEnemy().getPosition(), cw) + angle;
				if (radar.getLockedEnemy() != null) {
					angle = org.pattern.utils.Utils.absBearingPerpendicular(new Point2D.Double(getX(), getY()), radar.getLockedEnemy().getPosition(), cw);
				}
				
				ahead = 1;				
				if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - getHeading())) > 90) {
					ahead = -1;
					angle += 180;
				}

				_turnRight = robocode.util.Utils.normalRelativeAngleDegrees(angle - getHeading());
				bulletJustFired = false;
				followRandom = true;
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
				} else if (smoothC1) {
					if (getTurnRemaining()+_turnRight > -4)
						_turnRight = -4;
	
				} else if (smoothC2) {
					if (getTurnRemaining()+_turnRight < 4)
						_turnRight = 4;
				}
			}
			
			setAhead(_ahead);
			setTurnRight(_turnRight);
			execute();
		}
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

		Double endX = getX()+Math.sin(Math.toRadians(heading))*STICK_LENGTH;
		Double endY = getY()+Math.cos(Math.toRadians(heading))*STICK_LENGTH;
		

		if (!safeBF.contains(endX, endY)) {
			return true;
		}


		return false;
	}

	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		radar.consumeScannedRobotEvent(event);
		
		Enemy e = new Enemy(event, this);

		double angle = getAimingBearing(event,1.2);
		setTurnGunRight(robocode.util.Utils.normalRelativeAngleDegrees(angle - getGunHeading()));
		
		if (getGunHeat() == 0) {
			setFire(1.2);
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
	}
	
}
