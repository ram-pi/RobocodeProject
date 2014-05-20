package org.robot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import robocode.AdvancedRobot;
import robocode.Rules;

public class WallSmoothingTest extends AdvancedRobot{

	private Line2D line;
	private Line2D up;
	private Line2D down;
	private Line2D left;
	private Line2D right;
	private double stickLenght;
	private double distance;
	private Double heading;
	
	@Override
	public void run() {
		
		up=new Line2D.Double(0,this.getBattleFieldHeight(),this.getBattleFieldWidth(),this.getBattleFieldHeight());
		down=new Line2D.Double(0, 0, getBattleFieldWidth(),0);
		left=new Line2D.Double(0, 0, 0, getBattleFieldHeight());
		right=new Line2D.Double(getBattleFieldWidth(), 0, this.getBattleFieldWidth(),this.getBattleFieldHeight());
		line=new Line2D.Double();
		stickLenght=150;
		distance=50;
		heading=null;
		
		while(true){
			stickIntersection();
			execute();
		}
	}
	
	public void stickIntersection(){
		
		double xend=getX()+Math.sin(getHeadingRadians())*stickLenght;
		double yend=getY()+Math.cos(getHeadingRadians())*stickLenght;
		
		line.setLine(getX(), getY(), xend, yend);
		
//		if((line.intersectsLine(up) && Point2D.Double.distance(getX(), getY(), getX(), getBattleFieldHeight())<distance)
//				|| (line.intersectsLine(down) && Point2D.Double.distance(getX(), getY(), getX(), 0)<distance)
//				|| (line.intersectsLine(left) && Point2D.Double.distance(getX(), getY(), 0, getY())<distance)
//				|| (line.intersectsLine(right) && Point2D.Double.distance(getX(), getY(), getBattleFieldWidth(), getY())<distance)){
		if(line.intersectsLine(up)	|| line.intersectsLine(down) || line.intersectsLine(left) || line.intersectsLine(right)){
			System.out.println("intersection");
			setTurnRight(8);	
		}

		else{
			heading=null;
			setMaxVelocity(Rules.MAX_VELOCITY);
			setAhead(Double.POSITIVE_INFINITY);
		}
	}
	@Override
	public void onPaint(Graphics2D g) {
		
		super.onPaint(g);
		g.setColor(Color.blue);
		g.drawLine((int)line.getX1(), (int)line.getY1(), (int)line.getX2(), (int)line.getY2());
	}
}
