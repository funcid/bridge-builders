package me.reidj.bridgebuilders.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import me.reidj.bridgebuilders.user.Stat;
import ru.cristalix.core.network.CorePackage;

import java.util.UUID;

/**
 * @author : Рейдж
 * @project : BridgeBuilders
 **/

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class RejoinPackage extends CorePackage {

    // request
    private final UUID uuid;
    private final Stat stat;

    // no response
}
