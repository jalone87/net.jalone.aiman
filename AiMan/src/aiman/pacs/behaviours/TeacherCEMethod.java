package aiman.pacs.behaviours;

import java.util.LinkedList;

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
	private static final int	T 		= 50;	
	
	/**population size, possible ways of drawings M elements from pool into slots, was 1000*/
	private static final int 	N 		= 200;		
	
	/**selection ratio */
	private static final double rho 	= 0.05; 	
	
	/**step size per-episode ( the per-instance one alfaPrime = alpha/(rho*N) ) */
	private static final double alfa	= 0.6;		
	
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
	private int cemi = 0; 	
	
	/**samples storage*/
	//TODO private Map<Policy, Integer> samples; 
	private LinkedList<Integer> samples;  

	public TeacherCEMethod(int Mm, int Kk){
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
		samples = new LinkedList<Integer>();
	}

	public void update(int score){  //per ogni partita giocata

		/*if( cemt < T ){
			if( cemi < N ){

				//add sample in the correct position
				//TODO use iterators
				int it = 0;
				boolean inserted = false;
				while(it<samples.size() && !inserted){
					if(score > samples.get(it)){
						samples.add(score);
					}
				}
				if(!inserted){
					samples.add(score);
				}
				cemi++;
			}else{
				int lowerEliteSample = Math.min(49, samples.size()-1); //rho*N - 1;
				int gamma = samples.get(lowerEliteSample); //TODO .score
				
				
				//update probp
				 for(ogni slot j-esimo in probp: p){
				 	int counter_p = 0;
				 	for(policy elite: policy){
				 		if(slot j in policy != null){
				 			counter_p++;
				 		}
				 	}
				 	p_j = counter_p / rho*N;
				 }
				
				//update probq
				for(ogni slot j-esimo in probq){
					int counter_q = 0;
					for(ogni regola k-esima in probq: q){
						if(slot j in policy == k){
				 			counter_q++;
				 		}
						q[j,k] = counter_q / rho*N;
					}
				}
				
				resetSamples();
				cemt++;
			}
		}else{
			//TODO trig learning-completed
		}*/

	}

	/*public void update(int score){

		if(numIteration < CEMiter){ //learning main loop

			if(i < N){ //if population not complete

				//draw xi from bernoulli of m (p of numIteration) (means get a policy from probability(t) fixed all along N) (~)
				//evaluate score (OK)
				//put score in array f ( should also reference which policy ) (todo)

				i++;

			}else{//population completed

				//order f (todo)
				//next epsilon = (f of rho) * N , set level threshold(???) 
				//next E = {x of i | f(x of i) >= next epsilon} , get elite samples (todo & ???)
				//update p' = ...
				//update next p = alpha * ...

				numIteration++;
			}


		}else{ //learning complete
			//TODO signal to Teacher or whatever to end.
		}

	}*/

	/*public void update(int score){

		if(numIteration < CEMiter){ //learning main loop

			int N = coeffBin();

				//estrarre N possibili combinazioni di regole
				//per ognuna delle possibili combinazioni calcolare lo score con quelle regole
				//ordinarle in base allo score e prenderne le prime rho*N (o quanti te ne pare)
				//queste fanno parte dell'insieme Elite


			//aggiornare i parametri della distribuzione: probp e probq
			//per ogni elemento j di probp, contare quant volte e' presente nelle combinazioni Elite
			//probp[j] = quante volte e' presente in tutte le regole della combnazion / numero di elementi di Elite (rho * N)
			//
			numIteration++;
		}else{ //learning complete
			//TODO signal to Teacher or whatever to end.
		}

	}*/

	public int coeffBin(){
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
	}

	//	public List<int> getExtractRules(){
	//		
	//	}


}
