package packages

import ru.cristalix.core.network.CorePackage
import java.util.*

data class ChatPackage( val sender: String, val message: String, val data: Date): CorePackage()
