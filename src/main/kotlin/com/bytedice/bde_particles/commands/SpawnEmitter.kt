package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.*
import com.bytedice.bde_particles.particles.ParticleEmitter
import com.bytedice.bde_particles.particles.getEmitterDataById
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.Vec3ArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.math.Vec3d
import org.joml.Vector2f


object SpawnEmitter {
  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    val command = CommandManager.literal("SpawnEmitter")
      .requires { source -> source.hasPermissionLevel(2) }

    dispatcher.register(
      command.then(emitterListArg("Emitter ID")
        .executes { context ->
          execute(context, context.source.position)
          Command.SINGLE_SUCCESS
        }
        .then(CommandManager.argument("Pos", Vec3ArgumentType.vec3())
          .executes { context ->
            val pos = Vec3ArgumentType.getVec3(context, "Pos")
            execute(context, pos)
            Command.SINGLE_SUCCESS
          }
        )
      )
    )
  }

  private fun execute(context: CommandContext<ServerCommandSource>, pos: Vec3d) {
    val emitterId = StringArgumentType.getString(context, "Emitter ID")
    val emitterParams = getEmitterDataById(emitterId)
    val world = context.source.world

    if (emitterParams != null) {
      val emitter = ParticleEmitter(
        pos,
        Vector2f(0.0f, 0.0f),
        world,
        emitterParams,
        world.gameRules.getBoolean(Bde_particles.SHOW_PARTICLE_DEBUG)
      )

      spawnEmitter(emitter)

      positiveFeedback("Successfully spawned Emitter ID \"$emitterId\"", context)
    }
    else {
      negativeFeedback("Failed to spawn Emitter ID \"$emitterId\". Emitter ID doesn't exist!", context)
    }
  }
}