package aiman.structures;

import java.util.ArrayList;
import java.util.List;

import aiman.Energizer;

import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;

/**
 * class to represent the set of dots and energizers and implement utility functions such distance computations ecc 
 * @author jalone
 * TODO implement generics
 * TODO use composite and not extends ( bad to expose some Cninuous2D methods )
 */
public class CookieJar extends Continuous2D {

	private static final long serialVersionUID = 1L;
	
	private List<Energizer> energizers;

	public CookieJar(double discretization, double width, double height) {
		super(discretization, width, height);
		energizers = new ArrayList<Energizer>(4);
	}
	
	public Object getNearestObject(Double2D from){
		//TODO performance !!!! to make it much better first search the closer area with getObjectWithinLocation, if empty then get all objects
		Bag bag = this.getAllObjects();
		if(bag.isEmpty()){
			throw new ObjectNotFound("There are no cookies in the bag");
		}
		//seek closer
		double dist = Double.POSITIVE_INFINITY;
		Object obj = null;
		double tdist;
		for(Object tobj: bag){
			tdist = from.distanceSq(getObjectLocation(tobj));
			if(tdist < 0.9){//break if dot is next 
				return tobj; // cookie is the next in line 
			}
			if(tdist < dist){
				dist = tdist;
				obj = tobj;
			}
		}
		return obj;
	}
	
	public void setEnergizerLocation(Energizer e, final Double2D location){
		this.energizers.add(e);
		super.setObjectLocation(e, location);
    }
    
    public final void remove(final Energizer e){
    	this.energizers.remove(e);
		super.remove(e);
    }
    
	public Energizer getNearestPowerDot(Double2D from){
		if(energizers.isEmpty()){// || (energizers.size() < 4)){ //debug
			throw new ObjectNotFound("No energizers in the jar");
		}
		Energizer e = null;
		double dist = Double.POSITIVE_INFINITY;
		for(Energizer te: energizers){
			Double2D to = this.getObjectLocation(te);
			double td = from.distanceSq(to);
			if(td < dist){
				e = te;
				dist = td;
			}
		}
		return e;
	}
	
	public Bag clearAll(){
		this.energizers.clear();
		return super.clear();
	}

}
