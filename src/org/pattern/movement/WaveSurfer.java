package org.pattern.movement;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import org.pattern.radar.GBulletFiredEvent;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.HitByBulletEvent;

public class WaveSurfer {
	private AdvancedRobot robot;
	private List<GBulletFiredEvent> bullets;
	private double[] storage;

	private int NUM_BINS = 43;
	
	public WaveSurfer(AdvancedRobot robot) {
		this.robot = robot;
		bullets = new LinkedList<>();
		storage = new double[NUM_BINS];
	}
	public void addWave(GBulletFiredEvent bullet) {
		bullets.add(bullet);
	}
	
	public void removePassedWaves() {
		//TODO do with iterator
		LinkedList<GBulletFiredEvent> bullet_copy = new LinkedList<>();
		bullet_copy.addAll(bullets);
		for (GBulletFiredEvent bullet : bullet_copy) {
			if ((robot.getTime() - bullet.getFiringTime()) * bullet.getVelocity() > new Point2D.Double(robot.getX(), robot.getY()).distance(bullet.getFiringPosition())) {
				bullets.remove(bullet);
			}
		}
	}
	
	public GBulletFiredEvent getNearestWave() {
		double min = Double.MAX_VALUE;
		GBulletFiredEvent ret = null;
		Point2D myPos = new Point2D.Double(robot.getX(), robot.getY());
		for (GBulletFiredEvent bullet : bullets) {
			double distance = bullet.getFiringPosition().distance(myPos) - (robot.getTime() - bullet.getFiringTime()) * bullet.getVelocity();
			if (distance < min ) {
				min = distance;
				ret = bullet;
			}
		}
		return ret;
	}
	
	public List<GBulletFiredEvent> getWaves() {
		return bullets;
	}
	
	public double getDanger(double gf) {
		double danger = 0;
		int bin = (int)(gf * NUM_BINS/2);
		bin += NUM_BINS/2;

		int startBin = Math.max(bin-4, 0);
		int endBin = Math.min(bin+4, NUM_BINS);
		
		for (int i = startBin; i < endBin; i++) {
			danger += storage[i];
		}
		return danger;
	}
	
	public void consumeOnHitEvent(HitByBulletEvent event) {
		
	}
	
	public void consumeBulletHitBulletEvent(BulletHitBulletEvent event) {
		
	}
	public void hit(double gf) {
		
		int bin = (int)(gf * NUM_BINS/2.);
		bin += NUM_BINS/2;
		
		for(int i = 0; i < NUM_BINS; i++) {
			storage[i] /= 3.;
			if (i == bin) {
				storage[i] = 1;
				continue;
			}
			
			storage[i] += 1./(Math.abs(bin - i)*2);
		}
	}
}
