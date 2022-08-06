package me.reidj.bridgebuilders.packages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import ru.cristalix.core.network.CorePackage;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class UserRequestJoinPackage extends CorePackage {

    // request
    private UUID user;

    // response
    private boolean passed;

}
