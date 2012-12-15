package eharmonium

import java.util.TimerTask
import java.util.Timer

object CurrentPlayStyle {
	val playStyles = Array(PlayStyleRhytmByVolume, PlayStyleArpeggio)
	var currentPlayStyleIdx = 0
	
	def playStyle = playStyles(currentPlayStyleIdx)
	
	def switchToNextPlayStyle() {
		currentPlayStyleIdx = (currentPlayStyleIdx + 1) % playStyles.length
	}

	def switchToPrevPlayStyle() {
		currentPlayStyleIdx = (currentPlayStyleIdx - 1 + playStyles.length) % playStyles.length
	}
}

abstract class PlayStyle {

	var base: Int = 0
	var tones: List[Int] = null
	
	val periodicTimer = new PeriodicTask
	
	def init(base: Int, tones: List[Int]) {
		this.base = base
		this.tones = tones
	}
	
	def play(base: Int, tones: List[Int])
	def pressed()
	def released()
	def stop()
	def reset()
} 

object PlayStyleRhytmByVolume extends PlayStyle {
	val PLAYING_RESET = 0
	val PLAYING_STOPPED = 1
	val PLAYING_PRESSED = 2
	val PLAYING_RELEASED = 3
	var playingState = PLAYING_RESET

	var desiredVolume = 0
	var currentVolume = 0
	
	override def play(base: Int, tones: List[Int]) {
		init(base, tones)
		changePlayingState(PLAYING_PRESSED)
	} 

	override def pressed() {
		changePlayingState(PLAYING_PRESSED)
	} 

	override def released() {
		changePlayingState(PLAYING_RELEASED)
	} 

	override def stop() {
		changePlayingState(PLAYING_STOPPED)
	}
	
	override def reset() {
		changePlayingState(PLAYING_RESET)
	}
	
	def volumeRaiseTask() {
		currentVolume += (150 - currentVolume) / 25
		if (currentVolume >= desiredVolume) {
			periodicTimer.stopTask()
			currentVolume = desiredVolume
		}
		
		Sampler.setVolume(0, currentVolume)
	}
	
	def volumeDecreaseTask() {
		currentVolume -= (150 - currentVolume) / 25
		if (currentVolume <= desiredVolume) {
			periodicTimer.stopTask()
			currentVolume = desiredVolume
		}
		
		Sampler.setVolume(0, currentVolume)
	}
	
	def setVolume(vol: Int) {
		currentVolume = vol
		Sampler.setVolume(0, currentVolume)		
	}
	
	def raiseVolume(to: Int) {
		desiredVolume = to
		periodicTimer.startTask(volumeRaiseTask, if (currentVolume == 0) 20 else 60) // 3
	}
	
	def decreaseVolume(to: Int) {
		desiredVolume = to
		periodicTimer.startTask(volumeDecreaseTask, 80)
	}
	
	def changePlayingState(newPlayingState: Int) {
		(playingState, newPlayingState) match  {
			case (PLAYING_RESET, PLAYING_PRESSED) => {
				raiseVolume(125)
				for (note <- tones) {
					Sampler.noteOn(0, note)
				}
			}
			
			case (PLAYING_STOPPED, PLAYING_PRESSED) => {
				raiseVolume(125)
				for (note <- tones) {
					Sampler.noteOn(0, note)
				}
			}
			
			case (PLAYING_PRESSED, PLAYING_RELEASED) => {
				periodicTimer.stopTask()
				decreaseVolume(0) // 80
			}

			case (PLAYING_RELEASED, PLAYING_PRESSED) => {
				periodicTimer.stopTask()
				raiseVolume(125)				
			}

			case (oldState, newState) => {
				if (oldState != newState) {
					if (newState == PLAYING_STOPPED || newState == PLAYING_RESET) {
						periodicTimer.stopTask()
						
						for (note <- tones)
							Sampler.noteOff(0, note)				

						if (newState == PLAYING_RESET)
							setVolume(0)
					} else {
						sys.error("Unexpected combination of old and new state: " + oldState + " -> " + newState)
					}
				}
			}
		}
		
		playingState = newPlayingState
	} 
}


object PlayStyleArpeggio extends PlayStyle {
	
	val sequence = Array(
			Array(0, 2),
			Array(0, 1, 2),
			Array(0, 1),
			Array(0, 1, 2)
		)
		
	var playingState = 0
	
	var isPlaying = false
	var isPressed = false

	override def play(base: Int, tones: List[Int]) {
		init(base, tones)

		assert(isPlaying == false)
		isPlaying = true
		isPressed = true

		Sampler.setVolume(0, 100)

		playingState = 0
		for (note <- sequence(playingState))
			Sampler.noteOn(0, tones(note))
	} 

	override def pressed() {
		if (!isPressed) {
			playNext()
			isPressed = true
		}
	} 

	override def released() {
		if (isPressed) {
			playNext()
			isPressed = false
		}
	} 

	override def stop() {
		if (isPlaying) 
			for (note <- sequence(playingState))
				Sampler.noteOff(0, tones(note))
		
		isPressed = false
		isPlaying = false		
	} 
	
	override def reset() {
		stop()
		Sampler.setVolume(0, 0)
	}
	
	def playNext() {
		for (note <- sequence(playingState) if (note != 0))
			Sampler.noteOff(0, tones(note))
		
		playingState = (playingState + 1) % sequence.length
		
		for (note <- sequence(playingState) if (note != 0))
			Sampler.noteOn(0, tones(note))
	}
	
}
