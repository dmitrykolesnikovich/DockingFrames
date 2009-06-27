/*
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2007 Benjamin Sigg
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
package bibliothek.extension.gui.dock.theme.eclipse.stack.tab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;

import bibliothek.extension.gui.dock.theme.eclipse.EclipseBorder;
import bibliothek.extension.gui.dock.theme.eclipse.stack.EclipseTabPane;
import bibliothek.gui.DockController;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.themes.basic.action.buttons.ButtonPanel;
import bibliothek.gui.dock.util.color.ColorCodes;
import bibliothek.util.Colors;


/**
 * @author Janni Kovacs
 */
@ColorCodes({"stack.tab.border", "stack.tab.border.selected", "stack.tab.border.selected.focused", "stack.tab.border.selected.focuslost",
	"stack.tab.top", "stack.tab.top.selected", "stack.tab.top.selected.focused","stack.tab.top.selected.focuslost",
	"stack.tab.bottom", "stack.tab.bottom.selected", "stack.tab.bottom.selected.focused", "stack.tab.bottom.selected.focuslost",
	"stack.tab.text", "stack.tab.text.selected", "stack.tab.text.selected.focused", "stack.tab.text.selected.focuslost",
"stack.border" })
public class ArchGradientPainter extends BaseTabComponent {
	private final int[] TOP_LEFT_CORNER_X = { 0, 1, 1, 2, 3, 4, 5, 6 };
	private final int[] TOP_LEFT_CORNER_Y = { 6, 5, 4, 3, 2, 1, 1, 0 };
	
	private Arch arch;
	
	public static final TabPainter FACTORY = new TabPainter(){
		public TabComponent createTabComponent( EclipseTabPane pane, Dockable dockable ) {
			return new ArchGradientPainter( pane, dockable );
		}
		
		public TabPanePainter createDecorationPainter( EclipseTabPane pane ){
			return new LinePainter( pane );
		}
		
		public InvisibleTab createInvisibleTab( InvisibleTabPane pane, Dockable dockable ){
			return new DefaultInvisibleTab( pane, dockable );
		}

		public Border getFullBorder( DockController controller, Dockable dockable ){
			return new EclipseBorder( controller, true );
		}
	};

	/** number of pixels at the left side that are empty and under the selected predecessor of this tab */
	private final int TAB_OVERLAP = 24;

	public ArchGradientPainter( EclipseTabPane pane, Dockable dockable ){
		super( pane, dockable );

		setLayout( null );
		setOpaque( false );

		add( getButtons() );
	}

	@Override
	protected void updateBorder(){
		EclipseTabPane pane = getPane();
		int index = getDockableIndex();
		
		if( isBound() && pane != null && index >= 0 ){
			Color color2;

			Window window = SwingUtilities.getWindowAncestor( getComponent() );
			boolean focusTemporarilyLost = false;

			if( window != null ){
				focusTemporarilyLost = !window.isActive();
			}

			if( isSelected() ){
				if( isFocused() ){
					if( focusTemporarilyLost )
						color2 = colorStackTabBorderSelectedFocusLost.value();
					else
						color2 = colorStackTabBorderSelectedFocused.value();
				}
				else
					color2 = colorStackTabBorderSelected.value();
			}
			else
				color2 = colorStackTabBorder.value();

			// set border around tab content
			pane.setContentBorderAt( index, new MatteBorder( 2, 2, 2, 2, color2 ) );
		}
	}

	public Insets getOverlap( TabComponent other ) {
		if( other instanceof ArchGradientPainter ){
			ArchGradientPainter painter = (ArchGradientPainter)other;
			if( painter.isSelected() ){
				return new Insets( 0, 10 + TAB_OVERLAP, 0, 0 );
			}
		}
		
		return new Insets( 0, 0, 0, 0 );
	}

	@Override
	public Dimension getMinimumSize(){
		Dimension preferred = getPreferredSize();
		return preferred;
	}
	
	@Override
	public Dimension getPreferredSize() {
		Font font = getFont();
		if( font == null )
			font = getPane().getComponent().getFont();
			
		if( font == null )
			return new Dimension( 0, 0 );
		
		Dockable dockable = getDockable();
		boolean isSelected = isSelected();
		ButtonPanel buttons = getButtons();

		FontRenderContext frc = new FontRenderContext(null, false, false);
		Rectangle2D bounds = font.getStringBounds(dockable.getTitleText(), frc);
		int width = 5 + (int) bounds.getWidth() + 5;
		int height = 6 + (int) bounds.getHeight();
		if ((doPaintIconWhenInactive() || isSelected) && dockable.getTitleIcon() != null)
			width += dockable.getTitleIcon().getIconWidth() + 5;
		if (isSelected)
			width += 35;
		if( isPreviousTabSelected() )
			width += TAB_OVERLAP;

		if( buttons != null ){
			Dimension tabPreferred = buttons.getPreferredSize();
			width += tabPreferred.width+1;
			height = Math.max( height, tabPreferred.height+1 );
		}

		return new Dimension(width, height);
	}
	
	@Override
	public void doLayout(){
		ButtonPanel buttons = getButtons();

		if( buttons != null ){
			Dockable dockable = getDockable();
			boolean isSelected = isSelected();

			FontRenderContext frc = new FontRenderContext(null, false, false);
			Rectangle2D bounds = getFont().getStringBounds(dockable.getTitleText(), frc);
			int x = 5 + (int) bounds.getWidth() + 5;
			if ((doPaintIconWhenInactive() || isSelected) && dockable.getTitleIcon() != null)
				x += dockable.getTitleIcon().getIconWidth() + 5;

			if( isSelected )
				x += 5;

			if( isPreviousTabSelected() )
				x += TAB_OVERLAP;

			Dimension preferred = buttons.getPreferredSize();
			int width = Math.min( preferred.width, getWidth()-x );
			int height = Math.min( getHeight()-1, preferred.height );

			buttons.setBounds( x, getHeight()-1-height, width-1, height );
		}
	}

	public void update(){
		revalidate();
		repaint();
	}
	
	private Arch arch( int width, int height ){
		if( arch == null || arch.getWidth() != width || arch.getHeight() != height )
			arch = new Arch( width, height );
		return arch;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int x = 0;
		int y = 0;
		int w = getWidth();
		int h = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		Color lineColor = colorStackBorder.value();

		Color color1;
		Color color2;
		Color colorText;

		Window window = SwingUtilities.getWindowAncestor( getComponent() );
		boolean focusTemporarilyLost = false;

		if( window != null ){
			focusTemporarilyLost = !window.isActive();
		}


		if( isFocused() && !focusTemporarilyLost ){
			color1 = colorStackTabTopSelectedFocused.value();
			color2 = colorStackTabBottomSelectedFocused.value();
			colorText = colorStackTabTextSelectedFocused.value();
		}
		else if (isFocused() && focusTemporarilyLost) {
			color1 = colorStackTabTopSelectedFocusLost.value();
			color2 = colorStackTabBottomSelectedFocusLost.value();
			colorText = colorStackTabTextSelectedFocusLost.value();
		}
		else if( isSelected() ){
			color1 = colorStackTabTopSelected.value();
			color2 = colorStackTabBottomSelected.value();
			colorText = colorStackTabTextSelected.value();
		}
		else{
			color1 = colorStackTabTop.value();
			color2 = colorStackTabBottom.value();
			colorText = colorStackTabText.value();
		}

		// draw tab if selected
		if (isSelected()) {
			paintSelected( g2d, color1, color2 );
		}
		else{
			GradientPaint gradient = color1.equals( color2 ) ? null : new GradientPaint( x, y, color1, x, y + h, color2 );
			Paint old = g2d.getPaint();
			if( gradient != null )
				g2d.setPaint(gradient);
			else
				g2d.setPaint( color1 );

			g2d.fillRect( x, y, w, h-1 );
			g2d.setPaint(old);
		}

		// draw icon
		int iconOffset = 0;

		if( isPreviousTabSelected() )
			iconOffset += TAB_OVERLAP;

		if (isSelected() || doPaintIconWhenInactive()) {
			Icon i = getDockable().getTitleIcon();
			if (i != null) {
				iconOffset += 5;

				int iconY = (h - i.getIconHeight())/2;

				i.paintIcon( getComponent(), g, iconOffset, iconY);
				iconOffset += i.getIconWidth();
			}
		}

		// draw separator lines
		if (!isSelected() && !isNextTabSelected() ){
			g.setColor(lineColor);
			g.drawLine(w - 1, 0, w - 1, h);
		}

		// draw text
		g.setColor( colorText );
		g.drawString( getDockable().getTitleText(), x + 5 + iconOffset, h / 2 + g.getFontMetrics().getHeight() / 2 - 2);
	}

	@Override
	public boolean contains( int x, int y ) {
		if( !super.contains( x, y ) )
			return false;

		if( isSelected() ){
			int w = getWidth();
			int h = getHeight();
			
			Polygon left = leftSide( 0, 0, w, h );
			if( left.contains( x, y ))
				return true;
			
			Polygon right = rightSide( 0, 0, w, h );
			if( right.contains( x, y ))
				return true;
			
			Rectangle leftBox = left.getBounds();
			Rectangle rightBox = right.getBounds();
			
			if( leftBox.x + leftBox.width > x )
				return false;
			
			if( rightBox.x < x )
				return false;
			
			return true;
		}
		else
			return true;
	}
	
	/**
	 * Paints the background of a selected tab.
	 * @param g the graphics context to use
	 * @param top the color at the top
	 * @param bottom the color at the bottom
	 */
	private void paintSelected( Graphics g, Color top, Color bottom ){
		int x = 0;
		int y = 0;
		int w = getWidth();
		int h = getHeight();
		Graphics2D g2d = (Graphics2D) g;
		Color lineColor = colorStackBorder.value();
		
		boolean firstTab = getTabIndex() == 0;
		
		Polygon left = leftSide( x-1, y-1, w, h+1 );
		Polygon right = rightSide( x, y, w, h-1 );
		
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		
		// draw shadow
		if ( firstTab )
			left.translate( -1, 0 );
		
		g.setColor( Colors.between( lineColor, getBackground(), 0.75 ) );
		
		g.drawPolyline( left.xpoints, left.ypoints, left.npoints-1 );
		g.drawPolyline( right.xpoints, right.ypoints, right.npoints-1 );
		
		// fill inner areas
		GradientPaint gradient = top.equals( bottom ) ? null : new GradientPaint( x, y, top, x, y + h, bottom );
		
		Paint old = g2d.getPaint();
		if( gradient != null )
			g2d.setPaint(gradient);
		else
			g2d.setPaint( top );
		
		left.translate( 1, 0 );
		right.translate( -1, 0 );
		
		g.fillPolygon( left );
	// g.fillPolygon( right );
		right.translate( 1, 1 );
		g.fillPolygon( right );
		right.translate( -1, -1 );
		
		g.drawLine( 0, h-1, w-1, h-1 );
		
		Rectangle leftBox = left.getBounds();
		Rectangle rightBox = right.getBounds();
		if( leftBox.x+leftBox.width < rightBox.x ){
			g.fillRect(
					leftBox.x+leftBox.width, 0,
					rightBox.x-leftBox.x-leftBox.width+1, h );
		}
		
		g2d.setPaint( old );
		
		// draw border
		g.setColor( lineColor );
		//left.translate( -1, 0 );
		// right.translate( 1, 0 );
		g.drawPolyline( left.xpoints, left.ypoints, left.npoints-1 );
		g.drawPolyline( right.xpoints, right.ypoints, right.npoints-1 );
		
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
	}
	
	private Polygon leftSide( int x, int y, int w, int h ){
		int[] xPoints = new int[ TOP_LEFT_CORNER_X.length+2 ];
		int[] yPoints = new int[ TOP_LEFT_CORNER_Y.length+2 ];
		
		System.arraycopy( TOP_LEFT_CORNER_X, 0, xPoints, 1, TOP_LEFT_CORNER_X.length );
		System.arraycopy( TOP_LEFT_CORNER_Y, 0, yPoints, 1, TOP_LEFT_CORNER_Y.length );
		
		int max = 0;
		
		for( int i = 1, n = xPoints.length-1; i<n; i++ ){
			max = Math.max( max, xPoints[i] );
			xPoints[i] += x;
			yPoints[i] += y;
		}
		
		xPoints[0] = x;
		yPoints[0] = y+h-1;
		
		int index = xPoints.length-1;
		
		xPoints[index] = x+max;
		yPoints[index] = y+h-1;
		
		return new Polygon( xPoints, yPoints, xPoints.length );
	}
	
	private Polygon rightSide( int x, int y, int w, int h ){
		Arch arch = arch( h*34/22, h );
		
		int[] xPoints = new int[ arch.getWidth()+1 ];
		int[] yPoints = new int[ arch.getWidth()+1 ];
		
		for( int i = 0, n = arch.getWidth(); i<n; i++ ){
			xPoints[i] = x+w-n+i;
			yPoints[i] = arch.getValue( i ) + y;
		}
		
		int index = xPoints.length-1;
		
		xPoints[ index ] = xPoints[0];
		yPoints[ index ] = yPoints[ index-1 ];
		
		return new Polygon( xPoints, yPoints, xPoints.length );
	}
}
