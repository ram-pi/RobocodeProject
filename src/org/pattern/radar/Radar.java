package org.pattern.radar;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.robot.Enemy;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.HitRobotEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

public class Radar extends Observable{

	private AdvancedRobot robot;
	
	private Map<String, Enemy> enemies;
//	
//	private Map<String, VersionedDouble> energies;
//	private Map<String, Point> positions;
	
	private Enemy lockedEnemy;
	
	static long TIME_THRESHOLD = 1000;
	static int LOCKED_TIME_THREASHOLD = 10;
	
	
	public Radar(AdvancedRobot robot) {
		enemies = new HashMap<>();
		this.robot = robot;
	}
	
	public void doScan() {
		
		if(lockedEnemy == null || lockedEnemy.isDead()) {
			robot.setTurnRadarLeft(45);
			return;
		}
		
		if(robot.getTime() - lockedEnemy.getLastUpdated() > LOCKED_TIME_THREASHOLD){
			lockedEnemy = null;
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
		
		
		Enemy cachedRobot = enemies.get(event.getName());
		

		if (cachedRobot == null) {	
			cachedRobot = new Enemy(event, robot);
			
			if (lockedEnemy == null) 
				lockedEnemy = cachedRobot;
			
			enemies.put(lockedEnemy.getName(), cachedRobot);
			setChanged();
			notifyObservers(new UpdatedEnemiesListEvent(enemies));
			return;
		}
		
		
		if (lockedEnemy == null) 
			lockedEnemy = cachedRobot;
		
		double lastEnergy = cachedRobot.getEnergy();
		double currentEnergy = event.getEnergy();
	
		if (robot.getTime() - cachedRobot.getLastUpdated() < TIME_THRESHOLD && 
				(lastEnergy - currentEnergy) > 0. &&
				(lastEnergy - currentEnergy) < 3.1) {
			
				GBulletFiredEvent gBulletFiredEvent = new GBulletFiredEvent();
				gBulletFiredEvent.setFiringRobot(cachedRobot);
				gBulletFiredEvent.setEnergy(lastEnergy - currentEnergy);
				gBulletFiredEvent.setVelocity(20 - 3 * (lastEnergy - currentEnergy));
				gBulletFiredEvent.setFiringTime(robot.getTime()-1);
				gBulletFiredEvent.setFiringPosition(cachedRobot.getPosition());//TODO this or the updated one?
				setChanged();
				notifyObservers(gBulletFiredEvent);
		}
		
		cachedRobot.updateEnemy(event, robot);
		setChanged();
		notifyObservers();

	}

	public void consumeHitAnotherRobotEvent(HitRobotEvent event) {
		String name = event.getName();
		Enemy cachedRobot = getEnemies().get(name);
		
		cachedRobot.setEnergy(event.getEnergy());
		return;
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

	public void consumeRobotDeathEvent(RobotDeathEvent event) {
		Enemy enemy = enemies.get(event.getName());
		enemy.setDead(true);
	}
	
	public void consumeRobotHitEvent(BulletHitEvent event) {
		String name = event.getName();
		Enemy cachedRobot = getEnemies().get(name);
		
		cachedRobot.setEnergy(event.getEnergy());
		return;
	}
		
	public AdvancedRobot getRobot() {
		return robot;
	}

	public Map<String, Enemy> getEnemies() {
		return enemies;
	}

	public void setRobot(AdvancedRobot robot) {
		this.robot = robot;
	}

	public void setEnemies(Map<String, Enemy> enemies) {
		this.enemies = enemies;
	}

}
