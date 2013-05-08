package aiman.pacs.behaviours;

/**
 * class storing the outcome status of a game (score, policy used, etc.)
 * @author jalone
 *
 */
public class GameOutcome {

	private Policy 	policy;
	private int 	score;
	
	public GameOutcome(Policy policy, int score) {
		this.setPolicy(policy);
		this.setScore(score);
	}

	public Policy getPolicy() {
		return policy;
	}
	private void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public int getScore() {
		return score;
	}
	private void setScore(int score) {
		this.score = score;
	}
	
}
