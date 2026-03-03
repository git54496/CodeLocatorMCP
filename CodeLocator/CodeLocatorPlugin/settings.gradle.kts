rootProject.name = "CodeLocatorPlugin"

include(":CodeLocatorModel")

if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) {
    project(":CodeLocatorModel").projectDir = File("..\\CodeLocatorApp\\CodeLocatorModel")
} else {
    project(":CodeLocatorModel").projectDir = File("../CodeLocatorApp/CodeLocatorModel")
}
