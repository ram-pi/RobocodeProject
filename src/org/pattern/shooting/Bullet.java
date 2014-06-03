package org.pattern.shooting;

import java.awt.geom.Point2D;

public class Bullet {
	private Point2D from;
	private Point2D actualPosition;
	private double bearing, power;
	private long time;
	private boolean passed;
	private boolean enemyFired;
	private Point2D targetPosition;
	
	public Bullet() {}
	
	public Bullet(Point2D from, double bearing, double power, long time, Point2D targetPosition) {
		this.from = from;
		this.actualPosition = from;
		this.bearing = bearing;
		this.power = power;
		this.time = time;
		this.targetPosition = targetPosition;
	}
	
	public boolean targetOnBulletLeft() {
		if (from.getX() > targetPosition.getX())
			return true;
		return false;
	}
	
	public boolean targetOnBulletRight() {
		if (from.getX() < targetPosition.getX())
			return true;
		return false;
	}
	
	public boolean targetOnBulletTop() {
		if (from.getY() < targetPosition.getY())
			return true;
		return false;
	}
	
	public boolean targetOnBulletBottom() {
		if (from.getY() > targetPosition.getY())
			return true;
		return false;
	}

	public Point2D getFrom() {
		return from;
	}

	public double getBearing() {
		return bearing;
	}

	public double getPower() {
		return power;
	}

	public long getTime() {
		return time;
	}

	public boolean isPassed() {
		return passed;
	}

	public boolean isEnemyFired() {
		return enemyFired;
	}

	public void setFrom(Point2D from) {
		this.from = from;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public void setPower(double power) {
		this.power = power;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setPassed(boolean passed) {
		this.passed = passed;
	}

	public void setEnemyFired(boolean enemyFired) {
		this.enemyFired = enemyFired;
	}

	public Point2D getActualPosition() {
		return actualPosition;
	}

	public void setActualPosition(Point2D actualPosition) {
		this.actualPosition = actualPosition;
	}

	public Point2D getTargetPosition() {
		return targetPosition;
	}

	public void setTargetPosition(Point2D targetPosition) {
		this.targetPosition = targetPosition;
	}
	
	
}
