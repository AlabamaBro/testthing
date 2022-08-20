package net.mca.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mca.Config;
import net.mca.MCA;
import net.mca.cobalt.network.NetworkHandler;
import net.mca.network.s2c.OpenGuiRequest;
import net.mca.server.ServerInteractionManager;
import net.mca.server.world.data.PlayerSaveData;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

public class Command {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(MCA.MOD_ID)
                .then(register("help", Command::displayHelp))
                .then(register("propose").then(CommandManager.argument("target", EntityArgumentType.player()).executes(Command::propose)))
                .then(register("accept").then(CommandManager.argument("target", EntityArgumentType.player()).executes(Command::accept)))
                .then(register("proposals", Command::displayProposal))
                .then(register("procreate", Command::procreate))
                .then(register("separate", Command::separate))
                .then(register("reject").then(CommandManager.argument("target", EntityArgumentType.player()).executes(Command::reject)))
                .then(register("editor", Command::editor))
                .then(register("destiny", Command::destiny))
                .then(register("mail", Command::mail))
        );
    }

    private static int editor(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        if (ctx.getSource().hasPermissionLevel(2) || Config.getInstance().allowFullPlayerEditor) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.VILLAGER_EDITOR, ctx.getSource().getPlayer()), ctx.getSource().getPlayer());
            return 0;
        } else if (Config.getInstance().allowLimitedPlayerEditor) {
            NetworkHandler.sendToPlayer(new OpenGuiRequest(OpenGuiRequest.Type.LIMITED_VILLAGER_EDITOR, ctx.getSource().getPlayer()), ctx.getSource().getPlayer());
            return 0;
        } else {
            ctx.getSource().getPlayer().sendSystemMessage(new TranslatableText("command.no_permission").formatted(Formatting.RED), Util.NIL_UUID);
            return 1;
        }
    }

    private static int destiny(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        if (ctx.getSource().hasPermissionLevel(2) || Config.getInstance().allowDestinyCommandOnce) {
            ServerPlayerEntity player = ctx.getSource().getPlayer();
            if (!PlayerSaveData.get(player).isEntityDataSet() || Config.getInstance().allowDestinyCommandMoreThanOnce) {
                ServerInteractionManager.launchDestiny(player);
                return 0;
            } else {
                ctx.getSource().getPlayer().sendSystemMessage(new TranslatableText("command.only_one_destiny").formatted(Formatting.RED), Util.NIL_UUID);
                return 1;
            }
        } else {
            ctx.getSource().getPlayer().sendSystemMessage(new TranslatableText("command.no_permission").formatted(Formatting.RED), Util.NIL_UUID);
            return 1;
        }
    }

    private static int mail(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        PlayerSaveData data = PlayerSaveData.get(player);
        if (data.hasMail()) {
            while (data.hasMail()) {
                player.inventory.offerOrDrop(player.world, data.getMail());
            }
        } else {
            ctx.getSource().getPlayer().sendSystemMessage(new TranslatableText("command.no_mail"), Util.NIL_UUID);
        }
        return 0;
    }

    private static int displayHelp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        sendMessage(ctx.getSource().getPlayer(), Formatting.DARK_RED + "--- " + Formatting.GOLD + "PLAYER COMMANDS" + Formatting.DARK_RED + " ---");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca editor" + Formatting.GOLD + " - Choose your genetics and stuff.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca propose <PlayerName>" + Formatting.GOLD + " - Proposes marriage to the given player.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca proposals " + Formatting.GOLD + " - Shows all active proposals.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca accept <PlayerName>" + Formatting.GOLD + " - Accepts the player's marriage request.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca reject <PlayerName>" + Formatting.GOLD + " - Rejects the player's marriage request.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca procreate " + Formatting.GOLD + " - Starts procreation.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca separate " + Formatting.GOLD + " - Ends your marriage.");
        sendMessage(ctx.getSource().getPlayer(), Formatting.DARK_RED + "--- " + Formatting.GOLD + "GLOBAL COMMANDS" + Formatting.DARK_RED + " ---");
        sendMessage(ctx.getSource().getPlayer(), Formatting.WHITE + " /mca help " + Formatting.GOLD + " - Shows this list of commands.");
        return 0;
    }

    private static int propose(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().sendProposal(ctx.getSource().getPlayer(), target);

        return 0;
    }

    private static int accept(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().acceptProposal(ctx.getSource().getPlayer(), target);
        return 0;
    }

    private static int displayProposal(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().listProposals(ctx.getSource().getPlayer());

        return 0;
    }

    private static int procreate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().procreate(ctx.getSource().getPlayer());
        return 0;
    }

    private static int separate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerInteractionManager.getInstance().endMarriage(ctx.getSource().getPlayer());
        return 0;
    }

    private static int reject(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
        ServerInteractionManager.getInstance().rejectProposal(ctx.getSource().getPlayer(), target);
        return 0;
    }


    private static ArgumentBuilder<ServerCommandSource, ?> register(String name, com.mojang.brigadier.Command<ServerCommandSource> cmd) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(0)).executes(cmd);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> register(String name) {
        return CommandManager.literal(name).requires(cs -> cs.hasPermissionLevel(0));
    }

    private static void sendMessage(Entity commandSender, String message) {
        commandSender.sendSystemMessage(new LiteralText(Formatting.GOLD + "[MCA] " + Formatting.RESET + message), Util.NIL_UUID);
    }
}
