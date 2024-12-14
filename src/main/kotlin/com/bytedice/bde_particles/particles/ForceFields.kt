package com.bytedice.bde_particles.particles

import org.joml.Vector3f

data class ForceField (
  val pos:   Vector3f,
  val shape: ForceFieldShape
)