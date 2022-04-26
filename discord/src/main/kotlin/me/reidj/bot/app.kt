package me.reidj.bot

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.rest.service.RestClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import packages.ChatPackage
import ru.cristalix.core.microservice.MicroServicePlatform
import ru.cristalix.core.microservice.MicroserviceBootstrap
import ru.cristalix.core.network.Capability
import ru.cristalix.core.network.ISocketClient

private const val TOKEN = "Nzk5NzI5MjE2NTMwNjc3ODEx.YAHz3w._ExUOfYSUf9jvjK26Q-SfBk1db4"

suspend fun main() {
    val kord = Kord(TOKEN)
    val rest = RestClient(TOKEN)

    MicroserviceBootstrap.bootstrap(MicroServicePlatform(2))

    val client = ISocketClient.get()

    client.registerCapabilities(
        Capability.builder()
            .className(ChatPackage::class.java.name)
            .notification(true)
            .build()
    )

    val scope = CoroutineScope(Dispatchers.IO)

    client.addListener(ChatPackage::class.java) { realm, pckg ->
        scope.launch {
            rest.channel.createMessage(Snowflake(968266478363213834)) {
                content = "${pckg.data} | ${realm.realmName} | ${pckg.sender} | ${pckg.message}"
            }
        }
    }

    kord.login()
}