package org.pattern.movement;

import java.util.List;

import org.pattern.movement.Projection.tickProjection;

import robocode.AdvancedRobot;

public class Path {
	private Projection projection;
	private long tick, startingTick;
	public long getStartingTick() {
		return startingTick;
	}

	public void setStartingTick(long startingTick) {
		this.startingTick = startingTick;
	}

	private boolean end;
	
	public Path(Projection projection) {
		this.projection = projection;
		this.tick = 0;
		this.end = false;
	}
	
	public void init(AdvancedRobot robot) {
		startingTick = robot.getTime();
		tick = startingTick;
	}
	
	public boolean isEnded() {
		return end;
	}
	
	public void followPath(AdvancedRobot robot) {
		long actualTime = robot.getTime();
		
		if (actualTime - tick > 1) {
			robot.out.println("WARNING: SKIPPED TURN ");
		}
		tick = actualTime;
		
		long relativeTick = tick - startingTick;
		//Start of the path
 		if (actualTime == startingTick) {
			robot.setTurnRight(projection.getBearingOffset());
			robot.setAhead(projection.getWantedDirection()*100);
			return;
		}
		
		
		if (relativeTick > projection.getProjections().size() -1) {
			robot.out.println("projection finished");
			end = true;
			robot.setAhead(0);
			return;
		}
		
		tickProjection tickProjection = projection.getProjections().get((int)relativeTick);
		robot.setAhead(projection.getWantedDirection()*100);
		double adjustAngle = tickProjection.getTurnAjust();
		if (adjustAngle > 0) {
			robot.setTurnRight(robot.getTurnRemaining()+adjustAngle);
			robot.out.println("Movement: set adjustment at tick " + robot.getTime() + " relative tick: " + relativeTick);
		}
		return;
		
	}
	
}
