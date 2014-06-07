package org.pattern.utils;

import java.awt.geom.Point2D;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.pattern.movement.Projection;
import org.pattern.movement.WaveSurfer;
import org.pattern.radar.GBulletFiredEvent;
import org.robot.Enemy;
import org.robot.Rocky;
import org.robot.ScannerRobot;

import robocode.AdvancedRobot;

public class PositionFinder {

	private Hashtable<String, Enemy> enemies;
	private AdvancedRobot robot;
	private Double w;
	private Double h;
	
	public PositionFinder(Hashtable<String, Enemy> enemies, AdvancedRobot robot) {
		this.enemies = enemies;
		this.robot = robot;
		this.w = robot.getBattleFieldWidth();
		this.h  = robot.getBattleFieldHeight();
	}
	
	public PositionFinder() {}
	
	public Point2D.Double generateRandomPoint() {
		Random rand = new Random(Double.doubleToLongBits(Math.random()));
		Double x = rand.nextDouble()*1000;
		Double y = rand.nextDouble()*1000;
		x = x%(w-40);
		y = y%(h-40);
		if (x < 30 || y < 30)
			return generateRandomPoint();
		Point2D.Double ret = new Point2D.Double(x, y);
		return ret;
	}
	
	/* Return random double between 0.0 and 1.0*/
	public Double generateRandomDouble() {
		Random rand = new Random(Double.doubleToLongBits(Math.random()));
		return rand.nextDouble();
	}
	
	public Double generateRandomAngle() {
		Random rand = new Random(Double.doubleToLongBits(Math.random()));
		Double ret = 2*Math.PI*rand.nextDouble();
		return ret;
	}
	
	public Double evaluateRisk(Point2D.Double p) {
		// Risk point
		Double eval = 0.0;
		for (String key : enemies.keySet()) {
			Enemy e = enemies.get(key);
			Double distance = p.distance(e.getPosition());
			eval += e.getEnergy() / Math.pow(distance, 2);
		}
		
		//eval += minimumDistanceFromCorner(p);
		eval += minimumDistanceFromFrame(p);
		if (isOnTheSameRect(p))
			eval += eval*0.2;
		
		List<GBulletFiredEvent> waves = null;
		if (robot instanceof ScannerRobot) {
			waves = ((ScannerRobot)robot).waves.getWaves();
		}
		else if (robot instanceof Rocky) {
			waves = ((Rocky)robot).m_waves.getWaves();
		}
		for (GBulletFiredEvent wave : waves) {
			double firingOffset = org.pattern.utils.Utils.firingOffset(wave.getFiringPosition(),
					wave.getTargetPosition(), p);
			double gf = firingOffset > 0 ? firingOffset / wave.getMaxMAE()
					: -firingOffset / wave.getMinMAE();
//			eval += ((ScannerRobot)robot).storages.get(wave.getFiringRobot().getName()).getVisits(gf);
		}
		return eval;
	}
	
	private Double minimumDistanceFromFrame(java.awt.geom.Point2D.Double p) {
		
		Double minDistanceFromX = Double.MAX_VALUE;
		Double minDistanceFromY = Double.MAX_VALUE;
		
		if (robot.getX() > robot.getBattleFieldWidth()/2) {
			minDistanceFromY = robot.getBattleFieldWidth() - robot.getX();
		} else 
			minDistanceFromY = robot.getX();
		
		if (robot.getY() > robot.getBattleFieldHeight()/2) {
			minDistanceFromX = robot.getBattleFieldHeight() - robot.getY();
		} else
			minDistanceFromX = robot.getY();
		
		return Math.min(minDistanceFromX, minDistanceFromY);
	}

	private boolean isOnTheSameRect(java.awt.geom.Point2D.Double p) {
		if (p.getX() > robot.getX()-20 && p.getX() < robot.getX()+20) 
		{
			return true;
		}
		if (p.getY() > robot.getY()-20 && p.getY() < robot.getY()+20)
		{
			return true;
		}
		
		return false;
	}

	public Point2D.Double findBestPoint(int attempt) {
		int i = 0;
		Point2D.Double ret = new Point2D.Double();
		Double minimumRisk = Double.MAX_VALUE;
		while (i < attempt) {
			i++;
			Point2D.Double tmp = this.generateRandomPoint();
			Double tmpEval = this.evaluateRisk(tmp);
			if (tmpEval < minimumRisk) {
				minimumRisk = tmpEval;
				ret = tmp;
			}
		}
		return ret;
	}
	
	public Point2D.Double findBestPointInRange(int attempt, Double distanceRequired) {
		int i = 0;
		Point2D.Double ret = new Point2D.Double();
		Double minimumRisk = Double.MAX_VALUE;
		Point2D.Double myPos = new Point2D.Double(robot.getX(), robot.getY());
		while (i < attempt) {
			i++;
			Point2D.Double tmp = this.generateRandomPoint();
			Double tmpEval = this.evaluateRisk(tmp);
			if (tmpEval < minimumRisk && myPos.distance(tmp) <= distanceRequired) {
				minimumRisk = tmpEval;
				ret = tmp;
			}
		}
		return ret;
	}
	
	public Point2D.Double findBestPointInRangeWithRandomOffset(int attempt) {
		Enemy e = findNearest();
		if (e == null) {
			return findBestPointInRange(200, 100.0);
		}
		Point2D.Double ret = new Point2D.Double();
		Double minimumRisk = Double.MAX_VALUE;
		Point2D.Double myPos = new Point2D.Double(robot.getX(), robot.getY());
		int i = 0;
		while (i < attempt) {
			i++;
			Double randomDistance = e.getDistance()*0.8 + Math.random()*100;
			randomDistance = Math.min(randomDistance, 100);
			Point2D.Double tmp = Utils.calcPoint(myPos, randomDistance, generateRandomAngle());
			Double tmpEval = evaluateRisk(tmp);
			if (tmpEval < minimumRisk && inBattlefield(tmp)) {
				minimumRisk = tmpEval;
				ret = tmp;
			}
		}
		return ret;
	}
	
	

	private boolean inBattlefield(java.awt.geom.Point2D.Double tmp) {
		if (tmp.getX() < 50 || tmp.getX() > robot.getBattleFieldWidth()-50)
			return false;
		if (tmp.getY() < 50 || tmp.getY() > robot.getBattleFieldHeight()-50)
			return false;
		return true;
	}

	public Enemy findNearest() {
		if (enemies == null)
			return null;
		
		Enemy ret = new Enemy();
		Point2D.Double myPos = new Point2D.Double(robot.getX(), robot.getY());
		Double minimum = Double.MAX_VALUE;
		for (String key : enemies.keySet()) {
			Enemy e = enemies.get(key);
			Double tmpDist = myPos.distance(e.getPosition());
			if (tmpDist < minimum) {
				ret = e;
				minimum = tmpDist; 
			}
		}
		return ret;
	}
	
	public Double minimumDistanceFromCorner (Point2D p) {
		Point2D cornerNE = new Point2D.Double(robot.getBattleFieldWidth(), robot.getBattleFieldHeight());
		Point2D cornerSE = new Point2D.Double(robot.getBattleFieldWidth(), 0);
		Point2D cornerSO = new Point2D.Double(0, 0);
		Point2D cornerNO = new Point2D.Double(0, robot.getBattleFieldHeight());
		
		Double distanceNE = p.distance(cornerNE);
		Double distanceSE = p.distance(cornerSE);
		Double distanceSO = p.distance(cornerSO);
		Double distanceNO = p.distance(cornerNO);
		
		return Math.min(Math.min(distanceNO, distanceNE), Math.min(distanceSE, distanceSO));
	}

	public Hashtable<String, Enemy> getEnemies() {
		return enemies;
	}

	public AdvancedRobot getRobot() {
		return robot;
	}

	public void setEnemies(Hashtable<String, Enemy> enemies) {
		this.enemies = enemies;
	}

	public void setRobot(AdvancedRobot robot) {
		this.robot = robot;
	}
	
	public void setBattlefield(Double w, Double h) {
		this.w = w;
		this.h = h;
	}
	
}
