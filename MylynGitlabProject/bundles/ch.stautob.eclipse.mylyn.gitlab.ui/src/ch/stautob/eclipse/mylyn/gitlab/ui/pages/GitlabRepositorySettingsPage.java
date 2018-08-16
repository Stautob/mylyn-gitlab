package ch.stautob.eclipse.mylyn.gitlab.ui.pages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.stautob.eclipse.mylyn.gitlab.core.Activator;
import ch.stautob.eclipse.mylyn.gitlab.core.connection.GitlabConnector;
import ch.stautob.eclipse.mylyn.gitlab.core.utils.TaskRepositoryUtil;


public class GitlabRepositorySettingsPage extends AbstractRepositorySettingsPage {

   private Button useToken;

   private Text gitlabBaseUrl;

   private Pattern urlPattern = Pattern.compile("((http[s]?|ftp):\\/?\\/)?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?");

   private String usernameBackup = null;

   public GitlabRepositorySettingsPage(String title, String description, TaskRepository taskRepository) {
      super(title, description, taskRepository);
      setNeedsValidateOnFinish(true);
   }

   @Override
   protected void createAdditionalControls(final Composite composite) {

      savePasswordButton.setSelection(true);

      useToken = new Button(composite, SWT.CHECK);
      useToken.setText(Messages.GitlabRepositorySettingsPage_UseAccessToken);

      GridDataFactory.fillDefaults().span(2, 1).applyTo(useToken);

      /**
       * Enable Token login by default as username/password login seems to be broken in gitlab > 10
       */

      useToken.setSelection(true);
      setTokenLoginEnabled(true);

      useToken.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {

            setTokenLoginEnabled(useToken.getSelection());
            getWizard().getContainer().updateButtons();
         }

         @Override
         public void widgetDefaultSelected(SelectionEvent e) {}
      });

      Label l = new Label(composite, SWT.NONE);
      l.setText(Messages.GitlabRepositorySettingsPage_BaseUrl);

      gitlabBaseUrl = new Text(composite, SWT.SINGLE | SWT.BORDER);
      GridDataFactory.fillDefaults().span(1, 1).applyTo(gitlabBaseUrl);

      serverUrlCombo.addModifyListener(new ModifyListener() {

         @Override
         public void modifyText(ModifyEvent e) {
            Matcher m = urlPattern.matcher(serverUrlCombo.getText().trim());
            if (m.matches()) gitlabBaseUrl.setMessage(m.group(1) + m.group(3));
         }

      });

      /**
       * Set widget texts and check boxes if necessary.
       */
      if (serverUrlCombo.getText().isEmpty()) {
         // This means, that there the user is *not* editing an existing repository configuration
         serverUrlCombo.setToolTipText("Enter a repository url");
         serverUrlCombo.setText(Messages.GitlabRepositorySettingsPage_HostShadowText);
      }

      if (getRepository() != null) {
         if (TaskRepositoryUtil.getUsesPrivateToken(getRepository())) {
            useToken.setSelection(true);
            setTokenLoginEnabled(false);
         }

         if (TaskRepositoryUtil.getGitlabBaseUrl(getRepository()) != null) {
            gitlabBaseUrl.setText(TaskRepositoryUtil.getGitlabBaseUrl(getRepository()));
         }
      }
   }

   private void setTokenLoginEnabled(boolean enabled) {
      repositoryUserNameEditor.getTextControl(compositeContainer).setEnabled(enabled);
      if (!enabled) {
         repositoryUserNameEditor.setStringValue(usernameBackup);
         repositoryPasswordEditor.setLabelText(LABEL_PASSWORD);
      } else {
         usernameBackup = repositoryUserNameEditor.getStringValue();
         repositoryUserNameEditor.setStringValue(null);
         repositoryPasswordEditor.setLabelText(Messages.GitlabRepositorySettingsPage_AccessTokenRequest);
      }
      repositoryUserNameEditor.setEmptyStringAllowed(!enabled);
      compositeContainer.layout();
   }

   @Override
   public String getConnectorKind() {
      return Activator.CONNECTOR_KIND;
   }

   @Override
   public TaskRepository createTaskRepository() {
      TaskRepository repo = super.createTaskRepository();
      return repo;
   }

   @Override
   public void applyTo(TaskRepository repository) {
      repository.setCategory(TaskRepository.CATEGORY_BUGS);
      super.applyTo(repository);
      TaskRepositoryUtil.setUsePrivateToken(repository, useToken.getSelection());
      TaskRepositoryUtil.setGitlabBaseUrl(repository, gitlabBaseUrl.getText().isEmpty() ? gitlabBaseUrl.getMessage() : gitlabBaseUrl.getText());
   }

   @Override
   protected boolean isMissingCredentials() {
      if (useToken != null && useToken.getSelection()) {
         return !repositoryPasswordEditor.getStringValue().chars().anyMatch(i -> !Character.isWhitespace(i));
      } else {
         return super.isMissingCredentials();
      }
   }

   @Override
   protected Validator getValidator(final TaskRepository repository) {
      return new Validator() {

         @Override
         public void run(IProgressMonitor monitor) throws CoreException {
            GitlabConnector.validate(repository);
         }

      };
   }

}
