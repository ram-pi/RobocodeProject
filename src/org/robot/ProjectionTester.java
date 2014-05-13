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

		projection = new Projection(new Point2D.Double(getX(),getY()), getHeading(), getVelocity(), 1, getHeading());
		projectionsList = new LinkedList<>();
		Projection visualProjection = new Projection(new Point2D.Double(getX(), getY()), getHeading(), getVelocity(), 1, getHeading());
		
		projectionsList.addAll(visualProjection.projectNextTicks(5));
		
		tickProjection lastBeforeTurning = projectionsList.get(projectionsList.size()-1);
		
		visualProjection = new Projection(lastBeforeTurning.getPosition(), lastBeforeTurning.getHeading(), lastBeforeTurning.getVelocity(), -1, lastBeforeTurning.getHeading()+20);
		
		projectionsList.addAll(visualProjection.projectNextTicks(30));
		
		long lastTime = -1;
		while (true) {
			
			if (lastTime != -1 && getTime() - lastTime > 1){
				out.println("Skipped turn");
			}
			
			lastTime = getTime();
			
			lastTickProjection = projection.getProjections().get(projection.getProjections().size()-1);
			
			//out.println("X:" + lastTickProjection.getPosition().getX() + "\nY:" + lastTickProjection.getPosition().getY() + "\nvel:" + lastTickProjection.getVelocity() + "\nhed:" + getHeading());
			
			if (lastTickProjection.getPosition().distance(new Point2D.Double(getX(),getY())) > 0.00001 ||
				Math.abs(lastTickProjection.getHeading() - getHeading()) > 0.00001 ||
				Math.abs(lastTickProjection.getVelocity() - getVelocity()) > 0.00001) {
				
				out.println("Wrong prediction!");
			}
	
			if (getTime() < 5) {
				setAhead(100);
			}
			else if (getTime() == 5) {
				projection = new Projection(new Point2D.Double(getX(),getY()), getHeading(), getVelocity(), -1, getHeading()+20);
				setTurnRight(20);
				setBack(100);
			}
			else if (getTime() < 50){
				setBack(100);
			}
			
			projection.projectNextTick();
					
			execute();
		}
		
		
		
	}
	
	@Override
	public void onPaint(Graphics2D g) {
	
		for (tickProjection tickProjection : projectionsList) {
			g.drawRect((int)tickProjection.getPosition().getX() - 5, (int)tickProjection.getPosition().getY() - 5, 10, 10);
		}
		
		//g.drawRect((int)lastTickProjection.getPosition().getX() - 5, (int)lastTickProjection.getPosition().getY() - 5, 10, 10);
		
		
		super.onPaint(g);
		
	}
}
