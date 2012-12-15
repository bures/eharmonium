package eharmonium

class BellowsChordPlayer extends ChordPlayer {
	private val PLAYING_STOPPED = 1
	private val PLAYING_PRESSED = 2
	private val PLAYING_RELEASED = 3
	
	private var playingState = PLAYING_STOPPED

	private var base: Int = 0
	private var tones: List[Int] = null
	
	private val periodicTimer = new PeriodicTask
	
	private def reportError(newState: Int) = {
		sys.error("Unexpected combination of old and new state: " + playingState + " -> " + newState)
	}
	
	override protected def handlePlay() {
		if (playingState == PLAYING_STOPPED) {
			tones = currentChord.tones
			base = tones(0)
	
			currentStressVol = 0
			periodicTimer.startTask(volumeTask, 10)
	
			for (note <- tones)
				Sampler.noteOn(0, note)
	
			playingState = PLAYING_PRESSED
		} else reportError(PLAYING_PRESSED)
	}
	
	override protected def handleChordPressed() {
		if (playingState == PLAYING_RELEASED) {
			playingState = PLAYING_PRESSED
		} else reportError(PLAYING_PRESSED)
	}
	
	override protected def handleChordReleased() {
		if (playingState == PLAYING_PRESSED) {
			playingState = PLAYING_RELEASED
		} else reportError(PLAYING_PRESSED)
	}

	override protected def handleSpacePressed() {
		isStress = true
	}
	
	override protected def handleSpaceReleased() {
		isStress = false
	}
	
	override protected def handleStop() {
		periodicTimer.stopTask()
		
		for (note <- tones) 
			Sampler.noteOff(0, note)		
		
		playingState = PLAYING_STOPPED
	}
	
	override protected def handleReset() {
		handleStop()
		
		currentBellowsVol = 0
		currentStressVol = 0
		
		setVolume(0)
	}
	
	private var currentBellowsVol : Double = 0
	private var isFromZero = false
	private var currentStressVol : Double = 0
	private var isStress = false
	
	private def volumeTask() {
		val maxBellowsVol = 110
		
		if (playingState == PLAYING_PRESSED) {
			if (currentBellowsVol == 0) {
				isFromZero = true
			}
			
			val incr = ((maxBellowsVol + 15 - currentBellowsVol) / 15).floor / (if (isFromZero) 3 else 6)
			
			currentBellowsVol += incr
			
			if (currentBellowsVol > maxBellowsVol) 
				currentBellowsVol = maxBellowsVol
		} else if (playingState == PLAYING_RELEASED) {
			isFromZero = false
			
			val decr = ((maxBellowsVol + 15 - currentBellowsVol) / 15).floor / 9
			
			currentBellowsVol -= decr
			
			if (currentBellowsVol < 0) 
				currentBellowsVol = 0
		}
		
		if (isStress) {
			currentStressVol += 4
			
			if (currentStressVol > 15) 
				currentStressVol = 15
		} else {
			currentStressVol -= 4
			
			if (currentStressVol < 0) 
				currentStressVol = 0			
		}			
		
		setVolume((currentBellowsVol + currentStressVol).floor.toInt)		
	}

	private def setVolume(vol: Int) {
		Sampler.setVolume(0, vol)		
	}
	
}
