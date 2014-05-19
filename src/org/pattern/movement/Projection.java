package org.pattern.movement;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.utils.Utils;

import robocode.Rules;
import sun.net.www.protocol.http.HttpURLConnection.TunnelState;

public class Projection {
	
	public class tickProjection {
		private double heading;
		private double velocity;
		private Point2D position;
		private int tick;
		
		private double turnOffset;

		public double getTurnOffset() {
			return turnOffset;
		}
		public void setTurnOffset(double turnOffset) {
			this.turnOffset = turnOffset;
		}
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
	private Point2D position;
	private double velocity;
	private double bearingOffset;
	private double wantedHeading;
	private double heading;
	
	private boolean turnChanged;
	private double turnChangeOffset;
	
	public double getWantedHeading() {
		return wantedHeading;
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getBearingOffset() {
		return bearingOffset;
	}

	public void setBearingOffset(double bearingOffset) {
		this.bearingOffset = bearingOffset;
		this.wantedHeading = heading+bearingOffset;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
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


	public Projection(Point2D position, double heading, double velocity, int wantedDirection, double bearingOffset) {
		this.projections = new  LinkedList<>();
		this.wantedDirection = wantedDirection;
		this.wantedHeading = heading+bearingOffset;
		this.bearingOffset = bearingOffset;
		this.velocity = velocity;
		this.position = position;
		this.heading = heading;
		this.turnChanged = false;
		
		init();
	}
	
	public void setTurningOffset(double angle) {
		this.wantedHeading += angle;
		this.turnChangeOffset = angle;
		this.turnChanged = true;
		
	}

	public int getWantedDirection() {
		return wantedDirection;
	}

	public void setWantedDirection(int wantedDirection) {
		this.wantedDirection = wantedDirection;
	}
	
	public void init() {
		
		projections.clear();
		turnChanged = false;
		
		
		tickProjection zeroTick = new tickProjection();
		
		zeroTick.setHeading(heading);
		zeroTick.setPosition(position);
		zeroTick.setVelocity(velocity);
		zeroTick.setTick(0);
		
		
		projections.add(zeroTick);
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
			a = wantedDirection * Rules.ACCELERATION;
		}
		else if (wantedDirection == 0){
			a = 0;
		}
		else {
			a = wantedDirection * Rules.DECELERATION;
		}
		//updating heading
		h = lastProjection.getHeading();
		
		double turnRate = 10 - 0.75 * Math.abs(v);
		
		if (turnChanged) {
			turnChanged = false;
			projection.setTurnOffset(turnChangeOffset);
		}
		
		if (wantedHeading != h) {
			int turningDir = wantedHeading > h ? 1 : -1;
			turnRate = Utils.Min(turnRate, Math.abs(wantedHeading - h));
			
			h += turnRate * turningDir;
		}
		        
				
		// updating velocity
		
		//check if we change from dec to acc
		double decTime = Math.abs(v)/2.0;
		double accTime = (1 - decTime);
		
		if (v * wantedDirection < 0 && accTime > 0) {
			v += wantedDirection * decTime * Rules.DECELERATION;
			v += wantedDirection * accTime * Rules.ACCELERATION;
		}
		else {
			v += a;
		}
		
		if (v > 8.)
			v = 8.;
		
		if (v < -8.)
			v = -8.;
		

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
	
	public List<tickProjection> projectNextTicks(int numTicks)	{
		List<tickProjection> ret = new LinkedList<>();

		for(int i=0; i < numTicks; i++){
			ret.add(this.projectNextTick());
		}
		
		projections.addAll(ret);
		return ret;
	}
}
