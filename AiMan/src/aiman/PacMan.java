/*
  Copyright 2009  by Sean Luke and Vittorio Zipparo
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package aiman;

import net.jalone.jul4j.logging.FileLogger;
import aiman.pacs.*;
import aiman.pacs.behaviours.Teacher;
import aiman.pacs.behaviours.Teacher.TeachingState;
import aiman.structures.CookieJar;
import sim.engine.*;
import sim.util.*;
import sim.field.continuous.*;

/**
 * PacMan is the model for the game. The model contains three fields: a
 * Continuous2D for the agents, a Continuous2D for the dots, and an IntGrid2D
 * holding the maze (1 is wall, 0 is open space). The model holds an array of
 * "actions", one per player, in case we want to make this a multiplayer game.
 * 
 * <p>
 * Note that you can easily modify this code to have different kinds of Pacs
 * (internally we have invented AI Pacs. :-). Also if you just want one pacman,
 * not two, change the Pacs array to be of size 1 (see the code below).
 */

@SuppressWarnings("serial")
public class PacMan extends SimState {
	
	public boolean isLearning = true;

	public int MAX_MAZES = 2;

	public int numPacs = 2;

	/** Holds the ghosts and the Pac. */
	public Continuous2D agents;

	/** Holds Energizers and Dots. */
	public CookieJar dots;

	/** Debug shape to show the actual action goal */
	// DEBUG
	public Continuous2D actionGoals;

	/** The maze proper. */
	public IntPBMGrid2D maze;

	/**
	 * A signal to indicate to the ghosts that they should become frightened
	 * next step around.
	 */
	boolean frightenGhosts; // signal for the ghosts

	/** Desired actions from the user. Presently only actions[0] used. */
	public int[] actions;

	/** The number of deaths so far. */
	public int deaths = 0;

	/** The current level. */
	public int level = 1;

	/** The current score. */
	public int score = 0;
	
	/** Number of games (multiple levels count as 1) played */
	public int gameNumber = 0;
	
	/** The pacs. Used by the ghosts to figure out where the closest Pac is. */
	public Pac[] pacs;
	
	public Teacher teacher;

	/** Creates a PacMan simulation with the given random number seed. */
	public PacMan(long seed) {
		super(seed);
		teacher = new Teacher(this);
	}

	/**
	 * Resets the scores, loads the maze, creates the fields, adds the dots and
	 * energizers, and resets the Pac and Ghosts.
	 */
	public void start() {
		super.start();

		deaths = 0;
		level = 1;
		score = 0;

		maze = new IntPBMGrid2D(PacMan.class.getResourceAsStream("images/maze0.pbm"));
		agents = new Continuous2D(1.0, maze.getWidth(), maze.getHeight());
		dots = new CookieJar(1.0, maze.getWidth(), maze.getHeight());
		actionGoals = new Continuous2D(1.0, maze.getWidth(), maze.getHeight());

		resetGame();
	}

	/**
	 * Resets the game board. Doesn't change the score or deaths or level number
	 */
	public void resetGame() {

		//a game has been played, evaluate it
		if(gameNumber > 0){
			teacher.evaluate(score);
		}
		
		if(isLearning){
			teacher.setState(TeachingState.TEACHING); //trick to avoid first turn, avoid in a more elegant way if possible
		}else{
			teacher.setState(TeachingState.PLAYING);
		}
		score = 0;
		dots.clearAll();

		maze.read(PacMan.class.getResourceAsStream("images/maze" + (level - 1)
				% MAX_MAZES + ".pbm"));

		// TODO put position inside object as well, calls to cont2d cost
		Energizer e = new Energizer();
		dots.setEnergizerLocation(e, new Double2D(1, 5));
		e = new Energizer();
		dots.setEnergizerLocation(e, new Double2D(26, 5));
		e = new Energizer();
		dots.setEnergizerLocation(e, new Double2D(1, 25));
		e = new Energizer();
		dots.setEnergizerLocation(e, new Double2D(26, 25));

		// distribute dots. We allow dots right on the energizers, no biggie
		for (int x = 0; x < maze.getWidth(); x++)
			for (int y = 0; y < maze.getHeight(); y++)
				if (maze.field[x][y] == 0 && !(y == 16 && x >= 12 && x <= 16)) // not
																				// in
																				// the
																				// jail
					dots.setObjectLocation(new Dot(), new Double2D(x, y));
		
		resetAgents();

	}

	public int pacsLeft() {
		int count = 0;
		for (int i = 0; i < pacs.length; i++)
			if (pacs[i] != null)
				count++;
		return count;
	}

	public Pac pacClosestTo(MutableDouble2D location) {
		if (pacs.length == 1)
			return pacs[0];
		Pac best = null;
		int count = 1;
		for (int i = 0; i < pacs.length; i++) {
			if (pacs[i] != null) {
				if (best == null
						|| (best.location.distanceSq(location) > pacs[i].location
								.distanceSq(location)
								&& ((count = 1) == 1) || best.location
								.distanceSq(location) == pacs[i].location
								.distanceSq(location)
								&& random.nextBoolean(1.0 / (++count))))
					best = pacs[i];
			}
		}
		return best;
	}

	/**
	 * Puts the agents back to their regular locations, and clears the schedule.
	 */
	public void resetAgents() {
		
		gameNumber++;
		
		agents.clear();
		schedule.clear();
		actionGoals.clear();

		// make arrays
		actions = new int[] { Agent.NOTHING, Agent.NOTHING };

		// add Blinky
		Blinky blinky = new Blinky(this);

		// add Pinky
		new Pinky(this);

		// add Inky
		new Inky(this, blinky);

		// add Clyde
		new Clyde(this);

		// EDITED:AiMan
		pacs = new Pac[numPacs]; // set this to Pac[1] to delete human player
		
//		RandomPac pac = new RandomPac(this, 0);
//		AiPac pac = new AiPac(this, 0);
		LearningPac pac = new LearningPac(this, 0);
		FileLogger.getInstance().log();
		FileLogger.getInstance().log("### Game " + gameNumber + " ; agent: " + pac.getClass().getName());
		teacher.teach(pac);
		pacs[0] = pac;//TODO THIS MUST BE AN AUTOPAC ( find a way to decrease coupling )
		
		if (pacs.length > 1)
			pacs[1] = new HumanPac(this, 1);

		// ghosts are no longer frightened
		frightenGhosts = false;

        ActionGoal goal = new ActionGoal(); //TODO if really necessary this should be added to jar, not here?
        actionGoals.setObjectLocation(goal, new Double2D( 	((AutoPac)pacs[0]).goalLocation.x, 
        													((AutoPac)pacs[0]).goalLocation.x ));
	}

	/** Returns the desired user action. */
	public int getNextAction(int tag) {
		return actions[tag];
	}

	// EDITED:AiMan
	 public static void main(String[] args){
		 doLoop(PacMan.class, args);
		 System.exit(0);
	 }
}
