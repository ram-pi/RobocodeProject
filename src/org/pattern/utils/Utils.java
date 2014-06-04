package org.pattern.utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;

import robocode.AdvancedRobot;


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
}
