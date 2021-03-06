package arro.diagram.features;

import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.impl.LayoutContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.platform.IDiagramContainer;

import arro.Constants;
import arro.domain.ArroModule;
import arro.domain.ArroSynchronization;
import arro.editors.StateDiagramEditor;
import util.Logger;


public class SynchronizationAddFeature extends AbstractAddFeature implements IAddFeature, ICustomUndoRedoFeature {
	
	public SynchronizationAddFeature(IFeatureProvider fp) {
		super(fp);

	}

	public boolean canAdd(IAddContext context) {
		// TODO: check for right domain object instance below
		return (context.getNewObject() instanceof ArroSynchronization && context.getTargetContainer() instanceof Diagram);
	}

	/**
	 * Called when a State is added to the diagram.
	 */
	public PictogramElement add(IAddContext context) {
        
        IDiagramContainer dc = getDiagramBehavior().getDiagramContainer();
        if(!(dc instanceof StateDiagramEditor)) {
        	Logger.out.trace(Logger.EDITOR, "not an editor");
        	return null;
        }
        ArroModule domainModule =  ((StateDiagramEditor)dc).getDomainModule();
        
  		Object obj = context.getNewObject();
		
        if(!(obj instanceof ArroSynchronization)) {
        	return null;
        }
        
        ArroSynchronization addedDomainObject = (ArroSynchronization)obj;
        
        String instanceName = "a" + "Sync";
        while(domainModule.getStateDiagram().getStepByName(instanceName) != null) {
        	instanceName += "1";
        }
        addedDomainObject.setName(instanceName);

		ContainerShape containerShape = new SynchronizationHelper().create(context, addedDomainObject, manageColor(Constants.CLASS_FOREGROUND), manageColor(Constants.CLASS_BACKGROUND));

		context.putProperty(Constants.PROP_UNDO_NODE_KEY, domainModule.cloneNodeList());
        context.putProperty(Constants.PROP_DOMAIN_MODULE_KEY, domainModule);

        domainModule.getStateDiagram().addSynchronization(addedDomainObject);
        
	    // Now link PE (containerShape) to domain object and register diagram in POJOIndependencySolver
		link(containerShape, addedDomainObject);
		
		// After PE was linked to domain object..

        // To set location and size.
	    LayoutContext layoutContext = new LayoutContext(containerShape);
	    ILayoutFeature layoutFeature = getFeatureProvider().getLayoutFeature(layoutContext);
	    layoutFeature.layout(layoutContext);


		
		return containerShape;
	}

	@Override
	public void preUndo(IContext context) {
		ArroModule domainModule = (ArroModule) context.getProperty(Constants.PROP_DOMAIN_MODULE_KEY);
		
		Logger.out.trace(Logger.EDITOR, "undo " + context.getProperty(Constants.PROP_UNDO_NODE_KEY));
        context.putProperty(Constants.PROP_REDO_NODE_KEY, domainModule.cloneNodeList());
		Object undoList = context.getProperty(Constants.PROP_UNDO_NODE_KEY);
		domainModule.setNodeList(undoList);
	}

	@Override
	public boolean canRedo(IContext context) {
		return true;
	}

	@Override
	public void preRedo(IContext context) {
		ArroModule domainModule = (ArroModule) context.getProperty(Constants.PROP_DOMAIN_MODULE_KEY);
		
		Logger.out.trace(Logger.EDITOR, "redo " + context.getProperty(Constants.PROP_UNDO_NODE_KEY));
        context.putProperty(Constants.PROP_UNDO_NODE_KEY, domainModule.cloneNodeList());
		Object redoList = context.getProperty(Constants.PROP_REDO_NODE_KEY);
		domainModule.setNodeList(redoList);
	}

	@Override
	public void postUndo(IContext context) {
		
	}

	@Override
	public void postRedo(IContext context) {
		
	}
}

