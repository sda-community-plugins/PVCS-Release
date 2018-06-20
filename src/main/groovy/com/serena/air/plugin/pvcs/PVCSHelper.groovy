package com.serena.air.plugin.pvcs

import com.urbancode.air.CommandHelper
import com.urbancode.air.ExitCodeException

class PVCSHelper {
    String home
    File workdir
    boolean debug = false
    private String output
    private CommandHelper ch

    /**
     * Default constructor
     * @param workdir The working directory for the CommandHelper CLI
     * @param home The full path to the "pcli" script location
     */
    PVCSHelper(File workdir, String home) {
        if (home) {
            this.home = home
        } else {
            this.home = "pcli"
        }
        if (workdir) {
            this.workdir = workdir
        }
        ch = new CommandHelper(workdir)
    }

    String getHome() {
        return this.home
    }

    def setHome(String home) {
        this.home = home
    }

    String getOutput() {
        return this.output
    }

    def setOutput(String output) {
        this.output = output
    }

    boolean getDebug() {
        return this.debug
    }

    def setDebug(boolean debug) {
        this.debug = debug
    }

    /**
     * @param args An ArrayList of arguments to be executed by the command prompt
     * @return true if the command is run without any Standard Errors, false otherwise
     */
    public boolean runCommand(def message, def args) {
        def cmd = [home]
        args.each() { arg ->
            cmd << arg
        }
        boolean status
        debug("Executing: " + cmd.toString() + " " + args.toString())
        try {
            ch.runCommand("[INFO] ${message}", cmd) { Process proc ->
                def (String out, String err) = captureCommand(proc)
                setOutput(out)
                if (err) {
                    info("Error output:\n${err}")
                    status = false
                }
                if (out) {
                    info("Command output:\n${out}")
                    status = true
                }
            }
        } catch (ExitCodeException ex) {
            error(ex.toString())
            return false
        }
        return status
    }

    /**
     * @param args The list of arguments to add the configuration options to
     * @param command The PVCS CLI command to invoke
     * @param user The user to connect as
     * @param password The password for the user
     * @param databasePath The project database path
     * @param projectPath The project path
     * @param options Additional options
     * @param debug Enable debug logging
     */
    def setOptions(String command, String user, String password, String databasePath, String projectPath, String options, Boolean debug) {
        def args = []
        if (!isEmpty(command)) {
            args << "${command}"
        }
        def id = null
        if (user != null && user.trim().length() > 0) {
            if (password != null && password.trim().length() > 0) {
                id = user.trim() + ":" + password.trim()
            }
            else {
                id = user.trim()
            }
        }
        if (id != null) {
            args <<  "-id${id}"
        }
        if (!isEmpty(databasePath)) {
            args <<  "-pr${databasePath}"
        }
        if (!isEmpty(projectPath)) {
            args << "-pp${projectPath}"
        }
        if (!isEmpty(options)) {
            options.split("[\r\n]+").each() { option ->
                args << "${option}"
            }
        }
        if (debug) {
            setDebug(true)
        }
        return args
    }

    /**
     * Check if a string is null, empty, or all whitespace
     * @param str The string whose value to check
     */
    public boolean isEmpty(String str) {
        return (str == null) || str.trim().isEmpty();
    }

    // ----------------------------------------

    /**
     * @param proc The process to retrieve the standard output and standard error from
     * @return An array containing the standard output and standard error of the process
     */
    private String[] captureCommand(Process proc) {
        StringBuffer out = new StringBuffer()
        StringBuffer err = new StringBuffer()
        proc.waitForProcessOutput(out, err)
        proc.out.close()
        return [out.toString(), err.toString()]
    }

    private debug(String message) {
        if (this.debug) {
            println("[DEBUG] ${message}")
        }
    }

    private info(String message) {
        println("[INFO] ${message}")
    }

    private error(String message) {
        println("[ERROR] ${message}")
    }
}
