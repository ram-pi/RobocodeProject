package org.pattern.movement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
			if (path == null) 
				doFallBackMovement();
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
		int NUM_ORBIT = 1;
		int direction = 1;
		double MAX_OFFSET_FROM_PERPENDICULAR = 45.;
		double stickLenght = 160;
		Point2D robotPosition = new Point2D.Double(robot.getX(), robot.getY());
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, robot.getBattleFieldWidth()-36, robot.getBattleFieldHeight()-36);
		Rectangle2D battleField = new Rectangle2D.Double(0, 0, robot.getBattleFieldWidth(), robot.getBattleFieldHeight());

		Random rand = new Random(new Date().getTime());
		LinkedList<Projection> candidatesProjections = new LinkedList<>();
		for (int i = 0; i < NUM_ORBIT*2; i++) {
			if (i == NUM_ORBIT) {
				direction = 0;

			}

			double minAngle = org.pattern.utils.Utils.absBearingPerpendicular(robotPosition, bullet.getFiringRobot().getPosition(), direction);
			double offset = rand.nextDouble() * MAX_OFFSET_FROM_PERPENDICULAR;
			double angle = minAngle + offset;


			boolean ahead = true;
			if (Math.abs(robocode.util.Utils.normalRelativeAngleDegrees(angle - robot.getHeading())) > 90.) {
				ahead = false;
				angle += 180;
			}

			Projection proj = new Projection(robotPosition,
					robot.getHeading(), 
					robot.getVelocity(), 
					ahead? 1 : -1, 
							Utils.normalRelativeAngleDegrees(angle - robot.getHeading()));

			tickProjection tick = proj.projectNextTick();

			int w = 0;
			boolean hitWall = false;
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


				double directionHeading = proj.getWantedDirection() == 1 ? tick.getHeading() : tick.getHeading() + 180;

				if (!safeBF.contains(tick.getPosition().getX()-80, tick.getPosition().getY()-80, 160, 160)) {
					double xend=tick.getPosition().getX() + Math.sin(Math.toRadians(directionHeading))*stickLenght;
					double yend=tick.getPosition().getY()+ Math.cos(Math.toRadians(directionHeading))*stickLenght;				

					if (!safeBF.contains(xend, yend)) {
						robot.out.println("Projection " + i + ": set adjustment at tick " + tick.getTick());
						proj.setTurningAdjustment((direction == 1 ? 1 : 0) * 5.);
					}
				}
				
				if (!battleField.contains(tick.getPosition())) {
					hitWall = true;
				}
				
			}

			if (!hitWall)
				candidatesProjections.add(proj);
		}

		if (candidatesProjections.isEmpty()) {
			robot.out.println("can't find suitable paht");
			return null;
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

		Random r = new Random(new Date().getTime());
		int random  = r.nextInt(candidatesProjections.size());
		robot.out.println("choosed " + random);
		return candidatesProjections.get(random);
	}

	private boolean goingPerpendicular(Point2D position, long l,
			GBulletFiredEvent bullet) {
		return false;
//		if (l > robot.getTime() + 10) {
//			return true;
//		}
//
//		double DISTANCE_TRESHOLD = 50;
//		if (position.distance(bullet.getFiringRobot().getPosition()) < DISTANCE_TRESHOLD)
//			return true;
//		return false;
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
		
		double stickLenght = 160;
		double directionHeading = robot.getVelocity() > 0 ? robot.getHeading() : robot.getHeading()+180;
		double xend=robot.getX() + Math.sin(Math.toRadians(directionHeading))*stickLenght;
		double yend=robot.getY()+ Math.cos(Math.toRadians(directionHeading))*stickLenght;
		Line2D stick = new Line2D.Double(robot.getX(), robot.getY(), xend, yend);
		
		Rectangle2D safeBF = new Rectangle2D.Double(18, 18, robot.getBattleFieldWidth()-36, robot.getBattleFieldHeight()-36);
		g.draw(safeBF);
		g.draw(stick);
		g.setColor(c);
	}

}
