import com.serena.air.plugin.pvcs.PVCSHelper
import com.serena.air.plugin.pvcs.model.file
import com.urbancode.air.AirPluginTool
import groovy.xml.XmlUtil

final def apTool = new AirPluginTool(this.args[0], this.args[1])
final def props = apTool.getStepProperties()
def pcliPath       = props['pcliPath']?.trim()
def databasePath   = props['databasePath']?.trim()
def projectPath    = props['projectPath']?.trim()
def label          = props['label']?.trim()
def user           = props['user']?.trim()
def password       = props['password']?.trim()
def outputFile     = props['outputFile']?.trim()
def options        = props['options']?.trim()
def entityPath     = props['entityPath']?.trim()
def debug          = props['debug']?.trim().toBoolean()

PVCSHelper helper = new PVCSHelper(new File(".").canonicalFile,
        (pcliPath == null) || pcliPath.trim().isEmpty() ? "pcli" : pcliPath)

def args = helper.setOptions("ListRevision", user, password, databasePath, projectPath, options, debug)
args << "-f"
args << "-z"
if (label != null && label.trim().length() > 0) {
    args << new String("-v" + label.trim())
}
args << entityPath

if (helper.runCommand("Retrieving versions or label ${label}", args)) {
    def files = []
    BufferedReader bufReader = new BufferedReader(new StringReader(helper.getOutput()))
    while ((line=bufReader.readLine()) != null) {
        def arr = line.replace("${label}:=",'').split("\\s+")
        files.add(new file(
                name: arr[0],
                type: arr[0].substring(arr[0].lastIndexOf('.')+1, arr[0].length()),
                version: arr[1]))
    }

    def builder = new groovy.xml.StreamingMarkupBuilder()
    builder.encoding = 'UTF-8'

    def releaseNote = builder.bind {
        mkp.xmlDeclaration()
        mkp.yield "\r\n"
        release (
                name: "",
                description: "",
                system: "",
                subsystem: "",
                date: "",
                version: label,
        ) {
            deployment() {
                files.each() { obj ->
                    file(
                            "copy": obj.copy,
                            "destination": obj.destination,
                            "source": obj.source,
                            "version": obj.version,
                            "type": obj.type,
                            "name": obj.name
                    )
                }
            }
            rollback()
        }
    }

    XmlUtil.serialize(releaseNote, new File(outputFile).newPrintWriter("UTF-8"))

    println("Created XML release file ${outputFile}...")
    System.exit(0)
} else {
    System.exit(1)
}


