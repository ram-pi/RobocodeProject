package org.pattern.utils;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Date;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import org.pattern.movement.Move;
import org.pattern.movement.Projection;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.robot.Enemy;
import org.robot.Rocky;

import com.sun.org.apache.bcel.internal.Constants;

import robocode.AdvancedRobot;


public class Utils {
	


	public static double Max(double a, double b) {
		return a>b?a:b;
	}

	public static double Min(double a, double b) {
		return a>b?b:a;
	}

	public static double absBearing(Point2D a, Point2D b) {
		double theta = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());
		theta = Math.PI/2 - theta;
		theta = Math.toDegrees(theta);
		return theta; 
	}

	public static double absBearingPerpendicular (Point2D a, Point2D b, int direction) {
		double theta = robocode.util.Utils.normalAbsoluteAngleDegrees((Utils.absBearing(a, b) - 90));

		
		//Clockwise

		if (direction == 1 && ((a.getX() < b.getX() && theta > 90 && theta < 270) || 
				(a.getX() > b.getX() && (theta > 0 && theta < 90 || theta > 270 && theta < 360))) ) 
					theta += 180;
		if (direction == -1 && ((a.getX() < b.getX() && (theta > 0 && theta < 90 || theta > 270 && theta < 360)) ||
				a.getX() > b.getX() && theta > 90 && theta < 270))
				theta += 180;
	
		return robocode.util.Utils.normalAbsoluteAngleDegrees(theta);
	}
	
	public static int WallSmoothing(Projection proj, int turningDirection, Rectangle2D.Double rect, int numberOfTicks) {

		boolean hitWall = true;
		double angle = 0;
		int tries = 0;

		while (hitWall) {
			proj.setBearingOffset(angle);
			proj.init();
			
			hitWall = false;
			for (int t=0;t < numberOfTicks; t++) {
				tickProjection tick = proj.projectNextTick();
				if (!rect.contains(tick.getPosition())) {
					hitWall = true;
					angle += 2 * turningDirection;
					break;
				}
			}
			
			if (tries > 180) {
				return -1;
			}
			tries++;
		}
		return 0;
	}
	
	public static Point2D.Double calcPoint(Point2D p, double dist, double ang) {
		return new Point2D.Double(p.getX() + dist*Math.sin(ang), p.getY() + dist*Math.cos(ang));
	}

	public static double calcAngle(Point2D p2,Point2D p1){
		return Math.atan2(p2.getX() - p1.getX(), p2.getY() - p1.getY());
	}

	public static Boolean pointInBattlefield(AdvancedRobot robot, Point2D p) {
		if (p.getX() > robot.getBattleFieldWidth() || p.getX() < 0 || p.getY() > robot.getBattleFieldHeight() || p.getY() < 0)
			return false;
		return true;
	}
	

	public static double firingOffset(Point2D firingPosition, Point2D targetPosition,
			Point2D hitPosition) {
		double firingBearing = robocode.util.Utils
				.normalAbsoluteAngleDegrees(Utils.absBearing(firingPosition,
						hitPosition));
		double bearing = robocode.util.Utils.normalAbsoluteAngleDegrees(Utils
				.absBearing(firingPosition, targetPosition));

		double ret;
		if (firingBearing > bearing)
			ret = firingBearing - bearing;
		else
			ret = -(bearing - firingBearing);

		return robocode.util.Utils.normalRelativeAngleDegrees(ret);
	}
	
	public static List<Point2D> generatePoints(AdvancedRobot robot, Enemy enemy) {

		double ENEMY_DISTANCE = 0.8;
		List<Point2D> points = new LinkedList<>();
		Point2D myPosition  = new Point2D.Double(robot.getX(), robot.getY());
		double bearing;
		
		for (int i = 0; i < Costants.SURFING_NUM_POINTS; i++) {
			bearing = robocode.util.Utils.normalAbsoluteAngleDegrees(robot.getHeading() + (360 / Costants.SURFING_NUM_POINTS) * i);
			double distance = Math.min(ENEMY_DISTANCE*enemy.getDistance(), Costants.SURFING_MAX_POINT_DIST);
			points.add(calcPoint(myPosition, distance, bearing));
		}
		
		return points;
	}
	
	public static List<Point2D> generatePointsPerpendicular(AdvancedRobot robot, Enemy enemy) {
		List<Point2D> ret = null;
		int numPoints = (int)Costants.SURFING_NUM_POINTS;
		Point2D myPos = new Point2D.Double(robot.getX(), robot.getY());	
		for (int i = 0; i < numPoints; i++) {
			
		}
		return ret;
	}
	
	public static double getProjectedGF(AdvancedRobot robot, GBulletFiredEvent wave, Point2D toGo) {
		Point2D myPosition = new Point2D.Double(robot.getX(), robot.getY());

		double angle = robocode.util.Utils.normalAbsoluteAngleDegrees(absBearing(myPosition, toGo));
		Move m = new Move(robot);
		m.move(angle, robot.getHeading());
		
		
		Projection projection = new Projection(myPosition, robot.getHeading(), robot.getVelocity(), m.ahead, m.turnRight);


		tickProjection tick = projection.projectNextTick();
		int timeElapsed = (int) (robot.getTime() - wave.getFiringTime());

		while (tick.getPosition().distance(wave.getFiringPosition()) > (timeElapsed + tick
				.getTick()) * wave.getVelocity()) {
			tick = projection.projectNextTick();

			if (m.smooth(tick.getPosition(), tick.getHeading(),
					projection.getWantedHeading(), m.ahead)) {
				projection.setWantedDirection(m.ahead);
				projection.setWantedHeading(tick.getHeading() + m.turnRight);
			}
		}

		double firingOffset = firingOffset(wave.getFiringPosition(), wave.getTargetPosition(), tick.getPosition());

		double _mae = firingOffset > 0 ? wave.getMaxMAE() : wave.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset / _mae : -firingOffset
				/ _mae;

		return gf;
	}
	
	public static double calculateGF(GBulletFiredEvent wave, Point2D point) {
		double firingOffset = firingOffset(wave.getFiringPosition(),
				wave.getTargetPosition(), point);

		double _mae = firingOffset > 0 ? wave.getMaxMAE() : wave.getMinMAE();
		double gf = firingOffset > 0 ? firingOffset / _mae : -firingOffset
				/ _mae;
		
		return gf;
	}
	
	public static double getDistanceFromWall(Point2D position, double width, double height) {
		return Math.max(Math.abs(position.getX() - width), Math.abs(position.getY()- height));
	}
	
	public static double getLateralVelocity(Point2D position, Point2D target, double velocity, double heading) {
		
		double angle = absBearingPerpendicular(target, position, 1);
		if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - heading)) > 90) {
			angle += 180;
		}
		
		angle =  Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - heading));
		double latv = Math.cos(Math.toRadians(angle)) * velocity;
		return latv;
	}
	
	public static void setMeasure(double value, double maxValue, int startIndex, BitSet bitSet) {
		for (int i = 0; i < Costants.SEG_BITS_VARIABLE; i++) {
			if(value > (maxValue/Costants.SEG_BITS_VARIABLE)*i)
				bitSet.set(i+startIndex);
		}
	}
	public static BitSet getSnapshot(AdvancedRobot robot, Enemy enemy) {
		BitSet ret = new BitSet();

		
		Point2D myPos = new Point2D.Double(robot.getX(), robot.getY());
		double maxDistance = Math.max(robot.getBattleFieldHeight(), robot.getBattleFieldWidth());

//		setMeasure(Math.abs(robot.getVelocity()), 8., 0, ret);
//		setMeasure(Math.abs(enemy.getVelocity()), 8., Costants.SEG_BITS_VARIABLE, ret);
		//setMeasure(wave.getVelocity(), 20., Costants.SEG_BITS_VARIABLE*2, ret);
		int bits = 0;
		setMeasure(enemy.getPosition().distance(robot.getX(), robot.getY()), maxDistance, bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(getDistanceFromWall(enemy.getPosition(), robot.getBattleFieldWidth(), robot.getBattleFieldHeight()), maxDistance, bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(getLateralVelocity(myPos, enemy.getPosition(), enemy.getVelocity(), enemy.getHeading()), 8., bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(getLateralVelocity(enemy.getPosition(), myPos, robot.getVelocity(), robot.getHeading()), 8., bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(enemy.getEnergy(), 100., bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(enemy.getLastTimeDecel(), 30., bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(((Rocky)robot).o_lastTimeDecel, 30., bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		
		return ret;
	}
	
	public static BitSet makeSnapshot(double distance, double distanceFromWall, double enemyLateralVel, double myLateralVelo, double energy, double lastTimeDecel, double myTimedecel) {
		BitSet ret = new BitSet();

		

		//setMeasure(wave.getVelocity(), 20., Costants.SEG_BITS_VARIABLE*2, ret);
		int bits = 0;
		setMeasure(distance, Math.sqrt(800*800+600*600), bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(distanceFromWall, Math.sqrt(800*800+600*600), bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
//		setMeasure(enemyLateralVel, 8., bits, ret);
//		bits+=Costants.SEG_BITS_VARIABLE;
//		setMeasure(myLateralVelo, 8., bits, ret);
//		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(energy, 100., bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(lastTimeDecel, 30., bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		setMeasure(myTimedecel, 30., bits, ret);
		bits+=Costants.SEG_BITS_VARIABLE;
		
		return ret;
	}
	
	public static double getDanger(double gf, double absMae, VisitCountStorageDensity storage, GBulletFiredEvent wave) {

	
		double botWidth = Math.toDegrees(36/wave.getFiringPosition().distance(wave.getTargetPosition()));
		double angle = gf * absMae;
		double d = 0;
		for (Double _gf : storage.getStorage()) {
			double thisAngle =  _gf * absMae;
			
			double ux = (angle - thisAngle) / botWidth;
			d += Math.pow(Math.E, - 0.5 * ux * ux);
		}
		
		return d;
	}
	public static double getDanger(double gf, double absMae, VisitCountStorageSegmented storage, GBulletFiredEvent wave) {
		List<BitSet> Allnearest = storage.getNearest(wave.getSnapshot());
		
		List<BitSet> nearest = new LinkedList<>();
		int k = Math.min(Costants.KNN_K, Allnearest.size());
		for (int i = 0; i < k; i++) {
			nearest.add(Allnearest.get(i));
		}
		
		DecimalFormat f = new DecimalFormat("#0.00");
		System.out.print("GF: ");
		for (int i = 0; i < k; i++) {
			double _gf = storage.getGF(nearest.get(i));
			double a = _gf * absMae;
			System.out.print(f.format(_gf) + "("+f.format(a)+") ");
		}
		
	
		double botWidth = Math.toDegrees(36/wave.getFiringPosition().distance(wave.getTargetPosition()));
		double angle = gf * absMae;
		double d = 0;
		for (BitSet bitSet : nearest) {
			double thisGf = storage.getGF(bitSet);
			double thisAngle =  thisGf * absMae;
			
			double ux = (angle - thisAngle) / botWidth;
			d += Math.pow(Math.E, - 0.5 * ux * ux)/(1 + storage.distance(bitSet, wave.getSnapshot()));
		}
		
		System.out.println("D "+f.format(gf)+"("+f.format(gf*absMae)+") "+f.format(d));
		return d;
	}
	
	public static double getMAE(Point2D firingPosition, Point2D targetPosition,
			double targetHeading, double targetVelocity, double waveVelocity,
			int cw, AdvancedRobot robot) {

		double angle = org.pattern.utils.Utils.absBearingPerpendicular(
				targetPosition, firingPosition, cw);
		Move m = new Move(robot);
		m.move(angle, targetHeading);
		// TODO use values 2 ticks before detecting the wave
		Projection projection = new Projection(targetPosition, targetHeading,
				targetVelocity, m.ahead, m.turnRight);

		tickProjection tick = projection.projectNextTick();
		double tempMae = cw == -1 ? Double.MAX_VALUE : Double.MIN_VALUE;

		while (tick.getPosition().distance(firingPosition) > tick.getTick()
				* waveVelocity) {
			tick = projection.projectNextTick();

			if (cw == -1) {
				tempMae = Math.min(
						tempMae,
						firingOffset(firingPosition, targetPosition,
								tick.getPosition()));
			} else {
				tempMae = Math.max(
						tempMae,
						firingOffset(firingPosition, targetPosition,
								tick.getPosition()));
			}

			if (m.smooth(tick.getPosition(), tick.getHeading(),
					projection.getWantedHeading(), m.ahead)) {
				projection.setWantedDirection(m.ahead);
				projection.setWantedHeading(tick.getHeading() + m.turnRight);
			}
		}

		return tempMae;
	}

	
	public static double getFiringAngle(VisitCountStorageDensity storage, Point2D myPosition, Enemy enemy, double firePower, BitSet snapshot, AdvancedRobot robot) {
		double botWidth = Math.toDegrees(36/enemy.getDistance());
		double maxDensity = Double.MIN_VALUE;
		double ret = 0;
		double mae;
		for (Double _gf: storage.getStorage()) {
			double d = 0;
			
			
			int cw = 0;
			if (_gf > 0) {
				cw = 1;
			} else {
				cw = -1;
			}
			mae = Math.abs(getMAE(myPosition, enemy.getPosition(), enemy.getHeading(),
					enemy.getVelocity(), 20 - firePower * 3, cw, robot));
			double angle = _gf * mae;
			
			for(Double gf: storage.getStorage()) {
				if (gf == _gf)
					continue;
				

				if (_gf > 0) {
					cw = 1;
				} else {
					cw = -1;
				}
				mae = Math.abs(getMAE(myPosition, enemy.getPosition(), enemy.getHeading(),
						enemy.getVelocity(), 20 - firePower * 3, cw, robot));
				double angle1 = _gf * mae;
				
				if (Math.abs(angle1 - angle) < botWidth) {
					d++;
				}
			}
			if (d > maxDensity)  {
				maxDensity = d;
				ret = angle;
			}
		}
		return ret;
	}

	
	public static double getFiringAngle(VisitCountStorageSegmented storage, Point2D myPosition, Enemy enemy, double firePower, BitSet snapshot, AdvancedRobot robot) {
		

		List<BitSet> Allnearest = storage.getNearest(snapshot);


		List<BitSet> nearest = new LinkedList<>();
		int k = Math.min(Costants.KNN_K, Allnearest.size());
		for (int i = 0; i < k; i++) {
			nearest.add(Allnearest.get(i));
		}
		
		double botWidth = Math.toDegrees(36/enemy.getDistance());
		double maxDensity = Double.MIN_VALUE;
		double ret = 0;
		double mae;
		for (BitSet bs1 : nearest) {
			double d = 0;
			double gf = storage.getGF(bs1);
			
			int cw = 0;
			if (gf > 0) {
				cw = 1;
			} else {
				cw = -1;
			}
			mae = Math.abs(getMAE(myPosition, enemy.getPosition(), enemy.getHeading(),
					enemy.getVelocity(), 20 - firePower * 3, cw, robot));
			double angle = gf * mae;
			
			for(BitSet bs2 : nearest) {
				if (bs1 == bs2)
					continue;
				
				double gf1 = storage.getGF(bs1);
				if (gf1 > 0) {
					cw = 1;
				} else {
					cw = -1;
				}
				mae = Math.abs(getMAE(myPosition, enemy.getPosition(), enemy.getHeading(),
						enemy.getVelocity(), 20 - firePower * 3, cw, robot));
				double angle1 = gf1 * mae;
				
				if (Math.abs(angle1 - angle) < botWidth) {
					d++;
				}
			}
			if (d > maxDensity)  {
				maxDensity = d;
				ret = angle;
			}
		}
		return ret;
	}


	
	public static void setWaveMAE(GBulletFiredEvent wave, double heading,
			double velocity, AdvancedRobot robot) {

		double mae[] = new double[2];
		for (int orbitDirection = -1; orbitDirection < 2; orbitDirection += 2) {

			mae[orbitDirection == -1 ? 0 : 1] = getMAE(	wave.getFiringPosition(), wave.getTargetPosition(),
					heading, velocity, wave.getVelocity(), orbitDirection, robot);
		}
		wave.setMinMAE(Math.min(mae[0], mae[1]));
		wave.setMaxMAE(Math.max(mae[0], mae[1]));
		return;
	}
	
	public static double gunOffset(Point2D guessPosition,AdvancedRobot robot){
		
		double gunOffset = robot.getGunHeadingRadians() - (Math.PI/2 - Math.atan2(guessPosition.getY() - robot.getY(), guessPosition.getX() - robot.getX()));
		return robocode.util.Utils.normalRelativeAngle(gunOffset);

	}
	
	public static Point2D getFuturePoint(Enemy e, long when) {
		double x = e.getX() + Math.sin(e.getHeadingRadians())*e.getVelocity()*when;
		double y = e.getY() + Math.cos(e.getHeadingRadians())*e.getVelocity()*when;
		return new Point2D.Double(x, y);
	}
	
 }
