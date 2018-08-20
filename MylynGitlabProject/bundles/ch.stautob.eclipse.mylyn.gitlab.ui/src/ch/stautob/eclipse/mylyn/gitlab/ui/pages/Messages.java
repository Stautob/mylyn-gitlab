package ch.stautob.eclipse.mylyn.gitlab.ui.pages;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {

   private static final String BUNDLE_NAME = "ch.stautob.eclipse.mylyn.gitlab.ui.pages.messages"; //$NON-NLS-1$
   public static String        GitlabRepositorySettingsPage_AccessTokenRequest;
   public static String        GitlabRepositorySettingsPage_BaseUrl;
   public static String        GitlabRepositorySettingsPage_HostShadowText;
   public static String        GitlabRepositorySettingsPage_UseAccessToken;
   static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages() {}
}
