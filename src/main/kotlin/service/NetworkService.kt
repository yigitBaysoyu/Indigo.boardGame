package service

/**
 * Service layer class that realizes the necessary logic for sending and receiving messages
 * in multiplayer network games.
 */
class NetworkService (private  val rootService: RootService) : AbstractRefreshingService()