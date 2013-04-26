/**
 * Created By Lorenzo Tognalini, 2011
 * 
 */

package aiman.pacs.behaviours;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import aiman.Energizer;
import aiman.Ghost;
import aiman.PacMan;
import aiman.structures.ObjectNotFound;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.Bag;

/**
 * 
 * @author Lorenzo Tognalini, http://www.jalone.net
 *
 */
public class ObservationsHandler {	
	
	public static final String  OBSERVATION_CONSTANT = "Constant";
	public static final String  OBSERVATION_NEAREST_DOT = "NearestDot";
	public static final String  OBSERVATION_NEAREST_POWERDOT = "NearestPowerDot";
	public static final String  OBSERVATION_NEAREST_GHOST = "NearestGhost";
	public static final String  OBSERVATION_NEAREST_EDGHOST = "NearestEdGhost";
	public static final String  OBSERVATION_MAX_JUNCTION_SAFETY = "MaxJunctionSafety";
	public static final String  OBSERVATION_GHOST_CENTER_DIST = "GhostCenterDist";
	public static final String  OBSERVATION_DOT_CENTER_DIST = "DotCenterDist";
	public static final String  OBSERVATION_GHOST_DENSITY = "GhostDensity";
	public static final String  OBSERVATION_TOTAL_DIST_TO_GHOST = "TotalDistToGhost";

	//TODO this is absolutely bad: think another system ( at least get a safe out of index value ) and move away
	public static final Int2D	MAX_POINT = new Int2D(10000, 10000);

	private PacMan pacman;
	
	private Map<String, Double> observations;
	
	public ObservationsHandler(PacMan pacman){
		this.pacman = pacman;
		
		this.observations = new HashMap<String, Double>();
		this.observations.put(ObservationsHandler.OBSERVATION_CONSTANT, 0.0);
		this.observations.put(ObservationsHandler.OBSERVATION_NEAREST_DOT, 0.0);
		this.observations.put(ObservationsHandler.OBSERVATION_NEAREST_POWERDOT, 0.0);
		this.observations.put(ObservationsHandler.OBSERVATION_NEAREST_GHOST, 0.0);
		this.observations.put(ObservationsHandler.OBSERVATION_NEAREST_EDGHOST, 0.0);
//		this.observations.put(ObservationsHandler.OBSERVATION_MAX_JUNCTION_SAFETY, 0.0);
//		this.observations.put(ObservationsHandler.OBSERVATION_GHOST_CENTER_DIST, 0.0);
//		this.observations.put(ObservationsHandler.OBSERVATION_DOT_CENTER_DIST, 0.0);
//		this.observations.put(ObservationsHandler.OBSERVATION_GHOST_DENSITY, 0.0);
//		this.observations.put(ObservationsHandler.OBSERVATION_TOTAL_DIST_TO_GHOST, 0.0);
	}
	
	public double observe(String observation){
		return this.observations.get(observation);
	}
	
	public void update(){
		double val;
		for(Map.Entry<String, Double> entry: observations.entrySet()){
			val = updateObservation(entry.getKey());
			observations.put(entry.getKey(), val);
		}
	}
	
	/**
	 * for a given observation string return a value calling the corresponding built method
	 * @param observationToCall
	 * @return
	 */
	public double updateObservation(String observationToCall){
		Double2D ploc = pacman.agents.getObjectLocation(pacman.pacs[0]);
		
		double observation = 0;
		
		Method methodToCall;
		try {
			methodToCall = this.getClass().getMethod("get"+observationToCall, Double2D.class);
			observation = (Double) methodToCall.invoke(this, ploc);
			
		} catch (SecurityException e) { 		e.printStackTrace();
		} catch (NoSuchMethodException e) {		e.printStackTrace();
		} catch (IllegalArgumentException e) {	e.printStackTrace();
		} catch (IllegalAccessException e) {	e.printStackTrace();
		} catch (InvocationTargetException e) {	e.printStackTrace();
		}
		//Logger.getInstance().log("Observation: "+observationToCall+": "+observation);
		return observation;
		
	}
	
	//1
	/** return a constant value*/
	public double getConstant(Double2D pacPos){
		return 1.0;
	}
	
	//2 
	/** 
	 * return the distance from closer dot, if no energizer is available distance to MAX_POINT is returned
	 * */
	public double getNearestDot(Double2D pacPos){
		
		Double2D pos = null;
		Object dot = null;
		try{
			dot = pacman.dots.getNearestObject(pacPos);		
			pos = pacman.dots.getObjectLocation(dot);
		}catch(ObjectNotFound ec){
			pos = new Double2D(MAX_POINT);
		}
		return pacPos.distance(pos);
	}
	
	//3 
	/** 
	 * return the distance from closer energizer, if no energizer is available distance to MAX_POINT is returned
	 **/
	public double getNearestPowerDot(Double2D ploc){
		
		
		Double2D pos = null;
		Energizer e = null;
		try{
			e = pacman.dots.getNearestPowerDot(ploc);		
			pos = pacman.dots.getObjectLocation(e);
		}catch(ObjectNotFound ec){
			pos = new Double2D(MAX_POINT);
		}
		return ploc.distance(pos);
		
	}
	
	//4 
	/** return the distance from the nearest ghost */
	public double getNearestGhost(Double2D pacPos){
		Bag ghosts = pacman.agents.getAllObjects();
		
		double dist = 1000000;
		
		Ghost tGhost;
		double tDist = 1000000;
		Double2D tPosGhost = new Double2D(MAX_POINT);
		
		//4 is number of ghosts ( they must be instantiated before pacs )
		for(int i = 0; i<4; i++){
			tGhost = (Ghost)ghosts.get(i);
			if(tGhost.frightened <= 0){
				tPosGhost = pacman.agents.getObjectLocation(tGhost);
				tDist = tPosGhost.distance(pacPos);
				if(tDist < dist){
					dist = tDist;
				}
			}
		}
		
		//Logger.getInstance().log(Double.toString(dist));
		return dist;
	} 
	
	//5 /** return the distance*/
	/** return the distance from the nearest ghost */
	public double getNearestEdGhost(Double2D pacPos){
		Bag ghosts = pacman.agents.getAllObjects();
		double dist = 1000000;
		
		Ghost tGhost;
		double tDist = 1000000;
		Double2D tPosGhost = new Double2D(MAX_POINT);
		
		//4 is number of ghosts ( they must be instantiated before pacs )
		for(int i = 0; i<4; i++){
			tGhost = (Ghost)ghosts.get(i);
			if(tGhost.frightened > 0){
				tPosGhost = pacman.agents.getObjectLocation(tGhost);
				tDist = tPosGhost.distance(pacPos);
				if(tDist < dist){
					dist = tDist;
				}
			}
		}
		return dist;
	} 
	
	//6
	//getMaxJunctionSafety //TODO
	
	//7 /** return the distance*/
	//getGhostCenterDist //TODO
	
	//8 /** return the distance*/
	//getDotCenterDist //TODO
	
	//9 
	//getGhostDensity //TODO
	
	//10 /** return the distance*/
	//getTotalDistToGhost //TODO

	@Override
	public String toString(){
		String ret = "ObservationsHandler[";
		int i = 0;
		for(double val: observations.values()){
			ret = ret.concat(String.format("%d: %8.2f, ", i, val));
			i++;
		}
		ret = ret.concat("]");
		return ret;
	}
	
}
