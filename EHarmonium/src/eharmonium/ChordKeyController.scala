package eharmonium

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import javafx.scene.input.KeyCode

object KeyCodeModifier {
	val SHIFT = 1
	val CTRL = 2
}

class ChordKeyController(val mCtrl: MainController, val index: Int, val keyCode: KeyCode, val keyCodeModifier: Int, val label: String, val chordKind: ChordKind) {
	@FXML val abstractChord: Label = null;
	@FXML val actualChord: Label = null;
	@FXML val keyboardKey: Label = null;
	@FXML val rectangle: Rectangle = null;

	var isPressed: Boolean = false;
	var isPlaying: Boolean = false;
	
	def getChord = Chord.nthFifthFrom(mCtrl.key, chordKind, index - mCtrl.chordRootPos + chordKind.relPositionToMajor, mCtrl.chordRelativeHighestNote)
	
	def updateLabels() {
		val chord = getChord
		actualChord.setText(chord.name)
		abstractChord.setText(chord.abstractName(mCtrl.key))
		keyboardKey.setText(label)
	}
	
	def keyPressed() {
		if (isPressed) return
		isPressed = true
		
		if (!isPlaying) {
			if (mCtrl.chordNowPlaying != null) {
				mCtrl.chordNowPlaying.stop()
			}
		
			mCtrl.chordNowPlaying = this
			isPlaying = true
			mCtrl.chordPlayer.play(getChord)
			
			rectangle.getStyleClass.add("chordKeyRectanglePlaying");
		} else {
			mCtrl.chordPlayer.chordPressed()
		}

		rectangle.getStyleClass.add("chordKeyRectanglePressed");
	}
	
	def keyReleased() {
		isPressed = false
		
		if (isPlaying) {
			rectangle.getStyleClass.remove("chordKeyRectanglePressed");
			
			mCtrl.chordPlayer.chordReleased()			
		}
	}
	
	def stopOrReset(isReset: Boolean) {
		assert(mCtrl.chordNowPlaying == this)
		assert(isPlaying)
		mCtrl.chordNowPlaying = null

		rectangle.getStyleClass.remove("chordKeyRectanglePlaying");
		
		if (isPressed) {
			rectangle.getStyleClass.remove("chordKeyRectanglePressed");
		}

		if (isReset) 
			mCtrl.chordPlayer.reset()
		else 
			mCtrl.chordPlayer.stop()
			
		isPlaying = false
	}
	
	def stop() {
		stopOrReset(false)
	}
	
	def reset() {
		stopOrReset(true)
	}
	
	def initialize() {
		updateLabels()
	}
}

