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
		
		private double turnAdjust;

		public double getTurnAjust() {
			return turnAdjust;
		}
		public void setTurnAdjust(double turnAdjust) {
			this.turnAdjust = turnAdjust;
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
	private double distance;
	private double distanceRemaining;
	
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


	
	private double getNewVelocity(double velocity, double distance) {
		if (distance < 0) {
			// If the distance is negative, then change it to be positive
			// and change the sign of the input velocity and the result
			return -getNewVelocity(-velocity, -distance);
		}

		final double goalVel;

		if (distance == Double.POSITIVE_INFINITY) {
			goalVel = MAX_VELOCITY;
		} else {
			goalVel = Math.min(getMaxVelocity(distance), MAX_VELOCITY);
		}

		if (velocity >= 0) {
			return Math.max(velocity - Rules.DECELERATION, Math.min(goalVel, velocity + Rules.ACCELERATION));
		}
		// else
		return Math.max(velocity - Rules.ACCELERATION, Math.min(goalVel, velocity + maxDecel(-velocity)));
	}

	private final static double getMaxVelocity(double distance) {
		final double decelTime = Math.max(1, Math.ceil(// sum of 0... decelTime, solving for decelTime using quadratic formula
				(Math.sqrt((4 * 2 / Rules.DECELERATION) * distance + 1) - 1) / 2));

		if (decelTime == Double.POSITIVE_INFINITY) {
			return Rules.MAX_VELOCITY;
		}

		final double decelDist = (decelTime / 2.0) * (decelTime - 1) // sum of 0..(decelTime-1)
				* Rules.DECELERATION;

		return ((decelTime - 1) * Rules.DECELERATION) + ((distance - decelDist) / decelTime);
	}

	private static double maxDecel(double speed) {
		double decelTime = speed / Rules.DECELERATION;
		double accelTime = (1 - decelTime);

		return Math.min(1, decelTime) * Rules.DECELERATION + Math.max(0, accelTime) * Rules.ACCELERATION;
	}
	
	public Projection(Point2D position, double heading, double velocity, int wantedDirection, double bearingOffset, double distance) {

		Costruct(position, heading, velocity, wantedDirection, bearingOffset, distance); 
	}
	
	private void Costruct(Point2D position, double heading, double velocity, int wantedDirection, double bearingOffset, double distance) {
		this.projections = new  LinkedList<>();
		this.wantedDirection = wantedDirection;
		this.wantedHeading = heading+bearingOffset;
		this.bearingOffset = bearingOffset;
		this.velocity = velocity;
		this.position = position;
		this.heading = heading;
		this.distance = distance;
		this.distanceRemaining = distance;
		
		
		init();
	}
	public Projection(Point2D position, double heading, double velocity, int wantedDirection, double bearingOffset) {
		Costruct(position, heading, velocity, wantedDirection, bearingOffset, Double.POSITIVE_INFINITY);
	}
	
	public void setTurningAdjustment(double angle) {
		tickProjection last = projections.get(projections.size() - 1);
		last.setTurnAdjust(angle);
		this.wantedHeading += angle;
	}

	public int getWantedDirection() {
		return wantedDirection;
	}

	public void setWantedDirection(int wantedDirection) {
		this.wantedDirection = wantedDirection;
	}
	
	public void init() {
		
		projections.clear();

		
		
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
		
		if (wantedHeading != h) {
			int turningDir = wantedHeading > h ? 1 : -1;
			turnRate = Utils.Min(turnRate, Math.abs(wantedHeading - h));
			
			h += turnRate * turningDir;
		}
		        
				
		// updating velocityh
		
		//check if we change from dec to acc
//		double decTime = Math.abs(v)/2.0;
//		double accTime = (1 - decTime);
//		
//		if (v * wantedDirection < 0 && accTime > 0) {
//			v += wantedDirection * decTime * Rules.DECELERATION;
//			v += wantedDirection * accTime * Rules.ACCELERATION;
//		}
//		else {
//			v += a;
//		}
//		
//		if (v > 8.)
//			v = 8.;
//		
//		if (v < -8.)
//			v = -8.;
		
		v = getNewVelocity(v, distanceRemaining);
		

		//updating position
		double hRad = Math.toRadians(h);
		position = new Point2D.Double(lastProjection.getPosition().getX() + (v * Math.cos(Math.PI/2 - hRad)), lastProjection.getPosition().getY() + (v *Math.sin(Math.PI/2 - hRad)));
		distanceRemaining -= Math.abs(v);
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
