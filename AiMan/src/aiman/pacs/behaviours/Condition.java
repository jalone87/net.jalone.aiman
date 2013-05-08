package aiman.pacs.behaviours;


/**
 * 
 * @author Lorenzo Tognalini, http://www.jalone.net
 * 
 */
public final class Condition {
	
	public enum Direction{
		MIN, /** means minor or egual to */
		MAX; /** means major of */
	};
	
	private final String observation;
	private final Direction direction;
	private final double value; 
	
	private ObservationsHandler observator;
	
	public Condition(ObservationsHandler observator, String obs, Direction dir, double val){
		this.observation = obs;
		this.direction = dir;
		this.value = val;
		
		this.observator = observator;
	}
	
	public boolean check(){
		if(direction == Direction.MIN){
			return  (observator.observe(observation) <= value);
		}else{
			return  (observator.observe(observation) > value);
		}
	} 
}
