package frinky.economybuy.blocks;

import frinky.economybuy.EB_Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

import java.util.Objects;

// The interest chest block entity is a chest block entity that has an interest rate, every day (or, whatever you set ticks per day to), the amount of money in the chest will increase by the interest rate
// in the event the chest entity is unloaded, when its reloaded, we'll calculate the interest rate based on the time it was unloaded, so it'll be accurate
public class InterestChestBlockEntity extends ChestBlockEntity {
    private static final int INVENTORY_SIZE = 27; // Standard chest size
    private static final float INTEREST_RATE = 0.10f; // 10% interest per day
    private static final long TICKS_PER_DAY = 24000; // 20 minutes per day
    private long lastInterestDay;

    public InterestChestBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(EB_Blocks.INTEREST_CHEST_ENTITY_TYPE, blockPos, blockState);
    }

    public void tick() {
        if (this.world == null || this.world.isClient) return;
        long currentTime = Objects.requireNonNull(this.getWorld()).getTime();
        long currentDay = currentTime / TICKS_PER_DAY;
        if (currentDay > this.lastInterestDay) {
            long daysPassed = currentDay - this.lastInterestDay;
            this.calculateInterest(daysPassed);
            this.lastInterestDay = currentDay;
            markDirty(); // ensure the new lastInterestDay is saved
        }
    }


    void calculateInterest(long daysPassed) {
        System.out.println("Calculating interest for " + daysPassed + " days");
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        if (nbt.contains("lastInterestDay")) {
            this.lastInterestDay = nbt.getLong("lastInterestDay");
        } else {
            // Initialize for a new chest
            this.lastInterestDay = this.world != null ? this.world.getTime() / TICKS_PER_DAY : 0;
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putLong("lastInterestDay", this.lastInterestDay);
    }
}