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
	
	private Policy policiesManager;
	
	public Rule(String action, boolean switchTo, Policy policiesManager){
		
		this.conditions = new ArrayList<Condition>();

		this.action = action;
		this.switchTo = switchTo;
		
		this.policiesManager = policiesManager;
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
	
	public void switchAction(){
		if(check()){
			policiesManager.switchAction(action, switchTo);
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
