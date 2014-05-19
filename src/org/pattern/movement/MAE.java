package org.pattern.movement;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.movement.Projection.tickProjection;
import org.pattern.utils.Utils;

import robocode.AdvancedRobot;

public class MAE {

	private Point2D firingPosition;
	private Point2D position;
	private double heading;
	private double velocity;
	private Rectangle2D battlefield;
	
	private Projection cw,ccw;

	private List<tickProjection> projections;
	private double bulletVelocity;

	public List<tickProjection> getProjections() {
		return projections;
	}

	public void setProjections(List<tickProjection> projections) {
		this.projections = projections;
	}

	public Point2D getFiringPosition() {
		return firingPosition;
	}

	public void setFiringPosition(Point2D firingPosition) {
		this.firingPosition = firingPosition;
	}

	public Point2D getPosition() {
		return position;
	}

	public void setPosition(Point2D position) {
		this.position = position;
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

	public List<tickProjection> MAEturnFirst(Point2D firingPosition, Point2D position, double heading, double velocity,double bulletVelocity) {
		this.firingPosition = firingPosition;
		this.position = position;
		this.heading = heading;
		this.velocity = velocity;


		double bestHeading = Utils.absBearingPerpendicular(firingPosition, position, 1);

		int wantedDirection = (int)Math.signum(velocity);
		Projection projection = new Projection(position, heading, velocity, wantedDirection, robocode.util.Utils.normalRelativeAngleDegrees(bestHeading - heading));
		List<tickProjection> rotatingProjections = new LinkedList<>();


		for (int t = 1; t < 200; t++) {
			tickProjection tick = projection.projectNextTick();

			if (robocode.util.Utils.normalNearAbsoluteAngleDegrees(Math.abs(tick.getHeading() -  bestHeading)) < 0.0001) {
				rotatingProjections.addAll(projection.getProjections());
				break;
			}
		}


		tickProjection lastTick = rotatingProjections.get(rotatingProjections.size()-1);

		projection = new Projection(lastTick.getPosition(), lastTick.getHeading(), lastTick.getVelocity(), 1, 0);
		List<tickProjection> positiveMAE = new LinkedList<>();
		boolean found = false;

		double rotatingTime = lastTick.getTick();

		for (int t = 1; t < 200 || !found; t++) {
			tickProjection tick = projection.projectNextTick();

			if (Math.abs(tick.getPosition().distance(firingPosition) -  bulletVelocity * (t + rotatingTime)) < 10) {
				found = true;
				positiveMAE.addAll(projection.getProjections());
			}
		}

		projection = new Projection(lastTick.getPosition(), lastTick.getHeading(), lastTick.getVelocity(), -1, 0);
		List<tickProjection> negativeMAE = new LinkedList<>();
		found = false;

		for (int t = 1; t < 200 || !found; t++) {
			tickProjection tick = projection.projectNextTick();

			if (Math.abs(tick.getPosition().distance(firingPosition) -  bulletVelocity * (t + rotatingTime)) < 10) {
				found = true;
				negativeMAE.addAll(projection.getProjections());
			}
		}

		positiveMAE.remove(0);
		negativeMAE.addAll(positiveMAE);

		return negativeMAE;
	}


	public void noSmooth() {
		
		double startAngle = org.pattern.utils.Utils.absBearingPerpendicular(position, firingPosition, 1);
		
		Rectangle2D safeBF = new Rectangle2D.Double(battlefield.getX()+18, battlefield.getY()+18, battlefield.getWidth()-18, battlefield.getHeight()-18);
		
		boolean ahead = true;
		if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(startAngle - heading)) > 90.) {
			ahead = false;
			startAngle += 180;
		}
		
		cw = new Projection(position, 
				heading, 
				velocity, 
				ahead? 1 : -1, 
				robocode.util.Utils.normalRelativeAngleDegrees(startAngle-heading));
		projectsUntilCrossWaveOrHitWall(cw, safeBF);
		
		
		ccw = new Projection(position, 
				heading, 
				velocity, 
				ahead? -1 : 1, 
				robocode.util.Utils.normalRelativeAngleDegrees(startAngle-heading));
		
		projectsUntilCrossWaveOrHitWall(ccw, safeBF);
		
	}
	
	private void projectsUntilCrossWaveOrHitWall(Projection proj, Rectangle2D safeBF) {
		boolean found = false;
		for (int t = 1; t < 200 && !found; t++) {
			tickProjection tick = proj.projectNextTick();
			
			if (tick.getPosition().distance(firingPosition) <  bulletVelocity * t) {
				found = true;
			}
			//hit a wall
			if (!safeBF.contains(tick.getPosition())){
				found = true;
			}
		}
	}

	public List<tickProjection> getAllPoints() {
		List<tickProjection> ret = new LinkedList<>();
		ret.addAll(cw.getProjections());
		ret.remove(0);
		ret.addAll(ccw.getProjections());
		return ret;
	}
	
	public MAE(Point2D firingPosition, Point2D position, double heading, double velocity, double bulletVelocity, Rectangle2D battleField) {

		this.firingPosition = firingPosition;
		this.position = position;
		this.heading = heading;
		this.velocity = velocity;
		this.battlefield = battleField;
		this.bulletVelocity = bulletVelocity;

	}

}