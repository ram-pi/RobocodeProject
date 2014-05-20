package org.robot;

import java.awt.Graphics2D;

import org.pattern.movement.Movement;
import org.pattern.radar.Radar;
import org.pattern.shooting.Shooting;

import robocode.AdvancedRobot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class Gargoyle extends AdvancedRobot {
	private Radar radar;
	private Movement movement;
	private Shooting shooting;
	
	public Gargoyle() {
		radar = new Radar(this);
		movement = new Movement(this);
		shooting = new Shooting(this);
		
		radar.addObserver(movement);
		radar.addObserver(shooting);
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		// TODO Auto-generated method stub
		radar.consumeScannedRobotEvent(event);
		shooting.doShooting(event);
		
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
	
	@Override
	public void onRobotDeath(RobotDeathEvent event) {
		radar.consumeRobotDeathEvent(event);
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		// TODO Auto-generated method stub
		super.onPaint(g);
		movement.consumeOnPaintEvent(g);
		shooting.getVirtualGun().consumeOnPaint(g);
	}

	public Radar getRadar() {
		return radar;
	}

	public Movement getMovement() {
		return movement;
	}

	public Shooting getShooting() {
		return shooting;
	}

	public void setRadar(Radar radar) {
		this.radar = radar;
	}

	public void setMovement(Movement movement) {
		this.movement = movement;
	}

	public void setShooting(Shooting shooting) {
		this.shooting = shooting;
	}
}
	