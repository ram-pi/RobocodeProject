package org.pattern.utils;

import java.awt.geom.Point2D;
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

	public static double absBearingPerpendicular (Point2D a, Point2D b, double heading) {
		double theta = robocode.util.Utils.normalAbsoluteAngleDegrees((Utils.absBearing(a, b) - 90));

		double offset = robocode.util.Utils.normalRelativeAngleDegrees(theta - heading);

		if (Math.abs(offset) > 90)
			theta -= 180;

		return robocode.util.Utils.normalAbsoluteAngleDegrees(theta);
	}

}
