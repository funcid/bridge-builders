package me.reidj.bridgebuilders.user;

import me.reidj.bridgebuilders.data.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import me.reidj.bridgebuilders.data.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Stat implements Unique {
    private UUID uuid;

    private String realm;

    private int money;
    private int kills;
    private int wins;
    private int games;
    private int lootbox;
    private int lootboxOpenned;
    private Integer gameLockTime;
    private Integer gameExitTime;

    private List<Donate> donate;
    private Set<String> donates;
    private List<Achievement> achievement;

    private KillMessage activeKillMessage;
    private StepParticle activeParticle;
    private NameTag activeNameTag;
    private Corpse activeCorpse;
    private StarterKit activeKit;

    private Double banTime;
    private Double leaveTime;
    private Long timePlayedTotal;

    private Double dailyTimestamp;
    private Double lastEnterTime;
    private Long dailyClaimTimestamp;
    private Long lastEnter;
    private Integer rewardStreak;

    private Boolean isApprovedResourcepack;
    private Boolean isBan;
}