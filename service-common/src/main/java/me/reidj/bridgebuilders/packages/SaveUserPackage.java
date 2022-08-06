package me.reidj.bridgebuilders.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import me.reidj.bridgebuilders.user.Stat;
import ru.cristalix.core.network.CorePackage;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class SaveUserPackage extends CorePackage {

    // request
    private final UUID user;
    private final Stat userInfo;

    // no response

}
