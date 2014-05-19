package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.LinkedList;
import java.util.List;

import org.pattern.movement.MAE;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.utils.Utils;

import robocode.AdvancedRobot;

public class MAETester extends AdvancedRobot {

	public Point2D bulletPosition = new Point2D.Double(100, 200);
	public double bulletEnergy = 2.0;
	private MAE preciseMAE;
	
	private Point2D startPosition;
	private double v,h;
	
	public MAETester() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		startPosition = new Point2D.Double(getX(), getY());
		h = getHeading();
		v = getVelocity();
		preciseMAE = new MAE(bulletPosition, startPosition, getHeading(), getVelocity(), (20 - 3 * bulletEnergy), new Rectangle2D.Double(0, 0, getBattleFieldWidth(), getBattleFieldHeight()));
		preciseMAE.noSmooth();
		
		
		double startAngle = org.pattern.utils.Utils.absBearingPerpendicular(startPosition, bulletPosition, 1);
		
			
		boolean ahead = true;
		if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(startAngle - getHeading())) > 90.) {
			ahead = false;
			startAngle += 180;
		}
		
		setTurnRight(robocode.util.Utils.normalRelativeAngleDegrees(startAngle - getHeading()));
		setAhead(ahead?1000:-1000);
		execute();
	
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		//paint preciseMAE

		
		for (tickProjection pTick : preciseMAE.getAllPoints()) {

			g.drawRect((int)pTick.getPosition().getX()-2, (int)pTick.getPosition().getY()-2, 4, 4);
		}

		long tick = getTime();
		double radius = (20 - 3 * bulletEnergy) * (tick);

			/* the bullet is fired from cannon that is displaced 10px from the center of the robot */
		g.drawArc((int)(bulletPosition.getX() - radius), (int)(bulletPosition.getY() - radius), (int)radius*2, (int)radius*2, 0, 360);
		g.drawLine((int)getX(), (int)getY(), (int)bulletPosition.getX(), (int)bulletPosition.getY());
		
	}
	
}
