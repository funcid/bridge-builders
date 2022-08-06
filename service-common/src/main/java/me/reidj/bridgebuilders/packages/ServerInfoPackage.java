package me.reidj.bridgebuilders.packages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import ru.cristalix.core.network.CorePackage;
import me.reidj.bridgebuilders.user.Stat;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class ServerInfoPackage extends CorePackage {

    // request
    private final UUID uuid;

    // response
    private Stat stat;
}
