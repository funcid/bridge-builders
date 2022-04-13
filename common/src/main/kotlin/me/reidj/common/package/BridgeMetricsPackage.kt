package me.reidj.common.`package`

data class BridgeMetricsPackage(
    val serverName: String,
    val online: Int,
    val tps: Double,
    val freeMemory: Long,
    val allocatedMemory: Long,
    val totalMemory: Long,
    val metrics: Map<String, PacketMetric>
): BridgePackage() {
    data class PacketMetric(
        var received: Long,
        var receivedBytes: Long,
        var sent: Long,
        var sentBytes: Long,
        var decompressedBytes: Long,
        var compressedBytes: Long

    ) : java.io.Serializable, Cloneable {
        public override fun clone(): PacketMetric {
            return PacketMetric(
                received,
                receivedBytes,
                sent,
                sentBytes,
                decompressedBytes,
                compressedBytes
            )
        }
    }
}
