package com.nxmtl.android.sportsview;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.nxmtl.android.sportsview.initializers.AccelerationInitializer;
import com.nxmtl.android.sportsview.initializers.ParticleInitializer;
import com.nxmtl.android.sportsview.initializers.RotationInitializer;
import com.nxmtl.android.sportsview.initializers.RotationSpeedInitializer;
import com.nxmtl.android.sportsview.initializers.ScaleInitializer;
import com.nxmtl.android.sportsview.initializers.SpeedModuleAndRangeInitializer;
import com.nxmtl.android.sportsview.initializers.SpeeddByComponentsInitializer;
import com.nxmtl.android.sportsview.modifiers.AlphaModifier;
import com.nxmtl.android.sportsview.modifiers.ParticleModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ParticleSystem {

	private int mMaxParticles;
	private Random mRandom;

	private ArrayList<Particle> mParticles;// pool
	private final ArrayList<Particle> mActiveParticles = new ArrayList<>();
	private long mTimeToLive;
	private long mCurrentTime = 0;
	private float mParticlesPerMillisecond;
	private int mActivatedParticles;
	private long mEmittingTime;
	private SportsView mDrawingView;
	private List<ParticleModifier> mModifiers;
	private List<ParticleInitializer> mInitializers;


	private float mDpToPxScale;

	private int mEmitterXMin;
	private int mEmitterXMax;
	private int mEmitterYMin;
	private int mEmitterYMax;



	private ParticleSystem(SportsView mDrawingView,int maxParticles, long timeToLive) {
		this.mDrawingView = mDrawingView;
		mRandom = new Random();

		mModifiers = new ArrayList<>();
		mInitializers = new ArrayList<>();

		mMaxParticles = maxParticles;
		// Create the particles

		mParticles = new ArrayList<>();
		mTimeToLive = timeToLive;
		mDpToPxScale = (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	/**
	 * Creates a particle system with the given parameters
	 *
	 * @param drawable The drawable to use as a particle
	 * @param maxParticles The maximum number of particles
	 * @param timeToLive The time to live for the particles
	 */
	public ParticleSystem(SportsView mDrawingView,int maxParticles, Drawable drawable, long timeToLive) {
		this(mDrawingView,maxParticles, timeToLive);

		if (drawable instanceof AnimationDrawable) {
			AnimationDrawable animation = (AnimationDrawable) drawable;
			for (int i=0; i<mMaxParticles; i++) {
				mParticles.add (new AnimatedParticle (animation));
			}
		} else {
			Bitmap bitmap = null;
			if (drawable instanceof BitmapDrawable) {
				bitmap = ((BitmapDrawable) drawable).getBitmap();
			} else {
				bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
				drawable.draw(canvas);
			}
			for (int i=0; i<mMaxParticles; i++) {
				mParticles.add (new Particle (bitmap));
			}
		}
	}

	public float dpToPx(float dp) {
		return dp * mDpToPxScale;
	}
	/**
	 * Utility constructor that receives a Bitmap
	 *
	 * @param maxParticles The maximum number of particles
	 * @param bitmap The bitmap to use as particle
	 * @param timeToLive The time to live for the particles
	 */
	public ParticleSystem(SportsView mDrawingView,int maxParticles, Bitmap bitmap, long timeToLive) {
		this(mDrawingView, maxParticles, timeToLive);
		for (int i=0; i<mMaxParticles; i++) {
			mParticles.add (new Particle (bitmap));
		}
	}

	/**
	 * Utility constructor that receives an AnimationDrawable
	 *
	 * @param maxParticles The maximum number of particles
	 * @param animation The animation to use as particle
	 * @param timeToLive The time to live for the particles
	 */
	public ParticleSystem(SportsView mDrawingView,int maxParticles, AnimationDrawable animation, long timeToLive) {
		this(mDrawingView,maxParticles, timeToLive);
		// Create the particles
		for (int i=0; i<mMaxParticles; i++) {
			mParticles.add (new AnimatedParticle (animation));
		}
	}

	/**
	 * Adds a modifier to the Particle system, it will be executed on each update.
	 *
	 * @param modifier modifier to be added to the ParticleSystem
	 */
	public ParticleSystem addModifier(ParticleModifier modifier) {
		mModifiers.add(modifier);
		return this;
	}

	public ParticleSystem setSpeedRange(float speedMin, float speedMax) {
		mInitializers.add(new SpeedModuleAndRangeInitializer(dpToPx(speedMin), dpToPx(speedMax), 0, 360));
		return this;
	}

    /**
     * Initializes the speed range and angle range of emitted particles. Angles are in degrees
     * and non negative:
     * 0 meaning to the right, 90 to the bottom,... in clockwise orientation. Speed is non
	 * negative and is described in pixels per millisecond.
     * @param speedMin The minimum speed to emit particles.
     * @param speedMax The maximum speed to emit particles.
     * @param minAngle The minimum angle to emit particles in degrees.
     * @param maxAngle The maximum angle to emit particles in degrees.
     * @return This.
     */
	public ParticleSystem setSpeedModuleAndAngleRange(float speedMin, float speedMax, int minAngle, int maxAngle) {
        // else emitting from top (270°) to bottom (90°) range would not be possible if someone
        // entered minAngle = 270 and maxAngle=90 since the module would swap the values
        while (maxAngle < minAngle) {
            maxAngle += 360;
        }
		mInitializers.add(new SpeedModuleAndRangeInitializer(dpToPx(speedMin), dpToPx(speedMax), minAngle, maxAngle));
		return this;
	}

    /**
     * Initializes the speed components ranges that particles will be emitted. Speeds are
     * measured in density pixels per millisecond.
     * @param speedMinX The minimum speed in x direction.
     * @param speedMaxX The maximum speed in x direction.
     * @param speedMinY The minimum speed in y direction.
     * @param speedMaxY The maximum speed in y direction.
     * @return This.
     */
	public ParticleSystem setSpeedByComponentsRange(float speedMinX, float speedMaxX, float speedMinY, float speedMaxY) {
        mInitializers.add(new SpeeddByComponentsInitializer(dpToPx(speedMinX), dpToPx(speedMaxX),
				dpToPx(speedMinY), dpToPx(speedMaxY)));
		return this;
	}

    /**
     * Initializes the initial rotation range of emitted particles. The rotation angle is
     * measured in degrees with 0° being no rotation at all and 90° tilting the image to the right.
     * @param minAngle The minimum tilt angle.
     * @param maxAngle The maximum tilt angle.
     * @return This.
     */
	public ParticleSystem setInitialRotationRange(int minAngle, int maxAngle) {
		mInitializers.add(new RotationInitializer(minAngle, maxAngle));
		return this;
	}

    /**
     * Initializes the scale range of emitted particles. Will scale the images around their
     * center multiplied with the given scaling factor.
     * @param minScale The minimum scaling factor
     * @param maxScale The maximum scaling factor.
     * @return This.
     */
	public ParticleSystem setScaleRange(float minScale, float maxScale) {
		mInitializers.add(new ScaleInitializer(minScale, maxScale));
		return this;
	}

    /**
     * Initializes the rotation speed of emitted particles. Rotation speed is measured in degrees
     * per second.
     * @param rotationSpeed The rotation speed.
     * @return This.
     */
	public ParticleSystem setRotationSpeed(float rotationSpeed) {
        mInitializers.add(new RotationSpeedInitializer(rotationSpeed, rotationSpeed));
		return this;
	}

    /**
     * Initializes the rotation speed range for emitted particles. The rotation speed is measured
     * in degrees per second and can be positive or negative.
     * @param minRotationSpeed The minimum rotation speed.
     * @param maxRotationSpeed The maximum rotation speed.
     * @return This.
     */
	public ParticleSystem setRotationSpeedRange(float minRotationSpeed, float maxRotationSpeed) {
        mInitializers.add(new RotationSpeedInitializer(minRotationSpeed, maxRotationSpeed));
		return this;
	}

    /**
     * Initializes the acceleration range and angle range of emitted particles. The acceleration
     * components in x and y direction are controlled by the acceleration angle. The acceleration
     * is measured in density pixels per square millisecond. The angle is measured in degrees
     * with 0° pointing to the right and going clockwise.
     * @param minAcceleration
     * @param maxAcceleration
     * @param minAngle
     * @param maxAngle
     * @return
     */
	public ParticleSystem setAccelerationModuleAndAndAngleRange(float minAcceleration, float maxAcceleration, int minAngle, int maxAngle) {
        mInitializers.add(new AccelerationInitializer(dpToPx(minAcceleration), dpToPx(maxAcceleration),
				minAngle, maxAngle));
		return this;
	}

	/**
	 * Adds a custom initializer for emitted particles. The most common use case is the ability to
	 * update the initializer in real-time instead of adding new ones ontop of the existing one.
	 * @param initializer The non-null initializer to add.
	 * @return This.
	 */
	public ParticleSystem addInitializer(ParticleInitializer initializer) {
		if (initializer != null) {
			mInitializers.add(initializer);
		}
		return this;
	}

    /**
     * Initializes the acceleration for emitted particles with the given angle. Acceleration is
     * measured in pixels per square millisecond. The angle is measured in degrees with 0°
     * meaning to the right and orientation being clockwise. The angle controls the acceleration
     * direction.
     * @param acceleration The acceleration.
     * @param angle The acceleration direction.
     * @return This.
     */
	public ParticleSystem setAcceleration(float acceleration, int angle) {
        mInitializers.add(new AccelerationInitializer(acceleration, acceleration, angle, angle));
		return this;
	}



	public ParticleSystem setStartTime(long time) {
		mCurrentTime = time;
		return this;
	}

	/**
	 * Configures a fade out for the particles when they disappear
	 *
	 * @param milisecondsBeforeEnd fade out duration in milliseconds
	 * @param interpolator the interpolator for the fade out (default is linear)
	 */
	public ParticleSystem setFadeOut(long milisecondsBeforeEnd, Interpolator interpolator) {
		mModifiers.add(new AlphaModifier(255, 0, mTimeToLive-milisecondsBeforeEnd, mTimeToLive, interpolator));
		return this;
	}

	/**
	 * Configures a fade out for the particles when they disappear
	 *
	 * @param duration fade out duration in milliseconds
	 */
	public ParticleSystem setFadeOut(long duration) {
		return setFadeOut(duration, new LinearInterpolator());
	}



	private void configureEmitter(int [] emitter) {
		// We configure the emitter based on the window location to fix the offset of action bar if present
		if(emitter==null || emitter.length<4){
			return;
		}
		mEmitterXMin = emitter[0];
		mEmitterXMax = emitter[1];
		mEmitterYMin = emitter[2];
		mEmitterYMax = emitter[3];
	}

	public void prepareEmitting(int particlesPerSecond, int[] emitter) {
		configureEmitter(emitter);
		mActivatedParticles = 0;
		mParticlesPerMillisecond = particlesPerSecond/1000f;

		mEmittingTime = -1; // Meaning infinite
		mDrawingView.setParticles (mActiveParticles);
	}

	public void updateEmitPoint (int [] emitter) {
		configureEmitter(emitter);
	}

	private void activateParticle(long delay) {
		Particle p = mParticles.remove(0);
		p.init();
		// Initialization goes before configuration, scale is required before can be configured properly
		for (int i=0; i<mInitializers.size(); i++) {
			mInitializers.get(i).initParticle(p, mRandom);
		}
		int particleX = getFromRange (mEmitterXMin, mEmitterXMax);
		int particleY = getFromRange (mEmitterYMin, mEmitterYMax);
		p.configure(mTimeToLive, particleX, particleY);
		p.activate(delay, mModifiers);
		mActiveParticles.add(p);
		mActivatedParticles++;
	}

	private int getFromRange(int minValue, int maxValue) {
		if (minValue == maxValue) {
			return minValue;
		}
		if (minValue < maxValue) {
			return mRandom.nextInt(maxValue - minValue) + minValue;
		}
		else {
			return mRandom.nextInt(minValue - maxValue) + maxValue;
		}
	}

	protected void onUpdate(long miliseconds) {
		while (((mEmittingTime > 0 && miliseconds < mEmittingTime)|| mEmittingTime == -1) && // This point should emit
				!mParticles.isEmpty() && // We have particles in the pool
				mActivatedParticles < mParticlesPerMillisecond *miliseconds) { // and we are under the number of particles that should be launched
			// Activate a new particle
			activateParticle(miliseconds);
		}
		synchronized(mActiveParticles) {
			for (int i = 0; i < mActiveParticles.size(); i++) {
				boolean active = mActiveParticles.get(i).update(miliseconds);
				if (!active) {
					Particle p = mActiveParticles.remove(i);
					i--; // Needed to keep the index at the right position
					mParticles.add(p);
				}
			}
		}
		mDrawingView.postInvalidate();
	}

	public void stopEmitting () {
		// The time to be emitting is the current time (as if it was a time-limited emitter
		mEmittingTime = mCurrentTime;
	}

}
