import com.serena.air.plugin.pvcs.PVCSHelper
import com.urbancode.air.AirPluginTool


final def apTool = new AirPluginTool(this.args[0], this.args[1])
final def props = apTool.getStepProperties()
def pcliPath       = props['pcliPath']?.trim()
def pcliCommand    = props['pcliCommand']?.trim()
def databasePath   = props['databasePath']?.trim()
def projectPath    = props['projectPath']?.trim()
def user           = props['user']?.trim()
def password       = props['password']?.trim()
def options        = props['options']?.trim()
def entityPath     = props['entityPath']?.trim()
def outputFile     = props['outputFile']?.trim()
def debug          = props['debug']?.trim().toBoolean()

PVCSHelper helper = new PVCSHelper(new File(".").canonicalFile,
        (pcliPath == null) || pcliPath.trim().isEmpty() ? "pcli" : pcliPath)

def args = helper.setOptions(pcliCommand, user, password, databasePath, projectPath, options, debug)
if (!helper.isEmpty(entityPath)) {
    args << entityPath
}

if (helper.runCommand("Executing PCLI ${pcliCommand} command", args)) {
    helper.getOutput()
    if (!helper.isEmpty(outputFile)) {
        new File(outputFile).write(helper.getOutput())
        println("Created output file ${outputFile}...")
    }
    System.exit(0)
} else {
    System.exit(1)
}


