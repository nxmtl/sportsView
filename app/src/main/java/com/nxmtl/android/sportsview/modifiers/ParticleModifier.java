package com.nxmtl.android.sportsview.modifiers;


import com.nxmtl.android.sportsview.Particle;

public interface ParticleModifier {

	/**
	 * modifies the specific value of a particle given the current miliseconds
	 * @param particle
	 * @param miliseconds
	 */
	void apply(Particle particle, long miliseconds);

}
