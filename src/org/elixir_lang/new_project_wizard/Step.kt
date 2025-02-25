package org.elixir_lang.new_project_wizard

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.ide.wizard.AbstractNewProjectWizardStep
import com.intellij.ide.wizard.NewProjectWizardBaseData
import com.intellij.ide.wizard.NewProjectWizardLanguageStep
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ui.configuration.sdkComboBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import kotlinx.coroutines.CancellationException
import org.elixir_lang.Mix
import org.elixir_lang.module.ElixirModuleBuilder
import org.elixir_lang.module.ElixirModuleType
import org.elixir_lang.sdk.elixir.Type
import java.io.IOException
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

// Based on [NewPythonProjectStep](https://github.com/JetBrains/intellij-community/blob/dcb0ce2edd2c3b1dffb7e60103898acd5b913cfb/python/src/com/jetbrains/python/newProject/PythonNewProjectWizard.kt#L82-L145)
class Step(parent: NewProjectWizardLanguageStep) : AbstractNewProjectWizardStep(parent),
                                                   NewProjectWizardBaseData by parent,
                                                   Data {
    override val sdkProperty = propertyGraph.property<Sdk?>(null)
    override val mixNewAppProperty = propertyGraph.property<String>("")
    override val mixNewModuleProperty = propertyGraph.property<String>("")
    override val mixNewSupProperty = propertyGraph.property(false)
    override val mixNewUmbrellaProperty = propertyGraph.property(false)

    override var sdk by sdkProperty
    override var mixNewApp by mixNewAppProperty
    override var mixNewModule by mixNewModuleProperty
    override var mixNewSup by mixNewSupProperty
    override var mixNewUmbrella by mixNewUmbrellaProperty

    // Based on [IntelliJNewProjectWizardStep.setupUI](https://github.com/JetBrains/intellij-community/blob/dcb0ce2edd2c3b1dffb7e60103898acd5b913cfb/java/idea-ui/src/com/intellij/ide/projectWizard/generators/IntelliJNewProjectWizardStep.kt#L77-L124)
    override fun setupUI(builder: Panel) {
        with(builder) {
            row("Elixir SDK:") {
                val sdkTypeFilter = { it: SdkTypeId -> it == Type.instance; }
                sdkComboBox(context, sdkProperty, ElixirModuleType.MODULE_TYPE_ID, sdkTypeFilter)
                    .columns(COLUMNS_MEDIUM)
            }
            collapsibleGroup("mix new Options") {
                row("--app") {
                    textField()
                        .bindText(mixNewAppProperty)
                        .horizontalAlign(HorizontalAlign.FILL)
                }.bottomGap(BottomGap.SMALL)
                row("--module") {
                    textField()
                        .bindText(mixNewModuleProperty)
                        .horizontalAlign(HorizontalAlign.FILL)
                }.bottomGap(BottomGap.SMALL)
                row {
                    checkBox("--sup")
                        .bindSelected(mixNewSupProperty)
                }.bottomGap(BottomGap.SMALL)
                row {
                    checkBox("--umbrella")
                        .bindSelected(mixNewUmbrellaProperty)
                }
            }
        }
    }

    override fun setupProject(project: Project) {
        try {
            val sdk = this.sdk!!
            val workingDirectory = Paths.get(".").toAbsolutePath().normalize().toString()
            val projectDirectory = context.projectDirectory.toString()

            val commandLine = Mix.commandLine(emptyMap(), workingDirectory, sdk)
            commandLine.addParameters("new", projectDirectory)

            if (mixNewApp.isNotBlank()) {
                commandLine.addParameters("--app", mixNewApp)
            }

            if (mixNewModule.isNotBlank()) {
                commandLine.addParameters("--module", mixNewModule)
            }

            if (mixNewSup) {
                commandLine.addParameter("--sup")
            }

            if (mixNewUmbrella) {
                commandLine.addParameter("--umbrella")
            }

            // delete the caller's created empty directory so that `mix new` can create it.
            if (!context.projectDirectory.toFile().deleteRecursively()) {
                throw IOException("Could not delete ${context.projectDirectory}")
            }

            val processOutput = ProgressManager
                .getInstance()
                .run(object : Task.WithResult<ProcessOutput, ExecutionException>(
                    project,
                    "mix new $projectDirectory",
                    true
                ) {
                    @Throws(ExecutionException::class)
                    override fun compute(indicator: ProgressIndicator): ProcessOutput {
                        indicator.isIndeterminate = true

                        return ExecUtil.execAndGetOutput(commandLine, TimeUnit.SECONDS.toMillis(30).toInt())
                    }
                })

            if (processOutput.isCancelled) {
                throw CancellationException()
            } else if (processOutput.isTimeout) {
                throw TimeoutException()
            } else if (processOutput.exitCode != 0) {
                NotificationGroupManager
                    .getInstance()
                    .getNotificationGroup("Elixir")
                    .createNotification("mix new failed", processOutput.stderr, NotificationType.ERROR)
                    .notify(project)

                throw IOException()
            }

            super.setupProject(project)

            val builder = ElixirModuleBuilder()

            // Based on https://github.com/JetBrains/intellij-community/blob/8c0419e906efe686479832543092f4918e6516bd/java/idea-ui/src/com/intellij/ide/projectWizard/generators/IntelliJJavaNewProjectWizard.kt#L40-L48
            if (context.isCreatingNewProject) {
                context.projectJdk = sdk
            } else {
                builder.moduleJdk = sdk.takeIf { ProjectRootManager.getInstance(project).projectSdk?.name != it.name }
            }

            builder.addSourcePath(
                com.intellij.openapi.util.Pair(
                    Paths.get(projectDirectory, "lib").toString(),
                    ""
                )
            )

            builder.setCompilerOutputPath(
                Paths.get(
                    context.projectDirectory.toString(),
                    "_build",
                    "dev",
                    "lib",
                    mixNewApp,
                    "ebin"
                ).toString()
            )

            builder.commit(project)
        } catch (ioException: IOException) {
            Logger.getInstance(javaClass).error(ioException)

            throw ioException
        }
    }
}
