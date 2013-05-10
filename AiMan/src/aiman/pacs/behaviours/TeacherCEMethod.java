package aiman.pacs.behaviours;

import java.text.DecimalFormat;
import java.util.LinkedList;

import net.jalone.jul4j.logging.FileLogger;

/**
 * this class perform the Cross Entrophy Method for the Pac Learning Task of the Teacher class
 * @author jalone
 *
 */
public class TeacherCEMethod {

	/** M is the number of slots */
	private int M; 	
	
	/** K is the number of rules */
	private int K;	

	//####CEM parameters
	
	/**CEM iteration main loop max iterations*/ //(T)
	private static final int	T 		= 20;//50;	
	
	/**population size, possible ways of drawings M elements from pool into slots, was 1000*/
	private static final int 	N 		= 200;//200;		
	
	/**selection ratio */
	//private static final double	 rho 	= 0.05; //TODO set properly
	//private static final int	 rhoInv	= 3;  //TODO set really inverse of rho
	private int numEliteSamples 		= 10;
	
	/**step size per-episode ( the per-instance one alfaPrime = alpha/(rho*N) ) */
	private static final double alpha	= 0.6;		
	
	/**decay rate, decreased value of p(j) for each step to not use a slot (jth) */
	private static final double beta 	= 0.98;		

	//#### CEM probabilities matrices //TODO make private and set accessors
	
	/** whether a slot i will be filled (with probability probp[i]) or not */
	public double probp[]; 
	
	/** probability distribution (normalized for each line) of wich rule to pick for each slot */
	public double probq[][]; 

	//#### CEM state
	
	/**learning main loop index */
	private int cemt = 0;	
	
	/**population pointer*/
	private int cemi; 	
	
	/** wether the method has finished iterations or not */
	private boolean teachingFinished;
	
	/**samples storage, keep ordered first samples have higher score, TODO make specific container*/
	private LinkedList<GameOutcome> outcomeSamples; 

	public TeacherCEMethod(int Mm, int Kk){
		this.teachingFinished = false;
		this.M = Mm;
		this.K = Kk;
		probp = new double[M];
		probq = new double[M][K];
		for(int i = 0; i<M; i++){
			probp[i] = 0.5;
		}
		double val = 1.0/K;
		for(int i=0; i<M; i++){
			for(int j=0; j<K; j++){
				probq[i][j] = val;
			}
		}
		resetSamples();
	}

	public void resetSamples(){
		cemi = 0;
		outcomeSamples = new LinkedList<GameOutcome>();
	}

	public void update(GameOutcome sample){  //per ogni partita giocata	

		cemi++; //a game has just been played

		FileLogger.getInstance().log(cemt + " " + cemi + " size: " + outcomeSamples.size());
		
		if( cemt < T ){ //until learning is finished

			insertSample(sample);
			
			if(cemi >= N){ //a learning iteration has been completed with the previous game, then elaborate those

				processEliteSamples();
				resetSamples();
				cemt++;
			}
			
		}

		if(cemt == T){
			FileLogger.getInstance().log();
			FileLogger.getInstance().log("$$$ Larning Completed $$$");
			FileLogger.getInstance().log();
			this.teachingFinished = true;
		}

	}
	
	/** order-insert sample in the samples contanier */
	private void insertSample(GameOutcome sample){
		//TODO optimize: use iterators?
		//TODO optimize: no need to order and store all the samples, only elite ones are needed
		int it = 0;
		boolean inserted = false;
		while(it < outcomeSamples.size() && !inserted){
			if(sample.getScore() > outcomeSamples.get(it).getScore()){
				outcomeSamples.add(it, sample);
				inserted = true;
			}
			it++;
		}
		if(!inserted){
			outcomeSamples.add(sample);
			inserted = true;
		}
	}
	
	/** update probabilities with elite samples extracted data. performed at end of each iteration */
	private void processEliteSamples(){
		
		int lowerEliteSampleIndex = numEliteSamples - 1; // outcomeSamples.size() - 1;//TODO reset Math.min((int)Math.round(N/rhoInv), outcomeSamples.size() - 1); //rho*N - 1 = 49?;
		int gamma = outcomeSamples.get(lowerEliteSampleIndex).getScore(); 

		FileLogger.getInstance().log();
		FileLogger.getInstance().log("LowerScore is: " + gamma);
		FileLogger.getInstance().log();	
		
		//update probp
		//for(ogni slot j-esimo in probp: p){
		for(int j = 0; j < this.probp.length; j++){
			int counter_p = 0;
		 	//for(policy elite: policy){
			for(GameOutcome sample: outcomeSamples){
				if(sample.getScore() < gamma){
					break;
				}
				Policy policy = sample.getPolicy();
	 			//if(slot j in policy != null){
				if(!policy.isRuleSlotNull(j)){
		 			counter_p++;
		 		}
		 	}
		 	probp[j] = 1.0 * counter_p / numEliteSamples;
		 	//LearningLogger.getInstance().log(counter_p + " " + numEliteSamples);
		 }
		 	
		//update probq
		//for(ogni slot j-esimo in probq){
		for(int j = 0; j < this.probq.length; j++){
			int counter_q = 0;
			//for(ogni regola k-esima in probq: q){
			for(int k = 0; k <this.probq[j].length; k++){
				for(GameOutcome sample: outcomeSamples){
					if(sample.getScore() < gamma){
						break;
					}
					Policy policy = sample.getPolicy();
					//if(slot j in policy == k){
					if(policy.hasRuleAt(j,k)){
			 			counter_q++;
					}
				}
				probq[j][k] = 1.0 * counter_q / numEliteSamples;
			}
			normalizeQ(j);
		}
	
		this.printProbabilities();

	}
	
	public void normalizeQ(int j){
		double sum = 0;
		for(int i = 0; i<probq[j].length; i++){
			sum += probq[j][i];
		}
		
		if(sum > 0.00001){
			//normalize
			for(int i = 0; i<probq[j].length; i++){
				probq[j][i] = probq[j][i] /sum;
			}
		}else{
			//back to uniform probability
			for(int i = 0; i<probq[j].length; i++){
				probq[j][i] = 1.0 / probq[j].length;
			}
		}
	}
	
	public boolean isFinished(){
		return this.teachingFinished;
	}
	
	public void printProbabilities(){	

		FileLogger.getInstance().log();
		LearningLogger.getInstance().log();
		
		
		//print probp
		String p = "";
		DecimalFormat decim = new DecimalFormat("0.00");
		for(int i = 0; i<probp.length; i++){
			p += Double.parseDouble(decim.format(probp[i])) + " ";
		}
		FileLogger.getInstance().log(p);
		FileLogger.getInstance().log();
		LearningLogger.getInstance().log(p);
		LearningLogger.getInstance().log();
		
		//print probq
		for(int i = 0; i<probq.length; i++){
			String q = "";
			for(int j = 0; j<probq[i].length; j++){
				q += Double.parseDouble(decim.format(probq[i][j])) + " ";
			}
			FileLogger.getInstance().log(q);
			LearningLogger.getInstance().log(q);
		}
		FileLogger.getInstance().log();
		LearningLogger.getInstance().log();
	}

	/*public int coeffBin(){
		int fk=1;
		for(int i=this.K; i >0; i--){
			fk=fk*i;
		}
		int fm=1;
		int fkm=1;
		for(int i=this.M; i>0; i--){
			fm=fm*i;
		}
		for(int i=this.K-this.M; i>0; i--){
			fkm=fkm*i;
		}
		return fk/(fm*fkm);
	}*/

}
