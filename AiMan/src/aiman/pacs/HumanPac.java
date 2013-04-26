/*
 * Created By Lorenzo Tognalini, 2011
 * 
 */

package aiman.pacs;

import sim.engine.SimState;
import aiman.Pac;
import aiman.PacMan;

@SuppressWarnings("serial")
public class HumanPac extends Pac {

	public HumanPac(PacMan pacman, int tag) {
		super(pacman, tag);

	}

  /* Default policy implementation: Pac is controlled through the joystick/keyboard
   * To changhe Pacs behavior derived classes should override this method
   */
	@Override
	protected void doPolicyStep(SimState state){
		
		int nextAction = ((PacMan)state).getNextAction(tag);
		
		// pac man delays the next action until he can do it.  This requires a bit of special code
		if (isPossibleToDoAction(nextAction)){
			performAction(nextAction);
		}
		else if (isPossibleToDoAction(lastAction)){
			performAction(lastAction);
		}

	}
}
