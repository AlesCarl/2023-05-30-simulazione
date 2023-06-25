package it.polito.tdp.gosales.model;

import java.time.LocalDate;
import java.util.PriorityQueue;

import it.polito.tdp.gosales.dao.GOsalesDAO;
import it.polito.tdp.gosales.model.Event.EventType;

public class Simulatore {

	//parametri di ingresso
	private int anno;
	private int N;
	
	//parametri
	int avgD;
	int avgQ;
	private GOsalesDAO dao;
	double costoUnitario;
	double prezzoUnitario;
	double threshold;
	
	//variabili di uscita
	private int clientiTot;
	private int clientiSoddisfatti;
	private double costo;
	private double ricavo;
	
	//stato del mondo
	private int Q;
	
	//coda degli eventi
	PriorityQueue<Event> queue;

	public Simulatore(Retailers r, int anno, Products p, int n, int q, int nConnessi) {
		super();
		
		this.dao = new GOsalesDAO();
		this.anno = anno;
		N = n;
		Q = q;
		
		avgD = this.dao.getAvgD(r, p, anno);
		avgQ = this.dao.getAvgQ(r, p, anno);
		this.costoUnitario = p.getUnit_cost();
		this.prezzoUnitario = p.getUnit_price();

	// Probabilità pari a 20% + 1%*C con un massimo del 50% ....
		this.threshold = Math.min(0.2 + 0.1*nConnessi, 0.5);
	}
	
	
	
	/**
	 * metodo che popola la coda degli eventi
	 */
	public void popolaCoda() {
		this.queue = new PriorityQueue<Event>();
		
//eventi rifornimento... inizio di ogni mese
		for (int i = 1; i<=12; i++) {
			this.queue.add(new Event(EventType.RIFORNIMENTO,
					LocalDate.of(anno, i, 1)));
		}

		
//eventi vendita, che partono il 15.1 e si ripetono ogni avgD giorni, fino al 31.12
	    LocalDate data = LocalDate.of(anno, 1, 15);
		
		while(data.isBefore(LocalDate.of(anno, 12, 31))) {
			
			this.queue.add( new Event(EventType.VENDITA, data));
			data = data.plusDays(avgD);  // ocho
		}
	}
	
	
	
	
	public void processaEventi() {
		this.clientiSoddisfatti = 0;
		this.clientiTot = 0;
		this.costo = 0;
		this.ricavo = 0;
		
		while(!queue.isEmpty()) {
			Event e = queue.poll();
			
			switch(e.getType()) {

					
			case RIFORNIMENTO:
				double prob = Math.random();
					
				if(prob <= this.threshold) {
					Q += 0.8*N;
					this.costo += this.costoUnitario*0.8*N;
				}else {
					Q +=N;
					this.costo += this.costoUnitario*N;
				}
				break;

					
			case VENDITA:
				this.clientiTot++;

/** sono fatti un po a cazzo questi IF ... però vabbè è solo a scopo illustrativo */ 
					
			// se Q disp è > del 90% della domanda: cliente soddisfatto	
				if (Q >= 0.9*avgQ) {
					this.clientiSoddisfatti++;
				}
					
			// se Q disp è < della domanda: 	
				if(Q >=avgQ) {
					this.ricavo += this.prezzoUnitario*avgQ;
					Q-=avgQ;
					
			// se Q disp è < della domanda: vendo tutto 	
				}else {
					Q = 0;
					this.ricavo += this.prezzoUnitario*Q;
				}
				break;


					
			default:
				break;
			}
		}
	}
	
	
	
	public SimulationResult getSimulationResult() {
		System.out.println("Clienti tot.  = " + this.clientiTot);
		System.out.println("Clienti Sodd. = " + this.clientiSoddisfatti + "\n\n");
		return new SimulationResult(((double)this.clientiSoddisfatti) / ((double)this.clientiTot), 
				this.costo, this.ricavo, this.ricavo - this.costo);
	}
	
	
}
