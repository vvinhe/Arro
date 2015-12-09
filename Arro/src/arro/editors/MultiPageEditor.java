package arro.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.graphiti.ui.editor.DiagramEditorInput;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import util.Logger;
import util.PathUtil;
import util.ArroZipFile;
import arro.Constants;
import arro.domain.DomainNodeDiagram;
import arro.domain.ResourceCache;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class MultiPageEditor extends MultiPageEditorPart implements
        IResourceChangeListener {

    /** The text editor used in page 0. */
    // private TextEditor editor;
    private SubGraphitiEditor editor;
    private TextEditor pythonEditor;
    
    private ArroZipFile zip = null;

	private int documentType = Constants.FunctionFile;

    /** The font chosen in page 1. */
    private Font font;

    /** The text widget used in page 2. */
    private StyledText text;

    /**
     * Creates a multi-page editor example.
     */
    public MultiPageEditor() {
        super();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }
    
    /**
     * Creates page 0 of the multi-page editor, which contains a text editor.
     */
    void createPage0(String fileName, ArroZipFile zip) {

        IFile file = zip.getFile(Constants.HIDDEN_RESOURCE + fileName);
        if(file != null) {
        	FileEditorInput fei2 = new FileEditorInput(file);
        	

            try {
            	// pass the file so later it knows where to store the .xml file.
                editor = new SubGraphitiEditor(zip, documentType);
                
                int index = addPage(editor, fei2);
                setPageText(index, "Function Diagram");
            } catch (PartInitException e) {
                ErrorDialog.openError(getSite().getShell(),
                        "Error creating nested text editor", null, e.getStatus());
            }
        }
    }

    /**
     * Creates page 1 of the multi-page editor, which shows the sorted text.
     */
    void createPage1(String fileName, ArroZipFile zip) {
        IFile file = zip.getFile(Constants.HIDDEN_RESOURCE + fileName + ".py");
        if(file != null) {
        	FileEditorInput fei2 = new FileEditorInput(file);
        	
    		if(zip.getMETA("type").equals(Constants.CODE_BLOCK)) {
    			
                try {
                	// pass the file so later it knows where to store the .py file.
                    pythonEditor = new TextEditor();
                    
                    int index = addPage(pythonEditor, fei2);
                    setPageText(index, "Python Code");
                } catch (PartInitException e) {
                    ErrorDialog.openError(getSite().getShell(),
                            "Error creating nested text editor", null, e.getStatus());
                }
    		}
        }
    }

    /**
     * Creates page 2 of the multi-page editor, which allows you to change the
     * font used in page 2.
     */
    void createPage2() {

        Composite composite = new Composite(getContainer(), SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        layout.numColumns = 2;

        Button fontButton = new Button(composite, SWT.NONE);
        GridData gd = new GridData(GridData.BEGINNING);
        gd.horizontalSpan = 2;
        fontButton.setLayoutData(gd);
        fontButton.setText("Change Font...");

        fontButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                setFont();
            }
        });

        int index = addPage(composite);
        setPageText(index, "Properties");
    }

    /**
     * Creates the pages of the multi-page editor.
     */
    protected void createPages() {
    	// Open the ZIP file and create unzipped files
    	
        // let's here unzip the file and create a FileEditorInput for each of the subfiles.
        FileEditorInput fei = (FileEditorInput) getEditorInput();
        
        // Make this call to do some init inside ResourceCache. It's a bit ugly..
        ResourceCache.getInstance().getDiagramFolder(fei);
        
        String fileName = fei.getFile().getName();
        
        // Unzip Function Diagram file and load domain data in cache.
        zip = ResourceCache.getInstance().getZip(PathUtil.truncExtension(fileName));
        
        if(zip.getMETA("type").equals(Constants.FUNCTION_BLOCK)) {
			documentType = Constants.FunctionFile;
		} else {
			documentType = Constants.DeviceDiagram;
        }

        // Create page 0 containing Graphiti editor. File was just unzipped in ResourceCache.
        createPage0(fei.getName(), zip);
        if(documentType == Constants.DeviceDiagram) {
            createPage1(fei.getName(), zip);
        }
        //createPage2();
    }

    
    /**
     * The <code>MultiPageEditorPart</code> implementation of this
     * <code>IWorkbenchPart</code> method disposes all nested editors.
     * Subclasses may extend.
     */
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        
        super.dispose();
    }

    /**
     * Saves the multi-page editor's document.
     */
    public void doSave(IProgressMonitor monitor) {
        getEditor(0).doSave(monitor);
        if(documentType == Constants.DeviceDiagram) {
            getEditor(1).doSave(monitor);
        }
        
        // then zip the whole thing again
        FileEditorInput fei = (FileEditorInput) getEditorInput();
		
        zip.save();
    }

    /**
     * Saves the multi-page editor's document as another file. Also updates the
     * text for page 0's tab, and updates this multi-page editor's input to
     * correspond to the nested editor's.
     */
    public void doSaveAs() {
        IEditorPart editor = getEditor(0);
        editor.doSaveAs();
        setPageText(0, editor.getTitle());
        setInput(editor.getEditorInput());
    }

    /*
     * (non-Javadoc) Method declared on IEditorPart
     */
    public void gotoMarker(IMarker marker) {
        setActivePage(0);
        IDE.gotoMarker(getEditor(0), marker);
    }

    /**
     * The <code>MultiPageEditor</code> implementation of this method
     * checks that the input is an instance of <code>IFileEditorInput</code>.
     */
    public void init(IEditorSite site, IEditorInput editorInput)
            throws PartInitException {
        if (!(editorInput instanceof IFileEditorInput)
                && !(editorInput instanceof DiagramEditorInput))
            throw new PartInitException("Invalid Input: Must be Arro Diagram");

        super.init(site, editorInput);
        
		setPartName(editorInput.getName());

    }

    /*
     * (non-Javadoc) Method declared on IEditorPart.
     */
    public boolean isSaveAsAllowed() {
        return true;
    }

    /**
     * Calculates the contents of page 2 when the it is activated.
     */
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        // if (newPageIndex == 2) {
        // sortWords();
        // }
    }

    /**
     * Closes all project files on project close.
     */
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
                            .getPages();
                    for (int i = 0; i < pages.length; i++) {
                        if (((FileEditorInput) editor.getEditorInput())
                                .getFile().getProject()
                                .equals(event.getResource())) {
                            IEditorPart editorPart = pages[i].findEditor(editor
                                    .getEditorInput());
                            pages[i].closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }

    /**
     * Sets the font related data to be applied to the text in page 2.
     */
    void setFont() {
        FontDialog fontDialog = new FontDialog(getSite().getShell());
        fontDialog.setFontList(text.getFont().getFontData());
        FontData fontData = fontDialog.open();
        if (fontData != null) {
            if (font != null)
                font.dispose();
            font = new Font(text.getDisplay(), fontData);
            text.setFont(font);
        }
    }
    
    


}
