# Zeppelin-Remote-Run
Intelij IDEA plugin for integration with Zeppelin.

## For plugin develops:
### How to set project
1. Clone a project.
1. In _build.sbt_ write a right path for dataViz plugin.
1. In project folder in **terminal (not in sbt shell)** run `sbt updateIdea`. _It is connected with a bug in sbt-idea-plugin_.
1. Open IDEA and import the project as sbt project.
1. Restore deleted files from .idea project.
1. In sbt setting for current project (sbt toolwindow) select "use sbt shell" for all.
1. In folder `$HOME/.RemoteRunPluginIC/sdk/LATEST-EAP-SNAPSHOT/` copy all files from `externalPlugins` to `plugins`. _It is connected with a bug in sbt-idea-plugin_.
1. In Project structure -> Modules -> _idea-runner_ -> sbt: scala-library set Provided.
   **Notice, it is required to set scala library as provided after each updating of sbt project** (it is connected with a bug in Scala plugin).
1. Run `IDEA` run configuration.

### Problems 

**_Cannot download a IDEA._** 
Run `sbt updateIdea` in terminal not in shell. It is connected with bug in sbt plugin and sbt-idea-plugin.

**_After recompiling of a plugin nothing changes._** 
It is connected with sbt shell, build must be with sbt shell, try to click disable and enable _build with sbt shell_ in sbt settings.

**_IDEA handle exception which are connected with ClassLoader or extensions are not work._**
In _Project structure -> Modules -> _idea-runner_ -> sbt: scala-library_ set _Provided_. It is connected with a bug in scala plugin.

### Feedback

_For all questions:_

**Email:** veoring@gmail.com
**Telegram/Slack:** @nashikhmin