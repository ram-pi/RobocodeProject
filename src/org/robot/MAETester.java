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
	private List<tickProjection> turnFirst = new LinkedList<>();
	
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
		turnFirst = preciseMAE.MAEturnFirst(bulletPosition, new Point2D.Double(getX(), getY()), getHeading(), getVelocity(), (20 - 3 * bulletEnergy));
		
		//double bestHeading = robocode.util.Utils.normalAbsoluteAngleDegrees(Utils.absBearing(new Point2D.Double(getX(), getY()), bulletPosition) - 90);
		double bestHeading = Utils.absBearingPerpendicular(new Point2D.Double(getX(), getY()), bulletPosition, getHeading());
		
		setTurnRight(robocode.util.Utils.normalRelativeAngleDegrees(bestHeading - getHeading()));
		setAhead(500);
		execute();
	
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		//paint preciseMAE

		MAE superPreciseMAE = new MAE(bulletPosition, startPosition, h, v, (20 - 3 * bulletEnergy), new Rectangle2D.Double(0, 0, getBattleFieldWidth(), getBattleFieldHeight()));
		for (tickProjection pTick : preciseMAE.getProjections()) {
			g.drawRect((int)pTick.getPosition().getX()-2, (int)pTick.getPosition().getY()-2, 4, 4);
		}

		g.setColor(new Color(1, 0, 0));

		for (tickProjection pTick : turnFirst) {
			g.drawRect((int)pTick.getPosition().getX()-2, (int)pTick.getPosition().getY()-2, 4, 4);
		}

		long tick = getTime();
		double radius = (20 - 3 * bulletEnergy) * (tick);

			/* the bullet is fired from cannon that is displaced 10px from the center of the robot */
		g.drawArc((int)(bulletPosition.getX() - radius), (int)(bulletPosition.getY() - radius), (int)radius*2, (int)radius*2, 0, 360);

		g.drawLine((int)getX(), (int)getY(), (int)bulletPosition.getX(), (int)bulletPosition.getY());
		
	}
	
}
