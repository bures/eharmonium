package eharmonium

class BellowsChordPlayer extends ChordPlayer {
	private val PLAYING_STOPPED = 1
	private val PLAYING_PRESSED = 2
	private val PLAYING_RELEASED = 3
	
	private var playingState = PLAYING_STOPPED

	private var base: Int = 0
	private var tones: List[Int] = null
	
	private val periodicTimer = new PeriodicTask
	
	private var currentBellowsVol : Double = 0
	private var isFromZero = false
	private var currentStressVol : Double = 0
	private var stressVolDecay : Double = 0
	private var isBellowsPressedAsSpace = false
	private var isBellowsPressedAsChord = false
	
	
	private def reportError() = {
		sys.error("Unexpected transition from state: " + playingState)
	}
	
	private def ensureVolumeTaskStarted() {
		if (!periodicTimer.isStarted)
			periodicTimer.startTask(volumeTask, 10)			
	}
	
	override protected def handlePlay() {
		if (playingState == PLAYING_STOPPED) {
			tones = currentChord.tones
			base = tones(0)
	
			currentStressVol = 0
			ensureVolumeTaskStarted()
	
			for (note <- tones)
				Sampler.noteOn(0, note)
	
			playingState = PLAYING_PRESSED
			isBellowsPressedAsChord = true
		} else reportError()
	}
	
	override protected def handleChordPressed() {
		if (playingState == PLAYING_RELEASED) {
			playingState = PLAYING_PRESSED
			isBellowsPressedAsChord = true
		} else reportError()
	}
	
	override protected def handleChordReleased() {
		if (playingState == PLAYING_PRESSED) {
			playingState = PLAYING_RELEASED
			isBellowsPressedAsChord = false
			
		} else if (playingState == PLAYING_RELEASED) {  // This is the state after play command
			isBellowsPressedAsChord = false			
		
		} else reportError()
	}

	override protected def handleSpacePressed() {
		ensureVolumeTaskStarted()
		isBellowsPressedAsSpace = true
	}
	
	override protected def handleSpaceReleased() {
		isBellowsPressedAsSpace = false
	}
	
	override protected def handleStop() {
		for (note <- tones) 
			Sampler.noteOff(0, note)		
		
		isBellowsPressedAsChord = false

		playingState = PLAYING_STOPPED
	}
	
	override protected def handleReset() {
		handleStop()
		
		periodicTimer.stopTask()
		
		currentBellowsVol = 0
		currentStressVol = 0
		isBellowsPressedAsSpace = false
		
		setVolume(0)
	}
	
	private val maxStressVol = 7
	private val maxBellowsVol = 125 - maxStressVol
		
	private def volumeTask() {
		if (playingState == PLAYING_PRESSED) {
			currentStressVol += 3
			
			if (currentStressVol > maxStressVol) {
				currentStressVol = maxStressVol				
				stressVolDecay += 0.2
				if (stressVolDecay > maxStressVol) {
					stressVolDecay = maxStressVol
				}
			} else {
				stressVolDecay = 0
			}
		} else if (playingState == PLAYING_RELEASED){
			currentStressVol -= 3
			
			if (currentStressVol < 0) { 
				currentStressVol = 0	
			}
			if (stressVolDecay > currentStressVol) {
				stressVolDecay = currentStressVol
			}

		}			
			
		if (isBellowsPressedAsChord || isBellowsPressedAsSpace) {
			if (currentBellowsVol == 0) {
				isFromZero = true
			}
			
			val incr = ((maxBellowsVol + 15 - currentBellowsVol) / 15).floor / (if (isFromZero) 3 else 6)
			
			currentBellowsVol += incr
			
			if (currentBellowsVol > maxBellowsVol) 
				currentBellowsVol = maxBellowsVol
		} else {
			isFromZero = false
			
			val decr = ((maxBellowsVol + 15 - currentBellowsVol) / 15).floor / 9
			
			currentBellowsVol -= decr
			
			if (currentBellowsVol < 0) 
				currentBellowsVol = 0
		}
	
		setVolume((currentBellowsVol + currentStressVol - stressVolDecay).floor.toInt)
	}

	private def setVolume(vol: Int) {
		Sampler.setVolume(0, vol)		
	}
	
}
