/*
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2008 Benjamin Sigg
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
package bibliothek.extension.gui.dock.preference;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import bibliothek.extension.gui.dock.util.Path;
import bibliothek.extension.gui.dock.util.PathCombiner;

/**
 * A {@link PreferenceModel} that is also a {@link TreeModel}. It contains
 * other {@link PreferenceModel}s and organizes them in a tree. The nodes
 * of this {@link TreeModel} are of the type {@link PreferenceTreeModel.Node}.
 * The root of this model never has a name nor a model.
 * @author Benjamin Sigg
 */
public class PreferenceTreeModel extends AbstractPreferenceModel implements TreeModel{
    /** internal representation of all models */
    private MergedPreferenceModel delegate;
    
    /** a listener to <code>delegate</code> */
    private PreferenceModelListener delegateListener = new PreferenceModelListener(){
        public void preferenceAdded( PreferenceModel model, int beginIndex, int endIndex ) {
            firePreferenceAdded( beginIndex, endIndex );
        }
        public void preferenceChanged( PreferenceModel model, int beginIndex, int endIndex ) {
            firePreferenceChanged( beginIndex, endIndex );
        }
        public void preferenceRemoved( PreferenceModel model, int beginIndex, int endIndex ) {
            firePreferenceRemoved( beginIndex, endIndex );
        }
    };
    
    private List<TreeModelListener> treeListeners = new ArrayList<TreeModelListener>();
    
    private TreeNode root = new TreeNode( null, new Path() );
    
    /**
     * Creates a new empty model.
     */
    public PreferenceTreeModel(){
        delegate = new MergedPreferenceModel();
    }
    
    /**
     * Creates a new empty model.
     * @param combiner tells how to combine the {@link Path} of a model with
     * the preferences of a model. Used in {@link #getPath(int)}. Not <code>null</code>.
     * @see MergedPreferenceModel#MergedPreferenceModel(PathCombiner)
     */
    public PreferenceTreeModel( PathCombiner combiner ){
        delegate = new MergedPreferenceModel( combiner );
    }
    
    public void addTreeModelListener( TreeModelListener l ) {
        treeListeners.add( l );
    }
    
    @Override
    public void addPreferenceModelListener( PreferenceModelListener listener ) {
        boolean listeners = hasListeners();
        super.addPreferenceModelListener( listener );
        if( !listeners && hasListeners() ){
            delegate.addPreferenceModelListener( delegateListener );
        }
    }
    

    public void removeTreeModelListener( TreeModelListener l ) {
        treeListeners.remove( l );
    }

    @Override
    public void removePreferenceModelListener( PreferenceModelListener listener ) {
        boolean listeners = hasListeners();
        super.removePreferenceModelListener( listener );
        if( listeners && !hasListeners() ){
            delegate.removePreferenceModelListener( delegateListener );
        }
    }
    
    /**
     * Gets all the {@link TreeModel} currently known to this model.
     * @return the list of listeners
     */
    protected TreeModelListener[] getTreeModelListeners(){
        return treeListeners.toArray( new TreeModelListener[ treeListeners.size() ] );
    }
    
    private void fireNodeAdded( TreeNode parent, TreeNode child ){
        TreeModelListener[] listeners = getTreeModelListeners();
        if( listeners.length > 0 ){
            TreeModelEvent event = new TreeModelEvent(
                    this,
                    parent.getTreePath(),
                    new int[]{ parent.indexOf( child ) },
                    new Object[]{ child });
            
            for( TreeModelListener listener : listeners ){
                listener.treeNodesInserted( event );
            }
        }
    }
    
    private void fireNodeChanged( TreeNode node ){
        TreeModelListener[] listeners = getTreeModelListeners();
        if( listeners.length > 0 ){
            TreeNode parent = node.getParent();
            
            TreeModelEvent event;
            if( parent == null ){
                event = new TreeModelEvent(
                    this,
                    node.getTreePath() );
            }
            else{
                event = new TreeModelEvent(
                        this,
                        node.getTreePath(),
                        new int[]{ parent.indexOf( node ) },
                        new Object[]{ node });
            }
            
            for( TreeModelListener listener : listeners ){
                listener.treeNodesChanged( event );
            }
        }
    }
    
    private void fireNodeRemoved( TreeNode parent, int[] indices, Object[] children ){
        TreeModelListener[] listeners = getTreeModelListeners();
        if( listeners.length > 0 ){
            
            TreeModelEvent event = new TreeModelEvent(
                        this,
                        parent.getTreePath(),
                        indices,
                        children );
            
            for( TreeModelListener listener : listeners ){
                listener.treeNodesRemoved( event );
            }
        }
    }
    
    public Node getChild( Object parent, int index ) {
        return ((TreeNode)parent).getChild( index );
    }

    public int getChildCount( Object parent ) {
        return ((TreeNode)parent).getChildrenCount();
    }

    public int getIndexOfChild( Object parent, Object child ) {
        return ((TreeNode)parent).indexOf( (TreeNode)child );
    }

    public Node getRoot() {
        return root;
    }

    public boolean isLeaf( Object node ) {
        return ((TreeNode)node).getChildrenCount() == 0;
    }

    public void valueForPathChanged( TreePath path, Object newValue ) {
        // ignore
    }
    
    @Override
    public String getDescription( int index ) {
        return delegate.getDescription( index );
    }
    
    @Override
    public boolean isNatural( int index ) {
        return delegate.isNatural( index );
    }
    
    @Override
    public void setValueNatural( int index ) {
         delegate.setValueNatural( index );   
    }
    
    @Override
    public PreferenceOperation[] getOperations( int index ) {
        return delegate.getOperations( index );
    }
    
    @Override
    public boolean isEnabled( int index, PreferenceOperation operation ) {
        return delegate.isEnabled( index, operation );
    }
    
    @Override
    public void doOperation( int index, PreferenceOperation operation ) {
        delegate.doOperation( index, operation );
    }
    
    @Override
    public void read() {
        delegate.read();
    }
    
    @Override
    public void write() {
        delegate.write();
    }
    
    public String getLabel( int index ) {
        return delegate.getLabel( index );
    }
    
    public Path getPath( int index ) {
        return delegate.getPath( index );
    }
    
    public int getSize() {
        return delegate.getSize();
    }
    
    public Path getTypePath( int index ) {
        return delegate.getTypePath( index );
    }
    
    public Object getValueInfo(int index) {
    	return delegate.getValueInfo( index );
    }
    
    public Object getValue( int index ) {
        return delegate.getValue( index );
    }
    
    public void setValue( int index, Object value ) {
        delegate.setValue( index, value );
    }
    
    /**
     * Sets the name of the node at <code>path</code>. If there is no
     * such node, then the node and all its parents are created. Otherwise
     * just the name gets exchanged.
     * @param path the path to the node
     * @param name the new name of the node
     */
    public void putNode( Path path, String name ){
        root.getNode( path, 0 ).setName( name );
    }
    
    /**
     * Sets the model of the node at <code>path</code>. If there is
     * no such node, then the node and all its parents are created. Otherwise
     * just the model gets exchanged.
     * @param path the path to change
     * @param model the new model
     */
    public void putModel( Path path, PreferenceModel model ){
        root.getNode( path, 0 ).setModel( model );
        delegate.remove( path );
        delegate.add( model, path );
    }
    
    /**
     * Sets name and model of a given node.
     * @param path the path to the node
     * @param name the new name
     * @param model the new model, can be <code>null</code>
     * @see #putNode(Path, String)
     * @see #putModel(Path, PreferenceModel)
     */
    public void put( Path path, String name, PreferenceModel model ){
        delegate.remove( path );
        if( model != null ){
        	delegate.add( model, path );
        }
        root.getNode( path, 0 ).set( name, model );
    }
    
    /**
     * Deletes the node at <code>path</code> and all its children from the
     * tree. This also removes any {@link PreferenceModel} of the subtree. If
     * there is no node at <code>path</code>, then nothing happens
     * @param path the path to remove
     */
    public void delete( Path path ){
        root.delete( path, 0 );
    }
    
    /**
     * A single node of a {@link PreferenceTreeModel}.
     * @author Benjamin Sigg
     */
    public static interface Node{
        /**
         * Gets the path of this node.
         * @return the path
         */
        public Path getPath();
        
        /**
         * Gets the name of this node.
         * @return the user readable name, might be <code>null</code>
         */
        public String getName();
        
        /**
         * Gets the model of this node.
         * @return the model, might be <code>null</code>
         */
        public PreferenceModel getModel();
    }
    
    private class TreeNode implements Node{
        private TreeNode parent;
        private List<TreeNode> children;
        
        private Path path;
        private TreePath treePath;
        
        private String name;
        private PreferenceModel model;
        
        public TreeNode( TreeNode parent, Path path ){
            this.parent = parent;
            this.path = path;
        }
        
        public int getChildrenCount(){
            return children == null ? 0 : children.size();
        }
        
        public TreeNode getChild( int index ){
            return children.get( index );
        }
        
        public int indexOf( TreeNode child ){
            return children.indexOf( child );
        }
        
        /**
         * Gets the path to this node.
         * @return the path
         */
        public TreePath getTreePath(){
            if( treePath != null )
                return treePath;
            
            if( parent == null ){
                treePath = new TreePath( this );
            }
            else{
                treePath = parent.getTreePath().pathByAddingChild( this );
            }
            
            return treePath;
        }
        
        public TreeNode getNode( Path path, int segment ){
            if( segment == path.getSegmentCount() )
                return this;
            
            if( children == null ){
                children = new ArrayList<TreeNode>();
            }
            
            String check = path.getSegment( segment );
            for( TreeNode child : children ){
                if( check.equals( child.getPath().getLastSegment() )){
                    return child.getNode( path, segment+1 );
                }
            }
            
            TreeNode child = new TreeNode( this, path.subPath( 0, segment+1 ) );
            children.add( child );
            fireNodeAdded( this, child );
            return child.getNode( path, segment+1 );
        }
        
        public void delete( Path path, int segment ){
            if( segment == path.getSegmentCount() ){
                delete( false );
            }
            else if( children != null ){
                String check = path.getSegment( segment );
                for( TreeNode child : children ){
                    if( check.equals( child.getPath().getLastSegment() )){
                        child.delete( path, segment+1 );
                        break;
                    }
                }
            }
        }
        
        public void delete( boolean silent ){
            if( children != null ){
                for( TreeNode child : children ){
                    child.delete( true );
                }
            }
            
            if( !silent ){
                if( parent == null ){
                    if( children != null ){
                        int[] indices = new int[ children.size() ];
                        for( int i = 0; i < indices.length; i++ )
                            indices[i] = i;
                        
                        Object[] removed = children.toArray();
                        children.clear();
                        
                        fireNodeRemoved( this, indices, removed );
                    }
                }
                else{
                    parent.delete( this );
                }
            }
        }
        
        public void delete( TreeNode child ){
            int index = indexOf( child );
            children.remove( index );
            
            fireNodeRemoved( this, new int[]{ index }, new Object[]{ child });
        }
        
        public TreeNode getParent() {
            return parent;
        }
        
        @Override
        public String toString() {
            return name == null ? "" : name;
        }
        
        public Path getPath() {
            return path;
        }
        
        public void set( String name, PreferenceModel model ){
            this.name = name;
            this.model = model;
            fireNodeChanged( this );
        }
        
        public void setName( String name ) {
            this.name = name;
            fireNodeChanged( this );
        }
        
        public String getName() {
            return name;
        }
        
        public void setModel( PreferenceModel model ) {
            this.model = model;
            fireNodeChanged( this );
        }
        
        public PreferenceModel getModel() {
            return model;
        }
    }
}