package de.xgme.jojo.jigsaw_gradle_plugin.extension.task;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities.*;

public interface JavadocExtension
  extends Activatable,
          WithModuleName,
          WithDynamicExports,
          WithDynamicReads
{
}
