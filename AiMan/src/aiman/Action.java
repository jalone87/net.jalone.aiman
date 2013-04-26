/**
 * Created By Lorenzo Tognalini, 2011
 * 
 */

package aiman;

public enum Action {
	
	NOTHING(-1),
    N(0),
    E(1),
    S(2),
    W(3);
	
	/**
	 * integer value of the enum action
	 */
	public final double val;   
	
    Action(double value) {
        this.val = value;
    }
	
    /** Returns the opposite direction action of the one provided.  The opposite of NOTHING is NOTHING.  */
    public static Action reverseOf(Action action){
        switch(action){
            case N: return S;
            case E: return W;
            case S: return N;
            case W: return E;
        }
        return NOTHING;
    }
    
}
