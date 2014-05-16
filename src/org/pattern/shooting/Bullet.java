package org.pattern.shooting;

import java.awt.geom.Point2D;

public class Bullet {
	private Point2D from;
	private double bearing, power;
	private int direction;
	private long time;
	
	public Bullet(Point2D from, double bearing, double power, int direction, long time) {
		this.from = from;
		this.bearing = bearing;
		this.power = power;
		this.direction = direction;
		this.time = time;
	}
}
