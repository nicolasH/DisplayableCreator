/**
 * 
 */
package net.niconomicon.tile.source.app.tiling;

/**
 * @author Nicolas Hoibian
 * Used to report the progress status of the Displayable creation.
 */
public interface TilingStatusReporter {

	public abstract void setTilingStatus(String text, double percent);
}
