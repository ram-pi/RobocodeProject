package myrobot;

import java.awt.Point;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Enemy implements Comparable{

	private String nameEnemy;
	private double xPosition;
	private double yPosition;
	private double bearing;
	private double velocity;
	private double distance;
	private double heading;
	private double energy;
	private double bearingRadians;
	private double headingRadians;
	
	public Enemy() {
		
		nameEnemy="";
		xPosition=0;
		yPosition=0;
		bearing=0.0;
		velocity=0.0;
		distance=0.0;
		heading=0.0;
		energy=0.0;
		headingRadians=0.0;
		bearingRadians=0.0;	
		
	}
	
	public Enemy(ScannedRobotEvent event,AdvancedRobot robot){
		
		this.nameEnemy=event.getName();
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
		xPosition=robot.getX()+Math.sin(absBearing)*distance;
		yPosition=robot.getY()+Math.cos(absBearing)*distance;
		
	}
	
	public Point getPosition(){
		return new Point((int)xPosition,(int)yPosition);
	} 

	public String getNameEnemy() {
		return nameEnemy;
	}

	public void setNameEnemy(String nameEnemy) {
		this.nameEnemy = nameEnemy;
	}

	public double getxPosition() {
		return xPosition;
	}

	public void setxPosition(int xPosition) {
		this.xPosition = xPosition;
	}

	public double getyPosition() {
		return yPosition;
	}

	public void setyPosition(int yPosition) {
		this.yPosition = yPosition;
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
		
		return nameEnemy.hashCode();
	}

	@Override
	public int compareTo(Object o) {
		
			
		    Enemy e=(Enemy) o;
		    if(this.getNameEnemy().compareTo(e.getNameEnemy())==0)
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
		return getNameEnemy().equals(e.getNameEnemy());
	}
	@Override
	public String toString() {
		
		return nameEnemy;
	}
}
