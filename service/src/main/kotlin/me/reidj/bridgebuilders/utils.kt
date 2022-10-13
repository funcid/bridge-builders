package me.reidj.bridgebuilders

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.cristalix.core.network.Capability
import ru.cristalix.core.network.CorePackage
import ru.cristalix.core.network.ISocketClient
import ru.cristalix.core.realm.RealmId
import kotlin.reflect.KClass

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/

fun ISocketClient.capabilities(vararg classes: KClass<out CorePackage>) = registerCapabilities(*classes.map {
    Capability.builder()
        .className(it.java.name)
        .notification(true)
        .build()
}.toTypedArray())

@JvmSynthetic
inline fun <reified T : CorePackage> ISocketClient.listen(
    crossinline handler: suspend ISocketClient.(RealmId, T) -> Unit
): Unit = addListener(T::class.java) { realmId, pckg ->
    CoroutineScope(Dispatchers.Default).launch { handler(realmId, pckg) }
}