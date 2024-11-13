package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.items.ParticleEmitter
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.item.Items
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.awt.Color

object GiveParticleEmitter {
  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    dispatcher.register(
      CommandManager.literal("giveParticleEmitter")
        .requires {  source -> source.hasPermissionLevel(2) }
        .then(CommandManager.argument("item name", StringArgumentType.string()))
        .then(CommandManager.argument("item type", /*ItemStackArgumentType.itemStack()*/ StringArgumentType.string()))
        .then(CommandManager.argument("particle id", StringArgumentType.string()))

        .executes { context ->
          val name       = StringArgumentType.getString(context, "item name")
          val item       = Items.AMETHYST_SHARD // ItemStackArgumentType.getItemStackArgument(context, "item type")
          val particleId = StringArgumentType.getString(context, "particle id")

          val feedback = Text.literal("BPS - You like fancy particles. Don't you?\nBPS - [gave particle emitter, bound to id: \"${context}\"]")
            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(Color(0, 125, 0).rgb)).withItalic(true))

          context.source.player?.inventory?.insertStack(ParticleEmitter().makeData(item, name, particleId))
          context.source.sendFeedback({ feedback }, false)
          Command.SINGLE_SUCCESS
        }
    )
  }
}