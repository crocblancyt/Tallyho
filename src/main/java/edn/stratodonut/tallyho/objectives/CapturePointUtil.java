package edn.stratodonut.tallyho.objectives;

import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.TallyhoMod;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class CapturePointUtil {
    public static final String OBJECTIVE_NAME = "Capture1";
    public static final String CAPTUREPOINT_MAX_NAME = "maxCap";
    public static final String CAPTUREPOINT_TOTAL_NAME = "#total";
    public static final String TICKETS_NAME = "ticketsP2";
    public static final String TITLE_BAR_NAME = "capture_bar";
    public static final int CAPTURE_TICKS = 600;

    private static final HashMap<String, CapturePoint> captureProgress = new HashMap<>();

    public static class CapturePoint {
        public int progress;
        public boolean lock = false;
        @Nonnull
        public Occupation occupation = new Occupation(0, 0);

        public record Occupation(int red, int blue) {
            public static final Occupation EMPTY = new Occupation(0, 0);

            public static Occupation sum(Occupation a, Occupation b) {
                return new Occupation(a.red() + b.red(), a.blue() + b.blue());
            }

            public int delta() {
                        return Integer.compare(this.blue, this.red);
                    }
        }
    }

    public static void changeTicketScore(@Nonnull ServerLevel level, Team team, boolean other, int delta) {
        ServerScoreboard board = level.getScoreboard();
        Objective o = board.getObjective("ticketsP2");
        if (o == null)
            return;
        for (Score score : board.getPlayerScores(o)) {
            if (score.getScore() <= 0)
                continue;

            if (score.getOwner().equals(team.getName())) {
                if (!other)
                    score.add(delta);
                continue;
            }
            if (other)
                score.add(delta);
        }
    }

    @Nonnull
    public static Score getCaptureScore(@Nonnull ServerLevel level, String capturePointName) {
        ServerScoreboard board = level.getScoreboard();
        if (!board.hasObjective("Capture1"))
            board.addObjective("Capture1", ObjectiveCriteria.DUMMY, (Component)Components.literal("cap"), ObjectiveCriteria.RenderType.INTEGER);
        Objective o = board.getObjective("Capture1");
        return board.getOrCreatePlayerScore(capturePointName, o);
    }

    public static void incCaptureScoreValue(@Nonnull ServerLevel level, String capturePointName, int delta) {
        getCaptureScore(level, capturePointName).add(delta);
    }

    public static void setCaptureScoreValue(@Nonnull ServerLevel level, String capturePointName, int value) {
        getCaptureScore(level, capturePointName).setScore(value);
    }

    public static void updateTotalScores(@Nonnull ServerLevel level) {
        ServerScoreboard board = level.getScoreboard();
        if (!board.hasObjective("Capture1"))
            board.addObjective("Capture1", ObjectiveCriteria.DUMMY, (Component)Components.literal("cap"), ObjectiveCriteria.RenderType.INTEGER);
        Objective o = board.getObjective("Capture1");
        Collection<Score> scores = board.getPlayerScores(o);
        Optional<Integer> total = scores.stream().map(score -> !score.getOwner().equals("#total") ? score.getScore() : 0).reduce(Integer::sum);
        total.ifPresent(t -> setCaptureScoreValue(level, "#total", t));
    }

    @Nonnull
    public static CustomBossEvent getOrCreateBossEvent(@Nonnull MinecraftServer server, String id) {
        id = id.toLowerCase();
        CustomBossEvents be = server.getCustomBossEvents();
        CustomBossEvent b = be.get(TallyhoMod.asResource(id));
        if (b == null)
            b = be.create(TallyhoMod.asResource(id), (Component)Components.literal("loading..."));
        return b;
    }

    @Nonnull
    public static CustomBossEvent updateCapturePoint(@Nonnull MinecraftServer server, String name, int score, @Nullable CapturePoint.Occupation occupation, boolean locked) {
        if (!captureProgress.containsKey(name))
            captureProgress.put(name, new CapturePoint());
        CapturePoint cp = captureProgress.get(name);
        cp.progress = score;
        cp.lock = locked;
        if (occupation != null)
            cp.occupation = occupation;
        if (occupation == null)
            occupation = CapturePoint.Occupation.EMPTY;
        int cap = Math.round(score / 600.0F);
        CustomBossEvent b = getOrCreateBossEvent(server, name);
        String status = locked ? "POINT IS LOCKED DOWN! " : ((occupation.red == 0 || occupation.blue == 0) ? "CAPTURING " : "CONTESTING ");
        MutableComponent text = Component.literal(status + " ").setStyle(Style.EMPTY.withItalic(true)).append(Component.literal(">".repeat((int)Math.ceil((occupation.blue / 3.0F)))).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE))).append(Component.literal(" " + name + " ").setStyle(Style.EMPTY.withColor((cap == 0) ? ChatFormatting.WHITE : ((cap == 1) ? ChatFormatting.BLUE : ChatFormatting.RED)))).append(Component.literal("<".repeat((int)Math.ceil((occupation.red / 3.0F)))).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        b.setName((Component)text);
        b.setMax(1200);
        b.setValue(score + 600);
        b.setColor((score < 0) ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.BLUE);
        return b;
    }

    public static void removeCapturePoint(String name) {
        captureProgress.remove(name);
    }

    public static void updateCaptureStatusTitle(@Nonnull MinecraftServer server) {
        CustomBossEvent b = getOrCreateBossEvent(server, "capture_bar");
        MutableComponent text = Component.literal("");
        int capture_potential_total = 0;
        int capture_progress_sum = 0;
        if (captureProgress.isEmpty())
            return;
        for (Map.Entry<String, CapturePoint> entry : captureProgress.entrySet()) {
            CapturePoint cp = entry.getValue();
            int cap = Math.round(cp.progress / 600.0F);
            capture_potential_total += 30;
            capture_progress_sum += cp.progress / 20;
            appendCaptureText(text, entry.getKey(), cp.occupation, cap, cp.lock);
            text.append(Component.literal(" | "));
        }
        b.setName((Component)text);
        if (b.getMax() != capture_potential_total * 2)
            b.setMax(capture_potential_total * 2);
        if (b.getValue() != capture_progress_sum + capture_potential_total)
            b.setValue(capture_progress_sum + capture_potential_total);
    }

    private static void appendCaptureText(MutableComponent text, String name, CapturePoint.Occupation occ, int progress, boolean locked) {
        ChatFormatting blueColor = locked ? ChatFormatting.BLACK : ChatFormatting.BLUE;
        ChatFormatting redColor = locked ? ChatFormatting.BLACK : ChatFormatting.RED;
        ChatFormatting nameColor = locked ? ChatFormatting.GRAY : ChatFormatting.WHITE;
        if (occ.blue > 0 || locked)
            text.append(Component.literal(">").setStyle(Style.EMPTY.withColor(blueColor)));
        text.append(Component.literal(" " + name + " ").setStyle(Style.EMPTY.withColor((progress == 0) ? nameColor : ((progress == 1) ? blueColor : redColor))));
        if (occ.red > 0 || locked)
            text.append(Component.literal("<").setStyle(Style.EMPTY.withColor(redColor)));
    }
}