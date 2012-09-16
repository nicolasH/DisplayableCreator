/**
 * 
 */
package net.niconomicon.tile.source.app.tiling;


/**
 * @author Nicolas Hoibian
 * This class is used to stop the creation of a displayable on user request, as it is a lengthy process.
 */
public interface Inhibitor {

//	boolean shouldStillRun = true;

	public void requestRunInhibition();
	public boolean hasRunInhibitionBeenRequested();
	
}
