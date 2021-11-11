package me.reidj.bridgebuilders.mod

object ModHelper {

    fun sendTitle(user: me.reidj.bridgebuilders.user.User, text: String) {
        ModTransfer()
            .string(text)
            .send("func:title", user)
    }
}