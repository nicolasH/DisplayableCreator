package net.niconomicon.tile.source.app.tiling;

/**
 * STRUCT class for transferring information, because static method would need
 * an instance of the encompassing classes, and Java methods can't return more
 * than one result. :-(
 * 
 * @author niko
 * 
 */
public class InspectionResult {
	public Object srcbuf, dstbuf;
	public int factor;

}
