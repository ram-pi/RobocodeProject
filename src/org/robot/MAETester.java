package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.LinkedList;
import java.util.List;

import org.pattern.movement.MAE;
import org.pattern.movement.Path;
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
		preciseMAE.wallSmoothStick();
		
		out.println("MAE: " + preciseMAE.getMAE());
		out.println("CWMAE: " + preciseMAE.getCwMAE());
		out.println("CCWMAE: " + preciseMAE.getCcwMAE());
		
		Path cwPath = new Path(preciseMAE.getCw());
		cwPath.init(this);
		
		
		while(true) {
			cwPath.followPath(this);
			execute();
		}
	
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
		
		MAE asd = new MAE(bulletPosition, startPosition, getHeading(), getVelocity(), (20 - 3 * bulletEnergy) , new Rectangle2D.Double(0, getBattleFieldHeight(), getBattleFieldWidth(), getBattleFieldHeight()));
		asd.wallSmoothStick();
		
		Rectangle2D battlefield = new Rectangle2D.Double(0, 0, getBattleFieldWidth(), getBattleFieldHeight());
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, getBattleFieldWidth()-36, getBattleFieldHeight()-36);
		

		g.draw(safeBF);
		
		int dir = asd.getCw().getWantedDirection();
		double directionHeading = dir == 1? getHeading() : getHeading() + 180;
		double stickLenght = 160;
		double xend=getX()+Math.sin(Math.toRadians(directionHeading))*stickLenght;
		double yend=getY()+Math.cos(Math.toRadians(directionHeading))*stickLenght;
		Line2D stick = new Line2D.Double(getX(), getY(), xend, yend);
		g.draw(stick);
	}
	
}
