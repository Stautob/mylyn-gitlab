package ch.stautob.eclipse.mylyn.gitlab.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositorySettingsPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.stautob.eclipse.mylyn.gitlab.core.Activator;
import ch.stautob.eclipse.mylyn.gitlab.core.connection.GitlabConnector;

public class GitlabRepositorySettingsPage extends AbstractRepositorySettingsPage {

	private Button useToken;

	private Text gitlabBaseUrl;

	public GitlabRepositorySettingsPage(String title, String description,
			TaskRepository taskRepository) {
		super(title, description, taskRepository);

		setNeedsValidateOnFinish(true);
	}

	@Override
	protected void createAdditionalControls(final Composite composite) {
		savePasswordButton.setSelection(true);

		useToken = new Button(composite, SWT.CHECK);
		useToken.setText("Use private token instead of username/password");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(useToken);

		useToken.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				setUsernameFieldEnabled(!useToken.getSelection());
				getWizard().getContainer().updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Label l = new Label(composite, SWT.NONE);
		l.setText("Gitlab base URL");

		gitlabBaseUrl = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).applyTo(gitlabBaseUrl);

		/**
		 * Set widget texts and check boxes if necessary.
		 */
		if (serverUrlCombo.getText().length() == 0) {
			// This means, that there the user is *not* editing an existing repository configuration
			serverUrlCombo.setText("https://your-host.org/namespace/project.git");
		}

		if(getRepository() != null) {
			if("true".equals(getRepository().getProperty("usePrivateToken"))) {
				useToken.setSelection(true);
				setUsernameFieldEnabled(false);
			}

			if(getRepository().getProperty("gitlabBaseUrl") != null) {
				gitlabBaseUrl.setText(getRepository().getProperty("gitlabBaseUrl"));
			}
		}
	}

	private void setUsernameFieldEnabled(boolean enabled) {
		if(enabled) {
			repositoryUserNameEditor.getTextControl(compositeContainer).setEnabled(true);
			repositoryUserNameEditor.setEmptyStringAllowed(false);
			repositoryPasswordEditor.setLabelText(LABEL_PASSWORD);
			compositeContainer.layout();
		} else {
			repositoryUserNameEditor.setStringValue("");
			repositoryUserNameEditor.getTextControl(compositeContainer).setEnabled(false);
			repositoryUserNameEditor.setEmptyStringAllowed(true);
			repositoryPasswordEditor.setLabelText("Private token:");
			compositeContainer.layout();
		}
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
		if(useToken.getSelection()) {
			repository.setProperty("usePrivateToken", "true");
		} else {
			repository.setProperty("usePrivateToken", "false");
		}

		repository.setProperty("gitlabBaseUrl", gitlabBaseUrl.getText());

	}

	@Override
	protected boolean isMissingCredentials() {
		if(useToken != null && useToken.getSelection()) {
			return repositoryPasswordEditor.getStringValue().trim().equals("");
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
