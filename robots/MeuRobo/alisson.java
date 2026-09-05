/*
 * Copyright (c) 2001-2025 Mathew A. Nelson and Robocode contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://robocode.sourceforge.io/license/epl-v10.html
 */
package MeuRobo;

import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

import java.awt.*;

/**
 * Walls - A sample robot that demonstrates a wall-following movement pattern.
 * <p>
 * This robot navigates around the perimeter of the battlefield with the gun
 * pointed inward.
 *
 * @author Mathew A. Nelson (original)
 * @author Flemming N. Larsen (contributor)
 */
public class alisson extends AdvancedRobot {

	boolean peek; // Flag to determine if scanning is needed before turning
	double moveAmount; // The maximum distance to move along a wall

    /**
	 * run: Navigates the robot around the battlefield perimeter
	 */
	public void run() {
		// Set colors
		setBodyColor(Color.blue);
		setGunColor(Color.black);
		setRadarColor(Color.orange);
		setBulletColor(Color.cyan);
		setScanColor(Color.cyan);

		// Set movement distance to the maximum battlefield dimension
		moveAmount = Math.max(getBattleFieldWidth(), getBattleFieldHeight()) - 2 * getSentryBorderSize()
				- this.getWidth();
		// Initialize scanning flag to false
		peek = false;
		ahead(moveAmount);


		while (true) {
			out.println(this.getX());
	
			// Enable scanning before moving along the wall
			peek = true;
			// Move along the current wall
			ahead(moveAmount);
			// Disable scanning during turning
			peek = false;
			// Turn 90 degrees to follow the next wall
			turnRight(90);
		} 
	}

	/**
	 * onHitRobot: Handles collision with another robot by moving away
	 */
	public void onHitRobot(HitRobotEvent e) {
		// If the other robot is in front of us, move backward
		if (e.getBearing() > -90 && e.getBearing() < 90) {
			back(100);
		} // If the other robot is behind us, move forward
		else {
			ahead(100);
		}
        scan();
	}

	/**
	 * onScannedRobot: Fires at the detected robot and performs additional scanning
	 * when needed
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
		// Fire at the detected robot with power level 2
		fire(2);
		// Scan is automatically called during movement, but we need an additional scan
		// when the peek flag is true to detect robots before turning or moving toward a
		// new wall
		if (peek) {
			scan();
		}
	}

	@Override
	public void onHitWall(HitWallEvent event) {
		out.println(event.toString());
        back(250);
	}

    

}
