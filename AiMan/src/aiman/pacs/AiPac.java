/**
 * Created By Lorenzo Tognalini, 2011
 * 
 */

package aiman.pacs;

import net.jalone.jul4j.logging.Logger;
import aiman.PacMan;
import aiman.pacs.AiPac;
import aiman.pacs.behaviours.Condition;
import aiman.pacs.behaviours.ObservationsHandler;
import aiman.pacs.behaviours.Policy;
import aiman.pacs.behaviours.Rule;
import aiman.pacs.behaviours.Condition.Direction;
import sim.engine.*;

/**
 * Handcoded Rules implementation.
 *
 * @author Lorenzo Tognalini, http://www.jalone.net
 */
public class AiPac extends AutoPac {
	
	private static final long serialVersionUID = 1L;
	
	private Policy policy;
	ObservationsHandler obs;

    /** Creates a Pac assigned to the given tag, puts him in pacman.agents at the start location, and schedules him on the schedule. */
    public AiPac(PacMan pacman, int tag) {
        super(pacman, tag);
        
        obs = new ObservationsHandler(pacman); 
		this.policy = new Policy(obs, 3);
		
    }
    
	@Override
    protected void doPolicyStep(SimState state) {

		//TODO if optimization needed: add discretization if location == integer then get decision else keep doing
		
		policy.update();
		
		this.buildPolicy();
		
		String actionToCall = policy.pickAction();
		
		Logger.getInstance().log(actionToCall); //debug

		int nextAction = this.pickDirectionForAction(actionToCall);
		
		performAction(nextAction);
	}
	
	@Override
	public void buildPolicy(){
		
		//to escape if ghost is close
		Rule tpol = new Rule(0, AutoPac.ACTION_FROM_GHOST, true); 
		tpol.pushCondition(new Condition(obs, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MIN, 6));
		policy.pushRule(tpol);
		
		//don't escape if ghost far
		tpol = new Rule(1, AutoPac.ACTION_FROM_GHOST, false);
		tpol.pushCondition(new Condition(obs, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MAX, 6));
		policy.pushRule(tpol);
		
		//to closer dot
		tpol = new Rule(2, AutoPac.ACTION_TO_DOT, true);
		tpol.pushCondition(new Condition(obs, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MIN, 10000));
		policy.pushRule(tpol);
	}
	
}
