package gridExample;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class Builder implements ContextBuilder<Object>{

	@Override
	public Context build(Context<Object> context) {
		context.setId("gridExample");
		
		int xdim = 10;
		int ydim = 10;
		
		// Continuous Space
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, 
				new RandomCartesianAdder<Object>(), new repast.simphony.space.continuous.WrapAroundBorders(),xdim,ydim);
		
		
		// Grid space
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new WrapAroundBorders(),new SimpleGridAdder<Object>(),true,xdim,ydim));
		
		
		// Add a static AgentGrid to each cell of the grid (will play the role of the patch)
		for(int x=0;x<xdim;x++) {
			for(int y=0;y<ydim;y++) {
				new AgentGrid(context,grid,space,x,y);		
			}
		}
		
		// Add one agent in the continuous space
		context.add(new AgentMoving(context, space,grid));
			
		return context;
		
	}

}
