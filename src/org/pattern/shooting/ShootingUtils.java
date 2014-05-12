package org.pattern.shooting;

import java.awt.geom.Point2D;

public class ShootingUtils {

	public static double absoluteBearingBetweenTwoPoints(double x, double y, double xx, double yy) {
		double xo = xx - x;
		double yo = yy - y;

		double dist = Point2D.distance(x, y, xx, yy);
		double arcSin = Math.toDegrees(Math.asin(xo/dist));
		double bearing = 0;

		if (xo > 0 && yo > 0) { // both pos: lower-Left
			bearing = arcSin;
		} else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
			bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
		} else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
			bearing = 180 - arcSin;
		} else if (xo < 0 && yo < 0) { // both neg: upper-right
			bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
		}

		return bearing;
	}

	// normalizes a bearing to between +180 and -180
	public static double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	/* To avoid complete rotation of the gun */
	public double fixAngle (double angle) {
		angle = angle%360;
		if (angle < -180)
			angle += 360;
		if (angle > 180)
			angle -= 360;
		return angle;
	}
}
