package org.robot;

import org.pattern.movement.Movement;
import org.pattern.radar.Radar;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Gargoyle extends AdvancedRobot {
	private Radar radar;
	private Movement movement;
	
	public Gargoyle() {
		radar = new Radar(this);
		movement = new Movement(this);
		radar.addObserver(movement);
		
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		// TODO Auto-generated method stub
		radar.consumeScannedRobotEvent(event);
		fire(1.0);
	}
	
	@Override
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);

		while (true) {
			radar.doScan();
			execute();
		}
	}
}
	