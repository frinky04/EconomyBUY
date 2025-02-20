package frinky.economybuy;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class Trader extends PathAwareEntity {

    private static final double TRACKING_RANGE = 5D; // Range in blocks to track players
    private static final float MOVEMENT_SPEED = 0.5F;
    private final SimpleInventory traderInventory = new SimpleInventory(54);

    protected Trader(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
        this.setInvulnerable(true); // Makes the NPC invulnerable
        this.setCustomName(Text.of("Trader")); // Sets the name of the NPC
        this.setNoGravity(true); // Makes the NPC not fall
        this.setCustomNameVisible(true); // Makes the name visible

        ((MobNavigation)this.getNavigation()).setCanPathThroughDoors(true);

    }

    @Override
    public boolean canMoveVoluntarily() {
        return false;
    }


    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        // Open the trading screen
        if (!this.getWorld().isClient) {
            player.openHandledScreen(
                    new SimpleNamedScreenHandlerFactory((syncId, playerInventory, playerEntity) -> new TraderScreenHandler(syncId, playerInventory, traderInventory), this.getCustomName()
            ));
            return ActionResult.SUCCESS;
        }

        return ActionResult.FAIL;
    }

    @Override
    public void tick() {
        super.tick();
        this.setVelocity(0, 0, 0); // Stop the trader from moving

        if (!this.getWorld().isClient) {
            PlayerEntity closestPlayer = this.getWorld().getClosestPlayer(
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    TRACKING_RANGE,
                    true
            );

            if (closestPlayer != null) {
                // Make the trader look at the player
                this.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, closestPlayer.getEyePos());
            }
        }
    }

    @Override
    protected void initGoals() {
        // stand still mr trader
    }
}

