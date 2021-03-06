package movingAgentColour;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.continuous.ContinuousSpace;

public class Agent {

	ContinuousSpace space;
	int energy;
	
	
	public Agent(int e, ContinuousSpace space) {
		this.space = space;
		this.energy =e;
		
	}
	
	@ScheduledMethod(start=1,interval=1,priority=100,shuffle=true)
	public void move() {
		double angle = Math.toRadians(RandomHelper.nextDoubleFromTo(0, 360));
		this.space.moveByVector(this, 1, angle,0);
		this.energy -=1;
	}
	
}
