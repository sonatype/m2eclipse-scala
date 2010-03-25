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

/**
 * Configures AJDT project according to aspectj-maven-plugin configuration from pom.xml. Work in progress, most of
 * aspectj-maven-plugin configuration parameters is not supported yet.
 * 
 * @see http://mojo.codehaus.org/aspectj-maven-plugin/compile-mojo.html
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=160393
 * @author Igor Fedorenko
 * @author Eugene Kuleshov
 */
public class ScalaProjectConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

  public static String ID_NATURE = "ch.epfl.lamp.sdt.core.scalanature";
  //public static String SCALA_PLUGIN_ID_BUILDER = "ch.epfl.lamp.sdt.core.scalabuilder";

  public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor)
      throws CoreException {
    MavenProject mavenProject = request.getMavenProject();
    IProject project = request.getProject();
    if(!project.hasNature(ID_NATURE)) {
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
}
