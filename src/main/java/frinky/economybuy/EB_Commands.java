package frinky.economybuy;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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
            Trader trader = new Trader(EB_Entities.TRADER_ENTITY, world);
            trader.setPosition(player.getX(), player.getY(), player.getZ());
            world.spawnEntity(trader);
            source.sendFeedback(() -> Text.literal("NPC spawned!"), false);
        }

        return 1;
    }


}