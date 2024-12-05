package com.bytedice.bde_particles.commands

import com.bytedice.bde_particles.particles.*
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import java.awt.Color
import java.util.concurrent.CompletableFuture


// TODO: completely redesign


/*
// spawning
maxCount:       Int,
spawnRate:      Int,
spawnChance:    Float,
spawnDuration:  ParamClasses.Duration,
spawnPosOffset: Vector3f,
lifeTime:       ParamClasses.PairInt,
shape:          SpawningShape,
// init transforms
offset:         ParamClasses.PairVec3f,
initRot:        ParamClasses.PairVec3f,
rotVel:         ParamClasses.PairVec3f,
rotWithVel:     Boolean,
initVel:        ParamClasses.PairVec3f,
initCenterVel:  ParamClasses.PairFloat,
initScale:      ParamClasses.PairVec3f,
scaleWithVel:   Boolean,
// velocity
forceFields:    Array<ForceField>,
constVel:       Vector3f,
drag:           Float,
minVel:         Float,
// curves
offsetCurve:    ParamClasses.LerpVal,
rotVelCurve:    ParamClasses.LerpVal,
scaleCurve:     ParamClasses.LerpVal,
blockCurve:     Pair<Array<String>, LerpCurves>,
*/


// ManageEmitters
// <create / remove / config / list>
  // create
    // <emitter id>
    // <preset emitter id>
    // output -> register new emitter with a preset

  // remove
    // <emitter id>
    // output -> removes the particle with that id

  // config
    // <emitter id>
    // <PARTICLE / EMITTER>
    // <param key>
    // <new param value>
    // output -> update the selected parameter of the selected emitter id to the new value

  // list
    // output -> all registered emitters

  // copy
    // <emitter id>
    // output -> give player a command block with the particle params (should be paste-able in kotlin)


object ManageEmitters {
  val allEmitterIds = idRegister.keys

  fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    val command = CommandManager.literal("ManageEmitters")
      .requires { source -> source.hasPermissionLevel(2) }

    dispatcher.register(
      command
    )
  }
}
