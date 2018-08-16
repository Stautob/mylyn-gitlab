package ch.stautob.eclipse.mylyn.gitlab.ui;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {

   private static final String BUNDLE_NAME = "ch.stautob.eclipse.mylyn.gitlab.ui.messages"; //$NON-NLS-1$
   public static String        Strings_Assignee;
   public static String        Strings_EnterUrl;
   public static String        Strings_issue;
   public static String        Strings_LabelRegex;
   public static String        Strings_Labels;
   public static String        Strings_Milestone;
   public static String        Strings_NewRepo;
   public static String        Strings_QueryTitle;
   public static String        Strings_RemoveSelectedLbl;
   public static String        Strings_State;
   static {
      // initialize resource bundle
      NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }

   private Messages() {}
}
