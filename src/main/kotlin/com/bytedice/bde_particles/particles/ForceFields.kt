package com.bytedice.bde_particles.particles

import org.joml.Vector3f

data class ForceField (
  val pos:   Vector3f,
  val shape: ForceFieldShape
) {
  fun deepCopy(): ForceField {
    return ForceField(
      Vector3f(pos),
      shape.deepCopy()
    )
  }
}