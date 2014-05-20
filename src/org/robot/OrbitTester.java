package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.pattern.movement.Movement;
import org.pattern.movement.Path;
import org.pattern.radar.GBulletFiredEvent;

import robocode.AdvancedRobot;

public class OrbitTester extends AdvancedRobot{

	private Point2D enemyPosition;
	private Movement movement;
	
	public OrbitTester() {
		movement = new Movement(this);
		enemyPosition = new Point2D.Double(300, 400);
		
	}
	
	
	public void run() {
		
		Enemy mockEnemy = new Enemy();
		mockEnemy.setX(enemyPosition.getX());
		mockEnemy.setY(enemyPosition.getY());
		
		GBulletFiredEvent gBulletFiredEvent = new GBulletFiredEvent();
		gBulletFiredEvent.setFiringRobot(mockEnemy);
		gBulletFiredEvent.setEnergy(2.0);
		gBulletFiredEvent.setVelocity(20 - 3 * (2.0));
		gBulletFiredEvent.setFiringTime(getTime());
		gBulletFiredEvent.setFiringPosition(enemyPosition);
		
		movement.update(null, gBulletFiredEvent);

		
		while(true) {
//			if (getTime() % 15 == 1) {
//				
//				gBulletFiredEvent.setFiringTime(getTime());
//				
//				movement.update(null, gBulletFiredEvent);
//			}
			
			movement.doMovement();
			execute();
		}		
		
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		
		g.drawRect((int)enemyPosition.getX()-2, (int)enemyPosition.getY()-2, 4, 4);
		movement.consumeOnPaintEvent(g);
		
		double bulletVelocity = 14;
		double travellingTime = enemyPosition.distance(new Point2D.Double(getX(), getY()))/bulletVelocity;
		
		
		//movement.generateOrbits(enemyPosition, (int)travellingTime);
		
		g.drawLine((int)getX(), (int)getY(), (int)enemyPosition.getX(), (int)enemyPosition.getY());
	}
}
