package me.reidj.bridgebuilders.packages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import me.reidj.bridgebuilders.user.Stat;
import ru.cristalix.core.network.CorePackage;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class StatPackage extends CorePackage {

    // request
    private final UUID uuid;

    // response
    private Stat stat;
}
