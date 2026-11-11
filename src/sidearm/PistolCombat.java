package sidearm;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;

/** All hit selection, obstruction checks and damage run on the integrated server thread. */
public final class PistolCombat {
    public static final double RANGE = 64;
    public static final float DAMAGE = 8;
    public static final int COOLDOWN_TICKS = 10;
    private static final ResourceKey<DamageType> SHOT = ResourceKey.create(Registries.DAMAGE_TYPE,
        Identifier.fromNamespaceAndPath("iron_sidearm", "pistol_shot"));
    private static final Map<ServerPlayer, Integer> NEXT_SHOT = new WeakHashMap<>();

    public static boolean fire(ServerPlayer player, float yaw, float pitch) {
        if (!player.isAlive() || player.isSpectator() || player.isSleeping() || player.isPassenger()
            || player.getMainHandItem().getItem() != SidearmMod.PISTOL
            || !Float.isFinite(yaw) || !Float.isFinite(pitch)) return false;
        if (player.isUsingItem() && player.getUseItem().getItem() != SidearmMod.PISTOL) return false;
        if (player.tickCount < NEXT_SHOT.getOrDefault(player, 0)) return false;
        NEXT_SHOT.put(player, player.tickCount + COOLDOWN_TICKS);

        var level = player.level();
        Vec3 start = player.getEyePosition();
        Vec3 direction = Vec3.directionFromRotation(pitch, yaw);
        Vec3 end = start.add(direction.scale(RANGE));
        var block = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE, player));
        end = block.getLocation();
        var hit = ProjectileUtil.getEntityHitResult(player, start, end,
            player.getBoundingBox().expandTowards(direction.scale(RANGE)).inflate(1),
            entity -> entity instanceof LivingEntity && entity.isAlive() && entity.isPickable()
                && !entity.isSpectator() && !entity.isPassengerOfSameVehicle(player),
            start.distanceToSqr(end));
        if (hit != null) {
            end = hit.getLocation();
            var type = level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(SHOT);
            hit.getEntity().hurtServer(level, new DamageSource(type, player, player), DAMAGE);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.PLAYERS, .8f, 1.8f);
        Vec3 muzzle = start.add(direction.scale(.65)).add(0, -.12, 0);
        level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 3, .025, .025, .025, .005);
        level.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y, muzzle.z, 3, .025, .025, .025, .01);
        double distance = start.distanceTo(end);
        for (double travelled = 1; travelled < distance; travelled += 2) {
            Vec3 point = start.add(direction.scale(travelled));
            level.sendParticles(ParticleTypes.CRIT, point.x, point.y, point.z, 1, 0, 0, 0, 0);
        }
        level.sendParticles(ParticleTypes.CRIT, end.x, end.y, end.z, 5, .06, .06, .06, .03);
        return true;
    }
}
