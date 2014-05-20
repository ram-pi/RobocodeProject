package org.pattern.movement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.Timer;

import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.robot.Enemy;

import robocode.AdvancedRobot;
import robocode.BulletHitBulletEvent;
import robocode.util.Utils;



public class Movement implements Observer{
	private AdvancedRobot robot;
	private List<Point2D> points = new LinkedList<>();

	private boolean surfing = false;
	private Path path;

	private WaveSurfer waveSurfer;

	public Movement(AdvancedRobot robot) {
		this.robot = robot;
		waveSurfer = new WaveSurfer(robot);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof GBulletFiredEvent) {
			waveSurfer.addWave((GBulletFiredEvent)(arg));
		}
	}

	public void doMovement() {
		waveSurfer.removePassedWaves();

		if (waveSurfer.getWaves().size() == 0) {
			surfing = false;
			doFallBackMovement();
			return;
		}

		//else
		if (surfing == false) {
			GBulletFiredEvent nearestBullet = waveSurfer.getNearestWave();
			path = getBestPath(nearestBullet);
			path.setStartingTick(robot.getTime());
			path.init(robot);
			surfing = true;
		}

		if (surfing) {
			if (!path.isEnded()) {
				path.followPath(robot);
			} else {
				surfing = false;
			}
			return;
		}
	}

	private void doFallBackMovement() {
		robot.setAhead(3.);
		return;
	}

	private boolean insterctWithWave(Point2D position, long time, GBulletFiredEvent wave) {
		double INTERSECT_THRESHOLD = 30;
		if (Math.abs(position.distance(wave.getFiringPosition()) - (time - wave.getFiringTime()) * wave.getVelocity()) < INTERSECT_THRESHOLD)
			return true;
		return false;
	}
	
	public Path getBestPath(GBulletFiredEvent bullet) {
		//clockwise
		int NUM_ORBIT = 5;
		int direction = 1;
		double MAX_OFFSET_FROM_PERPENDICULAR = 45.;

		//TODO add firing position 
		double minAngle = org.pattern.utils.Utils.absBearingPerpendicular(new Point2D.Double(robot.getX(), robot.getY()), bullet.getFiringRobot().getPosition(), 1);
		Random rand = new Random(new Date().getTime());
		LinkedList<Projection> candidatesProjections = new LinkedList<>();
		for (int i = 0; i < 5; i++) {
			double offset = rand.nextDouble() * MAX_OFFSET_FROM_PERPENDICULAR;
			double angle = minAngle + offset;

			boolean ahead = true;
			if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - robot.getHeading())) > 90.) {
				ahead = false;
				angle += 180;
			}

			Projection proj = new Projection(new Point2D.Double(robot.getX(), robot.getY()),
					robot.getHeading(), 
					robot.getVelocity(), 
					ahead? 1 : -1, 
							Utils.normalRelativeAngleDegrees(angle - robot.getHeading()));

			tickProjection tick = proj.projectNextTick();
			
			int w = 0;
			while (!insterctWithWave(tick.getPosition(), tick.getTick()+robot.getTime(), bullet)) {
				w++;
				if (w > 200) {
					robot.out.println("WARNING: CANT FIND INTERSECTION WITH WAVE!");
					break;
				}
				tick = proj.projectNextTick();

				if (goingPerpendicular(tick.getPosition(),  tick.getTick()+robot.getTime(), bullet)) {
					
					double absPerpendicular = org.pattern.utils.Utils.absBearingPerpendicular(tick.getPosition(), bullet.getFiringRobot().getPosition(), direction);
					double adjustment = Utils.normalRelativeAngleDegrees(absPerpendicular - proj.getWantedHeading());
					proj.setTurningAdjustment(adjustment);
					
				}
			}
			candidatesProjections.add(proj);
		}
		
		Projection ret = getLessDangerous(candidatesProjections);
		//debug
		points = new LinkedList<>();
		
		for (tickProjection tick : ret.getProjections()) {
			points.add(tick.getPosition());
		}
		
		
		Path p = new Path(ret);
		return p;

//
//		List<Double> randomTurningAngles = new LinkedList<>();
//		Point2D orbitCenter = bullet.getFiringRobot().getPosition();
//		int ticks = (int) (new Point2D.Double(robot.getX(), robot.getY()).distance(orbitCenter) / bullet.getVelocity());
//
//		double startAngle = org.pattern.utils.Utils.absBearingPerpendicular(new Point2D.Double(robot.getX(), robot.getY()), orbitCenter, direction);
//		randomTurningAngles.add(startAngle);
//		//		for (int i= 1; i< 15; i++) {
//		//			randomTurningAngles.add(startAngle+i*5.);
//		//		}
//
//
//		//TODO change in a Rect2D
//		Line2D up=new Line2D.Double(18,robot.getBattleFieldHeight()-18,robot.getBattleFieldWidth()-18,robot.getBattleFieldHeight()-18);
//		Line2D down=new Line2D.Double(18, 18, robot.getBattleFieldWidth()-18,18);
//		Line2D left=new Line2D.Double(18, 18, 18,robot.getBattleFieldHeight()-18);
//		Line2D right=new Line2D.Double(robot.getBattleFieldWidth()-18, 18, robot.getBattleFieldWidth()-18,robot.getBattleFieldHeight()-18);
//
//		points = new LinkedList<>();
//		double stickLenght = 160;
//
//		for (Double angle : randomTurningAngles) {
//			boolean ahead = true;
//			if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - robot.getHeading())) > 90.) {
//				ahead = false;
//				angle += 180;
//			}
//
//			Projection proj = new Projection(new Point2D.Double(robot.getX(), robot.getY()),
//					robot.getHeading(), 
//					robot.getVelocity(), 
//					ahead? 1 : -1, 
//							Utils.normalRelativeAngleDegrees(angle - robot.getHeading()));
//
//
//
//			for (int t = 1; t < ticks; t++) {
//				tickProjection tickProjection = proj.projectNextTick();
//
//				if (!(robot.getX() < 180 || robot.getX() > robot.getBattleFieldWidth() - 180 || robot.getY() < 180 || robot.getY() > robot.getBattleFieldHeight() - 180))
//					continue;
//				//TODO only if near wall
//				double directionHeading = ahead ? tickProjection.getHeading() : tickProjection.getHeading() + 180;
//
//				double xend=tickProjection.getPosition().getX()+Math.sin(Math.toRadians(directionHeading))*stickLenght;
//				double yend=tickProjection.getPosition().getY()+Math.cos(Math.toRadians(directionHeading))*stickLenght;
//				Line2D stick = new Line2D.Double(tickProjection.getPosition().getX(), tickProjection.getPosition().getY(), xend, yend);
//
//				if(stick.intersectsLine(up)	|| stick.intersectsLine(down) || stick.intersectsLine(left) || stick.intersectsLine(right)){
//					if (Math.abs(tickProjection.getHeading() - proj.getWantedHeading()) < 5.) {
//						robot.out.println("set adjustment at tick " + t);
//						proj.setTurningAdjustment(direction * 5.);
//					}
//				}
//
//				//TODO check if we collide with a wall regardless of the smoothing
//
//
//			}
//
//			Path path = new Path(proj);
//			path.setStartingTick(robot.getTime());
//
//			for (tickProjection tick : proj.getProjections()) {
//				points.add(tick.getPosition());
//			}
//
//			return path;
//
//		}
//		return null;




	}

	private Projection getLessDangerous(
			LinkedList<Projection> candidatesProjections) {

		return candidatesProjections.get(0);
	}

	private boolean goingPerpendicular(Point2D position, long l,
			GBulletFiredEvent bullet) {
		
		if (l > robot.getTime() + 10) {
			return true;
		}
		
		double DISTANCE_TRESHOLD = 50;
		if (position.distance(bullet.getFiringRobot().getPosition()) < DISTANCE_TRESHOLD)
			return true;
		return false;
	}

	public void consumeOnPaintEvent(Graphics2D g) {

		for (GBulletFiredEvent bullet : waveSurfer.getWaves()) {
			double radius = bullet.getVelocity() * (robot.getTime() - bullet.getFiringTime());

			/* the bullet is fired from cannon that is displaced 10px from the center of the robot */
			radius += 10;

			g.drawArc((int)(bullet.getFiringPosition().getX() - radius), (int)(bullet.getFiringPosition().getY() - radius), (int)radius*2, (int)radius*2, 0, 360);
			g.drawRect((int)bullet.getFiringPosition().getX() - 5, (int)bullet.getFiringPosition().getY() - 5, 10, 10);
		}

		for (Point2D point : points) {
			g.drawRect((int)point.getX()-2, (int)point.getY()-2, 4, 4);
		}

		Color c = g.getColor();
		g.setColor(new Color(0, 255, 255));
		g.drawRect((int)robot.getX()-4, (int)robot.getY()-4, 8, 8);
		g.setColor(c);
	}

}
