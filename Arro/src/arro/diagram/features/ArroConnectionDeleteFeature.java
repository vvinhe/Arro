package arro.diagram.features;

import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.platform.IDiagramContainer;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;

import util.Logger;
import arro.Constants;
import arro.domain.ArroConnection;
import arro.domain.ArroModule;
import arro.editors.FunctionDiagramEditor;

public class ArroConnectionDeleteFeature extends DefaultDeleteFeature implements ICustomUndoRedoFeature {

	public ArroConnectionDeleteFeature(IFeatureProvider fp) {
		super(fp);
	}
	
	public void delete(IDeleteContext context) {
        IDiagramContainer dc = getDiagramBehavior().getDiagramContainer();
        if(!(dc instanceof FunctionDiagramEditor)) {
        	Logger.out.trace(Logger.EDITOR, "not an editor");
        } else {
        	ArroModule domainModule =  ((FunctionDiagramEditor)dc).getDomainModule();
	        context.putProperty(Constants.PROP_UNDO_CONNECTION_KEY, domainModule.cloneConnectionList());
	        context.putProperty(Constants.PROP_DOMAIN_MODULE_KEY, domainModule);
        }
		super.delete(context);
	}


	@Override
	protected void deleteBusinessObjects(Object[] businessObjects) {
		super.deleteBusinessObjects(businessObjects);
	}

	@Override
	protected void deleteBusinessObject(Object bo) {
		if(bo instanceof ArroConnection) {
			ArroConnection obj = (ArroConnection)bo;
			
			obj.getParent().removeConnection(obj);
			
			//POJOIndependenceSolver.getInstance().removeBusinessObject(bo);
		}
	}

	@Override
	public boolean canRedo(IContext context) {
		return true;
	}

	@Override
	public void preUndo(IContext context) {
		ArroModule domainModule = (ArroModule) context.getProperty(Constants.PROP_DOMAIN_MODULE_KEY);
		
		Logger.out.trace(Logger.EDITOR, "undo " + context.getProperty(Constants.PROP_UNDO_CONNECTION_KEY));
        context.putProperty(Constants.PROP_REDO_CONNECTION_KEY, domainModule.cloneConnectionList());
		Object undoList = context.getProperty(Constants.PROP_UNDO_CONNECTION_KEY);
		domainModule.setConnectionList(undoList);
	}

	@Override
	public void postUndo(IContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preRedo(IContext context) {
		ArroModule domainModule = (ArroModule) context.getProperty(Constants.PROP_DOMAIN_MODULE_KEY);
		
		Logger.out.trace(Logger.EDITOR, "redo " + context.getProperty(Constants.PROP_UNDO_CONNECTION_KEY));
        context.putProperty(Constants.PROP_UNDO_CONNECTION_KEY, domainModule.cloneConnectionList());
		Object redoList = context.getProperty(Constants.PROP_REDO_CONNECTION_KEY);
		domainModule.setConnectionList(redoList);
	}

	@Override
	public void postRedo(IContext context) {
		// TODO Auto-generated method stub
		
	}
}
