package net.niconomicon.tile.source.app;

import java.util.Collection;

public interface DisplayablesSource {

	public Object getDisplayablesLock(); 
	public Collection<String> getDisplayables();
}
