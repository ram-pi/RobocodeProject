package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.pattern.movement.Movement;
import org.pattern.movement.Path;

import robocode.AdvancedRobot;

public class OrbitTester extends AdvancedRobot{

	private Point2D enemyPosition;
	private Movement movement;
	
	
	
	public void run() {
		enemyPosition = new Point2D.Double(300, 400);
		movement = new Movement(this);
		
		double bulletVelocity = 14;
		double travellingTime = enemyPosition.distance(new Point2D.Double(getX(), getY()))/bulletVelocity;
		
		List<Path> orbits = movement.generateOrbits(enemyPosition, (int)travellingTime);
		
		Path p = orbits.get(0);
		int dir = p.getDirection();
		double initialTurn = p.getStartingBearingOffset();
		
		if (dir == 1)
			setAhead(1000);
		else
			setBack(1000);
		

		long currentTime = 0;
		setTurnRight(initialTurn);
		p.getNextTurnOffset();
		p.getNextTurnOffset();
		execute();
		
		
		while (true) {
			double angleToTurn = 0;
			if (currentTime != getTime()) {
				long tickPassed = getTime() - currentTime;
				currentTime = getTime();
				if (tickPassed > 1) {
					out.println("WARNING: SKIPPED TICK!");
				}
				angleToTurn = 0;
				for (int i = 0; i < tickPassed; i++) {
					angleToTurn += p.getNextTurnOffset();
				}
			}
			if(angleToTurn > 0){
				setTurnRight(angleToTurn);
			}
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
