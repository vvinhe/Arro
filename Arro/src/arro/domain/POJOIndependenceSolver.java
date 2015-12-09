package arro.domain;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.graphiti.features.impl.AbstractFeatureProvider;
import org.eclipse.graphiti.features.impl.IIndependenceSolver;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.ArroZipFile;
import util.Logger;
import arro.Constants;
import arro.node.diagram.ArroNodeFeatureProvider;


/**
 * This class is there to allow Graphiti to look up domain objects (NonEmfDomainObject)
 * using getBusinessObjectForKey:
 * - Node objects
 * - Pad objects
 * In order to allow cleanup up when a diagram is closed, we also store the diagram ID
 * with the domain object.
 * 
 */
public class POJOIndependenceSolver implements IIndependenceSolver {
		
	/* Make it a singleton, used for all PictogramElements in all diagrams. */
	private static POJOIndependenceSolver pojoIndependenceSolver = null;
	
	private AbstractFeatureProvider fp = null;

	/* for XML load / store */
	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	
	//private ArroNodeInternal intern;
	private HashMap<String, NonEmfDomainObject> node;
	
	// Build a very simple cache. Increase usage count on every access.
	// A garbage collector cleans entries with count != 0 and resets all counts to 0.

	
	
	public POJOIndependenceSolver() {
		node = new HashMap<String, NonEmfDomainObject>();
	}
	
	public static POJOIndependenceSolver getInstance() {
		if(pojoIndependenceSolver == null)
			pojoIndependenceSolver = new POJOIndependenceSolver();

		return pojoIndependenceSolver;
	}

	public void setFeatureProvider(ArroNodeFeatureProvider fp) {
		this.fp = fp;
	}


	/**
	 * This function provides the key for the given business object.
	 * Key being the UUID string used in Graphiti diagrams.
	 * 
	 * @see getBusinessObjectForKey
	 * @see org.eclipse.graphiti.features.impl.IIndependenceSolver#getKeyForBusinessObject(java.lang.Object)
	 */
	public String getKeyForBusinessObject(Object bo) {
		String result = null;
		if(bo != null && bo instanceof NonEmfDomainObject ) {
			result = ((NonEmfDomainObject) bo).getId();
		}
		return result;
	}

	/**
	 * This function provides the business object for the given key.
	 * Key being the UUID string used in Graphiti diagrams.
	 * 
	 * @see getKeyForBusinessObject
	 * @see org.eclipse.graphiti.features.impl.IIndependenceSolver#getBusinessObjectForKey(java.lang.String)
	 */
	public Object getBusinessObjectForKey(String key) {
		return node.get(key);
	}
	
	/**
	 * Do not remove BOs from the list. When an undo is done on a Graphiti
	 * element referring such object, the link would be broken.
	 * 
	 * @param bo
	 */
	public void removeBusinessObject(Object bo) {
	}

	public void RemovePOJOObjects(String name) {
		// TODO Auto-generated method stub
		Logger.out.trace(Logger.STD, "Remove POJO file " + name);
		
	}

	public void RegisterPOJOObject(NonEmfDomainObject nonEmfDomainObject) {
		Collection<NonEmfDomainObject> list = node.values();
		for(NonEmfDomainObject obj: list) {
			if(obj == nonEmfDomainObject) {
				// remove it
				node.remove(obj.getId());
				break;
			}
		}
		node.put(nonEmfDomainObject.getId(), nonEmfDomainObject);
	}
	
    public NonEmfDomainObject findPOJOObjectByName(String name, Class<?> class1) {
	    Iterator<Entry<String, NonEmfDomainObject>> it = node.entrySet().iterator();
	    
	    NonEmfDomainObject found = null;
	    
	    // search for business object that is a ArroNode with name nodeName
	    Logger.out.trace(Logger.STD, "printing POJOIndependenceSolver list ");
	    
	    while (it.hasNext()) {
	    	Map.Entry<String, NonEmfDomainObject> pair = it.next();
	    	Logger.out.trace(Logger.STD, "id: " + pair.getKey() + " name: " + pair.getValue().getName());
	    	if(pair.getValue().getName().equals(name) /*&& pair.getValue().getClass().equals(class1)*/) {
	    		found = pair.getValue();
	    	}
	    }
	    return found;
    
    }
    
    // Helper method to get POJO from PE
    public NonEmfDomainObject findPOJOObjectByPictureElement(PictogramElement pe) {
		Object[] obj = fp.getAllBusinessObjectsForPictogramElement(pe);
		if(obj == null || obj.length == 0) {
			return null;
		} else {
			return ((NonEmfDomainObject) (obj[0]));
		}
    }
    
	    

}

