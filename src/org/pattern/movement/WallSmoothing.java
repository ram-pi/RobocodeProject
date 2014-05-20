package org.pattern.movement;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.pattern.movement.Projection.tickProjection;

public class WallSmoothing {

	public WallSmoothing(Point2D.Double position, double velocity, double heading, Rectangle2D.Double rect, int turningDirection) {
		
		//TODO how many ticks we check?
		
		boolean hitWall = true;
		double angle = 0;
		
		while (!hitWall) {
			Projection proj = new Projection(position, velocity, heading, (int)Math.signum(velocity), 0);
			hitWall = false;
			for (int t=0;t < 25; t++) {
				tickProjection tick = proj.projectNextTick();
				if (!rect.contains(tick.getPosition())) {
					hitWall = true;
					angle += .5;
					break;
				}
			}
		}
		
	}
}
