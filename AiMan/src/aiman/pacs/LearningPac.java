/**
 * Created By Lorenzo Tognalini, 2011
 * 
 */

package aiman.pacs;

import net.jalone.jul4j.logging.Logger;

import sim.engine.SimState;
import aiman.*;
import aiman.pacs.behaviours.Policy;

/**
 * 
 * @author Lorenzo Tognalini, http://www.jalone.net
 *
 */
@SuppressWarnings("serial")
public class LearningPac extends AutoPac {

	private Policy policy;

	public LearningPac(PacMan pacman, int tag) {
		super(pacman, tag);

	}

	@Override
	protected void doPolicyStep(SimState state) {

		//PERFORMANCES if optimization needed: add discretization if location == integer then get decision else keep doing
		
		policy.update(); //update
		
		String actionToCall = policy.pickAction(); //choose
		
		Logger.getInstance().log(actionToCall); //debug
		
		int direction = pickDirectionForAction(actionToCall); 
		
		performAction(direction); //execute low level action
	}

	public void adoptPolicy(Policy policy){
		this.policy = policy;
	}
	
	
}
