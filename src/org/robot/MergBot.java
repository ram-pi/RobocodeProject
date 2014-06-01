package org.robot;
import java.awt.Graphics2D;

import org.pattern.movement.Movement;
import org.pattern.radar.Radar;
import org.pattern.shooting.Shooting;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;

public class MergBot extends AdvancedRobot {

	private Radar radar;
	private Movement movement;
	private Shooting shooting;
	
	public MergBot() {
		radar = new Radar(this);
		movement = new Movement(this);
		shooting = new Shooting(this);
		
		radar.addObserver(movement);
		radar.addObserver(shooting);
	}
	
	@Override
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		
		while(true) {
			radar.doScan();
			movement.doMovement();
			execute();
		}
	}
	
	@Override
	public void onScannedRobot(ScannedRobotEvent event) {
		radar.consumeScannedRobotEvent(event);
		shooting.doShooting(event);
	}
	
	@Override
	public void onBulletHit(BulletHitEvent event) {
		radar.consumeRobotHitEvent(event);
	}
	
	@Override
	public void onHitRobot(HitRobotEvent event) {
		radar.consumeHitAnotherRobotEvent(event);
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		//shooting.getVirtualGun().consumeOnPaint(g);
		movement.consumeOnPaintEvent(g);
	}
	
}
