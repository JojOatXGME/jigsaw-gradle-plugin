package de.xgme.jojo.jigsaw_gradle_plugin.extension.task;

import de.xgme.jojo.jigsaw_gradle_plugin.extension.task.capabilities.*;

public interface JavaCompileExtension
  extends Activatable,
          WithModuleName,
          WithModuleVersion,
          WithDynamicExports,
          WithDynamicReads
{
}
