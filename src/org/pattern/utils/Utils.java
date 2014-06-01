package org.pattern.utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;


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

//	public double WallSmoothing(Point2D.Double position, double velocity, double heading, Rectangle2D.Double rect, int turningDirection) {
//
//		//TODO how many ticks we check?
//
//		boolean hitWall = true;
//		double angle = 0;
//		int tries = 0;
//
//		while (!hitWall) {
//			Projection proj = new Projection(position, velocity, heading, (int)Math.signum(velocity), 0);
//			hitWall = false;
//			for (int t=0;t < 25; t++) {
//				tickProjection tick = proj.projectNextTick();
//				if (!rect.contains(tick.getPosition())) {
//					hitWall = true;
//					angle += .5 * turningDirection;
//					break;
//				}
//			}
//			
//			if (tries > 25) {
//				return -1;
//			}
//			tries++;
//			
//		}
//		return angle;
//	}
	
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
	
//	public static Quadrant getQuadrant(Point2D position, Rectangle2D battleField) {
//		
//		if (position.getX() < battleField.getWidth()/2) {
//			if (position.getY() < battleField.getHeight()/2) {
//				return Quadrant.SW;
//			}
//			else {
//				return Quadrant.NW;
//			}
//		}
//		else {
//			if (position.getY() < battleField.getHeight()/2) {
//				return Quadrant.SE;
//			}
//			else 
//				return Quadrant.SW;
//		}
//	}
//	
//	public static int getTurningDirection(Quadrant quadrant, double heading, int direction) {
//		heading = robocode.util.Utils.normalAbsoluteAngle(heading);
//		if (quadrant == Quadrant.NW || quadrant == Quadrant.SW) {
//			if (heading > 0 && heading < 90  || heading > 270 && heading < 360) {
//				return 1 * direction;
//			}
//			else {
//				return -1 * direction;
//			}
//		}
//		else if(quadrant == Quadrant.NE || quadrant == Quadrant.SE) {
//			if (heading > 0 && heading < 90  || heading > 270 && heading < 360) {
//				return 1 * direction;
//			}
//			else {
//				return -1 * direction;
//			}
//		}
//		return 1;
//	}

}
