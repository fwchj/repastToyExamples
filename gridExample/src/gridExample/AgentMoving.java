package gridExample;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;

public class AgentMoving {

	
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;

	private Context<Object> context;
	private double heading;
	
	public AgentMoving(Context<Object> context,ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.context = context;
		this.heading = RandomHelper.nextDoubleFromTo(0, 360);
		
	}
	
	
	@ScheduledMethod(start=1,interval=1,priority=20,shuffle=true)
	public void move() {
		
		// Randomly change the heading by +/- 30°
		this.heading += RandomHelper.nextDoubleFromTo(-30, 30);
		
		// Move 0.25 units
		this.space.moveByVector(this, 0.25, Math.toRadians(this.heading),0);
		// We also have to move it in the GridSpace
		NdPoint pos = space.getLocation(this);
		this.grid.moveTo(this,(int)pos.getX(),(int)pos.getY());
		
		
		
		
		
		
		
		
	}
	
}
