package gridExample;

import java.awt.Color;

import repast.simphony.visualizationOGL2D.DefaultStyleOGL2D;
import saf.v3d.scene.VSpatial;


/**
 * Style for Grass value layer in 2D display.
 * 
 * @author Eric Tatara
 */
public class AgentGridStyle extends DefaultStyleOGL2D {

	public float getCellSize() {
		return 15.0f;
	}
	
	@Override
	public Color getColor(Object o){
		
		AgentGrid a = (AgentGrid)o;
		if(a.occupied) {
			return Color.RED;
		}
		else {
			return Color.GREEN;
		}
	}
	
	@Override
	public VSpatial getVSpatial(Object agent, VSpatial spatial) {
	    if (spatial == null) {
	      spatial = shapeFactory.createRectangle(15, 15); //createCircle(4, 16);
	    }
	    return spatial;
	  }
}