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

	private List<tickProjection> projections;

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


		double bestHeading = Utils.absBearingPerpendicular(firingPosition, position, heading);

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


	public MAE(Point2D firingPosition, Point2D position, double heading, double velocity, double bulletVelocity, Rectangle2D battleField) {

		this.firingPosition = firingPosition;
		this.position = position;
		this.heading = heading;
		this.velocity = velocity;

		double bestHeading = Utils.absBearingPerpendicular(firingPosition, position, heading);
		double wantedAngle = robocode.util.Utils.normalRelativeAngleDegrees(bestHeading - heading);

		Projection projection = new Projection(position, heading, velocity, 1, robocode.util.Utils.normalRelativeAngleDegrees(bestHeading - heading));
		List<tickProjection> positiveMAE = new LinkedList<>();

		boolean found = false;
		boolean wallsSmoothed = false;

		while (!wallsSmoothed) {
			wallsSmoothed = true;
			for (int t = 1; t < 200 || !found; t++) {
				tickProjection tick = projection.projectNextTick();

				boolean top = false, bottom = false, left = false, right= false;
				double currentHeading = robocode.util.Utils.normalAbsoluteAngleDegrees(tick.getHeading());

				if (tick.getPosition().getY() < 120. && (currentHeading > 90 && currentHeading < 270))
					bottom = true;

				if (tick.getPosition().getY() > battleField.getHeight() - 120. && (currentHeading < 90 && currentHeading > 0 || currentHeading > 270 && currentHeading < 360))
					top = true;

				if (tick.getPosition().getX() < 120. && (currentHeading > 180 && currentHeading < 360))
					left = true;

				if (tick.getPosition().getX() > battleField.getWidth() - 120. && (currentHeading > 0 && currentHeading < 180))
					right = true;

				double distanceToWall;
				if (bottom && !left && !right) {
					double alfa = Math.abs(180. - currentHeading);
					distanceToWall = tick.getPosition().getY() / Math.cos(Math.toRadians(alfa));
					if (distanceToWall < 120) {
						wallsSmoothed = false;
						if (currentHeading > 180)
							projection.setBearingOffset(wantedAngle+2);
						else
							projection.setBearingOffset(wantedAngle-2);

						projection.init();
						break;
					}
				}


				if (Math.abs(tick.getPosition().distance(firingPosition) -  bulletVelocity * t) < 10) {
					found = true;
					positiveMAE.addAll(projection.getProjections());
				}
			}
		}

		projection = new Projection(position, heading, velocity, -1, robocode.util.Utils.normalRelativeAngleDegrees(bestHeading - heading));
		List<tickProjection> negativeMAE = new LinkedList<>();
		found = false;

		for (int t = 1; t < 200 || !found; t++) {
			tickProjection tick = projection.projectNextTick();

			if (Math.abs(tick.getPosition().distance(firingPosition) -  bulletVelocity * t) < 10) {
				found = true;
				negativeMAE.addAll(projection.getProjections());
			}
		}

		positiveMAE.remove(0);
		negativeMAE.addAll(positiveMAE);

		projections = negativeMAE;

	}

}