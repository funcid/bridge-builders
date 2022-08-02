package packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import tops.PlayerTopEntry;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class TopPackage extends BridgePackage {

    // request
    private final TopType topType;
    private final int limit;

    // response
    private List<PlayerTopEntry<Object>> entries;

    public enum TopType {

        WINS,
        GAMES
        ;

    }

}