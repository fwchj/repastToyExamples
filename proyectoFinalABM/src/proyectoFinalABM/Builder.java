package proyectoFinalABM;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.environment.RunState;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.valueLayer.GridValueLayer;

public class Builder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("proyectoFinalABM");
		
		//Reseteamos el valor de IDconsumidores en cada run
		Consumidor.IDconsumidores=1;
		Banco.IDbancos=1;
		Parameters params = RunEnvironment.getInstance().getParameters();
		int xmax=params.getInteger("tamanyoMundo");
		int ymax=params.getInteger("tamanyoMundo");
		
		//Crear espaio continuo (mundo)
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.
				createContinuousSpaceFactory(null);
		ContinuousSpace<Object> mundo = spaceFactory.createContinuousSpace
				("mundo", context, new RandomCartesianAdder<Object>(), 
						new repast.simphony.space.continuous.WrapAroundBorders(), xmax,ymax);
		
		//Crear grid (ciudades)
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> ciudades = gridFactory.createGrid("ciudades", context, 
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(),true,xmax,ymax));
		
		//Crear layer value
		GridValueLayer currentRegion = new GridValueLayer("CurrentRegion", true,xmax,ymax);
		
		//Colocamos los valores de la region en el value layer
		for(int i=0;i<xmax;i++) {
			for(int j=0;j<ymax;j++) {
				int[] coord=new int[] {i,j};
				double x=RandomHelper.nextDoubleFromTo(0,1);
				if(x>.5) {
					currentRegion.set(1,coord);
				}
				else {
					currentRegion.set(0,coord);
				}
			}
		}
		
		//Crear agentes
		int consumidorCount=params.getInteger("numeroConsumidores");
		for(int i=0; i<consumidorCount; i++) {
			context.add(new Consumidor(mundo,ciudades,currentRegion));
		}
		
		//Crear bancos
		int numeroBancos=params.getInteger("numeroBancos");
		for(int i=0; i<numeroBancos; i++) {
			context.add(new Banco(mundo, ciudades));
		}
		
		//localizamos a los objetos del espacio continuo en el grid
		for(Object obj: context) {
			NdPoint pt = mundo.getLocation(obj);
			ciudades.moveTo(obj, (int) pt.getX(),(int)pt.getY());	
		}
			
		System.out.println("El builder corrio exitosamente");
		System.out.printf("Tamaño mundo (ContinuousSpace): %s %n", mundo.size());
		System.out.printf("Tamaño ciudades (grid): %s %n", ciudades.size());
		System.out.printf("Tamaño: %s %n", currentRegion.size());
		System.out.printf("Run %s: Context has been built successfully."
				+ " It's size is: %s\n",RunState.getInstance().getRunInfo().getRunNumber(),context.size());
		
		if(RunEnvironment.getInstance().isBatch()) {
			RunEnvironment.getInstance().endAt(100); 
		}
		else {
			RunEnvironment.getInstance().pauseAt(10);
		}
		
		return context;
	}

}
