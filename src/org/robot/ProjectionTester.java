package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.LinkedList;
import java.util.List;


import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;

import robocode.AdvancedRobot;

public class ProjectionTester extends AdvancedRobot {
	private Projection projection;
	private List<tickProjection> projectionsList;
	private tickProjection lastTickProjection;
	
	public ProjectionTester() {
		
	}

	@Override
	public void run() {
		projectionsList = new LinkedList<>();
		projection = new Projection(new Point2D.Double(getX(),getY()), getHeading(), getVelocity(), 1, getHeading());
		
		
		long lastTime = -1;
		while (true) {
			
			if (lastTime != -1 && getTime() - lastTime > 1){
				out.println("Skipped turn");
			}
			
			lastTime = getTime();
			
			lastTickProjection = projection.getProjections().get(projection.getProjections().size()-1);
			
			if (!lastTickProjection.getPosition().equals(new Point2D.Double(getX(),getY())) ||
				lastTickProjection.getHeading() != getHeading() ||
				lastTickProjection.getVelocity() != getVelocity()) {
				
				out.println("Wrong prediction!");
			}
	
			if (getTime() < 20) {
				setAhead(100);
			}
			else if (getTime() == 20) {
				projection = new Projection(new Point2D.Double(getX(),getY()), getHeading(), getVelocity(), -1, getHeading()+20);
				setTurnRight(20);
				setBack(100);
			}
			else {
				setBack(100);
			}
			
			projection.projectNextTick();
					
			execute();
		}
		
		
		
	}
	
	@Override
	public void onPaint(Graphics2D g) {
	
		
		g.drawRect((int)lastTickProjection.getPosition().getX() - 5, (int)lastTickProjection.getPosition().getY() - 5, 10, 10);
		
		
		super.onPaint(g);
		
	}
}
