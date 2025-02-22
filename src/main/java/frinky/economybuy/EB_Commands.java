package frinky.economybuy;

import com.mojang.brigadier.context.CommandContext;
import frinky.economybuy.economy.EB_EconomyManager;
import frinky.economybuy.trader.EB_Trader;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class EB_Commands {

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("balance").executes(EB_Commands::balance));
            dispatcher.register(CommandManager.literal("spawnTrader").executes(EB_Commands::spawnTrader));
            dispatcher.register(CommandManager.literal("syncMarket").executes(EB_Commands::syncMarket));
            dispatcher.register(CommandManager.literal("clearTraders").executes(EB_Commands::clearTraders));
        });
    }

    private static int balance(CommandContext<ServerCommandSource> context) {
        if(context.getSource().getPlayer() == null) {
            return 0;
        }
        context.getSource().sendMessage(Text.literal("Your balance is: " + EB_Util.GetNetWorth(context.getSource().getPlayer())));

        return 1;
    }

    private static int spawnTrader(CommandContext<ServerCommandSource> context) {
        if(context.getSource().getPlayer() == null) {
            return 0;
        }

        context.getSource().sendMessage(Text.literal("Spawning trader"));

        ServerCommandSource source = context.getSource();
        ServerWorld world = source.getWorld();
        ServerPlayerEntity player = source.getPlayer();

        if (player != null) {
            EB_Trader EBTrader = new EB_Trader(EB_Entities.TRADER_ENTITY, world);
            EBTrader.setPosition(player.getX(), player.getY(), player.getZ());
            world.spawnEntity(EBTrader);
            source.sendFeedback(() -> Text.literal("NPC spawned!"), false);
        }

        return 1;
    }

    private static int clearTraders(CommandContext<ServerCommandSource> context) {
        if(context.getSource().getPlayer() == null) {
            return 0;
        }

        // remove all trader entities
        context.getSource().getWorld().getEntitiesByType(EB_Entities.TRADER_ENTITY, entity -> true).forEach(entity -> entity.remove(Entity.RemovalReason.KILLED));


        return 1;
    }

    private static int syncMarket(CommandContext<ServerCommandSource> context) {
        EB_EconomyManager.getInstance().syncMarket(context.getSource().getServer());
        return 1;
    }

}