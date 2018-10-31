package moveOnGridAndDynamicDisplay;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.valueLayer.GridValueLayer;

public class Patch {
	
	public int value;
	public Grid<Object> grid;
	public GridValueLayer valueLayer;
	
	
	public Patch(Context context, int x, int y ){
		
		// First I add this new patch to the context
		context.add(this);
		
		// Get the projections
		Grid 			grid  = (Grid)context.getProjection("grid");
		ContinuousSpace space = (ContinuousSpace)context.getProjection("space");
		this.grid = grid;
		
	  // move the grass to its position on the patch grid
		grid.moveTo(this, x, y);   
		
    //  and to its position on the continuous space
		space.moveTo(this, x, y);				
		
		
		GridValueLayer vl = (GridValueLayer)context.getValueLayer("patchLayer");
		this.valueLayer = vl;
		vl.set(value, grid.getLocation(this).toIntArray(null));
		
	
	}
	
	
	@ScheduledMethod(start=1,interval=1,shuffle=true,priority=100)
	public void stepPatch() {
		
		// Wth probability 0.1%, the patch turns greeen (e.g. the value is above 0)
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		if(r<0.001) {
			this.value=5;
		}
			    
		this.valueLayer.set(value, this.grid.getLocation(this).toIntArray(null));
		}
	

}
