package org.pattern.utils;

import java.awt.geom.Point2D;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import org.robot.Enemy;
import org.robot.TheTester;
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
		
		eval += minimumDistanceFromCorner(p);
		if (isOnTheSameRect(p))
			eval *= 0.4;
		
		return eval;
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

	private Double findDistanceFromY(java.awt.geom.Point2D.Double p) {
		Double ret;
		if (p.getX() > robot.getBattleFieldWidth()/2) {
			ret = robot.getBattleFieldWidth() - p.getX();
		} else {
			ret = p.getX();
		}
		return ret;
	}

	private Double findDistanceFromX(java.awt.geom.Point2D.Double p) {
		Double ret;
		if (p.getY() > robot.getBattleFieldHeight()/2) {
			ret = robot.getBattleFieldHeight() - p.getY();
		} else {
			ret = p.getY();
		}
		return ret;
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
			Double randomDistance = generateRandomDouble()*e.getDistance();
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
		if (tmp.getX() < 30 || tmp.getX() > robot.getBattleFieldWidth()-30)
			return false;
		if (tmp.getY() < 30 || tmp.getY() > robot.getBattleFieldHeight()-30)
			return false;
		return true;
	}

	private Enemy findNearest() {
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
	
	public static void main(String[] args) {
		PositionFinder p = new PositionFinder();
		p.setBattlefield(800.0, 600.0);
		int i = 0;
		while (i < 10) {
			i++;
			System.out.println(p.generateRandomAngle());
		}
	}
	
}
