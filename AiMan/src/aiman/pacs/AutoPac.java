package aiman.pacs;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sim.util.Bag;
import sim.util.Double2D;
import aiman.Ghost;
import aiman.Pac;
import aiman.PacMan;
import aiman.pacs.behaviours.ObservationsHandler;
import aiman.structures.ObjectNotFound;

/**
 * Extends Pac to include support to some higher level actions than just go-up, go-down, go-left or go-right
 * 
 * Note that to properly show any AutoPac inheriting Pac you must provide a proper portrayal 
 * in the PacManWithUI method setPortrayal() (just follow previous implemented pac)
 * 
 * public static final String ACTION_DO_SOMETHING are the strings representing the possible default action an AutoPac can perform
 * IMPORTANT:the string variable name is not important, but the content must reflect the method that the string will be 
 * calling when invoking pickDirectionForAction(String actionToCall).
 * All action method must follow following naming "action"+"upper-camel-case-name", 
 * while string representing it must only contain "upper-camel-case-name".
 *  
 * @author Lorenzo Tognalini
 *
 */
public abstract class AutoPac extends Pac {

	private static final long serialVersionUID = 1L;
	
	/** true to let the pac continue to move around even if no action for actual state is provided by policy */
	private static final boolean CONTINUE_IF_NO_ACTION = false;
	
	public static final String  NO_ACTION = "NoAction";
	
	public static final String  ACTION_TO_DOT 					= "ToDot";
	public static final String  ACTION_TO_POWERDOT 				= "ToPowerDot";
	public static final String  ACTION_FROM_POWERDOT 			= "FromPowerDot";
	public static final String  ACTION_TO_EDGHOST 				= "ToEdGhost";
	public static final String  ACTION_FROM_GHOST 				= "FromGhost";
	public static final String  ACTION_TO_SAFE_JUNCTION 		= "ToSafeJunction";
	public static final String  ACTION_FROM_GHOST_CENTER 		= "FromGhostCenter";
	public static final String  ACTION_KEEP_DIRECTION 			= "KeepDirection";
	public static final String  ACTION_TO_LOWER_GHOST_DENSITY 	= "ToLowerGhostDensity";
	public static final String  ACTION_TO_GHOST_AREA_FREE 		= "ToGhostAreaFree";
	
	/**
	 * keep the position of last action goal location ( only for debugging reason at this moment )
	 */
	public Double2D goalLocation;
	
	/**
	 * this method must be implemented to initialize the policy pool with policies that will be used to learn a behavior, 
	 * or from whose to pick randomly etc..
	 */
	public void  buildPolicy(){
	}
	
	public AutoPac(PacMan pacman, int tag) {
		super(pacman, tag);
		goalLocation = this.getStartLocation();
	}

	public int pickDirectionForAction(String actionToCall){
		
		Method methodToCall;
		int nextAction = NOTHING;
		
//		if(lastAction == NOTHING){
//			nextAction = actionKeepDirection();
//		}
		
		try {
			methodToCall = this.getClass().getMethod("action"+actionToCall);
			nextAction = (Integer) methodToCall.invoke(this);

		}catch (ObjectNotFound e) {
			System.out.println(e.getMessage());
			nextAction = actionKeepDirection();
		
		} catch (SecurityException e) { 		
			e.printStackTrace();
		
		} catch (NoSuchMethodException e) {		
			
			if(CONTINUE_IF_NO_ACTION){
				nextAction = actionKeepDirection(); 
			}else{
				nextAction = NOTHING; 
			}
			
		} catch (IllegalArgumentException e) {	e.printStackTrace();
		} catch (IllegalAccessException e) {	e.printStackTrace();
		
		} catch (InvocationTargetException e) {	
			System.err.println(e.getCause().getMessage());
			nextAction = actionKeepDirection();
		}
		
		if(lastAction == NOTHING){
			nextAction = actionKeepDirection();
		}
		return nextAction;
	}
	
	/**
	 * used by actions modules to reach a given location
	 * @return action to be performed
	 */
	//TODO this can be picked better
	
	public int toLocation(Double2D target){
		
		//TODO dont like the location update here? 
		this.goalLocation = target;
        //pacman.actionGoals.setObjectLocation(goal, goalLocation);
		
		//TODO DEBUG 
        pacman.actionGoals.setObjectLocation( pacman.actionGoals.allObjects.get(0), goalLocation);

		int bestAction = NOTHING;

		if (location.x == (int) location.x && location.y == (int) location.y){
			double bestActionDistanceSquared = Double.POSITIVE_INFINITY;

			int reverseAction = reverseOf(lastAction);

			double nx = 0;
			double ny = 0;
			double x = location.x;
			double y = location.y;

			for(int action = N; action <= W; action++){ // compute the possible actions I can do
				if (isPossibleToDoAction(action)){//action != reverseAction && isPossibleToDoAction(action)){

					switch(action){
						case N: nx = x; ny = y - 1; break;
						case E: nx = x + 1; ny = y; break;
						case S: nx = x; ny = y + 1; break;
						case W: nx = x - 1; ny = y; break;
					}

					double dist = 0;

//					if (Math.abs(agents.stx(target.x - nx)) <= 	MIN_DIST_FOR_TOROIDAL || 
//						Math.abs(agents.sty(target.y - ny)) < - MIN_DIST_FOR_TOROIDAL)
//						dist = agents.tds(target, new Double2D(nx, ny));
//					else
					dist = target.distanceSq(new Double2D(nx, ny));

					if((bestAction == NOTHING) || (dist < bestActionDistanceSquared)){  // pick the best when I'm not afraid
						bestAction = action; 
						bestActionDistanceSquared = dist;
					}

				}//end for each possible action
			}//end for each action

			if (bestAction == NOTHING){
				//FileLogger.getInstance().log(new Integer(bestAction).toString());
				bestAction = reverseAction;
			}

		}else{
			bestAction = lastAction;
		}
		return bestAction;
	}

	//ACTIONS MODULES [10]

	/*1*/
	public int actionToDot(){

		Double2D pacPos = new Double2D(this.location);
		Double2D pos = null;
		Object dot = null;
		try{
			dot = pacman.dots.getNearestObject(pacPos);		
			pos = pacman.dots.getObjectLocation(dot);
		}catch(ObjectNotFound ec){
			pos = new Double2D(ObservationsHandler.MAX_POINT);
		}
		return toLocation(pos);
	}

	/*2*/
	public int actionToPowerDot(){
		Double2D ploc = new Double2D(this.location);
		Object energizer = pacman.dots.getNearestPowerDot(ploc);
		Double2D pos = pacman.dots.getObjectLocation(energizer);
		
		return toLocation(pos);
	}

	/*3*/
	//actionFromPowerDot //TODO

	/*4*/
	/**
	 * go in direction of the nearest ghost, if no ghost, keep direction
	 */
	public int actionToEdGhost(){
		int action = NOTHING;

		Bag ghosts = pacman.agents.getAllObjects();
		
		//Ghost closerGhost = null;
		
		Double2D pacPos = pacman.agents.getObjectLocation(this);
		Double2D ghostPos = null;
		double dist = 1000000;
		
		Ghost tGhost;
		double tDist = 1000000;
		Double2D tPosGhost = new Double2D(1000,1000);
		
		//4 is number of ghosts ( they must be instantiated before pacs )
		for(int i = 0; i<4; i++){
			tGhost = (Ghost)ghosts.get(i);
			if(tGhost.frightened > 0){
				tPosGhost = pacman.agents.getObjectLocation(tGhost);
				tDist = tPosGhost.distance(pacPos);
				if(tDist < dist){
					//closerGhost = tGhost;
					ghostPos = tPosGhost;
					dist = tDist;
				}
			}
		}
		
		//Logger.getInstance().log(Double.toString(dist));
		
		if(ghostPos != null){
			action = this.toLocation(ghostPos);
		}else{
			action = this.actionKeepDirection();
		}
		
		return action;
	} 

	/*5*/
	/**
	 * go in direction opposite to the nearest ghost, if no ghost, keep direction
	 */
	public int actionFromGhost(){
		int action = NOTHING;

		Bag ghosts = pacman.agents.getAllObjects();
		
		//Ghost closerGhost = null;
		
		Double2D pacPos = pacman.agents.getObjectLocation(this);
		Double2D ghostPos = null;
		double dist = Double.POSITIVE_INFINITY;
		
		Ghost tGhost;
		double tDist = Double.POSITIVE_INFINITY;
		Double2D tPosGhost = new Double2D(1000,1000);
		
		//4 is number of ghosts ( they must be instantiated before pacs )
		for(int i = 0; i<4; i++){
			tGhost = (Ghost)ghosts.get(i);
			if(tGhost.frightened <= 0){
				tPosGhost = pacman.agents.getObjectLocation(tGhost);
				tDist = tPosGhost.distance(pacPos);
				if(tDist < dist){
					//closerGhost = tGhost;
					ghostPos = tPosGhost;
					dist = tDist;
				}
			}
		}
		
		//Logger.getInstance().log(Double.toString(dist));
		
		if(ghostPos != null){
			Double2D toPos = null;
			
			if(pacPos.x >= ghostPos.x){
				if(pacPos.y >= ghostPos.y){
					toPos = new Double2D(pacPos.x + ghostPos.x,
										 pacPos.y + ghostPos.y);
				}else{//pac y < ghost y
					toPos = new Double2D(pacPos.x + ghostPos.x,
							 			 pacPos.y - ghostPos.y);
				}
			}else{ //pac x < ghost x
				if(pacPos.y >= ghostPos.y){
					toPos = new Double2D(pacPos.x - ghostPos.x,
							 			 pacPos.y + ghostPos.y);
				}else{//pac y < ghost y
					toPos = new Double2D(pacPos.x - ghostPos.x,
							 			 pacPos.y - ghostPos.y);
				}
			}
			
			action = this.toLocation(toPos);
		}else{
			action = this.actionKeepDirection();
		}
		return action;
		
	} 

	/*6*/
	//actionToSafeJunction //TODO

	/*7*/
	//actionFromGhostCenter //TODO

	/*8*/
	public int actionKeepDirection(){
		if(isPossibleToDoAction(lastAction))
			return lastAction;
		else{
			int nextAction = NOTHING;
			do{
				nextAction = (int)(Math.random() * 4);
			}while((!isPossibleToDoAction(nextAction)) || (((nextAction + 2) % 4) == lastAction ));
			
			return nextAction;
		}
	}

	/*9*/
	//actionToLowerGhostDensity //TODO

	/*10*/
	//actionToGhostAreaFree //TODO

}
