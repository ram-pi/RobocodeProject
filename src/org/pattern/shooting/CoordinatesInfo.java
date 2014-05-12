package org.pattern.shooting;

public class CoordinatesInfo {
	private double x;
	private double y;
	private double heading;
	private double velocity;
	
	public CoordinatesInfo(double x, double y, double heading, double velocity) {
		this.x = x;
		this.y = y;
		this.heading = heading;
		this.velocity = velocity;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getHeading() {
		return heading;
	}

	public void setX(double x) {
		this.x = x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}
	
	
}
