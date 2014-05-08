package org.pattern.shooting;

import java.util.Observable;
import java.util.Observer;

import robocode.AdvancedRobot;

public class Shooting implements Observer {
	private AdvancedRobot robot;
	
	public Shooting(AdvancedRobot robot) {
		this.robot = robot;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}
}
