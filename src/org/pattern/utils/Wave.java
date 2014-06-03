package org.pattern.utils;

import java.awt.geom.Point2D;

import org.robot.Enemy;

import robocode.AdvancedRobot;

public class Wave {
	private Point2D source;
	private Double velocity;
	private Long creationTime;
	private Double heading;
	
	public Wave() {}
	
	public Wave(Point2D source, Double velocity, Long creationTime, Double heading) {
		this.source = source;
		this.velocity = velocity;
		this.creationTime = creationTime;
		this.heading = heading;
	}
	
	public Boolean hitsEnemy(AdvancedRobot robot, Enemy e) {
		Point2D pos = new Point2D.Double(robot.getX(), robot.getY());
		Double dist = e.getPosition().distance(pos);
		if (velocity*(robot.getTime()-creationTime) >= dist) 
			return true;
		return false;
	}

	public Point2D getSource() {
		return source;
	}

	public Double getVelocity() {
		return velocity;
	}

	public Long getCreationTime() {
		return creationTime;
	}

	public Double getHeading() {
		return heading;
	}

	public void setSource(Point2D source) {
		this.source = source;
	}

	public void setVelocity(Double velocity) {
		this.velocity = velocity;
	}

	public void setCreationTime(Long creationTime) {
		this.creationTime = creationTime;
	}

	public void setHeading(Double heading) {
		this.heading = heading;
	}
}
