package com.bytedice.bde_particles

import com.bytedice.bde_particles.commands.GiveEmitterTool
import com.bytedice.bde_particles.commands.KillAllEmitters
import com.bytedice.bde_particles.commands.ManageEmitters
import com.bytedice.bde_particles.items.ParticleEmitterTool
import com.bytedice.bde_particles.particles.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import java.util.*


var ALL_PARTICLE_EMITTERS: Array<ParticleEmitter> = emptyArray()
val sessionUuid: UUID = UUID.randomUUID()


class Bde_particles : ModInitializer {
  override fun onInitialize() {
    if (FabricLoader.getInstance().environmentType != EnvType.SERVER) {
      return
    }

    println("BPS - Initializing on ${FabricLoader.getInstance().environmentType}")

    ServerLifecycleEvents.SERVER_STARTED.register { _ ->
      init()
    }

    ServerTickEvents.START_SERVER_TICK.register { server ->
      tick(server)
    }

    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, _ ->
      GiveEmitterTool.register(dispatcher, registryAccess)
      ManageEmitters .register(dispatcher)
      KillAllEmitters.register(dispatcher)
    }

    UseItemCallback.EVENT.register(UseItemCallback { player: PlayerEntity, world: World, hand: Hand ->
      if (world is ServerWorld) {
        onRightClick(player, world, hand)
      }
      TypedActionResult(ActionResult.PASS, player.getStackInHand(hand))
    })

    ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { _ ->
      println("BPS - Progress bar filled :3")
      println(
        "\n____________  _____              _____ \n" +
        "| ___ \\ ___ \\/  ___|         _  |____ |\n" +
        "| |_/ / |_/ /\\ `--.         (_)     / /\n" +
        "| ___ \\  __/  `--. \\                \\ \\\n" +
        "| |_/ / |    /\\__/ /         _  .___/ /\n" +
        "\\____/\\_|    \\____/         (_) \\____/ \n"
      )
    })
  }
}


fun init() {
  addToEmitterRegister("DEFAULT", EmitterParams.DEFAULT)
  addToEmitterRegister("FIRE_GEYSER", EmitterParams.FIRE_GEYSER)
  addToEmitterRegister("RING_EXPLOSION", EmitterParams.RING_EXPLOSION)
}


fun tick(server: MinecraftServer) {
  for (world in server.worlds) {
    for (entity in world.iterateEntities()) {
      if (entity.type == EntityType.BLOCK_DISPLAY) {
        if (displayEntityContainsTag(entity as BlockDisplayEntity, "BPS_UUID")
          && !displayEntityContainsTag(entity, sessionUuid.toString())) {

          entity.kill()
        }
      }
    }
  }

  for (emitter in ALL_PARTICLE_EMITTERS) {
    if (emitter.isDead) {
      ALL_PARTICLE_EMITTERS = ALL_PARTICLE_EMITTERS.toMutableList().apply { remove(emitter) }.toTypedArray()
    }
    else { emitter.tick() }
  }
}


fun onRightClick(player: PlayerEntity, world: ServerWorld, hand: Hand) : TypedActionResult<ItemStack> {
  val handItem = player.getStackInHand(hand)
  val (isEmitterTool, emitterId) = ParticleEmitterTool.getToolDetails(handItem)
  val hitResult = raycastFromPlayer(player as ServerPlayerEntity, 200.0)
  val emitterParams = getEmitterParams(emitterId)

  if (
    !isEmitterTool
    || emitterParams == null
    || hitResult == null
  ) {
    return TypedActionResult(ActionResult.PASS, handItem)
  }

  val emitter = ParticleEmitter(hitResult.pos, world, emitterParams)
  ALL_PARTICLE_EMITTERS += emitter

  return TypedActionResult(ActionResult.PASS, handItem)
}


fun emitterParamsToJson(params: EmitterParams) : Map<String, Any> {
  val allParticleParamsJSON: MutableList<Map<String, Any?>> = mutableListOf()
  val particle = params.particle

  val particleParamsJSON = mapOf(
    "shape"        to particle.shape,
    "blockCurve"   to particle.blockCurve,
    "rotRandom"    to particle.rotRandom,
    "rotVelRandom" to particle.rotVelRandom,
    "rotVelCurve"  to particle.rotVelCurve,
    "sizeRandom"   to particle.sizeRandom,
    "uniformSize"  to particle.uniformSize,
    "sizeCurve"    to particle.sizeCurve,
    "velRandom"    to particle.velRandom,
    "forceFields"  to particle.forceFields,
    "gravity"      to particle.gravity,
    "drag"         to particle.drag,
    "minVel"       to particle.minVel,
    "lifeTime"     to particle.lifeTime
  )

    allParticleParamsJSON.add(particleParamsJSON)

  val emitterParamsJSON = mapOf(
    "maxCount"      to params.maxCount,
    "spawnsPerTick" to params.spawnsPerTick,
    "loopDur"       to params.loopDur,
    "loopDelay"     to params.loopDelay,
    "loopCount"     to params.loopCount,
    "particleTypes" to allParticleParamsJSON
  )

  return emitterParamsJSON
}


fun displayEntityContainsTag(entity: BlockDisplayEntity, tag: String) : Boolean {
  val nbt = NbtCompound()
  entity.writeNbt(nbt)
  val tagsNbtList = nbt.getList("Tags", 8)

  return tagsNbtList?.any { it.asString() == tag } == true
}