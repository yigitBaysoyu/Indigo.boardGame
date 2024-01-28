import view.IndigoApplication

/**
 * Main function of the Program. Everything starts here.
 */
fun main() {
    val indigoApplication = IndigoApplication()
    indigoApplication.show()
    indigoApplication.rootService.networkService.disconnect()
    println("Application ended. Goodbye")
}
