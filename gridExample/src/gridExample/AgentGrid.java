package gridExample;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;

public class AgentGrid {
	
	private Grid<Object> grid;
	private ContinuousSpace<Object> space;
	Context<Object> context;
	public boolean occupied;
	
	
	public AgentGrid (Context<Object> context,Grid<Object> grid,ContinuousSpace<Object> space,int x,int y) {
		this.grid = grid;
		this.context = context;
		this.space = space;
		context.add(this);
		this.grid.moveTo(this, x,y);
		this.space.moveTo(this, x+0.5,y+0.5);
	}
	
	@ScheduledMethod(start=1,interval=1,shuffle=true,priority=10)
	public void checkColor() {
		GridPoint here = this.grid.getLocation(this);
		Iterable<Object> objects = grid.getObjectsAt(here.getX(),here.getY());
		boolean hasMovingAgent=false;
		for(Object o:objects) {
			if(o instanceof  AgentMoving) {
				hasMovingAgent=true;
				break;
			}
		}
		
		this.occupied = hasMovingAgent;
		
		
		
	}

}
