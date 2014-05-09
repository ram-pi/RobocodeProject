package org.robot;

import java.awt.Point;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Enemy implements Comparable{

	private String name;
	private double x;
	private double y;
	private double bearing;
	private double velocity;
	private double distance;
	private double heading;
	private double energy;
	private double bearingRadians;
	private double headingRadians;
	private long lastUpdated;
	private boolean dead;
	
	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public long getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Enemy() {
		
		name="";
		x=0;
		y=0;
		bearing=0.0;
		velocity=0.0;
		distance=0.0;
		heading=0.0;
		energy=0.0;
		headingRadians=0.0;
		bearingRadians=0.0;	
		dead = false;
		
	}
	
	public Enemy(ScannedRobotEvent event,AdvancedRobot robot){
		
		this.name=event.getName();
		updateEnemy(event, robot);
	}
	
	public void updateEnemy(ScannedRobotEvent event,AdvancedRobot robot){
		
		this.distance=event.getDistance();
		this.energy=event.getEnergy();
		this.bearing=event.getBearing();
		this.heading=event.getHeading();
		this.velocity=event.getVelocity();
		this.headingRadians=event.getHeadingRadians();
		this.bearingRadians=event.getBearingRadians();
		double absBearing=event.getBearingRadians()+robot.getHeadingRadians();
		x=robot.getX()+Math.sin(absBearing)*distance;
		y=robot.getY()+Math.cos(absBearing)*distance;
		dead = false;
	}
	
	public Point getPosition(){
		return new Point((int)x,(int)y);
	} 


	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getHeading() {
		return heading;
	}

	public void setHeading(double heading) {
		this.heading = heading;
	}

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getBearingRadians() {
		return bearingRadians;
	}

	public void setBearingRadians(double bearingRadians) {
		this.bearingRadians = bearingRadians;
	}

	public double getHeadingRadians() {
		return headingRadians;
	}

	public void setHeadingRadians(double headingRadians) {
		this.headingRadians = headingRadians;
	}
	@Override
	public int hashCode() {
		
		return name.hashCode();
	}

	@Override
	public int compareTo(Object o) {
		
			
		    Enemy e=(Enemy) o;
		    if(this.getName().compareTo(e.getName())==0)
		    	return 0;
			if(this.getDistance()>=e.getDistance())
				return 1;
			else
				return -1;
	}
	@Override
	public boolean equals(Object obj) {
		
		if(this==obj)
			return true;
		if(obj==null)
			return false;
		if(this.getClass()!=obj.getClass())
			return false;
		Enemy e=(Enemy) obj;
		return getName().equals(e.getName());
	}
	@Override
	public String toString() {
		
		return name;
	}
}
