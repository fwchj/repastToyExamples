package moveOnGridAndDynamicDisplay;



import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.valueLayer.GridValueLayer;
import repast.simphony.valueLayer.ValueLayer;

public class Agent {

	private double energy;
	private ContinuousSpace<Object> space;	// This is simply to know to which space it belongs
	private Grid<Object> grid;				// This is simply to know to which grid it belongs
	
	public Agent(ContinuousSpace<Object> space,Grid<Object> grid) {
		this.grid = grid;
		this.space = space;
		this.energy = 10;
		
		
	}
	
	
	@ScheduledMethod(start=1,interval=1,priority=90,shuffle=true)
	public void step() {
		
		// get current location
		NdPoint pt = space.getLocation(this);
		
		
		// Find neighbours (everywhere, of course, you could limit this to some distance)
		GridValueLayer vl = (GridValueLayer)ContextUtils.getContext(this).getValueLayer("patchLayer");
		NdPoint destination=null;				// set destination (to be found)
		double distance = Double.MAX_VALUE;		// we go for the closest neighbour
		for(int x=0;x<50;x++) {
			for(int y=0;y<50;y++) {
				double value = vl.get(x,y);		// check if there is a positive value to get
				if(value>0) {
					NdPoint dest = new NdPoint(x,y);			// define the potential destination
					
					if(space.getDistance(pt, dest)<distance) { // check if closer than the previously best path
						destination = dest;						// if true, then change the previously best to this
						distance = space.getDistance(pt, dest);
					}
				}
			}
			
		}
		
		if(destination!=null) { // if a destination was found => run
		
		// Get angle of movement
		double angle = SpatialMath.calcAngleFor2DMovement(space,pt,destination);
		
		// Move
		space.moveByVector(this, Math.min(Math.max(2, this.energy/10),space.getDistance(pt, destination) ), angle,0);
		
		// Reduce the energy for running
		this.energy-=0.1;
		
		
		// Now, check if we arrived 
		pt = space.getLocation(this);
		double valueAtDestintion = vl.get((int)pt.getX(),(int)pt.getY()); // check if the is something of value I can take
		if(valueAtDestintion>0) {
			this.energy+=valueAtDestintion;			// add the value at the destination 
			vl.set(0,(int)pt.getX(),(int)pt.getY());
			// Find the agent (patch) here and reset his 'value'
			Iterable<Object> patches = grid.getObjectsAt((int)pt.getX(),(int)pt.getY());
			for(Object p:patches) {
				if(p instanceof Patch) {
					Patch patch = (Patch)p;
					patch.value=0;
					break;
				}
			}
		}
		
		}
		
		
		
		
		
		

	   
	   
	   
	   

		
	}
	
	
	// Export the energy level of the Repast-GUI
	public double getEnergy() {
		return this.energy;
	}
	
	
}
