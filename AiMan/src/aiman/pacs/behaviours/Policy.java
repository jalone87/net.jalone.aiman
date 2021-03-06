package aiman.pacs.behaviours;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import ec.util.MersenneTwisterFast;

import net.jalone.jul4j.logging.Logger;
import aiman.pacs.AutoPac;

/**
 * Manage the list( priorities and values ) of both policies and actions
 * Keep a reference to an ObservationsHandler to pick meaningful informations from the context
 * 
 * TODO the class has a low cohesion. divide et impera. ( RandomPac does not need to update and create observations or pools )
 * 
 * @author Lorenzo Tognalini, http://www.jalone.net
 *
 */
public class Policy {
	

	private MersenneTwisterFast randomFast;
	
	private Map<String, Boolean> actions;	/** available actions */
	private Rule[] rules;					/** ordered, keep selected policies*/
	private ObservationsHandler obsHandler;
	
	public Policy(ObservationsHandler obsHandler, int numberOfRulesSlot){
		
		this.obsHandler = obsHandler;
		
		this.actions = new LinkedHashMap<String, Boolean>(); //SHOULD be ordered map see SortedMap in future if needed to move items
		this.actions.put(AutoPac.ACTION_TO_DOT, false);
		this.actions.put(AutoPac.ACTION_TO_POWERDOT, false);
		this.actions.put(AutoPac.ACTION_FROM_POWERDOT, false);
		this.actions.put(AutoPac.ACTION_TO_EDGHOST, false);
		this.actions.put(AutoPac.ACTION_FROM_GHOST, false);
		//this.actions.put(AutoPac.ACTION_TO_SAFE_JUNCTION, false);
		//this.actions.put(AutoPac.ACTION_FROM_GHOST_CENTER, false);
		this.actions.put(AutoPac.ACTION_KEEP_DIRECTION, false);
		//this.actions.put(AutoPac.ACTION_TO_LOWER_GHOST_DENSITY, false);
		//this.actions.put(AutoPac.ACTION_TO_GHOST_AREA_FREE, false);

		this.rules = new Rule[numberOfRulesSlot];
		Arrays.fill(this.rules, null);		

	}
	
	/** return the number of the rules slots */
	public int getNumberOfRulesSlot(){
		return rules.length;
	}

	/**
	 * check wether a rule exists and has id ruleId
	 * @return true if rule exists and has id ruleId
	 */
	public boolean hasRuleAt(int ruleIndex, int ruleId){
		if(isRuleSlotNull(ruleIndex)){
			return false;
		}
		return rules[ruleIndex].getId() == ruleId;
	}
	
	/** */
	public boolean isRuleSlotNull(int slotIndex){
		if(slotIndex < rules.length){
			return rules[slotIndex] == null;
		}else{
			throw new IndexOutOfBoundsException("Policy rules slot index " + slotIndex + "is out of bound.");
		}
	}
	
	
	/**
	 * an initialized rule r is pushed in the rules pool at a given position (replacing the existing one if any)
	 * @param rule an initialized Rule
	 */
	public void pushRule(Rule rule, int index){
		this.rules[index] = rule;
	}
	
	/**
	 * an initialized rule r is pushed in the rules pool in the first available (null) slot
	 * @param rule an initialized Rule
	 */
	public void pushRule(Rule rule){
		for(int index = 0; index < this.rules.length; index++){
			if(this.rules[index] == null){
				this.pushRule(rule, index);
				return;//function exit here if empty slot found
			}
		}
		throw new IndexOutOfBoundsException("No empty slot of " + this.rules.length + " found in Policy!");
	}
	
	public int size(){
		return this.rules.length;
	}
	
	public void clear(){
		Arrays.fill(this.rules, null);
		for(Map.Entry<String, Boolean> e: actions.entrySet()){
			e.setValue(false);
		}
	}
	
	/**
	 * return a random action string totally ignoring actual state
	 * @return
	 */
	public String getRandomAction(){
		randomFast = new MersenneTwisterFast();
		int number = randomFast.nextInt(actions.size());
		return (String)actions.keySet().toArray()[number];
	}
	
	/**
	 * what this method do is
	 * ->update the observations
	 * ->switch action FOR EACH RULE
	 * 
	 * no need to call this if the policy is blind ( wrt the state) or has no memory )
	 * 
	 * TODO if a switch on requires to switch off all remaining then add a break ( or similar ) to first positive result inside for? 
	 */
	public void update(){
		obsHandler.update();
		
		for(Rule rule: rules){ 
			if(rule != null){
				rule.switchAction(this);
			}
		}
		//Logger.getInstance().log(this.toString());
	}
	
	/**
	 * swich an action of the policy to true or false 
	 * @param action
	 * @param switchTo
	 */
	public void switchAction(String action, boolean switchTo){
		if(actions.containsKey(action)){
			this.actions.put(action, switchTo);
		}else{
			throw new RuntimeException("PoliciesManager: Action to switch not Found");
		}
	}
	
	/**
	 * return last action set to true in the ordered actions List 
	 * TODO find a proper way to order item and return the one with higher priority
	 * TODO before returning picked action check if action.isAppliable() is true else pick next and check
	 * @return
	 */
	public String pickAction(){
		String action = null;
		for(Map.Entry<String, Boolean> e: actions.entrySet()){
			if(e.getValue()){
				action = e.getKey();
				Logger.getInstance().log(printState() + " : " + e.getKey());
			}
		}
		if(action != null){
			return action;
		}
		Logger.getInstance().log(printState() + " : " + AutoPac.NO_ACTION);
		Logger.getInstance().log("No action in policy or no action switched on. ");
		return AutoPac.NO_ACTION;
	}
	
	public double observe(String observation){
		return obsHandler.observe(observation);
	}
	
	public String printState(){
		String ret = "Policy[";
		int activated = 0;
		for(Map.Entry<String, Boolean> entry: actions.entrySet()){
			if(entry.getValue()){
				activated = 1;
			}else{
				activated = 0;
			}
			ret = ret.concat(String.format("%s: %d, ", entry.getKey(), activated));
		}
		ret = ret.concat("]");
		return ret;
	}

	@Override
	public String toString(){
		String ret = "PolicyRules{";
		for(Rule rule: rules){
			ret += rule;
			ret += ", ";
		}
		ret += "}";
		return ret;
	}

}
