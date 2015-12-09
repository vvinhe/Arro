package property;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import arro.domain.ArroParameter;


public class EditingSupportForValue extends EditingSupport {

  private final TableViewer viewer;
  private final CellEditor editor;
  private final ArroParamSection paramEditor;

	class DeltaInfoOperation extends AbstractOperation {
		Object undoList, redoList;
		ArroParameter selectedArroParameter;
		String undoName, redoName, newName;
		public DeltaInfoOperation(ArroParameter message, String name) {
			super("Name change");
			this.selectedArroParameter = message;
			this.newName = name;
			paramEditor.update();
		}
		public IStatus execute(IProgressMonitor monitor, IAdaptable info) {
			undoName = selectedArroParameter.getValue();
			selectedArroParameter.setValue(newName);
			paramEditor.update();
			return Status.OK_STATUS;
		}
		public IStatus undo(IProgressMonitor monitor, IAdaptable info) {
			redoName = selectedArroParameter.getValue();
			selectedArroParameter.setValue(undoName);
			paramEditor.update();
			return Status.OK_STATUS;
		}
		public IStatus redo(IProgressMonitor monitor, IAdaptable info) {
			undoName = selectedArroParameter.getValue();
			selectedArroParameter.setValue(redoName);
			paramEditor.update();
			return Status.OK_STATUS;
		}
	}
	
  public EditingSupportForValue(TableViewer viewer, ArroParamSection paramEditor) {
    super(viewer);
    this.viewer = viewer;
    this.paramEditor = paramEditor;
    this.editor = new TextCellEditor(viewer.getTable());
  }

  @Override
  protected CellEditor getCellEditor(Object element) {
    return editor;
  }

  @Override
  protected boolean canEdit(Object element) {
    return true;
  }

  @Override
  protected Object getValue(Object element) {
    return ((ArroParameter) element).getValue();
  }

  @Override
  protected void setValue(Object element, Object userInputValue) {
    IUndoableOperation operation = new DeltaInfoOperation(((ArroParameter) element), String.valueOf(userInputValue));
  	operation.addContext(paramEditor.getUndoContext());
  	try {
		paramEditor.getOperationHistory().execute(operation, null, null);
	} catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    viewer.update(element, null);

  }
} 