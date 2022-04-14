package packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * @author Рейдж 03.10.2021
 * @project ThePit
 */

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class GreetingPackage extends BridgePackage {

    // request
    private final String password;
    private final String serverName;

    // no response
}
