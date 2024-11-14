package com.bytedice.bde_particles.particle

import net.minecraft.util.math.Vec3d
import org.joml.Vector3f
import org.joml.Vector4f

class Particle(particleParams: ParticleParams) {
  fun init() {

  }

  fun registerEmitter(id: String, params: ParticleEmitterParams) {
    addToParticleRegister(id, params)
  }
}