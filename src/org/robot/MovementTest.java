package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.pattern.utils.Utils;

import robocode.AdvancedRobot;
import robocode.GunTurnCompleteCondition;

public class MovementTest extends AdvancedRobot {
	Point2D.Double destination = new Point2D.Double(200, 300);
	@Override
	public void run() {
		double theta = robocode.util.Utils.normalAbsoluteAngleDegrees(Utils.absBearing(new Point2D.Double(getX(), getY()), destination));
		double angleToTurn = robocode.util.Utils.normalRelativeAngleDegrees(theta - getHeading());
		

		turnRight(angleToTurn);
		ahead(destination.distance(getX(), getY()));
		
//		setTurnRight(angleToTurn);
//		setAhead(destination.distance(getX(),getY()));

		
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		// TODO Auto-generated method stub
		super.onPaint(g);
		g.drawRect(200-5, 300-5, 10, 10);
	}
}
