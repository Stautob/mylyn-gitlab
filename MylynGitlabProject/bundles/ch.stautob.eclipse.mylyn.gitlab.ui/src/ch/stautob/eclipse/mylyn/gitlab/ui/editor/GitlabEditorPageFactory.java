package ch.stautob.eclipse.mylyn.gitlab.ui.editor;

import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.ui.ITasksUiConstants;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPageFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.editor.IFormPage;

import ch.stautob.eclipse.mylyn.gitlab.core.Activator;
import ch.stautob.eclipse.mylyn.gitlab.ui.Messages;


public class GitlabEditorPageFactory extends AbstractTaskEditorPageFactory {

   @Override
   public boolean canCreatePageFor(TaskEditorInput input) {
      if (input.getTask().getConnectorKind().equals(Activator.CONNECTOR_KIND) || //
          TasksUiUtil.isOutgoingNewTask(input.getTask(), Activator.CONNECTOR_KIND)) {
         return true;
      } else {
         return false;
      }
   }

   @Override
   public IFormPage createPage(TaskEditor editor) {
      return new GitlabEditorPage(editor, Activator.CONNECTOR_KIND);
   }

   @Override
   public int getPriority() {
      return 0;
   }

   @Override
   public Image getPageImage() {
      return CommonImages.getImage(TasksUiImages.TASK);
   }

   @Override
   public String getPageText() {
      return Messages.Strings_issue;
   }

   @Override
   public String[] getConflictingIds(TaskEditorInput input) {
      return new String[] { ITasksUiConstants.ID_PAGE_PLANNING };
   }

}
