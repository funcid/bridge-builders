package packages

import ru.cristalix.core.network.CorePackage

data class ChatPackage( val sender: String, val message: String, val data: String?): CorePackage()
