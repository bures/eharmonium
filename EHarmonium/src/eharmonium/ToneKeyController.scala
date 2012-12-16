package eharmonium

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.shape.Rectangle
import javafx.scene.input.KeyCode

class ToneKeyController(val mCtrl: MainController, val index: Int, val keyCode: KeyCode, val label: String) {
	@FXML val pane: Pane = null
	@FXML val actualTone: Label = null
	@FXML val keyboardKey: Label = null
	@FXML val rectangle: Rectangle = null

	var isPlaying: Boolean = false
	
	var currentTone: Tone = null
	
	private def getTone = {
		
		val tos = mCtrl.toneOctaveStart 
		
		val relPos = index - mCtrl.toneRootPos

		val relPosMod = relPos % 14
		
		if (relPosMod == ((5 + 14 - tos) % 14) || relPosMod == (13 - tos) || relPosMod == ((-1 - 14 + tos) % 14) || relPosMod == ((-9 - 14 + tos) % 14)) 
			EmptyTone
		else {
			val relTone = relPos - (
				if (relPos >= 0) 
					(relPos + (9 + 14 - tos) % 14) / 14 + // This is how many times we cross index 5 on the way up
					(relPos + (1 + 14 - tos) % 14) / 14 // This is how many times we cross index 13 on the way up
				else
					(relPos + (-5 - 14 + tos) % 14) / 14 + // This is how many times we cross index 5 on the way down
					(relPos + (-13 + tos)) / 14 // This is how many times we cross index 13 on the way down
			)
			
			Tone(mCtrl.key + relTone)
		} 
	}
	
	def updateLabels() {
		assert(!isPlaying)
		
		currentTone = getTone
		
		if (currentTone.isEmpty) {
			if (!rectangle.getStyleClass().contains("toneKeyRectangleNone"))
				rectangle.getStyleClass().add("toneKeyRectangleNone")

			actualTone.setText("")
			keyboardKey.setText("")
		} else {
			rectangle.getStyleClass.remove("toneKeyRectangleNone");
			
			actualTone.setText(currentTone.name)
			// abstractTone.setText(tone.abstractName(mCtrl.key))
			keyboardKey.setText(label)			
		}
	}
	
	def play() {
		if (isPlaying || currentTone.isEmpty) return		

		isPlaying = true
		
		Sampler.noteOn(1, currentTone.toneNo)
			
		rectangle.getStyleClass.add("toneKeyRectanglePressed");
	}
	
	def stop() {
		if (!isPlaying || currentTone.isEmpty) return
		
		isPlaying = false

		Sampler.noteOff(1, currentTone.toneNo)

		rectangle.getStyleClass.remove("toneKeyRectanglePressed");
	}
		
	def initialize() {
		if (index % 2 == 0)
			pane.getStyleClass().add("toneKeyWhite")
		else
			pane.getStyleClass().add("toneKeyBlack")
			
		updateLabels()
	}
}

