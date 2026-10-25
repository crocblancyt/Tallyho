package edn.stratodonut.tallyho.objectives;

import com.simibubi.create.foundation.utility.Components;
import edn.stratodonut.tallyho.TallyhoMod;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map.Entry;
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
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;

public class CapturePointUtil {
   public static final String OBJECTIVE_NAME = "Capture1";
   public static final String CAPTUREPOINT_MAX_NAME = "maxCap";
   public static final String CAPTUREPOINT_TOTAL_NAME = "#total";
   public static final String TICKETS_NAME = "ticketsP2";
   public static final String TITLE_BAR_NAME = "capture_bar";
   public static final int CAPTURE_TICKS = 600;
   private static final HashMap<String, CapturePointUtil.CapturePoint> captureProgress = new HashMap<>();

   public static void changeTicketScore(@Nonnull ServerLevel level, Team team, boolean other, int delta) {
      ServerScoreboard board = level.getScoreboard();
      Objective o = board.getObjective("ticketsP2");
      if (o != null) {
         for (Score score : board.getPlayerScores(o)) {
            if (score.getScore() > 0) {
               if (score.getOwner().equals(team.getName())) {
                  if (!other) {
                     score.add(delta);
                  }
               } else if (other) {
                  score.add(delta);
               }
            }
         }
      }
   }

   @Nonnull
   public static Score getCaptureScore(@Nonnull ServerLevel level, String capturePointName) {
      ServerScoreboard board = level.getScoreboard();
      if (!board.hasObjective("Capture1")) {
         board.addObjective("Capture1", ObjectiveCriteria.DUMMY, Components.literal("cap"), RenderType.INTEGER);
      }

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
      if (!board.hasObjective("Capture1")) {
         board.addObjective("Capture1", ObjectiveCriteria.DUMMY, Components.literal("cap"), RenderType.INTEGER);
      }

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
      if (b == null) {
         b = be.create(TallyhoMod.asResource(id), Components.literal("loading..."));
      }

      return b;
   }

   @Nonnull
   public static CustomBossEvent updateCapturePoint(
      @Nonnull MinecraftServer server, String name, int score, @Nullable CapturePointUtil.CapturePoint.Occupation occupation, boolean locked
   ) {
      if (!captureProgress.containsKey(name)) {
         captureProgress.put(name, new CapturePointUtil.CapturePoint());
      }

      CapturePointUtil.CapturePoint cp = captureProgress.get(name);
      cp.progress = score;
      cp.lock = locked;
      if (occupation != null) {
         cp.occupation = occupation;
      }

      if (occupation == null) {
         occupation = CapturePointUtil.CapturePoint.Occupation.EMPTY;
      }

      int cap = Math.round(score / 600.0F);
      CustomBossEvent b = getOrCreateBossEvent(server, name);
      String status = locked ? "POINT IS LOCKED DOWN! " : (occupation.red != 0 && occupation.blue != 0 ? "CONTESTING " : "CAPTURING ");
      MutableComponent text = Component.literal(status + " ")
         .withStyle(Style.EMPTY.withItalic(true))
         .append(Component.literal(">".repeat((int)Math.ceil(occupation.blue / 3.0F))).setStyle(Style.EMPTY.withColor(ChatFormatting.BLUE)))
         .append(
            Component.literal(" " + name + " ")
               .setStyle(Style.EMPTY.withColor(cap == 0 ? ChatFormatting.WHITE : (cap == 1 ? ChatFormatting.BLUE : ChatFormatting.RED)))
         )
         .append(Component.literal("<".repeat((int)Math.ceil(occupation.red / 3.0F))).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
      b.setName(text);
      b.setMax(1200);
      b.setValue(score + 600);
      b.setColor(score < 0 ? BossBarColor.RED : BossBarColor.BLUE);
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
      if (!captureProgress.isEmpty()) {
         for (Entry<String, CapturePointUtil.CapturePoint> entry : captureProgress.entrySet()) {
            CapturePointUtil.CapturePoint cp = entry.getValue();
            int cap = Math.round(cp.progress / 600.0F);
            capture_potential_total += 30;
            capture_progress_sum += cp.progress / 20;
            appendCaptureText(text, entry.getKey(), cp.occupation, cap, cp.lock);
            text.append(Component.literal(" | "));
         }

         b.setName(text);
         if (b.getMax() != capture_potential_total * 2) {
            b.setMax(capture_potential_total * 2);
         }

         if (b.getValue() != capture_progress_sum + capture_potential_total) {
            b.setValue(capture_progress_sum + capture_potential_total);
         }
      }
   }

   private static void appendCaptureText(MutableComponent text, String name, CapturePointUtil.CapturePoint.Occupation occ, int progress, boolean locked) {
      ChatFormatting blueColor = locked ? ChatFormatting.BLACK : ChatFormatting.BLUE;
      ChatFormatting redColor = locked ? ChatFormatting.BLACK : ChatFormatting.RED;
      ChatFormatting nameColor = locked ? ChatFormatting.GRAY : ChatFormatting.WHITE;
      if (occ.blue > 0 || locked) {
         text.append(Component.literal(">").setStyle(Style.EMPTY.withColor(blueColor)));
      }

      text.append(Component.literal(" " + name + " ").setStyle(Style.EMPTY.withColor(progress == 0 ? nameColor : (progress == 1 ? blueColor : redColor))));
      if (occ.red > 0 || locked) {
         text.append(Component.literal("<").setStyle(Style.EMPTY.withColor(redColor)));
      }
   }

   public static class CapturePoint {
      public int progress;
      public boolean lock = false;
      @Nonnull
      public CapturePointUtil.CapturePoint.Occupation occupation = new CapturePointUtil.CapturePoint.Occupation(0, 0);

      public record Occupation(int red, int blue) {
         public static final CapturePointUtil.CapturePoint.Occupation EMPTY = new CapturePointUtil.CapturePoint.Occupation(0, 0);

         public static CapturePointUtil.CapturePoint.Occupation sum(CapturePointUtil.CapturePoint.Occupation a, CapturePointUtil.CapturePoint.Occupation b) {
            return new CapturePointUtil.CapturePoint.Occupation(a.red() + b.red(), a.blue() + b.blue());
         }

         public int delta() {
            return Integer.compare(this.blue, this.red);
         }
      }
   }
}
