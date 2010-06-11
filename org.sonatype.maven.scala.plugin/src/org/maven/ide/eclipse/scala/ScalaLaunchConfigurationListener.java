package org.maven.ide.eclipse.scala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;

/**
 *
 * ScalaLaunchConfigurationListener is a workaround,
 * because m2eclipse override classpath of Launcher (Application, JUnit, TestNG) without including addtionnal Librairie Container define in the project BuildPath.
 * And Scala project need the ScalaLibrairy Container to run.
 *
 * @see http://github.com/sonatype/m2eclipse-core/blob/0.10.0.20100205-2200/org.maven.ide.eclipse.jdt/src/org/maven/ide/eclipse/jdt/internal/launch/MavenRuntimeClasspathProvider.java
 *
 * @author david.bernard
 */
public class ScalaLaunchConfigurationListener implements ILaunchConfigurationListener {

  private StandardClasspathProvider _scp = new StandardClasspathProvider();

  public void launchConfigurationAdded(ILaunchConfiguration configuration) {
//    updateLaunchConfiguration(configuration);
  }

  public void launchConfigurationChanged(ILaunchConfiguration configuration) {
    //updateLaunchConfiguration(configuration);
  }

  public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
  }

  private void updateLaunchConfiguration(ILaunchConfiguration configuration) {
    try {
      IJavaProject javaProject = JavaRuntime.getJavaProject(configuration);
      if (javaProject != null && javaProject.getProject().hasNature(ScalaProjectConfigurator.NATURE_ID)) {
        enable(configuration);
      }
    } catch(CoreException ex) {
      ex.printStackTrace();
    }
  }

  private void enable(ILaunchConfiguration config) throws CoreException {
    if (config instanceof ILaunchConfigurationWorkingCopy) {
      enable((ILaunchConfigurationWorkingCopy) config);
    } else {
      ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
      enable(wc);
      wc.doSave();
    }
  }

  @SuppressWarnings("unchecked")
  private void enable(ILaunchConfigurationWorkingCopy wc) throws CoreException {
    //  m2eclipse define ATTR_CLASSPATH_PROVIDER and ATTR_SOURCE_PATH_PROVIDER (like below)
    //  as we don't know wh will be called last, we prefer to define Scala into the regular classpath
    //wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, MAVEN_CLASSPATH_PROVIDER);
    //wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, MAVEN_SOURCEPATH_PROVIDER);

    // TO avoid MavenRuntimeClasspathProvider to override the classpath
    //TODO to fix when m2eclipse's MavenRuntimeClasspathProvider will support additional Container
    //boolean useDefault = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
    //if (useDefault) {
      wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
    IJavaProject javaProject = JavaRuntime.getJavaProject(wc);
    IClasspathEntry[] entries = javaProject.getRawClasspath();
    List<String> classpath = new ArrayList<String>(entries.length);
    for (IClasspathEntry entry  : entries) {
      IRuntimeClasspathEntry re = null;
      switch(entry.getEntryKind()) {
        case IClasspathEntry.CPE_CONTAINER :
          //TODO if jre or scala set IRuntimeClasspathEntry.BOOTSTRAP_CLASSES
          re = JavaRuntime.newRuntimeContainerClasspathEntry(entry.getPath(),IRuntimeClasspathEntry.USER_CLASSES);
          break;
        case IClasspathEntry.CPE_PROJECT :
//          re = JavaRuntime.newRuntimeContainerClasspathEntry(entry.getPath(),IRuntimeClasspathEntry.USER_CLASSES);
          break;
        case IClasspathEntry.CPE_LIBRARY :
          re = JavaRuntime.newArchiveRuntimeClasspathEntry(entry.getPath());
          break;
        case IClasspathEntry.CPE_VARIABLE :
          re = JavaRuntime.newVariableRuntimeClasspathEntry(entry.getPath());
          break;
      }
      if (re != null) {
        classpath.add(re.getMemento());
      }
    }
    wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
//    }

//      //TODO find a more clean way to grab Path of "Scala Libs"
//      IRuntimeClasspathEntry scalaLibsEntry = JavaRuntime.newRuntimeContainerClasspathEntry(new Path("ch.epfl.lamp.sdt.launching.SCALA_CONTAINER"), IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
//      String scalaLibsMemento = scalaLibsEntry.getMemento();
//      List<String> classpath = new ArrayList<String>();
//      classpath = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
//      if (!classpath.contains(scalaLibsMemento)) {
//        classpath.add(0, scalaLibsMemento);
//        wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classpath);
//        wc.doSave();
//      }

//      if (null == wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, (List)null)) {
//
//        IJavaProject javaProject = JavaRuntime.getJavaProject(wc);
//        IClasspathEntry[] scalaLibsEntries = JavaCore.getClasspathContainer(new Path("ch.epfl.lamp.sdt.launching.SCALA_CONTAINER"), javaProject).getClasspathEntries();
//        List<String> bootClasspath = new ArrayList<String>(scalaLibsEntries.length);
//        for (int i = 0 ; i< scalaLibsEntries.length ; i++) {
//          bootClasspath.add(scalaLibsEntries[i].getPath().makeAbsolute().toString());
//        }
//        wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH, bootClasspath);
//      }

  }
}
