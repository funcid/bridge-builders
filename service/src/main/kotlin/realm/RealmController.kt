package realm

import ru.cristalix.core.CoreApi
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.network.packages.AllRealmsPackage
import ru.cristalix.core.realm.RealmInfo
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.stream.Stream

class RealmController {

    private var realms: List<RealmInfo> = ArrayList()

    init {
        CoreApi.get().platform.scheduler.runAsyncRepeating({
            try {
                realms = fetchBridgeRealms()[5L, TimeUnit.SECONDS]
            } catch (ex: Exception) {
                ex.printStackTrace()
                println("AllRealmsPackage timeout")
            }
        }, 5L, TimeUnit.SECONDS)
    }

    fun bestRealm(): Optional<RealmInfo> {
        return realms
            .stream()
            .filter { info: RealmInfo -> info.maxPlayers - info.currentPlayers > 0 }
            .min(Comparator.comparingInt { obj: RealmInfo -> obj.currentPlayers })
    }

    private fun fetchBridgeRealms(): CompletableFuture<List<RealmInfo>> {
        return ISocketClient.get().writeAndAwaitResponse<AllRealmsPackage>(AllRealmsPackage())
            .thenApply { pckg: AllRealmsPackage ->
                Stream.of(*pckg.realms).filter { info: RealmInfo ->
                    info.realmId.typeName == "BRI"
                }
                    .collect(Collectors.toList())
            }
    }
}