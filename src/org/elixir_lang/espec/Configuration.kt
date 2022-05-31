package org.elixir_lang.espec

import com.intellij.execution.Executor
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationWithSuppressedDefaultDebugAction
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.RunConfigurationWithSuppressedDefaultRunAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.options.SettingsEditorGroup
import com.intellij.openapi.project.Project
import org.elixir_lang.ESpec
import org.elixir_lang.debugger.Modules
import org.elixir_lang.debugger.configuration.Debuggable
import org.elixir_lang.debugger.settings.stepping.ModuleFilter
import org.elixir_lang.espec.configuration.Factory
import org.elixir_lang.mix.ensureMostSpecificSdk
import org.elixir_lang.run.*
import org.jdom.Element

class Configuration(name: String, project: Project) :
        org.elixir_lang.run.Configuration(name, project, Factory),
        Debuggable<org.elixir_lang.espec.Configuration>,
        RunConfigurationWithSuppressedDefaultRunAction,
        RunConfigurationWithSuppressedDefaultDebugAction {
    override val cookie: String? = null
    override var inheritApplicationModuleFilters: Boolean = true
    override var moduleFilterList: MutableList<ModuleFilter> = mutableListOf()
    override val nodeName: String? = null

    override fun debuggedConfiguration(name: String, cookie: String): Configuration {
        val debugged = Configuration(this.name, project)

        debugged.erlArgumentList.addAll(erlArgumentList)
        debugged.erlArgumentList.addAll(arrayOf("-name", name))
        debugged.erlArgumentList.addAll(arrayOf("-setcookie", cookie))
        debugged.erlArgumentList.addAll(Modules.erlArgumentList(mix = true))

        debugged.elixirArgumentList.addAll(elixirArgumentList)

        debugged.mixArgumentList.addAll(listOf("do", "intellij_elixir.debug,"))

        debugged.mixESpecArgumentList.addAll(mixESpecArgumentList)
        debugged.mixESpecArgumentList.add("--trace")

        debugged.workingDirectory = workingDirectory
        debugged.isPassParentEnvs = isPassParentEnvs

        // Explicit MIX_ENV so that `intellij_elixir.debug` uses the test code paths
        val envs = mutableMapOf<String, String>()
        envs.putAll(this.envs)
        envs.putIfAbsent("MIX_ENV", "test")
        debugged.envs = envs

        debugged.configurationModule.module = configurationModule.module

        return debugged
    }

    override fun getProgramParameters(): String? = mixESpecArguments

    override fun setProgramParameters(value: String?) {
        mixESpecArguments = value
    }

    var erlArgumentList: MutableList<String> = mutableListOf()

    var erlArguments: String?
        get() = erlArgumentList.toArguments()
        set(arguments) = erlArgumentList.fromArguments(arguments)

    var elixirArgumentList: MutableList<String> = mutableListOf()

    var elixirArguments: String?
        get() = elixirArgumentList.toArguments()
        set(arguments) = elixirArgumentList.fromArguments(arguments)

    /**
     * This property only exists so that [Configuration.debuggedConfiguration] can add `do intellij_elixir.debug,`
     * before `espec` task argument in the command line.
     */
    private var mixArgumentList: MutableList<String> = mutableListOf()

    private var mixESpecArguments: String?
        get() = mixESpecArgumentList.toArguments()
        set(arguments) = mixESpecArgumentList.fromArguments(arguments)

    var mixESpecArgumentList: MutableList<String> = mutableListOf()

    fun commandLine(): GeneralCommandLine {
        val workingDirectory = ensureWorkingDirectory()
        val module = ensureModule()
        val sdk = ensureMostSpecificSdk(module)
        val commandLine = ESpec.commandLine(
                envs,
                workingDirectory,
                sdk,
                erlArgumentList,
                elixirArgumentList,
                mixArgumentList
        )
        commandLine.addParameters(mixESpecArgumentList)

        return commandLine
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
            SettingsEditorGroup<Configuration>().apply {
                this.addEditor("Configuration", org.elixir_lang.espec.configuration.Editor())
                this.addEditor("Interpreted Modules", org.elixir_lang.debugger.configuration.interpreted_modules.Editor<Configuration>())
            }

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState =
            State(environment, this)

    override fun readExternal(element: Element) {
        super.readExternal(element)
        element.readExternalArgumentList(ERL, erlArgumentList)
        element.readExternalArgumentList(ELIXIR, elixirArgumentList)
        element.readExternalArgumentList(MIX_ESPEC, mixESpecArgumentList)
        workingDirectoryURL = element.readExternalWorkingDirectory()
        EnvironmentVariablesComponent.readExternal(element, envs)
        element.readExternalModule(this)
        element.readModuleFilters(moduleFilterList) { inheritApplicationModuleFilters ->
            this.inheritApplicationModuleFilters = inheritApplicationModuleFilters
        }
    }

    override fun writeExternal(element: Element) {
        super.writeExternal(element)
        element.writeExternalArgumentList(ERL, erlArgumentList)
        element.writeExternalArgumentList(ELIXIR, elixirArgumentList)
        element.writeExternalArgumentList(MIX_ESPEC, mixESpecArgumentList)
        element.writeExternalWorkingDirectory(workingDirectoryURL)
        EnvironmentVariablesComponent.writeExternal(element, envs)
        element.writeExternalModule(this)
        element.writeModuleFilters(moduleFilterList, inheritApplicationModuleFilters)
    }
}

const val MIX_ESPEC = "mix-espec"
