package org.pattern.shooting;

import java.awt.geom.Point2D;

import robocode.util.Utils;

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

	/* normalizes a bearing to between +180 and -180 */
	public static double normalizeBearing(double angle) {
		while (angle >  180) angle -= 360;
		while (angle < -180) angle += 360;
		return angle;
	}
	
	/* To avoid complete rotation of the gun */
	public static double fixAngle (double angle) {
		angle = angle%360;
		if (angle < -180)
			angle += 360;
		if (angle > 180)
			angle -= 360;
		return angle;
	}
	
	/* This function return the angle in degrees for rotate the gun for aiming a point */
	public static double findAngle(double predictedX, double predictedY, double myX, double myY) {
		double theta = Math.atan2(Math.toRadians(predictedY - myY), Math.toRadians(predictedX - myX));
		theta = Math.PI/2 - theta;
		theta = Math.toDegrees(theta);
		return theta;
	}
	
	/* This function give the MEA assuming that the angle C is 90 degrees and the enemy' s velocity is the maximum (8.0) */
	public static double maximumEscapeAngle(double bulletPower) {
		double bulletVelocity = 20 -3*bulletPower;
		double theta = Math.asin(8.0 / bulletVelocity);
		theta = Utils.normalAbsoluteAngle(theta);
		theta = Math.toDegrees(theta);
		return theta;
	}
	
	public static double getBulletSpeed(double bulletPower) {
		return 20 - 3*bulletPower;
	}
}
