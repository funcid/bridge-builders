package me.reidj.bridgebuilders.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import ru.cristalix.core.network.CorePackage;
import me.reidj.bridgebuilders.user.Stat;

import java.util.UUID;

/**
 * @author : Рейдж
 * @project : BridgeBuilders
 **/

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class ResetRejoin extends CorePackage {

    // request
    private final UUID uuid;

    // no response
    private Stat stat;
}
