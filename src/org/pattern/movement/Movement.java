package org.pattern.movement;

import java.util.Observable;
import java.util.Observer;

import org.pattern.radar.GBulletFiredEvent;

import robocode.AdvancedRobot;

public class Movement implements Observer{
	private AdvancedRobot robot;
	public Movement(AdvancedRobot robot) {
		this.robot = robot;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof GBulletFiredEvent) {
			robot.out.println("Bullet fired");
		}
		
	}
	
}
