/*
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2010 Benjamin Sigg
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Benjamin Sigg
 * benjamin_sigg@gmx.ch
 * CH - Switzerland
 */
package bibliothek.gui.dock.common.perspective;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import bibliothek.gui.Dockable;
import bibliothek.gui.dock.DockElement;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CStation;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CSetting;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.facile.mode.CLocationModeSettings;
import bibliothek.gui.dock.facile.mode.Location;
import bibliothek.gui.dock.facile.mode.LocationSettingConverter;
import bibliothek.gui.dock.frontend.FrontendPerspectiveFactory;
import bibliothek.gui.dock.frontend.Setting;
import bibliothek.gui.dock.perspective.Perspective;
import bibliothek.gui.dock.perspective.PerspectiveElement;
import bibliothek.gui.dock.support.mode.ModeSettings;
import bibliothek.util.Todo;

/**
 * A {@link CControlPerspective} is a wrapper around a {@link CControl} allowing
 * access to various {@link CPerspective}s.
 * @author Benjamin Sigg
 */
public class CControlPerspective {
	private CControl control;
	
	/**
	 * Creates a new wrapper
	 * @param control the control whose perspectives are modified
	 */
	public CControlPerspective( CControl control ){
		if( control == null ){
			throw new IllegalArgumentException( "control must not be null" );
		}
		
		this.control = control;
	}
	
    
    /**
     * Creates a new {@link CPerspective} that is set up with the root-stations of the {@link CControl}. 
     * There are no {@link Dockable}s stored in the new perspective.
     * @return the new perspective
     */
    public CPerspective createEmptyPerspective(){
    	CPerspective perspective = new CPerspective();
    	for( CStation<?> station : control.getStations() ){
    		perspective.addRoot( station.createPerspective() );
    	}
    	return perspective;
    }
    
    /**
     * Gets a perspective that matches the current layout of the application.
     * @param includeWorkingAreas whether {@link Dockable}s that are managed by a working-area should be
     * included in the layout or not
     * @return the current perspective
     */
    public CPerspective getCurrentPerspective( boolean includeWorkingAreas ){
    	Setting setting = control.intern().getSetting( !includeWorkingAreas );
    	return convert( setting, includeWorkingAreas );
    }

    /**
     * Gets the perspective which represents a layout that was stored using {@link CControl#save(String)}.
     * @param name the name of the stored layout
     * @return the perspective or <code>null</code> if <code>name</code> was not found
     */
    public CPerspective getPerspective( String name ){
    	Setting setting = control.intern().getSetting( name );
    	if( setting == null ){
    		return null;
    	}
    	return convert( setting, false );
    }
    
    /**
     * Changes the layout of the associated {@link CControl} such that it matches <code>perspective</code>. 
     * @param perspective the perspective to apply, not <code>null</code>
     * @param includeWorkingAreas whether {@link Dockable}s that are managed by a working-area should be
     * included in the layout or not
     */
    @Todo( description="handle modes" )
    public void setPerspective( CPerspective perspective, boolean includeWorkingAreas ){
    	control.intern().setSetting( convert( perspective, includeWorkingAreas ), !includeWorkingAreas );
    }
    
    /**
     * Stores <code>perspective</code> as a layout that can be selected by the user by calling
     * {@link CControl#load(String)}.
     * @param name the name of the layout
     * @param perspective the new layout, not <code>null</code>
     */
    public void setPerspective( String name, CPerspective perspective ){
    	control.intern().setSetting( name, convert( perspective, false ) );
    }
    
    private Setting convert( CPerspective perspective, boolean includeWorkingAreas ){
    	CSetting setting = new CSetting();
    	ModeSettings<Location, ?> modes = new CLocationModeSettings<Location>( new LocationSettingConverter() );
    	setting.setModes( modes );
    	
    	Perspective conversion = control.intern().getPerspective( !includeWorkingAreas, new PerspectiveElementFactory( perspective ) );
    	
    	for( String key : perspective.getRootKeys() ){
    		CStationPerspective station = perspective.getRoot( key );
    		if( station.asDockable() == null || station.asDockable().getParent() == null ){
    			setting.putRoot( key, conversion.convert( station.intern() ) );
    		}
    	}
    	
    	return setting;
    }
    
    private CPerspective convert( Setting setting, boolean includeWorkingAreas ){
    	CPerspective cperspective = createEmptyPerspective();
    	Perspective perspective = control.intern().getPerspective( !includeWorkingAreas, new PerspectiveElementFactory( cperspective ) );
    	
    	for( String key : setting.getRootKeys() ){
    		perspective.convert( setting.getRoot( key ) );
    	}
    	
    	return cperspective;
    }
    
    /**
     * Helper class for converting {@link DockElement}s to {@link PerspectiveElement}s.
     * @author Benjamin Sigg
     */
    private class PerspectiveElementFactory implements FrontendPerspectiveFactory{
    	private CPerspective perspective;
    	private Map<String, SingleCDockablePerspective> dockables = new HashMap<String, SingleCDockablePerspective>();
    	
    	/**
    	 * Creates a new factory.
    	 * @param perspective the perspective for which items are required
    	 */
    	public PerspectiveElementFactory( CPerspective perspective ){
    		this.perspective = perspective;
    		Iterator<PerspectiveElement> elements = perspective.elements();
    		while( elements.hasNext() ){
    			PerspectiveElement element = elements.next();
    			if( element instanceof SingleCDockablePerspective ){
    				SingleCDockablePerspective dockable = (SingleCDockablePerspective) element;
    				dockables.put( dockable.getUniqueId(), dockable );
    			}
    		}
    	}
    	
		public PerspectiveElement get( String id, DockElement element, boolean isRootStation ){
			if( isRootStation ){
				return perspective.getRoot( id ).intern();
			}
			else if( element instanceof CommonDockable ){
				CDockable dockable = ((CommonDockable)element).getDockable();
				if( dockable instanceof SingleCDockable ){
					String key = ((SingleCDockable)dockable).getUniqueId();
					SingleCDockablePerspective result = dockables.get( key );
					if( result == null ){
						result = new SingleCDockablePerspective( key );
						dockables.put( key, result );
					}
					return result;
				}
			}
			
			throw new IllegalArgumentException( "The intern DockFrontend of the CControl has elements registered that are not SingleCDockables: " + id + "=" + element );
		}    	
    }
}