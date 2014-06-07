package org.pattern.utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.movement.Move;
import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.robot.Enemy;

import robocode.AdvancedRobot;
import robocode.Robocode;


public class Utils {
	

	public static double Max(double a, double b) {
		return a>b?a:b;
	}

	public static double Min(double a, double b) {
		return a>b?b:a;
	}

	public static double absBearing(Point2D a, Point2D b) {
		double theta = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());
		theta = Math.PI/2 - theta;
		theta = Math.toDegrees(theta);
		return theta; 
	}

	public static double absBearingPerpendicular (Point2D a, Point2D b, int direction) {
		double theta = robocode.util.Utils.normalAbsoluteAngleDegrees((Utils.absBearing(a, b) - 90));

		
		//Clockwise

		if (direction == 1 && ((a.getX() < b.getX() && theta > 90 && theta < 270) || 
				(a.getX() > b.getX() && (theta > 0 && theta < 90 || theta > 270 && theta < 360))) ) 
					theta += 180;
		if (direction == -1 && ((a.getX() < b.getX() && (theta > 0 && theta < 90 || theta > 270 && theta < 360)) ||
				a.getX() > b.getX() && theta > 90 && theta < 270))
				theta += 180;
	
		return robocode.util.Utils.normalAbsoluteAngleDegrees(theta);
	}
	
	public static int WallSmoothing(Projection proj, int turningDirection, Rectangle2D.Double rect, int numberOfTicks) {

		boolean hitWall = true;
		double angle = 0;
		int tries = 0;

		while (hitWall) {
			proj.setBearingOffset(angle);
			proj.init();
			
			hitWall = false;
			for (int t=0;t < numberOfTicks; t++) {
				tickProjection tick = proj.projectNextTick();
				if (!rect.contains(tick.getPosition())) {
					hitWall = true;
					angle += 2 * turningDirection;
					break;
				}
			}
			
			if (tries > 180) {
				return -1;
			}
			tries++;
		}
		return 0;
	}
	
	public static Point2D.Double calcPoint(Point2D p, double dist, double ang) {
		return new Point2D.Double(p.getX() + dist*Math.sin(ang), p.getY() + dist*Math.cos(ang));
	}

	public static double calcAngle(Point2D p2,Point2D p1){
		return Math.atan2(p2.getX() - p1.getX(), p2.getY() - p1.getY());
	}

	public static Boolean pointInBattlefield(AdvancedRobot robot, Point2D p) {
		if (p.getX() > robot.getBattleFieldWidth() || p.getX() < 0 || p.getY() > robot.getBattleFieldHeight() || p.getY() < 0)
			return false;
		return true;
	}
	

	public static double firingOffset(Point2D firingPosition, Point2D targetPosition,
			Point2D hitPosition) {
		double firingBearing = robocode.util.Utils
				.normalAbsoluteAngleDegrees(Utils.absBearing(firingPosition,
						hitPosition));
		double bearing = robocode.util.Utils.normalAbsoluteAngleDegrees(Utils
				.absBearing(firingPosition, targetPosition));

		double ret;
		if (firingBearing > bearing)
			ret = firingBearing - bearing;
		else
			ret = -(bearing - firingBearing);

		return robocode.util.Utils.normalRelativeAngleDegrees(ret);
	}
	
	public static List<Point2D> generatePoints(AdvancedRobot robot, Enemy enemy) {
		int numPoints = Costants.SURFING_NUM_POINTS;
		double ENEMY_DISTANCE = 0.8;
		List<Point2D> points = new LinkedList<>();
		Point2D myPosition  = new Point2D.Double(robot.getX(), robot.getY());
		double bearing;
		
		for (int i = 0; i < numPoints; i++) {
			bearing = robocode.util.Utils.normalAbsoluteAngleDegrees(robot.getHeading() + (360 / numPoints) * i);
			double distance = Math.min(ENEMY_DISTANCE*enemy.getDistance(), 100);
			points.add(calcPoint(myPosition, distance, bearing));
		}
		
		return points;
	}
	
	public static List<Point2D> generatePointsPerpendicular(AdvancedRobot robot, Enemy enemy) {
		List<Point2D> ret = null;
		int numPoints = Costants.SURFING_NUM_POINTS;
		Point2D myPos = new Point2D.Double(robot.getX(), robot.getY());	
		for (int i = 0; i < numPoints; i++) {
			
		}
		return ret;
	}
	
	public static double getProjectedGF(AdvancedRobot robot, GBulletFiredEvent wave, Point2D toGo) {
		Point2D myPosition = new Point2D.Double(robot.getX(), robot.getY());

		double angle = robocode.util.Utils.normalAbsoluteAngleDegrees(absBearing(myPosition, toGo));
		Move m = new Move(robot);
		m.move(angle, robot.getHeading());
		
		
		Projection projection = new Projection(myPosition, robot.getHeading(), robot.getVelocity(), m.ahead, m.turnRight);


		tickProjection tick = projection.projectNextTick();
		int timeElapsed = (int) (robot.getTime() - wave.getFiringTime());

		while (tick.getPosition().distance(wave.getFiringPosition()) > (timeElapsed + tick
				.getTick()) * wave.getVelocity()) {
			tick = projection.projectNextTick();

			if (m.smooth(tick.getPosition(), tick.getHeading(),
					projection.getWantedHeading(), m.ahead)) {
				projection.setWantedDirection(m.ahead);
				projection.setWantedHeading(tick.getHeading() + m.turnRight);
			}
		}

		double firingOffset = firingOffset(wave.getFiringPosition(), wave.getTargetPosition(), tick.getPosition());

		double _mae = firingOffset > 0 ? wave.getMaxMAE() : wave.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset / _mae : -firingOffset
				/ _mae;

		return gf;
	}
	
	public static double calculateGF(GBulletFiredEvent wave, Point2D point) {
		double firingOffset = firingOffset(wave.getFiringPosition(),
				wave.getTargetPosition(), point);

		double _mae = firingOffset > 0 ? wave.getMaxMAE() : wave.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset / _mae : -firingOffset
				/ _mae;
		
		return gf;
	}
	
	public static double getDistanceFromWall(Point2D position, Rectangle2D battlefield) {
		return Math.max(Math.abs(position.getX() - battlefield.getWidth()), Math.abs(position.getY()- battlefield.getHeight()));
	}
	
	public static double getMAE(Point2D firingPosition, Point2D targetPosition,
			double targetHeading, double targetVelocity, double waveVelocity,
			int cw, AdvancedRobot robot) {

		double angle = org.pattern.utils.Utils.absBearingPerpendicular(
				targetPosition, firingPosition, cw);
		Move m = new Move(robot);
		m.move(angle, targetHeading);
		// TODO use values 2 ticks before detecting the wave
		Projection projection = new Projection(targetPosition, targetHeading,
				targetVelocity, m.ahead, m.turnRight);

		tickProjection tick = projection.projectNextTick();
		double tempMae = cw == -1 ? Double.MAX_VALUE : Double.MIN_VALUE;

		while (tick.getPosition().distance(firingPosition) > tick.getTick()
				* waveVelocity) {
			tick = projection.projectNextTick();

			if (cw == -1) {
				tempMae = Math.min(
						tempMae,
						firingOffset(firingPosition, targetPosition,
								tick.getPosition()));
			} else {
				tempMae = Math.max(
						tempMae,
						firingOffset(firingPosition, targetPosition,
								tick.getPosition()));
			}

			if (m.smooth(tick.getPosition(), tick.getHeading(),
					projection.getWantedHeading(), m.ahead)) {
				projection.setWantedDirection(m.ahead);
				projection.setWantedHeading(tick.getHeading() + m.turnRight);
			}
		}

		return tempMae;
	}
	
	public static void setWaveMAE(GBulletFiredEvent wave, double heading,
			double velocity, AdvancedRobot robot) {

		double mae[] = new double[2];
		for (int orbitDirection = -1; orbitDirection < 2; orbitDirection += 2) {

			mae[orbitDirection == -1 ? 0 : 1] = getMAE(	wave.getFiringPosition(), wave.getTargetPosition(),
					heading, velocity, wave.getVelocity(), orbitDirection, robot);
		}
		wave.setMinMAE(Math.min(mae[0], mae[1]));
		wave.setMaxMAE(Math.max(mae[0], mae[1]));
		return;
	}
	
 }
