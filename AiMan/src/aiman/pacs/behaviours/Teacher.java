package aiman.pacs.behaviours;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.jalone.jul4j.logging.*;

import ec.util.MersenneTwisterFast;

import aiman.PacMan;
import aiman.pacs.AutoPac;
import aiman.pacs.LearningPac;
import aiman.pacs.behaviours.Condition.Direction;

/**
 * Class that keep pac learning data along the training and perform learning tasks
 *  
 *	//original exp: k=42, m=30
 *	//a policy score is the one of 3 games averaged
 *	//learning for 50 episodes
 * 
 * @author jalone
 *
 */
public class Teacher {
	
	public enum TeachingState{
		INITIALIZING,
		TEACHING,
		COMPLETE
	}
	
	private List<Integer> resultsHistory;
	private TeachingState state;
	private PacMan pacman;
	
	private MersenneTwisterFast random = new MersenneTwisterFast();
	
	/** size of the rulesSlot TODO should be final? can be edited during learning? */
	private int M = 5;
	
	/**  K is the rules pool size (number of rules in rulebase) */
	private int K;		
	
	/** contains all the rules, either used or not */
	private Map<Integer, Rule> rulesPool; 

	/** contain the actual rules used in the policy, if -1 slot is empty */
	private int rulesSlots[]; 
	
	/** the real policy to teach to pac */
	private Policy policy; 
	
	/** the real learning class using CEM (cross entrophy method) */
	private TeacherCEMethod cem; 
	
	/**
	 * ctor
	 * @param pacman (the game state, NOT the agent)
	 */
	public Teacher(PacMan pacman){
		
		this.pacman = pacman;
		state = TeachingState.INITIALIZING;
		this.resultsHistory = new LinkedList<Integer>();
		
		rulesSlots = new int[M]; 
		ObservationsHandler observator = new ObservationsHandler(this.pacman);
		policy = new Policy(observator, M);
		buildRuleBase(observator);
		cem = new TeacherCEMethod(M, K);
	}
	
	public void setState(TeachingState s){
		state = s;
	}
	
	/**
	 * evaluate the result of last experience and teach the pac if is teaching
	 * @param result of last game (until death)
	 */
	public void evaluateAndTeach(int score){
		FileLogger.getInstance().log("# Evaluating...");
		if(state == TeachingState.TEACHING){
			resultsHistory.add(score);
		}

		FileLogger.getInstance().log("score: " + score);

		if(state == TeachingState.TEACHING){
			teach(score);
		}
	}
	
	/** teach the pac (modify probabilities matrices) */
	private void teach(int score){
		FileLogger.getInstance().log("# Teaching...");
		GameOutcome outcome = new GameOutcome(policy, score);
		cem.update(outcome); //update probabilities matrices
	}
	
	/**
	 * based on the probability distribution probq 
	 * @param i the slot index
	 * @return the rule index in the rulesPool
	 */
	private int getSlotRuleIndex(int i){ 
		double sum = 0; //cdf
		double prob = random.nextDouble();
		//Logger.getInstance().log("Prob: " +prob);
		for(int j=0; j< K; j++){
			sum += cem.probq[i][j]; //TODO avoid acces to members of CEM
			//Logger.getInstance().log("CDF: " + sum);
			if(prob < sum){
				return  j;	//nth rule index
			}
		}
		throw new IndexOutOfBoundsException("Probability out of CDF");
	}
	
	public boolean isTeachingComplete(){
		return state == TeachingState.COMPLETE;
	}
	
	/**
	 * create a new policy and associate it with the pac
	 */
	public void createAndGivePolicy(LearningPac pac){
		
		if(cem.isFinished()){
			state = TeachingState.COMPLETE;
		}
		
		if(state == TeachingState.TEACHING){
			FileLogger.getInstance().log("# Teaching with policy:");
		}else if(state == TeachingState.COMPLETE){
			FileLogger.getInstance().log("# Playing with policy:");
		}
		
		//TODO this sucks, since each step rebuilds rulebase just cos observer for conditions change with game. does it?
		ObservationsHandler observator = new ObservationsHandler(this.pacman);
		policy = new Policy(observator, M);
		buildRuleBase(observator);

		//TODO rulesSlots should be in learningPac
		for(int i=0; i < M; i++){
			if( random.nextDouble() < cem.probp[i]){  //TODO avoid direct access to members of CEM
				rulesSlots[i] = getSlotRuleIndex(i);
			}else{
				rulesSlots[i] = -1;
			}	
		}
		
		//TODO this should not be in teacher, but in learningPac.adaptPolicy method, since its learning pac that create policy not teacher. 
		// teacher should only modify probabilities of pac??????
		if(state == TeachingState.TEACHING || state == TeachingState.COMPLETE){ //pick the next attempt to find a better policy
			for(int i=0; i<M; i++){
				if(rulesSlots[i] >= 0){
					Rule rule = rulesPool.get(rulesSlots[i]); 
					policy.pushRule(rule, i);
				}
			}
			FileLogger.getInstance().log("POLICY SIZE: " + policy.size());
			FileLogger.getInstance().log("POLICY: " + policy);
		}
		pac.adoptPolicy(policy);
	}
	
	/**
	 * create all the existing rules, wether used or not in learning or in any policy
	 */
	private void buildRuleBase(ObservationsHandler observator){
		//TODO better if from xml file
		this.rulesPool = new HashMap<Integer, Rule>();
		
		int id = 0;
		
		//to escape if ghost is close
		Rule rule = new Rule(id, AutoPac.ACTION_FROM_GHOST, true); 
		rule.pushCondition(new Condition(observator, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MIN, 4));
		rulesPool.put(id, rule);
		
		//don't escape if ghost far
		id++;
		rule = new Rule(id, AutoPac.ACTION_FROM_GHOST, false);
		rule.pushCondition(new Condition(observator, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MAX, 4));
		rulesPool.put(id, rule);

		//to closer energizer
		id++;
		rule = new Rule(id, AutoPac.ACTION_TO_POWERDOT, true);
		rule.pushCondition(new Condition(observator, ObservationsHandler.OBSERVATION_NEAREST_POWERDOT, Direction.MIN, 6));
		rulesPool.put(id, rule);
		
		//to closer energizer
		id++;
		rule = new Rule(id, AutoPac.ACTION_TO_POWERDOT, false);
		rule.pushCondition(new Condition(observator, ObservationsHandler.OBSERVATION_NEAREST_POWERDOT, Direction.MAX, 6));
		rulesPool.put(id, rule);

		//to closer dot
		id++;
		rule = new Rule(id, AutoPac.ACTION_TO_DOT, true);
		rule.pushCondition(new Condition(observator, ObservationsHandler.OBSERVATION_NEAREST_GHOST, Direction.MIN, 100)); //100 should be max map diagonal, but its ok
		rulesPool.put(id, rule);

		K = rulesPool.size();
		
	}
	
	/**
	 * 
	 */
	public void printResultsHistory(){
		if(resultsHistory.size() > 0){
			int i = 0;
			int lower = resultsHistory.get(0);
			int higher = 0;
			int average = 0;
			for(int r: resultsHistory){
				System.out.println("Res "+i+": "+r);
				average += r;
				if(r>higher)
					higher = r;
				if(r<lower)
					lower = r;
				i++;
			}
			System.out.println("Lower: " + lower);
			System.out.println("Average: " + (average/resultsHistory.size()));
			System.out.println("Higher: " + higher);
		}else{
			System.out.println("No evaluated results");
		}
	}
	
}
