package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import org.pattern.movement.Movement;

import robocode.AdvancedRobot;
import sun.awt.TracedEventQueue;

public class OrbitTester extends AdvancedRobot{

	private Point2D enemyPosition;
	private Movement movement;
	
	
	
	public void run() {
		enemyPosition = new Point2D.Double(300, 400);
		movement = new Movement(this);
		
		double bulletVelocity = 14;
		double travellingTime = enemyPosition.distance(new Point2D.Double(getX(), getY()))/bulletVelocity;
		
		movement.generateOrbits(enemyPosition, (int)travellingTime);
		
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		
		g.drawRect((int)enemyPosition.getX()-2, (int)enemyPosition.getY()-2, 4, 4);
		movement.consumeOnPaintEvent(g);
		
		double bulletVelocity = 14;
		double travellingTime = enemyPosition.distance(new Point2D.Double(getX(), getY()))/bulletVelocity;
		
		
		movement.generateOrbits(enemyPosition, (int)travellingTime);
		
		g.drawLine((int)getX(), (int)getY(), (int)enemyPosition.getX(), (int)enemyPosition.getY());
	}
}
