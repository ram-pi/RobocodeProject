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
	
}
