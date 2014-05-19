package org.pattern.movement;

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
			if (bullet.getFiringTime() * bullet.getVelocity() - new Point2D.Double(robot.getX(), robot.getY()).distance(bullet.getFiringRobot().getPosition()) > 50) {
				bullets.remove(bullet);
			}
		}
	}

	
	public void choosePath() {
		// return path (setAhead and setFront)
		
		//generatePathOrbitPoint(Point enemy);
		
	}
	
	
	 
	
	public List<Path> generateOrbits(Point2D orbitCenter, int ticks) {
		//clockwise
		int direction = 1;
		List<Path> ret = new LinkedList<>();

		
		List<Double> randomTurningAngles = new LinkedList<>();
		
		double startAngle = org.pattern.utils.Utils.absBearingPerpendicular(new Point2D.Double(robot.getX(), robot.getY()), orbitCenter, direction);
		randomTurningAngles.add(startAngle);
//		for (int i= 1; i< 15; i++) {
//			randomTurningAngles.add(startAngle+i*5.);
//		}
				
		
		//TODO change in a Rect2D
		Line2D up=new Line2D.Double(0,robot.getBattleFieldHeight(),robot.getBattleFieldWidth(),robot.getBattleFieldHeight());
		Line2D down=new Line2D.Double(0, 0, robot.getBattleFieldWidth(),0);
		Line2D left=new Line2D.Double(0, 0, 0,robot.getBattleFieldHeight());
		Line2D right=new Line2D.Double(robot.getBattleFieldWidth(), 0, robot.getBattleFieldWidth(),robot.getBattleFieldHeight());

		points = new LinkedList<>();
		double stickLenght = 151;

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
			


			for (int t = 0; t < ticks; t++) {
				tickProjection tickProjection = proj.projectNextTick();
				
				//TODO only if near wall
				double directionHeading = ahead ? tickProjection.getHeading() : tickProjection.getHeading() + 180;
				
				double xend=tickProjection.getPosition().getX()+Math.sin(Math.toRadians(directionHeading))*stickLenght;
				double yend=tickProjection.getPosition().getY()+Math.cos(Math.toRadians(directionHeading))*stickLenght;
				Line2D stick = new Line2D.Double(tickProjection.getPosition().getX(), tickProjection.getPosition().getY(), xend, yend);
				
				if(stick.intersectsLine(up)	|| stick.intersectsLine(down) || stick.intersectsLine(left) || stick.intersectsLine(right)){
					if (Math.abs(tickProjection.getHeading() - proj.getWantedHeading()) < 5.) {
						proj.setTurningOffset(direction * 5.);
					}
				}
				
				//TODO check if we collide with a wall regardless of the smoothing

				
			}
			
			Path orbit = new Path(proj.getProjections());
			orbit.setInitialTick(0);
			orbit.setDirection(ahead ? 1 : -1);
			orbit.setStartingBearingOffset(Utils.normalRelativeAngleDegrees(angle - robot.getHeading()));
			ret.add(orbit);
	
			for (tickProjection tick : proj.getProjections()) {
				points.add(tick.getPosition());
			}
			
		}
		
		return ret;
		
		
		
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
		

		
	}
	
}
