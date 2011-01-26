/**
 * 
 */
package net.niconomicon.tile.source.app.tiling;

/**
 * @author Nicolas Hoibian
 * 
 */
public interface TilingStatusReporter {

	public abstract void setTilingStatus(String text, double percent);
}
