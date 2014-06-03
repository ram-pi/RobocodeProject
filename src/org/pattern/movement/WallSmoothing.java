package org.pattern.movement;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pattern.movement.Projection.tickProjection;

import robocode.AdvancedRobot;

public class WallSmoothing {

	public final static int STICK_LENGTH = 140;
	public final static int MINIMUM_RADIUS = 114;
	private java.awt.geom.Point2D.Double center1;
	private java.awt.geom.Point2D.Double center2;
	private Rectangle2D safeBF;
	private AdvancedRobot robot;
	

//	public WallSmoothing(Point2D.Double position, double velocity, double heading, Rectangle2D.Double rect, int turningDirection) {
//		
//		//TODO how many ticks we check?
//		
//		boolean hitWall = true;
//		double angle = 0;
//		
//		while (!hitWall) {
//			Projection proj = new Projection(position, velocity, heading, (int)Math.signum(velocity), 0);
//			hitWall = false;
//			for (int t=0;t < 25; t++) {
//				tickProjection tick = proj.projectNextTick();
//				if (!rect.contains(tick.getPosition())) {
//					hitWall = true;
//					angle += .5;
//					break;
//				}
//			}
//		}
//		
//	}
	
	public WallSmoothing(AdvancedRobot robot) {
		
		this.robot=robot;
		safeBF = new Rectangle2D.Double(18, 18, robot.getBattleFieldWidth()-36, robot.getBattleFieldHeight()-36);
		center1=new Point2D.Double(0, 0);
		center2=new Point2D.Double(0, 0);
	}
	
	public boolean doSmoothing(int ahead,tickProjection t){
		
		double _ahead=ahead*100;
		double _turnRight=0;
		if (stickCollide(t.getPosition(), ahead == 1 ? t.getHeading() : t.getHeading()+180)) {
			center1 = new Point2D.Double(t.getPosition().getX() + Math.sin(Math.toRadians(t.getHeading()-90))*MINIMUM_RADIUS,
					t.getPosition().getY() + Math.cos(Math.toRadians(t.getHeading()-90))*MINIMUM_RADIUS);
			center2 = new Point2D.Double(t.getPosition().getX() + Math.sin(Math.toRadians(t.getHeading()+90))*MINIMUM_RADIUS,
					t.getPosition().getY() + Math.cos(Math.toRadians(t.getHeading()+90))*MINIMUM_RADIUS);
			Boolean smoothC1 = canSmooth(center1);
			Boolean smoothC2 = canSmooth(center2);
			if (!smoothC1 && !smoothC2) {
				/* TODO  Facciamo manovra*/ 
				_ahead=ahead*(-100);
				System.out.println("No way out");
			} else if (smoothC1 && ahead == 1 || smoothC2 && ahead == -1) {
				if (robot.getTurnRemaining() > -4)
					_turnRight=-4;

			} else if (smoothC2 && ahead == 1 || smoothC1 && ahead == -1) {
				if (robot.getTurnRemaining() < 4)
					_turnRight=4;
			}
			robot.setAhead(_ahead);
			robot.setTurnRight(_turnRight);
			return true;
		}
		return false;
	}
	public Boolean canSmooth (Point2D center) {
		Point2D up, down, left, right;
		up = new Point2D.Double(center.getX(), center.getY()+MINIMUM_RADIUS);
		down = new Point2D.Double(center.getX(), center.getY()-MINIMUM_RADIUS);
		left = new Point2D.Double(center.getX()-MINIMUM_RADIUS, center.getY());
		right = new Point2D.Double(center.getX()+MINIMUM_RADIUS, center.getY());
		if (!safeBF.contains(up) || !safeBF.contains(right) || !safeBF.contains(down) || !safeBF.contains(left)) {
			return false;
		}
		return true;
	}
	
	public Boolean stickCollide (Point2D tickPosition, Double heading) {

		Double endX = tickPosition.getX()+Math.sin(Math.toRadians(heading))*STICK_LENGTH;
		Double endY = tickPosition.getY()+Math.cos(Math.toRadians(heading))*STICK_LENGTH;
		//endPoint = new Point2D.Double(endX, endY);

		if (!safeBF.contains(endX, endY)) {
			return true;
		}


		return false;
	}

	public java.awt.geom.Point2D.Double getCenter1() {
		return center1;
	}

	public void setCenter1(java.awt.geom.Point2D.Double center1) {
		this.center1 = center1;
	}

	public java.awt.geom.Point2D.Double getCenter2() {
		return center2;
	}

	public void setCenter2(java.awt.geom.Point2D.Double center2) {
		this.center2 = center2;
	}
	
	public Rectangle2D getSafeBF() {
		return safeBF;
	}
	
	public void setSafeBF(Rectangle2D safeBF) {
		this.safeBF = safeBF;
	}

	
}
