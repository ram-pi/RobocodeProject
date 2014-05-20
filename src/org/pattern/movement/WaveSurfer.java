package org.pattern.movement;

import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.radar.GBulletFiredEvent;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;

public class WaveSurfer {
	private AdvancedRobot robot;
	private List<GBulletFiredEvent> bullets;
	
	public WaveSurfer(AdvancedRobot robot) {
		this.robot = robot;
		bullets = new LinkedList<>();
	}
	public void addWave(GBulletFiredEvent bullet) {
		bullets.add(bullet);
	}
	
	public void removePassedWaves() {
		//TODO do with iterator
		LinkedList<GBulletFiredEvent> bullet_copy = new LinkedList<>();
		bullet_copy.addAll(bullets);
		for (GBulletFiredEvent bullet : bullet_copy) {
			if ((robot.getTime() - bullet.getFiringTime()) * bullet.getVelocity() > new Point2D.Double(robot.getX(), robot.getY()).distance(bullet.getFiringRobot().getPosition())) {
				bullets.remove(bullet);
			}
		}
	}
	
	public GBulletFiredEvent getNearestWave() {
		double min = Double.MAX_VALUE;
		GBulletFiredEvent ret = null;
		for (GBulletFiredEvent bullet : bullets) {
			if ((robot.getTime() - bullet.getFiringTime()) * bullet.getVelocity() < min ) {
				min = (robot.getTime() - bullet.getFiringTime()) * bullet.getVelocity();
				ret = bullet;
			}
		}
		return ret;
	}
	
	public List<GBulletFiredEvent> getWaves() {
		return bullets;
	}
	
	public int getDanger(Point2D intersection, GBulletFiredEvent wave) {
		return 0;
	}
	
	public void consumeOnHitEvent(HitByBulletEvent event) {
		
	}
	
	public void consumeBulletHitBulletEvent(BulletHitBulletEvent event) {
		
	}
}
