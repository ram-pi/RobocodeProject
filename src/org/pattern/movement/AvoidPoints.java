package org.pattern.movement;

import java.awt.Point;
import java.util.ArrayList;
import robocode.AdvancedRobot;
import robocode.util.Utils;

public class AvoidPoints {
	
	public static double aHead=30;
	
	public static void avoid(AdvancedRobot robot,ArrayList<Point>avoidPoints){
		
		double angle;
		double distance;
		double absBearing;
		double x=0.0;
	    double y=0.0;
		
		//walls
	    avoidPoints.add(new Point(0, (int)robot.getY()));
	    avoidPoints.add(new Point((int)robot.getX(), (int)robot.getBattleFieldHeight()));
	    avoidPoints.add(new Point((int)robot.getBattleFieldWidth(), (int)robot.getY()));
	    avoidPoints.add(new Point((int)robot.getX(), 0));
		
		//resultant force
	    for(Point pointTmp:avoidPoints){
			absBearing=Utils.normalAbsoluteAngle(Math.atan2(pointTmp.getY()-robot.getY(), pointTmp.getX()-robot.getX()));
			distance=pointTmp.distance(robot.getX(), robot.getY());
			x+=-1*Math.sin(absBearing)/(Math.pow(distance,2));
			y+=-1*Math.cos(absBearing)/(Math.pow(distance,2));
		}
		
		angle=Math.atan2(y, x);
		
		//sets the direction of the robot with the direction of the force vector
		if(Utils.normalRelativeAngle(angle-robot.getHeadingRadians())>Math.PI/2){
			robot.setTurnRightRadians(angle-robot.getHeadingRadians()-Math.PI);
			robot.setAhead(-aHead);
			
		}
		else if(Utils.normalRelativeAngle(angle-robot.getHeadingRadians())<-Math.PI/2){
			robot.setTurnRightRadians(angle-robot.getHeadingRadians()+Math.PI);
			robot.setAhead(-aHead);
		}
		else{
			robot.setTurnRightRadians(angle-robot.getHeadingRadians());
			robot.setAhead(aHead);
		}
	}
	
	public static double angleResultantForce(AdvancedRobot robot,ArrayList<Point>avoidPoints){
		
		double distance;
		double absBearing;
		double x=0.0;
	    double y=0.0;
		
		//resultant force
	    for(Point pointTmp:avoidPoints){
			absBearing=Utils.normalAbsoluteAngle(Math.atan2(pointTmp.getY()-robot.getY(), pointTmp.getX()-robot.getX()));
			distance=pointTmp.distance(robot.getX(), robot.getY());
			x+=-1*Math.sin(absBearing)/(Math.pow(distance,2));
			y+=-1*Math.cos(absBearing)/(Math.pow(distance,2));
		}
		
		return Math.atan2(y, x);
	}
}
