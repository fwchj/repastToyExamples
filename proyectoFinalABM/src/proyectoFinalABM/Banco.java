package proyectoFinalABM;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;

import repast.simphony.query.space.grid.GridCell;

import repast.simphony.space.SpatialMath;
  
public class Banco {
	Parameters params = RunEnvironment.getInstance().getParameters();
	/**Variable para asignar identificadores unicos a los bancos*/
	static int IDbancos;
	
	/**numero de ticks que un banco lleva vivo*/
	private int numeroTicksVivo=0;
	
	/**identificador unico del banco*/
	private int IDbanco;
	
	/**capital del banco*/
	private double capital;
	
	/**puntaje crediticio minimo que exige un banco de un consumidor para prestarle dinero*/
	private int puntajeCrediticioMinimo;
	
	/**espacio continuo donde vive el banco*/
	private ContinuousSpace<Object> space;
	
	/**grid donde vive el banco*/
	private Grid<Object> grid;
	
	/**tasa activa del banco (tasa a la que presta)*/
	private double tasaActiva;
	
	/**tasa pasiva del banco (tasa que el banco paga a sus inversionistas)*/
	private double tasaPasiva;
	
	 /**tipo de banco 
	  * banco tipo 1: ofrece mayores tasas activas y es mas flexible con el puntaje crediticio que acepta
	  * banco tipo 2: ofrece tasa activas mas bajas y exige puntajes crediticios mas altos*/
	private int tipoBanco;
	
	/** reserva minima (en porcentaje de capital total) que las autoridades monetarias exigen que los bancos tengan.
	 * Esta sirve como medida de prevencion ante situaciones donde los consumidores no cumplen
	 * con sus obligaciones crediticias, para que el banco si pueda seguir cumpliendo con las suyas*/
	double reservaMinima = params.getDouble("reservaMinima");
	
	/** reserva del banco en un periodo dado*/
	private double reserva;
	
	/**lista de  inversionistas del banco */
	public ArrayList<Consumidor> inversionistas = new ArrayList<Consumidor>();
	
	/**lista de deudores del banco*/
	public ArrayList<Consumidor> deudores = new ArrayList<Consumidor>();
	
	/**Constructor del banco
	 * @param space espacio continuo donde vive el banco
	 * @param grid  grid donde vive el banco */
	
	public Banco(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.reserva = reservaMinima*RandomHelper.nextDoubleFromTo(0.5, 2);
		this.capital=this.reserva;
		//parametrizamos el tipo de banco para hacer diferentes pruebas
		this.tipoBanco = RandomHelper.nextIntFromTo(params.getInteger("tipoBancoMin")
				,params.getInteger("tipoBancoMax"));
		Banco.IDbancos++;
		this.IDbanco=Banco.IDbancos;
		if(this.tipoBanco==1) {
			this.puntajeCrediticioMinimo=RandomHelper.nextIntFromTo(6,10);
			this.tasaActiva = RandomHelper.nextDoubleFromTo(0, 1);
			this.tasaPasiva = RandomHelper.nextDoubleFromTo(0,this.tasaActiva);
		}
		if(this.tipoBanco==2) {
			this.puntajeCrediticioMinimo=RandomHelper.nextIntFromTo(0,5);
			this.tasaActiva = RandomHelper.nextDoubleFromTo(0.5, 1);
			this.tasaPasiva = RandomHelper.nextDoubleFromTo(0,this.tasaActiva);
		}
	}
	
	
	//METODOS
	//Scheduled methods
	/**Itera sobre el ArrayList de los deudores del banco y (1) recauda el dinero que le deben, 
	 * (2) el banco actualiza su nivel de capital y (3) se actualiza el estatus financiero de los deudores*/
	//Tambien tiene que quitar de la lista de inversionistas a los que ya se les pago*/ 
	@ScheduledMethod(start=2, interval=1,shuffle=true, priority=810)
	public void stepCobrar() {
		if(this.deudores != null) {
			for(Consumidor este: this.deudores) {
				if(este != null) {
					/*si el dinero de los consumidores no es suficiente para pagar toda la deuda,
					 * el banco les quitara todo lo que tienen y les quitara un punto crediticioel banco ajusta su nivel de capital.*/
					//si el dinero que tiene el consumidor es menor que su deuda,
					//entonces el banco le cobra todo lo que el consumidor tiene
					if(este.getDineroDisponible()<Math.abs(este.getFinancialStatus())) {
						this.capital = this.capital + este.getDineroDisponible();	
						/*dado que la relacion banco-consumidor, cuando sea pertinente, se renueva
						 * cada periodo, actualiza el
						 * estatus financiero del Consumidor  */
						este.setFinancialStatus(-este.getDineroDisponible());
						este.setPuntajeCrediticio();	
					}
					else {
						this.capital = this.capital + Math.abs(este.getFinancialStatus());
						/*como el consumidor no le pago todo lo que le debia al banco, el banco lo castiga quitandole un punto crediticio*/	
					}
				}	
			}
		}
		//el banco lo quita de su lista de clientes 
		deudores.clear();
	}
	

	@ScheduledMethod(start = 2, interval = 1,shuffle=true, priority=800)
	/**Itera sobre la lista de los acreedores del banco, a quienes se les pagara en la 
	 * medida en que el nivel de capital del banco lo permita. Si el banco se queda sin capital, cierra.
	 * En este caso, los acreedores pierden la inversion que tenian*/
	public void stepPagar() {
		//primero se actualiza el numero de ticks que el banco lleva viviendo. Esto es independiente de este metodo, pero conviene ponerlo aqui
		this.numeroTicksVivo=this.numeroTicksVivo+1;
		if(inversionistas.size()>0) {
			for(Consumidor este: this.inversionistas) {
				//si el banco se queda sin capital, antes de cerrar, ajustara los estatus financieros de los inversionistas
				//a quienes no les pudo pagar. estos simplemente pierden su inversion 
				if(this.capital==0) {
					//el banco sin capital actualiza los estatus financieros de los consumidores a los que no les pudo pagar, antes de que cierre
					este.setFinancialStatus(0);
					}
				if(this.capital >= este.getFinancialStatus()) {
					this.capital = this.capital - Math.abs(este.getFinancialStatus());
					}
				//si no tiene suficiente, el banco unicamente paga lo que puede y cierra
				if(this.capital<este.getFinancialStatus() && this.capital>0) {
					este.setFinancialStatus(this.capital);
					this.capital=0;
					}	
			}
			//para el caso cuando el banco se queda sin capital y tiene que cerrar
			if(this.capital==0 && inversionistas.size()>0) {
				inversionistas.clear();
				cerrarBanco();
			}
			inversionistas.clear();
		}
	}
	

	@ScheduledMethod(start=1, interval =1,shuffle=true, priority= 550)
	/** el banco (1) recibe las inversiones de su casilla quienes estan interesados en invertir en el, 
	 * (2) actualiza su propio capital y (3) actualiza el estatus financiero del consumidor */
	public void recibirInversiones() {
		//vaciamos la lista de inversionistas para el nuevo periodo
		inversionistas.clear();
		for(Object o:this.grid.getObjects()) {
			//si el objeto es Consumidor y a este le interesa establecer una relacion de inversionista con ESTE banco...
			if(o instanceof Consumidor && ((Consumidor) o).getBancoRelacionado()==this 
					&& ((Consumidor)o).getDineroBanco()>0) {
				//el banco agrega al consumidor a su ArrayList de inversionistas
				this.inversionistas.add((Consumidor)o);
				//el banco incrementa su capital a partir de la inversion del consumidor inversionista
				this.capital = this.capital + ((Consumidor) o).getDineroBanco();
				//el banco actualiza el estatus financiero del consumidor
				((Consumidor) o).setFinancialStatus((1+this.tasaPasiva)*((Consumidor)o).getDineroBanco());
			}
		}
	}
	
	@ScheduledMethod(start=1, interval=1,shuffle=true, priority = 520)
	/**el banco (1) otorga prestamos a quienes lo solicitan (previamente ya se aprobo su
	 * eligibilidad para el banco, a traves de los mismos consumidores), (2) actualiza su propio
	 * capital y (3) actualiza el estatus financiero de los consumidores */
	public void otorgarPrestamos() {
		for(Object o:this.grid.getObjects()) {
			//FIXME: esta parte del codigo podria hacerse mejor usando Consumidor.class
			//si el objeto es Consumidor y le interesa que ESTE banco le preste dinero
			double deudaConsumidor=0;
			if (o instanceof Consumidor && ((Consumidor) o).getBancoRelacionado()==this &&
					((Consumidor)o).getMontoDeuda()>0) {
				//el banco debe revisar que su nivel de capital no sea menor al de su reserva minima
				if(this.capital - ((Consumidor)o).getMontoDeuda() > 0) {
					//el banco agrega al consumidor a su ArrayList de deudores
					deudaConsumidor=((Consumidor)o).getMontoDeuda();
					this.deudores.add((Consumidor)o);
					//System.out.printf("%nSe agrego al deudor con ID: %s; Tamanyo ALdeudores: %s", ((Consumidor)o).getIDconsumidor(), deudores.size());
					//el banco disminuye su nivel de capital conforme va prestando su dinero
				}
				//en caso de que no le pueda prestar todo lo que pide al Consumidor, le prestara todo lo que pueda (i.e., todo su capital)
					if(this.capital<((Consumidor)o).getMontoDeuda()&&this.capital>0) {
					deudaConsumidor=this.capital;
					this.deudores.add((Consumidor)o);
					System.out.printf("%nSe agrego al deudor con ID: %s; Tamanyo ALdeudores: %s", 
							((Consumidor)o).getIDconsumidor(), deudores.size());
				}
					//el banco actualiza la deuda y el estatus financiero del consumidor y actualiza su capital
					((Consumidor) o).setFinancialStatus(-(1+tasaActiva)*deudaConsumidor);	
					this.capital = this.capital - deudaConsumidor;
					((Consumidor)o).setDeuda(deudaConsumidor);
			}
		}	
	}

	
	/**el banco ajusta su tasa pasiva cuando su capital es menor a su reserva minima, cuidando que esta 
	 * no quede por encima de la activa*/
	@ScheduledMethod(start=1, interval =1,shuffle=true, priority = 710)
	public void ajustarTasaPasiva() {
		if(this.capital<0.9*this.reservaMinima) {
			this.tasaPasiva = RandomHelper.nextDoubleFromTo(this.tasaPasiva, this.tasaActiva);
		}
	}
	
	@ScheduledMethod(start=2, interval=1,shuffle=true, priority =700)
	/**Ajusta la posición del banco. Si el banco no está «contento», buscará moverse a un lugar donde haya más consumidores, porque
	 esto aumenta la posibilidad de llevar a cabo transacciones exitosas. Si el banco esta por debajo de su reserva minima, entonces 
	 necesita inversionistas. Por tanto, buscara moverse a lugares donde la gente tiene mas dinero, que tipicamente son los que tienen una mayor
	 poblacion. Si el banco esta por arriba de su reserva minima, necesita prestar mas prestatarios. Por ello, buscara
	 moverse a los lugares con una menor densidad de poblacion (pero no vacios), pues en estos la gente
	 tipicamente pide mas dinero prestado */
	public void ajustarPosicion() {
		//esta variable sirve para modelar si el banco se encuentra cierto porcentaje por encima de su reserva minima...
		double umbralAR=params.getDouble("umbralAlrededorReserva");
		if(this.capital>(1+umbralAR)*reservaMinima) {
			//
			GridPoint pt =grid.getLocation(this);
			GridCellNgh<Consumidor> nghCreator = new GridCellNgh<Consumidor>(grid, pt,
					Consumidor.class, 1, 1);
			List<GridCell<Consumidor>> gridCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
			GridPoint lugarConMasConsumidores = null;
			int maxCount =-1;
			for(GridCell<Consumidor> cell: gridCells) {
				if(cell.size()>maxCount) {
					lugarConMasConsumidores = cell.getPoint();
					maxCount = cell.size();
				}	
			}
			//Movemos al banco al lugar con más consumidores del grid
			grid.moveTo(this, lugarConMasConsumidores.getX(),
					lugarConMasConsumidores.getY());
			//Y actualizamos su posición en el espacio continuo
			space.moveTo(this, grid.getLocation(this).getX(),
					grid.getLocation(this).getY());
				
		}
		
		//si el capital del banco es mas chico que su reserva minima establecida...
			if(this.capital<(1-umbralAR)*reservaMinima) {
				GridPoint pt =grid.getLocation(this);
				GridCellNgh<Consumidor> nghCreator = new GridCellNgh<Consumidor>(grid, pt,
						Consumidor.class, 1, 1);
				List<GridCell<Consumidor>> gridCells = nghCreator.getNeighborhood(true);
				SimUtilities.shuffle(gridCells, RandomHelper.getUniform());
				GridPoint lugarConMenosConsumidores = null;
				int minCount =Integer.MAX_VALUE;
				for(GridCell<Consumidor> cell: gridCells) {
					if(cell.size()>0 && cell.size()<minCount) {
						lugarConMenosConsumidores = cell.getPoint();
						minCount = cell.size();
					}	
				}
				//Movemos al banco al lugar con más consumidores del grid
				grid.moveTo(this, lugarConMenosConsumidores.getX(),
						lugarConMenosConsumidores.getY());
				//Y actualizamos su posición en el espacio continuo
				space.moveTo(this, grid.getLocation(this).getX(),
						grid.getLocation(this).getY());
			}	
	}


	//Métodos auxiliares
	/** se cierra el banco y se genera uno que lo reemplace*/
	public void cerrarBanco() {
		//se genera un nuevo banco
		Context<Object> context=ContextUtils.getContext(this);
		Banco bancoNuevo = new Banco(space,grid);
		context.add(bancoNuevo);
		NdPoint  pt =space.getLocation(bancoNuevo);
		grid.moveTo(bancoNuevo, (int)pt.getX(), (int)pt.getY());
		
		//se cierra el banco muerto
		//System.out.println("\nSe murio un banco");
		context.remove(this);
		
	}
	
	
	//Metodos get()
 	public double getCapital() {
		return this.capital;
	}
	
	/**devuelve el puntaje crediticio minimo que debe tener un consumidor para que el banco le preste*/
	public int getPuntajeCrediticioMinimo() {
		return this.puntajeCrediticioMinimo;
	}
	
	public double getTasaActiva() {
		return this.tasaActiva;
	}
	
	public double getTasaPasiva() {
		return this.tasaPasiva;
	}
	
	public int getIDbanco() {
		return this.IDbanco;
	}
	
	public int getCoordenadaXBanco() {
		return grid.getLocation(this).getX();
	}
	
	public int getCoordenadaYBanco() {
		return grid.getLocation(this).getY();
	}
	
	public int getTipoBanco() {
		return this.tipoBanco;
	}
	
	public int getNumeroTickVivo() {
		return this.numeroTicksVivo;
	}
	
	public int tamanyoListaDeudores() {
		return deudores.size();
	}
	
	public int tamanyoListaInversionistas() {
		return inversionistas.size();
	}
	
}
