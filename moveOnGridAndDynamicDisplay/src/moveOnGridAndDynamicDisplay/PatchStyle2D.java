package moveOnGridAndDynamicDisplay;

import java.awt.Color;

import repast.simphony.valueLayer.ValueLayer;
import repast.simphony.visualizationOGL2D.ValueLayerStyleOGL;

/**
 * Style for Grass value layer in 2D display.
 * 
 * @author Eric Tatara
 */
public class PatchStyle2D implements ValueLayerStyleOGL {

	protected ValueLayer layer;
	

	public void init(ValueLayer layer) {
		this.layer = layer;
	}

	public float getCellSize() {
		return 15.0f;
	}

	/**
	 * Return the color based on the value at given coordinates.
	 */
	public Color getColor(double... coordinates) {
		double v = layer.get(coordinates);
		
		if (v>0)
			return Color.GREEN;
		else 
			return Color.RED;
	}
}