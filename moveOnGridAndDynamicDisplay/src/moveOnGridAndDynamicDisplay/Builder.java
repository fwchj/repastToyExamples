package moveOnGridAndDynamicDisplay;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;
import repast.simphony.valueLayer.GridValueLayer;

public class Builder implements ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		context.setId("moveOnGridAndDynamicDisplay");
		
		// Continuous Space
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(),50,50);
		
		// Grid space
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, new GridBuilderParameters<Object>(new WrapAroundBorders(),new SimpleGridAdder<Object>(),true,50,50));
		
		// value layer (for the colours of the patches)
		GridValueLayer vl = new GridValueLayer("patchLayer", true, 
				new repast.simphony.space.grid.WrapAroundBorders(),50,50);
		
		context.addValueLayer(vl);
		
		
		// Add the agents
		Parameters params = RunEnvironment.getInstance().getParameters();
		int nAgents = params.getInteger("nAgents");
		
		for(int i=0;i<nAgents;i++) {
			context.add(new Agent(space,grid));
		}
		
		// Add the patches
		for (int i=0; i<50; i++){
			for (int j=0; j<50; j++){
				Patch patch = new Patch(context,i,j);				// create a new grass
			}
		}
		
		System.out.printf("Grid-size:%s\n",grid.size());
		System.out.printf("Space-size:%s\n",space.size());
		
		
		
		return context;
	}

}
