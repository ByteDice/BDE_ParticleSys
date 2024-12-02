package com.bytedice.bde_particles

import com.bytedice.bde_particles.commands.GiveEmitterTool
import com.bytedice.bde_particles.commands.KillAllEmitters
//import com.bytedice.bde_particles.commands.ManageEmitters
import com.bytedice.bde_particles.items.ParticleEmitterTool
import com.bytedice.bde_particles.particles.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.EnvType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.UseItemCallback
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.GameRules
import net.minecraft.world.World
import org.joml.Vector2f
import java.util.*


// TODO: (future additions)
// Allowing the use of regular Minecraft particles instead of just BDEs.
  // not implementing in v1.0. Maybe in v2.0.

// Better command auto-completion. (and change names of args)
  // force field "config" option

// Custom curve equation command args (parse from strings)

// Cylinder and cone force field shape.
  // not implementing in v1.0. Maybe in v2.0.

// Emitter groups (multiple emitters for EmitterTool)

// Private functions/classes


// TODO: (project)
// fix ManageEmitter command
// add SpawnEmitter command for spawning emitters via commands


var ALL_PARTICLE_EMITTERS: Array<ParticleEmitter> = emptyArray()
var LIVING_PARTICLE_COUNT: Int = 0
val SESSION_UUID: UUID = UUID.randomUUID()


class Bde_particles : ModInitializer {
  companion object {
    val GLOBAL_MAX_PARTICLES: GameRules.Key<GameRules.IntRule> = GameRuleRegistry.register(
      "GlobalMaxParticles",
      GameRules.Category.UPDATES,
      GameRuleFactory.createIntRule(3000, 0)
    )
    val SHOW_PARTICLE_DEBUG: GameRules.Key<GameRules.BooleanRule> = GameRuleRegistry.register(
      "ShowParticleDebug",
      GameRules.Category.UPDATES,
      GameRuleFactory.createBooleanRule(false)
    )
  }

  override fun onInitialize() {
    if (FabricLoader.getInstance().environmentType != EnvType.SERVER) {
      return
    }

    println("BPS - Initializing on ${FabricLoader.getInstance().environmentType}")

    ServerTickEvents.START_SERVER_TICK.register { server ->
      tick(server)
    }

    CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, _ ->
      GiveEmitterTool.register(dispatcher, registryAccess)
      //ManageEmitters .register(dispatcher)
      KillAllEmitters.register(dispatcher)
    }

    UseItemCallback.EVENT.register(UseItemCallback { player: PlayerEntity, world: World, hand: Hand ->
      if (world is ServerWorld) {
        onRightClick(player, world, hand)
      }
      TypedActionResult(ActionResult.PASS, player.getStackInHand(hand))
    })

    ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { _ ->
      init()

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
  addToRegister("DEFAULT", EmitterParams.DEFAULT, 137)
  addToRegister("DEBUG", EmitterParams.DEBUG, 137)
  addToRegister("STRESS_TEST", EmitterParams.STRESS_TEST, 1532)
}


fun tick(server: MinecraftServer) {
  runBlocking {
    server.worlds.map { world ->
      async {
        val displayEntities = world.iterateEntities()
          .filter { it.type == EntityType.ITEM_DISPLAY }
        for (entity in displayEntities) {
          if (displayEntityContainsTag(entity as ItemDisplayEntity, "BPS_UUID")
            && !displayEntityContainsTag(entity, SESSION_UUID.toString())) {

            entity.kill()
          }
        }
      }
    }.awaitAll()
  }

  runBlocking {
    ALL_PARTICLE_EMITTERS.map { emitter ->
      async {
        if (emitter.isDead) {
          synchronized(ALL_PARTICLE_EMITTERS) {
            ALL_PARTICLE_EMITTERS = ALL_PARTICLE_EMITTERS.toMutableList().apply { remove(emitter) }.toTypedArray()
          }
        }
        else { emitter.tick() }
      }
    }.awaitAll()
  }
}


fun onRightClick(player: PlayerEntity, world: ServerWorld, hand: Hand) : TypedActionResult<ItemStack> {
  val handItem = player.getStackInHand(hand)
  val (isEmitterTool, emitterId) = ParticleEmitterTool.getToolDetails(handItem)
  val hitResult = raycastFromPlayer(player as ServerPlayerEntity, 200.0)
  val emitterParams = getEmitterDataById(emitterId)

  if (
    !isEmitterTool
    || emitterParams == null
    || hitResult == null
  ) {
    return TypedActionResult(ActionResult.PASS, handItem)
  }

  val debug = world.gameRules.getBoolean(Bde_particles.SHOW_PARTICLE_DEBUG)
  val emitter = ParticleEmitter(
    hitResult.pos,
    Vector2f(0.0f, 0.0f),
    world,
    emitterParams,
    debug
  )
  ALL_PARTICLE_EMITTERS += emitter

  return TypedActionResult(ActionResult.PASS, handItem)
}


/*
fun emitterParamsToJson(params: EmitterParams) : Map<String, Any> { // TODO: map to new params
  val allParamsJson: MutableList<Map<String, Any?>> = mutableListOf()

  val paramsJson = mapOf(
    "shape"        to params.shape
  )

  allParamsJson.add(paramsJson)

  val emitterParamsJSON = mapOf(
    "maxCount"      to params.maxCount,
  )

  return emitterParamsJSON
}
*/


fun displayEntityContainsTag(entity: ItemDisplayEntity, tag: String) : Boolean {
  val nbt = NbtCompound()
  entity.writeNbt(nbt)
  val tagsNbtList = nbt.getList("Tags", 8)

  return tagsNbtList?.any { it.asString() == tag } == true
}