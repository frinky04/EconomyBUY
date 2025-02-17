package frinky.economybuy;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class EB_Commands {

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // Checks the balance of the player
            dispatcher.register(CommandManager.literal("balance").executes(EB_Commands::run));
        });
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.literal("Hello!"));

        return 1;
    }
}