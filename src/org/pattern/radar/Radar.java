package org.pattern.radar;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.pattern.utils.VersionedDouble;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Radar extends Observable{

	private AdvancedRobot robot;
	private Map<String, VersionedDouble> energies;
	private Map<String, Point> positions;
	
	private String EnemyRobot;
	
	static long TIME_THRESHOLD = 1000;
	
	
	public Radar(AdvancedRobot robot) {
		energies = new HashMap<>();
		positions = new HashMap<>();
		this.robot = robot;
	}
	
	public void doScan() {
		
		if(EnemyRobot == null) {
			robot.setTurnRadarLeft(45);
			return;
		}

		
		double tan = ((double) positions.get(EnemyRobot).x - robot.getX())/((double)positions.get(EnemyRobot).y - robot.getY());
		double bearing = Math.toDegrees(Math.atan(tan));
		
		if (positions.get(EnemyRobot).y < robot.getY()){
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
		
		VersionedDouble lastSeenEnergy = energies.get(event.getName());
		VersionedDouble currentEnergy = new VersionedDouble();
		currentEnergy.value = event.getEnergy();
		currentEnergy.time = robot.getTime();
		
		
		if (lastSeenEnergy != null && currentEnergy.value < lastSeenEnergy.value) {
			if (currentEnergy.time - lastSeenEnergy.time > TIME_THRESHOLD){}
				GBulletFiredEvent gBulletFiredEvent = new GBulletFiredEvent();
				setChanged();
				notifyObservers(gBulletFiredEvent);
		}
		
		energies.put(event.getName(), currentEnergy);
		
		if (EnemyRobot == null)
			EnemyRobot = event.getName();
		
		Point enemyPosition = calculateEnemyPosition(event);
		
		positions.put(EnemyRobot, enemyPosition);

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
