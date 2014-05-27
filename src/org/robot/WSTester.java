package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.Random;

import org.pattern.movement.Path;
import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;

import robocode.AdvancedRobot;
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

public class WSTester extends AdvancedRobot{


	int ahead = 1;
	double randomAngle;
	Point2D center1, center2;
	Point2D endPoint;

	final int STICK_LENGTH = 140;
	final int MINIMUM_RADIUS = 114;

	@Override
	public void run() {
		Boolean followRandom = false;
		Random r = new Random(new Date().getTime());

		Long lastTimeRandom = new Long(0);

		while (true) {

			stickCollide(new Point2D.Double(getX(), getY()), ahead == 1 ? getHeading() : getHeading()+180);

			if (!followRandom) {
				randomAngle = r.nextInt(360);
				ahead = 1;

				if (robocode.util.Utils.normalRelativeAngleDegrees(randomAngle - getHeading()) > 90) {
					ahead = -1;
					randomAngle += 180;
				}

				setTurnRight(robocode.util.Utils.normalRelativeAngleDegrees(randomAngle - getHeading()));
				setAhead(ahead*200);
				followRandom = true;
			}

			if (getTime() - lastTimeRandom > 20) {
				followRandom = false;
				lastTimeRandom = getTime();
			}



			Projection proj = new Projection(new Point2D.Double(getX(), getY()), getHeading(), getVelocity(), ahead, getTurnRemaining());
			tickProjection t = proj.projectNextTick();
			if (stickCollide(t.getPosition(), ahead == 1 ? getHeading() : getHeading()+180)) {
				center1 = new Point2D.Double(getX() + Math.sin(Math.toRadians(getHeading()-90))*MINIMUM_RADIUS,
						getY() + Math.cos(Math.toRadians(getHeading()-90))*MINIMUM_RADIUS);
				center2 = new Point2D.Double(getX() + Math.sin(Math.toRadians(getHeading()+90))*MINIMUM_RADIUS,
						getY() + Math.cos(Math.toRadians(getHeading()+90))*MINIMUM_RADIUS);
				Boolean smoothC1 = canSmooth(center1);
				Boolean smoothC2 = canSmooth(center2);
				if (!smoothC1 && !smoothC2) {
					/* TODO  Facciamo manovra*/ 
					setAhead(ahead*(-100));
					System.out.println("No way out");
				} else if (smoothC1) {
					if (getTurnRemaining() > -4)
						setTurnRight(-4);

				} else if (smoothC2) {
					if (getTurnRemaining() < 4)
						setTurnRight(4);
				}
			}
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
		Boolean ret = false;
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);

		Double endX = getX()+Math.sin(Math.toRadians(heading))*STICK_LENGTH;
		Double endY = getY()+Math.cos(Math.toRadians(heading))*STICK_LENGTH;
		endPoint = new Point2D.Double(endX, endY);

		if (!safeBF.contains(endX, endY)) {
			return true;
		}


		return ret;
	}

	@Override
	public void onPaint(Graphics2D g) {
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
		g.draw(safeBF);
		
		g.fillRect((int)center1.getX()-5, (int)center1.getY()-5, 10, 10);
		g.fillRect((int)center2.getX()-5, (int)center2.getY()-5, 10, 10);

		g.drawLine((int) getX(), (int) getY(), (int)endPoint.getX(), (int)endPoint.getY());

		g.drawArc((int)(center1.getX()-MINIMUM_RADIUS), (int)(center1.getY()-MINIMUM_RADIUS), MINIMUM_RADIUS*2, MINIMUM_RADIUS*2, 0, 360);
		g.drawArc((int)(center2.getX()-MINIMUM_RADIUS), (int)(center2.getY()-MINIMUM_RADIUS), MINIMUM_RADIUS*2, MINIMUM_RADIUS*2, 0, 360);
	}
}
