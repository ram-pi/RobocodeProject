package org.pattern.movement;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.pattern.movement.Projection.tickProjection;
import org.pattern.radar.GBulletFiredEvent;
import org.robot.Enemy;

import robocode.AdvancedRobot;
import robocode.util.Utils;



public class Movement implements Observer{
	private AdvancedRobot robot;
	private List<GBulletFiredEvent> bullets;

	private List<Point2D> points;
	
	private boolean surfing = false;
	private Path path;
	
	
	public Movement(AdvancedRobot robot) {
		this.robot = robot;
		bullets = new LinkedList<>();
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg instanceof GBulletFiredEvent) {
			bullets.add((GBulletFiredEvent)arg);
		}
	}

	public void doMovement() {
		for (GBulletFiredEvent bullet : bullets) {
			if ((robot.getTime() - bullet.getFiringTime()) * bullet.getVelocity() > new Point2D.Double(robot.getX(), robot.getY()).distance(bullet.getFiringRobot().getPosition())) {
				bullets.remove(bullet);
			}
		}
		
		if (bullets.size() == 0) {
			surfing = false;
			doFallBackMovement();
			return;
		}
		
		//else
		if (surfing == false) {
			GBulletFiredEvent nearestBullet = getNearestWave();
			path = getBestPath(nearestBullet);
			path.setStartingTick(robot.getTime());
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

	

	
	private GBulletFiredEvent getNearestWave() {
		
		return bullets.get(0);
	}

	private void doFallBackMovement() {
		robot.setAhead(3.);
		return;
	}

	public Path getBestPath(GBulletFiredEvent bullet) {
		//clockwise
		int direction = 1;
		
		List<Double> randomTurningAngles = new LinkedList<>();
		Point2D orbitCenter = bullet.getFiringRobot().getPosition();
		int ticks = (int) (new Point2D.Double(robot.getX(), robot.getY()).distance(orbitCenter) / bullet.getVelocity());
		
		double startAngle = org.pattern.utils.Utils.absBearingPerpendicular(new Point2D.Double(robot.getX(), robot.getY()), orbitCenter, direction);
		randomTurningAngles.add(startAngle);
//		for (int i= 1; i< 15; i++) {
//			randomTurningAngles.add(startAngle+i*5.);
//		}
				
		
		//TODO change in a Rect2D
		Line2D up=new Line2D.Double(18,robot.getBattleFieldHeight()-18,robot.getBattleFieldWidth()-18,robot.getBattleFieldHeight()-18);
		Line2D down=new Line2D.Double(18, 18, robot.getBattleFieldWidth()-18,18);
		Line2D left=new Line2D.Double(18, 18, 18,robot.getBattleFieldHeight()-18);
		Line2D right=new Line2D.Double(robot.getBattleFieldWidth()-18, 18, robot.getBattleFieldWidth()-18,robot.getBattleFieldHeight()-18);

		points = new LinkedList<>();
		double stickLenght = 160;

		for (Double angle : randomTurningAngles) {
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
			


			for (int t = 1; t < ticks; t++) {
				tickProjection tickProjection = proj.projectNextTick();
				
				if (!(robot.getX() < 180 || robot.getX() > robot.getBattleFieldWidth() - 180 || robot.getY() < 180 || robot.getY() > robot.getBattleFieldHeight() - 180))
					continue;
				//TODO only if near wall
				double directionHeading = ahead ? tickProjection.getHeading() : tickProjection.getHeading() + 180;
				
				double xend=tickProjection.getPosition().getX()+Math.sin(Math.toRadians(directionHeading))*stickLenght;
				double yend=tickProjection.getPosition().getY()+Math.cos(Math.toRadians(directionHeading))*stickLenght;
				Line2D stick = new Line2D.Double(tickProjection.getPosition().getX(), tickProjection.getPosition().getY(), xend, yend);
				
				if(stick.intersectsLine(up)	|| stick.intersectsLine(down) || stick.intersectsLine(left) || stick.intersectsLine(right)){
					if (Math.abs(tickProjection.getHeading() - proj.getWantedHeading()) < 5.) {
						robot.out.println("set adjustment at tick " + t);
						proj.setTurningAdjustment(direction * 5.);
					}
				}
				
				//TODO check if we collide with a wall regardless of the smoothing

				
			}
			
			Path path = new Path(proj);
			path.setStartingTick(robot.getTime());

			for (tickProjection tick : proj.getProjections()) {
				points.add(tick.getPosition());
			}
			
			return path;
			
		}
		return null;
		

		
		
	}
	
	public void consumeOnPaintEvent(Graphics2D g) {

		for (GBulletFiredEvent bullet : bullets) {
   			double radius = bullet.getVelocity() * (robot.getTime() - bullet.getFiringTime());
   			
   			/* the bullet is fired from cannon that is displaced 10px from the center of the robot */
   			radius += 10;
   			
   			g.drawArc((int)(bullet.getFiringRobot().getX() - radius), (int)(bullet.getFiringRobot().getY() - radius), (int)radius*2, (int)radius*2, 0, 360);
   			g.drawRect((int)bullet.getFiringRobot().getX(), (int)bullet.getFiringRobot().getY(), 10, 10);
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
