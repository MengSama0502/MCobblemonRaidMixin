package MengSama.Mod.mcobbleraidmixinfabric;

import com.necro.raid.dens.common.data.raid.RaidBoss;
import com.necro.raid.dens.common.events.RaidJoinEvent;
import com.necro.raid.dens.common.events.RaidEvents;
import com.cobblemon.mod.common.api.Priority;
import net.minecraft.server.level.ServerPlayer;
import kotlin.Unit;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class RaidParticipantTracker {
    private static final Map<RaidBoss, Set<ServerPlayer>> RAID_PARTICIPANTS = new ConcurrentHashMap<>();

    static {
        RaidEvents.RAID_JOIN.subscribe(Priority.NORMAL, (RaidJoinEvent event) -> {
            RaidBoss raidBoss = event.getRaidBoss();
            ServerPlayer player = event.getPlayer();
            if (raidBoss != null && player != null) {
                RAID_PARTICIPANTS.computeIfAbsent(raidBoss, k -> new CopyOnWriteArraySet<>()).add(player);
            }
            return Unit.INSTANCE;
        });
    }

    public static Set<ServerPlayer> getParticipants(RaidBoss raidBoss) {
        Set<ServerPlayer> set = RAID_PARTICIPANTS.get(raidBoss);
        return set == null ? Set.of() : set;
    }

    public static void clearParticipants(RaidBoss raidBoss) {
        RAID_PARTICIPANTS.remove(raidBoss);
    }
}