package aiman.pacs.behaviours;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Lorenzo Tognalini, http://www.jalone.net
 *
 */
public class Rule {

	private List<Condition> conditions;
	private String action;
	private boolean switchTo;
	private int id;
	
	public Rule(int id, String action, boolean switchTo){
		
		this.conditions = new ArrayList<Condition>();
		this.id = id;
		this.action = action;
		this.switchTo = switchTo;
		
	}
	
	public int getId(){
		return this.id;
	}
	
	public void pushCondition(Condition condition){
		conditions.add(condition);
	}
	
	public boolean check(){
		boolean ok = true;
		for(Condition cond : conditions){
			ok = ok && cond.check();
		}
		return ok;
	}
	
	public void switchAction(Policy policy){
		if(check()){
			policy.switchAction(action, switchTo);
		}
	}
	
	public String toString(){
		String ret = action+"[";
		if(switchTo){
			ret += "ON";
		}else{
			ret += "OFF";
		}
		ret+="]";
		return ret;
	}

}
