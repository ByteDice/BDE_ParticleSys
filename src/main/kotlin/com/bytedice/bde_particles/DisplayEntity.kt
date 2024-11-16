package com.bytedice.bde_particles

import net.minecraft.entity.EntityType
import net.minecraft.entity.decoration.DisplayEntity.BlockDisplayEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtFloat
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.server.world.ServerWorld

class DisplayEntity(val properties: DisplayEntityProperties?) {
  private var hasSpawned = false
  private var entity: BlockDisplayEntity? = null

  fun spawn(world: ServerWorld) : BlockDisplayEntity? {
    if (hasSpawned || properties == null) { return null }

    val e = updateProperties(world, properties)
    world.spawnEntity(e)

    this.hasSpawned = true
    return e
  }

  fun updateProperties(world: ServerWorld, properties: DisplayEntityProperties) : BlockDisplayEntity {
    val e = BlockDisplayEntity(EntityType.BLOCK_DISPLAY, world)

    val nbt = NbtCompound().apply {
      val tagList = NbtList()

      properties.tags.forEach { tag ->
        tagList.add(NbtString.of(tag))
      }

      put("Tags", tagList)

      put("block_state", NbtCompound().apply { putString("Name", properties.blockType) })

      put("transformation", NbtCompound().apply {
        val offsetList = NbtList().apply {
          add(NbtFloat.of(properties.translation.x))
          add(NbtFloat.of(properties.translation.y))
          add(NbtFloat.of(properties.translation.z))
        }
        val rotList = NbtList().apply {
          add(NbtFloat.of(properties.leftRotation.x))
          add(NbtFloat.of(properties.leftRotation.y))
          add(NbtFloat.of(properties.leftRotation.z))
          add(NbtFloat.of(properties.leftRotation.w))
        }
        val scaleList = NbtList().apply {
          add(NbtFloat.of(properties.scale.x))
          add(NbtFloat.of(properties.scale.y))
          add(NbtFloat.of(properties.scale.z))
        }
        val rightRotList = NbtList().apply {
          add(NbtFloat.of(properties.rightRotation.x))
          add(NbtFloat.of(properties.rightRotation.y))
          add(NbtFloat.of(properties.rightRotation.z))
          add(NbtFloat.of(properties.rightRotation.w))
        }

        put("translation",    offsetList)
        put("left_rotation",  rotList)
        put("scale",          scaleList)
        put("right_rotation", rightRotList)
      })
    }

    e.readNbt(nbt)

    e.refreshPositionAndAngles(properties.pos.x, properties.pos.y, properties.pos.z, properties.rot.x, properties.rot.y)

    entity = e
    return e
  }

  fun kill() {
    entity?.kill()
  }
}