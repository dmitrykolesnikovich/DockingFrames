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
package bibliothek.gui.dock.facile.menu;

import bibliothek.gui.dock.support.menu.MenuPiece;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * A {@link MenuPiece} which consists only of one {@link JMenu}. There is a
 * new subtree attached to this {@link SubmenuPiece}.
 *
 * @author Benjamin Sigg
 */
public class SubmenuPiece extends MenuPiece {
  /**
   * the root of this submenu
   */
  private RootMenuPiece root;

  /**
   * Creates a new submenu-piece, using a normal {@link JMenu} to inserts
   * its content.
   */
  public SubmenuPiece() {
    root = new RootMenuPiece();
    root.setParent(this);
  }

  /**
   * Creates a new submenu-piece, using a normal {@link JMenu}.
   *
   * @param text             the text of the menu
   * @param disableWhenEmpty whether to disable the menu when it is empty
   * @param pieces           the elements of this piece
   */
  public SubmenuPiece(String text, boolean disableWhenEmpty, MenuPiece... pieces) {
    root = new RootMenuPiece(text, disableWhenEmpty, pieces);
    root.setParent(this);
  }

  /**
   * Creates a new submenu-piece.
   *
   * @param menu the menu into which this piece will insert its content
   */
  public SubmenuPiece(JMenu menu) {
    root = new RootMenuPiece(menu);
    root.setParent(this);
  }

  @Override
  public void bind() {
    super.bind();
    root.bind();
  }

  @Override
  public void unbind() {
    super.unbind();
    root.unbind();
  }

  @Override
  public void fill(List<Component> items) {
    items.add(getMenu());
  }

  @Override
  public int getItemCount() {
    return 1;
  }

  /**
   * Whether to disable the menu when it is empty or not.
   *
   * @return <code>true</code> if the menu gets disabled
   */
  public boolean isDisableWhenEmpty() {
    return root.isDisableWhenEmpty();
  }

  /**
   * Disables the menu if there are no items in the menu.
   *
   * @param disableWhenEmpty <code>true</code> if the menu should be
   *                         disabled when empty
   */
  public void setDisableWhenEmpty(boolean disableWhenEmpty) {
    root.setDisableWhenEmpty(disableWhenEmpty);
  }

  /**
   * Tells whether the submenu could be enabled. The actual state of the
   * menu also depends on {@link #isDisableWhenEmpty()}
   *
   * @return whether the menu could be enabled
   */
  public boolean isEnabled() {
    return root.isEnabled();
  }

  /**
   * Sets whether the submenu can be enabled. The actual state of the
   * menu also depends on {@link #isDisableWhenEmpty()}.
   *
   * @param enabled whether the submenu can be enabled
   */
  public void setEnabled(boolean enabled) {
    root.setEnabled(enabled);
  }

  @Override
  public JMenu getMenu() {
    return root.getMenu();
  }

  /**
   * Gets the root of this subtree.
   *
   * @return the root
   */
  public RootMenuPiece getRoot() {
    return root;
  }
}
