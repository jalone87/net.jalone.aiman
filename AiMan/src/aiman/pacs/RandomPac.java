package aiman.pacs;

import net.jalone.jul4j.logging.Logger;
import aiman.PacMan;
import aiman.pacs.behaviours.ObservationsHandler;
import aiman.pacs.behaviours.Policy;
import sim.engine.SimState;

public class RandomPac extends AutoPac {

	Policy policies;
	
	public RandomPac(PacMan pacman, int tag) {
		super(pacman, tag);

		this.policies = new Policy(new ObservationsHandler(pacman),0);

	}

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPolicyStep(SimState state) {
		
		String actionToCall = policies.getRandomAction();
		
		Logger.getInstance().log(actionToCall); //DEBUG

		int nextAction = this.pickDirectionForAction(actionToCall);
		
		performAction(nextAction);
		
	}

	@Override
	public void buildPolicy() {
		//no policy
	}

}
