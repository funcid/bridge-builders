package me.reidj.common.util

import com.google.gson.Gson
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import me.reidj.common.`package`.BridgePackage
import me.reidj.common.`package`.PackageWrapper

object UtilNetty {

    private val gson = Gson()

    fun toFrame(thePitPackage: BridgePackage): TextWebSocketFrame? {
        return TextWebSocketFrame(
            gson.toJson(
                PackageWrapper(
                    thePitPackage::class.java.name,
                    gson.toJson(thePitPackage)
                )
            )
        )
    }

    fun readFrame(textFrame: TextWebSocketFrame): BridgePackage {
        val wrapper: PackageWrapper = gson.fromJson(textFrame.text(), PackageWrapper::class.java)
        return gson.fromJson(wrapper.objectData, Class.forName(wrapper.clazz)) as BridgePackage
    }
}