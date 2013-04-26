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

    /** Creates a Pac assigned to the given tag, puts him in pacman.agents at the start location, and schedules him on the schedule. */
    public AiPac(PacMan pacman, int tag) {
        super(pacman, tag);
        
		this.policy = new Policy(new ObservationsHandler(pacman));
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
		//TODO passing "policies" as parameter to stuff to be pushed in policies is a bad thin. make policies manage themselves 
		
		//to escape if ghost is close
		Rule tpol = new Rule(AutoPac.ACTION_FROM_GHOST, true, policy); 
		tpol.pushCondition(new Condition(policy, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MIN, 6));
		policy.pushRule(tpol);
		
		//don't escape if ghost far
		tpol = new Rule(AutoPac.ACTION_FROM_GHOST, false, policy);
		tpol.pushCondition(new Condition(policy, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MAX, 6));
		policy.pushRule(tpol);
		
		//to closer dot
		tpol = new Rule(AutoPac.ACTION_TO_DOT, true, policy);
		tpol.pushCondition(new Condition(policy, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MIN, 10000));
		policy.pushRule(tpol);
	}
	
}
