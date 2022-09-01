package me.reidj.bridgebuilders.data

import java.util.*

/**
 * @project : BridgeBuilders
 * @author : Рейдж
 **/
data class Stat(
    val uuid: UUID,

    var lastRealm: String,

    var ether: Int,
    var kills: Int,
    var wins: Int,
    var games: Int,
    var lootBoxOpened: Int,
    var rewardStreak: Int,

    var donates: MutableSet<String>,
    var walkingEffects: MutableSet<String>,
    var graves: MutableSet<String>,
    var messages: MutableSet<String>,
    var nameTags: MutableSet<String>,
    var startingKits: MutableSet<String>,
    var achievements: MutableSet<String>,
    var lootBoxes: MutableList<LootBoxType>,

    var currentWalkingEffect: String,
    var currentGrave: String,
    var currentMessages: String,
    var currentNameTag: String,
    var currentStarterKit: String,

    var exp: Double,

    var gameLockTime: Long,
    var gameExitTime: Long,
    var dailyClaimTimestamp: Long,
    var lastEnter: Long,

    var isApprovedResourcepack: Boolean,
)
