package arro.node.features;

import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;
import org.eclipse.graphiti.mm.pictograms.Diagram;

import arro.domain.ArroNode;

public class ArroNodeCreateFeature extends AbstractCreateFeature implements
		ICreateFeature {

	public ArroNodeCreateFeature(IFeatureProvider fp) {
		super(fp, "Node", "Creates a new Node");
	}

	public boolean canCreate(ICreateContext context) {
		return context.getTargetContainer() instanceof Diagram;
	}

	public Object[] create(ICreateContext context) {
		
		
		// TODO: in case of an EMF object add the new object to a suitable resource
		// getDiagram().eResource().getContents().add(newDomainObject);
		
	    // create EClass
        ArroNode newClass = new ArroNode();
        // Add model element to resource.
        // We add the model element to the resource of the diagram for
        // simplicity's sake. Normally, a customer would use its own
        // model persistence layer for storing the business model separately.
        //getDiagram().eResource().getContents().add(newClass);
        //newClass.setName(newClassName);
 
        // do the add
		addGraphicalRepresentation(context, newClass);
		return new Object[] { newClass };
	}
}

