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

	public ImageIcon ic_preferences_16;
	public ImageIcon ic_preferences_24;
	public ImageIcon ic_list_16;
	public ImageIcon ic_list_24;
	public ImageIcon ic_settings_24;
	
	public ImageIcon ic_loading_16;
	public ImageIcon ic_sharingOn_24;
	public ImageIcon ic_sharingOff_24;
	
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
		ic_windowContract_24 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "windowExpand24.png");
		ic_windowExpand_24 = new ImageIcon(url);

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

		url = this.getClass().getClassLoader().getResource(iconsLocation + "Preferences16.gif");
		ic_preferences_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "Preferences24.gif");
		ic_preferences_24 = new ImageIcon(url);

		url = this.getClass().getClassLoader().getResource(iconsLocation + "settings24.png");
		ic_settings_24 = new ImageIcon(url);

		url = this.getClass().getClassLoader().getResource(iconsLocation + "List16.png");
		ic_list_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "List24.png");
		ic_list_24 = new ImageIcon(url);

		url = this.getClass().getClassLoader().getResource(iconsLocation + "Loading16.gif");
		ic_loading_16 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "SharingOn24.png");
		ic_sharingOn_24 = new ImageIcon(url);
		url = this.getClass().getClassLoader().getResource(iconsLocation + "SharingOff24.png");
		ic_sharingOff_24 = new ImageIcon(url);

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
