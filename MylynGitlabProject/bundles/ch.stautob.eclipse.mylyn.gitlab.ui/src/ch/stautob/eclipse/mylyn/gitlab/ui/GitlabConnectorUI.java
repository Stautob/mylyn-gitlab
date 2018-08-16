package ch.stautob.eclipse.mylyn.gitlab.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;

import ch.stautob.eclipse.mylyn.gitlab.core.Activator;
import ch.stautob.eclipse.mylyn.gitlab.core.attributes.GitlabAttribute;


public class GitlabConnectorUI extends AbstractRepositoryConnectorUi {

   @Override
   public String getConnectorKind() {
      return Activator.CONNECTOR_KIND;
   }

   @Override
   public IWizard getNewTaskWizard(TaskRepository repository, ITaskMapping mapping) {
      return new NewTaskWizard(repository, mapping);
   }

   @Override
   public IWizard getQueryWizard(TaskRepository repository, IRepositoryQuery query) {
      RepositoryQueryWizard wizard = new RepositoryQueryWizard(repository);
      wizard.addPage(new GitlabQueryPage("New Page", repository, query));
      return wizard;
   }

   @Override
   public ITaskRepositoryPage getSettingsPage(TaskRepository repository) {
      return new GitlabRepositorySettingsPage(Strings.NEW_REPOSITORY, Strings.SETTINGS_PAGE, repository);
   }

   @Override
   public boolean hasSearchPage() {
      return false;
   }

   @Override
   public ImageDescriptor getTaskKindOverlay(ITask task) {
      switch (task.getTaskKind()) {
      case GitlabAttribute.TypeBug:
         return GitlabImages.OVERLAY_BUG;
      case GitlabAttribute.TypeFeature:
         return GitlabImages.OVERLAY_FEATURE;
      case GitlabAttribute.TypeStory:
         return GitlabImages.OVERLAY_STORY;
      default:
         return super.getTaskKindOverlay(task);
      }
   }

   @Override
   public String getReplyText(TaskRepository taskRepository, ITask task, ITaskComment taskComment, boolean includeTask) {
      return "Reply to " + taskComment.getAuthor();
   }

}
