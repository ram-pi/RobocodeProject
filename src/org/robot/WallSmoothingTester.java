package org.robot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pattern.movement.Projection;
import org.pattern.movement.WallSmoothing;
import org.pattern.movement.Projection.tickProjection;
import org.pattern.utils.Utils;

import robocode.AdvancedRobot;

public class WallSmoothingTester extends AdvancedRobot{

	Projection proj;
	
	private Rectangle2D.Double rect;

	public WallSmoothingTester() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void run() {
		double fieldW = getBattleFieldWidth();
		double fieldH = getBattleFieldHeight();
		
		rect = new Rectangle2D.Double(18, 18, fieldW - 32, fieldH - 32);
		proj = new Projection(new Point2D.Double(getX(), getY()), getHeading(), getVelocity(), 1, 0);
		
		if (Utils.WallSmoothing(proj, 1, rect, 50) == -1) {
			out.println("can't smooth");		
		}
		
		out.println(proj.getBearingOffset());
		
		double angle = robocode.util.Utils.normalAbsoluteAngleDegrees(proj.getBearingOffset());
		setTurnRight(angle);
		setAhead(500);
		execute();
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		for (tickProjection pTick : proj.getProjections()) {
			g.drawRect((int)pTick.getPosition().getX()-2, (int)pTick.getPosition().getY()-2, 4, 4);
		}

	}
	
}
