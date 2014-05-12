package org.robot;

	
import java.awt.Graphics2D;

import org.pattern.shooting.ShootingUtils;
import org.pattern.shooting.ViewFinder;

import robocode.AdvancedRobot;
import robocode.util.Utils;

public class AimingTester extends AdvancedRobot {

	boolean nearWall;
	public int moveDirection = 1; // Switch direction from 1 to -1 
	ViewFinder v;
	double x = 150.0;
	double y = 200.0;
	
	@Override
	public void run() {
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		v.setPointToShot(x, y);
		v = new ViewFinder(this);
	
		
		while(true) {
			double theta = ShootingUtils.findAngle(x, y, getX(), getY());
			theta = Utils.normalAbsoluteAngleDegrees(theta - getGunHeading());
			theta = Utils.normalRelativeAngleDegrees(theta);
			setTurnGunRight(theta);
			execute();
		}
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		g.drawRect((int) x-5, (int) y-5, 10, 10);
	}
}
