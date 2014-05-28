package org.pattern.movement;

import java.awt.geom.Point2D;
import java.util.Date;
import java.util.Hashtable;
import java.util.Random;

import org.robot.Enemy;

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
		x = x%(w-60);
		y = y%(h-60);
		if (x < 30 || y < 30)
			return generateRandomPoint();
		Point2D.Double ret = new Point2D.Double(x, y);
		return ret;
	}
	
	public Double evaluateRisk(Point2D.Double p) {
		Double eval = 0.0;
		for (String key : enemies.keySet()) {
			Enemy e = enemies.get(key);
			Double distance = p.distance(e.getPosition());
			eval += 1 / Math.pow(distance, 2);
		}
		return eval;
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
			if (tmpEval < minimumRisk && myPos.distance(tmp) >= distanceRequired) {
				minimumRisk = tmpEval;
				ret = tmp;
			}
		}
		return ret;
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
			System.out.println(p.generateRandomPoint());
		}
	}
	
}
