package arro.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.part.FileEditorInput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import util.Logger;
import util.PathUtil;
import util.ArroZipFile;
import arro.Constants;

/**
 * Simple cache for ArroZipFile instances. Such instances will keep
 * their zipped files unzipped and editable as long as the instance
 * exists. Cleanup is done when??
 * 
 */
public class ResourceCache {
	private static ResourceCache myCache = null;
	private HashMap<String, ArroZipFile> cache;
	private IFolder save = null;

	
	/* for XML load / store */
	DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

	/**
	 * Constructor, init the cache.
	 */
	public ResourceCache() {
		cache = new HashMap<String, ArroZipFile>();
	}


	/**
	 * Open domain file <typeName> by unzipping it:
	 * - <typeName>.anod into .<typeName>.anod and .<typeName>.anod.xml
	 * Zip file also contains other stuff, but we only need module here.
	 * 
	 * Then read the domain file (.<typeName>.anod) into a DomainModule
	 * instance and register this instance in the cache.
	 * 
	 * FIXME: must search all resources in the open project.
	 */
	public ArroZipFile getZip(String typeName) throws RuntimeException {
		ArroZipFile zip = null;
        IFolder folder = getDiagramFolder(null);

		if(cache.containsKey(typeName)) {
			// Already in cache, double check that file exists..
	          if(folder.getFile(typeName + "." + Constants.NODE_EXT).exists()) {
	              return cache.get(typeName);
	          } else {
	              cache.remove(typeName);
	              return null;
	          }

		} else {
	        if(folder.getFile(typeName + "." + Constants.NODE_EXT).exists()) {
		        zip = new ArroZipFile(folder.getFile(typeName + "." + Constants.NODE_EXT));
	        } else {
	    		throw new RuntimeException("No zip file with this name");
	        }

			ArroModule module = loadModule(zip, typeName);
			zip.setDomainDiagram(module);
			cache.put(PathUtil.truncExtension(typeName), zip);
			
			return zip;
		}
	}
	
	private ArroModule loadModule(ArroZipFile zip, String typeName) {
		// TODO: handle error if zip file removed.
		String fileName = zip.getName();

		
		ArroModule n = null;
		
	    try {
	    	
	    	Logger.out.trace(Logger.STD, "Loading for " + zip.getName());
		    
    		n = new ArroModule();
    		// FIXME compare name in file with name passed as parameter.
    		n.setType(typeName);


	    	InputStream fXmlFile = zip.getFile(Constants.HIDDEN_RESOURCE + fileName + ".xml").getContents();
	    	if(fXmlFile == null) {
	    		// file doesn't exist
	    		return null;
	    	}
	    	DocumentBuilder dBuilder = builderFactory.newDocumentBuilder();
	    	Document doc = dBuilder.parse(fXmlFile);
	     
	    	//optional, but recommended
	    	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	    	doc.getDocumentElement().normalize();
	     
	    	Logger.out.trace(Logger.STD, "Root element :" + doc.getDocumentElement().getNodeName());
	    	
	    	NodeList nList = doc.getElementsByTagName("module");
	    	for (int temp = 0; temp < nList.getLength(); temp++) {
	     
	    		Node nNode = nList.item(temp);
	    		
	    		// FIXME we expect only one node!
	    		n.xmlRead(nNode);
	    	}
	    } catch (Exception e) {
	        /* no file */;
            throw new RuntimeException("Module not found "  + typeName);
        }
	    return n;
	}

	/**
	 * Save specified zip file, leaving unzipped files open
	 * for further editing.
	 * 
	 * @param zip
	 */
	public void storeDomainDiagram(ArroZipFile zip) {
		ArroModule dnd = (ArroModule)zip.getDomainDiagram();
		
		storeNodeDiagram(dnd, zip, zip.getName());
	}
	
	
	private void storeNodeDiagram(ArroModule domainModule, ArroZipFile zip, String fileName) {
		DocumentBuilder builder = null;
		
		try {
		    builder = builderFactory.newDocumentBuilder();
		    	 
			// root elements
			Document doc = builder.newDocument();

			Element elt = doc.createElement("module");
			doc.appendChild(elt);
			
			domainModule.xmlWrite(doc, elt);
	 
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", 4);

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		    Logger.out.trace(Logger.STD, "Saving to " + fileName);
			
			DOMSource source = new DOMSource(doc);
			ByteArrayOutputStream fXmlFile = new ByteArrayOutputStream();

			StreamResult result = new StreamResult(fXmlFile /*new File(fullPath)*/);
			transformer.transform(source, result);
	 
			// Output to console for testing
			StreamResult result2 = new StreamResult(System.out);
			transformer.transform(source, result2);

			IFile f = zip.getFile(Constants.HIDDEN_RESOURCE + fileName + ".xml");
			f.setContents(new ByteArrayInputStream(fXmlFile.toByteArray()), true, true, null);
	 
			Logger.out.trace(Logger.STD, "File saved!");
	 
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
	/**
	 * Get singleton object.
	 * 
	 * @return
	 */
	public static ResourceCache getInstance() {
		if(myCache == null) {
			myCache = new ResourceCache();
		}
		return myCache;
	}

	   /**
     * Open domain file <typeName> by unzipping it:
     * - <typeName>.anod into .<typeName>.anod and .<typeName>.anod.xml
     * Zip file also contains other stuff, but we only need module here.
     * 
     * Then read the domain file (.<typeName>.anod) into a DomainModule
     * instance and register this instance in the cache.
     * 
     * FIXME: must search all resources in the open project.
     */
    public void removeFromCache(String typeName) throws RuntimeException {

        if(cache.containsKey(typeName)) {
            cache.remove(typeName);
        }
    }


	/**
	 * First call for opening a resource will allow this function
	 * to determine the folder where all files exist.
	 * In future a more advanced mechanism could be thought of.
	 * 
	 * @param fei
	 * @return
	 */
   public IFolder getDiagramFolder(FileEditorInput fei) {
    	if(fei != null) {
            IProject project = fei.getFile().getProject();
            IPath p = fei.getFile().getProjectRelativePath().removeLastSegments(1);
            Logger.out.trace(Logger.STD, p.toString());
            save = project.getFolder(p.toString());
            return project.getFolder(p.toString());
    	} else {
    		return save; // FIXME something better? --> caller must search through all resources in the project
    	}
    }

	    

}

