package org.es.gargoyle;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import robocode.ScannedRobotEvent;

public class Radar extends Observable{

	private Gargoyle gargoyle;
	private Map<String, VersionedDouble> energies;
	private Map<String, Point> positions;
	
	private String EnemyRobot;
	
	static long TIME_THRESHOLD = 1000;
	
	
	public Radar(Gargoyle gargoyle) {
		energies = new HashMap<>();
		positions = new HashMap<>();
		this.gargoyle = gargoyle;
	}
	
	public void doScan() {
		
		if(EnemyRobot == null) {
			gargoyle.setTurnRadarLeft(45);
			return;
		}

		
		double tan = ((double) positions.get(EnemyRobot).x - gargoyle.getX())/((double)positions.get(EnemyRobot).y - gargoyle.getY());
		double bearing = Math.toDegrees(Math.atan(tan));
		
		if (positions.get(EnemyRobot).y < gargoyle.getY()){
			bearing =  bearing + 180;
		}

		double scanBearing = bearing - gargoyle.getRadarHeading();
		
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
		
		if (gargoyle.getRadarTurnRemaining() == 0)
			gargoyle.setTurnRadarRight(scanBearing);
			
		return;
	}
	
	public void consumeScannedRobotEvent(ScannedRobotEvent event) {
		
		VersionedDouble lastSeenEnergy = energies.get(event.getName());
		VersionedDouble currentEnergy = new VersionedDouble();
		currentEnergy.value = event.getEnergy();
		currentEnergy.time = gargoyle.getTime();
		
		
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
		
		
		double absBearing = event.getBearing() + gargoyle.getHeading();
		
		Point enemyPositionPoint = new Point();
		
		/* 
		 * shouldn't be necessary 
		 */
		while(absBearing < 0)
			absBearing += 360;
		
		while(absBearing > 360)
			absBearing -= 360;
		
		enemyPositionPoint.x = (int)gargoyle.getX() + (int)(event.getDistance() * Math.sin(Math.toRadians(absBearing)));
		enemyPositionPoint.y = (int)gargoyle.getY() + (int)(event.getDistance() * Math.cos(Math.toRadians(absBearing)));
		
		return enemyPositionPoint;
	}

}
