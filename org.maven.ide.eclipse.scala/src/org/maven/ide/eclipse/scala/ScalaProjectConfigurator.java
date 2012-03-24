/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IClasspathEntryDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;


//TODO check the jre/java version compliance (>= 1.5)
//TODO check JDT Weaving is enabled (if not enabled, icon of scala file is [J] same as java (and property of  the file display "Type :... Java Source File" )
//TODO check that pom.xml and ScalaLib Container declare the same scala version
//TODO keep sync scala-compiler configuration between pom.xml and scala-plugin ? (sync bi-direction) ?
//TODO ask sonatype/mailing-list about how to retreive maven plugin configuration, like additional sourceDirectory
/**
 * @author Sonatype Inc (http://github.com/sonatype/m2eclipse-scala)
 * @author davidB (http://github.com/davidB)
 * @author germanklf  (http://github.com/germanklf)
 */
public class ScalaProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

  /////////////////////////////////////////////////////////////////////////////
  // STATIC
  /////////////////////////////////////////////////////////////////////////////

  private static String MOJO_GROUP_ID = "org.scala-tools";

  private static String MOJO_ARTIFACT_ID = "maven-scala-plugin";

  private Map<String, Integer> mapSourceTypeWeight;

  private Map<String, Integer> mapResourceWeight;

  private Comparator<IClasspathEntry> comparator;

  public ScalaProjectConfigurator() {
    mapSourceTypeWeight = new HashMap<String, Integer>();
    mapSourceTypeWeight.put("src/main/", 9000);
    mapSourceTypeWeight.put("src/test/", 1000);

    mapResourceWeight = new HashMap<String, Integer>();
    mapResourceWeight.put("java", 100);
    mapResourceWeight.put("resources", -10);

    comparator = new Comparator<IClasspathEntry>() {
      public int compare(IClasspathEntry o1, IClasspathEntry o2) {
        Integer w1 = getWeight(o1);
        Integer w2 = getWeight(o2);
        if(w1 > 0 || w2 > 0) {
          if(w1.equals(w2))
            return o1.getPath().toString().compareTo(o2.getPath().toString());
          else
            return w1.compareTo(w2) * -1;
        }
        return 0;
      }

      private Integer getWeight(IClasspathEntry ce) {
        int value = 0;
        if(ce.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
          for(Entry<String, Integer> e : mapSourceTypeWeight.entrySet()) {
            if(ce.getPath().toString().contains(e.getKey())) {
              value += e.getValue();
            }
          }
          for(Entry<String, Integer> e : mapResourceWeight.entrySet()) {
            if(ce.getPath().toString().endsWith(e.getKey())) {
              value += e.getValue();
            }
          }
        }
        return value;
      }
    };
  }

  /////////////////////////////////////////////////////////////////////////////
  // INSTANCE
  /////////////////////////////////////////////////////////////////////////////

  private String scalaNatureId() {
    ScalaPluginIds ids = Activator.getInstance().scalaPluginIds();
    return (ids == null) ? null : ids.natureId;
  }

  private String scalaLibId() {
    ScalaPluginIds ids = Activator.getInstance().scalaPluginIds();
    return (ids == null) ? null : ids.containerLibId;
  }

  @Override
  public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
    String scalaNature = scalaNatureId();
    if(scalaNature == null) {
      //TODO show an alert to user that he should to install scala-ide plugin;
      return;
    }
    try {
      if(request != null) {
        IProject project = request.getProject();
        if(!project.hasNature(scalaNature) && isScalaProject(request.getMavenProjectFacade(), monitor)) {
          addNature(project, scalaNature, monitor);
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * configure Classpath : contain of "Maven Dependencies" Librairies Container.
   */
  public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor)
      throws CoreException {
    String scalaNature = scalaNatureId();
    if(scalaNature == null) {
      //TODO show an alert to user that he should to install scala-ide plugin;
      return;
    }
    if(!isLaunchConfigurationCtx()) {
      IProject project = facade.getProject();
      if(isScalaProject(project)) {
        //      if(!project.hasNature(ID_NATURE)) {
        //        addNature(project, ID_NATURE, monitor);
        //      }
        removeScalaFromMavenContainer(classpath);
        addDefaultScalaSourceDirs(facade, classpath, monitor);
        sortContainerScalaJre(project, monitor); //
      }
    }
  }

  /**
   * To work as maven-scala-plugin, src/main/scala and src/test/scala are added if directory exists
   * 
   * @param facade
   * @throws CoreException
   */
  //TODO take a look at http://github.com/sonatype/m2eclipse-extras/blob/master/org.maven.ide.eclipse.temporary.mojos/src/org/maven/ide/eclipse/buildhelper/BuildhelperProjectConfigurator.java
  private void addDefaultScalaSourceDirs(IMavenProjectFacade facade, IClasspathDescriptor classpath,
      IProgressMonitor monitor) throws CoreException {
    IProject project = facade.getProject();
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
    int initSize = rawClasspath.length;

    // can't use classpath.addSourceEntry because source entry are append under "Maven Dependencies" container
    IFolder defaultMainSrc = project.getFolder("src/main/scala");
    if(defaultMainSrc != null && defaultMainSrc.exists()) {
      IPath p = defaultMainSrc.getFullPath();
      rawClasspath = addSourceEntry(rawClasspath, p, facade.getOutputLocation());
    }

    IFolder defaultTestSrc = project.getFolder("src/test/scala");
    if(defaultTestSrc != null && defaultTestSrc.exists()) {
      IPath p = defaultTestSrc.getFullPath();
      rawClasspath = addSourceEntry(rawClasspath, p, facade.getTestOutputLocation());
    }

    if(rawClasspath.length != initSize) {
      javaProject.setRawClasspath(rawClasspath, monitor);
    }

  }

  private IClasspathEntry[] addSourceEntry(IClasspathEntry[] rawClasspath, IPath sourcePath, IPath outputLocation) {
//    if (!classpath.containsPath(sourcePath)) {
//      classpath.addSourceEntry(sourcePath, facade.getTestOutputLocation(), false);
//    }
    IClasspathEntry[] back = rawClasspath;
    IClasspathEntry entry = JavaCore.newSourceEntry(sourcePath, //
        new IPath[0], //
        new IPath[0], //
        outputLocation, //
        new IClasspathAttribute[0]);
    if(!contains(rawClasspath, entry)) {
      back = add(rawClasspath, entry);
    }
    return back;
  }

  private boolean contains(IClasspathEntry[] rawClasspath, IClasspathEntry entry) {
    if(entry != null && rawClasspath != null) {
      for(IClasspathEntry e : rawClasspath) {
        if(entry.equals(e)) {
          return true;
        }
      }
    }
    return false;
  }

  //TODO add source in the right position (near other source folder and in the alphanum order
  private IClasspathEntry[] add(IClasspathEntry[] rawClasspath, IClasspathEntry entry) {
    IClasspathEntry[] back = new IClasspathEntry[rawClasspath.length + 1];
    back[0] = entry;
    System.arraycopy(rawClasspath, 0, back, 1, rawClasspath.length);
    return back;
  }

  private void removeScalaFromMavenContainer(IClasspathDescriptor classpath) {
    classpath.removeEntry(new IClasspathDescriptor.EntryFilter() {
      public boolean accept(IClasspathEntryDescriptor descriptor) {
        boolean back = "org.scala-lang".equals(descriptor.getGroupId());
        //TODO, use content of Scala Library Container instead of hardcoded value
        back = back && ("scala-library".equals(descriptor.getArtifactId())
        //|| "scala-compiler".equals(descriptor.getArtifactId())
            || "scala-dbc".equals(descriptor.getArtifactId()) || "scala-swing".equals(descriptor.getArtifactId()));
        return back;
      }
    });
  }

//  /**
//   * To work as maven-scala-plugin, src/main/scala and src/test/scala are added if directory exists
//   *
//   * @param facade
//   * @throws CoreException
//   */
//  //TODO take a look at http://github.com/sonatype/m2eclipse-extras/blob/master/org.maven.ide.eclipse.temporary.mojos/src/org/maven/ide/eclipse/buildhelper/BuildhelperProjectConfigurator.java
//  private void addDefaultScalaSourceDirs(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor) throws CoreException {
//    IProject project = facade.getProject();
//
//    // can't use classpath.addSourceEntry because source entry are append under "Maven Dependencies" container
//    IResource defaultMainSrc = project.findMember("src/main/scala");
//    if (defaultMainSrc != null && defaultMainSrc.exists()) {
//      IPath p = defaultMainSrc.getFullPath();
//      classpath.addSourceEntry(p, facade.getOutputLocation(), false);
//    }
//
//    IResource defaultTestSrc = project.findMember("src/test/scala");
//    if (defaultTestSrc != null && defaultTestSrc.exists()) {
//      IPath p = defaultTestSrc.getFullPath();
//      classpath.addSourceEntry(p, facade.getTestOutputLocation(), false);
//    }
//  }

  /**
   * Not called with m2eclipse 0.10.0 Configure the eclipse project classpath (similar to eclipse
   * IJavaProject.getRawClasspath).
   */
  public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath,
      IProgressMonitor monitor) throws CoreException {
//    System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//    IProject project = request.getProject();
//    if(isScalaProject(project)) {
//      addDefaultScalaSourceDirs(request.getMavenProjectFacade(), classpath, monitor);
//      sortContainerScalaJre(project, monitor);
//    }

  }

  /**
   * Check and reorder Containers : "Scala Lib" should be before "JRE Sys", else 'Run Scala Application' set Boot
   * Entries JRE before Scala and failed with scala.* NotFound Exception. Should already be done when adding nature
   * 
   * @see scala.tools.eclipse.Nature#configure()
   */
  private void sortContainerScalaJre(IProject project, IProgressMonitor monitor) throws CoreException {
    IJavaProject javaProject = JavaCore.create(project);
    IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();

    List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>(classpathEntries.length);
    Collections.addAll(entries, classpathEntries);

    Collections.sort(entries, comparator);
    javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), monitor);
  }

  private boolean isScalaProject(IProject project) {
    try {
      return project != null && project.isAccessible() && project.hasNature(scalaNatureId());
    } catch(CoreException e) {
      return false;
    }
  }

  private boolean isScalaProject(IMavenProjectFacade facade, IProgressMonitor monitor) throws CoreException {
    List<Plugin> plugins = facade.getMavenProject(monitor).getBuildPlugins();
    if(plugins != null) {
      for(Plugin plugin : plugins) {
        if(isMavenBundlePluginMojo(plugin) && !plugin.getExecutions().isEmpty()) {
          return true;
        }
      }
    }
    return false;
  }

  protected boolean isLaunchConfigurationCtx() {
    for(StackTraceElement e : Thread.currentThread().getStackTrace()) {
      if("launch".equals(e.getMethodName())) {
        return true;
      }
    }
    return false;
  }

  protected boolean isMavenBundlePluginMojo(MojoExecution execution) {
    return isMavenBundlePluginMojo(execution.getGroupId(), execution.getArtifactId());
  }

  private boolean isMavenBundlePluginMojo(Plugin plugin) {
    return isMavenBundlePluginMojo(plugin.getGroupId(), plugin.getArtifactId());
  }

  private boolean isMavenBundlePluginMojo(String groupId, String artifactId) {
    return MOJO_GROUP_ID.equals(groupId) && MOJO_ARTIFACT_ID.equals(artifactId);
  }

}
