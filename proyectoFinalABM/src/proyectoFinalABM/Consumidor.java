package proyectoFinalABM;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cern.jet.random.Normal;
import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

public class Consumidor {
	Parameters params = RunEnvironment.getInstance().getParameters();
	///////Escribimos las variables de instancia/////////////
	/**Variable para asignar identifiadores unicos a cada consumidor*/
	public static int IDconsumidores=0;
	
	/**Identificador unico de los consumidores*/
	private int IDconsumidor;
	
	/** Variable que captura el tick desde que se creo el agente */
	private int 			t;
	
	/** Variable que captura si la region en la que vive es rural o no.*/
	private boolean 		regionRural;
	
	/** Variable que captura el ingreso inicial, el cual depende de la region en la que vive. */
	private double 			ingresoInicial;
	
	/** Variable que captura su ingreso del periodo t.*/
	private double 			ingreso;
	
	/** Variable que captura su cuota de consumo inicial y que depende de datos de la ENIGH. */
	private double 			cuotaConsumoInicial;
	
	/** Variable que captura la cuota de consumo con la que debe cumplir en su periodo t. */
	private double 			cuotaConsumo;
	
	/** Variable que captura la deuda de una persona.*/
	private double			deuda;
	
	/** Variable que captura el estatus financiero de una persona del periodo t-1, incluye deudas, activos del banco y dinero en el colchon.*/
	private double			estatusFinanciero;
	
	/** Variable que captura el historial crediticio de la persona, va de 0 a 10. */
	private int				historialCrediticio;
	
	/** Variable que captura el dinero  que se tiene en una inversiÛn riesgosa y que generara ciertas ganancias. */
	private double			dineroBanco;
	
	/** Variable que captura el total del dinero disponible del individuo. */
	private double			dineroDisponible;

	/** Espacio continuo donde se encuentra el consumidor. */
	private ContinuousSpace<Object> space;
	
	/**Banco con el cual esta relacionado el consumidor */
	private Banco bancoRelacionado;
	
	/** Grid, lo que nos dara las distintas regiones donde se puede encontrar el consumidor. */
	private Grid<Object> grid;
	
	public GridValueLayer currentRegion;
	
	
	
	/**
	 * Constructor del consumidor.
	 * @param space Espacio continuo donde se encuentra el consumidor.
	 * @param grid  Grid donde se encuentra el consumidor.
	 */
	public Consumidor(ContinuousSpace<Object> space, Grid<Object> grid, GridValueLayer currentRegion) {
		this.space=space;
		this.grid=grid;
		this.currentRegion=currentRegion;
		this.historialCrediticio=10;
		this.dineroBanco=0; 
		this.dineroDisponible=0;
		this.estatusFinanciero=0;
		Consumidor.IDconsumidores++;
		this.IDconsumidor=Consumidor.IDconsumidores;
		this.t=1;
		}
	
	
	/**
	 * Metodo que 
	 * inicializa la region, el ingreso inicial y el consumo inicial 
	 * de cada individuo recien creado
	 */
	@ScheduledMethod(start=1, interval=1, shuffle=true,priority=1000)
	public void stepInitialValues() { //revisado
		//para que solo se ejecute el primer tick que vive el individuo
		if(this.t==1) {
		this.regionRural=this.tipoRegion();		//revisado
		this.ingresoInicial=this.ingresoInicial(); //revisado
		this.cuotaConsumoInicial=this.cuotaConsumoInicial(); //revisado
		}
		this.t++;
	} 
	
	
	/** Metodo que se ejecuta en cada tick y que devuelve el ingreso */
	@ScheduledMethod(start=1,interval=1,shuffle=true,priority=900)
	public void stepDeterminarIngresoYCuotaConsumoActual() { //revisado
		double sdIngreso=params.getDouble("sdIngreso");
		this.ingreso = RandomHelper.createNormal(this.ingresoInicial,sdIngreso*this.ingresoInicial).nextDouble();
		this.cuotaConsumo=this.consumoActual(); //revisado
		}
	
	@ScheduledMethod(start=1,interval=1,shuffle=true,priority=600)
	public void stepDecisionesFinancieras() {
		this.dineroDisponible=this.dineroDisponible();
		this.bancoRelacionado=this.bancoRelacionado();
		this.dineroBanco=this.dineroBanco();
		this.deuda=this.getMontoDeuda();
		this.dineroDisponible=this.dineroDisponible+this.estatusFinanciero;
		//System.out.printf("%n Deuda: %s, dineroBanco: %s", this.deuda,this.dineroBanco);
		//La l�nea de arriba lo que hace es restar el estatus financiero anterior
	}
	
	@ScheduledMethod(start=1,interval=1,shuffle=true,priority=500)
	public void stepDineroDisponibleFinalDelPeriodo() {
		//Aqui volveremos a calcular el dinero disponible, pero ahora con la deuda y la inversi�n real, la cual es seteada por el banco
		this.dineroDisponible=this.dineroDisponible-this.dineroBanco+this.deuda-this.cuotaConsumo;
	}
	
	@ScheduledMethod(start=1,interval=1,shuffle=true,priority=400)
	public void stepDieOrNot() {
		if(this.dineroDisponible<0) {
			Context<Object> context=ContextUtils.getContext(this);
			context.remove(this);
			//System.out.println("Murio un consumidor");
			Consumidor consumidorNuevo = new Consumidor(space,grid,currentRegion);
			context.add(consumidorNuevo);
			//y lo movemos a su locación correspondiente en el grid
			NdPoint  pt =space.getLocation(consumidorNuevo);
			grid.moveTo(consumidorNuevo, (int)pt.getX(), (int)pt.getY());
			//eliminamos al consumidor muerto del contexto
			}		
	}
	
	//Métodos auxiliares
	
	/**
	 * Metodo de Ingreso Inicial
	 * @return Un decimal que representa el ingreso con el que nace un consumidor y que depende de si vive en una zona rural o urbana.
	 */
	private double ingresoInicial() {
		if(this.regionRural==false) {
			return 30808*Math.pow(Math.pow(Math.random(), -1/0.296)-1, (-1/4.873));
		}
		else {
			return 18146*Math.pow(Math.pow(Math.random(), -1/0.61)-1, (-1/1.998));
		}
	}
	
	/**
	 * Metodo para obtener region
	 * @return Tipo de region
	 */
	private boolean tipoRegion() {
		boolean rural=false;
		GridPoint pt=grid.getLocation(this);
		if(this.currentRegion.get(pt.getX(),pt.getY())==1) {
			rural=true;
		}
		return rural;
	}
	
	/**
	 * Metodo para obtener la cuota de consumo inicial
	 * @return Un decimal que representa la cuota de consumo con el que nace un consumidor y que depende de si vive en una zona rural o urbana.
	 */
	private double cuotaConsumoInicial() {    //revisado
		double x=this.ingresoInicial;
		if(this.regionRural==true) {
			return x*(params.getDouble("cuotaConsumoPorcentajeIngresoRural"));
		}
		else {
			return x*(params.getDouble("cuotaConsumoPorcentajeIngresoUrbano"));
		}
	}
	
	
	/**
	 * Metodo para obtener el consumo del periodo t.
	 * @return Decimal que corresponde al consumo del periodo t, la cual se ve influenciada por la region en la que habita.
	 */
	private double consumoActual() { //revisado
		//desviacion estandar de la distribucion normal de la cuota de consumo 
		double sdCuotaConsumo;
		if(this.regionRural==true) {
			sdCuotaConsumo=params.getDouble("sdCuotaConsumoRural");
		}
		else {
			sdCuotaConsumo=params.getDouble("sdCuotaConsumoUrbana");
		} 
		return RandomHelper.createNormal(this.cuotaConsumoInicial,sdCuotaConsumo*this.cuotaConsumoInicial).nextDouble();
	}
	
	/**
	 * Metodo que nos dice, el dinero disponible del consumidor.
	 * @return Decimal que representa el dinero disponible. 
	 */
	private double dineroDisponible() {
		//
		return this.dineroDisponible + this.ingreso;
	}
	
	
	/**
	 * Metodo que nos dice la deuda que desea contraer en el periodo t el consumidor.
	 * @return Decimar que representa la deuda.
	 */
	private double montoDeuda() {
		double prestamoRequerido=0;
		if(this.dineroDisponible-this.cuotaConsumo <= 0&&this.bancoRelacionado!=null) {
			prestamoRequerido=this.cuotaConsumo-this.dineroDisponible+RandomHelper.nextDoubleFromTo(0,.1*(this.cuotaConsumo-this.dineroDisponible));
		}
		return prestamoRequerido;
	}
	
	/**
	 * Metodo que nos dice el dinero invertido en el banco.
	 * @return Decimal que representa el dinero que se invierte en el banco si el dinero disponible del consumidor es mayor a 1 
	 * desviaciÛn est·ndar de la cuota de consumo.
	 */
	private double dineroBanco() {
		double dineroBanco=0;
		if(this.bancoRelacionado!=null&&this.dineroDisponible-this.cuotaConsumo>0) {
			dineroBanco=this.dineroDisponible-this.cuotaConsumo-RandomHelper.nextDoubleFromTo(0, this.dineroDisponible-this.cuotaConsumo);
		}
		return dineroBanco;
	}
	
	
	/**
	 * Metodo que devuelve el Banco, en caso de existir, con el que se encuentra relacionado nuestro consumidor.
	 * @return Banco en la misma ciudad con la mejor tasa pasiva o activa, y m·s cercano, seg˙n corresponda.
	 */
	private Banco bancoRelacionado() {
		double educacionFinanciera;
		if(this.regionRural==true) {
			educacionFinanciera=1000;
		}
		else {
			educacionFinanciera=500;
		}
		Banco bancoRelacionado=null;
		GridPoint pt=grid.getLocation(this);
		//Coordenadas del consumidor
		int xc=pt.getX();
		int yc=pt.getY();
		List<Object> banks = new ArrayList<Object>();
		//Creamos las variables que utilizaremos para comparar a los bancos en el mismo grid
		double rp=0;
		double ra=Double.MAX_VALUE;
		for(Object o:grid.getObjectsAt(xc,yc)){
			if(o instanceof Banco) {
				banks.add((Banco)o);
				}
			}
		
		if(banks.size()>0) {
			for(Object o:banks) {
				//Si quiere ser inversionista
				if(this.dineroDisponible-this.cuotaConsumo>0) {
					double tp=((Banco)o).getTasaPasiva();
					if(tp>rp) {
						rp=tp;
						bancoRelacionado=(Banco)o;
				}
				if(tp==rp) {
					double d1=space.getDistance(space.getLocation(this), space.getLocation(bancoRelacionado));
					double d2=space.getDistance(space.getLocation(this), space.getLocation(o));
					if(d2<d1) {
						bancoRelacionado=(Banco)o;
					}
				}
			}
				//Si quiere algun prestamo
				if(this.dineroDisponible-this.cuotaConsumo<=0){
					if(this.historialCrediticio>=((Banco)o).getPuntajeCrediticioMinimo()) {
					double ta=((Banco)o).getTasaActiva();
					if(ta<ra) {
						ra=ta;
						bancoRelacionado=(Banco)o;
					}
					if(ta==ra) {
						double d1=space.getDistance(space.getLocation(this), space.getLocation(bancoRelacionado));
						double d2=space.getDistance(space.getLocation(this), space.getLocation(o));
						if(d2<d1) {
							bancoRelacionado=(Banco)o;
						}	
					}
					}
				}
			}
		}
		return bancoRelacionado;
	}
	

	
	
	//Metodos get y set
	/**
	 * Metodo para dar un valor al estatus financiero
	 * @param estatusFinanciero Decimal decidido por el banco.
	 */
	public void setFinancialStatus(double estatusFinanciero) {
		this.estatusFinanciero = estatusFinanciero;
	}
	
	public double getFinancialStatus() {
		return this.estatusFinanciero;
	}
	
	/**
	 * Metodo para obtener el historial crediticio
	 * @return CalificaciÛn crediticio
	 */
	public int getHistorialCrediticio() {
		return this.historialCrediticio;
	}
	
	/**
	 * Metodo que permite observar el dinero disponible, para que se llamde desde otras clases
	 * @return
	 */
	public double getDineroDisponible() {
		return this.dineroDisponible;
	}
	
	public int getIDconsumidor() {
		return this.IDconsumidor;
	}
	
	public String getTipoRegion() {
		if (this.regionRural==true) return "Rural";
		else return "Urbano";
	}
	

	public double getIngresoInicial() {
		return this.ingresoInicial;
	}
	
	
	/**Metodo que le quita un puntaje crediticio al consumidor, cuando este no
	 * cumplio con sus obligaciones financieras */
	public void setPuntajeCrediticio() {
		this.historialCrediticio--;
	}	
	
	//Metodo para que el banco vea el banco relacionado del consumidor
	public Banco getBancoRelacionado() {
		return this.bancoRelacionado; 
	}
	
	public double getMontoDeuda() {
		return this.deuda;
	}
	
	public void setDeuda(double deuda) {
		this.deuda=deuda;
	}
	
	public double getConsumoInicial(){
		return this.cuotaConsumoInicial;
	}
	
	public double getIngresoActual(){
		return this.ingreso;
	}
	
	public double getConsumoActual(){
		return this.cuotaConsumo;
	}
	
	public double getDineroBanco() {
		return this.dineroBanco;
	}
	
	public int getTickVivo() {
		return this.t;
	}
	
	public int getCoordenadaX() {
		GridPoint pt = this.grid.getLocation(this);
		int X = pt.getX();
		return X;
	}
	
	public int getCoordenadaY() {
		GridPoint pt = this.grid.getLocation(this);
		int Y = pt.getY();
		return Y;
	}
	

}