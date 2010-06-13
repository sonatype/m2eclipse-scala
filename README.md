m2e-scala
========

[m2e-scala] is a work in progress to ease integration between [m2eclipse] and [scala-ide for eclipse].

* issue tracker : [m2e-scala at assembla](http://scala-ide.assembla.com/spaces/m2e-scala/tickets)
* mailing-list : [scala-ide-user](http://groups.google.fr/group/scala-ide-user)
* update-site : not available (use dropins directory see below for install)

### Install
1. install [m2eclipse]
2. install [m2e-scala] from davidB, by [dowloading](http://github.com/davidB/m2e-scala/downloads) the lastest version of the plugin  into your eclipse/dropins directory
3. Eclipse : `File > import > maven > Existing Maven Project ...`



### Working configuration

The plugin is "valitaded" by importing into eclipse the samples projects :

* [Scala Only (no java)](http://github.com/davidB/m2e-scala/tree/master/samples/prj-scala-only/)
* [Scala depends of some java code](http://github.com/davidB/m2e-scala/tree/master/samples/prj-scala-after-java/)
* [Scala and Java cyclic dependency](http://github.com/davidB/m2e-scala/tree/master/samples/prj-scala-cycle-java/) (configuration longer than mono direction scala -> java or java -> scala to compile under maven)
* [Java main code and Scala for test](http://github.com/davidB/m2e-scala/tree/master/samples/prj-java-test-in-scala/)
* [Custom Layout](http://github.com/davidB/m2e-scala/tree/master/samples/prj-custom-layout/) java + scala cyclic dependencies, java+scala under the same source folder `src` and `test` (for test source folder).

### Changes

#### 0.0.5 (wip)

* use assembla as "project host" to be under Scala-IDE umbrella
* re-do tycho configuration (to build on any box)
* rename directory
* replace Sonatype as Vendor by Alchim31
* generate update-site via tycho

#### 0.0.4


* include a workaround to run JUnit test, but right-click on JUnit test doesn't propose 'Run as unit test'. Tu run as junit :
  * select source file and use keybord shortcut (Alt+Shit+X T)
    * source file have to be named with test class name and being in the 'right' directory (like in java)
    * I only test with annotation (see source of samples projects listed on wiki)
  * create JUnit Launch configuration

#### 0.0.3

* try to fix a issue (NPE) if default folders doesn't exist

#### 0.0.2


* auto add "src/main/scala" and "src/test/scala" if you don't use goal "add-sources" (nor <sourceDirectory>)
* add samples project http://github.com/davidB/m2e-scala/tree/master/samples/
* put documentation at https://www.assembla.com/wiki/show/scala-ide/With_M2Eclipse

#### 0.0.1

* I fork the m2e-scala project from sonatype and add the following feature
* remove of scala-library, scala-compiler, scala-dbc, scala-swing from "Maven Dependencies" Container (under eclipse only)
* re-order JRE Container and Scala Container (to avoid scala.ScalaObject NotFound and C° when use "Run As Scala Application")
* add Scala-tools.org catalog


   [maven-scala-plugin]: http://scala-tools.org/mvnsites/maven-scala-plugin/
   [maven-eclipse-plugin]: http://maven.apache.org/plugins/maven-eclipse-plugin
   [build-helper-maven-plugin]: http://mojo.codehaus.org/build-helper-maven-plugin/
   [m2eclipse]: http://m2eclipse.sonatype.org/
   [m2e-scala]: https://www.assembla.com/wiki/edit/scala-ide/With_M2Eclipse
   [IAM]: http://www.eclipse.org/iam/
   [Q4E]: http://code.google.com/p/q4e/
   [ESMi]: http://code.google.com/p/esmi/
   [scala-ide for eclipse]: http://scala-ide.assembla.com/
