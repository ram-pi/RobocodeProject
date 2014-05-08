package org.pattern.radar;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.pattern.utils.VersionedDouble;
import org.robot.Enemy;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Radar extends Observable{

	private AdvancedRobot robot;
	
	private Map<String, Enemy> enemies;
//	
//	private Map<String, VersionedDouble> energies;
//	private Map<String, Point> positions;
	
	private Enemy lockedEnemy;
	
	static long TIME_THRESHOLD = 1000;
	
	
	public Radar(AdvancedRobot robot) {
		enemies = new HashMap<>();
		this.robot = robot;
	}
	
	public void doScan() {
		
		if(lockedEnemy == null || lockedEnemy.isDead()) {
			robot.setTurnRadarLeft(45);
			return;
		}

		lockedEnemy = enemies.get(lockedEnemy.getName());
		
		double tan = ((double) lockedEnemy.getX() - robot.getX())/((double)lockedEnemy.getY() - robot.getY());
		double bearing = Math.toDegrees(Math.atan(tan));
		
		if (lockedEnemy.getY() < robot.getY()){
			bearing =  bearing + 180;
		}

		double scanBearing = bearing - robot.getRadarHeading();
		
		/*  -180 < scanBearing < 180 */
		while (scanBearing < -180){
			scanBearing += 360;
		}
		
		while (scanBearing > 180){
			scanBearing -= 360;
		}
		
		if (scanBearing < -45 || scanBearing > 45){
			System.out.println("mm some error too big bearing");
		}
		
		/* move somehow to init scan */
		if (scanBearing == 0)
			scanBearing = .5;
		
		if (robot.getRadarTurnRemaining() == 0)
			robot.setTurnRadarRight(scanBearing);
			
		return;
	}
	
	public void consumeScannedRobotEvent(ScannedRobotEvent event) {
		
		Enemy scannedRobot = new Enemy(event, robot);
		Enemy cachedRobot = enemies.get(event.getName());
		
		if (lockedEnemy == null) {
			lockedEnemy = scannedRobot;
		}
		
		if (cachedRobot == null) {	
			setChanged();
			notifyObservers(new UpdatedEnemiesListEvent(enemies));
			enemies.put(scannedRobot.getName(), scannedRobot);
			return;
		}
		
		double lastEnergy = cachedRobot.getEnergy();
		double currentEnergy = scannedRobot.getEnergy();
	
		if (robot.getTime() - cachedRobot.getLastUpdated() < TIME_THRESHOLD) {
				GBulletFiredEvent gBulletFiredEvent = new GBulletFiredEvent();
				setChanged();
				notifyObservers(gBulletFiredEvent);
		}
		
		enemies.put(cachedRobot.getName(), scannedRobot);

	}

	private Point calculateEnemyPosition(ScannedRobotEvent event) {
		
		
		double absBearing = event.getBearing() + robot.getHeading();
		
		Point enemyPositionPoint = new Point();
		
		/* 
		 * shouldn't be necessary 
		 */
		while(absBearing < 0)
			absBearing += 360;
		
		while(absBearing > 360)
			absBearing -= 360;
		
		enemyPositionPoint.x = (int)robot.getX() + (int)(event.getDistance() * Math.sin(Math.toRadians(absBearing)));
		enemyPositionPoint.y = (int)robot.getY() + (int)(event.getDistance() * Math.cos(Math.toRadians(absBearing)));
		
		return enemyPositionPoint;
	}

}
