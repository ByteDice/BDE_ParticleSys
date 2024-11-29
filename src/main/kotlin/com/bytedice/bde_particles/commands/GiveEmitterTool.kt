package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.items.ParticleEmitterTool
import com.bytedice.bde_particles.particles.idRegister
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.awt.Color
import java.util.concurrent.CompletableFuture

object GiveEmitterTool {
  fun register(dispatcher: CommandDispatcher<ServerCommandSource>, registryAccess: CommandRegistryAccess) {
    val command = CommandManager.literal("GiveEmitterTool")
      .requires { source -> source.hasPermissionLevel(4) }

    val allEmitterIds = idRegister.keys

    val itemNameArg = CommandManager.argument("Item Name", StringArgumentType.string())
    val itemTypeArg = CommandManager.argument("Item Type", ItemStackArgumentType.itemStack(registryAccess))

    val emitterIdSuggestion = CommandManager.argument("Emitter ID", StringArgumentType.string())
      .suggests { _, builder ->
        allEmitterIds.forEach { key ->
          builder.suggest(key)
        }
        CompletableFuture.completedFuture(builder.build())
      }

    dispatcher.register(
      command
        .then(itemNameArg
          .then(itemTypeArg
            .then(emitterIdSuggestion
              .executes { context ->
                val name = StringArgumentType.getString(context, "Item Name")
                val item = ItemStackArgumentType.getItemStackArgument(context, "Item Type")
                val emitterId = StringArgumentType.getString(context, "Emitter ID")

                val feedback = Text.literal("BPS - You like fancy particles. Don't you?\nBPS - gave particle emitter, bound to ID: \"${emitterId}\".")
                  .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 200, 0).rgb)))

                context.source.player?.inventory?.insertStack(ParticleEmitterTool.makeData(item.item, name, emitterId))
                context.source.sendFeedback({ feedback }, false)
                Command.SINGLE_SUCCESS
              }
            )
          )
        )
    )
  }
}
