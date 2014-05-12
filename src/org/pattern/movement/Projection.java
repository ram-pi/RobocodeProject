package org.pattern.movement;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.utils.Utils;

public class Projection {
	
	public class tickProjection {
		private double heading;
		private double velocity;
		private Point2D position;
		private int tick;

		public int getTick() {
			return tick;
		}
		public void setTick(int tick) {
			this.tick = tick;
		}
		public double getHeading() {
			return heading;
		}
		public void setHeading(double heading) {
			this.heading = heading;
		}
		public double getVelocity() {
			return velocity;
		}
		public void setVelocity(double velocity) {
			this.velocity = velocity;
		}
		public Point2D getPosition() {
			return position;
		}
		public void setPosition(Point2D position) {
			this.position = position;
		}

	}
	
	
	private List<tickProjection> projections;
	private int wantedDirection;
	private double wantedHeading;

	
	public double getWantedHeading() {
		return wantedHeading;
	}

	public void setWantedHeading(double wantedHeading) {
		this.wantedHeading = wantedHeading;
	}

	public List<tickProjection> getProjections() {
		return projections;
	}

	public void setProjections(List<tickProjection> projections) {
		this.projections = projections;
	}

	private static double MAX_VELOCITY = 8.0;
	
	public Projection(Point2D position, double heading, double velocity, int wantedDirection, double wantedHeading) {
		this.projections = new  LinkedList<>();
		this.wantedDirection = wantedDirection;
		this.wantedHeading = wantedHeading;
		
		tickProjection firstProjection = new tickProjection();
		
		firstProjection.setHeading(heading);
		firstProjection.setPosition(position);
		firstProjection.setVelocity(velocity);
		firstProjection.setTick(getTick());
		
		
		projections.add(firstProjection);
	}
	

	public int getWantedDirection() {
		return wantedDirection;
	}

	public void setWantedDirection(int wantedDirection) {
		this.wantedDirection = wantedDirection;
	}

	public tickProjection projectNextTick() {
		double a,v,h;
		Point2D position;
		
		tickProjection projection = new tickProjection();
		tickProjection lastProjection = projections.get(projections.size()-1);
		
		
		h = lastProjection.getHeading();
		position = lastProjection.getPosition();
		v = lastProjection.getVelocity();
		
		if (v == 0 || wantedDirection * v > 0) { 
			a = wantedDirection * 1;
		}
		else {
			a = wantedDirection * 2;
		}
		
		
		// updating velocity
		v += a;
		
		if (v > 8.)
			v = 8.;
		
		if (v < -8.)
			v = -8.;
		
		//updating heading
		h = lastProjection.getHeading();
		
		double turnRate = 10 - 0.75 * Math.abs(v);
		
		if (wantedHeading != h) {
			int turningDir = wantedHeading > h ? 1 : -1;
			turnRate = Utils.Min(turnRate, Math.abs(wantedHeading - h));
			
			h += turnRate * turningDir;
		}
		
		
		//updating position
		double hRad = Math.toRadians(h);
		position = new Point2D.Double(lastProjection.getPosition().getX() + (v * Math.cos(Math.PI/2 - hRad)), lastProjection.getPosition().getY() + (v *Math.sin(Math.PI/2 - hRad)));
		
		//todo check collision
		
		projection.setHeading(h);
		projection.setTick(lastProjection.getTick() + 1);
		projection.setVelocity(v);
		projection.setPosition(position);
		
		projections.add(projection);
		
		return projection;
		
	}
	
}
