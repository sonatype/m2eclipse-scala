/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.scala;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.maven.ide.eclipse.jdt.IClasspathDescriptor;
import org.maven.ide.eclipse.jdt.IJavaProjectConfigurator;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;

public class ScalaProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

  public static String ID_NATURE = "ch.epfl.lamp.sdt.core.scalanature";
  public static String MOJO_GROUP_ID = "org.scala-tools";
  public static String MOJO_ARTIFACT_ID = "maven-scala-plugin";
  
  //public static String SCALA_PLUGIN_ID_BUILDER = "ch.epfl.lamp.sdt.core.scalabuilder";

  public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
    MavenProject mavenProject = request.getMavenProject();
    IProject project = request.getProject();
    if(!project.hasNature(ID_NATURE) && isScalaProject(request.getMavenProjectFacade(), monitor)) {
      addNature(project, ID_NATURE, monitor);
    }
  }

  public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor)
      throws CoreException {

    /*
    if(isAjdtProject(facade.getProject())) {
      // TODO cache in facade.setSessionProperty
      ScalaPluginConfiguration config = ScalaPluginConfiguration.create( //
          facade.getMavenProject(monitor), facade.getProject());
      if(config != null) {
        for (IClasspathEntryDescriptor descriptor : classpath.getEntryDescriptors()) {
          String key = descriptor.getGroupId() + ":" + descriptor.getArtifactId();
          Set<String> aspectLibraries = config.getAspectLibraries(); // from pom.xml
          if(aspectLibraries != null && aspectLibraries.contains(key)) {
            descriptor.addClasspathAttribute(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE);
            continue;
          }
          Set<String> inpathDependencies = config.getInpathDependencies();
          if (inpathDependencies != null && inpathDependencies.contains(key)) {
            descriptor.addClasspathAttribute(AspectJCorePreferences.INPATH_ATTRIBUTE);
          }
        }
      }
    }
    */
  }

  public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath,
      IProgressMonitor monitor) throws CoreException {
    // TODO Auto-generated method configureRawClasspath

  }

  static boolean isAjdtProject(IProject project) {
    try {
      return project != null && project.isAccessible() && project.hasNature(ID_NATURE);
    } catch(CoreException e) {
      return false;
    }
  }

  private boolean isScalaProject(IMavenProjectFacade facade, IProgressMonitor monitor) 
    throws CoreException {
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
