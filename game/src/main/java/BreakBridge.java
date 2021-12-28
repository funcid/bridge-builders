import kotlin.Pair;
import me.reidj.bridgebuilders.AppKt;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import ru.cristalix.core.formatting.Color;

public class BreakBridge {

    public BreakBridge() {
        AppKt.getTeams().forEach(it -> {
            double blockX = it.getBridge().get(0).x;
            double blockZ = it.getBridge().get(1).z;

            for (double y = 77; y < 102; y++) {
                if (it.getColor().equals(Color.RED)) {
                    for (double x = blockX; x > blockX - 16; x--) {
                        for (double z = blockZ; z > blockZ - 84; z--) {
                            Block block = new Location(AppKt.getMap().getWorld(), x, y, z).getBlock();
                            it.getBlocksConstruction().putIfAbsent(block.getLocation(), new Pair<>(block.getTypeId(), block.getData()));
                            block.setType(Material.AIR);
                        }
                    }
                } else if (it.getColor().equals(Color.GREEN)) {
                    for (double x = blockX; x < blockX + 16; x++) {
                        for (double z = blockZ; z < blockZ + 84; z++) {
                            Block block = new Location(AppKt.getMap().getWorld(), x, y, z).getBlock();
                            it.getBlocksConstruction().putIfAbsent(block.getLocation(), new Pair<>(block.getTypeId(), block.getData()));
                            block.setType(Material.AIR);
                        }
                    }
                } else if (it.getColor().equals(Color.YELLOW)) {
                    for (double x = blockX; x < blockX + 84; x++) {
                        for (double z = blockZ; z > blockZ - 16; z--) {
                            Block block = new Location(AppKt.getMap().getWorld(), x, y, z).getBlock();
                            it.getBlocksConstruction().putIfAbsent(block.getLocation(), new Pair<>(block.getTypeId(), block.getData()));
                            block.setType(Material.AIR);
                        }
                    }
                } else if (it.getColor().equals(Color.BLUE)) {
                    for (double x = blockX; x > blockX - 84; x--) {
                        for (double z = blockZ; z < blockZ + 16; z++) {
                            Block block = new Location(AppKt.getMap().getWorld(), x, y, z).getBlock();
                            it.getBlocksConstruction().putIfAbsent(block.getLocation(), new Pair<>(block.getTypeId(), block.getData()));
                            block.setType(Material.AIR);
                        }
                    }
                }
            }
        });
    }
}
