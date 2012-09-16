/**
 * 
 */
package net.niconomicon.tile.source.app.viewer.icons;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * @author Nicolas Hoibian
 * 
 */
public class IconsLoader {

	public ImageIcon ic_itouch_24_h;
	public ImageIcon ic_itouch_24_v;
	// public ImageIcon ic_itouch_24;
	public ImageIcon ic_windowExpand_24;
	public ImageIcon ic_windowContract_24;

	public ImageIcon ic_delete_16;
	public ImageIcon ic_delete_24;
	public ImageIcon ic_stop_16;
	public ImageIcon ic_stop_24;
	public ImageIcon ic_remove_16;
	public ImageIcon ic_remove_24;
	
	public ImageIcon ic_edit_16;
	public ImageIcon ic_edit_24;
	public ImageIcon ic_save_16;
	public ImageIcon ic_save_24;
	public ImageIcon ic_zoom_16;
	public ImageIcon ic_zoom_24;
	public ImageIcon ic_zoomIn_16;
	public ImageIcon ic_zoomIn_24;
	public ImageIcon ic_zoomOut_16;
	public ImageIcon ic_zoomOut_24;

	static final String iconsLocation = "net/niconomicon/tile/source/app/viewer/icons/";
	private static IconsLoader icons;

	public static IconsLoader getIconsLoader() {
		if (icons == null) {
			icons = new IconsLoader();
		}
		return icons;
	}

	private IconsLoader() {
		URL url;

		url = this.getClass().getClassLoader().getResource(iconsLocation + "itouch24_h.png");
		ic_itouch_24_h = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "itouch24_v.png");
		ic_itouch_24_v = new ImageIcon(url);

		url = this.getClass().getClassLoader().getResource(iconsLocation + "windowContract24.png");
		ic_windowContract_24= new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "windowExpand24.png");
		ic_windowExpand_24= new ImageIcon(url);

		url = this.getClass().getClassLoader().getResource(iconsLocation + "Delete16.gif");
		ic_delete_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Delete24.gif");
		ic_delete_24 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Stop16.gif");
		ic_stop_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Stop24.gif");
		ic_stop_24 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Remove16.gif");
		ic_remove_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Remove24.gif");
		ic_remove_24 = new ImageIcon(url);

		url = this.getClass().getClassLoader().getResource(iconsLocation + "Edit16.gif");
		ic_edit_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Edit24.gif");
		ic_edit_24 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Save16.gif");
		ic_save_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Save24.gif");
		ic_save_24 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Zoom16.gif");
		ic_zoom_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Zoom24.gif");
		ic_zoom_24 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "ZoomIn16.gif");
		ic_zoomIn_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "ZoomIn24.gif");
		ic_zoomIn_24 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "ZoomOut16.gif");
		ic_zoomOut_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "ZoomOut24.gif");
		ic_zoomOut_24 = new ImageIcon(url);
		
	}
}
