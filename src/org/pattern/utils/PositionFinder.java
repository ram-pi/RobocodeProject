package org.pattern.utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.pattern.movement.Move;
import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.robot.Enemy;
import org.robot.Rocky;

import robocode.AdvancedRobot;

public class PositionFinder {

	private Hashtable<String, Enemy> enemies;
	private AdvancedRobot robot;
	private Double w;
	private Double h;
	private Double maxDistance;
	private Point2D lastPosition, lastLastPosition, actualPosition;	

	public PositionFinder(Hashtable<String, Enemy> enemies, AdvancedRobot robot) {
		this.enemies = enemies;
		this.robot = robot;
		this.w = robot.getBattleFieldWidth();
		this.h  = robot.getBattleFieldHeight();
		this.maxDistance = new Point2D.Double(0, 0).distance(new Point2D.Double(w, h));
		this.lastPosition = this.lastLastPosition = new Point2D.Double(robot.getX(), robot.getY());
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
			//eval += e.getEnergy() / Math.pow(distance, 2);
			Double enemyfactor = Math.cos(Utils.calcAngle(actualPosition, p)-Utils.calcAngle(e.getPosition(), p));
			enemyfactor = 1 + Math.abs(enemyfactor)/p.distanceSq(e.getPosition());
			eval += enemyfactor;
		}

		//eval += minimumDistanceFromCorner(p);
		eval += minimumDistanceFromFrame(p)/maxDistance;
		//		eval += 1/p.distance(lastPosition);
		//		eval += 1/p.distance(lastLastPosition);
		//		if (isOnTheSameRect(p))
		//			eval += eval*1.2;
		//		
		return eval;
	}

	private double riskFromWave(GBulletFiredEvent wave, Point2D point) {
		Point2D myPosition = new Point2D.Double(robot.getX(), robot.getY());

		Move m = new Move(robot);
		double absBearing = org.pattern.utils.Utils.absBearing(myPosition, point);
		m.move(absBearing, robot.getHeading());

		Projection turning = new Projection(myPosition, robot.getHeading(), robot.getVelocity(), 0, m.turnRight, 0);
		turning.setMaxVelocity(0);
		tickProjection tick = turning.projectNextTick();
		int elapsedTime = 0;
		boolean hitted = false;

		while (Math.abs(tick.getHeading() - turning.getWantedHeading()) > 0.001) {
			if (wave.getFiringPosition().distance(tick.getPosition()) < wave.getVelocity() * elapsedTime) {
				hitted = true;
				break;
			}
			tick = turning.projectNextTick();
			elapsedTime++;
		}

		Projection going = new Projection(tick.getPosition(), tick.getHeading(), tick.getVelocity(), m.ahead, 0, m.ahead*point.distance(tick.getPosition()));
		tick = going.projectNextTick();

		while (!hitted && wave.getFiringPosition().distance(tick.getPosition()) > wave.getVelocity() * elapsedTime) {
			tick = going.projectNextTick();
			elapsedTime++;
		}


		double firingOffset = org.pattern.utils.Utils.firingOffset(wave.getFiringPosition(),
				wave.getTargetPosition(), tick.getPosition());
		double gf = firingOffset > 0 ? firingOffset / wave.getMaxMAE()
				: -firingOffset / wave.getMinMAE();

		//		//debug
		//		List<Rectangle2D> toDraw = new LinkedList<>();
		//		for (tickProjection t : turning.getProjections()) {
		//			Rectangle2D r = new Rectangle2D.Double((int)t.getPosition().getX()-3, (int)t.getPosition().getY()-3, 6, 6);
		//			toDraw.add(r);
		//		}
		//		
		//		for (tickProjection t : going.getProjections()) {
		//			Rectangle2D r = new Rectangle2D.Double((int)t.getPosition().getX()-3, (int)t.getPosition().getY()-3, 6, 6);
		//			toDraw.add(r);
		//		}
		//		
		//		((Rocky)robot).o_toDraw.addAll(toDraw);

		return ((Rocky)robot).m_storages.get(wave.getFiringRobot().getName()).getVisits(gf);
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

		lastLastPosition = lastPosition;
		lastPosition = ret;
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
		lastLastPosition = lastPosition;
		lastPosition = new Point2D.Double(robot.getX(), robot.getY());
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
		while (i < attempt 
				&& !enemies.isEmpty()) {
			i++;
			Double randomDistance = e.getDistance()*0.8 + Math.random()*100;
			randomDistance = Math.min(randomDistance, 100);
			Point2D.Double tmp = Utils.calcPoint(myPos, randomDistance, generateRandomAngle());
			//Double tmpEval = evaluateRisk(tmp);
			Double tmpEval = evaluateRiskRevision(tmp);
			if (tmpEval < minimumRisk && inBattlefield(tmp)) {
				minimumRisk = tmpEval;
				ret = tmp;
			}
		}
		lastLastPosition = lastPosition;
		lastPosition = ret;
		return ret;
	}

	public Double evaluateRiskRevision(Point2D toEval) {
		double danger = 0.0;
		Set<String> keys = enemies.keySet();
		Rectangle2D field = new Rectangle2D.Double(0.0, 0.0, w, h);
		for (String key : keys) {
			Enemy e = enemies.get(key);
			danger += 0.1 / toEval.distance(field.getCenterX(), field.getCenterY());
			danger += 1 / toEval.distance(e.getPosition());
			danger += 1 / toEval.distance(lastPosition);
		}
		return danger;
	}

	private boolean inBattlefield(java.awt.geom.Point2D.Double tmp) {
		if (tmp.getX() < 50 || tmp.getX() > robot.getBattleFieldWidth()-50)
			return false;
		if (tmp.getY() < 50 || tmp.getY() > robot.getBattleFieldHeight()-50)
			return false;
		return true;
	}

	public Enemy findNearest() {
		if (enemies.isEmpty())
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
		this.w = robot.getBattleFieldWidth();
		this.h  = robot.getBattleFieldHeight();
		this.maxDistance = new Point2D.Double(0, 0).distance(new Point2D.Double(w, h));
		this.actualPosition = new Point2D.Double(robot.getX(), robot.getY());
		if (lastPosition == null || this.lastLastPosition == null) {
			this.lastPosition = this.lastLastPosition = this.actualPosition;
		}
	}

	public void setBattlefield(Double w, Double h) {
		this.w = w;
		this.h = h;
	}

	public double riskFromWaveDebug(GBulletFiredEvent wave, Point2D point) {
		Point2D myPosition = new Point2D.Double(robot.getX(), robot.getY());

		Move m = new Move(robot);
		double absBearing = org.pattern.utils.Utils.absBearing(myPosition, point);
		m.move(absBearing, robot.getHeading());

		Projection turning = new Projection(myPosition, robot.getHeading(), robot.getVelocity(), m.ahead, m.turnRight, 0);
		turning.setMaxVelocity(0);
		tickProjection tick = turning.projectNextTick();
		int elapsedTime = (int)(robot.getTime() - wave.getFiringTime());
		boolean hitted = false;

		while (Math.abs(tick.getHeading() - turning.getWantedHeading()) > 0.001) {
			if (wave.getFiringPosition().distance(tick.getPosition()) < wave.getVelocity() * elapsedTime) {
				hitted = true;
				break;
			}
			tick = turning.projectNextTick();
			elapsedTime++;
		}

		m.move(absBearing, tick.getHeading());
		Projection going = new Projection(tick.getPosition(), tick.getHeading(), tick.getVelocity(), m.ahead, 0, m.ahead*point.distance(tick.getPosition()));
		tick = going.projectNextTick();

		while (!hitted && wave.getFiringPosition().distance(tick.getPosition()) > wave.getVelocity() * elapsedTime) {
			tick = going.projectNextTick();
			elapsedTime++;
		}


		double firingOffset = org.pattern.utils.Utils.firingOffset(wave.getFiringPosition(),
				wave.getTargetPosition(), tick.getPosition());
		double gf = firingOffset > 0 ? firingOffset / wave.getMaxMAE()
				: -firingOffset / wave.getMinMAE();

		//		//debug
		List<Rectangle2D> toDraw = new LinkedList<>();
		for (tickProjection t : turning.getProjections()) {
			Rectangle2D r = new Rectangle2D.Double((int)t.getPosition().getX()-3, (int)t.getPosition().getY()-3, 6, 6);
			toDraw.add(r);
		}

		for (tickProjection t : going.getProjections()) {
			Rectangle2D r = new Rectangle2D.Double((int)t.getPosition().getX()-3, (int)t.getPosition().getY()-3, 6, 6);
			toDraw.add(r);
		}

		((Rocky)robot).o_toDraw.addAll(toDraw);

		return ((Rocky)robot).m_storages.get(wave.getFiringRobot().getName()).getVisits(gf);
	}

}
